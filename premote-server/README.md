# premote-server

This is the server site of the `premote` system - a very simple wifi (and maybe
bluetooth, at some point) based presentation remote system.

**NOTE: None of this works yet. The following is just a plan.**

The way it works is by spawning a simple (tls-enabled) web server (by making
use of the Scotty framework). In the future, bluetooth may be supported in
addition. There will probably also be additional security measures added to
ensure that the correct device was paired.[1]

Clients can be written for any device that can handle connecting to http
server via tls after scanning a QR code. Connecting will look like this:

- Start premote-server:
  `premote-server <window id to send events to> <ssl key path> <ssl cert path>`
- premote-server will ask which interface you wish to bind to.
- premote-server will create a QR code with some information (such as the IP
  address and port of the server and version of protocol to use).
- Open the premote app on your portable device and scan the QR code.

[1] In general, this isn't a huge issue. When the server starts, it will
    generate a QR code that contains some bits of information (such as the IP
    and port of the server, and the protocol version). A portable device will
    scan this, connect, and get a session id back in a cookie. After this, the
    communication will be tied to that session id (which will only be sent via
    tls). The only way this could introduce a problem is if the presenter shows
    the audience the QR code and somebody scans it before the presenter. But
    this would fairly quickly be noticed (an error would be shown on the
    presenter's portable device, hopefully), and the presenter could restart
    the server and try again. **After a session id is handed out, the server
    will reject any communication that doesn't use it.**

## License

BSD-2. Have fun!
