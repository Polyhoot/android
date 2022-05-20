/*
 * Copyright 2022 Arseniy Graur
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ciphen.polyhoot.game

import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.ciphen.polyhoot.game.event.GameEventType
import net.ciphen.polyhoot.patterns.observer.Observable
import net.ciphen.polyhoot.patterns.observer.Observer
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

private const val WEBSOCKET_CLOSE_CODE = 1000

class WebSocketSession : Observable, WebSocketListener() {
    override val observers: MutableList<Observer> = mutableListOf()
    private val okHttpClient = OkHttpClient()
    private lateinit var webSocket: WebSocket

    companion object {
        private var INSTANCE: WebSocketSession? = null

        fun getInstance(): WebSocketSession = INSTANCE ?: WebSocketSession().also { INSTANCE = it }
    }

    fun openWebSocket(url: String) {
        notifyObservers(Pair(GameEventType.CONNECTING, ""))
        webSocket = okHttpClient.newWebSocket(
            Request.Builder().url(url).build(),
            this
        )
    }

    fun sendMessage(text: String) {
        webSocket.send(text)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        notifyObservers(Pair(GameEventType.CONNECTED, ""))
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        notifyObservers(Pair(GameEventType.NOT_CONNECTED, ""))
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        val json = Json.parseToJsonElement(text)
        Log.i("WebSocketSession", text)
        when (json.jsonObject["event"]!!.jsonPrimitive.content) {
            "CONNECT" -> notifyObservers(Pair(GameEventType.STATUS, json.jsonObject["status"].toString()))
            "NAME_TAKEN" -> { notifyObservers(Pair(GameEventType.NAME_TAKEN, null)) }
            "START_GAME" -> { notifyObservers(Pair(GameEventType.START_GAME, null))}
            "QUESTION" -> { notifyObservers(Pair(GameEventType.QUESTION, text))}
            "FORCE_STOP" -> { notifyObservers(Pair(GameEventType.FORCE_STOP, null)) }
            "GET_READY" -> { notifyObservers(Pair(GameEventType.GET_READY, null))}
            "END" -> { notifyObservers(Pair(GameEventType.END, null)) }
            "TIME_UP" -> { notifyObservers(Pair(GameEventType.TIME_UP, null))}
            else -> notifyObservers(Pair(GameEventType.DEBUG_MESSAGE, text))
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        notifyObservers(Pair(GameEventType.FAIL, t.message))
    }

    fun endSession() {
        webSocket.close(WEBSOCKET_CLOSE_CODE, "GameActivity died.")
    }
}
