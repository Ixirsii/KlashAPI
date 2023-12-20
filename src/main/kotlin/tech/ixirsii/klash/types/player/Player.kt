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

package tech.ixirsii.klash.types.player

import kotlinx.serialization.Serializable
import tech.ixirsii.klash.types.BuilderBaseLeague
import tech.ixirsii.klash.types.Label
import tech.ixirsii.klash.types.League
import tech.ixirsii.klash.types.PlayerHouse
import tech.ixirsii.klash.types.Role

/**
 * Clash of Clans player.
 *
 * @property achievements List of player achievements.
 * @property attackWins Number of attack wins.
 * @property bestBuilderBaseTrophies Best builder base trophies.
 * @property bestTrophies Best trophies.
 * @property builderBaseLeague Builder base league.
 * @property builderBaseTrophies Builder base trophies.
 * @property builderHallLevel Builder hall level.
 * @property clan Player's clan.
 * @property clanCapitalContributions Clan capital contributions.
 * @property defenseWins Number of defense wins.
 * @property donations Number of troops donated.
 * @property donationsReceived Number of troops received.
 * @property expLevel Experience level.
 * @property heroes Hero levels.
 * @property heroEquipment Hero equipment.
 * @property labels Player labels.
 * @property league Player league.
 * @property legendStatistics Player legend statistics.
 * @property name Player name.
 * @property playerHouse Clan capital house.
 * @property role Clan role.
 * @property spells Spell levels.
 * @property tag Player tag.
 * @property townHallLevel Town hall level.
 * @property townHallWeaponLevel Town hall weapon level.
 * @property troops Troop levels.
 * @property trophies Number of trophies.
 * @property warPreference War preference.
 * @property warStars Number of war stars.
 */
@Serializable
data class Player(
    val achievements: List<PlayerAchievementProgress> = emptyList(),
    val attackWins: Int = 0,
    val bestBuilderBaseTrophies: Int = 0,
    val bestTrophies: Int = 0,
    val builderBaseLeague: BuilderBaseLeague? = null,
    val builderBaseTrophies: Int = 0,
    val builderHallLevel: Int = 0,
    val clan: PlayerClan? = null,
    val clanCapitalContributions: Int = 0,
    val defenseWins: Int = 0,
    val donations: Int = 0,
    val donationsReceived: Int = 0,
    val expLevel: Int = 0,
    val heroes: List<PlayerItemLevel> = emptyList(),
    val heroEquipment: List<HeroEquipment> = emptyList(),
    val labels: List<Label> = emptyList(),
    val league: League? = null,
    val legendStatistics: PlayerLegendStatistics? = null,
    val name: String = "",
    val playerHouse: PlayerHouse? = null,
    val role: Role = Role.NOT_MEMBER,
    val spells: List<PlayerItemLevel> = emptyList(),
    val tag: String = "",
    val townHallLevel: Int = 0,
    val townHallWeaponLevel: Int = 0,
    val troops: List<PlayerItemLevel> = emptyList(),
    val trophies: Int = 0,
    val warPreference: WarPreference = WarPreference.OUT,
    val warStars: Int = 0,
)
