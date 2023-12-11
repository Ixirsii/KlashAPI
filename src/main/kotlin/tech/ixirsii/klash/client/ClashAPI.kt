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
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import reactor.core.publisher.Mono
import tech.ixirsii.klash.error.ClashAPIError
import tech.ixirsii.klash.logging.Logging
import tech.ixirsii.klash.logging.LoggingImpl
import tech.ixirsii.klash.types.cwl.ClanWarLeagueGroup
import tech.ixirsii.klash.types.war.War
import java.io.IOException

/**
 * Clash of Clans API client.
 *
 * @author Ixirsii <ixirsii@ixirsii.tech>
 */
class ClashAPI(private val token: String) : Logging by LoggingImpl<ClashAPI>() {
    /**
     * HTTP client for making requests.
     */
    private val http: OkHttpClient = OkHttpClient()

    /**
     * JSON serializer/deserializer.
     */
    private val json = Json {
        coerceInputValues = true
        prettyPrint = true
    }

    /* *********************************************** Clan APIs ************************************************ */

    /**
     * Get the war league group for a clan.
     *
     * @param tag The clan tag (without leading '#').
     * @return The war league group for the clan.
     */
    fun leagueGroup(tag: String): Mono<Either<ClashAPIError, ClanWarLeagueGroup>> {
        log.trace("Getting league group for clan {}", tag)

        val response: Mono<Either<ClashAPIError, Response>> = get("/clans/%23$tag/currentwar/leaguegroup")

        return response.map { either -> either.flatMap { deserialize<ClanWarLeagueGroup>(it.body?.string() ?: "") } }
    }

    /**
     * Get a war league war.
     *
     * @param tag Clan war tag (without leading '#').
     * @return The war league war.
     */
    fun leagueWar(tag: String): Mono<Either<ClashAPIError, War>> {
        log.trace("Getting league war {}", tag)

        val response: Mono<Either<ClashAPIError, Response>> = get("/clanwarleagues/wars/%23$tag")

        return response.map { either -> either.flatMap { deserialize<War>(it.body?.string() ?: "") } }
    }

    /* *************************************** Private utility functions **************************************** */

    /**
     * Create a base request builder with the authorization header set.
     *
     * This returns a builder instead of the built request so that [post] can set the content type and request body.
     *
     * @param suffix The specific endpoint to append to the base URL.
     * @return [Request.Builder] with the authorization header set.
     */
    private fun baseRequest(suffix: String): Request.Builder {
        return Request.Builder()
            .header("Authorization", "Bearer $token")
            .url(URL + API_VERSION + suffix)
    }

    /**
     * Check the response for errors.
     *
     * @param response HTTP response.
     * @return [Either.Right] with the response if successful, [Either.Left] with an error otherwise.
     */
    private fun checkResponse(response: Mono<Response>): Mono<Either<ClashAPIError, Response>> = response.map {
        log.trace("Checking response {}", it)

        if (it.isSuccessful) {
            log.debug("Received successful response: {}", it)

            it.right()
        } else {
            log.warn("Received error response: {} {}", it.code, it.message)

            when (it.code) {
                400 -> ClashAPIError.BadRequest(it.message).left()
                403 -> ClashAPIError.Forbidden(it.message).left()
                404 -> ClashAPIError.NotFound(it.message).left()
                429 -> ClashAPIError.TooManyRequests(it.message).left()
                500 -> ClashAPIError.InternalServerError(it.message).left()
                503 -> ClashAPIError.ServiceUnavailable(it.message).left()
                else -> ClashAPIError.Unknown(it.message).left()
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
    @Throws(IOException::class)
    private inline fun <reified T> deserialize(body: String = ""): Either<ClashAPIError.DeserializationError, T> =
        Either.catch {
            json.decodeFromString<T>(body)
        }.mapLeft {
            log.error("Caught exception deserializing response", it)
            ClashAPIError.DeserializationError(it.message ?: "Caught exception deserializing response")
        }

    /**
     * Make a GET request.
     *
     * @param endpoint The specific endpoint to append to the base URL.
     * @return [Either.Right] with the response if successful, [Either.Left] with an error otherwise.
     */
    private fun get(endpoint: String): Mono<Either<ClashAPIError, Response>> {
        log.trace("Making GET request to {}", endpoint)

        val call: Call = http.newCall(baseRequest(endpoint).build())

        log.debug("Making GET request to {}", call.request().url)

        val response: Mono<Response> = Mono.fromCallable { call.execute() }

        return checkResponse(response)
    }

    /**
     * Get a query parameter string containing pagination parameters.
     *
     * @param limit Maximum number of items to return.
     * @param after Return only items after this marker.
     * @param before Return only items before this marker.
     * @return Query parameter string.
     */
    private fun paginationQueryParameters(limit: Int?, after: String?, before: String?): String {
        var queryParams = "?"

        queryParams += queryParameter("limit", limit, false)
        queryParams += queryParameter("after", after, queryParams.length > 1)
        queryParams += queryParameter("before", before, queryParams.length > 1)

        log.debug("Pagination query parameters: {}", queryParams)

        return queryParams
    }

    /**
     * Make a POST request.
     *
     * @param endpoint The specific endpoint to append to the base URL.
     * @param body Request body.
     * @return [Either.Right] with the response if successful, [Either.Left] with an error otherwise.
     */
    private fun post(endpoint: String, body: RequestBody): Mono<Either<ClashAPIError, Response>> {
        log.trace("Making POST request to {}", endpoint)

        val call: Call = http.newCall(baseRequest(endpoint).post(body).build())

        log.debug("Making POST request to {}", call.request().url)

        val response: Mono<Response> = Mono.fromCallable { call.execute() }

        return checkResponse(response)
    }

    /**
     * Get a substring for a single query parameter.
     *
     * @param parameter Query parameter name.
     * @param value Query parameter value.
     * @param hasAmpersand Whether the query parameter string needs an ampersand prefix.
     */
    private fun <T> queryParameter(parameter: String, value: T?, hasAmpersand: Boolean): String {
        return if (value != null) {
            "${if (hasAmpersand) "&" else ""}$parameter=$value"
        } else {
            ""
        }
    }

    /**
     * Create a request body for a token verification request.
     *
     * @param token Player token to verify.
     * @return Request body.
     */
    private fun tokenVerificationBody(token: String): RequestBody {
        val contentType: MediaType? = "application/json; charset=utf-8".toMediaTypeOrNull()

        return "{\"token\":\"$token\"}".toRequestBody(contentType)
    }

    companion object {
        /**
         * Base URL for the Clash of Clans API.
         */
        private const val URL = "https://api.clashofclans.com/"

        /**
         * Clash of Clans API version.
         */
        private const val API_VERSION = "v1"
    }
}
