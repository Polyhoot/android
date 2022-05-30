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

package net.ciphen.polyhoot

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import net.ciphen.polyhoot.activity.MainActivity
import net.ciphen.polyhoot.enums.TestStage
import net.ciphen.polyhoot.mockhost.MockHost
import net.ciphen.polyhoot.patterns.observer.Observable
import net.ciphen.polyhoot.patterns.observer.Observer
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.TypeSafeMatcher
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TAG = "GameTest"

@LargeTest
@RunWith(AndroidJUnit4::class)
class GameTest : Observer {
    @Rule
    @JvmField
    var mActivityScenarioRule = ActivityScenarioRule(MainActivity::class.java)
    private val mockHost = MockHost()
    private lateinit var gameId: String
    private var testCompleted = false

    @Test
    fun gameTest() {
        startHostTest()
        /*
            Test progresses asynchronously
            Launch waiter for testCompleted
            to become true in order to finish test
         */
        while (!testCompleted) {
            Thread.sleep(500)
        }
        Log.i(TAG, "Test completed!")
    }

    private fun startHostTest() {
        assertEquals(0, mockHost.observers.size)
        mockHost.addObserver(this)
        assertEquals(1, mockHost.observers.size)
        mockHost.start()
        Log.i(TAG, "Start host test passed [1/7]")
    }

    private fun startGameTest() {
        val statusTextView = onView(
            allOf(
                withId(R.id.game_status_text),
                isDisplayed()
            )
        )
        val nameTextView = onView(
            allOf(
                withId(R.id.name_text),
                isDisplayed()
            )
        )
        val gameIdText = onView(
            allOf(
                withId(R.id.game_id_text),
                isDisplayed()
            )
        )

        statusTextView.check(matches(withText("Starting game!")))
        nameTextView.check(matches(withText("test")))
        gameIdText.check(matches(withText(containsString(mockHost.gameId.toString()))))

        Log.i(TAG, "Start game test passed [2/7]")
    }

    private fun joinGame() {
        val textInputEditText = onView(
            allOf(
                withId(R.id.game_uid_field),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.game_uid_field_layout),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        textInputEditText.perform(replaceText(mockHost.gameId), closeSoftKeyboard())

        val textInputEditText2 = onView(
            allOf(
                withId(R.id.game_uid_field), withText(mockHost.gameId),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.game_uid_field_layout),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        textInputEditText2.perform(pressImeActionButton())

        val extendedFloatingActionButton = onView(
            allOf(
                withId(R.id.game_uid_enter), withText("Play"),
                childAtPosition(
                    childAtPosition(
                        withId(android.R.id.content),
                        0
                    ),
                    4
                ),
                isDisplayed()
            )
        )
        extendedFloatingActionButton.perform(click())

        val textInputEditText3 = onView(
            allOf(
                withId(R.id.name_field),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.name_field_layout),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        textInputEditText3.perform(replaceText("test"))

        val extendedFloatingActionButton2 = onView(
            allOf(
                withId(R.id.game_join), withText("Join game"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.game_fragment_container_view),
                        0
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        extendedFloatingActionButton2.perform(click())
    }

    private fun getReadyTest() {
        val statusTextView = onView(
            allOf(
                withId(R.id.game_status_text),
                isDisplayed()
            )
        )
        statusTextView.check(matches(withText("Get ready to answer!")))
        Log.i(TAG, "Get ready test passed [3/7]")
    }

    private fun answerReceivedTest() {
        val statusTextView = onView(
            allOf(
                withId(R.id.game_status_text),
                isDisplayed()
            )
        )
        statusTextView.check(matches(withText("Waiting for others…")))
        Log.i(TAG, "Answer received test passed [4/7]")
    }

    private fun timeUpTest() {
        val statusTextView = onView(
            allOf(
                withId(R.id.game_status_text),
                isDisplayed()
            )
        )
        statusTextView.check(matches(withText("Good job! Keep it up!")))
        Log.i(TAG, "Time up test passed [5/7]")
    }

    private fun scoreTest(score: Int) {
        onView(
            allOf(
                withId(R.id.score_text),
                isDisplayed()
            )
        ).check(matches(withText(containsString(score.toString()))))
        Log.i(TAG, "Score test passed [6/7]")
    }

    private fun gameEndTest() {
        onView(
            allOf(
                withId(R.id.game_status_text),
                isDisplayed()
            )
        ).check(matches(withText("Game has ended.")))
        onView(
            allOf(
                withId(R.id.go_back_button),
                isDisplayed()
            )
        ).perform(click())
        Log.i(TAG, "Game end test passed [7/7]")
        testCompleted = true
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }

    fun getString(resId: Int) =
        InstrumentationRegistry.getInstrumentation().context.getString(resId)

    override fun update(o: Observable, arg: Any?) {
        Thread.sleep(2000)
        when (arg as TestStage) {
            TestStage.GAME_CREATE -> gameId = mockHost.gameId!!
            TestStage.WAIT_FOR_PLAYER -> joinGame()
            TestStage.GAME_STARTED -> {
                startGameTest()
                mockHost.getReady()
            }
            TestStage.GET_READY -> {
                getReadyTest()
                mockHost.sendQuestion()
            }
            TestStage.QUESTION ->
                onView(
                    allOf(
                        withId(R.id.answer_1),
                        isDisplayed()
                    )
                ).perform(click())
            TestStage.ANSWER_RECEIVED -> answerReceivedTest()
            TestStage.TIME_UP -> timeUpTest()
            TestStage.SCOREBOARD -> scoreTest(mockHost.score!!)
            TestStage.GAME_END -> gameEndTest()
        }
    }
}
