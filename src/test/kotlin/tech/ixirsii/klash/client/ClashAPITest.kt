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
import arrow.core.getOrElse
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import tech.ixirsii.klash.error.ClashAPIError
import tech.ixirsii.klash.types.Label
import tech.ixirsii.klash.types.capital.CapitalRaidSeason
import tech.ixirsii.klash.types.clan.Clan
import tech.ixirsii.klash.types.clan.ClanMember
import tech.ixirsii.klash.types.clan.WarFrequency
import tech.ixirsii.klash.types.cwl.ClanWarLeagueGroup
import tech.ixirsii.klash.types.goldpass.GoldPassSeason
import tech.ixirsii.klash.types.league.BuilderBaseLeague
import tech.ixirsii.klash.types.league.CapitalLeague
import tech.ixirsii.klash.types.league.League
import tech.ixirsii.klash.types.league.LeagueSeason
import tech.ixirsii.klash.types.league.PlayerRanking
import tech.ixirsii.klash.types.league.WarLeague
import tech.ixirsii.klash.types.location.ClanBuilderBaseRanking
import tech.ixirsii.klash.types.location.ClanCapitalRanking
import tech.ixirsii.klash.types.location.ClanRanking
import tech.ixirsii.klash.types.location.Location
import tech.ixirsii.klash.types.location.PlayerBuilderBaseRanking
import tech.ixirsii.klash.types.pagination.Page
import tech.ixirsii.klash.types.player.Player
import tech.ixirsii.klash.types.war.State
import tech.ixirsii.klash.types.war.War
import tech.ixirsii.klash.types.war.WarLogEntry
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ClashAPITest {
    private val underTest: ClashAPI

    init {
        val token: String? = System.getenv("API_KEY")

        underTest = if (token != null) {
            ClashAPI(token)
        } else {
            val email: String = System.getenv("API_EMAIL")
            val password: String = System.getenv("API_PASSWORD")

            ClashAPI(email, password).getOrElse { fail("Failed to create ClashAPI: $it") }
        }
    }

    /* *********************************************** Clan APIs ************************************************ */

    @Test
    internal fun `GIVEN clan tag WHEN capitalRaidSeasons THEN returns capital raid seasons`() {
        // When
        val actual: Either<ClashAPIError, Page<CapitalRaidSeason>> =
            underTest.capitalRaidSeasons(CLAN_TAG).block()!!

        // Then
        actual.onRight { seasons ->
            assertTrue("Seasons should not be empty") { seasons.items.isNotEmpty() }
        }.onLeft { fail("Seasons should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN limit WHEN capitalRaidSeasons THEN returns capital raid seasons`() {
        // Given
        val limit = 10

        // When
        val actual: Either<ClashAPIError, Page<CapitalRaidSeason>> =
            underTest.capitalRaidSeasons(CLAN_TAG, limit = limit).block()!!

        // Then
        actual.onRight { seasons ->
            assertTrue("Seasons should not be empty") { seasons.items.isNotEmpty() }
            assertTrue("Seasons should have at most $limit items") { seasons.items.size <= limit }
        }.onLeft { fail("Seasons should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN clan tag WHEN clan THEN returns clan`() {
        // When
        val actual: Either<ClashAPIError, Clan> = underTest.clan(CLAN_TAG).block()!!

        // Then
        actual.onRight { clan: Clan ->
            assertEquals("midwest warrior", clan.name, "Clan name should equal expected")
        }.onLeft {
            fail("Clan should be right but was \"$it\"")
        }
    }

    @Test
    internal fun `GIVEN pound in clan tag WHEN clan THEN formats tag and returns clan`() {
        // Given
        val clanTag = "#$CLAN_TAG"

        // When
        val actual: Either<ClashAPIError, Clan> = underTest.clan(clanTag).block()!!

        // Then
        actual.onRight { clan: Clan ->
            assertEquals("midwest warrior", clan.name, "Clan name should equal expected")
        }.onLeft {
            fail("Clan should be right but was \"$it\"")
        }
    }

    @Test
    internal fun `GIVEN limit WHEN clans THEN returns clans`() {
        // Given
        val limit = 1
        val name = "midwest warrior"

        // When
        val actual: Either<ClashAPIError, Page<Clan>> = underTest.clans(name = name, limit = limit).block()!!

        // Then
        actual.onRight { clans ->
            assertEquals(limit, clans.items.size, "Clans should have one item")
            assertNotEquals("", clans.paging?.cursors?.after, "After cursor should not be empty")
            assertEquals("", clans.paging?.cursors?.before, "Before cursor should be empty")
        }.onLeft { fail("Clans should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN name WHEN clans THEN returns clans`() {
        // Given
        val name = "midwest warrior"

        // When
        val actual: Either<ClashAPIError, Page<Clan>> = underTest.clans(name = name).block()!!

        // Then
        actual.onRight { clans ->
            assertTrue("Clans should not be empty") { clans.items.isNotEmpty() }
            assertNotNull(clans.items.find { it.name == name }, "Clans should contain \"$name\"")
        }.onLeft { fail("Clans should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN warFrequency WHEN clans THEN returns clans`() {
        // Given
        val limit = 10
        val warFrequency = "always"

        // When
        val actual: Either<ClashAPIError, Page<Clan>> =
            underTest.clans(warFrequency = warFrequency, limit = limit).block()!!

        // Then
        actual.onRight { clans ->
            assertTrue("Clans should not be empty") { clans.items.isNotEmpty() }
            assertTrue("Clans war frequency should be always") {
                clans.items.all { it.warFrequency == WarFrequency.ALWAYS }
            }
        }.onLeft { fail("Clans should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN location ID WHEN clans THEN returns clans`() {
        // Given
        val limit = 10

        // When
        val actual: Either<ClashAPIError, Page<Clan>> =
            underTest.clans(locationID = LOCATION_ID, limit = limit).block()!!

        // Then
        actual.onRight { clans ->
            assertTrue("Clans should not be empty") { clans.items.isNotEmpty() }
            assertTrue("Clans location ID should be $LOCATION_ID") {
                clans.items.all { it.location?.id == LOCATION_ID }
            }
        }.onLeft { fail("Clans should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN minMembers WHEN clans THEN returns clans`() {
        // Given
        val limit = 10
        val minMembers = 10

        // When
        val actual: Either<ClashAPIError, Page<Clan>> =
            underTest.clans(minMembers = minMembers, limit = limit).block()!!

        // Then
        actual.onRight { clans ->
            assertTrue("Clans should not be empty") { clans.items.isNotEmpty() }
            assertTrue("Clans should have at least $minMembers members") {
                clans.items.all { it.members >= minMembers }
            }
        }.onLeft { fail("Clans should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN maxMembers WHEN clans THEN returns clans`() {
        // Given
        val limit = 10
        val maxMembers = 10

        // When
        val actual: Either<ClashAPIError, Page<Clan>> =
            underTest.clans(maxMembers = maxMembers, limit = limit).block()!!

        // Then
        actual.onRight { clans ->
            assertTrue("Clans should not be empty") { clans.items.isNotEmpty() }
            assertTrue("Clans should have at most $maxMembers members") { clans.items.all { it.members <= maxMembers } }
        }.onLeft { fail("Clans should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN minClanPoints WHEN clans THEN returns clans`() {
        // Given
        val limit = 10
        val minClanPoints = 1000

        // When
        val actual: Either<ClashAPIError, Page<Clan>> =
            underTest.clans(minClanPoints = minClanPoints, limit = limit).block()!!

        // Then
        actual.onRight { clans ->
            assertTrue("Clans should not be empty") { clans.items.isNotEmpty() }
            assertTrue("Clans should have at least $minClanPoints points") {
                clans.items.all { it.clanPoints >= minClanPoints }
            }
        }.onLeft { fail("Clans should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN minClanLevel WHEN clans THEN returns clans`() {
        // Given
        val limit = 10
        val minClanLevel = 10

        // When
        val actual: Either<ClashAPIError, Page<Clan>> =
            underTest.clans(minClanLevel = minClanLevel, limit = limit).block()!!

        // Then
        actual.onRight { clans ->
            assertTrue("Clans should not be empty") { clans.items.isNotEmpty() }
            assertTrue("Clans should be at least level $minClanLevel") {
                clans.items.all { it.clanLevel >= minClanLevel }
            }
        }.onLeft { fail("Clans should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN label IDs WHEN clans THEN returns clans`() {
        // Given
        val limit = 10
        val labelIDs: List<Int> = listOf(56000000, 56000004, 56000016)
        val labelIDString = "56000000,56000004,56000016"

        // When
        val actual: Either<ClashAPIError, Page<Clan>> =
            underTest.clans(labelIDs = labelIDString, limit = limit).block()!!

        // Then
        actual.onRight { clans ->
            assertTrue("Clans should not be empty") { clans.items.isNotEmpty() }
            assertTrue("Clans' labels should contain $labelIDString") {
                clans.items.all { clan ->
                    clan.labels.all { label ->
                        labelIDs.contains(label.id)
                    }
                }
            }
        }.onLeft { fail("Clans should be right but was \"$it\"") }
    }

    @Disabled("Times will be null when the clan is NOT_IN_WAR")
    @Test
    internal fun `GIVEN clan tag WHEN currentWar THEN returns current war`() {
        // When
        val actual: Either<ClashAPIError, War> = underTest.currentWar(CLAN_TAG).block()!!

        // Then
        actual.onRight { war: War ->
            assertNotNull(war.clan, "Clan should not be null")
            assertNotNull(war.opponent, "Opponent should not be null")
            assertNotNull(war.endTime, "End time should not be null")
            assertNotNull(war.preparationStartTime, "Preparation start time should not be null")
            assertNotNull(war.startTime, "Start time should not be null")
        }.onLeft { fail("War should be right but was \"$it\"") }
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

    @Disabled("The leagueWarTag \"expires\" and the returned war might be NOT_IN_WAR")
    @Test
    internal fun `GIVEN war tag WHEN leagueWar THEN returns league war`() {
        // Given
        val leagueWarTag = "82P0QP0Y2"

        // When
        val actual: Either<ClashAPIError, War> = underTest.leagueWar(leagueWarTag).block()!!

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
                ZonedDateTime.parse("2023-12-07T03:06:27.000Z"),
                war.startTime,
                "Start time should equal expected"
            )
            assertEquals(
                ZonedDateTime.parse("2023-12-07T03:06:27.000Z"),
                war.warStartTime,
                "War start time should equal expected"
            )
        }.onLeft { fail("War should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN clan tag WHEN members THEN returns members`() {
        // When
        val actual: Either<ClashAPIError, Page<ClanMember>> = underTest.members(CLAN_TAG).block()!!

        // Then
        actual.onRight { members ->
            assertTrue("Members should not be empty") { members.items.isNotEmpty() }
            assertTrue("Members should contain member with tag \"$PLAYER_TAG\"") {
                members.items.any { member -> member.tag.endsWith(PLAYER_TAG) }
            }
            assertEquals("", members.paging?.cursors?.after, "After cursor should be empty")
            assertEquals("", members.paging?.cursors?.before, "Before cursor should be empty")
        }.onLeft { fail("Members should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN clan tag WHEN warLog THEN returns war log`() {
        // When
        val actual: Either<ClashAPIError, Page<WarLogEntry>> = underTest.warLog(CLAN_TAG).block()!!

        // Then
        actual.onRight { warLog ->
            assertTrue("War log should not be empty") { warLog.items.isNotEmpty() }
            assertEquals("", warLog.paging?.cursors?.after, "After cursor should be empty")
            assertEquals("", warLog.paging?.cursors?.before, "Before cursor should be empty")
        }.onLeft { fail("War log should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN limit WHEN warLog THEN returns war log`() {
        // Given
        val limit = 1

        // When
        val actual: Either<ClashAPIError, Page<WarLogEntry>> = underTest.warLog(CLAN_TAG, limit = limit).block()!!

        // Then
        actual.onRight { warLog ->
            assertEquals(limit, warLog.items.size, "War log should have one item")
            assertNotEquals("", warLog.paging?.cursors?.after, "After cursor should not be empty")
            assertEquals("", warLog.paging?.cursors?.before, "Before cursor should be empty")
        }.onLeft { fail("War log should be right but was \"$it\"") }
    }

    /*
     * The main thing this is testing is (de)serialization of the cursor.
     */
    @Test
    internal fun `GIVEN after WHEN warLog THEN returns war log after cursor`() {
        // Given
        val limit = 1
        val prefix: Either<ClashAPIError, Page<WarLogEntry>> = underTest.warLog(CLAN_TAG, limit = limit).block()!!

        prefix.onRight { prefixWarLog ->
            // When
            val actual: Either<ClashAPIError, Page<WarLogEntry>> =
                underTest.warLog(CLAN_TAG, limit = limit, after = prefixWarLog.paging?.cursors?.after).block()!!

            // Then
            actual.onRight { warLog ->
                assertEquals(limit, warLog.items.size, "War log should have one item")
                assertNotEquals("", warLog.paging?.cursors?.after, "After cursor should not be empty")
                assertNotEquals("", warLog.paging?.cursors?.before, "Before cursor should not be empty")
                prefixWarLog.items.forEach { prefixItem ->
                    assertFalse("Actual should not contain prefix items") { warLog.items.contains(prefixItem) }
                }
            }.onLeft { fail("War log should be right but was \"$it\"") }
        }.onLeft { fail("Prefix should be right but was \"$it\"") }
    }

    /*
     * The main thing this is testing is (de)serialization of the cursor.
     */
    @Test
    internal fun `GIVEN before WHEN warLog THEN returns war log before cursor`() {
        // Given
        val limit = 1
        val prefix: Either<ClashAPIError, Page<WarLogEntry>> = underTest.warLog(CLAN_TAG, limit = limit).block()!!

        prefix.onRight { prefixWarLog ->
            val suffix: Either<ClashAPIError, Page<WarLogEntry>> =
                underTest.warLog(CLAN_TAG, limit = limit, after = prefixWarLog.paging?.cursors?.after).block()!!

            suffix.onRight { suffixWarLog ->
                // When
                val actual: Either<ClashAPIError, Page<WarLogEntry>> =
                    underTest.warLog(CLAN_TAG, limit = limit, before = suffixWarLog.paging?.cursors?.before).block()!!

                // Then
                actual.onRight { actualWarLog ->
                    assertEquals(limit, actualWarLog.items.size, "War log should have one item")
                    assertNotEquals("", actualWarLog.paging?.cursors?.after, "After cursor should not be empty")
                    assertEquals("", actualWarLog.paging?.cursors?.before, "Before cursor should be empty")

                    prefixWarLog.items.forEach { prefixItem ->
                        assertTrue("Actual should contain prefix items") { actualWarLog.items.contains(prefixItem) }
                    }

                    suffixWarLog.items.forEach { suffixItem ->
                        assertFalse("Actual should not contain suffix items") {
                            actualWarLog.items.contains(suffixItem)
                        }
                    }
                }.onLeft { fail("War log should be right but was \"$it\"") }
            }.onLeft { fail("suffix should be right but was \"$it\"") }
        }.onLeft { fail("Prefix should be right but was \"$it\"") }
    }

    /* ********************************************** Player APIs *********************************************** */

    @Test
    internal fun `GIVEN player tag WHEN player THEN returns player`() {
        // When
        val actual: Either<ClashAPIError, Player> = underTest.player(PLAYER_TAG).block()!!

        // Then
        actual.onRight { player ->
            assertEquals("Ixirsii", player.name, "Player name should equal expected")
        }.onLeft { fail("Player should be right but was \"$it\"") }
    }

    @Disabled("Needs a new API token for every test")
    @Test
    internal fun `GIVEN API token WHEN isPlayerVerified THEN returns verification status`() {
        // When
        val actual: Either<ClashAPIError, Boolean> =
            underTest.isPlayerVerified(PLAYER_TAG, System.getenv("API_TOKEN")).block()!!

        // Then
        // This is a one-time-use token, so it will be invalid, and thus we can't assert anything about the result.
        actual.onLeft { fail("Player should be right but was \"$it\"") }
    }

    /* ********************************************** League APIs *********************************************** */

    @Test
    internal fun `GIVEN league ID WHEN builderBaseLeague THEN returns builder base league`() {
        // Given
        val leagueID = "44000000"

        // When
        val actual: Either<ClashAPIError, BuilderBaseLeague> = underTest.builderBaseLeague(leagueID).block()!!

        // Then
        actual.onRight { league: BuilderBaseLeague ->
            assertEquals(leagueID, league.id.toString(), "League ID should equal expected")
            assertEquals("Wood League V", league.name, "League name should equal expected")
        }.onLeft { fail("Builder base league should be right but was \"$it\"") }
    }

    @Test
    internal fun `WHEN builderBaseLeagues THEN returns capital leagues`() {
        // When
        val actual: Either<ClashAPIError, Page<BuilderBaseLeague>> =
            underTest.builderBaseLeagues().block()!!

        // Then
        actual.onRight { leagues: Page<BuilderBaseLeague> ->
            assertTrue("Leagues should not be empty") { leagues.items.isNotEmpty() }
        }.onLeft { fail("Builder base leagues should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN limit WHEN builderBaseLeagues THEN returns capital leagues`() {
        // Given
        val limit = 10

        // When
        val actual: Either<ClashAPIError, Page<BuilderBaseLeague>> =
            underTest.builderBaseLeagues(limit = limit).block()!!

        // Then
        actual.onRight { leagues: Page<BuilderBaseLeague> ->
            assertTrue("Leagues should not be empty") { leagues.items.isNotEmpty() }
            assertTrue("Leagues should have at most $limit items") { leagues.items.size <= limit }
        }.onLeft { fail("Builder base leagues should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN league ID WHEN capitalLeague THEN returns capital league`() {
        // Given
        val leagueID = "85000000"

        // When
        val actual: Either<ClashAPIError, CapitalLeague> = underTest.capitalLeague(leagueID).block()!!

        // Then
        actual.onRight { league: CapitalLeague ->
            assertEquals(leagueID, league.id.toString(), "League ID should equal expected")
            assertEquals("Unranked", league.name, "League name should equal expected")
        }.onLeft { fail("Capital league should be right but was \"$it\"") }
    }

    @Test
    internal fun `WHEN capitalLeagues THEN returns capital leagues`() {
        // When
        val actual: Either<ClashAPIError, Page<CapitalLeague>> = underTest.capitalLeagues().block()!!

        // Then
        actual.onRight { leagues: Page<CapitalLeague> ->
            assertTrue("Leagues should not be empty") { leagues.items.isNotEmpty() }
        }.onLeft { fail("Capital leagues should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN limit WHEN capitalLeagues THEN returns capital leagues`() {
        // Given
        val limit = 10

        // When
        val actual: Either<ClashAPIError, Page<CapitalLeague>> = underTest.capitalLeagues(limit = limit).block()!!

        // Then
        actual.onRight { leagues: Page<CapitalLeague> ->
            assertTrue("Leagues should not be empty") { leagues.items.isNotEmpty() }
            assertTrue("Leagues should have at most $limit items") { leagues.items.size <= limit }
        }.onLeft { fail("Capital leagues should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN league ID WHEN league THEN returns capital league`() {
        // Given
        val leagueID = "29000000"

        // When
        val actual: Either<ClashAPIError, League> = underTest.league(leagueID).block()!!

        // Then
        actual.onRight { league: League ->
            assertEquals(leagueID, league.id.toString(), "League ID should equal expected")
            assertEquals("Unranked", league.name, "League name should equal expected")
        }.onLeft { fail("League should be right but was \"$it\"") }
    }

    @Test
    internal fun `WHEN leagues THEN returns leagues`() {
        // When
        val actual: Either<ClashAPIError, Page<League>> = underTest.leagues().block()!!

        // Then
        actual.onRight { leagues: Page<League> ->
            assertTrue("Leagues should not be empty") { leagues.items.isNotEmpty() }
        }.onLeft { fail("Leagues should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN limit WHEN leagues THEN returns leagues`() {
        // Give
        val limit = 10

        // When
        val actual: Either<ClashAPIError, Page<League>> = underTest.leagues(limit = limit).block()!!

        // Then
        actual.onRight { leagues: Page<League> ->
            assertTrue("Leagues should not be empty") { leagues.items.isNotEmpty() }
            assertTrue("Leagues should have at most $limit items") { leagues.items.size <= limit }
        }.onLeft { fail("Leagues should be right but was \"$it\"") }
    }

    @Disabled("This request times out if the seasonID is not the current league season.")
    @Test
    internal fun `GIVEN IDs WHEN leagueSeason THEN returns league seasons`() {
        // Given
        val limit = 10
        val leagueID = "29000022"
        val seasonID = "2024-03"

        // When
        val actual: Either<ClashAPIError, Page<PlayerRanking>> =
            underTest.leagueSeason(leagueID = leagueID, seasonID = seasonID, limit = limit).block()!!

        // Then
        actual.onRight { rankings: Page<PlayerRanking> ->
            assertTrue("Rankings should not be empty") { rankings.items.isNotEmpty() }
        }.onLeft { fail("Rankings should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN league ID WHEN leagueSeasons THEN returns league seasons`() {
        // Given
        val leagueID = "29000022"

        // When
        val actual: Either<ClashAPIError, Page<LeagueSeason>> =
            underTest.leagueSeasons(leagueID = leagueID).block()!!

        // Then
        actual.onRight { seasons: Page<LeagueSeason> ->
            assertTrue("Seasons should not be empty") { seasons.items.isNotEmpty() }
        }.onLeft { fail("Seasons should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN limit WHEN leagueSeasons THEN returns league seasons`() {
        // Given
        val limit = 10
        val leagueID = "29000022"

        // When
        val actual: Either<ClashAPIError, Page<LeagueSeason>> =
            underTest.leagueSeasons(leagueID = leagueID, limit = limit).block()!!

        // Then
        actual.onRight { seasons: Page<LeagueSeason> ->
            assertTrue("Seasons should not be empty") { seasons.items.isNotEmpty() }
            assertTrue("Seasons should have at most $limit items") { seasons.items.size <= limit }
        }.onLeft { fail("Seasons should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN league ID WHEN warLeague THEN returns capital league`() {
        // Given
        val leagueID = "48000000"

        // When
        val actual: Either<ClashAPIError, WarLeague> = underTest.warLeague(leagueID).block()!!

        // Then
        actual.onRight { league: WarLeague ->
            assertEquals(leagueID, league.id.toString(), "League ID should equal expected")
            assertEquals("Unranked", league.name, "League name should equal expected")
        }.onLeft { fail("War league should be right but was \"$it\"") }
    }

    @Test
    internal fun `WHEN warLeagues THEN returns league seasons`() {
        // When
        val actual: Either<ClashAPIError, Page<WarLeague>> = underTest.warLeagues().block()!!

        // Then
        actual.onRight { leagues: Page<WarLeague> ->
            assertTrue("War leagues should not be empty") { leagues.items.isNotEmpty() }
        }.onLeft { fail("War leagues should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN limit WHEN warLeagues THEN returns league seasons`() {
        // Given
        val limit = 10

        // When
        val actual: Either<ClashAPIError, Page<WarLeague>> = underTest.warLeagues(limit = limit).block()!!

        // Then
        actual.onRight { leagues: Page<WarLeague> ->
            assertTrue("War leagues should not be empty") { leagues.items.isNotEmpty() }
            assertTrue("War leagues should have at most $limit items") { leagues.items.size <= limit }
        }.onLeft { fail("War leagues should be right but was \"$it\"") }
    }

    /* ********************************************* Location APIs ********************************************** */

    @Test
    internal fun `GIVEN location ID WHEN clanBuilderBaseRankings THEN returns clan rankings`() {
        // When
        val actual: Either<ClashAPIError, Page<ClanBuilderBaseRanking>> =
            underTest.clanBuilderBaseRankings(LOCATION_ID).block()!!

        // Then
        actual.onRight { rankings: Page<ClanBuilderBaseRanking> ->
            assertTrue("Rankings should not be empty") { rankings.items.isNotEmpty() }
        }.onLeft { fail("Rankings should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN limit WHEN clanBuilderBaseRankings THEN returns clan rankings`() {
        // Given
        val limit = 10

        // When
        val actual: Either<ClashAPIError, Page<ClanBuilderBaseRanking>> =
            underTest.clanBuilderBaseRankings(LOCATION_ID, limit = limit).block()!!

        // Then
        actual.onRight { rankings: Page<ClanBuilderBaseRanking> ->
            assertTrue("Rankings should not be empty") { rankings.items.isNotEmpty() }
            assertTrue("Rankings should have at most $limit items") { rankings.items.size <= limit }
        }.onLeft { fail("Rankings should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN location ID WHEN clanRankings THEN returns clan rankings`() {
        // When
        val actual: Either<ClashAPIError, Page<ClanRanking>> =
            underTest.clanRankings(LOCATION_ID).block()!!

        // Then
        actual.onRight { rankings: Page<ClanRanking> ->
            assertTrue("Rankings should not be empty") { rankings.items.isNotEmpty() }
        }.onLeft { fail("Rankings should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN limit WHEN clanRankings THEN returns clan rankings`() {
        // Given
        val limit = 10

        // When
        val actual: Either<ClashAPIError, Page<ClanRanking>> =
            underTest.clanRankings(LOCATION_ID, limit = limit).block()!!

        // Then
        actual.onRight { rankings: Page<ClanRanking> ->
            assertTrue("Rankings should not be empty") { rankings.items.isNotEmpty() }
            assertTrue("Rankings should have at most $limit items") { rankings.items.size <= limit }
        }.onLeft { fail("Rankings should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN location ID WHEN clanCapitalRankings THEN returns clan capital rankings`() {
        // When
        val actual: Either<ClashAPIError, Page<ClanCapitalRanking>> =
            underTest.clanCapitalRankings(LOCATION_ID).block()!!

        // Then
        actual.onRight { rankings: Page<ClanCapitalRanking> ->
            assertTrue("Rankings should not be empty") { rankings.items.isNotEmpty() }
        }.onLeft { fail("Rankings should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN limit WHEN clanCapitalRankings THEN returns clan capital rankings`() {
        // Given
        val limit = 10

        // When
        val actual: Either<ClashAPIError, Page<ClanCapitalRanking>> =
            underTest.clanCapitalRankings(LOCATION_ID, limit = limit).block()!!

        // Then
        actual.onRight { rankings: Page<ClanCapitalRanking> ->
            assertTrue("Rankings should not be empty") { rankings.items.isNotEmpty() }
            assertTrue("Rankings should have at most $limit items") { rankings.items.size <= limit }
        }.onLeft { fail("Rankings should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN location ID WHEN location THEN returns location`() {
        // When
        val actual: Either<ClashAPIError, Location> = underTest.location(LOCATION_ID).block()!!

        // Then
        actual.onRight { locations: Location ->
            assertEquals("US", locations.countryCode, "Country code should equal expected")
            assertTrue("Location should be a country") { locations.isCountry }
            assertEquals("United States", locations.name, "Name should equal expected")
        }.onLeft { fail("Location should be right but was \"$it\"") }
    }

    @Test
    internal fun `WHEN locations THEN returns locations`() {
        // When
        val actual: Either<ClashAPIError, Page<Location>> = underTest.locations().block()!!

        // Then
        actual.onRight { locations: Page<Location> ->
            assertTrue("Locations should not be empty") { locations.items.isNotEmpty() }
        }.onLeft { fail("Locations should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN limit WHEN locations THEN returns locations`() {
        // Given
        val limit = 10

        // When
        val actual: Either<ClashAPIError, Page<Location>> = underTest.locations(limit = limit).block()!!

        // Then
        actual.onRight { locations: Page<Location> ->
            assertTrue("Locations should not be empty") { locations.items.isNotEmpty() }
            assertTrue("Locations should have at most $limit items") { locations.items.size <= limit }
        }.onLeft { fail("Locations should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN location ID WHEN playerBuilderBaseRankings THEN returns player rankings`() {
        // When
        val actual: Either<ClashAPIError, Page<PlayerBuilderBaseRanking>> =
            underTest.playerBuilderBaseRankings(LOCATION_ID).block()!!

        // Then
        actual.onRight { rankings: Page<PlayerBuilderBaseRanking> ->
            assertTrue("Rankings should not be empty") { rankings.items.isNotEmpty() }
        }.onLeft { fail("Rankings should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN limit WHEN playerBuilderBaseRankings THEN returns player rankings`() {
        // Given
        val limit = 10

        // When
        val actual: Either<ClashAPIError, Page<PlayerBuilderBaseRanking>> =
            underTest.playerBuilderBaseRankings(LOCATION_ID, limit = limit).block()!!

        // Then
        actual.onRight { rankings: Page<PlayerBuilderBaseRanking> ->
            assertTrue("Rankings should not be empty") { rankings.items.isNotEmpty() }
            assertTrue("Rankings should have at most $limit items") { rankings.items.size <= limit }
        }.onLeft { fail("Rankings should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN location ID WHEN playerRankings THEN returns player rankings`() {
        // When
        val actual: Either<ClashAPIError, Page<tech.ixirsii.klash.types.location.PlayerRanking>> =
            underTest.playerRankings(LOCATION_ID).block()!!

        // Then
        actual.onRight { rankings: Page<tech.ixirsii.klash.types.location.PlayerRanking> ->
            assertTrue("Rankings should not be empty") { rankings.items.isNotEmpty() }
        }.onLeft { fail("Rankings should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN limit WHEN playerRankings THEN returns player rankings`() {
        // Given
        val limit = 10

        // When
        val actual: Either<ClashAPIError, Page<tech.ixirsii.klash.types.location.PlayerRanking>> =
            underTest.playerRankings(LOCATION_ID, limit = limit).block()!!

        // Then
        actual.onRight { rankings: Page<tech.ixirsii.klash.types.location.PlayerRanking> ->
            assertTrue("Rankings should not be empty") { rankings.items.isNotEmpty() }
            assertTrue("Rankings should have at most $limit items") { rankings.items.size <= limit }
        }.onLeft { fail("Rankings should be right but was \"$it\"") }
    }

    /* ********************************************* Gold Pass APIs ********************************************* */

    @Test
    internal fun `GIVEN nothing WHEN currentGoldPassSeason THEN returns current gold pass season`() {
        // When
        val actual: Either<ClashAPIError, GoldPassSeason> = underTest.currentGoldPassSeason().block()!!

        // Then
        actual.onLeft { fail("Season should be right but was \"$it\"") }
    }

    /* *********************************************** Label APIs *********************************************** */

    @Test
    internal fun `WHEN clanLabels THEN returns clan labels`() {
        // When
        val actual: Either<ClashAPIError, Page<Label>> = underTest.clanLabels().block()!!

        // Then
        actual.onRight { labels: Page<Label> ->
            assertTrue("Labels should not be empty") { labels.items.isNotEmpty() }
        }.onLeft { fail("Labels should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN limit WHEN clanLabels THEN returns clan labels`() {
        // Given
        val limit = 10

        // When
        val actual: Either<ClashAPIError, Page<Label>> = underTest.clanLabels(limit = limit).block()!!

        // Then
        actual.onRight { labels: Page<Label> ->
            assertTrue("Labels should not be empty") { labels.items.isNotEmpty() }
            assertTrue("Labels should have at most $limit items") { labels.items.size <= limit }
        }.onLeft { fail("Labels should be right but was \"$it\"") }
    }

    @Test
    internal fun `WHEN playerLabels THEN returns player labels`() {
        // When
        val actual: Either<ClashAPIError, Page<Label>> = underTest.playerLabels().block()!!

        // Then
        actual.onRight { labels: Page<Label> ->
            assertTrue("Labels should not be empty") { labels.items.isNotEmpty() }
        }.onLeft { fail("Labels should be right but was \"$it\"") }
    }

    @Test
    internal fun `GIVEN limit WHEN playerLabels THEN returns player labels`() {
        // Given
        val limit = 10

        // When
        val actual: Either<ClashAPIError, Page<Label>> = underTest.playerLabels(limit = limit).block()!!

        // Then
        actual.onRight { labels: Page<Label> ->
            assertTrue("Labels should not be empty") { labels.items.isNotEmpty() }
            assertTrue("Labels should have at most $limit items") { labels.items.size <= limit }
        }.onLeft { fail("Labels should be right but was \"$it\"") }
    }

    /* *************************************** Private utility functions **************************************** */

    companion object {
        private const val CLAN_TAG = "2Q82UJVY"
        private const val LOCATION_ID = 32000249
        private const val PLAYER_TAG = "2Q09RPGL8"
    }
}
