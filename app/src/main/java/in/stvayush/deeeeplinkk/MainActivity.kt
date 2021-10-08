package `in`.stvayush.deeeeplinkk

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.CATEGORY_BROWSABLE
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {
    private val TAG = "DeepLink"

    private lateinit var uri: Uri
    private lateinit var rawUri: String

    private val cachedDeepLinkFlag = "cached_dl"
    private val famDl = "fp://fampay.in/"
    private lateinit var parentLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        parentLayout = findViewById(android.R.id.content)

        findViewById<TextInputLayout>(R.id.uri_til).editText?.setText(
            getPreferences(Context.MODE_PRIVATE)
                .getString(cachedDeepLinkFlag, famDl)
        )
        findViewById<AppCompatButton>(R.id.deepLink_triggerer).setOnClickListener {
            hideKeyboard()
            triggerDeepLink()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        with(getPreferences(Context.MODE_PRIVATE).edit()) {
            putString(cachedDeepLinkFlag, rawUri)
            apply()
        }
    }

    private fun triggerDeepLink() {
        rawUri = findViewById<TextInputLayout>(R.id.uri_til).editText?.text?.trim().toString().lowercase()
        Log.d(TAG, "rawUri: $rawUri")
        if (rawUri.isNotBlank()) {
            uri = Uri.parse(rawUri.appendSlashIfNeeded())
            try {
                val potentialIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addCategory(CATEGORY_BROWSABLE)
                    flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_REQUIRE_NON_BROWSER
                }
                startActivity(potentialIntent)
                finish()
            } catch (e: ActivityNotFoundException) {
                // FIXME: 08/10/21  Replace with SnackBar & Coroutines
                Toast.makeText(
                    this,
                    "No Application present to handle this Deep Link \nRedirecting to default browser!",
                    looong
                ).show()
//                parentLayout.makeSnack(
//                    "No Application present to handle this Deep Link \nRedirecting to default browser!",
//                    short
//                )
                if (rawUri.startsWith("https://") || rawUri.startsWith("http://")) {
                    startActivity(Intent(Intent.ACTION_VIEW, uri).apply {
                        addCategory(CATEGORY_BROWSABLE)
                        flags = FLAG_ACTIVITY_NEW_TASK
                    })
                } else {
                    parentLayout.makeSnack("Invalid DeepLink", short)
                }
            }
        } else {
            parentLayout.makeSnack("DeepLink cannot be empty", short)
            return
        }
    }

    private fun String.appendSlashIfNeeded(): String = if (!endsWith('/')) "${this}/" else this

    private fun View.makeSnack(message: String, duration: Int) =
        Snackbar.make(this, message, duration).show()

    private fun hideKeyboard() {
        val imm: InputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = currentFocus
        if (view == null) {
            view = View(this)

        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    companion object {
        val short = Snackbar.LENGTH_SHORT
        val looong = Snackbar.LENGTH_LONG
    }
}
