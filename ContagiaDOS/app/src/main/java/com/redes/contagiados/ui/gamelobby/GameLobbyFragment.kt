package com.redes.contagiados.ui.gamelobby

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.redes.contagiados.R
import com.redes.contagiados.api.APIService
import com.redes.contagiados.api.ConexiónAPI
import com.redes.contagiados.databinding.FragmentGameLobbyBinding
import com.redes.contagiados.ui.gamelobby.playerlist.adapter.PlayerListAdapter
import com.redes.contagiados.util.GlobalData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response
import java.net.ConnectException

class GameLobbyFragment : Fragment() {


    private lateinit var adapter: PlayerListAdapter
    private var _binding: FragmentGameLobbyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_game_lobby, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentGameLobbyBinding.bind(view)

        GlobalData.startGameObservation(binding.root.context)
        GlobalData.singleDevice = false

        GlobalData.actualGame?.observe(viewLifecycleOwner) {
            iniciarRecyclerView(context, it.players)
        }

        binding.apply {
            if (GlobalData.actualGame?.value?.owner == GlobalData.userName) {
                btnStartGame.visibility = View.VISIBLE
                btnSingleDevice.visibility = View.VISIBLE
            } else {
                btnStartGame.visibility = View.GONE
                btnSingleDevice.visibility = View.GONE
            }
            observeGameStatus()

            btnSingleDevice.setOnClickListener {
                if (btnSingleDevice.isChecked) {
                    btnAddPlayer.visibility = View.VISIBLE
                    GlobalData.singleDevice = true
                } else {
                    btnAddPlayer.visibility = View.GONE
                    GlobalData.singleDevice = false
                }
                Log.i("games", btnSingleDevice.isChecked.toString())
            }

            btnStartGame.setOnClickListener {

                CoroutineScope(Dispatchers.IO).launch {
                    var head: Response<Void>?
                    try {
                        head =
                            GlobalData.userName?.let { it1 ->
                                GlobalData.actualGame?.value?.let { it2 ->
                                    ConexiónAPI.getRetrofit().create(APIService::class.java)
                                        .getGameHeader(
                                            it2.gameId,
                                            it1,
                                            GlobalData.actualGame!!.value!!.password
                                        )
                                }
                            }

                        if (head?.code() == 406) {
                            activity?.runOnUiThread {
                                Toast.makeText(
                                    context,
                                    "Not enough players. Invite more to join",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            activity?.runOnUiThread {
                                view.findNavController()
                                    .navigate(GameLobbyFragmentDirections.actionGameLobbyFragmentToPlayGameFragment())
                            }
                        }

                        Log.i("games", head.toString())
                    } catch (e: ConnectException) {
                        activity?.runOnUiThread {
                            Toast.makeText(requireContext(), "Error", Toast.LENGTH_LONG).show()
                        }
                    }

                }

            }

            btnAddPlayer.setOnClickListener {
                addPLayer()
            }
        }

    }

    private fun observeGameStatus() {
        val myHandler = Handler(Looper.getMainLooper())

        myHandler.post(object : Runnable {
            override fun run() {

                if (GlobalData.actualGame?.value?.status == "lobby") {
                    myHandler.postDelayed(this, 2000 /*2 segundos*/)
                } else {
                    myHandler.removeCallbacks(this)
                    view?.findNavController()
                        ?.navigate(GameLobbyFragmentDirections.actionGameLobbyFragmentToPlayGameFragment())
                }

            }
        })
    }

    private fun iniciarRecyclerView(context: Context?, players: List<String>) {
        adapter = PlayerListAdapter(players)
        val layoutManager = LinearLayoutManager(context)
        binding.recyclerPlayers.layoutManager = layoutManager
        binding.recyclerPlayers.adapter = adapter
    }

    fun addPLayer() {
        val builder = android.app.AlertDialog.Builder(context, R.style.DialogTheme)
        builder.setTitle("Nick name")
            .setMessage("Write player NickName")
            .setCancelable(false)

        val name = EditText(context)
        name.setTextColor(R.color.black)
        name.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(name)

        builder.setPositiveButton("Continue") { _, _ ->
            if (name.text.toString().isNullOrBlank()) {
                Toast.makeText(context, "You must add a nickname to continue", Toast.LENGTH_SHORT)
                    .show()
                addPLayer()
            } else {
               joinGame(context, name.text.toString())

            }

        }
        builder.show()
    }

    private fun joinGame(context: Context?, playerName: String) {

        CoroutineScope(Dispatchers.IO).launch {


            var result: Response<ResponseBody>?
            try {
                result = GlobalData.actualGame?.value?.let {
                    ConexiónAPI.getRetrofit().create(APIService::class.java)
                        .joinGame(it.gameId, playerName, it.password)
                }

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
                }
            } catch (e: Exception) {
                (binding.root.context as Activity)?.runOnUiThread {
                    Toast.makeText(binding.root.context, "Error", Toast.LENGTH_LONG).show()
                }
            }

        }
    }
}