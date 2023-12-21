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
     * Clash of Clans API error (non-successful response).
     *
     * @author Ixirsii <ixirsii@ixirsii.tech>
     */
    sealed interface ClientError : ClashAPIError {
        /**
         *  The client error.
         */
        val error: tech.ixirsii.klash.types.error.ClientError

        /**
         * 400 Bad Request error.
         *
         * @property message The error message.
         * @property error The client error.
         * @author Ixirsii <ixirsii@ixirsii.tech>
         */
        data class BadRequest(
            override val message: String,
            override val error: tech.ixirsii.klash.types.error.ClientError,
        ) : ClientError

        /**
         * 403 Forbidden error.
         *
         * @property message The error message.
         * @property error The client error.
         * @author Ixirsii <ixirsii@ixirsii.tech>
         */
        data class Forbidden(
            override val message: String,
            override val error: tech.ixirsii.klash.types.error.ClientError,
        ) : ClientError

        /**
         * 404 Not Found error.
         *
         * @property message The error message.
         * @property error The client error.
         * @author Ixirsii <ixirsii@ixirsii.tech>
         */
        data class NotFound(
            override val message: String,
            override val error: tech.ixirsii.klash.types.error.ClientError,
        ) : ClientError

        /**
         * 429 Too Many Requests error.
         *
         * @property message The error message.
         * @property error The client error.
         * @author Ixirsii <ixirsii@ixirsii.tech>
         */
        data class TooManyRequests(
            override val message: String,
            override val error: tech.ixirsii.klash.types.error.ClientError,
        ) : ClientError

        /**
         * 500 Internal Server Error error.
         *
         * @property message The error message.
         * @property error The client error.
         * @author Ixirsii <ixirsii@ixirsii.tech>
         */
        data class InternalServerError(
            override val message: String,
            override val error: tech.ixirsii.klash.types.error.ClientError,
        ) :
            ClientError

        /**
         * 503 Service Unavailable error.
         *
         * @property message The error message.
         * @property error The client error.
         * @author Ixirsii <ixirsii@ixirsii.tech>
         */
        data class ServiceUnavailable(
            override val message: String,
            override val error: tech.ixirsii.klash.types.error.ClientError,
        ) :
            ClientError

        /**
         * Unknown error.
         *
         * @property message The error message.
         * @property error The client error.
         * @author Ixirsii <ixirsii@ixirsii.tech>
         */
        data class Unknown(
            override val message: String,
            override val error: tech.ixirsii.klash.types.error.ClientError,
        ) : ClientError
    }

    /**
     * Deserialization error.
     *
     * @property message The error message.
     * @author Ixirsii <ixirsii@ixirsii.tech>
     */
    data class DeserializationError(override val message: String) : ClashAPIError
}
