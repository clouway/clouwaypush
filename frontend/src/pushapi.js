/**
 * @author Stefan Dimitrov (stefan.dimitrov@clouway.com).
 *
 * setup endpoint :
 *  .config(function (pushApiProvider) {
 *    var backendServiceUrl = '/v1/pushService/token';
 *
 *    pushApiProvider
 *      .backendServiceUrl(backendServiceUrl);
 *  })
 * 
 *
 */
angular.module('clouway-push', [])

  .provider('pushApi', function () {

    /**
     * Sets the token serving path
     * setup endpoint :
     *  .config(function (pushApiProvider) {
     *    var backendServiceUrl = 'v1/pushService/token';
     *
     *    pushApiProvider
     *      .backendServiceUrl(backendServiceUrl);
     *  })
     */
    this.endpoints = {
      serviceUrl: ""
    };

    /**
     * Sets the backend url connection.
     *
     * @param url the new url
     * @returns {*} the provider instance for chaining purposes.
     */
    this.backendServiceUrl = function (url) {
      this.endpoints.serviceUrl = url;
      return this;
    };

    this.$get = function ($rootScope, $timeout, $http, $q) {
      var endpoints = this.endpoints;
      var boundEvents = {};
      var pendingBulkBindings = [];
      var pendingInitialBindings = [];

      var initialFlushTimeout;

      var service = {};
      var firebaseDatabaseRef = {};
      var namespace = '';

      /**
       * Handle push event message.
       *
       * @param {String} message
       */
      var onMessage = function (message) {
        var eventSource = angular.fromJson(message);
        var event = eventSource.event;
        var bindingKey = [event.key, eventSource.correlationId].join("");

        var handlers = boundEvents[bindingKey];

        angular.forEach(handlers, function (handler) {
          handler(event);
        });
      };

      /**
       * Open channel using specified channel token.
       *
       * @param {Object} data the data from server to use for opening channel.
       */
      var openChannel = function (data) {
        return firebase.auth().signInWithCustomToken(data.token).then(function () {
          namespace = data.namespace;
        });
      };

      var subscribeForEvents = function (events) {
        // Used for testing purposes only.
        if (!endpoints.serviceUrl || endpoints.serviceUrl === '') {
          return;
        }

        if (events.length) {
          service.openConnection().then(function () {
            firebaseDatabaseRef = firebase.database().ref(namespace);

            angular.forEach(events, function (event) {
              var fbDbChildRef = firebaseDatabaseRef.child(event.eventName);
              var isInitialBind = true;

              //to skip initial binding of added child
              fbDbChildRef.once('value', function () {
                isInitialBind = false;
              });

              fbDbChildRef.on('child_added', function (result) {
                if (!isInitialBind) {
                  onMessage(result.val());
                }
              });

              fbDbChildRef.on('child_changed', function (result) {
                onMessage(result.val());
              });
            });
          });
        }
      };

      var unsubscribeFromEvent = function (eventName) {
        // Used for testing purposes only.
        if (!endpoints.serviceUrl || endpoints.serviceUrl === '') {
          return;
        }

        firebaseDatabaseRef.child(eventName).off();
      };


      service.openConnection = function () {
        if (namespace) {
          return $q.resolve();
        }

        return $http.get(endpoints.serviceUrl).then(function (response) {
          return openChannel(response.data);
        });
      };

      /**
       * Fire a push event
       *
       * Only for testing used for now
       * @param {string} eventKey to be fired.
       * @param {string} correlationId event correlationId
       * @param {Object} data data object of the event.
       */
      service.fireSpecificEvent = function (eventKey, correlationId, data) {
        var event = angular.extend({key: eventKey}, data);
        var eventSource = {correlationId: correlationId, event: event};
        onMessage(eventSource);
      };

      /**
       * Fire a push event
       *
       * Only for testing used for now
       * @param {string} eventKey to be fired.
       * @param {Object} data data object of the event.
       */
      service.fireEvent = function (eventKey, data) {
        service.fireSpecificEvent(eventKey, "", data);
      };

      /**
       * Bind handler to push event.
       *
       * @param {String} eventName name of the push event to which to bind the handler.
       * @param {Function} handler handler to be called when the event occurs.
       * @returns {Function} the bound handler.
       */
      service.bind = function (eventName, handler) {
        return service.bindId(eventName, '', handler);
      };

      /**
       * Bind handler to push event.
       *
       * @param {String} eventName name of the push event to which to bind the handler.
       * @param {String} correlationId additional Id for the event.
       * @param {Function} handler handler to be called when the event occurs.
       * @returns {Function} the bound handler.
       */
      service.bindId = function (eventName, correlationId, handler) {
        var singlePending = [];
        // Add binding to temp list and flush it immediately
        var eventHandler = addPendingBinding(singlePending, eventName, correlationId, handler);

        // Add to initial bindings list if there are any, instead of flushing directly.
        if (pendingInitialBindings.length) {
          pendingInitialBindings.push(singlePending[0]);

        } else {
          flushPendingBindings(singlePending);
        }

        return eventHandler;
      };

      /**
       * Add binding of push event handler to pending bulk.
       * After flushing all bindings from bulk will be applied at once.
       *
       * @param {String} eventName name of the push event to which to bind the handler.
       * @param {Function} handler handler to be called when the event occurs.
       * @returns {Function} the bound handler.
       */
      service.bulkBind = function (eventName, handler) {
        return service.bulkBindId(eventName, '', handler);
      };

      /**
       * Add binding of push event handler to pending bulk.
       * After flushing all bindings from bulk will be applied at once.
       *
       * @param {String} eventName name of the push event to which to bind the handler.
       * @param {String} correlationId additional Id for the event.
       * @param {Function} handler handler to be called when the event occurs.
       * @returns {Function} the bound handler.
       */
      service.bulkBindId = function (eventName, correlationId, handler) {
        return addPendingBinding(pendingBulkBindings, eventName, correlationId, handler);
      };

      /**
       * Flush all pending bulk bindings.
       */
      service.flushBulkBind = function () {
        // If there are initial bindings the flush of bulk will be done later with them.
        if (pendingInitialBindings.length) {
          return;
        }
        flushPendingBindings(pendingBulkBindings);
      };

      /**
       * Add binding of push event handler to pending initial bindings.
       * After the main application initiates, all the added bindings will be applied at once.
       *
       * @param {String} eventName name of the push event to which to bind the handler.
       * @param {Function} handler handler to be called when the event occurs.
       * @returns {Function} the bound handler.
       */
      service.initialBind = function (eventName, handler) {
        return service.initialBindId(eventName, '', handler);
      };

      /**
       * Add binding of push event handler to pending initial bindings.
       * After the main application initiates, all the added bindings will be applied at once.
       *
       * @param {String} eventName name of the push event to which to bind the handler.
       * @param {String} correlationId additional Id for the event.
       * @param {Function} handler handler to be called when the event occurs.
       * @returns {Function} the bound handler.
       */
      service.initialBindId = function (eventName, correlationId, handler) {
        var boundHandler = addPendingBinding(pendingInitialBindings, eventName, correlationId, handler);

        // Set initial flush
        if (angular.isUndefined(initialFlushTimeout)) {
          initialFlushTimeout = $timeout(function () {
            // Flush the combined initial and bulk bindings
            flushPendingBindings(pendingInitialBindings.concat(pendingBulkBindings));

            // Clear both bindings lists
            pendingInitialBindings.splice(0, pendingInitialBindings.length);
            pendingBulkBindings.splice(0, pendingBulkBindings.length);
          });
        }

        return boundHandler;
      };

      /**
       * Return whether there are any pending bulk bindings.
       *
       * @returns {boolean}
       */
      service.isBulkBindPending = function () {
        return pendingBulkBindings.length > 0;
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

      /**
       * Add pending event binding to the specified bindings list.
       *
       * @param {Array} pendingBindings the list to add pending event bindings to.
       * @param {String} eventName name of the push event to which to bind the handler
       * @param {String} correlationId additional Id for the event
       * @param {Function} handler handler to be called when the event occurs
       * @returns {Function} the bound handler
       */
      function addPendingBinding(pendingBindings, eventName, correlationId, handler) {
        if (!correlationId) {
          correlationId = '';
        }
        var eventKey = eventName + correlationId;

        var eventHandler = function (data) {
          handler(data);
          $rootScope.$apply();
        };

        pendingBindings.push({eventKey: eventKey, eventName: eventName, handler: eventHandler});

        return eventHandler;
      }

      /**
       * Flush all pending bindings from the specified list.
       *
       * @param {Array} pendingBindings the list of pending bindings to flush.
       */
      function flushPendingBindings(pendingBindings) {
        // Do nothing if bindings list is empty
        if (!pendingBindings.length) {
          return;
        }

        var uniqueEventsList = [];
        angular.forEach(pendingBindings, function (each) {
          var eventKey = each.eventKey;

          if (!(eventKey in boundEvents)) {
            boundEvents[eventKey] = [];

            // Only add events that haven't been subscribed for
            uniqueEventsList.push({eventKey: eventKey, eventName: each.eventName});
          }
          // Add handler binding
          boundEvents[eventKey].push(each.handler);
        });

        // Clear list of pending bindings
        pendingBindings.splice(0, pendingBindings.length);
        subscribeForEvents(uniqueEventsList);
      }

      return service;
    };
  })

  /**
   * @ngdoc directive
   * @name pushHandler
   * @restrict E
   *
   * @description
   * Binds push event handler and takes care of unbinding it when the directive is destroyed.
   *
   * @example
   * As element:
   * <push-handler event="MyPushEvent" correlation-id="vm.eventId" on-event="vm.handleEvent(data)"></push-handler>
   *
   *
   * @param {''} event - name of push event to bind handler to.
   * @param {String} [correlationId] - correlationId for push event.
   * @param {function(data)} onEvent - handler method to bind to push event.
   */
  .directive('pushHandler', function ($parse, pushApi) {
    return {
      restrict: 'E',
      link: function (scope, elem, attrs) {
        var eventName = attrs.event;
        var correlationId = $parse(attrs.correlationId)(scope);
        var onPushEvent = $parse(attrs.onEvent);

        elem.remove();

        var handler = pushApi.bindId(eventName, correlationId, function (eventData) {
          onPushEvent(scope, {$event: eventData});
        });

        scope.$on('$destroy', function () {
          pushApi.unbindId(eventName, correlationId, handler);
        });
      }
    };
  })

;
