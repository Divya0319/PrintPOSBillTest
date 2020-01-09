package co.practicesession.printbillpostest

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.*
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    companion object {
        const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    }

    private lateinit var usbManager: UsbManager
    private lateinit var mDevice: UsbDevice
    private var mConnection: UsbDeviceConnection? = null
    private lateinit var mInterface: UsbInterface
    private var mEndPoint: UsbEndpoint? = null
    private var mPermissionIntent: PendingIntent? = null
    private lateinit var edTxt: EditText
    private val forceClaim: Boolean = true

    private lateinit var mDeviceList: HashMap<String, UsbDevice>
    private lateinit var mDeviceIterator: Iterator<UsbDevice>
    private lateinit var testBytes: ByteArray

    private val mUsbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent!!.action
            if (ACTION_USB_PERMISSION == action) {
                synchronized(this) {
                    val device =
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE) as UsbDevice
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        //call method to set up device communication
                        mInterface = device.getInterface(0)
                        mEndPoint =
                            mInterface.getEndpoint(1) // 0 IN and  1 OUT to printer.
                        mConnection = usbManager.openDevice(device)
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

    override fun onResume() {
        super.onResume()
        checkPrinterConnectedOrNot()
    }

    private fun checkPrinterConnectedOrNot() {
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        mDeviceList = usbManager.deviceList

        if (mDeviceList.size > 0) {
            mDeviceIterator = mDeviceList.values.iterator()

            Toast.makeText(this, "Device List Size: " + mDeviceList.size, Toast.LENGTH_SHORT).show()

            val textView = findViewById<TextView>(R.id.usbDevice)

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

                Toast.makeText(this, "INTERFACE COUNT: $interfaceCount", Toast.LENGTH_SHORT).show()

                mDevice = usbDevice1

                Toast.makeText(this, "Device is attached", Toast.LENGTH_SHORT).show()
                textView.text = usbDevice

            }

            mPermissionIntent =
                PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
            val filter = IntentFilter(ACTION_USB_PERMISSION)
            registerReceiver(mUsbReceiver, filter)

            usbManager.requestPermission(mDevice, mPermissionIntent)
        } else {
            Toast.makeText(this, "Please attach printer via USB", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        edTxt = findViewById(R.id.ed_txt)
        val print = findViewById<Button>(R.id.print)

        print.setOnClickListener {
            print(mConnection, mInterface)
        }

    }

    private fun print(connection: UsbDeviceConnection?, usbInterface: UsbInterface?) {
        val test = edTxt.text.toString()
        testBytes = test.toByteArray()

        when {
            usbInterface == null -> {
                Toast.makeText(this, "INTERFACE IS NULL", Toast.LENGTH_SHORT).show()
            }
            connection == null -> {
                Toast.makeText(this, "CONNECTION IS NULL", Toast.LENGTH_SHORT).show()
            }
            else -> {
                connection.claimInterface(usbInterface, forceClaim)

                val thread = Thread(Runnable {
                    val cutPaper = byteArrayOf(0x1D, 0x56, 0x41, 0x10)
                    connection.bulkTransfer(mEndPoint, testBytes, testBytes.size, 0)
                    connection.bulkTransfer(mEndPoint, cutPaper, cutPaper.size, 0)

                })

                thread.run()
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
}
