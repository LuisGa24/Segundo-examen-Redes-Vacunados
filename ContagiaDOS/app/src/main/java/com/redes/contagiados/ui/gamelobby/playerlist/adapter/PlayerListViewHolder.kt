package com.redes.contagiados.ui.gamelobby.playerlist.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.redes.contagiados.databinding.ItemPlayerListBinding

class PlayerListViewHolder(view: View): RecyclerView.ViewHolder(view) {
    private val binding = ItemPlayerListBinding.bind(view)

    fun crearItem(player: String){

        binding.apply {
            txvNickNameLobby.text = player
        }


    }


}