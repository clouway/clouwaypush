/**
 * @author Stefan Dimitrov (stefan.dimitrov@clouway.com).
 */

describe('PushApi', function () {

  beforeEach(module('clouway-push'));

  var pushApi, socket, rootScope, connectMethod, bindMethod, unbindMethod;
  var subscriber = 'test-subscriber';
  var channelToken = 'fake-channel-token';
  var $window = {};

  describe('service should', function () {

    beforeEach(function () {
      module(function ($provide, pushApiProvider) {
        connectMethod = jasmine.createSpy('connectMethod');
        bindMethod = jasmine.createSpy('bindMethod');
        unbindMethod = jasmine.createSpy('unbindMethod');

        pushApiProvider.openConnectionMethod(connectMethod)
          .bindMethod(bindMethod)
          .unbindMethod(unbindMethod);

        $provide.value('$window', $window);
      });

      inject(function ($rootScope, $q, _pushApi_) {
        rootScope = $rootScope;
        pushApi = _pushApi_;
        var connectDeferred = $q.defer();

        connectMethod.and.returnValue(connectDeferred.promise);

        pushApi.openConnection(subscriber);
        connectDeferred.resolve(channelToken);
        rootScope.$digest();
        socket = goog.appengine.Socket._get(channelToken);
      });
    });

    it('call bound event handler', function () {
      spyOn(pushApi, 'openConnection').and.returnValue('connection subscriber');
      var eventName = 'fake-event';

      var callback = jasmine.createSpy('callback');
      pushApi.bind(eventName, callback);
      expect(pushApi.openConnection).toHaveBeenCalled();
      expect(bindMethod).toHaveBeenCalledWith('connection subscriber', eventName, '');

      socket.onmessage({data: angular.toJson({event: eventName})});

      expect(callback).toHaveBeenCalledWith({event: eventName});
    });

    it('call many bound event handlers', function () {
      var eventName = 'fake-event';

      var callback1 = jasmine.createSpy('callback1');
      var callback2 = jasmine.createSpy('callback2');
      var callback3 = jasmine.createSpy('callback3');
      pushApi.bind(eventName, callback1);
      pushApi.bind(eventName, callback2);
      pushApi.bind(eventName, callback3);

      expect(bindMethod.calls.count()).toEqual(3);
      expect(bindMethod.calls.argsFor(0)).toEqual([subscriber, eventName, '']);
      expect(bindMethod.calls.argsFor(1)).toEqual([subscriber, eventName, '']);
      expect(bindMethod.calls.argsFor(2)).toEqual([subscriber, eventName, '']);

      var messageData = {event: eventName};
      socket.onmessage({data: angular.toJson(messageData)});

      expect(callback1).toHaveBeenCalledWith(messageData);
      expect(callback2).toHaveBeenCalledWith(messageData);
      expect(callback3).toHaveBeenCalledWith(messageData);
    });

    it('call bound event handlers with correlationIds', function () {
      spyOn(pushApi, 'openConnection').and.returnValue(subscriber);
      var eventName = 'fake-event';
      var correlationIdA = 'id-08433';
      var correlationIdB = 'id-12345';

      var callback1 = jasmine.createSpy('callback1');
      var callback2 = jasmine.createSpy('callback2');
      pushApi.bindId(eventName, correlationIdA, callback1);
      pushApi.bindId(eventName, correlationIdB, callback2);

      expect(pushApi.openConnection).toHaveBeenCalled();
      expect(bindMethod.calls.count()).toEqual(2);
      expect(bindMethod.calls.argsFor(0)).toEqual([subscriber, eventName, correlationIdA]);
      expect(bindMethod.calls.argsFor(1)).toEqual([subscriber, eventName, correlationIdB]);

      socket.onmessage({data: angular.toJson({event: eventName + correlationIdA})});

      expect(callback1).toHaveBeenCalledWith({event: eventName + correlationIdA});
      expect(callback2).not.toHaveBeenCalled();
    });

    it('call bound event handler with undefined correlationId', function () {
      spyOn(pushApi, 'openConnection').and.returnValue('connection subscriber');
      var eventName = 'fake-event';
      var correlationId;

      var callback = jasmine.createSpy('callback');
      pushApi.bindId(eventName, correlationId, callback);
      expect(pushApi.openConnection).toHaveBeenCalled();
      expect(bindMethod).toHaveBeenCalledWith('connection subscriber', eventName, '');

      socket.onmessage({data: angular.toJson({event: eventName})});

      expect(callback).toHaveBeenCalledWith({event: eventName});
    });

    it('not call non-bound event handlers', function () {
      var eventName = 'fake-event';

      var callback1 = jasmine.createSpy('callback1');
      var callback2 = jasmine.createSpy('callback2');
      var callback3 = jasmine.createSpy('callback3');
      var boundCallback1 = pushApi.bind(eventName, callback1);
      var boundCallback2 = pushApi.bind(eventName, callback2);
      var boundCallback3 = pushApi.bind(eventName, callback3);

      pushApi.unbind(eventName, boundCallback1);
      pushApi.unbind(eventName, boundCallback3);

      var messageData = {event: eventName};
      socket.onmessage({data: angular.toJson(messageData)});

      expect(callback1).not.toHaveBeenCalled();
      expect(callback2).toHaveBeenCalledWith(messageData);
      expect(callback3).not.toHaveBeenCalled();
    });

    it('not unbind handler for non-existing event', function () {
      var eventName = 'fake-event';

      var callback = jasmine.createSpy('callback');
      var boundCallback = pushApi.bind(eventName, callback);

      pushApi.unbind('non-existing-event', boundCallback);

      var messageData = {event: eventName};
      socket.onmessage({data: angular.toJson(messageData)});

      expect(callback).toHaveBeenCalled();
    });

    it('not unbind handler not for event', function () {
      var eventName = 'fake-event';

      var callback1 = jasmine.createSpy('callback1');
      var callback2 = jasmine.createSpy('callback2');
      var boundCallback1 = pushApi.bind(eventName, callback1);
      var boundCallback2 = pushApi.bind('other-event', callback2);

      pushApi.unbind(eventName, boundCallback2);
      expect(unbindMethod).not.toHaveBeenCalled();

      var messageData = {event: eventName};
      socket.onmessage({data: angular.toJson(messageData)});

      expect(callback1).toHaveBeenCalled();
    });

    it('unbind all handlers for event', function () {
      var eventName = 'fake-event';

      var callback1 = jasmine.createSpy('callback1');
      var callback2 = jasmine.createSpy('callback2');
      var callback3 = jasmine.createSpy('callback3');
      pushApi.bind(eventName, callback1);
      pushApi.bind(eventName, callback2);
      pushApi.bind(eventName, callback3);

      pushApi.unbind(eventName);
      expect(unbindMethod).toHaveBeenCalledWith(subscriber, eventName, '');

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

      var callback1 = jasmine.createSpy('callback1');
      var callback2 = jasmine.createSpy('callback2');
      var callback3 = jasmine.createSpy('callback3');
      pushApi.bindId(eventName, correlationIdB, callback1);
      pushApi.bindId(eventName, correlationIdA, callback2);
      pushApi.bindId(eventName, correlationIdB, callback3);

      pushApi.unbindId(eventName, correlationIdB);
      expect(unbindMethod).toHaveBeenCalledWith(subscriber, eventName, correlationIdB);

      socket.onmessage({data: angular.toJson({event: eventName + correlationIdB})});
      socket.onmessage({data: angular.toJson({event: eventName + correlationIdA})});

      expect(callback1).not.toHaveBeenCalled();
      expect(callback2).toHaveBeenCalled();
      expect(callback3).not.toHaveBeenCalled();
    });

    it('unbind all handlers for event with undefined correlationId', function () {
      var eventName = 'fake-event';
      var correlationIdA = 'id-12345';

      var callback1 = jasmine.createSpy('callback1');
      var callback2 = jasmine.createSpy('callback2');
      var callback3 = jasmine.createSpy('callback3');
      pushApi.bind(eventName, callback1);
      pushApi.bindId(eventName, correlationIdA, callback2);
      pushApi.bind(eventName, callback3);

      pushApi.unbindId(eventName, undefined);
      expect(unbindMethod).toHaveBeenCalledWith(subscriber, eventName, '');

      socket.onmessage({data: angular.toJson({event: eventName})});
      socket.onmessage({data: angular.toJson({event: eventName + correlationIdA})});

      expect(callback1).not.toHaveBeenCalled();
      expect(callback2).toHaveBeenCalled();
      expect(callback3).not.toHaveBeenCalled();
    });

    it('call backend unbind only when last handler is unbound', function () {
      var eventName = 'fake-event';

      var callback1 = angular.noop;
      var callback2 = angular.noop;
      var boundCallback1 = pushApi.bind(eventName, callback1);
      var boundCallback2 = pushApi.bind(eventName, callback2);

      pushApi.unbind(eventName, boundCallback2);
      expect(unbindMethod).not.toHaveBeenCalled();
      pushApi.unbind(eventName, boundCallback1);
      expect(unbindMethod).toHaveBeenCalledWith(subscriber, eventName, '');
    });

    it('call backend unbind with correlationId', function () {
      var eventName = 'fake-event';
      var correlationId = 'id-12345';

      var callback1 = angular.noop;
      var callback2 = angular.noop;
      var boundCallback1 = pushApi.bindId(eventName, correlationId, callback1);
      var boundCallback2 = pushApi.bindId(eventName, correlationId, callback2);

      pushApi.unbindId(eventName, correlationId, boundCallback2);
      expect(unbindMethod).not.toHaveBeenCalled();
      pushApi.unbindId(eventName, correlationId, boundCallback1);
      expect(unbindMethod).toHaveBeenCalledWith(subscriber, eventName, correlationId);
    });

    it('call backend unbind with undefined correlationId', function () {
      var eventName = 'fake-event';

      var callback = angular.noop;
      var boundCallback = pushApi.bind(eventName, callback);

      pushApi.unbindId(eventName, undefined, boundCallback);
      expect(unbindMethod).toHaveBeenCalledWith(subscriber, eventName, '');
    });

  });

  describe('connection opening should', function () {

    var connectDeferred, keepAliveMethod, $interval, $timeout;
    var keepAliveInterval = 5; //in seconds
    var reconnectInterval = 20; //in seconds
    beforeEach(function () {
      module(function (pushApiProvider) {
        connectMethod = jasmine.createSpy('connectMethod');
        keepAliveMethod = jasmine.createSpy('keepAliveMethod');

        pushApiProvider.openConnectionMethod(connectMethod);
        pushApiProvider.keepAliveMethod(keepAliveMethod);
        pushApiProvider.keepAliveTimeInterval(keepAliveInterval);
      });

      inject(function ($rootScope, $q, _$interval_, _$timeout_, _pushApi_) {
        rootScope = $rootScope;
        $interval = _$interval_;
        $timeout = _$timeout_;
        pushApi = _pushApi_;
        connectDeferred = $q.defer();

        connectMethod.and.returnValue(connectDeferred.promise);
      });
    });

    it('call configured connect method', function () {
      pushApi.openConnection(subscriber);
      expect(connectMethod).toHaveBeenCalledWith(subscriber);
    });

    it('not call anything when already connected', function () {
      pushApi.openConnection(subscriber);

      expect(connectMethod.calls.count()).toBe(1);
      connectDeferred.resolve(channelToken);
      rootScope.$digest();

      connectMethod.calls.reset();

      pushApi.openConnection('any subscriber');
      expect(connectMethod).not.toHaveBeenCalled();
    });

    it('call keepAlive after time interval', function () {
      pushApi.openConnection(subscriber);
      connectDeferred.resolve('token');
      rootScope.$digest();

      expect(keepAliveMethod).not.toHaveBeenCalled();
      $interval.flush(keepAliveInterval * 1000);
      expect(keepAliveMethod).toHaveBeenCalledWith(subscriber);
    });

    it('attempt a reconnect after time interval', function () {
      var dummySubscriber = 'dummy_subscriber';
      pushApi.openConnection(dummySubscriber);

      expect(connectMethod.calls.count()).toBe(1);

      connectDeferred.reject();
      rootScope.$digest();

      expect(connectMethod.calls.count()).toBe(1);
      $timeout.flush(reconnectInterval * 1000);
      expect(connectMethod.calls.count()).toBe(2);
      expect(connectMethod.calls.mostRecent().args).toEqual([dummySubscriber]);
    });

    it('establish new connection after channel expire', function () {
      pushApi.openConnection(subscriber);
      connectDeferred.resolve(channelToken);
      rootScope.$digest();

      connectMethod.calls.reset();

      expect(connectMethod).not.toHaveBeenCalled();
      var socket = goog.appengine.Socket._get(channelToken);
      socket.onerror();
      expect(connectMethod).toHaveBeenCalledWith(subscriber);
    });

    it('call connect only once when multiple open connection calls before response', function () {
      pushApi.openConnection(subscriber);
      pushApi.openConnection(subscriber);
      pushApi.openConnection(subscriber);

      expect(connectMethod.calls.count()).toBe(1);
      connectDeferred.resolve(channelToken);
      rootScope.$digest();

      connectMethod.calls.reset();
      expect(connectMethod).not.toHaveBeenCalled();
    });

    it('call connect only once when multiple open connection calls after failure', function () {
      pushApi.openConnection(subscriber);

      expect(connectMethod.calls.count()).toBe(1);
      connectDeferred.reject();
      rootScope.$digest();

      connectMethod.calls.reset();
      pushApi.openConnection(subscriber);
      pushApi.openConnection(subscriber);
      expect(connectMethod).not.toHaveBeenCalled();

    });

    it('generate a subscriber if it is not specified', function () {
      spyOn(Math, 'random').and.returnValue(0.07);
      connectMethod.and.returnValue({then: angular.noop});

      var generatedSubscriber = pushApi.openConnection();

      expect(generatedSubscriber).toEqual('eeeeeeeeeeeeeee');
      expect(connectMethod).toHaveBeenCalledWith(generatedSubscriber);
    });

    it('generate a subscriber if it is empty string', function () {
      spyOn(Math, 'random').and.returnValue(0.07);
      connectMethod.and.returnValue({then: angular.noop});

      var generatedSubscriber = pushApi.openConnection('');

      expect(generatedSubscriber).toEqual('eeeeeeeeeeeeeee');
      expect(connectMethod).toHaveBeenCalledWith(generatedSubscriber);
    });

  });

});