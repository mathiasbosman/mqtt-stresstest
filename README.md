# MQTT stress tester

## Configuration

To configure set the desired properties (either in a separate profile properties
or the [default one](src/main/resources/application.yml))

## How it works

The application will spin up an asynchronous thread for each "client".

The prefix combined with an index has to match with the access token.
For example `TestDevice_1` should be a valid access token.

The prefix can be configured as well as the minimum and maximum index.

Once a connection is a made the application will immediately start publishing messages for that
client.

Once the time to live (in seconds) has passed the application will initiate a shutdown and close all
connections.

If you shut down the application manually the `@PreDestroy` method will attempt the same.