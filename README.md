# MQTT stress tester

To configure set the desired properties (either in a separate profile properties 
or the [default one](src/main/resources/application.yml))

The application will spin up an asynchronous thread for each "client" and start 
publishing messages.

Once the time to live (in seconds) has passed the application will initiate a shutdown and close all connections.

If you shut down the application manually the @PreDestroy method will attempt the same.
Although shutdown errors might appear.