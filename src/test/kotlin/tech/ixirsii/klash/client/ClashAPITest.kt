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
import reactor.core.publisher.Mono
import tech.ixirsii.klash.exception.ClashAPIException
import tech.ixirsii.klash.types.clan.ClanWarLeagueGroup
import java.io.FileInputStream
import java.util.Properties
import kotlin.test.BeforeTest
import kotlin.test.Test
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
        val actual: Mono<Either<ClashAPIException, ClanWarLeagueGroup>> = underTest.leagueGroup(CLAN_TAG)

        // Then
        val either: Either<ClashAPIException, ClanWarLeagueGroup> = actual.block()!!

        either.onRight { leagueGroup ->
            assertTrue("Clans should not be empty") { leagueGroup.clans.isNotEmpty() }
            assertDoesNotThrow("") { leagueGroup.clans.first { it.tag.endsWith(CLAN_TAG) } }
            assertTrue { leagueGroup.rounds.isNotEmpty() }
            assertNotEquals("", leagueGroup.season, "Season should not be empty")
        }
            .onLeft {
                when (it) {
                    // Succeed test on NotFound because CWL may not be active when test is run
                    is ClashAPIException.NotFound -> Unit
                    else -> fail("Unexpected exception")
                }
            }
    }

    companion object {
        private const val CLAN_TAG = "2Q82UJVY"
        private const val CONFIG = "tokens.properties"
        private const val PLAYER_TAG = "2Q09RPGL8"
    }
}
