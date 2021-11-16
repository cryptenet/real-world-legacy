package com.cryptenet.rwl_rest

import com.google.common.truth.Truth.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature

object ExampleUnitTest : Spek({
    Feature("Example") {
        var target = 0

        Scenario("Sample Test") {
            When("adding 2 with 2") {
                target = 2 + 2
            }

            Then("it should be equal to 4") {
                assertThat(target).isEqualTo(4)
            }
        }
    }
})
