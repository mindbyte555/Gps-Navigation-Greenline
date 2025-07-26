package com.example.gpstest.activities

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gpstest.AdsManager.AdsManager
import com.example.gpstest.MyApp.Companion.apiKey
import com.example.gpstest.MyApp.Companion.bannerEnabled
import com.example.gpstest.MyApp.Companion.baseUrl
import com.example.gpstest.MyApp.Companion.clickWithDebounce
import com.example.gpstest.MyApp.Companion.isEnabled
import com.example.gpstest.MyApp.Companion.isInternetAvailable
import com.example.gpstest.MyApp.Companion.modelName1
import com.example.gpstest.MyApp.Companion.modelName2
import com.example.gpstest.MyApp.Companion.modelName3
import com.example.gpstest.MyApp.Companion.selected_model
import com.example.gpstest.databinding.ActivityAichatBinding
import com.example.gpstest.firebase.FirebaseCustomEvents
import com.example.gpstest.firebase.customevents.Companion.AI_Bot_launched
import com.example.gpstest.firebase.customevents.Companion.AI_Bot_sendbtn
import com.example.gpstest.utls.Prefutils
import com.google.android.gms.ads.AdView
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

class AIChat : BaseActivity() {
    companion object {
        var checkForText = false
    }

    lateinit var binding: ActivityAichatBinding
    private lateinit var messageAdapter: MessageAdapter
    private var showAlert = false
    private var searchCounter = 0
    private var adView: AdView? = null
    lateinit var prefutils: Prefutils
    private lateinit var messageList: MutableList<Message>  // List to store messages

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAichatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkForText = false
        Log.e("tago", "onCreate: $checkForText")
        WindowCompat.setDecorFitsSystemWindows(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime()) // Get keyboard insets

            val topPadding = maxOf(0, systemBars.top - 10) // Adjust top padding
            val bottomPadding =
                if (imeInsets.bottom > 0) imeInsets.bottom else systemBars.bottom // Use keyboard insets if visible

