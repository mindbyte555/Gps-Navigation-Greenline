package com.example.gpstest.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.gpstest.AdsManager.AdsManager
import com.example.gpstest.MyApp.Companion.bannerEnabled
import com.example.gpstest.MyApp.Companion.clickWithDebounce
import com.example.gpstest.MyApp.Companion.isEnabled
import com.example.gpstest.MyApp.Companion.isInternetAvailable
import com.example.gpstest.R
import com.example.gpstest.databinding.ActivitySearchBinding
import com.example.gpstest.firebase.FirebaseCustomEvents
import com.example.gpstest.firebase.customevents
import com.example.gpstest.gps.LocationPermissionHelper
import com.example.gpstest.utls.Constants.destinationName
import com.example.gpstest.utls.InfoUtil
import com.example.gpstest.utls.Prefutils
import com.google.android.gms.ads.AdView
import com.mapbox.search.ApiType
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.autofill.Query
import com.mapbox.search.offline.OfflineResponseInfo
import com.mapbox.search.offline.OfflineSearchEngine
import com.mapbox.search.offline.OfflineSearchEngineSettings
import com.mapbox.search.offline.OfflineSearchResult
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.ui.adapter.engines.SearchEngineUiAdapter
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultsView
import java.util.Locale

class Search : BaseActivity() {
    private var searchLocationLatitude = 0.0
    private var searchLocationLongitude = 0.0
    private val tag = javaClass.simpleName
    var navigatingFromsearch = false
    private var voice = false
    private var search = false
    private var adView: AdView? = null
    lateinit var binding: ActivitySearchBinding
    private lateinit var locationHelper: LocationPermissionHelper
    private var isSearchInProgress = false
    var voiceclick = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // API 30+
            window.insetsController?.show(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        locationHelper = LocationPermissionHelper(this)
        checkPermissions()

        voice = intent.getBooleanExtra("voice", false)
        search = intent.getBooleanExtra("search", false)
        if (voice) {
            binding.title.text = resources.getString(R.string.voice_search)
            binding.voiceSearch.visibility = View.VISIBLE
            voiceclick = true
        } else if (search) {
            binding.title.text = resources.getString(R.string.get_direction)
            binding.voiceSearch.visibility = View.VISIBLE
            voiceclick = true
        } else {
            binding.voiceSearch.visibility = View.INVISIBLE
        }
        InfoUtil(this).setSystemBarsColor(R.attr.backgroundColor)

        binding.icBack.setOnClickListener {
            finish()
        }
        binding.voiceSearch.setOnClickListener {
            getSpeechStart()
        }
        binding.searchResults.initialize(
            SearchResultsView.Configuration(
                commonConfiguration = CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL)
            )
        )

