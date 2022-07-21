package com.redes.contagiados.ui.gameslist.adapter

import android.app.Activity
import android.content.Context
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.redes.contagiados.HomeFragmentDirections
import com.redes.contagiados.R
import com.redes.contagiados.api.APIService
import com.redes.contagiados.api.ConexiónAPI
import com.redes.contagiados.databinding.ItemGameListBinding
import com.redes.contagiados.entidades.Game

import com.redes.contagiados.entidades.GameHeader
import com.redes.contagiados.util.GlobalData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response
import java.net.ConnectException

class GameListViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    private val binding = ItemGameListBinding.bind(view)

    fun crearItem(gameHeader: GameHeader) {

        binding.apply {
            txvGameId.text = gameHeader.gameId
            txvGameName.text = gameHeader.name

            btnJoinGame.setOnClickListener {

                writeGamePassword(binding.root.context, gameHeader)

            }


        }


    }

    private fun joinGame(context: Context?, gameHeader: GameHeader, password: String) {

        CoroutineScope(Dispatchers.IO).launch {


            var result: Response<ResponseBody>?
            try {
                result = GlobalData.userName?.let { it1 ->
                    ConexiónAPI.getRetrofit().create(APIService::class.java)
                        .joinGame(gameHeader.gameId, it1, password)
                }


                Log.i("games", result.toString())

                if (result?.code() == 401) {
                    (binding.root.context as Activity).runOnUiThread {
                        Toast.makeText(
                            context,
                            "Incorrect authentication",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else if (result?.code() == 403) {
                    (binding.root.context as Activity).runOnUiThread {
                        Toast.makeText(
                            context,
                            "You are not part of the players list",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else if (result?.code() == 404) {
                    (binding.root.context as Activity).runOnUiThread {
                        Toast.makeText(context, "Invalid Game's id", Toast.LENGTH_SHORT).show()
                    }
                } else {

                    if (result?.code() == 406) {
                        (binding.root.context as Activity).runOnUiThread {
                            Toast.makeText(
                                context,
                                "Game has already started or is full",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    if (result?.code() == 409) {
                        (binding.root.context as Activity).runOnUiThread {
                            Toast.makeText(
                                context,
                                "You are already part of this game or the name \"${GlobalData.userName}\" is already in use",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    }

                    var response: Response<Game>? = null
                    try {
                        response =
                            GlobalData.userName?.let {
                                ConexiónAPI.getRetrofit().create(APIService::class.java).getGame(
                                    gameHeader.gameId, it, password
                                )
                            }
                        if (response?.code() == 403) {
                            (binding.root.context as Activity).runOnUiThread {
                                Toast.makeText(
                                    context,
                                    "You are not part of the players list",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else if (response?.code() == 404) {
                            (binding.root.context as Activity).runOnUiThread {
                                Toast.makeText(context, "Invalid Game's id", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } else if (response?.code() == 406) {
                            (binding.root.context as Activity).runOnUiThread {
                                Toast.makeText(
                                    context,
                                    "There was an error getting game data",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {

                            var game = response?.body()

                            Log.i("games", game.toString())

                            (binding.root.context as Activity)?.runOnUiThread {
                                GlobalData.actualGame?.value = response?.body()
                                view.findNavController()
                                    .navigate(HomeFragmentDirections.actionHomeFragmentToGameLobbyFragment())
                            }
                        }

                    } catch (e: ConnectException) {
                        (context as Activity)?.runOnUiThread {
                            Toast.makeText(context, "Error", Toast.LENGTH_LONG).show()
                        }
                    }

                }
            } catch (e: ConnectException) {
                (binding.root.context as Activity)?.runOnUiThread {
                    Toast.makeText(binding.root.context, "Error", Toast.LENGTH_LONG).show()
                }
            }

        }
    }

    fun writeGamePassword(context: Context, gameHeader: GameHeader) {
        var password = ""

        val builder = android.app.AlertDialog.Builder(context, R.style.DialogTheme)
        builder.setTitle("Join game").setMessage("Write the game password.")

        val name = EditText(context)
        name.inputType = InputType.TYPE_CLASS_TEXT
        name.setTextColor(R.color.black)
        builder.setView(name)

        builder.setPositiveButton("Continue") { _, _ ->
            if (name.text.toString().isNullOrBlank()) {
                Toast.makeText(
                    context,
                    "You must type a password or enter without password",
                    Toast.LENGTH_SHORT
                ).show()

                writeGamePassword(context, gameHeader)


            } else {
                joinGame(context, gameHeader, name.text.toString())

            }

        }
        builder.setNegativeButton("Enter without password") { _, _ ->
            joinGame(context, gameHeader, "")
        }
        builder.show()


    }


}