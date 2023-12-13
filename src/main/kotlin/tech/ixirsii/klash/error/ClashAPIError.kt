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

package tech.ixirsii.klash.error

import tech.ixirsii.klash.types.error.ClientError

/**
 * Represents an error returned by the Clash of Clans API.
 *
 * @author Ixirsii <ixirsii@ixirsii.tech>
 */
sealed interface ClashAPIError {
    /**
     * The error message.
     */
    val message: String

    /**
     *  The client error.
     */
    val error: ClientError?

    /**
     * 400 Bad Request error.
     */
    data class BadRequest(override val message: String, override val error: ClientError) : ClashAPIError

    /**
     * Deserialization error.
     */
    data class DeserializationError(override val message: String, override val error: ClientError? = null) :
        ClashAPIError

    /**
     * 403 Forbidden error.
     */
    data class Forbidden(override val message: String, override val error: ClientError) : ClashAPIError

    /**
     * 404 Not Found error.
     */
    data class NotFound(override val message: String, override val error: ClientError) : ClashAPIError

    /**
     * 429 Too Many Requests error.
     */
    data class TooManyRequests(override val message: String, override val error: ClientError) : ClashAPIError

    /**
     * 500 Internal Server Error error.
     */
    data class InternalServerError(override val message: String, override val error: ClientError) : ClashAPIError

    /**
     * 503 Service Unavailable error.
     */
    data class ServiceUnavailable(override val message: String, override val error: ClientError) : ClashAPIError

    /**
     * Unknown error.
     */
    data class Unknown(override val message: String, override val error: ClientError) : ClashAPIError
}
