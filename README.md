#### Clouway Push

#### Build Status

[![Build Status](https://travis-ci.org/clouway/clouwaypush.svg?branch=master)](https://travis-ci.org/clouway/clouwaypush)

Clouway Push library is used server to send updates to the clients.

## Usage
### Setup
Include the firebase web init snippet setup in your app
``` in index.html
 let firebaseConfig = {
            apiKey: "${config.apiKey}",
            databaseURL: "${config.databaseUrl}"
        };

        firebase.initializeApp(firebaseConfig);
```

Include `<script src="https://www.gstatic.com/firebasejs/4.3.0/firebase.js"></script>` in your app

### On the frontend
In your package.json add:
```"clouwaypush": "clouway/clouwaypush#master"```

Added bindings for event:
```
pushApi.bind('RegisteredPushEvent', function (data) {
        //make something with data
        console.log(data);
      });
     
```

### On the backend
To add lib as dependency clone or download the repository and added local .jar file dependency.

Added providers for ``PushService``:

```
PushServiceFactory.create(gsonPushEventSerializer.get(), firebase-service-account, "firebase-request-url", namespaceProvider);
```



### On the backend
Added event
```
public class RegisteredPushEvent extends PushEvent {
     public final SomeAdditianalInfoThatWillBeSendToTheClient information;
   
     public RegisteredPushEvent(SomeAdditianalInfoThatWillBeSendToTheClient information) {
       super("RegisteredPushEvent");
       this.information = information;
     }
   }
```

Push event to the client:
```
try {

      pushService.pushEvent(new RegisteredPushEvent(information));

    } catch (UnableToPushEventException exception) {} 
```

