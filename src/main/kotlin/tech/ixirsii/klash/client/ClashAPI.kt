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
import tech.ixirsii.klash.types.clan.Clan
import tech.ixirsii.klash.types.cwl.ClanWarLeagueGroup
import tech.ixirsii.klash.types.error.ClientError
import tech.ixirsii.klash.types.pagination.Page
import tech.ixirsii.klash.types.war.War
import tech.ixirsii.klash.types.war.WarLogEntry
import java.io.IOException

/**
 * Clash of Clans API client.
 *
 * @property token Clash of Clans API token for authenticating requests.
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
     * List clans.
     *
     * @param name Search clans by name.
     * @param warFrequency Filter by war frequency.
     * @param locationID Filter by location.
     * @param minMembers Filter by minimum number of clan members.
     * @param maxMembers Filter by maximum number of clan members.
     * @param minClanPoints Filter by minimum amount of clan points.
     * @param minClanLevel Filter by minimum clan level.
     * @param limit Limit the number of items returned in the response.
     * @param after Return only items that occur after this marker.
     * @param before Return only items that occur before this marker.
     * @param labelIDs Filter by clan labels.
     */
    fun clans(
        name: String? = null,
        warFrequency: String? = null,
        locationID: Int? = null,
        minMembers: Int? = null,
        maxMembers: Int? = null,
        minClanPoints: Int? = null,
        minClanLevel: Int? = null,
        limit: Int? = null,
        after: String? = null,
        before: String? = null,
        labelIDs: String? = null,
    ): Mono<Either<ClashAPIError, Page<Clan>>> {
        log.trace("Getting clans")

        var queryParams = "?"

        queryParams += queryParameter("name", name, false)
        queryParams += queryParameter("warFrequency", warFrequency, queryParams.length > 1)
        queryParams += queryParameter("locationID", locationID, queryParams.length > 1)
        queryParams += queryParameter("minMembers", minMembers, queryParams.length > 1)
        queryParams += queryParameter("maxMembers", maxMembers, queryParams.length > 1)
        queryParams += queryParameter("minClanPoints", minClanPoints, queryParams.length > 1)
        queryParams += queryParameter("minClanLevel", minClanLevel, queryParams.length > 1)
        queryParams += queryParameter("limit", limit, queryParams.length > 1)
        queryParams += queryParameter("after", after, queryParams.length > 1)
        queryParams += queryParameter("before", before, queryParams.length > 1)
        queryParams += queryParameter("labelIds", labelIDs, queryParams.length > 1)

        val response: Mono<Either<ClashAPIError, Response>> = get("/clans$queryParams")

        return response.map { either -> either.flatMap { deserialize<Page<Clan>>(it.body?.string() ?: "") } }
    }

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

    fun warLog(
        tag: String,
        limit: Int? = null,
        after: String? = null,
        before: String? = null,
    ): Mono<Either<ClashAPIError, Page<WarLogEntry>>> {
        log.trace("Getting war log for clan {}", tag)

        val endpoint = "/clans/%23$tag/warlog${paginationQueryParameters(limit, after, before)}"
        val response: Mono<Either<ClashAPIError, Response>> = get(endpoint)

        return response.map { either -> either.flatMap { deserialize<Page<WarLogEntry>>(it.body?.string() ?: "") } }
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
        return Request.Builder().header("Authorization", "Bearer $token").url(URL + API_VERSION + suffix)
    }

    /**
     * Check the response for errors.
     *
     * @param responseMono HTTP response.
     * @return [Either.Right] with the response if successful, [Either.Left] with an error otherwise.
     */
    private fun checkResponse(responseMono: Mono<Response>): Mono<Either<ClashAPIError, Response>> =
        responseMono.map { response ->
            log.trace("Checking response {}", response)

            if (response.isSuccessful) {
                log.debug("Received successful response: {}", response)

                response.right()
            } else {
                log.warn("Received error response: {} \"{}\"", response.code, response.message)
                val error: Either<ClashAPIError, ClientError> = deserialize(response.body?.string() ?: "")

                when (response.code) {
                    400 -> error.flatMap { ClashAPIError.ClientError.BadRequest(response.message, it).left() }
                    403 -> error.flatMap { ClashAPIError.ClientError.Forbidden(response.message, it).left() }
                    404 -> error.flatMap { ClashAPIError.ClientError.NotFound(response.message, it).left() }
                    429 -> error.flatMap { ClashAPIError.ClientError.TooManyRequests(response.message, it).left() }
                    500 -> error.flatMap { ClashAPIError.ClientError.InternalServerError(response.message, it).left() }
                    503 -> error.flatMap { ClashAPIError.ClientError.ServiceUnavailable(response.message, it).left() }
                    else -> error.flatMap { ClashAPIError.ClientError.Unknown(response.message, it).left() }
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
        log.trace("Making GET request to \"{}\"", endpoint)

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
