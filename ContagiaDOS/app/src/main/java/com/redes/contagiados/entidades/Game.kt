package com.redes.contagiados.entidades

import java.io.Serializable


data class Game(
    var gameId: String = "",
    var name: String = "",
    var owner: String = "",
    var password: String = "",
    var players: List<String> = emptyList(),
    var psychos: List<String> = emptyList(),
    var psychoWin: List<Boolean> = emptyList(),
    var status: String = "",
    var rounds: List<Rounds> = emptyList(),

    ) : Serializable

