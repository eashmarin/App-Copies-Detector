# App-Copies-Detector

## Launching
1. Download & unpack archive
2. Move to project folder & launch using terminal
```
cd App-Copies-Detector
java -jar target/detector.jar <multicast_address>
```

## Using
- App monitors launching/closing copies of itself and prints addresses of alive copies when these events are happening. <br />
- Server reacts to ongoing events with delay of few seconds, so wait a little.
