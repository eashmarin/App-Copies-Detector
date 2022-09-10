# App-Copies-Detector

## Launching
1. Download & unpack archive
2. Move to project folder
3. Launch using terminal:
```
java -jar target/detector.jar <multicast address>
```

## Using
- After launching the first copy of app, server will be raised up on this copy.<br />
- Server monitors launching/closing copies of app and prints addresses of alive copies when these events are happening. <br />
- Closing copy with server on it means restarting server on another alive copy.<br />
- Copies with no server do not print anything, server is responsible for it.<br />
- Server reacts to ongoing events with delay of few seconds, so wait a little.
