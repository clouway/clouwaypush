/**
 * @author Stefan Dimitrov (stefan.dimitrov@clouway.com).
 */

describe('PushApi', function () {
  window.firebase = {
    __ref: {
      events: {
        'child_added': [],
        'child_changed': [],
        'value': []
      },

      push: function (data) {
        angular.forEach(this.events["value"], function (each) {
          each(data);
        });

        angular.forEach(this.events["child_added"], function (each) {
          each(data);
        });
      },

      update: function (data) {
        angular.forEach(this.events["child_changed"], function (each) {
          each(data);
        });
      },

      child: function () {
        return {
          on: function (eventType, callback) {
            window.firebase.__ref.events[eventType].push(callback);
          },
          off: function () {
          },
          once: function (eventType, callback) {
            window.firebase.__ref.events[eventType].push(callback);
          }
        };
      }
    },

    database: function () {
      return {
        ref: function () {
          return window.firebase.__ref;
        },

        $clean: function () {
          window.firebase.__ref.events['child_added'] = [];
          window.firebase.__ref.events['child_changed'] = [];
        }
      };
    },

    auth: function () {
      return {
        signInWithCustomToken: function () {
          return {
            then: function (onSuccess) {
              onSuccess();
            }
          };
        }
      };
    }
  };
  beforeEach(module('clouway-push'));

  var pushApi;

  describe('open firebase connection', function () {
    var httpBackend;
    var callback1, callback2, callback3;

    beforeEach(function () {
      module(function (pushApiProvider) {
        pushApiProvider
          .backendServiceUrl("/pushService/credentials");
      });

      inject(function (_pushApi_, $httpBackend) {
        pushApi = _pushApi_;
        httpBackend = $httpBackend;

        httpBackend.expectGET('/pushService/credentials').respond(200, {token: "token1", namespace: 'namespace'});
        pushApi.openConnection();

        callback1 = jasmine.createSpy('callback1');
        callback2 = jasmine.createSpy('callback2');
        callback3 = jasmine.createSpy('callback3');

        firebase = window.firebase;
        ref = firebase.database().ref();
      });
    });

    afterEach(function () {
      httpBackend.verifyNoOutstandingExpectation();
      httpBackend.verifyNoOutstandingRequest();
      firebase.database().$clean();
    });


    it('subscribe for added event and receive message for it', function () {
      var eventName = 'fake-event';
      var callback = jasmine.createSpy('callback');

      expectBindCall();

      pushApi.bind(eventName, callback);
      httpBackend.flush();

      ref.push({
        val: function () {
          return {event: {key: eventName}};
        }
      });

      expect(callback).toHaveBeenCalledWith({key: eventName});
    });


    it('subscribes for single event multiple times', function () {
      var eventName = 'fake-event';

      expectBindCall();

      pushApi.bind(eventName, callback1);
      pushApi.bind(eventName, callback2);
      pushApi.bind(eventName, callback3);

      httpBackend.flush();

      var messageData = {key: eventName};
      ref.update({
        val: function () {
          return {event: messageData};
        }
      });

      expect(callback1).toHaveBeenCalledWith(messageData);
      expect(callback2).toHaveBeenCalledWith(messageData);
      expect(callback3).toHaveBeenCalledWith(messageData);
    });


    it('subscribes for single event with correlationIds', function () {

      var eventName = 'fake-event';
      var correlationIdA = 'id-08433';
      var correlationIdB = 'id-12345';

      expectBindCall();
      expectBindCall();

      pushApi.bindId(eventName, correlationIdA, callback1);
      pushApi.bindId(eventName, correlationIdB, callback2);

      httpBackend.flush();

      ref.update({
        val: function () {
          return {event: {key: eventName}, correlationId: correlationIdA};
        }
      });

      expect(callback1).toHaveBeenCalledWith({key: eventName});
      expect(callback2).not.toHaveBeenCalled();
    });


    it('subscribes for event with undefined correlationId', function () {
      var eventName = 'another-fake-event';

      expectBindCall();

      pushApi.bindId(eventName, undefined, callback1);

      httpBackend.flush();

      ref.update({
        val: function () {
          return {event: {key: eventName}};
        }
      });

      expect(callback1).toHaveBeenCalledWith({key: eventName});
    });


    it('not call non-bound event handlers', function () {
      var eventName = 'fake-event';

      expectBindCall();

      var boundCallback1 = pushApi.bind(eventName, callback1);
      pushApi.bind(eventName, callback2);
      var boundCallback3 = pushApi.bind(eventName, callback3);

      pushApi.unbind(eventName, boundCallback1);
      pushApi.unbind(eventName, boundCallback3);

      httpBackend.flush();

      var messageData = {key: eventName};
      ref.update({
        val: function () {
          return {event: messageData};
        }
      });

      expect(callback1).not.toHaveBeenCalled();
      expect(callback2).toHaveBeenCalledWith(messageData);
      expect(callback3).not.toHaveBeenCalled();
    });


    it('not unbind handler for non-existing event', function () {
      var eventName = 'fake-event';
      expectBindCall();

      var boundCallback = pushApi.bind(eventName, callback1);

      httpBackend.flush();

      pushApi.unbind('non-existing-event', boundCallback);

      var messageData = {key: eventName};
      ref.update({
        val: function () {
          return {event: messageData};
        }
      });

      expect(callback1).toHaveBeenCalled();
    });


    it('not unbind handler not for event', function () {
      var eventName = 'fake-event';
      var otherEvent = 'other-event';

      expectBindCall();
      expectBindCall();

      pushApi.bind(eventName, callback1);
      var boundCallback2 = pushApi.bind(otherEvent, callback2);

      pushApi.unbind(eventName, boundCallback2);

      httpBackend.flush();
      var messageData = {key: eventName};
      ref.update({
        val: function () {
          return {event: messageData};
        }
      });

      expect(callback1).toHaveBeenCalled();
    });


    it('unbind all handlers for event', function () {
      var eventName = 'fake-event';

      expectBindCall(eventName);

      pushApi.bind(eventName, callback1);
      pushApi.bind(eventName, callback2);
      pushApi.bind(eventName, callback3);

      httpBackend.flush();
      pushApi.unbind(eventName);

      var messageData = {key: eventName};
      ref.push({
        val: function () {
          return {event: messageData};
        }
      });

      expect(callback1).not.toHaveBeenCalled();
      expect(callback2).not.toHaveBeenCalled();
      expect(callback3).not.toHaveBeenCalled();
    });


    it('unbind all handlers for event with correlationId', function () {
      var eventName = 'fake-event';

      var correlationIdA = 'id-12345';
      var correlationIdB = 'id-75980';

      expectBindCall(eventName, correlationIdB);
      expectBindCall(eventName, correlationIdA);

      pushApi.bindId(eventName, correlationIdB, callback1);
      pushApi.bindId(eventName, correlationIdA, callback2);
      pushApi.bindId(eventName, correlationIdB, callback3);
      httpBackend.flush();

      pushApi.unbindId(eventName, correlationIdB);

      ref.update({
        val: function () {
          return {event: {key: eventName}, correlationId: correlationIdB};
        }
      });

      ref.update({
        val: function () {
          return {event: {key: eventName}, correlationId: correlationIdA};
        }
      });

      expect(callback1).not.toHaveBeenCalled();
      expect(callback2).toHaveBeenCalled();
      expect(callback3).not.toHaveBeenCalled();
    });


    it('unbind all handlers for event with undefined correlationId', function () {
      var eventName = 'fake-event';
      var correlationIdA = 'id-12345';

      expectBindCall();
      expectBindCall();

      pushApi.bind(eventName, callback1);
      pushApi.bindId(eventName, correlationIdA, callback2);
      pushApi.bind(eventName, callback3);

      httpBackend.flush();

      pushApi.unbindId(eventName, undefined);

      ref.update({
        val: function () {
          return {event: {key: eventName}};
        }
      });

      ref.update({
        val: function () {
          return {event: {key: eventName}, correlationId: correlationIdA};
        }
      });

      expect(callback1).not.toHaveBeenCalled();
      expect(callback2).toHaveBeenCalled();
      expect(callback3).not.toHaveBeenCalled();
    });


    it('call backend unbind only when last handler is unbound', function () {
      var eventName = 'fake-event';

      expectBindCall();

      var callback1 = angular.noop;
      var callback2 = angular.noop;
      var boundCallback1 = pushApi.bind(eventName, callback1);
      var boundCallback2 = pushApi.bind(eventName, callback2);
      httpBackend.flush();

      pushApi.unbind(eventName, boundCallback2);
      pushApi.unbind(eventName, boundCallback1);
    });


    it('call backend unbind with correlationId', function () {
      var eventName = 'fake-event';
      var correlationId = 'id-12345';

      expectBindCall();

      var callback1 = angular.noop;
      var callback2 = angular.noop;

      var boundCallback1 = pushApi.bindId(eventName, correlationId, callback1);
      var boundCallback2 = pushApi.bindId(eventName, correlationId, callback2);
      httpBackend.flush();

      pushApi.unbindId(eventName, correlationId, boundCallback2);
      pushApi.unbindId(eventName, correlationId, boundCallback1);

    });


    it('binds and unbinds from event with undefined correlationId', function () {
      var eventName = 'fake-event';
      var callback = angular.noop;

      expectBindCall();

      var boundCallback = pushApi.bind(eventName, callback);
      httpBackend.flush();

      pushApi.unbindId(eventName, undefined, boundCallback);
    });

    it('fires an event', function () {
      var manualEvent = 'manual-event';

      expectBindCall();

      pushApi.bind(manualEvent, callback1);

      pushApi.fireEvent(manualEvent, {data: 'dummy'});

      httpBackend.flush();

      expect(callback1).toHaveBeenCalledWith({data: 'dummy', key: manualEvent});
    });


    it('does not bind immediately when initial bindings', function () {
      httpBackend.flush();
      var eventName = 'fake-event';

      pushApi.initialBind('initial-event-1', callback1);
      pushApi.initialBind('initial-event-2', callback2);
      pushApi.bind(eventName, callback3);

      httpBackend.verifyNoOutstandingRequest();
    });


    describe('bulk bindings', function () {

      it('bulk binds several events', function () {

        pushApi.bulkBindId('eventA', 'id-12345', callback1);
        pushApi.bulkBind('eventB', callback2);
        pushApi.bulkBind('eventC', callback3);

        httpBackend.verifyNoOutstandingRequest();

        expectBindCall();
        pushApi.flushBulkBind();
        httpBackend.flush();
      });

      it('binds callback only after flushing bulk', function () {
        var eventName = 'fake-event';
        var messageData = {key: eventName};

        pushApi.bulkBind(eventName, callback1);

        ref.update({
          val: function () {
            return {event: messageData};
          }
        });

        expect(callback1).not.toHaveBeenCalled();

        expectBindCall();
        pushApi.flushBulkBind();
        httpBackend.flush();

        ref.update({
          val: function () {
            return {event: messageData};
          }
        });

        expect(callback1).toHaveBeenCalledWith(messageData);
      });

      it('flushes bulk with no pending binds', function () {
        httpBackend.flush();
        pushApi.flushBulkBind();
        httpBackend.verifyNoOutstandingRequest();
      });

      it('does not flush bulk when initial bindings', function () {
        httpBackend.flush();
        pushApi.bulkBind('bulk-event', callback1);
        pushApi.initialBind('initial-event', callback2);

        pushApi.flushBulkBind();
        httpBackend.verifyNoOutstandingRequest();
      });

      it('checks for pending bulk', function () {

        expect(pushApi.isBulkBindPending()).toBe(false);

        pushApi.bulkBind("fake-event", angular.noop);
        expect(pushApi.isBulkBindPending()).toBe(true);

        expectBindCall();
        pushApi.flushBulkBind();
        expect(pushApi.isBulkBindPending()).toBe(false);

        httpBackend.flush();
      });

      it('does not mix with regular bindings', function () {
        pushApi.bulkBind('bulk-event1', callback1);
        pushApi.bulkBind('bulk-event2', callback2);

        expectBindCall();
        pushApi.bind('event3', callback3);

        httpBackend.flush();
        expect(pushApi.isBulkBindPending()).toBe(true);
      });

    });

    describe('initial binding', function () {

      var $timeout;
      beforeEach(inject(function (_$timeout_) {
        $timeout = _$timeout_;
      }));

      it('initial binds several events', function () {
        pushApi.initialBindId('eventA', 'id-12345', callback1);
        pushApi.initialBind('eventB', callback2);
        pushApi.initialBind('eventC', callback3);

        httpBackend.verifyNoOutstandingRequest();

        expectBindCall();
        $timeout.flush(1);
        httpBackend.flush();
      });

      it('binds callback only after flushing initials', function () {
        var eventName = 'fake-event';
        var messageData = {key: eventName};

        pushApi.initialBind(eventName, callback1);

        ref.update({
          val: function () {
            return {event: messageData};
          }
        });

        expect(callback1).not.toHaveBeenCalled();

        expectBindCall();
        $timeout.flush(1);
        httpBackend.flush();

        ref.update({
          val: function () {
            return {event: messageData};
          }
        });

        expect(callback1).toHaveBeenCalledWith(messageData);
      });

      it('flushes initials with no pending binds', function () {
        httpBackend.flush();
        $timeout.flush(1);
        httpBackend.verifyNoOutstandingRequest();
      });

      it('flushes initials and bulk binds', function () {

        pushApi.bulkBind('bulk-event', callback1);
        pushApi.initialBind('initial-event', callback2);

        expectBindCall();
        $timeout.flush(1);

        // Verify bulk bindings list cleared
        expect(pushApi.isBulkBindPending()).toBe(false);

        // Verify initial bindings list also cleared
        // (because bulk binds don't flush when there are pending initial binds)
        pushApi.bulkBind('verify-event', callback3);
        expectBindCall();
        pushApi.flushBulkBind();
        httpBackend.flush();
      });

      it('flushes initials and single bindings', inject(function ($timeout) {
        var eventName = 'single-event';

        pushApi.initialBind('initial-event-1', callback1);
        pushApi.initialBind('initial-event-2', callback2);
        pushApi.bind(eventName, callback3);

        expectBindCall();
        $timeout.flush(1);
        httpBackend.flush();

        ref.update({
          val: function () {
            return {event: {key: eventName}};
          }
        });

        expect(callback3).toHaveBeenCalledWith({key: eventName});
      }));

      it('does not mix with regular bindings', function () {

        expectBindCall('event3');
        pushApi.bind('event3', callback3);

        httpBackend.flush();

        $timeout.flush(1);
        httpBackend.verifyNoOutstandingRequest();
      });

    });

    function expectBindCall() {
      httpBackend.expectGET('/pushService/credentials').respond(200, {token: 'token1', namespace: 'namespace'});
    }
  });

  describe('Connection', function () {
    var httpBackend, firebase;

    beforeEach(function () {
      module(function (pushApiProvider) {
        pushApiProvider
          .backendServiceUrl("/pushService/credentials");
      });

      inject(function (_pushApi_, $httpBackend) {
        pushApi = _pushApi_;
        httpBackend = $httpBackend;

        firebase = window.firebase;
      });
    });

    afterEach(function () {
      httpBackend.verifyNoOutstandingExpectation();
      httpBackend.verifyNoOutstandingRequest();
    });


    it('opens new one', function () {
      httpBackend.expectGET('/pushService/credentials').respond(200, {token: "token1", namespace: 'namespace'});
      pushApi.openConnection("subscriber1");

      httpBackend.flush();
    });

    it('reuses existing connection on second open', function () {
      httpBackend.expectGET('/pushService/credentials').respond(200, {token: 'token', namespace: 'namespace'});

      pushApi.openConnection();
      httpBackend.flush();

      pushApi.openConnection();
      pushApi.openConnection();
    });
  });
});