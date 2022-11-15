# MQTT stress tester
[![Code QL](https://github.com/mathiasbosman/mqtt-stresstest/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/mathiasbosman/mqtt-stresstest/actions/workflows/codeql-analysis.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=mathiasbosman_mqtt-stresstest&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=mathiasbosman_mqtt-stresstest)

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


# Release Deployment
See [release deployment](https://github.com/mathiasbosman/branching-strategy/blob/master/release-deployment.md).