            v.setPadding(
                systemBars.left, topPadding, systemBars.right, bottomPadding
            ) // Apply adjusted padding
            insets
        }

        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = true  // Light status bar icons
        controller.isAppearanceLightNavigationBars = true
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        prefutils = Prefutils(this)


        if (Prefutils(this@AIChat).getBool("is_premium", false) || !bannerEnabled || !isEnabled) {
            binding.adLayout.visibility = View.GONE
        } else {
            binding.adLayout.visibility = View.VISIBLE
            if (isInternetAvailable(this)) {
                AdsManager.loadAiBannerAd(binding.adLayout,
                    this@AIChat,
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








        searchCounter = prefutils.getInt("search_Counter", 0)
        Log.d("counter", "onCreate: $searchCounter")
        FirebaseCustomEvents(this).createFirebaseEvents(AI_Bot_launched, "true")
        messageList = mutableListOf()
        messageAdapter = MessageAdapter(this, messageList)
        binding.recyclerViewChat.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewChat.adapter = messageAdapter
        binding.icBack.clickWithDebounce {
            finish()
        }
        binding.btnSend.clickWithDebounce {
            FirebaseCustomEvents(this).createFirebaseEvents(AI_Bot_sendbtn, "true")
            if (isInternetAvailable(this@AIChat)) {

                binding.tcxGuide.visibility = View.GONE
                binding.idEdtQuery.clearFocus()
                if (!checkForText) {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.idEdtQuery.windowToken, 0)
                    val query = binding.idEdtQuery.text.toString()
                    if (prefutils.getBool("is_premium", false)) {
                        if (query.isNotEmpty()) {
                            // Add the user query message
                            addMessage(Message(role = "user", content = query))
                            binding.idEdtQuery.text?.clear()
                            getResponse(query)
                        } else {
                            Toast.makeText(this, "Please enter your query.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        if (query.isNotEmpty()) {
                            // Add the user query message
                            if (!showAlert) {
                                addMessage(Message(role = "user", content = query))
                            }

                            binding.idEdtQuery.text?.clear()
                            if (searchCounter < 9) {
                                getResponse(query)
                            }

                        } else {
                            Toast.makeText(this, "Please enter your query.", Toast.LENGTH_SHORT)
                                .show()
                        }


                    }
                } else {
                    Toast.makeText(this, "Please wait.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            }


        }
    }

    override fun onResume() {
        super.onResume()
        if (Prefutils(this@AIChat).getBool("is_premium", false) || !bannerEnabled || !isEnabled) {
            binding.adLayout.visibility = View.GONE
        }
    }

    private fun addMessage(message: Message, removeLoading: Boolean = false) {
        if (removeLoading) {
            val loadingIndex = messageList.indexOfFirst { it.content == "loading" }
            if (loadingIndex != -1) {
                messageList.removeAt(loadingIndex)
                messageAdapter.notifyItemRemoved(loadingIndex)
            }
        }

        if (!Prefutils(this).getBool("is_premium", false)) {
            searchCounter++
            prefutils.setInt("search_Counter", searchCounter)
            Log.d("counter", "on message: $searchCounter")

            if (searchCounter > 9) {
                showAlert = true
                message.isAlert = true
            }
        }

        messageList.add(message)
        messageAdapter.notifyItemInserted(messageList.size - 1)
        binding.recyclerViewChat.scrollToPosition(messageList.size - 1)
    }

//    private fun addMessage(message: Message) {
//        if (!Prefutils(this).getBool("is_premium", false)) {
//            searchCounter++
//
//            prefutils.setInt("search_Counter", searchCounter)
//            Log.d("counter", "on message: $searchCounter")
//
//            if (searchCounter > 6) {
//                showAlert = true
//                message.isAlert = true
//            }
//        }
//        messageList.add(message)
//
//        // Notify adapter for the new item
//        messageAdapter.notifyItemInserted(messageList.size - 1)
//
//        binding.recyclerViewChat.scrollToPosition(messageList.size - 1)  // Scroll to the last message
//    }


    private fun getResponse(query: String) {
        var model = "meta-llama/llama-3.1-70b-instruct"

        when (selected_model) {
            1 -> model = modelName1
            2 -> model = modelName2
            3 -> model = modelName3
        }
        checkForText = true
        addMessage(Message(role = "system", content = "loading"))
        val request = OpenAIRequest(
            model = model, messages = listOf(
                Message(
                    role = "system",
                    content = "You are a travel assistant. Provide only place names and key highlights.Keep responses within 100 words."
                ), Message(role = "user", content = query)
            )
        )

        RetrofitInstance.api.getResponse(request).enqueue(object : Callback<OpenAIResponse> {
            override fun onResponse(
                call: Call<OpenAIResponse>, response: Response<OpenAIResponse>
            ) {
                if (response.isSuccessful) {
                    val responseText =
                        response.body()?.choices?.get(0)?.message?.content ?: "No response"
                    // Add the bot response message
                    addMessage(
                        Message(role = "system", content = responseText), removeLoading = true
                    )
                } else {
                    Log.e("TAG", "onResponse: Error: ${response.errorBody()?.string()} ")
                    Toast.makeText(
                        this@AIChat, "Error: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT
                    ).show()
                    addMessage(
                        Message(role = "system", content = "Failed to get response"),
                        removeLoading = true
                    )
                    checkForText = false
                }
            }

            override fun onFailure(call: Call<OpenAIResponse>, t: Throwable) {
                checkForText = false
                Log.e("TAG", "onFailure: ${t.message}")
                Toast.makeText(this@AIChat, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
                addMessage(
                    Message(role = "system", content = "Failed to get response"),
                    removeLoading = true
                )
            }
        })
    }

}

object RetrofitInstance {

    private val client = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
        val request: Request =
            chain.request().newBuilder().addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json").build()
        chain.proceed(request)
    }).build()

    val api: OpenAIService by lazy {
        Retrofit.Builder().baseUrl(baseUrl).client(client)
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(OpenAIService::class.java)
    }
}

interface OpenAIService {
    @POST("chat/completions") // Correct endpoint for chat models
    fun getResponse(@Body request: OpenAIRequest): Call<OpenAIResponse>
}

data class OpenAIResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)

data class OpenAIRequest(
    //  val model: String = "gpt-4o-mini",//gpt-4o-mini
    val model: String,
    val messages: List<Message>,
    val temperature: Double = 0.2,
    val maxTokens: Int = 200,
    val topP: Int = 0.5.toInt()
)

data class Message(val role: String, val content: String, var isAlert: Boolean = false) {
    companion object {
        const val SENT_BY_ME = "user"
    }
}