# SmartThings Yamaha Receiver

Features:

* Turn on/off a Yamaha RX V477 Reiver
* Usable in new SmartThings mobile app

Limitations:

* Requires installing via legacy developer tools.
* Might break in a future update of SmartThings.

## Installation

On https://account.smartthings.com:

1. On the "My Device Handlers" page, add a device handler using yamaha-receiver.groovy
2. On the "My Devices" page add a new Device with type "Yamaha Receiver". You must set the "Device Network Id" to the host:port of your reciever in hexadecimal. For example if your reciever has IP address 10.1.10.123 (port 80) use 0a010a7b:0050.  On linux you can do the conversion with: `printf "%02x%02x%02x%02x:%04x\n" 10 1 10 123 80`.
3. In the SmartThings app, find the reciever and go to the Settings menu. Set the correct zone.

That's it!
