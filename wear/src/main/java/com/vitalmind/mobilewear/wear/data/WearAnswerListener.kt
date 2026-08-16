package com.vitalmind.mobilewear.wear.data

import android.content.Context
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WearAnswerListener(
    context: Context
) : MessageClient.OnMessageReceivedListener {

    private val messageClient =
        Wearable.getMessageClient(context)

    private val _answer =
        MutableStateFlow<String?>(null)

    val answer: StateFlow<String?> =
        _answer.asStateFlow()

    fun start() {
        messageClient.addListener(this)
    }

    fun stop() {
        messageClient.removeListener(this)
    }

    override fun onMessageReceived(
        messageEvent: MessageEvent
    ) {
        if (
            messageEvent.path ==
            WearMessagePaths.CHAT_ANSWER
        ) {
            _answer.value =
                messageEvent.data
                    .toString(
                        Charsets.UTF_8
                    )
        }
    }
}