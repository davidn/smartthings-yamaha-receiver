# SmartThings Yamaha Receiver

Features:

* Turn on/off a Yamaha RX V477 Reiver
* Usable in new SmartThings mobile app

Limitations:

* Only supports controlling the Main Zone.
* Requires installing via legacy developer tools.
* Might break in a future update of SmartThings.

## Installation

On https://graph.api.smartthings.com/:

1. On the "My Device Handlers" page, add a device handler using yamaha-receiver.groovy
2. On the "My Devices" page add a new Device with type "Yamaha Receiver". You must set the "Device Network Id" to the host:port of your reciever in hexadecimal. For example if your reciever has IP address 10.1.10.123 (port 80) use 0a010a7b:0050.  On linux you can do the conversion with: `printf "%02x%02x%02x%02x:%04x\n" 10 1 10 123 80`.

That's it!
