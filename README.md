# USBHostControlUSBRelay

[参考USB Relay标准库](https://github.com/pavel-a/usb-relay-hid/blob/master/lib/usb_relay_lib.c)

通过USB Manager 查找到USBDevice列表找到对应的USB设备，    
    
    获取Interface,找到Endpoint，
    打开设备进行操作，
    使用controlTransfer收发指令

重点：需要先打开USB Relay设备，才能操控，否则，controlTransfer会一直返回-1.


Control relay over USB from Android

So, using the Android USB API this translates into:

        Open device (device->host direction):

        int r = mUsbConnection.controlTransfer(0xa1, 0x01, 0x0300, 0x00, buffer, buffer.length, 500);

Open relay '1' (host->device direction).

        byte[] buffer = {(byte) 0xff, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        int r = mUsbConnection.controlTransfer(0x21, 0x09, 0x0300, 0x00, buffer, buffer.length, 500);

Note, the second parameter in the buffer (0x01) is the relay number (in case you have >1 relay on the board). I only had one.

Close relay '1' (host->device direction):

        byte[] buffer = {(byte) 0xfd, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        int r = mUsbConnection.controlTransfer(0x21, 0x09, 0x0300, 0x00, buffer, buffer.length, 500);
