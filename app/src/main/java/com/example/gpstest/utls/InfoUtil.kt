package com.example.gpstest.utls

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.gpstest.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class InfoUtil(var activity: Activity) {
    val tag: String = "abc"
    fun setSystemBarsColor(attr: Int) {
        val typedValue = TypedValue()
        activity.theme.resolveAttribute(attr, typedValue, true)
        val window: Window = activity.window

        // Ensure content extends under system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Use WindowInsetsControllerCompat to manage system bars
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34+
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        } else {
            // Fallback for older versions
            @Suppress("DEPRECATION")
            window.statusBarColor = typedValue.data
            @Suppress("DEPRECATION")
            window.navigationBarColor = typedValue.data
        }

        // Check if the current theme is light or dark
        val isLightTheme = (activity.resources.configuration.uiMode
                and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO

        // Set light/dark mode for system bar icons
        insetsController.isAppearanceLightStatusBars = isLightTheme
        insetsController.isAppearanceLightNavigationBars = isLightTheme
    }
    fun showRatingDialog(onCancel:()-> Unit) {
//        val dialogView = LayoutInflater.from(activity).inflate(R.layout.review_dialog, null)
        val dialog = Dialog(activity, R.style.AlertDialogDayNight)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.review_dialog)
        dialog.setCancelable(false)
        dialog.show()
        val stars = listOf(
            dialog.findViewById<ImageView>(R.id.star1),
            dialog.findViewById<ImageView>(R.id.star2),
            dialog.findViewById<ImageView>(R.id.star3),
            dialog.findViewById<ImageView>(R.id.star4),
            dialog.findViewById<ImageView>(R.id.star5)
        )

        val btnCancel = dialog.findViewById<AppCompatButton>(R.id.nobtn)
        val btnSubmit = dialog.findViewById<AppCompatButton>(R.id.yessure)
        val feedbackLayout = dialog.findViewById<TextInputLayout>(R.id.editTextLayout)
        val etFeedback = dialog.findViewById<TextInputEditText>(R.id.etFeedback)

        var selectedRating = 0

        fun updateStars(rating: Int) {
            for (i in stars.indices) {
                stars[i].setImageResource(if (i < rating) R.drawable.selected_start else R.drawable.unselected_start)
            }
            selectedRating = rating
        }

        stars.forEachIndexed { index, star ->
            star.setOnClickListener {
                updateStars(index + 1)
            }
        }

        btnCancel.setOnClickListener {
            onCancel()
            dialog.dismiss() }

        btnSubmit.setOnClickListener {
            if (selectedRating == 0) {
                Toast.makeText(activity, "Please Select stars", Toast.LENGTH_SHORT).show()
            } else if (selectedRating < 4) {
                feedbackLayout.visibility = View.VISIBLE
                if (etFeedback.text.toString().isEmpty()) {
                    Toast.makeText(activity, "Please Enter Feedback", Toast.LENGTH_SHORT).show()
                } else {
                    sendEmail(etFeedback.text.toString()) // Call the global email function
                    dialog.dismiss()
                    onCancel()
                }
            } else {
                feedbackLayout.visibility = View.GONE
                rateUs()
                dialog.dismiss()
                onCancel()
            }
        }

        dialog.show()
    }
    fun sendEmail(feedbackText: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822" // Ensures only email apps handle it
            putExtra(Intent.EXTRA_EMAIL, arrayOf("linsgreen3@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "User Feedback")
            putExtra(Intent.EXTRA_TEXT, feedbackText)
        }

        try {
            activity.startActivity(Intent.createChooser(intent, "Send Feedback via"))
        } catch (e: Exception) {
            Toast.makeText(activity, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }

    fun rateUs() {
        val appPackageName = activity.packageName
        try {
            activity.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")
                )
            )
        } catch (activityNotFoundException: ActivityNotFoundException) {
            activity.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
            )
        }
    }

    fun shareApp() {
        try {
            val appPackageName = activity.packageName
            val myUrl = "https://play.google.com/store/apps/details?id=$appPackageName"
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, myUrl)
            sendIntent.type = "text/plain"
            activity.startActivity(
                Intent.createChooser(
                    sendIntent,
                    "share"
                )
            )
        } catch (e: Exception) {
            // Catch Exception here
        }
    }
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }


    fun openPrivacy() {
        try {
            val url = "https://sites.google.com/view/gpsnavigationmapdirection/home"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            activity.startActivity(browserIntent)
        } catch (e: Exception) {
            // Catch Exception here
        }
    }

    fun openSubscriptionPage() {
        try {
            val url = "http://play.google.com/store/account/subscriptions"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            activity.startActivity(browserIntent)
        } catch (e: Exception) {
            // Catch Exception here
        }
    }

    fun openGooglePrivacy() {
        try {
            val url =
                "https://payments.google.com/payments/apis-secure/u/0/get_legal_document?ldl=en_GB&ldo=0&ldt=buyertos"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            activity.startActivity(browserIntent)
        } catch (e: Exception) {
            // Catch Exception here
        }
    }



    

}