        val searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
            apiType = ApiType.GEOCODING,
            settings = SearchEngineSettings(getString(R.string.mapbox_access_token))
        )

        val offlineSearchEngine =
            OfflineSearchEngine.create(OfflineSearchEngineSettings(getString(R.string.mapbox_access_token)))

        val searchEngineUiAdapter = SearchEngineUiAdapter(
            view = binding.searchResults,
            searchEngine = searchEngine,
            offlineSearchEngine = offlineSearchEngine
        )

        searchEngineUiAdapter.addSearchListener(object : SearchEngineUiAdapter.SearchListener {
            override fun onCategoryResultsShown(
                suggestion: SearchSuggestion,
                results: List<SearchResult>,
                responseInfo: ResponseInfo
            ) {

            }

            override fun onError(e: Exception) {
                Log.e(tag, "onError: ${e.message}")
            }

            override fun onFeedbackItemClick(responseInfo: ResponseInfo) {
                Log.e(tag, "onFeedbackItemClick: ")
//                responseInfo.requestOptions.options.origin.latitude()
            }

            override fun onHistoryItemClick(historyRecord: HistoryRecord) {
                binding.queryEditText.setText(historyRecord.name)
                searchLocationLatitude = historyRecord.coordinate.latitude()
                searchLocationLongitude = historyRecord.coordinate.longitude()
                Log.e(tag, "onHistoryItemClick: ")
            }

            override fun onOfflineSearchResultSelected(
                searchResult: OfflineSearchResult,
                responseInfo: OfflineResponseInfo
            ) {
            }

            override fun onOfflineSearchResultsShown(
                results: List<OfflineSearchResult>,
                responseInfo: OfflineResponseInfo
            ) {
            }

            override fun onPopulateQueryClick(
                suggestion: SearchSuggestion,
                responseInfo: ResponseInfo
            ) {
//                binding.queryEditText.setText(suggestion.name)
            }

            @SuppressLint("SuspiciousIndentation")
            override fun onSearchResultSelected(
                searchResult: SearchResult,
                responseInfo: ResponseInfo
            ) {
                val name = searchResult.name
                searchLocationLatitude = searchResult.coordinate.latitude()
                searchLocationLongitude = searchResult.coordinate.longitude()


                if (intent.hasExtra("from") && intent.getStringExtra("from") == "LocationPicker") {
                    val intent = Intent()
                    intent.putExtra("name", name)
                    intent.putExtra("lat", searchLocationLatitude)
                    intent.putExtra("lng", searchLocationLongitude)

                    setResult(RESULT_OK, intent)
                    finish()

                } else {
                    destinationName = searchResult.name

                    startActivity(
                        Intent(this@Search, Navigate::class.java)
                            .putExtra("destinationLatitude", searchLocationLatitude)
                            .putExtra("destinationLongitude", searchLocationLongitude)
                            .putExtra("address", name)
                    )
                    if (!voiceclick) {
                        finish()
                    }

                }


                Log.e(tag, "onSearchResultSelected: ")

            }

            override fun onSuggestionSelected(searchSuggestion: SearchSuggestion): Boolean {
                binding.queryEditText.setText(searchSuggestion.name)
                return false
            }

            override fun onSuggestionsShown(
                suggestions: List<SearchSuggestion>,
                responseInfo: ResponseInfo
            ) {
            }
        })

        binding.queryEditText.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, after: Int) {
                val queryText = s.toString().trim()
                if (queryText.isNotEmpty()) {
                    val query = Query.create(queryText)

                    if (query != null) {
                        lifecycleScope.launchWhenStarted {
                            if (queryText.length in 5..100) {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    if (!isSearchInProgress) {
                                        searchEngineUiAdapter.search(queryText)
                                    }
                                    // alertDialogLoading?.dismiss()
                                }, 1000)
                            }
                        }
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(e: Editable) {}
        })


        binding.queryEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = Query.create(binding.queryEditText.toString())
                if (query != null) {
                    searchEngineUiAdapter.search(binding.queryEditText.toString())
                }

                true
            } else false
        }
        binding.grantpermission.clickWithDebounce {
            locationHelper.requestLocationPermission(this)
        }

    }

    override fun onResume() {
        navigatingFromsearch = false
        checkPermissions()
        super.onResume()
    }

    private fun checkPermissions() {
        when {
            !locationHelper.isLocationPermissionGranted() -> {
                binding.permissionLayout.visibility = View.VISIBLE
                binding.groupcontent.visibility = View.GONE
            }

            else -> {
                binding.permissionLayout.visibility = View.GONE
                binding.groupcontent.visibility = View.VISIBLE
                loadads()
            }
        }
    }

    private fun loadads() {
        if (Prefutils(this@Search).getBool("is_premium", false) || !bannerEnabled || !isEnabled) {

            binding.adLayout.visibility = View.GONE

        } else {
            binding.adLayout.visibility = View.VISIBLE
            if (isInternetAvailable(this)) {
                AdsManager.loadHomeBannerAd(binding.adLayout,
                    this@Search, "search",
                    object : AdsManager.AdmobBannerAdListener {
                        override fun onAdFailed() {
                            Log.e("TEST TAG", "onAdFailed: Banner")
                        }

                        override fun onAdLoaded() {
                            Log.e("TEST TAG", "onAdLoaded: Banner")
                        }
                    })?.let { adView = it }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LocationPermissionHelper.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissions()
            } else {

                locationHelper.openAppSettings(this)
            }
        }
    }

    private fun getSpeechStart() {
        try {
            val mIntent = Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            )
            mIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            mIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
            )
            startActivityForResult(mIntent, 10)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "voice not supported", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            10 -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (!result.isNullOrEmpty()) {
                        val recognizedText = result[0]
                        binding.queryEditText.isFocusableInTouchMode = true
                        binding.queryEditText.requestFocus()
                        binding.queryEditText.isCursorVisible = true
                        binding.queryEditText.setText(recognizedText)
                        binding.queryEditText.setSelection(binding.queryEditText.text.length)

                    }
                }
            }
        }

    }

    override fun onDestroy() {
        adView?.let { it.destroy() }
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        if (!navigatingFromsearch) {
            FirebaseCustomEvents(this).createFirebaseEvents(customevents.search_disappear, "true")
        }
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        navigatingFromsearch = true
    }
}