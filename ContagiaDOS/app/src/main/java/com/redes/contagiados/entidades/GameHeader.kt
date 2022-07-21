package com.redes.contagiados.entidades

import java.io.Serializable


data class GameHeader(
    var gameId: String,
    var name: String,
) : Serializable
