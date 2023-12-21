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

package tech.ixirsii.klash.types.cwl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Clan War League group.
 *
 * @property clans Clans in CWL group.
 * @property rounds CWL group rounds.
 * @property season CWL group season.
 * @property state CWL group state.
 * @property tag CWL group tag. This seems to always be null.
 * @author Ixirsii <ixirsii@ixirsii.tech>
 */
@Serializable
data class ClanWarLeagueGroup(
    val clans: List<ClanWarLeagueClan> = emptyList(),
    val rounds: List<ClanWarLeagueRound> = emptyList(),
    val season: String = "",
    val state: State = State.NOT_IN_WAR,
    val tag: String = "",
) {
    /**
     * CWL group state.
     *
     * @author Ixirsii <ixirsii@ixirsii.tech>
     */
    enum class State {
        @SerialName("groupNotFound")
        GROUP_NOT_FOUND,

        @SerialName("notInWar")
        NOT_IN_WAR,

        @SerialName("preparation")
        PREPARATION,

        @SerialName("war")
        WAR,

        @SerialName("ended")
        ENDED
    }
}
