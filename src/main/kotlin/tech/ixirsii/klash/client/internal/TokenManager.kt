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
import java.util.*

/**
 * Get or create a token for the Clash of Clans API.
 *
 * @constructor Create a new token manager.
 * @param email Clash of Clans developer portal email.
 * @param password Clash of Clans developer portal password.
 * @param client HTTP client.
 * @param json JSON serializer.
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

            val description: String = getDescription()
            val ip: String = getIP()
            val name: String = getName()

            return login(email, password).flatMap {
                getKeys()
            }.flatMap { keys: List<Key> ->
                // Search by IP first, then by name. This way if multiple keys have the same name, we don't short
                // circuit before (potentially) finding the key by IP.
                val key: Key? = keys.firstOrNull { it.cidrRanges.contains(ip) } ?: keys.firstOrNull { it.name == name }

                if (key != null && !key.cidrRanges.contains(ip)) {
                    val result: Either<ClashTokenError, Unit> = deleteKey(key.id)

                    if (result.isRight()) {
                        createKey(key.cidrRanges + ip, name, description)
                    } else {
                        result.leftOrNull()!!.left()
                    }
                } else {
                    key.right()
                }
            }.flatMap { key: Key? ->
                key?.right() ?: createKey(listOf(ip), name, description)
            }.map { key: Key ->
                key.key
            }
        }

    /* ********************************************************************************************************** *
     *                                          Private utility functions                                         *
     * ********************************************************************************************************** */

    /**
     * Create a new API key for the current IP address.
     *
     * @param cidrRanges IP addresses to allow access to the API key.
     * @param name Name of the API key.
     * @param description Description of the API key.
     * @return Clash of Clans API key.
     */
    private fun createKey(cidrRanges: List<String>, name: String, description: String): Either<ClashTokenError, Key> {
        val body: RequestBody = json.encodeToString(
            CreateAPIKeyBody(cidrRanges = cidrRanges, description = description, name = name)
        ).toRequestBody(MEDIA_TYPE)
        val request: Request = Request.Builder().url("$DEVELOPER_URL/apikey/create").post(body).build()
        val call: Call = client.newCall(request)

        log.debug("Creating new key \"{}\" with description \"{}\" and ip {}", name, description, cidrRanges)

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
     * Delete an API key.
     *
     * @param id ID of the API key.
     * @return Unit if successful, error otherwise.
     */
    private fun deleteKey(id: UUID): Either<ClashTokenError, Unit> {
        val body: RequestBody = json.encodeToString(DeleteAPIKeyBody(id)).toRequestBody(MEDIA_TYPE)
        val request: Request = Request.Builder().url("$DEVELOPER_URL/apikey/revoke").post(body).build()
        val call: Call = client.newCall(request)

        log.debug("Deleting key {}", id)

        return call.execute().use { response: Response ->
            if (response.isSuccessful) {
                Unit.right()
            } else {
                log.error("Failed to delete key: {}", response.body.string())
                ClashTokenError.DeleteAPIKeyError(response.message).left()
            }
        }
    }

    /**
     * Deserialize the response body.
     *
     * @param T Type to deserialize the response body to.
     * @param body HTTP response body.
     * @return [Either.Right] with the deserialized response body if successful, [Either.Left] with an error otherwise.
     */
    private inline fun <reified T> deserialize(body: String): Either<ClashTokenError, T> =
        Either.catch {
            json.decodeFromString<T>(body)
        }.mapLeft {
            log.error("Caught exception deserializing response", it)
            ClashTokenError.DeserializationError(it.message ?: "Caught exception deserializing response")
        }

    private fun getDescription(): String {
        val description: String = System.getenv("API_DESCRIPTION") ?: "Automatically generated by KlashAPI"

        log.debug("Description: {}", description)

        return description
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

    private fun getName(): String {
        val name: String = System.getenv("API_NAME") ?: "KlashAPI"

        log.debug("Name: {}", name)

        return name
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
            .post("{\"email\":\"$email\",\"password\":\"$password\"}".toRequestBody(MEDIA_TYPE))
            .build()
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
