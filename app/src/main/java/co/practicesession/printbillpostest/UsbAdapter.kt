package co.practicesession.printbillpostest

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.*
import android.widget.Toast

/**
 * Created by Divya Gupta on 09-Jan-20.
 **/
class UsbAdapter(private val listener: DeviceSpecListener) {

    companion object {
        const val ACTION_USB_PERMISSION = "co.behtarinternal.thermalprinter.USB_PERMISSION"

    }

    private lateinit var mUsbManager: UsbManager
    private lateinit var mUsbDevice: UsbDevice
    private var mConnection: UsbDeviceConnection? = null
    private var mInterface: UsbInterface? = null
    private var mEndPoint: UsbEndpoint? = null
    private lateinit var mPermissionIntent: PendingIntent
    private val forceClaim: Boolean = true

    private val mUsbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (ACTION_USB_PERMISSION == action) {
                synchronized(this) {

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        mInterface = mUsbDevice.getInterface(0)
                        mEndPoint =
                            mInterface?.getEndpoint(1) // 0 IN and  1 OUT to printer.
                        mConnection = mUsbManager.openDevice(mUsbDevice)

                    } else {
                        Toast.makeText(
                            context,
                            "PERMISSION DENIED FOR THIS DEVICE",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    }

    private val mUsbDetachedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action

            if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
                Toast.makeText(context, "Device unplugged", Toast.LENGTH_SHORT).show()
                mConnection?.close()
            }
        }

    }

    fun createCon(context: Context) {
        mPermissionIntent = PendingIntent.getBroadcast(context, 0, Intent(ACTION_USB_PERMISSION), 0)
        mUsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

        getConnectedPrinterData(context)
        val usbDetachedFilter = IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED)
        context.registerReceiver(mUsbDetachedReceiver, usbDetachedFilter)
    }

    private fun getConnectedPrinterData(context: Context) {
        val mDeviceList = mUsbManager.deviceList

        if (mDeviceList.size > 0) {
            val mDeviceIterator = mDeviceList.values.iterator()

            Toast.makeText(context, "Device List Size: " + mDeviceList.size, Toast.LENGTH_SHORT)
                .show()


            var usbDevice = ""

            while (mDeviceIterator.hasNext()) {
                val usbDevice1 = mDeviceIterator.next()
                usbDevice += "\n" +
                        "DeviceID: " + usbDevice1.deviceId + "\n" +
                        "DeviceName: " + usbDevice1.deviceName + "\n" +
                        "Protocol: " + usbDevice1.deviceProtocol + "\n" +
                        "Product Name: " + usbDevice1.productName + "\n" +
                        "Manufacturer Name: " + usbDevice1.manufacturerName + "\n" +
                        "DeviceClass: " + usbDevice1.deviceClass + " - " + translateDeviceClass(
                    usbDevice1.deviceClass
                ) + "\n" +
                        "DeviceSubClass: " + usbDevice1.deviceSubclass + "\n" +
                        "VendorID: " + usbDevice1.vendorId + "\n" +
                        "ProductID: " + usbDevice1.productId + "\n"

                val interfaceCount = usbDevice1.interfaceCount

                Toast.makeText(context, "INTERFACE COUNT: $interfaceCount", Toast.LENGTH_SHORT)
                    .show()
                mUsbDevice = usbDevice1
                listener.onDeviceSpecReceived(usbDevice)



                Toast.makeText(context, "Device is attached", Toast.LENGTH_SHORT).show()

            }

            mPermissionIntent =
                PendingIntent.getBroadcast(context, 0, Intent(ACTION_USB_PERMISSION), 0)
            val usbPermFilter = IntentFilter(ACTION_USB_PERMISSION)
            context.registerReceiver(mUsbReceiver, usbPermFilter)

            mUsbManager.requestPermission(mUsbDevice, mPermissionIntent)
        } else {
            Toast.makeText(context, "Please attach printer via USB", Toast.LENGTH_SHORT).show()
        }

    }

    fun printMessage(context: Context, msg: String) {
        val test = msg + "\n\n"

        val testBytes = test.toByteArray()

        when {
            mInterface == null -> {
                Toast.makeText(context, "INTERFACE IS NULL", Toast.LENGTH_SHORT).show()
            }
            mConnection == null -> {
                Toast.makeText(context, "CONNECTION IS NULL", Toast.LENGTH_SHORT).show()
            }
            else -> {
                mConnection?.claimInterface(mInterface, forceClaim)

                val thread = Thread(Runnable {
                    val center = byteArrayOf(0x1b, 0x61, 0x01)
//                    val cutPaper = byteArrayOf(0x1D, 0x56, 0x41, 0x10)
//                    val cutPaper = byteArrayOf(0x0a, 0x0a, 0x0a, 0x0a)
                    mConnection?.bulkTransfer(mEndPoint, center, center.size, 0)
                    mConnection?.bulkTransfer(mEndPoint, testBytes, testBytes.size, 0)
                })
                thread.run()

                mConnection?.releaseInterface(mInterface)
            }
        }
    }


    private fun translateDeviceClass(deviceClass: Int): String? {
        return when (deviceClass) {
            UsbConstants.USB_CLASS_APP_SPEC -> "Application specific USB class"
            UsbConstants.USB_CLASS_AUDIO -> "USB class for audio devices"
            UsbConstants.USB_CLASS_CDC_DATA -> "USB class for CDC devices (communications device class)"
            UsbConstants.USB_CLASS_COMM -> "USB class for communication devices"
            UsbConstants.USB_CLASS_CONTENT_SEC -> "USB class for content security devices"
            UsbConstants.USB_CLASS_CSCID -> "USB class for content smart card devices"
            UsbConstants.USB_CLASS_HID -> "USB class for human interface devices (for example, mice and keyboards)"
            UsbConstants.USB_CLASS_HUB -> "USB class for USB hubs"
            UsbConstants.USB_CLASS_MASS_STORAGE -> "USB class for mass storage devices"
            UsbConstants.USB_CLASS_MISC -> "USB class for wireless miscellaneous devices"
            UsbConstants.USB_CLASS_PER_INTERFACE -> "USB class indicating that the class is determined on a per-interface basis"
            UsbConstants.USB_CLASS_PHYSICA -> "USB class for physical devices"
            UsbConstants.USB_CLASS_PRINTER -> "USB class for printers"
            UsbConstants.USB_CLASS_STILL_IMAGE -> "USB class for still image devices (digital cameras)"
            UsbConstants.USB_CLASS_VENDOR_SPEC -> "Vendor specific USB class"
            UsbConstants.USB_CLASS_VIDEO -> "USB class for video devices"
            UsbConstants.USB_CLASS_WIRELESS_CONTROLLER -> "USB class for wireless controller devices"
            else -> "Unknown USB class!"
        }
    }

    fun unregisterAllReceivers(context: Context) {
        context.unregisterReceiver(mUsbDetachedReceiver)
        context.unregisterReceiver(mUsbReceiver)
    }

}