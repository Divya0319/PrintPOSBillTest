package co.practicesession.printbillpostest

import android.content.Context
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import java.lang.Exception

/**
 * Created by Divya Gupta on 09-Jan-20.
 **/
class PrintOrder {
    private lateinit var usbA: UsbAdapter
    fun print(context: Context, msg: String) {
        usbA = UsbAdapter()

        try {
            usbA.printMessage(context, msg)
        } catch (ex: Exception) {
            Crashlytics.logException(ex)
            Toast.makeText(context.applicationContext, ex.message, Toast.LENGTH_LONG).show()
        }
    }

    fun createConn(context: Context) {
        usbA.createCon(context)
    }

    fun unregisterAllReceivers(context: Context) {
        usbA.unregisterAllReceivers(context)
    }
}