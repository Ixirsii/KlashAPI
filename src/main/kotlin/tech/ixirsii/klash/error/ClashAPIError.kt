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
sealed class ClashAPIError(message: String): Exception(message) {
    /**
     * 400 Bad Request error.
     */
    class BadRequest(message: String): ClashAPIError(message)

    /**
     * Deserialization error.
     */
    class DeserializationError(message: String): ClashAPIError(message)

    /**
     * 403 Forbidden error.
     */
    class Forbidden(message: String): ClashAPIError(message)

    /**
     * 404 Not Found error.
     */
    class NotFound(message: String): ClashAPIError(message)

    /**
     * 429 Too Many Requests error.
     */
    class TooManyRequests(message: String): ClashAPIError(message)

    /**
     * 500 Internal Server Error error.
     */
    class InternalServerError(message: String): ClashAPIError(message)

    /**
     * 503 Service Unavailable error.
     */
    class ServiceUnavailable(message: String): ClashAPIError(message)

    /**
     * Unknown error.
     */
    class Unknown(message: String): ClashAPIError(message)
}
