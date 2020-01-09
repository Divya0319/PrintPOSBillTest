package co.practicesession.printbillpostest

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity(), DeviceSpecListener {
    private lateinit var edTxt: EditText
    private lateinit var usbDeviceSpecs: TextView
    private lateinit var printOrder: PrintOrder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        edTxt = findViewById(R.id.ed_txt)
        usbDeviceSpecs = findViewById(R.id.usbDevice)
        val print = findViewById<Button>(R.id.print)


        printOrder = PrintOrder(this)
        printOrder.createConn(this)

        print.setOnClickListener {
            printOrder.print(this@MainActivity, edTxt.text.toString())
        }

    }

    override fun onStop() {
        super.onStop()
        printOrder.unregisterAllReceivers(this)
    }

    override fun onDeviceSpecReceived(deviceSpecs: String) {
        usbDeviceSpecs.text = deviceSpecs
    }
}
