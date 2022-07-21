package com.redes.contagiados.ui.playgame.workteamlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.redes.contagiados.R

class GroupListAdapter(
    private val playerList: List<String>,
    val viewLifecycleOwner: LifecycleOwner
): RecyclerView.Adapter<GroupListViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return GroupListViewHolder(layoutInflater.inflate(R.layout.item_group_player_list, parent, false))
    }

    override fun onBindViewHolder(holder: GroupListViewHolder, position: Int) {
        val item = playerList[position]

        holder.crearItem(item, viewLifecycleOwner)
    }

    override fun getItemCount(): Int {
        return playerList.size
    }

}