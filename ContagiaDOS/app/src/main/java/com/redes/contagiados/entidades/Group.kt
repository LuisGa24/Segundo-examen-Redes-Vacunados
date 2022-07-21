package com.redes.contagiados.entidades

import java.io.Serializable

data class Group(
    var group: MutableList<String> = mutableListOf(),
) : Serializable