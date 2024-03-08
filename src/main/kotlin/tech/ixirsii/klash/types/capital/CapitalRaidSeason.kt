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

package tech.ixirsii.klash.types.capital

import kotlinx.serialization.Serializable
import tech.ixirsii.klash.serialize.ZonedDateTimeSerializer
import java.time.ZonedDateTime

/**
 * Clan capital raid season.
 *
 * @property attackLog List of attacks made by clan members.
 * @property capitalTotalLoot Total loot earned by the clan.
 * @property defenseLog List of defenses made by the clan.
 * @property defensiveReward Clan's defensive reward.
 * @property endTime End time of the season.
 * @property enemyDistrictsDestroyed Number of enemy districts destroyed.
 * @property offensiveReward Clan's offensive reward.
 * @property members List of clan members.
 * @property raidsCompleted Number of raids completed.
 * @property startTime Start time of the season.
 * @property state State of the season.
 * @property totalAttacks Total number of attacks made by the clan.
 * @author Ixirsii <ixirsii@ixirsii.tech>
 */
@Serializable
public data class CapitalRaidSeason(
    val attackLog: List<CapitalRaidSeasonAttackLog> = emptyList(),
    val capitalTotalLoot: Int = 0,
    val defenseLog: List<CapitalRaidSeasonDefenseLog> = emptyList(),
    val defensiveReward: Int = 0,
    @Serializable(with = ZonedDateTimeSerializer::class) val endTime: ZonedDateTime? = null,
    val enemyDistrictsDestroyed: Int = 0,
    val offensiveReward: Int = 0,
    val members: List<CapitalRaidSeasonMember> = emptyList(),
    val raidsCompleted: Int = 0,
    @Serializable(with = ZonedDateTimeSerializer::class) val startTime: ZonedDateTime? = null,
    val state: String = "",
    val totalAttacks: Int = 0,
)
