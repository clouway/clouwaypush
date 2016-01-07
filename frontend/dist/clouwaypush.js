/**
 * clouwaypush - 2016-01-08
 *
 * Copyright (c) 2016 clouWay ltd
 */
(function ( window, angular, undefined ) {

/**
 * @author Stefan Dimitrov (stefan.dimitrov@clouway.com).
 */
angular.module('clouway-push', [])

  .provider('pushApi', function () {

    this.subscriberLength = 15;

    this.endpoints = {
      serviceUrl: ""
    };

    this.timeIntervals = {
      keepAlive: 60,
      channelReconnect: 5
    };

    /**
     * Sets the backend url connection.
     *
     * @param url the new url
     * @returns {*} the provider instance for chaining purposes.
     */
    this.backendServiceUrl = function(url) {
      this.endpoints.serviceUrl = url;
      return this;
    };


    /**
     * Set a time interval for sending a keepAlive.
     *
     * @param {number} seconds time in seconds between each keepAlive.
     * @returns {*} the provider instance for chaining purposes.
     */
    this.keepAliveTimeInterval = function (seconds) {
      this.timeIntervals.keepAlive = seconds;
      return this;
    };

    /**
     * Set a time interval for attempting a reconnect.
     *
     * @param {number} seconds time in seconds between each reconnect attempt.
     * @returns {*} the provider instance for chaining purposes.
     */
    this.reconnectTimeInterval = function (seconds) {
      this.timeIntervals.channelReconnect = seconds;
      return this;
    };

    this.$get = ["$rootScope", "$interval", "$timeout", "$window", "$http", function ($rootScope, $interval, $timeout, $window, $http) {
      var subscriberLength = this.subscriberLength;
      var timeIntervals = this.timeIntervals;
      var endpoints = this.endpoints;
      var boundEvents = {};
      var channelSubscriber;
      var keepAliveInterval;

      var service = {};

      /**
       * Generate a random alpha-numeric subscriber.
       *
       * @returns {string} the generated subscriber.
       */
      var generateSubscriber = function (length) {
        var symbols = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456';
        var generated = [];
        var i = 0;

        while (i < length) {
          i++;
          var character = Math.floor(Math.random() * symbols.length);
          generated.push(symbols.charAt(character));
        }

        return generated.join('');
      };

      /**
       * Handle push event message.
       *
       * @param {Object} message
       */
      var onMessage = function (message) {
        var messageData = message.data;
        var eventData = angular.fromJson(messageData);
        var handlers = boundEvents[eventData.event];

        angular.forEach(handlers, function (handler) {
          handler(eventData);
        });
      };

      /**
       * Open channel using specified channel token.
       *
       * @param {String} channelToken the token to use for opening channel.
       */
      var openChannel = function (channelToken) {
        var channel = new goog.appengine.Channel(channelToken);
        var socket = channel.open();

        socket.onmessage = onMessage;

        socket.onerror = function (errorMessage) {
          establishConnection();
        };
      };

      var subscribeForEvent = function (eventName, correlationId) {
        var subscriber = service.openConnection();
        var params = {
          subscriber: subscriber,
          eventName: eventName,
          correlationId: correlationId
        };

        // Used for testing purposes only.
        if (!endpoints.serviceUrl || endpoints.serviceUrl === '') {
          return;
        }
        $http.put(endpoints.serviceUrl, '', {params: params}).then(function (data) {

        });
      };

      var unsubscribeFromEvent = function (eventName, correlationId) {
        var params = {subscriber: channelSubscriber, eventName: eventName, correlationId: correlationId};

        // Used for testing purposes only.
        if (!endpoints.serviceUrl || endpoints.serviceUrl === '') {
          return;
        }
        $http['delete'](endpoints.serviceUrl, {params: params}).then(function(data) {

        });
      };

      var keepAlive = function () {
        var params = {subscriber: channelSubscriber};
        $http.post(endpoints.serviceUrl, '', {params: params}).then(function(data) {

        });
      };


      var establishConnection = function () {
        var params = {subscriber: channelSubscriber};
        $http.get(endpoints.serviceUrl, {params: params}).then(function (response) {
          openChannel(response.data);

          if (!keepAliveInterval) { // Create only if there is no existing interval set.
            keepAliveInterval = $interval(keepAlive, timeIntervals.keepAlive * 1000);
          }

        }, function () {
          // Retry connection after time interval.
          $timeout(establishConnection, timeIntervals.channelReconnect * 1000, true);
        });
      };


      /**
       * Open connection for the specified subscriber.
       *
       * @param {String} subscriber subscriber to open connection for.
       */
      service.openConnection = function (subscriber) {
        if (channelSubscriber) {
          return channelSubscriber;
        }

        if (!subscriber) {
          subscriber = generateSubscriber(subscriberLength);
        }

        channelSubscriber = subscriber;
        establishConnection(subscriber);

        return subscriber;
      };

      /**
       * Fire a push event
       *
       * @param {string} event event to be fired
       * @param {Object} data data object of the event
       */
      service.fireEvent = function (event, data) {
        var eventData = angular.extend({event: event}, data);
        onMessage({data: eventData});
      };

      /**
       * Bind handler to push event.
       *
       * @param {String} eventName name of the push event to which to bind the handler
       * @param {Function} handler handler to be called when the event occurs
       * @returns {Function} the bound handler
       */
      service.bind = function (eventName, handler) {
        return service.bindId(eventName, '', handler);
      };

      /**
       * Bind handler to push event.
       *
       * @param {String} eventName name of the push event to which to bind the handler
       * @param {String} correlationId additional Id for the event
       * @param {Function} handler handler to be called when the event occurs
       * @returns {Function} the bound handler
       */
      service.bindId = function (eventName, correlationId, handler) {
        if (!correlationId) {
          correlationId = '';
        }
        var eventKey = eventName + correlationId;
        if (angular.isUndefined(boundEvents[eventKey])) {
          boundEvents[eventKey] = [];
        }

        var eventHandler = function (data) {
          handler(data);
          $rootScope.$apply();
        };

        subscribeForEvent(eventName, correlationId);
        boundEvents[eventKey].push(eventHandler);

        return eventHandler;
      };

      /**
       * Unbind handler/handlers from push event.
       * If no handler is specified then unbind all bound handlers from the event.
       *
       * @param {String} eventName name of the event from which to unbind the handler/handlers
       * @param {Function} [handler] the handler to be unbound from the event. If not defined, unbind all handlers for the event.
       */
      service.unbind = function (eventName, handler) {
        service.unbindId(eventName, '', handler);
      };

      /**
       * Unbind handler/handlers from push event.
       * If no handler is specified then unbind all bound handlers from the event.
       *
       * @param {String} eventName name of the event from which to unbind the handler/handlers
       * @param {String} correlationId additional Id of the event
       * @param {Function} [handler] the handler to be unbound from the event. If not defined, unbind all handlers for the event.
       */
      service.unbindId = function (eventName, correlationId, handler) {
        if (!correlationId) {
          correlationId = '';
        }
        var eventKey = eventName + correlationId;
        if (!(eventKey in boundEvents)) {
          return;
        }

        if (angular.isUndefined(handler)) {
          unsubscribeFromEvent(eventName, correlationId);
          delete boundEvents[eventKey];
          return;
        }

        var handlerIndex = boundEvents[eventKey].indexOf(handler);
        if (handlerIndex < 0) {
          return;
        }

        boundEvents[eventKey].splice(handlerIndex, 1);
        if (!boundEvents[eventKey].length) {
          unsubscribeFromEvent(eventName, correlationId);
          delete boundEvents[eventKey];
        }
      };

      return service;
    }];
  })

  .config(["pushApiProvider", function (pushApiProvider) {
    var backendServiceUrl = '/pushService';
    var keepAliveInterval = 30; //in seconds
    var reconnectInterval = 10; //in seconds

    pushApiProvider
            .backendServiceUrl(backendServiceUrl)
            .keepAliveTimeInterval(keepAliveInterval)
            .reconnectTimeInterval(reconnectInterval);
  }])

;

})( window, window.angular );
