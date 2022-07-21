package com.redes.contagiados.ui.playgame.workteamlist

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.redes.contagiados.databinding.ItemGroupPlayerListBinding
import com.redes.contagiados.entidades.Player
import com.redes.contagiados.util.GlobalData


class GroupListViewHolder(var view: View) : RecyclerView.ViewHolder(view) {


    private val binding = ItemGroupPlayerListBinding.bind(view)

    fun crearItem(player: String, viewLifecycleOwner: LifecycleOwner) {

        binding.apply {
            txvPlayerGroupName.text = player

            btnPlayerSelected.visibility = View.GONE
            imvPlayerSelected.visibility = View.GONE

            GlobalData.actualGame?.observe(viewLifecycleOwner) { game ->

                var iAmPsycho = false
                game.psychos?.forEach { psycho ->
                    if (psycho == GlobalData.userName) {
                        iAmPsycho = true

                    }
                }


                imvPsycho.visibility=View.INVISIBLE
                if(iAmPsycho){

                        if(game.psychos.contains(player)){
                            imvPsycho.visibility=View.VISIBLE
                        }

                }


                if (GlobalData.temporalGroup.group.contains(/*Group(*/player/*, null)*/))
                    btnPlayerSelected.isChecked = true
                var iAmLeader = game.rounds.last().leader == GlobalData.userName
                if (game.status == "leader" && iAmLeader) {
                    btnPlayerSelected.visibility = View.VISIBLE
                }



                if (game.status == "rounds" && game.rounds.last().group.contains(
                        Player(
                            player,
                            null
                        )
                    )
                ) {
                    imvPlayerSelected.visibility = View.VISIBLE
                } else {
                    imvPlayerSelected.visibility = View.GONE
                }

                if (game.status == "rounds" && (game.rounds.last().group.contains(
                        Player(
                            player,
                            true
                        )
                    ))
                    || (game.rounds.last().group.contains(
                        Player(
                            player, false
                        )
                    ))
                ) {
                    imvPlayerReady.visibility = View.VISIBLE
                } else {
                    imvPlayerReady.visibility = View.GONE
                }


            }


            btnPlayerSelected.setOnClickListener {
                Log.i(
                    "games2",
                    "fila ${GlobalData.actualGame?.value?.rounds?.size!!.minus(1)}   columna ${
                        (GlobalData.actualGame?.value?.players?.size)!!.minus(5)
                    }  valor${
                        GlobalData.weeklyGroups[GlobalData.actualGame?.value?.rounds?.size!!.minus(1)][(GlobalData.actualGame?.value?.players?.size)!!.minus(
                            5
                        )]
                    }  checked ${GlobalData.temporalGroup.group.contains(/*Group(*/player/*, null)*/)}"
                )

                if ( (!GlobalData.temporalGroup.group.contains(player)) && GlobalData.temporalGroup.group.size >= GlobalData.weeklyGroups[GlobalData.actualGame?.value?.rounds?.size!!.minus(1)][(GlobalData.actualGame?.value?.players?.size)!!.minus(
                        5
                    )]
                ) {

                    Toast.makeText(
                        binding.root.context,
                        "No more players can be selected",
                        Toast.LENGTH_SHORT
                    ).show()
                    btnPlayerSelected.isChecked = false

                } else {
                    if (GlobalData.temporalGroup.group.contains(/*Group(*/player/*, null)*/)) {
                        GlobalData.temporalGroup.group.remove(/*Group(*/player/*, null)*/)
                        btnPlayerSelected.isChecked = false
                    } else {
                        GlobalData.temporalGroup.group.add(/*Group(*/player/*, null)*/)
                    }

                    Log.i("games", GlobalData.temporalGroup.toString())
                }


            }

        }


    }


}