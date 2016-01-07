/**
 * @author Stefan Dimitrov (stefan.dimitrov@clouway.com).
 */

describe('PushApi', function () {

  beforeEach(module('clouway-push'));

  var pushApi, socket, rootScope;
  var subscriber = 'test-subscriber';
  var channelToken = 'fake-channel-token';
  var $window = {};

  describe('after established connection ', function () {
     var httpBackend;
     var keepAliveInterval = 5; //in seconds
     var callback1, callback2, callback3;

     beforeEach(function () {
       module(function (pushApiProvider) {
         pushApiProvider
                 .keepAliveTimeInterval(keepAliveInterval)
                 .backendServiceUrl("/pushService");
       });

       inject(function (_pushApi_, $httpBackend) {
         pushApi = _pushApi_;
         httpBackend = $httpBackend;

         httpBackend.expectGET('/pushService?subscriber=subscriber1').respond(200, "token1");
         pushApi.openConnection("subscriber1");
         httpBackend.flush();

         callback1 = jasmine.createSpy('callback1');
         callback2 = jasmine.createSpy('callback2');
         callback3 = jasmine.createSpy('callback3');

         socket = goog.appengine.Socket._get("token1");
       });
     });

     afterEach(function () {
        httpBackend.verifyNoOutstandingExpectation();
        httpBackend.verifyNoOutstandingRequest();
     });


    it('subscribes for event and receive message for it', function () {
      var eventName = 'fake-event';
      var callback = jasmine.createSpy('callback');

      expectBindCall(eventName);

      pushApi.bind(eventName, callback);
      httpBackend.flush();

      socket.onmessage({data: angular.toJson({event: eventName})});

      expect(callback).toHaveBeenCalledWith({event: eventName});
    });


    it('subscribes for single event multiple times', function () {
      var eventName = 'fake-event';

      expectBindCall(eventName);
      expectBindCall(eventName);
      expectBindCall(eventName);

      pushApi.bind(eventName, callback1);
      pushApi.bind(eventName, callback2);
      pushApi.bind(eventName, callback3);

      httpBackend.flush();

      var messageData = {event: eventName};
      socket.onmessage({data: angular.toJson(messageData)});

      expect(callback1).toHaveBeenCalledWith(messageData);
      expect(callback2).toHaveBeenCalledWith(messageData);
      expect(callback3).toHaveBeenCalledWith(messageData);
    });


    it('subscribes for single event with correlationIds', function () {

      var eventName = 'fake-event';
      var correlationIdA = 'id-08433';
      var correlationIdB = 'id-12345';

      expectBindCall(eventName, correlationIdA);
      expectBindCall(eventName, correlationIdB);

      pushApi.bindId(eventName, correlationIdA, callback1);
      pushApi.bindId(eventName, correlationIdB, callback2);

      httpBackend.flush();

      socket.onmessage({data: angular.toJson({event: eventName + correlationIdA})});

      expect(callback1).toHaveBeenCalledWith({event: eventName + correlationIdA});
      expect(callback2).not.toHaveBeenCalled();
    });


    it('subscribes for event with undefined correlationId', function () {
      var eventName = 'another-fake-event';

      expectBindCall(eventName);

      pushApi.bindId(eventName, undefined, callback1);

      httpBackend.flush();

      socket.onmessage({data: angular.toJson({event: eventName})});

      expect(callback1).toHaveBeenCalledWith({event: eventName});
    });


    it('not call non-bound event handlers', function () {
      var eventName = 'fake-event';

      expectBindCall(eventName);
      expectBindCall(eventName);
      expectBindCall(eventName);

      var boundCallback1 = pushApi.bind(eventName, callback1);
      var boundCallback2 = pushApi.bind(eventName, callback2);
      var boundCallback3 = pushApi.bind(eventName, callback3);

      pushApi.unbind(eventName, boundCallback1);
      pushApi.unbind(eventName, boundCallback3);

      httpBackend.flush();

      var messageData = {event: eventName};
      socket.onmessage({data: angular.toJson(messageData)});

      expect(callback1).not.toHaveBeenCalled();
      expect(callback2).toHaveBeenCalledWith(messageData);
      expect(callback3).not.toHaveBeenCalled();
    });


    it('not unbind handler for non-existing event', function () {
      var eventName = 'fake-event';
      expectBindCall(eventName);

      var boundCallback = pushApi.bind(eventName, callback1);

      httpBackend.flush();

      pushApi.unbind('non-existing-event', boundCallback);

      var messageData = {event: eventName};
      socket.onmessage({data: angular.toJson(messageData)});

      expect(callback1).toHaveBeenCalled();
    });


    it('not unbind handler not for event', function () {
      var eventName = 'fake-event';
      var otherEvent = 'other-event';

      expectBindCall(eventName);
      expectBindCall(otherEvent);

      var boundCallback1 = pushApi.bind(eventName, callback1);
      var boundCallback2 = pushApi.bind(otherEvent, callback2);

      pushApi.unbind(eventName, boundCallback2);

      httpBackend.flush();
      var messageData = {event: eventName};
      socket.onmessage({data: angular.toJson(messageData)});

      expect(callback1).toHaveBeenCalled();
    });


    it('unbind all handlers for event', function () {
      var eventName = 'fake-event';

      expectBindCall(eventName);
      expectBindCall(eventName);
      expectBindCall(eventName);

      expectUnbindCall(eventName);

      pushApi.bind(eventName, callback1);
      pushApi.bind(eventName, callback2);
      pushApi.bind(eventName, callback3);

      pushApi.unbind(eventName);

      httpBackend.flush();

      var messageData = {event: eventName};
      socket.onmessage({data: angular.toJson(messageData)});

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
      expectBindCall(eventName, correlationIdB);

      expectUnbindCall(eventName, correlationIdB);

      pushApi.bindId(eventName, correlationIdB, callback1);
      pushApi.bindId(eventName, correlationIdA, callback2);
      pushApi.bindId(eventName, correlationIdB, callback3);

      pushApi.unbindId(eventName, correlationIdB);

      httpBackend.flush();

      socket.onmessage({data: angular.toJson({event: eventName + correlationIdB})});
      socket.onmessage({data: angular.toJson({event: eventName + correlationIdA})});

      expect(callback1).not.toHaveBeenCalled();
      expect(callback2).toHaveBeenCalled();
      expect(callback3).not.toHaveBeenCalled();
    });


    it('unbind all handlers for event with undefined correlationId', function () {
      var eventName = 'fake-event';
      var correlationIdA = 'id-12345';

      expectBindCall(eventName);
      expectBindCall(eventName, correlationIdA);
      expectBindCall(eventName);
      expectUnbindCall(eventName);

      pushApi.bind(eventName, callback1);
      pushApi.bindId(eventName, correlationIdA, callback2);
      pushApi.bind(eventName, callback3);

      pushApi.unbindId(eventName, undefined);

      httpBackend.flush();

      socket.onmessage({data: angular.toJson({event: eventName})});
      socket.onmessage({data: angular.toJson({event: eventName + correlationIdA})});

      expect(callback1).not.toHaveBeenCalled();
      expect(callback2).toHaveBeenCalled();
      expect(callback3).not.toHaveBeenCalled();
    });


    it('call backend unbind only when last handler is unbound', function () {
      var eventName = 'fake-event';

      expectBindCall(eventName);
      expectBindCall(eventName);
      expectUnbindCall(eventName);

      var callback1 = angular.noop;
      var callback2 = angular.noop;
      var boundCallback1 = pushApi.bind(eventName, callback1);
      var boundCallback2 = pushApi.bind(eventName, callback2);

      pushApi.unbind(eventName, boundCallback2);
      pushApi.unbind(eventName, boundCallback1);

      httpBackend.flush();
    });


    it('call backend unbind with correlationId', function () {
      var eventName = 'fake-event';
      var correlationId = 'id-12345';

      expectBindCall(eventName, correlationId);
      expectBindCall(eventName, correlationId);
      expectUnbindCall(eventName, correlationId);

      var callback1 = angular.noop;
      var callback2 = angular.noop;

      var boundCallback1 = pushApi.bindId(eventName, correlationId, callback1);
      var boundCallback2 = pushApi.bindId(eventName, correlationId, callback2);

      pushApi.unbindId(eventName, correlationId, boundCallback2);
      pushApi.unbindId(eventName, correlationId, boundCallback1);

      httpBackend.flush();
    });


    it('binds and unbinds from event with undefined correlationId', function () {
      var eventName = 'fake-event';
      var callback = angular.noop;

      expectBindCall(eventName);
      expectUnbindCall(eventName);

      var boundCallback = pushApi.bind(eventName, callback);

      pushApi.unbindId(eventName, undefined, boundCallback);

      httpBackend.flush();

    });

    it('fires an event', function () {
      var manualEvent = 'manual-event';

      expectBindCall(manualEvent);

      pushApi.bind(manualEvent, callback1);

      pushApi.fireEvent(manualEvent, {data: 'dummy'});

      httpBackend.flush();

      expect(callback1).toHaveBeenCalledWith({data: 'dummy', event: manualEvent});
    });

    function expectBindCall(eventName,correlationId,response) {
      if (!correlationId) {
        correlationId = "";
      }

      if (!response) {
        httpBackend.expectPUT('/pushService?correlationId=' + correlationId + '&eventName=' + eventName + '&subscriber=subscriber1').respond(200, "");
      } else {
        httpBackend.expectPUT('/pushService?correlationId=' + correlationId + '&eventName=' + eventName + '&subscriber=subscriber1').respond(response.status, response.body);
      }
    }

    function expectUnbindCall(eventName, correlationId, response) {
      if (!correlationId) {
        correlationId = "";
      }

      if (!response) {
        httpBackend.expectDELETE('/pushService?correlationId=' + correlationId + '&eventName=' + eventName + '&subscriber=subscriber1').respond(200, "");
      } else {
        httpBackend.expectDELETE('/pushService?correlationId=' + correlationId + '&eventName=' + eventName + '&subscriber=subscriber1').respond(response.status, response.body);
      }
    }

   });

  describe('Connection', function() {
    var $interval, $timeout, httpBackend;
    var keepAliveInterval = 5; //in seconds
    var reconnectInterval = 20; //in seconds

    beforeEach(function () {
      module(function (pushApiProvider) {
        pushApiProvider
                .keepAliveTimeInterval(keepAliveInterval)
                .reconnectTimeInterval(reconnectInterval)
                .backendServiceUrl("/pushService");
      });

      inject(function (_pushApi_, $httpBackend, _$interval_, _$timeout_) {
        pushApi = _pushApi_;
        httpBackend = $httpBackend;
        $interval = _$interval_;
        $timeout = _$timeout_;

        socket = goog.appengine.Socket._get("token1");
      });
    });

    afterEach(function () {
      httpBackend.verifyNoOutstandingExpectation();
      httpBackend.verifyNoOutstandingRequest();
    });


    it('opens new one', function () {
      httpBackend.expectGET('/pushService?subscriber=subscriber1').respond(200, "token1");
      pushApi.openConnection("subscriber1");

      httpBackend.flush();
    });

    it('reuses existing connection on second open', function () {
      httpBackend.expectGET('/pushService?subscriber=subscriber1').respond(200, "token1");

      pushApi.openConnection("subscriber1");
      pushApi.openConnection("subscriber1");

      httpBackend.flush();
    });

    it('keeps connection alive at specified time interval', function () {
      httpBackend.expectGET('/pushService?subscriber=test-subscriber').respond(200, "token2");

      pushApi.openConnection("test-subscriber");
      httpBackend.flush();

      httpBackend.expectPOST('/pushService?subscriber=test-subscriber').respond(200, "");
      $interval.flush(keepAliveInterval * 1000);

      httpBackend.flush();
    });


    it('attempts to reconnect periodically if connection fails', function () {
      var dummySubscriber = 'dummy_subscriber';
      httpBackend.expectGET('/pushService?subscriber=dummy_subscriber').respond(500, "dummy-token");

      pushApi.openConnection(dummySubscriber);
      httpBackend.flush();

      httpBackend.expectGET('/pushService?subscriber=dummy_subscriber').respond(200, "dummy-token");

      $timeout.flush(reconnectInterval * 1000);

      httpBackend.flush();
    });

    it('establishes new connection after channel expires', function () {
      var dummySubscriber = 'dummy_subscriber';
      httpBackend.expectGET('/pushService?subscriber=dummy_subscriber').respond(200, "expiring-token");

      pushApi.openConnection(dummySubscriber);
      httpBackend.flush();


      httpBackend.expectGET('/pushService?subscriber=dummy_subscriber').respond(200, "dummy-token2");

      var socket = goog.appengine.Socket._get("expiring-token");
      socket.onerror();

      httpBackend.flush();
    });

    it('uses single keep-alive for all channels', function () {
      var dummySubscriber = 'dummy_subscriber';

      httpBackend.expectGET('/pushService?subscriber=dummy_subscriber').respond(200, channelToken);
      pushApi.openConnection(dummySubscriber);

      httpBackend.flush();

      httpBackend.expectGET('/pushService?subscriber=dummy_subscriber').respond(200, channelToken);
      var socket = goog.appengine.Socket._get(channelToken);
      socket.onerror();
      httpBackend.flush();

      httpBackend.expectPOST('/pushService?subscriber=dummy_subscriber').respond(200, channelToken);
      $interval.flush(keepAliveInterval * 1000);

      httpBackend.flush();
    });

    it('sends single connect requests when multiple open connection calls are made', function () {
      var dummySubscriber = 'dummy_subscriber';

      httpBackend.expectGET('/pushService?subscriber=dummy_subscriber').respond(200, "token1");
      pushApi.openConnection(dummySubscriber);
      pushApi.openConnection(dummySubscriber);
      pushApi.openConnection(dummySubscriber);
      httpBackend.flush();

    });


    it('generates a subscriber if it is not specified', function () {
      spyOn(Math, 'random').and.returnValue(0.07);

      httpBackend.expectGET('/pushService?subscriber=eeeeeeeeeeeeeee').respond(200, "token1");
      pushApi.openConnection();
      httpBackend.flush();

    });


    it('generates a subscriber if it is empty string', function () {
      spyOn(Math, 'random').and.returnValue(0.07);
      httpBackend.expectGET('/pushService?subscriber=eeeeeeeeeeeeeee').respond(200, "token1");
      pushApi.openConnection('');
      httpBackend.flush();
    });

  });




});