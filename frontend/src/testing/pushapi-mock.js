/**
 * @author Stefan Dimitrov (stefan.dimitrov@clouway.com).
 */

angular.module('clouway-push')

  .config(function ($provide, pushApiProvider) {

    // Override external request methods
    pushApiProvider
      .bindMethod(angular.noop)
      .unbindMethod(angular.noop)
      .keepAliveMethod(angular.noop);

    /**
     * @ngdoc service
     * @name pushApi
     * @description
     *
     * This service is just a simple decorator for {@link clouway-push.pushApi pushApi} service
     * that overrides the "openConnection" method.
     */
    $provide.decorator('pushApi', function ($delegate) {
      $delegate.openConnection = angular.noop;
      return $delegate;
    });
  })
;
