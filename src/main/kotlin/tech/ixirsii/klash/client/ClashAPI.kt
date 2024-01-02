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
import arrow.core.raise.either
import arrow.core.right
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import reactor.core.publisher.Mono
import tech.ixirsii.klash.client.internal.APICookieJar
import tech.ixirsii.klash.client.internal.MEDIA_TYPE
import tech.ixirsii.klash.client.internal.TokenManager
import tech.ixirsii.klash.error.ClashAPIError
import tech.ixirsii.klash.error.ClashTokenError
import tech.ixirsii.klash.logging.Logging
import tech.ixirsii.klash.logging.LoggingImpl
import tech.ixirsii.klash.types.TokenResponse
import tech.ixirsii.klash.types.capital.CapitalRaidSeason
import tech.ixirsii.klash.types.clan.Clan
import tech.ixirsii.klash.types.clan.ClanMember
import tech.ixirsii.klash.types.cwl.ClanWarLeagueGroup
import tech.ixirsii.klash.types.error.ClientError
import tech.ixirsii.klash.types.league.BuilderBaseLeague
import tech.ixirsii.klash.types.league.CapitalLeague
import tech.ixirsii.klash.types.league.League
import tech.ixirsii.klash.types.league.LeagueSeason
import tech.ixirsii.klash.types.league.PlayerRanking
import tech.ixirsii.klash.types.league.WarLeague
import tech.ixirsii.klash.types.location.ClanRanking
import tech.ixirsii.klash.types.pagination.Page
import tech.ixirsii.klash.types.player.Player
import tech.ixirsii.klash.types.war.War
import tech.ixirsii.klash.types.war.WarLogEntry

/**
 * Clash of Clans API client.
 *
 * @constructor Create a new Clash of Clans API client.
 * @param token Clash of Clans API token for authenticating requests.
 * @param client HTTP client for making requests.
 * @param json JSON (de)serializer.
 * @author Ixirsii <ixirsii@ixirsii.tech>
 */
