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
import arrow.core.left
import arrow.core.right
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import reactor.core.publisher.Mono
import tech.ixirsii.klash.exception.ClashAPIException
import tech.ixirsii.klash.logging.Logging
import tech.ixirsii.klash.logging.LoggingImpl
import tech.ixirsii.klash.types.clan.ClanWarLeagueGroup
import java.io.IOException

class ClashAPI(private val token: String) : Logging by LoggingImpl<ClashAPI>() {
    private val http: OkHttpClient = OkHttpClient()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
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
    fun leagueGroup(tag: String): Mono<Either<ClashAPIException, ClanWarLeagueGroup>> {
        val response: Mono<Either<ClashAPIException, Response>> = get("/clans/%23$tag/currentwar/leaguegroup")

        return response.map { either -> either.map { deserialize(it) } }
    }

    /* *************************************** Private utility functions **************************************** */

    private fun baseRequest(suffix: String): Request.Builder {
        return Request.Builder()
            .header("Authorization", "Bearer $token")
            .url(URL + API_VERSION + suffix)
    }

    private fun checkResponse(response: Mono<Response>): Mono<Either<ClashAPIException, Response>> = response.map {
        if (it.isSuccessful) {
            it.right()
        } else {
            when (it.code) {
                400 -> ClashAPIException.BadRequest(it.message).left()
                403 -> ClashAPIException.Forbidden(it.message).left()
                404 -> ClashAPIException.NotFound(it.message).left()
                429 -> ClashAPIException.TooManyRequests(it.message).left()
                500 -> ClashAPIException.InternalServerError(it.message).left()
                503 -> ClashAPIException.ServiceUnavailable(it.message).left()
                else -> ClashAPIException.Unknown(it.message).left()
            }
        }
    }

    @Throws(IOException::class)
    private inline fun <reified T> deserialize(res: Response): T {
        return json.decodeFromString(res.body?.string() ?: "")
    }

    private fun get(url: String): Mono<Either<ClashAPIException, Response>> {
        val response: Mono<Response> = Mono.fromCallable { http.newCall(baseRequest(url).build()).execute() }

        return checkResponse(response)
    }

    private fun getPaginationQueryParameters(limit: Int?, after: String?, before: String?): String {
        var queryParams = "?"

        queryParams += getQueryParameter("limit", limit, false)
        queryParams += getQueryParameter("after", after, queryParams.length > 1)
        queryParams += getQueryParameter("before", before, queryParams.length > 1)

        return if (queryParams.length > 1) queryParams else ""
    }

    private fun <T> getQueryParameter(parameter: String, value: T?, hasAmpersand: Boolean): String {
        return if (value != null) {
            "${if (hasAmpersand) "&" else ""}$parameter=$value"
        } else {
            ""
        }
    }

    private fun post(url: String, body: RequestBody): Mono<Either<ClashAPIException, Response>> {
        val response: Mono<Response> = Mono.fromCallable { http.newCall(baseRequest(url).post(body).build()).execute() }

        return checkResponse(response)
    }

    private fun getTokenVerificationBody(token: String): RequestBody {
        val contentType: MediaType? = "application/json; charset=utf-8".toMediaTypeOrNull()

        return "{\"token\":\"$token\"}".toRequestBody(contentType)
    }

    companion object {
        private const val URL = "https://api.clashofclans.com/"
        private const val API_VERSION = "v1"
    }
}
