# BlutoothTethering
Programmatically turn on and off  Bluetooth Tethering service.

This repository is a fork and only here because it is easier for me as well. I don't give any support.

## Car side (head unit):
At the top of the screen is a spinner containing a list of all paired bluetooth devices. If you select one of those devices, and hit the "CONNECT PAN" button, it will try to connect to that device as a bluetooth tethering client.

If you enable the switch "Auto Connect PAN", then whenever it connects to that device (the one currently showing in the spinner), it will AUTOMATICALLY try to connect PAN. The application does NOT need to be running for this to occur, since there is a receiver registered in the application's manifest.

The switch "Disable Wifi" will disable Wifi on the head unit whenever it connects to a phone, as the China head units always start Wifi and we do not want that.

The next button down, "Launch Bluetooth Settings", just launches the standard Android Settings --> Bluetooth activity.

### Requirements:
On the Joyings all apps are killed upon sleep. Also this app/service. There are several options to work-around that which re not discussed here but on XDA. When starting it again, it will start in "stopped" state unless you install it into /system/priv-app/, which will allow it to receive broadcast intents regardless of that state.

## Phone side:
At the bottom, there is a button "START SERVICE", hit that button on your phone, and it should start up bluetooth tethering service.
And the switch "Auto Bluetooth Tethering", means that every time the bluetooth device turns ON, it will automatically enable bluetooth tethering. Again, it receives an intent and does the work in the background, so the application need not be open.

