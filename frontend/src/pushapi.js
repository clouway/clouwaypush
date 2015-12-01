/**
 * @author Stefan Dimitrov (stefan.dimitrov@clouway.com).
 */

angular.module('clouway-push', [])

  .provider('pushApi', function () {

    this.subscriberLength = 15;

    this.connectionMethods = {
      connect: angular.noop,
      bind: angular.noop,
      unbind: angular.noop,
      keepAlive: angular.noop
    };

    this.timeIntervals = {
      keepAlive: 60,
      channelReconnect: 5
    };

    /**
     * Set a method to call for opening of connection.
     * The method must return a promise.
     *
     * @param {function(subscriber)} method method that will be called for opening of connection.
     * @returns {*} the provider instance for chaining purposes.
     */
    this.openConnectionMethod = function (method) {
      this.connectionMethods.connect = method;
      return this;
    };

    /**
     * Set a method to call when binding event handler.
     *
     * @param {function(subscriber, eventName, correlationId)} method method that will be called.
     * @returns {*} the provider instance for chaining purposes.
     */
    this.bindMethod = function (method) {
      this.connectionMethods.bind = method;
      return this;
    };

    /**
     * Set a method to call when unbinding event handler.
     *
     * @param {function(subscriber, eventName, correlationId)} method method that will be called.
     * @returns {*} the provider instance for chaining purposes.
     */
    this.unbindMethod = function (method) {
      this.connectionMethods.unbind = method;
      return this;
    };

    /**
     * Set a method to call for sending a keepAlive.
     *
     * @param {function(subscriber)} method method that will be called.
     * @returns {*} the provider instance for chaining purposes.
     */
    this.keepAliveMethod = function (method) {
      this.connectionMethods.keepAlive = method;
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

    this.$get = function ($rootScope, $interval, $timeout, $window) {
      var subscriberLength = this.subscriberLength;
      var connectionMethods = this.connectionMethods;
      var timeIntervals = this.timeIntervals;
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
        connectionMethods.bind(subscriber, eventName, correlationId);
      };

      var unsubscribeFromEvent = function (eventName, correlationId) {
        connectionMethods.unbind(channelSubscriber, eventName, correlationId);
      };

      var keepAlive = function () {
        connectionMethods.keepAlive(channelSubscriber);
      };


      var establishConnection = function () {
        connectionMethods.connect(channelSubscriber).then(function (token) {
          openChannel(token);
          keepAliveInterval = $interval(keepAlive, timeIntervals.keepAlive * 1000);

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
    };
  })

  .config(function (pushApiProvider) {
    var backendServiceUrl = '/pushService';
    var keepAliveInterval = 30; //in seconds
    var reconnectInterval = 10; //in seconds

    var injector = angular.injector(['ng']);
    var $http = injector.get('$http');
    var $q = injector.get('$q');

    /**
     * Helper function to return a promise with the $http response.data field.
     *
     * @param {Promise} httpPromise a promise object from the $http service
     * @returns {Promise} a promise which resolves(or rejects) to the response.data object from the $http promise.
     */
    function dataPromise(httpPromise) {
      var deferred = $q.defer();

      httpPromise.then(function (response) {
        deferred.resolve(response.data);
      }, function (response) {
        deferred.reject(response.data);
      });

      return deferred.promise;
    }

    pushApiProvider.keepAliveTimeInterval(keepAliveInterval)
      .reconnectTimeInterval(reconnectInterval)
      .openConnectionMethod(function (subscriber) {
        var params = {subscriber: subscriber};
        return dataPromise($http.get(backendServiceUrl, {params: params}));
    })
      .bindMethod(function (subscriber, eventName, correlationId) {
        var params = {subscriber: subscriber, eventName: eventName, correlationId: correlationId};
        return dataPromise($http.put(backendServiceUrl, '', {params: params}));
      })
      .unbindMethod(function (subscriber, eventName, correlationId) {
        var params = {subscriber: subscriber, eventName: eventName, correlationId: correlationId};
        return dataPromise($http['delete'](backendServiceUrl, {params: params}));
      })
      .keepAliveMethod(function (subscriber) {
        var params = {subscriber: subscriber};
        return dataPromise($http.post(backendServiceUrl, '', {params: params}));
      })
    ;
  })

;
