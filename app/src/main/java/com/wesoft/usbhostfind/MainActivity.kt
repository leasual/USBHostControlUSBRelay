package com.wesoft.usbhostfind

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    private lateinit var usbManager: UsbManager
    private val TIMEOUT = 5000
    private val forceClaim = true
    lateinit var usbDevice: UsbDevice

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findUSBDevice()
        bt_close.setOnClickListener {
            close()
            tv_command.text = "send close command"
        }
        bt_open.setOnClickListener {
            open()
            tv_command.text = "send open command"
        }
        bt_open_device.setOnClickListener {
            writeCommand(0xa1, 0x01, ByteArray(8))
            tv_command.text = "open device command"
        }
    }

    private fun findUSBDevice() {
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        registerReceiver(usbReceiver, filter)
        val deviceList = usbManager.deviceList
        deviceList?.values?.forEach { device ->
            usbManager.requestPermission(device, permissionIntent)
            Log.d(TAG, "getDeviceVendorId= ${device.vendorId} productId= ${device.productId}")
        }
        val deviceAccessoryList = usbManager.accessoryList
        deviceAccessoryList?.forEach { accessory ->
            Log.d(TAG, "accessory= $accessory")
        }
    }

    private val usbReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_USB_PERMISSION == intent.action) {
                synchronized(this) {
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.apply {
                            //call method to set up device communication
                            usbDevice = device
                            tv_device_info.text = String.format("interfaceCount= $device device info= vendorId= %s productId= %s", device.vendorId, device.productId)
                            Log.d(TAG, "find device= ${device.deviceId} ${device.deviceName} productId= ${device.productId} vendorId= ${device.vendorId}")
                        }
                    } else {
                        Log.d(TAG, "permission denied for device $device")
                    }
                }
            }
        }
    }

    private fun open() {
        //Thread(Runnable {
            writeCommand(0x21, 0x09, CommandUtil.openCommand)
        //}).start()
    }

    private fun close() {
        //Thread(Runnable {
            writeCommand(0x21, 0x09, CommandUtil.closeCommand)
        //}).start()
    }

    private fun writeCommand(requestType: Int, request: Int, buffer: ByteArray) {
        usbDevice.getInterface(0).also { intf ->
            runOnUiThread {
                tv_endpoint.text = "EndpointCount= ${intf.getEndpoint(0).direction == UsbConstants.USB_DIR_IN}"
            }
            intf.getEndpoint(0)?.also { endpoint ->
                usbManager.openDevice(usbDevice)?.apply {
                    val flag = claimInterface(intf, forceClaim)
                    runOnUiThread {
                        tv_command.text = "claimInterface= $flag"
                    }
                    Thread(Runnable {
                        val result = controlTransfer(requestType, request, 0x300, 0x00, buffer, buffer.size, TIMEOUT)
                        //val result = bulkTransfer(endpoint, bytes, bytes.size, TIMEOUT) //do in another thread

                        runOnUiThread {
                            tv_command.text = "claimInterface= $flag bulkTransfer= $result"
                        }
                    }).start()
                }
            }
        }
    }
}
