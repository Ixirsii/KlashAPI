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

import kotlinx.serialization.Serializable
import tech.ixirsii.klash.types.PlayerHouse
import tech.ixirsii.klash.types.Role
import tech.ixirsii.klash.types.league.BuilderBaseLeague
import tech.ixirsii.klash.types.league.League

/**
 * Member of a clan.
 *
 * @property builderBaseLeague The builder base league the player is in.
 * @property builderBaseTrophies The player's builder base trophies.
 * @property clanRank The player's rank in the clan.
 * @property donations The number of troops the player has donated.
 * @property donationsReceived The number of troops the player has received.
 * @property expLevel The player's experience level.
 * @property league The league the player is in.
 * @property name The player's name.
 * @property playerHouse The player's home.
 * @property previousClanRank The player's previous rank in the clan.
 * @property role The player's role in the clan.
 * @property tag The player's tag.
 * @property townHallLevel The player's town hall level.
 * @property trophies The player's trophies.
 * @property versusTrophies The player's builder base trophies.
 * @author Ixirsii <ixirsii@ixirsii.tech>
 */
@Serializable
data class ClanMember(
    val builderBaseLeague: BuilderBaseLeague? = null,
    val builderBaseTrophies: Int = 0,
    val clanRank: Int = 0,
    val donations: Int = 0,
    val donationsReceived: Int = 0,
    val expLevel: Int = 0,
    val league: League? = null,
    val name: String = "",
    val playerHouse: PlayerHouse? = null,
    val previousClanRank: Int = 0,
    val role: Role = Role.NOT_MEMBER,
    val tag: String = "",
    val townHallLevel: Int = 0,
    val trophies: Int = 0,
    val versusTrophies: Int = 0,
)
