package net.ciphen.polyhoot.game

import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.ciphen.polyhoot.game.event.GameEvent
import net.ciphen.polyhoot.game.event.GameEventType
import net.ciphen.polyhoot.patterns.observer.Observable
import net.ciphen.polyhoot.patterns.observer.Observer
import okhttp3.*

class WebSocketSession() : Observable, WebSocketListener() {
    override val observers: MutableList<Observer> = mutableListOf()
    private val okHttpClient = OkHttpClient()
    private lateinit var webSocket: WebSocket

    companion object {
        const val WS_URL = "ws://192.168.31.181:8080/game/session"
    }

    fun openWebSocket() {
        notifyObservers(Pair(GameEventType.CONNECTING, ""))
        webSocket = okHttpClient.newWebSocket(
            Request.Builder().url(WS_URL).build(),
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

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
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
        webSocket.close(1000, "GameActivity died.")
    }
}