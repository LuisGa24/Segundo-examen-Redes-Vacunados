package com.redes.contagiados.ui.gamelobby.playerlist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.redes.contagiados.R
import com.redes.contagiados.entidades.GameHeader
import com.redes.contagiados.ui.gameslist.adapter.GameListViewHolder

class PlayerListAdapter(
    private val playerList: List<String>
): RecyclerView.Adapter<PlayerListViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PlayerListViewHolder(layoutInflater.inflate(R.layout.item_player_list, parent, false))
    }

    override fun onBindViewHolder(holder: PlayerListViewHolder, position: Int) {
        val item = playerList[position]

        holder.crearItem(item)
    }

    override fun getItemCount(): Int {
        return playerList.size
    }

}