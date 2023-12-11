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
 * @author Ixirsii <ixirsii@ixirsii.tech>
 */
@Serializable
data class ClanWarLeagueGroup(
    /**
     * Clans in CWL group.
     */
    val clans: List<ClanWarLeagueClan>,
    /**
     * CWL group rounds.
     */
    val rounds: List<ClanWarLeagueRound>,
    /**
     * CWL group season.
     */
    val season: String,
    /**
     * CWL group state.
     */
    val state: State,
    /**
     * CWL group tag. This seems to always be null.
     */
    val tag: String = "",
) {
    /**
     * CWL group state.
     */
    enum class State {
        /**
         * Group not found.
         */
        @SerialName("groupNotFound")
        GROUP_NOT_FOUND,
        /**
         * Not in war.
         */
        @SerialName("notInWar")
        NOT_IN_WAR,
        /**
         * Preparation day.
         */
        @SerialName("preparation")
        PREPARATION,
        /**
         * War day.
         */
        @SerialName("war")
        WAR,
        /**
         * CWL ended.
         */
        @SerialName("ended")
        ENDED
    }
}
