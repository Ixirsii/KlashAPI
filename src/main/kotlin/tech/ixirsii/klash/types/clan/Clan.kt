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

package tech.ixirsii.klash.types.clan

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tech.ixirsii.klash.types.BadgeURLs

/**
 * A clan.
 *
 * @property badgeURLs The badge URLs of the clan.
 * @property capitalLeague The capital league of the clan.
 * @property chatLanguage The chat language of the clan.
 * @property clanBuilderBasePoints The builder base points of the clan.
 * @property clanCapital The clan capital.
 * @property clanCapitalPoints The clan capital points of the clan.
 * @property clanLevel The level of the clan.
 * @property clanPoints The points of the clan.
 * @property clanVersusPoints The versus points of the clan.
 * @property description The description of the clan.
 * @property isFamilyFriendly Whether the clan is family friendly.
 * @property isWarLogPublic Whether the war log of the clan is public.
 * @property labels The labels of the clan.
 * @property location The location of the clan.
 * @property members The number of members in the clan.
 * @property memberList The list of members in the clan.
 * @property name The name of the clan.
 * @property requiredBuilderBaseTrophies The required builder base trophies to join the clan.
 * @property requiredTownHallLevel The required town hall level to join the clan.
 * @property requiredTrophies The required trophies to join the clan.
 * @property requiredVersusTrophies The required versus trophies to join the clan.
 * @property tag The clan tag.
 * @property type The invite type of the clan.
 * @property warFrequency How frequently the clan wars.
 * @property warLeague The war league of the clan.
 * @property warLosses The number of war losses of the clan.
 * @property warTies The number of war ties of the clan.
 * @property warWins The number of war wins of the clan.
 * @property warWinStreak How many wars the clan has won in a row.
 * @author Ixirsii <ixirsii@ixirsii.tech>
 */
@Serializable
data class Clan(
    @SerialName("badgeUrls") val badgeURLs: BadgeURLs? = null,
    val capitalLeague: CapitalLeague? = null,
    val chatLanguage: Language? = null,
    val clanBuilderBasePoints: Int = 0,
    val clanCapital: ClanCapital? = null,
    val clanCapitalPoints: Int = 0,
    val clanLevel: Int = 0,
    val clanPoints: Int = 0,
    @Deprecated("Use clanBuilderBasePoints instead") val clanVersusPoints: Int = 0,
    val description: String = "",
    val isFamilyFriendly: Boolean = false,
    val isWarLogPublic: Boolean = false,
    val labels: List<Label> = emptyList(),
    val location: Location? = null,
    val members: Int = 0,
    val memberList: List<ClanMember> = emptyList(),
    val name: String = "",
    val requiredBuilderBaseTrophies: Int = 0,
    @SerialName("requiredTownhallLevel") val requiredTownHallLevel: Int = 0,
    val requiredTrophies: Int = 0,
    @Deprecated("Use requiredBuilderBaseTrophies instead") val requiredVersusTrophies: Int = 0,
    val tag: String = "",
    val type: Type = Type.CLOSED,
    val warFrequency: WarFrequency = WarFrequency.UNKNOWN,
    val warLeague: WarLeague? = null,
    val warLosses: Int = 0,
    val warTies: Int = 0,
    val warWins: Int = 0,
    val warWinStreak: Int = 0,
)
