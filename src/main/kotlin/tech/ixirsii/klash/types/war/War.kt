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

package tech.ixirsii.klash.types.war

import kotlinx.serialization.Serializable
import tech.ixirsii.klash.serialize.ZonedDateTimeSerializer
import java.time.ZonedDateTime

/**
 * Clan War League war.
 *
 * @property attacksPerMember Attacks per member.
 * @property clan First clan in war.
 * @property endTime War end time.
 * @property opponent Second clan in war.
 * @property preparationStartTime Preparation start time.
 * @property startTime Attack day start time.
 * @property state War state.
 * @property teamSize Team size.
 * @property warStartTime War start time (?).
 * @author Ixirsii <ixirsii@ixirsii.tech>
 */
@Serializable
public data class War(
    val attacksPerMember: Int = 0,
    val clan: WarClan? = null,
    @Serializable(with = ZonedDateTimeSerializer::class) val endTime: ZonedDateTime? = null,
    val opponent: WarClan? = null,
    @Serializable(with = ZonedDateTimeSerializer::class) val preparationStartTime: ZonedDateTime? = null,
    @Serializable(with = ZonedDateTimeSerializer::class) val startTime: ZonedDateTime? = null,
    val state: State = State.CLAN_NOT_FOUND,
    val teamSize: Int = 0,
    @Serializable(with = ZonedDateTimeSerializer::class) val warStartTime: ZonedDateTime? = null,
)
