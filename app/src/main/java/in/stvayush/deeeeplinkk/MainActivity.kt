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
import com.google.android.material.snackbar.Snackbar
import `in`.stvayush.deeeeplinkk.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
  private val TAG = "DeepLink"

  private lateinit var parentLayout: View
  private lateinit var rawUri: String
  private lateinit var uri: Uri
  private lateinit var activityMainBinding: ActivityMainBinding

  private val cachedDeepLinkFlag = "cached_dl"
  private val defDl = "https://nerds.airbnb.com"
  private val appendTrailingSlashFlag = "wanna_slash"
  private var slashState: Boolean = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(activityMainBinding.root)
    parentLayout = findViewById(android.R.id.content)

    with(activityMainBinding) {
      uriTil.editText?.setText(
        getPreferences(Context.MODE_PRIVATE).getString(cachedDeepLinkFlag, defDl)
      )
      deepLinkTriggerer.setOnClickListener {
        hideKeyboard()
        triggerDeepLink()
      }
      clearAll.setOnClickListener { uriTil.editText?.setText("") }
      optionalSlashFlag.apply {
        Log.d(
          TAG,
          "onCreate: setting checkboxxx: ${
          getPreferences(Context.MODE_PRIVATE).getBoolean(
            appendTrailingSlashFlag,
            slashState
          )
          }"
        )
        isChecked =
          getPreferences(Context.MODE_PRIVATE).getBoolean(appendTrailingSlashFlag, slashState)
        setOnClickListener {
          Log.d(TAG, "Clicked Checkbox: ${this.isChecked}")
          slashState = isChecked
        }
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    // fix it | It works as expected ONLY when @triggerDeepLink button is clicked
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
    rawUri = activityMainBinding.uriTil.editText?.text?.trim().toString().lowercase()
    Log.d(TAG, "rawUri: $rawUri")
    if (rawUri.isNotBlank()) {
      uri = Uri.parse(rawUri.appendSlashIfNeeded(activityMainBinding.optionalSlashFlag.isChecked))
      try {
        val potentialIntent =
          Intent(Intent.ACTION_VIEW, uri).apply {
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
            )
            .show()
          //                parentLayout.makeSnack(
          //                    "No Application present to handle this Deep Link \nRedirecting to
          // default browser!",
          //                    short
          //                )
          startActivity(
            Intent(Intent.ACTION_VIEW, uri).apply {
              addCategory(CATEGORY_BROWSABLE)
              flags = FLAG_ACTIVITY_NEW_TASK
            }
          )
          finish()
        } else {
          parentLayout.makeSnack("Invalid DeepLink", short)
        }
      }
    } else {
      parentLayout.makeSnack("DeepLink cannot be empty", short)
      return
    }
  }

  private fun String.appendSlashIfNeeded(wannaAppend: Boolean): String =
    if (!endsWith('/') && wannaAppend) "$this/" else this

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
