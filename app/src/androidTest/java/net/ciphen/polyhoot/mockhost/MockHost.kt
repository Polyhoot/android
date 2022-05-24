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

package net.ciphen.polyhoot.mockhost

import android.util.Log
import kotlinx.serialization.json.*
import net.ciphen.polyhoot.enums.TestStage
import net.ciphen.polyhoot.patterns.observer.Observable
import net.ciphen.polyhoot.patterns.observer.Observer
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

private const val GAME_CREATE_URL = "wss://polyhoot.ciphen.net/game/create"
private const val GAME_HOST_URL = "wss://polyhoot.ciphen.net/game/host"
private const val TAG = "MockHost"

class MockHost : WebSocketListener(), Observable {
    override val observers: MutableList<Observer> = mutableListOf()
    private lateinit var webSocket: WebSocket
    private val okHttpClient = OkHttpClient()
    var gameId: String? = null
    var score: Int? = null
    private var stage = TestStage.GAME_CREATE

    fun start() {
        Log.i(TAG, "Creating game...")
        openWebSocket(GAME_CREATE_URL)
    }

    private fun waitForTestPlayer() {
        Log.i(TAG, "Closing /game/create websocket...")
        webSocket.cancel()
        Log.i(TAG, "Connecting as game host...")
        openWebSocket(GAME_HOST_URL)
        webSocket.send(
            JsonObject(
                mapOf(
                    Pair("action", JsonPrimitive("connect")),
                    Pair("gameId", JsonPrimitive(gameId!!.toInt()))
                )
            ).toString()
        )
        Log.i(TAG, "Connected as game host. Waiting for player.")
        notifyObservers(TestStage.WAIT_FOR_PLAYER.also { stage = it })
    }

    private fun startGame() {
        webSocket.send(
            JsonObject(
                mapOf(
                    Pair("action", JsonPrimitive("start_game"))
                )
            ).toString()
        )
        Log.i(TAG, "Game started!")
        notifyObservers(TestStage.GAME_STARTED.also { stage = it })
    }

    fun getReady() {
        webSocket.send(
            JsonObject(
                mapOf(
                    Pair("action", JsonPrimitive("get_ready"))
                )
            ).toString()
        )
        notifyObservers(TestStage.GET_READY.also { stage = it })
    }

    fun sendQuestion() {
        webSocket.send(
            JsonObject(
                mapOf(
                    Pair("action", JsonPrimitive("send_question")),
                    Pair("duration", JsonPrimitive(10)),
                    Pair("answer", JsonPrimitive(0))
                ),
            ).toString()
        )
        notifyObservers(TestStage.QUESTION.also { stage = it })
    }

    private fun receivedAnswer() {
        notifyObservers(TestStage.ANSWER_RECEIVED.also { stage = it })
        Log.i(TAG, "Right answer received!")
        timeUp()
    }

    private fun timeUp() {
        webSocket.send(
            JsonObject(
                mapOf(
                    Pair("action", JsonPrimitive("time_up"))
                )
            ).toString()
        )
        notifyObservers(TestStage.TIME_UP.also { stage = it })
        scoreboard()
    }

    private fun scoreboard() {
        webSocket.send(
            JsonObject(
                mapOf(
                    Pair("action", JsonPrimitive("scoreboard"))
                )
            ).toString()
        )
        stage = TestStage.SCOREBOARD
    }

    private fun endGame() {
        webSocket.send(
            JsonObject(
                mapOf(
                    Pair("action", JsonPrimitive("end"))
                )
            ).toString()
        )
        notifyObservers(TestStage.GAME_END)
    }

    private fun openWebSocket(url: String) {
        webSocket = okHttpClient.newWebSocket(
            Request.Builder().url(url).build(),
            this
        )
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        when (stage) {
            TestStage.GAME_CREATE -> {
                val json = Json.parseToJsonElement(text)
                gameId = json
                    .jsonObject["gameId"]
                    ?.jsonPrimitive
                    ?.content
                    ?: throw IllegalStateException("Received null game id.")
                Log.i(TAG, "Received gameId = $gameId")
                notifyObservers(TestStage.GAME_CREATE)
                waitForTestPlayer()

            }
            TestStage.WAIT_FOR_PLAYER -> {
                val json = Json.parseToJsonElement(text)
                val action = json
                    .jsonObject["action"]
                    ?.jsonPrimitive
                    ?.content
                    ?: throw IllegalStateException("Received null action code!")
                if (action == "player_connected") {
                    Log.i(TAG, "Player connected!")
                    val name = json
                        .jsonObject["name"]
                        ?.jsonPrimitive
                        ?.content
                        ?: throw IllegalStateException("Player with null name?")
                    if (name == "test") {
                        println("Test player detected. Starting game.")
                        startGame()
                    }
                } else {
                    throw IllegalStateException("how?")
                }
            }
            TestStage.QUESTION -> {
                val json = Json.parseToJsonElement(text)
                val action = json
                    .jsonObject["action"]
                    ?.jsonPrimitive
                    ?.content
                    ?: throw IllegalStateException("Received null action code!")
                if (action == "answer") {
                    Log.i(TAG, "Received answer!")
                    val answer = json
                        .jsonObject["answer"]
                        ?.jsonPrimitive
                        ?.int
                        ?: throw IllegalStateException("how?")
                    if (answer == 0) {
                        receivedAnswer()
                    } else {
                        throw IllegalStateException("how?")
                    }
                }
            }
            TestStage.SCOREBOARD -> {
                val json = Json.parseToJsonElement(text)
                val action = json
                    .jsonObject["action"]
                    ?.jsonPrimitive
                    ?.content
                    ?: throw IllegalStateException("Received null action code!")
                if (action == "scoreboard") {
                    Log.i(TAG, "Received scoreboard!")
                    score = json
                        .jsonObject["scoreboard"]
                        ?.jsonArray?.get(0)
                        ?.jsonObject?.get("score")
                        ?.jsonPrimitive
                        ?.int
                        ?: throw IllegalStateException("Didn't receive score.")
                    notifyObservers(TestStage.SCOREBOARD)
                    endGame()
                }
            }
        }
    }
}