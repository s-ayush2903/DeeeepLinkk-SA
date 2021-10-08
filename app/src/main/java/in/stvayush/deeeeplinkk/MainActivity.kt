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
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {
    private val TAG = "DeepLink"

    private lateinit var parentLayout: View
    private lateinit var rawUri: String
    private lateinit var uri: Uri

    private val cachedDeepLinkFlag = "cached_dl"
    private val famDl = "fp://fampay.in/"
    private val appendTrailingSlashFlag = "wanna_slash"
    private var slashState: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        parentLayout = findViewById(android.R.id.content)

        findViewById<TextInputLayout>(R.id.uri_til).editText?.setText(
            getPreferences(Context.MODE_PRIVATE)
                .getString(cachedDeepLinkFlag, famDl)
        )
        findViewById<Button>(R.id.deepLink_triggerer).setOnClickListener {
            hideKeyboard()
            triggerDeepLink()
        }
        findViewById<Button>(R.id.clear_all).setOnClickListener {
            findViewById<TextInputLayout>(R.id.uri_til).editText?.setText("")
        }
        findViewById<CheckBox>(R.id.optional_slash_flag).apply {
            Log.d(
                TAG,
                "onCreate: setting checkboxxx: ${
                    getPreferences(Context.MODE_PRIVATE).getBoolean(
                        appendTrailingSlashFlag,
                        slashState
                    )
                }"
            )
            isChecked = getPreferences(Context.MODE_PRIVATE).getBoolean(
                appendTrailingSlashFlag,
                slashState
            )
            setOnClickListener {
                Log.d(TAG, "Checkbox: ${this.isChecked}")
                slashState = isChecked
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // fix it nigga | It works as expected when @triggerDeepLink button is clicked, otherwise wrong in all cases
        with(getPreferences(Context.MODE_PRIVATE).edit()) {
            Log.d(TAG, "onDestroy: called, $slashState")
            putBoolean(appendTrailingSlashFlag, slashState)
            if (this@MainActivity::rawUri.isInitialized && rawUri.isNotBlank()) {
                putString(cachedDeepLinkFlag, rawUri)
            }
            apply()
        }
    }

    private fun triggerDeepLink() {
        rawUri = findViewById<TextInputLayout>(R.id.uri_til).editText?.text?.trim().toString()
            .lowercase()
        Log.d(TAG, "rawUri: $rawUri")
        if (rawUri.isNotBlank()) {
            rawUri =
                if (findViewById<CheckBox>(R.id.optional_slash_flag).isChecked) rawUri.appendSlashIfNeeded() else rawUri
            uri = Uri.parse(rawUri)
            try {
                val potentialIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addCategory(CATEGORY_BROWSABLE)
                    flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_REQUIRE_NON_BROWSER
                }
                startActivity(potentialIntent)
                finish()
            } catch (e: ActivityNotFoundException) {
                if (rawUri.startsWith("https://") || rawUri.startsWith("http://")) {
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
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    companion object {
        val short = Snackbar.LENGTH_SHORT
        val looong = Snackbar.LENGTH_LONG
    }
}
