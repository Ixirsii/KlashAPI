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

package tech.ixirsii.klash.client

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import tech.ixirsii.klash.error.ClashTokenError
import tech.ixirsii.klash.logging.Logging
import tech.ixirsii.klash.logging.LoggingImpl
import java.net.URI

/**
 * Get or create a token for the Clash of Clans API.
 *
 * @property client HTTP client.
 * @property email Clash of Clans developer portal email.
 * @property password Clash of Clans developer portal password.
 * @author Ixirsii <ixirsii@ixirsii.tech>
 */
internal class TokenManager(
    private val email: String,
    private val password: String,
    private val client: OkHttpClient,
    private val json: Json,
) : Logging by LoggingImpl<TokenManager>() {

    /**
     * Clash of Clans API token for the current IP address.
     */
    val token: Either<ClashTokenError, String>
        get() {
            log.trace("Getting token")

            val ip: String = getIP()

            return login(email, password).flatMap {
                getKeys()
            }.map { keys: List<Key> ->
                keys.firstOrNull { it.cidrRanges.contains(ip) }
            }.flatMap {
                it?.right() ?: createKey(ip)
            }.map {
                it.key
            }
        }

    /* ********************************************************************************************************** *
     *                                          Private utility functions                                         *
     * ********************************************************************************************************** */

    /**
     * Create a new API key for the current IP address.
     *
     * @param ip Current IP address.
     * @return Clash of Clans API key.
     */
    private fun createKey(ip: String): Either<ClashTokenError, Key> {
        val body: RequestBody = json.encodeToString(CreateAPIKeyBody(cidrRanges = listOf(ip))).toRequestBody(MEDIA_TYPE)
        val request: Request = Request.Builder().url("$DEVELOPER_URL/apikey/create").post(body).build()
        val call: Call = client.newCall(request)

        log.debug("Creating new key")

        return call.execute().use { response: Response ->
            if (response.isSuccessful) {
                deserialize<CreateAPIKeyResponse>(response.body.string()).map { it.key }
            } else {
                log.error("Failed to create new key: {}", response.body.string())
                ClashTokenError.CreateAPIKeyError(response.message).left()
            }
        }
    }

    /**
     * Deserialize the response body.
     *
     * @param body HTTP response body.
     * @param T Type to deserialize the response body to.
     * @return [Either.Right] with the deserialized response body if successful, [Either.Left] with an error otherwise.
     */
    private inline fun <reified T> deserialize(body: String): Either<ClashTokenError, T> =
        Either.catch {
            json.decodeFromString<T>(body)
        }.mapLeft {
            log.error("Caught exception deserializing response", it)
            ClashTokenError.DeserializationError(it.message ?: "Caught exception deserializing response")
        }

    /**
     * Get the current IP address.
     *
     * @return Current IP address.
     */
    private fun getIP(): String {
        val ip: String = URI("https://checkip.amazonaws.com").toURL().readText().trim()

        log.debug("Current IP address: {}", ip)

        return ip
    }

    /**
     * Get the list of API keys from the Clash of Clans developer portal.
     *
     * @return List of API keys.
     */
    private fun getKeys(): Either<ClashTokenError, List<Key>> {
        val request: Request = Request.Builder().url("$DEVELOPER_URL/apikey/list").post("".toRequestBody(null)).build()
        val call: Call = client.newCall(request)

        log.debug("Fetching keys")

        return call.execute().use { response: Response ->
            if (response.isSuccessful) {
                deserialize<KeyList>(response.body.string()).map { it.keys }
            } else {
                ClashTokenError.KeyRetrievalError(response.message).left()
            }
        }
    }

    /**
     * Log in to the Clash of Clans developer portal.
     *
     * @param email Clash of Clans developer portal email.
     * @param password Clash of Clans developer portal password.
     * @return Unit if successful, error otherwise.
     */
    private fun login(email: String, password: String): Either<ClashTokenError, Unit> {
        log.trace("Logging in with email \"{}\"", email)

        val request: Request = Request.Builder().url("$DEVELOPER_URL/login")
            .post("{\"email\":\"$email\",\"password\":\"$password\"}".toRequestBody(MEDIA_TYPE)).build()
        val call: Call = client.newCall(request)

        log.debug("Logging in to developer portal")

        return call.execute().use {
            if (it.code == 403) {
                ClashTokenError.LoginError(it.message).left()
            } else {
                Unit.right()
            }
        }
    }

    companion object {
        /**
         * Clash of Clans developer portal URL.
         */
        private const val DEVELOPER_URL = "https://developer.clashofclans.com/api"
    }
}
