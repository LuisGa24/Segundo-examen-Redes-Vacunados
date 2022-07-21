package com.redes.contagiados.entidades

import java.io.Serializable

data class Rounds(
    var id: Float,
    var group: List<Player>,
    var leader: String,
) : Serializable