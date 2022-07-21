package com.redes.contagiados.api

import com.redes.contagiados.entidades.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface APIService {

    @GET("game")
    suspend fun getGameList(): Response<List<GameHeader>>

    @Headers("Content-Type: application/json")
    @GET("game")
    suspend fun getGameListFilter(
        @Query("filter")filterBy: String,
        @Query("filterValue")filterValue: String
    ): Response<List<GameHeader>>

    @Headers("Content-Type: application/json")
    @POST("game/create")
    suspend fun createNewGame(
        @Body game: NewGameRequest,
        @Header("name") name: String,
    ): Response<Game>


    @HEAD("game/{gameId}/start")
    suspend fun getGameHeader(
        @Path("gameId") gameId: String,
        @Header("name") name: String,
        @Header("password") password: String
    ): Response<Void>


    @GET("game/{gameId}")
    suspend fun getGame(
        @Path("gameId") gameId: String,
        @Header("name") name: String,
        @Header("password") password: String
    ): Response<Game>

    @Headers("Content-Type: application/json")
    @PUT("game/{gameId}/join")
    suspend fun joinGame(
        @Path("gameId") gameId: String,
        @Header("name") name: String,
        @Header("password") password: String
    ): Response<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("game/{gameId}/go")
    suspend fun goIntoRound(
        @Path("gameId") gameId: String,
        @Body psycho: Psycho,
        @Header("name") name: String,
        @Header("password") password: String
    ): Response<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("game/{gameId}/group")
    suspend fun groupProposal(
        @Path("gameId") gameId: String,
        @Body group: Group,
        @Header("name") name: String,
        @Header("password") password: String
    ): Response<ResponseBody>

}
