/**
 * @author Stefan Dimitrov (stefan.dimitrov@clouway.com).
 */

describe('PushHandler directive', function () {
  beforeEach(module('clouway-push'));

  var $compile, $timeout, element, pushApi, eventName, scope;
  beforeEach(function () {
    module(function ($provide, pushApiProvider) {
      pushApiProvider.backendServiceUrl('');
    });
    
    inject(function ($rootScope, _$compile_, _$timeout_, _pushApi_) {
      $compile = _$compile_;
      $timeout = _$timeout_;
      scope = $rootScope.$new();
      pushApi = _pushApi_;
      pushApi.openConnection = angular.noop;

      eventName = 'AnyPushEvent';
      scope.handlePush = jasmine.createSpy('handlePush');
      scope.showSection = true;

      var elem = '<div>' +
        '<push-handler ng-if="showSection" event="AnyPushEvent" on-event="handlePush($event)"></push-handler>' +
        '</div>';
      element = $compile(elem)(scope);
      scope.$digest();
    });
  });

  it("fire event", function () {

    pushApi.fireEvent(eventName, {feasibility: {id: 4444}});

    expect(scope.handlePush).toHaveBeenCalledWith({key: eventName, feasibility: { id: 4444 }});
  });

  it("fire event with correlationId", function () {
    scope.testId = '123';
    $compile('<push-handler event="AnyPushEvent" correlation-id="testId" on-event="handlePush($event)"></push-handler>')(scope);
    scope.$digest();
    pushApi.fireSpecificEvent('AnyPushEvent', '123', {feasibility: {id: 4444}});

    expect(scope.handlePush).toHaveBeenCalledWith({key: 'AnyPushEvent', feasibility: { id: 4444 }});
  });

  it("unbinds handler when element is destroyed", function () {
    scope.showSection = false;
    scope.$digest();

    pushApi.fireEvent(eventName, {feasibility: {id: 4444}});

    expect(scope.handlePush).not.toHaveBeenCalled();
  });

  it("unbinds handler with correlationId when scope is destroyed", function () {
    scope.testId = '123';
    $compile('<div>' +
      '<push-handler ng-if="showSection" event="AnyPushEvent" correlation-id="testId" on-event="handlePush($event)"></push-handler>' +
      '</div>')(scope);
    scope.$digest();

    scope.showSection = false;
    scope.$digest();

    pushApi.fireSpecificEvent('AnyPushEvent', '123', {feasibility: {id: 4444}});

    expect(scope.handlePush).not.toHaveBeenCalled();
  });

  it("removes directive element from DOM", function () {
    element = $compile('<div>' +
      '<push-handler event="AnyPushEvent" correlation-id="testId" on-event="handlePush($event)"></push-handler>' +
      '</div>')(scope);
    scope.$digest();

    expect(element.html()).toEqual('');
  });
});