class ClashAPI(
    private val token: String,
    private val client: OkHttpClient = CLIENT,
    private val json: Json = JSON,
) : Logging by LoggingImpl<ClashAPI>() {

    /* ********************************************************************************************************** *
     *                                                  Clan APIs                                                 *
     * ********************************************************************************************************** */

    /**
     * Get clan's capital raid seasons.
     *
     * @param clanTag The clan tag (without leading '#').
     * @param limit Limit the number of items returned in the response.
     * @param after Return only items that occur after this marker.
     * @param before Return only items that occur before this marker.
     * @return The clan's capital raid seasons.
     */
    fun capitalRaidSeasons(
        clanTag: String,
        limit: Int? = null,
        after: String? = null,
        before: String? = null,
    ): Mono<Either<ClashAPIError, Page<CapitalRaidSeason>>> {
        log.trace("Getting capital raid seasons for clan {}", clanTag)

        val queryParameters: String = paginationQueryParameters(limit, after, before)

        return get("/clans/%23$clanTag/capitalraidseasons$queryParameters")
    }

    /**
     * Get clan information.
     *
     * @param clanTag The clan tag (without leading '#').
     * @return Clan information.
     */
    fun clan(clanTag: String): Mono<Either<ClashAPIError, Clan>> {
        log.trace("Getting clan {}", clanTag)

        return get("/clans/%23$clanTag")
    }

    /**
     * Search clans.
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
     * @return A list of clans that match the search criteria.
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

        return get("/clans$queryParams")
    }

    /**
     * Get information about clan's current clan war.
     *
     * @param clanTag The clan tag (without leading '#').
     * @return Information about clan's current clan war.
     */
    fun currentWar(clanTag: String): Mono<Either<ClashAPIError, War>> {
        log.trace("Getting current war for clan {}", clanTag)

        return get("/clans/%23$clanTag/currentwar")
    }

    /**
     * Get information about clan's current clan war league group.
     *
     * @param clanTag The clan tag (without leading '#').
     * @return Information about clan's current clan war league group.
     */
    fun leagueGroup(clanTag: String): Mono<Either<ClashAPIError, ClanWarLeagueGroup>> {
        log.trace("Getting league group for clan {}", clanTag)

        return get("/clans/%23$clanTag/currentwar/leaguegroup")
    }

    /**
     * Get information about individual clan war league war.
     *
     * @param warTag Clan war tag (without leading '#').
     * @return Information about individual clan war league war.
     */
    fun leagueWar(warTag: String): Mono<Either<ClashAPIError, War>> {
        log.trace("Getting league war {}", warTag)

        return get("/clanwarleagues/wars/%23$warTag")
    }

    /**
     * List clan members.
     *
     * @param clanTag The clan tag (without leading '#').
     * @param limit Limit the number of items returned in the response.
     * @param after Return only items that occur after this marker.
     * @param before Return only items that occur before this marker.
     * @return A list of members in the clan.
     */
    fun members(
        clanTag: String,
        limit: Int? = null,
        after: String? = null,
        before: String? = null,
    ): Mono<Either<ClashAPIError, Page<ClanMember>>> {
        log.trace("Getting members for clan {}", clanTag)

        val queryParameters: String = paginationQueryParameters(limit, after, before)

        return get("/clans/%23$clanTag/members$queryParameters")
    }

    /**
     * Get clan's clan war log.
     *
     * @param clanTag The clan tag (without leading '#').
     * @param limit Limit the number of items returned in the response.
     * @param after Return only items that occur after this marker.
     * @param before Return only items that occur before this marker.
     * @return Clan's clan war log.
     */
    fun warLog(
        clanTag: String,
        limit: Int? = null,
        after: String? = null,
        before: String? = null,
    ): Mono<Either<ClashAPIError, Page<WarLogEntry>>> {
        log.trace("Getting war log for clan {}", clanTag)

        val queryParameters: String = paginationQueryParameters(limit, after, before)

        return get("/clans/%23$clanTag/warlog$queryParameters")
    }

    /* ********************************************************************************************************** *
     *                                                 Player APIs                                                *
     * ********************************************************************************************************** */

    /**
     * Get player information.
     *
     * @param playerTag The player tag (without leading '#').
     * @return Player information.
     */
    fun player(playerTag: String): Mono<Either<ClashAPIError, Player>> {
        log.trace("Getting player {}", playerTag)

        return get("/players/%23$playerTag")
    }

    /**
     * Verify player API token.
     *
     * Verify player API token that can be found from the game settings. This API call can be used to check that players
     * own the game accounts they claim to own as they need to provide the one-time use API token that exists inside
     * the game.
     *
     * @param playerTag The player tag (without leading '#').
     * @param token API token.
     * @return Whether the token is valid.
     */
    fun isPlayerVerified(playerTag: String, token: String): Mono<Either<ClashAPIError, Boolean>> {
        log.trace("Verifying player {}", playerTag)

        return post<TokenResponse>(
            "/players/%23$playerTag/verifytoken",
            "{\"token\":\"$token\"}".toRequestBody(MEDIA_TYPE)
        )
            .map { either: Either<ClashAPIError, TokenResponse> ->
                either.map { tokenResponse: TokenResponse -> tokenResponse.status == "ok" }
            }
    }

    /* ********************************************************************************************************** *
     *                                                 League APIs                                                *
     * ********************************************************************************************************** */

    /**
     * Get builder base league information.
     *
     * @param leagueID League ID.
     * @return Builder base league information.
     */
    fun builderBaseLeague(leagueID: String): Mono<Either<ClashAPIError, BuilderBaseLeague>> {
        log.trace("Getting builder base league {}", leagueID)

        return get("/builderbaseleagues/$leagueID")
    }

    /**
     * List capital leagues.
     *
     * @param limit Limit the number of items returned in the response.
     * @param after Return only items that occur after this marker.
     * @param before Return only items that occur before this marker.
     * @return A list of builder base leagues.
     */
    fun builderBaseLeagues(
        limit: Int? = null,
        after: String? = null,
        before: String? = null,
    ): Mono<Either<ClashAPIError, Page<BuilderBaseLeague>>> {
        log.trace("Getting builder base leagues")

        val queryParameters: String = paginationQueryParameters(limit, after, before)
        return get("/builderbaseleagues$queryParameters")
    }

    /**
     * Get capital league information.
     *
     * @param leagueID League ID.
     * @return Capital league information.
     */
    fun capitalLeague(leagueID: String): Mono<Either<ClashAPIError, CapitalLeague>> {
        log.trace("Getting capital league {}", leagueID)

        return get("/capitalleagues/$leagueID")
    }

    /**
     * List capital leagues.
     *
     * @param limit Limit the number of items returned in the response.
     * @param after Return only items that occur after this marker.
     * @param before Return only items that occur before this marker.
     * @return A list of capital leagues.
     */
    fun capitalLeagues(
        limit: Int? = null,
        after: String? = null,
        before: String? = null,
    ): Mono<Either<ClashAPIError, Page<CapitalLeague>>> {
        log.trace("Getting capital leagues")

        val queryParameters: String = paginationQueryParameters(limit, after, before)

        return get("/capitalleagues$queryParameters")
    }

    /**
     * Get league information.
     *
     * @param leagueID League ID.
     * @return League information.
     */
    fun league(leagueID: String): Mono<Either<ClashAPIError, League>> {
        log.trace("Getting league {}", leagueID)

        return get("/leagues/$leagueID")
    }

    /**
     * List leagues.
     *
     * @param limit Limit the number of items returned in the response.
     * @param after Return only items that occur after this marker.
     * @param before Return only items that occur before this marker.
     * @return A list of leagues.
     */
    fun leagues(
        limit: Int? = null,
        after: String? = null,
        before: String? = null,
    ): Mono<Either<ClashAPIError, Page<League>>> {
        log.trace("Getting leagues")

        val queryParameters: String = paginationQueryParameters(limit, after, before)

        return get("/leagues$queryParameters")
    }

    /**
     * Get league season rankings.
     *
     * @param leagueID League ID.
     * @param seasonID Season ID.
     * @param limit Limit the number of items returned in the response.
     * @param after Return only items that occur after this marker.
     * @param before Return only items that occur before this marker.
     * @return League season rankings.
     */
    fun leagueSeason(
        leagueID: String,
        seasonID: String,
        limit: Int? = null,
        after: String? = null,
        before: String? = null,
    ): Mono<Either<ClashAPIError, Page<PlayerRanking>>> {
        log.trace("Getting league season")

        val queryParameters: String = paginationQueryParameters(limit, after, before)

        return get("/leagues/$leagueID/seasons/$seasonID$queryParameters")
    }

    /**
     * Get league seasons.
     *
     * @param leagueID League ID.
     * @param limit Limit the number of items returned in the response.
     * @param after Return only items that occur after this marker.
     * @param before Return only items that occur before this marker.
     * @return League seasons.
     */
    fun leagueSeasons(
        leagueID: String,
        limit: Int? = null,
        after: String? = null,
        before: String? = null,
    ): Mono<Either<ClashAPIError, Page<LeagueSeason>>> {
        log.trace("Getting seasons for league {}", leagueID)

        val queryParameters: String = paginationQueryParameters(limit, after, before)

        return get("/leagues/$leagueID/seasons$queryParameters")
    }

    /**
     * Get war league information.
     *
     * @param leagueID League ID.
     * @return War league information.
     */
    fun warLeague(leagueID: String): Mono<Either<ClashAPIError, WarLeague>> {
        log.trace("Getting war league {}", leagueID)

        return get("/warleagues/$leagueID")
    }

    /**
     * List war leagues.
     *
     * @param limit Limit the number of items returned in the response.
     * @param after Return only items that occur after this marker.
     * @param before Return only items that occur before this marker.
     * @return A list of war leagues.
     */
    fun warLeagues(
        limit: Int? = null,
        after: String? = null,
        before: String? = null,
    ): Mono<Either<ClashAPIError, Page<WarLeague>>> {
        log.trace("Getting war leagues")

        val queryParameters: String = paginationQueryParameters(limit, after, before)

        return get("/warleagues$queryParameters")
    }

    /* ********************************************************************************************************** *
     *                                               Location APIs                                                *
     * ********************************************************************************************************** */

    /**
     * Get clan rankings for a specific location.
     *
     * @param locationID Location ID.
     * @param limit Limit the number of items returned in the response.
     * @param after Return only items after this marker.
     * @param before Return only items before this marker.
     * @return Clan rankings for a specific location.
     */
    fun clanRankings(
        locationID: Int,
        limit: Int? = null,
        after: String? = null,
        before: String? = null,
    ): Mono<Either<ClashAPIError, Page<ClanRanking>>> {
        log.trace("Getting clan rankings for location {}", locationID)

        val queryParameters: String = paginationQueryParameters(limit, after, before)

        return get("/locations/$locationID/rankings/clans$queryParameters")
    }

    /**
     * Get player rankings for a specific location.
     *
     * @param locationID Location ID.
     * @param limit Limit the number of items returned in the response.
     * @param after Return only items after this marker.
     * @param before Return only items before this marker.
     * @return Player rankings for a specific location.
     */
    fun playerRankings(
        locationID: Int,
        limit: Int? = null,
        after: String? = null,
        before: String? = null,
    ): Mono<Either<ClashAPIError, Page<tech.ixirsii.klash.types.location.PlayerRanking>>> {
        log.trace("Getting player rankings for location {}", locationID)

        val queryParameters: String = paginationQueryParameters(limit, after, before)

        return get("/locations/$locationID/rankings/players$queryParameters")
    }

    /* ********************************************************************************************************** *
     *                                          Private utility functions                                         *
     * ********************************************************************************************************** */

    /**
     * Create a base request builder with the authorization header set.
     *
     * This returns a builder instead of the built request so that [post] can set the content type and request body.
     *
     * @param suffix The specific endpoint to append to the base URL.
     * @return [Request.Builder] with the authorization header and URL set.
     */
    private fun baseRequest(suffix: String): Request.Builder =
        Request.Builder().header("Authorization", "Bearer $token").url(URL + API_VERSION + suffix)

    /**
     * Call a request.
     *
     * @param T Type to deserialize the response body to.
     * @param call HTTP call.
     * @return [Either.Right] with the response if successful, [Either.Left] with an error otherwise.
     */
    private inline fun <reified T> callRequest(call: Call): Mono<Either<ClashAPIError, T>> = Mono.fromCallable {
        Either.catch {
            call.execute()
        }.mapLeft { throwable: Throwable ->
            log.error("Caught exception making request", throwable)

            ClashAPIError.RequestError(throwable.message ?: "Caught exception making request")
        }.flatMap { response: Response ->
            response.use { useResponse: Response ->
                checkResponse(useResponse).flatMap { deserialize<T>(it.body.string()) }
            }
        }
    }

    /**
     * Check the response for errors.
     *
     * @param response HTTP response.
     * @return [Either.Right] with the response if successful, [Either.Left] with an error otherwise.
     */
    private fun checkResponse(response: Response): Either<ClashAPIError, Response> {
        log.trace("Checking response {}", response)

        return if (response.isSuccessful) {
            log.debug("Received successful response: {}", response)

            response.right()
        } else {
            log.warn("Received error response: {} \"{}\"", response.code, response.message)
            val error: Either<ClashAPIError, ClientError> = deserialize(response.body.string())

            when (response.code) {
                400 -> error.flatMap { ClashAPIError.ClientError.BadRequest(response.message, it).left() }
                403 -> error.flatMap { ClashAPIError.ClientError.Forbidden(response.message, it).left() }
                404 -> error.flatMap { ClashAPIError.ClientError.NotFound(response.message, it).left() }
                429 -> error.flatMap { ClashAPIError.ClientError.TooManyRequests(response.message, it).left() }
                500 -> error.flatMap {
                    ClashAPIError.ClientError.InternalServerError(response.message, it).left()
                }

                503 -> error.flatMap {
                    ClashAPIError.ClientError.ServiceUnavailable(response.message, it).left()
                }

                else -> error.flatMap { ClashAPIError.ClientError.Unknown(response.message, it).left() }
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
    private inline fun <reified T> deserialize(body: String): Either<ClashAPIError.DeserializationError, T> =
        Either.catch {
            json.decodeFromString<T>(body)
        }.mapLeft {
            log.error("Caught exception deserializing response", it)
            ClashAPIError.DeserializationError(it.message ?: "Caught exception deserializing response")
        }

    /**
     * Make a GET request.
     *
     * @param T Type to deserialize the response body to.
     * @param endpoint The specific endpoint to append to the base URL.
     * @return [Either.Right] with the response if successful, [Either.Left] with an error otherwise.
     */
    private inline fun <reified T> get(endpoint: String): Mono<Either<ClashAPIError, T>> {
        log.trace("Making GET request to \"{}\"", endpoint)

        val call: Call = client.newCall(baseRequest(endpoint).build())

        log.debug("Making GET request to \"{}\"", call.request().url)

        return callRequest(call)
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
     * @param T Type to deserialize the response body to.
     * @param endpoint The specific endpoint to append to the base URL.
     * @param body Request body.
     * @return [Either.Right] with the response if successful, [Either.Left] with an error otherwise.
     */
    private inline fun <reified T> post(endpoint: String, body: RequestBody): Mono<Either<ClashAPIError, T>> {
        log.trace("Making POST request to \"{}\"", endpoint)

        val call: Call = client.newCall(baseRequest(endpoint).post(body).build())

        log.debug("Making POST request to \"{}\"", call.request().url)

        return callRequest(call)
    }

    /**
     * Get a substring for a single query parameter.
     *
     * @param T Type of the query parameter value.
     * @param parameter Query parameter name.
     * @param value Query parameter value.
     * @param hasAmpersand Whether the query parameter string needs an ampersand prefix.
     * @return Query parameter substring.
     */
    private fun <T> queryParameter(parameter: String, value: T?, hasAmpersand: Boolean): String {
        return if (value != null) {
            "${if (hasAmpersand) "&" else ""}$parameter=$value"
        } else {
            ""
        }
    }

    companion object {
        /**
         * Clash of Clans API version.
         */
        private const val API_VERSION = "v1"

        /**
         * Base URL for the Clash of Clans API.
         */
        private const val URL = "https://api.clashofclans.com/"

        /**
         * Default HTTP client.
         */
        private val CLIENT: OkHttpClient = OkHttpClient.Builder().cookieJar(APICookieJar()).build()

        /**
         * Default JSON (de)serializer.
         */
        private val JSON: Json = Json {
            coerceInputValues = true
            encodeDefaults = true
            ignoreUnknownKeys = true
            prettyPrint = true
        }

        /**
         * Clash of Clans API client.
         *
         * @param email Clash of Clans developer portal email.
         * @param password Clash of Clans developer portal password.
         * @param client HTTP client for making requests.
         * @param json JSON (de)serializer.
         * @return Clash of Clans API client.
         */
        operator fun invoke(
            email: String,
            password: String,
            client: OkHttpClient = CLIENT,
            json: Json = JSON,
        ): Either<ClashTokenError, ClashAPI> = either {
            val token: String = TokenManager(email, password, client, json).token.bind()

            return ClashAPI(token, client, json).right()
        }
    }
}
