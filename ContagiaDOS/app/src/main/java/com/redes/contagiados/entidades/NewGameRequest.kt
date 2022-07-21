package com.redes.contagiados.entidades

import java.io.Serializable

data class NewGameRequest(
    var name: String,
    var password: String,
) : Serializable
