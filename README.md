# App-Copies-Detector

## Launching
1. Download & unpack archive
2. Move to project folder
3. Launch using terminal:
```
java -jar target/detector.jar <multicast address>
```

## How to use it?
After launching the first copy of app, server will be raised up on this copy. <br />
Launching another copies leads server to print addresses of all alive copies.<br />
Closing copies with **^C** have the same effect.<br />
Closing copy with server on it means restarting server on another alive copy.
