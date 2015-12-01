clouwaypush
===========

Clouway Push AngularJS module


### Quick start

1. Install the library

   ```sh
   $ bower install https://github.com/clouway/clouwaypush.git
   ```

2. Include the javascript in your html

   First you need to include the _google appengine_ channel api javascript
   ```html
   <script type="text/javascript" src="/_ah/channel/jsapi"></script>
   ```

   then include the _clouwaypush_ javascript
   ```html
   <script type="text/javascript" src="bower_components/clouwaypush/frontend/dist/clouwaypush.min.js"></script>
   ```

3. Add the module dependency in your app

   ```js
   angular.module('myModule', ['clouway-push']);
   ```

4. Inject the `pushApi` service and bind handlers for events

   ```js
   .controller('MyPageCtrl', ['$scope', 'pushApi', function ($scope, pushApi) {
   
     pushApi.bind('some_push_event', function(data) {
       //Do something with the data object
     });

   }])
   ```