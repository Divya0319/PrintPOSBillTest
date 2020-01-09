package co.practicesession.printbillpostest

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private lateinit var edTxt: EditText
    private lateinit var printOrder: PrintOrder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        printOrder.createConn(this)

        edTxt = findViewById(R.id.ed_txt)
        printOrder = PrintOrder()
        val print = findViewById<Button>(R.id.print)

        print.setOnClickListener {
            printOrder.print(this@MainActivity, edTxt.text.toString())
        }

    }

    override fun onStop() {
        super.onStop()
        printOrder.unregisterAllReceivers(this)
    }
}
