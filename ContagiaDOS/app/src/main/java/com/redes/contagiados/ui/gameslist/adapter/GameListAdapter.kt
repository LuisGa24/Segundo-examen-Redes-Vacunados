package com.redes.contagiados.ui.gameslist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.redes.contagiados.R
import com.redes.contagiados.entidades.GameHeader
import com.redes.contagiados.entidades.NewGameRequest

class GameListAdapter(
    private val gameList: List<GameHeader>
): RecyclerView.Adapter<GameListViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return GameListViewHolder(layoutInflater.inflate(R.layout.item_game_list, parent, false))
    }

    override fun onBindViewHolder(holder: GameListViewHolder, position: Int) {
        val item = gameList[position]

        holder.crearItem(item)
    }

    override fun getItemCount(): Int {
        return gameList.size
    }

}