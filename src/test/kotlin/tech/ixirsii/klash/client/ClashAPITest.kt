/*
 * Copyright (c) Ryan Porterfield 2023.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of KlashAPI nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package tech.ixirsii.klash.client

import arrow.core.Either
import org.junit.jupiter.api.assertDoesNotThrow
import tech.ixirsii.klash.error.ClashAPIError
import tech.ixirsii.klash.types.cwl.ClanWarLeagueGroup
import tech.ixirsii.klash.types.war.State
import tech.ixirsii.klash.types.war.War
import tech.ixirsii.klash.types.war.WarLog
import java.io.FileInputStream
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.fail

internal class ClashAPITest {
    private lateinit var underTest: ClashAPI

    @BeforeTest
    internal fun setUp() {
        val tokens = Properties()
        tokens.load(FileInputStream(CONFIG))
        underTest = ClashAPI(tokens.getProperty("apiKey"))
    }

    @Test
    internal fun `GIVEN clan tag WHEN leagueGroup THEN returns league group or not found`() {
        // When
        val actual: Either<ClashAPIError, ClanWarLeagueGroup> = underTest.leagueGroup(CLAN_TAG).block()!!

        // Then
        actual.onRight { leagueGroup ->
            assertTrue("Clans should not be empty") { leagueGroup.clans.isNotEmpty() }
            assertDoesNotThrow("") { leagueGroup.clans.first { it.tag.endsWith(CLAN_TAG) } }
            assertTrue { leagueGroup.rounds.isNotEmpty() }
            assertNotEquals("", leagueGroup.season, "Season should not be empty")
        }.onLeft {
                when (it) {
                    // Succeed test on NotFound because CWL may not be active when test is run
                    is ClashAPIError.ClientError.NotFound -> Unit
                    else -> fail("Unexpected error \"$it\"")
                }
            }
    }

    @Test
    internal fun `GIVEN war tag WHEN leagueWar THEN returns league war`() {
        // When
        val actual: Either<ClashAPIError, War> = underTest.leagueWar(CLAN_WAR_LEAGUE_WAR_TAG).block()!!

        // Then
        actual.onRight { war ->
            assertEquals(State.ENDED, war.state, "War should be ended")
            assertEquals(30, war.teamSize, "Team size should equal expected")
            assertEquals(ZonedDateTime.parse("2023-12-08T03:06:27.000Z"), war.endTime, "End time should equal expected")
            assertEquals(
                ZonedDateTime.parse("2023-12-06T03:08:12.000Z"),
                war.preparationStartTime,
                "Preparation start time should equal expected"
            )
            assertEquals(
                ZonedDateTime.parse("2023-12-07T03:06:27.000Z"), war.startTime, "Start time should equal expected"
            )
            assertEquals(
                ZonedDateTime.parse("2023-12-07T03:06:27.000Z"),
                war.warStartTime,
                "War start time should equal expected"
            )
        }.onLeft { fail("War should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN clan tag WHEN warLog THEN returns war log`() {
        // When
        val actual: Either<ClashAPIError, WarLog> = underTest.warLog(CLAN_TAG).block()!!

        // Then
        actual.onRight { warLog ->
            assertTrue("War log should not be empty") { warLog.items.isNotEmpty() }
            assertEquals("", warLog.paging.cursors.after, "After cursor should be empty")
            assertEquals("", warLog.paging.cursors.before, "Before cursor should be empty")
        }.onLeft { fail("War log should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN limit WHEN warLog THEN returns war log`() {
        // When
        val actual: Either<ClashAPIError, WarLog> = underTest.warLog(CLAN_TAG, limit = 1).block()!!

        // Then
        actual.onRight { warLog ->
            assertEquals(1, warLog.items.size, "War log should have one item")
            assertNotEquals("", warLog.paging.cursors.after, "After cursor should not be empty")
            assertEquals("", warLog.paging.cursors.before, "Before cursor should be empty")
        }.onLeft { fail("War log should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN after WHEN warLog THEN returns war log after cursor`() {
        // Given
        val prefix: Either<ClashAPIError, WarLog> = underTest.warLog(CLAN_TAG, limit = 1).block()!!

        prefix.onRight { prefixWarLog ->
            // When
            val actual: Either<ClashAPIError, WarLog> =
                underTest.warLog(CLAN_TAG, limit = 1, after = prefixWarLog.paging.cursors.after).block()!!

            // Then
            actual.onRight { warLog ->
                assertEquals(1, warLog.items.size, "War log should have one item")
                assertNotEquals("", warLog.paging.cursors.after, "After cursor should not be empty")
                assertNotEquals("", warLog.paging.cursors.before, "Before cursor should not be empty")
                prefixWarLog.items.forEach { prefixItem ->
                    assertFalse("Actual should not contain prefix items") { warLog.items.contains(prefixItem) }
                }
            }.onLeft { fail("War log should be right but was \"$it\"") }
        }.onLeft { fail("Prefix should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN before WHEN warLog THEN returns war log before cursor`() {
        // Given
        val prefix: Either<ClashAPIError, WarLog> = underTest.warLog(CLAN_TAG, limit = 1).block()!!

        prefix.onRight { prefixWarLog ->
            // When
            val suffix: Either<ClashAPIError, WarLog> =
                underTest.warLog(CLAN_TAG, limit = 1, after = prefixWarLog.paging.cursors.after).block()!!

            // Then
            suffix.onRight { suffixWarLog ->
                // When
                val actual: Either<ClashAPIError, WarLog> =
                    underTest.warLog(CLAN_TAG, limit = 1, before = suffixWarLog.paging.cursors.before).block()!!

                actual.onRight { actualWarLog ->
                    assertEquals(1, actualWarLog.items.size, "War log should have one item")
                    assertNotEquals("", actualWarLog.paging.cursors.after, "After cursor should not be empty")
                    assertEquals("", actualWarLog.paging.cursors.before, "Before cursor should be empty")

                    prefixWarLog.items.forEach { prefixItem ->
                        assertTrue("Actual should contain prefix items") { actualWarLog.items.contains(prefixItem) }
                    }

                    suffixWarLog.items.forEach { suffixItem ->
                        assertFalse("Actual should not contain suffix items") { actualWarLog.items.contains(suffixItem) }
                    }
                }.onLeft { fail("War log should be right but was \"$it\"") }
            }.onLeft { fail("suffix should be right but was \"$it\"") }
        }.onLeft { fail("Prefix should be right but was \"$it\"") }
    }

    companion object {
        private const val CLAN_TAG = "2Q82UJVY"
        private const val CONFIG = "tokens.properties"
        private const val PLAYER_TAG = "2Q09RPGL8"
        private const val CLAN_WAR_LEAGUE_WAR_TAG = "82P0QP0Y2"
    }
}
