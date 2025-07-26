package com.example.gpstest.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.gpstest.R
import com.example.gpstest.activities.AIChat.Companion.checkForText
import com.example.gpstest.firebase.FirebaseCustomEvents
import com.example.gpstest.firebase.customevents.Companion.AI_Bot_upgrade

class MessageAdapter(private val context: Context, private val messageList: MutableList<Message>) :
    RecyclerView.Adapter<MessageAdapter.MyViewHolder>() {
    private val animatedPositions = HashSet<Int>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val chatView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_item, parent, false)
        return MyViewHolder(chatView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val message = messageList[position]

        holder.alertLayout.visibility = if (message.isAlert) {View.VISIBLE  } else View.GONE
        holder.leftChatView.visibility = View.GONE
        holder.rightChatView.visibility = View.GONE
        holder.loader.visibility=View.GONE

        if (!message.isAlert) {
            if (message.role == Message.SENT_BY_ME) {
                holder.rightChatView.visibility = View.VISIBLE
                holder.rightTextView.text = message.content
            } else {
                if (message.content == "loading") {
                    holder.loader.visibility = View.VISIBLE // Show Lottie animation
                } else {
                    holder.leftChatView.visibility = View.VISIBLE
                    if (!animatedPositions.contains(position)) {
                        animateText(holder.leftTextView, message.content){
                            checkForText=false
                            Log.e("checking", "onBindViewHolder: $checkForText")
                        }
                        animatedPositions.add(position) // Mark position as animated
                    } else {
                        holder.leftTextView.text = message.content // Set text directly
                    }
                }
            }
        }
//        if (!message.isAlert) {
//            if (message.role == Message.SENT_BY_ME) {
//                holder.rightChatView.visibility = View.VISIBLE
//                holder.rightTextView.text = message.content
//
//            } else {
//                holder.leftChatView.visibility = View.VISIBLE
//                if (!animatedPositions.contains(position)) {
//                    animateText(holder.leftTextView, message.content)
//                    animatedPositions.add(position) // Mark position as animated
//                } else {
//                    holder.leftTextView.text = message.content // Set text directly
//                }
//
//            }
//        }
        // Handle Upgrade Button Click
        holder.upgradeButton.setOnClickListener {
            FirebaseCustomEvents(context).createFirebaseEvents(AI_Bot_upgrade, "true")
            val intent = Intent(context, InApp_Purchase_Screen::class.java)
            intent.putExtra("startActivity", true)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var leftChatView: LinearLayout = itemView.findViewById(R.id.left_chat_view)
        var rightChatView: LinearLayout = itemView.findViewById(R.id.right_chat_view)
        var leftTextView: TextView = itemView.findViewById(R.id.left_chat_text_view)
        var rightTextView: TextView = itemView.findViewById(R.id.right_chat_text_view)
        var alertLayout: LinearLayout = itemView.findViewById(R.id.alert_layout)
        var upgradeButton: Button = itemView.findViewById(R.id.upgrade_button)
        var loader:LottieAnimationView=itemView.findViewById(R.id.loading_animation)
    }
    @SuppressLint("SetTextI18n")
    private fun animateText(textView: TextView, text: String,onComplete: () -> Unit) {
        textView.text = "" // Clear existing text
        val handler = Handler(Looper.getMainLooper())
        var index = 0

        val runnable = object : Runnable {
            override fun run() {
                if (index < text.length) {
                    textView.text = textView.text.toString() + text[index]
                    index++
                    handler.postDelayed(this, 30) // Adjust speed here (50ms per character)
                }
                else {
                    onComplete() // Call the callback when animation completes
                }

            }
        }
        handler.post(runnable)
    }
}
