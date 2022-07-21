package com.redes.contagiados.entidades

import java.io.Serializable

data class Player(
    var name: String,
    var psycho: Boolean?,
) : Serializable