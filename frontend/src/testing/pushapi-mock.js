/**
  * @author Stefan Dimitrov (stefan.dimitrov@clouway.com).
  */
 angular.module('clouway-push')

   .config(function ($provide, pushApiProvider) {
     pushApiProvider.backendServiceUrl(''); // ensure no requests will be sent

     /**
      * @ngdoc service
      * @name pushApi
      * @description
      *
      * This service is just a simple decorator for {@link clouway-push.pushApi pushApi} service
      * that decorates the "flush" method of pushApi to call $httpBackend.
      */
     $provide.decorator('pushApi', function ($delegate, $timeout) {
       $delegate.openConnection = angular.noop; // no need to execute this logic in user's tests. If needed it can be spied on.

       /**
        * Flushes pending initial bindings.
        */
       $delegate.flushInitialBind = function () {
         $timeout.flush(0);
       };
       return $delegate;
     });

   });