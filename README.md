# MQTT stress tester

To configure set the desired properties (either in a separate profile properties 
or the [default one](src/main/resources/application.yml))

The application will spin up an asynchronous thread for each "client" and start 
publishing messages until you shut down the application.