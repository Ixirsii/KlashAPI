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

package tech.ixirsii.klash.client.internal

import kotlinx.serialization.Serializable
import tech.ixirsii.klash.serialize.UUIDSerializer
import tech.ixirsii.klash.serialize.ZonedDateTimeSerializer
import java.time.ZonedDateTime
import java.util.*

/**
 * A Clash of Clans API key.
 *
 * @property cidrRanges List of IP addresses that can use this key.
 * @property description Description of the key.
 * @property developerId ID of the developer who created the key.
 * @property id ID of the key.
 * @property key The key.
 * @property name Name of the key.
 * @property origins Origins of the key.
 * @property scopes Scopes of the key.
 * @property tier Tier of the key.
 * @property validUntil Timestamp when the key expires.
 * @author Ixirsii <ixirsii@ixirsii.tech>
 */
@Serializable
internal data class Key(
    val cidrRanges: List<String>,
    val description: String,
    @Serializable(with = UUIDSerializer::class) val developerId: UUID,
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    val key: String,
    val name: String,
    val origins: List<String>?,
    val scopes: List<String>,
    val tier: String,
    @Serializable(with = ZonedDateTimeSerializer::class) val validUntil: ZonedDateTime?,
)
