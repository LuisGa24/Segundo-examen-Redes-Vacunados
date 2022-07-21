package com.redes.contagiados

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.redes.contagiados.api.APIService
import com.redes.contagiados.api.ConexiónAPI
import com.redes.contagiados.databinding.FragmentHomeBinding
import com.redes.contagiados.entidades.Game
import com.redes.contagiados.entidades.GameHeader
import com.redes.contagiados.entidades.NewGameRequest
import com.redes.contagiados.ui.gameslist.adapter.GameListAdapter
import com.redes.contagiados.util.GlobalData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import java.net.ConnectException

class HomeFragment : Fragment(), PopupMenu.OnMenuItemClickListener {
    private lateinit var adapter: GameListAdapter
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var filterBy: String = "gameid"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        GlobalData.resetData()



        if (GlobalData.userName.isNullOrEmpty()) {
            setUserName()
        } else {
            binding.txvNickName.text = GlobalData.userName
        }

        loadGames()


        binding.apply {

            btnNewGame.setOnClickListener {
                createNewGame()
            }

            btnExit.setOnClickListener {
                activity?.finish()
            }

            btnRefresh.setOnClickListener {
                loadGames()
                etxFilterValue.setText("")
            }

            btnChangeUser.setOnClickListener {
                setUserName()
            }

            btnChangeAPI.setOnClickListener {
                changeApiDialog()
            }

            btnFilter.setOnClickListener {
                showPopupMenu(it)
            }

            btnSearch.setOnClickListener {
                loadGames(true)
            }


        }


    }

    private fun showPopupMenu(view: View?) {
        val popupMenu = PopupMenu(view?.context, view)
        popupMenu.inflate(R.menu.popup_menu_filter)
        popupMenu.setOnMenuItemClickListener(this)
        popupMenu.show()
    }

    private fun changeApiDialog() {
        val builder = android.app.AlertDialog.Builder(context)

        builder.setTitle("Select the API")
            .setItems(
                arrayOf("Meseguercr API", "Azure API", "New API"),
                DialogInterface.OnClickListener { dialog, which ->
                    // The 'which' argument contains the index position
                    // of the selected item
                    when (which) {
                        0 -> ConexiónAPI.setNewBaseUrl(
                            "https://vacunados.meseguercr.com/",
                            binding.root.context
                        )
                        1 -> ConexiónAPI.setNewBaseUrl(
                            "https://apivacunados.azurewebsites.net/",
                            binding.root.context
                        )
                        2 -> typeNewApiUrl()
                        else -> dialog.dismiss()

                    }

                })
        builder.create().show()
    }

    private fun typeNewApiUrl() {
        val builder = android.app.AlertDialog.Builder(context, R.style.DialogTheme)
        builder.setTitle("New API").setMessage("Write the URL o the new API.")

        val name = EditText(context)
        name.hint = "https://my-new-api.com/"
        name.setHintTextColor(R.color.black)
        name.setTextColor(R.color.black)
        name.inputType = InputType.TYPE_CLASS_TEXT


        builder.setView(name)

        builder.setPositiveButton("Continue") { _, _ ->
            if (name.text.toString().isNullOrBlank()) {
                Toast.makeText(context, "You must enter the new API URL", Toast.LENGTH_SHORT).show()
                createNewGame()
            } else {
                var apiUrl = name.text.toString()
                ConexiónAPI.setNewBaseUrl(apiUrl, binding.root.context)
            }

        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun loadGames(filter: Boolean? = false) {

        CoroutineScope(Dispatchers.IO).launch {
            var call: Response<List<GameHeader>>? = null
            try {
                call = if (filter == true && binding.etxFilterValue.text.toString().isNotBlank()) {
                    ConexiónAPI.getRetrofit().create(APIService::class.java)
                        .getGameListFilter(filterBy, binding.etxFilterValue.text.toString())
                    /**Agregar filtro*/

                } else {
                    ConexiónAPI.getRetrofit().create(APIService::class.java).getGameList()

                }


                val games = call?.body()

                activity?.runOnUiThread {

                    games?.let { iniciarRecyclerView(context, it.reversed()) }

                }

                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Game list updated", Toast.LENGTH_LONG).show()
                }
            } catch (e: ConnectException) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Connection error", Toast.LENGTH_LONG).show()
                    e.message?.let { Log.i("games", it) }
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Connection error", Toast.LENGTH_LONG).show()
                    e.message?.let { Log.i("games", it) }
                }
            }


        }
    }

    private fun createNewGame() {
        val builder = android.app.AlertDialog.Builder(context, R.style.DialogTheme)
        builder.setTitle("New game").setMessage("Write the new game name.")

        val name = EditText(context)
        name.setTextColor(R.color.black)
        name.inputType = InputType.TYPE_CLASS_TEXT


        builder.setView(name)

        builder.setPositiveButton("Continue") { _, _ ->
            if (name.text.toString().isNullOrBlank()) {
                Toast.makeText(context, "You must create a game name", Toast.LENGTH_SHORT).show()
                createNewGame()
            } else {
                var gameName = name.text.toString()
                gameWithPassword(gameName)
            }

        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun gameWithPassword(gameName: String) {
        AlertDialog.Builder(binding.root.context, R.style.DialogTheme)
            .setPositiveButton(
                "Yes"
            ) { _, _ ->
                commitGameWithPassword(gameName)
            }
            .setNegativeButton(
                "No, Create game now!"
            ) { _, _ ->

                postNewGame(gameName, null)

            }
            .setTitle("New game") // El título
            .setMessage(
                "Do you want to add a password to the new game?"
            )
            .create()
            .show()
    }

    private fun postNewGame(gameName: String, password: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            //TODO Agregar validaciones segun el response result code

            var result: Response<Game>?
            try {
                result = GlobalData.userName?.let {
                    ConexiónAPI.getRetrofit().create(APIService::class.java)
                        .createNewGame(
                            NewGameRequest(
                                gameName, when {
                                    password.isNullOrBlank() -> ""
                                    else -> password
                                }
                            ), it
                        )
                }

                activity?.runOnUiThread {
                    GlobalData.actualGame?.value = result?.body()
                }



                activity?.runOnUiThread {
                    view?.findNavController()
                        ?.navigate(
                            HomeFragmentDirections.actionHomeFragmentToGameLobbyFragment()
                        )
                }


            } catch (e: ConnectException) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Error", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    @SuppressLint("ResourceAsColor")
    private fun commitGameWithPassword(gameName: String) {

        val builder = android.app.AlertDialog.Builder(context, R.style.DialogTheme)
        builder.setTitle("New game").setMessage("Write the new game password.")

        val password = EditText(context)


        password.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
        password.setTextColor(R.color.black)
        builder.setView(password)

        builder.setPositiveButton("Create new game!") { _, _ ->
            if (password.text.toString().isNullOrBlank()) {
                Toast.makeText(context, "You must create a game password", Toast.LENGTH_SHORT)
                    .show()
                createNewGame()
            } else {
                var gamePassword = password.text.toString()
                postNewGame(gameName, gamePassword)
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }


        builder.show()
    }

    private fun iniciarRecyclerView(context: Context?, games: List<GameHeader>) {
        adapter = GameListAdapter(games)
        val layoutManager = LinearLayoutManager(context)
        binding.recyclerGameList.layoutManager = layoutManager
        binding.recyclerGameList.adapter = adapter

    }


    fun setUserName() {
        val builder = android.app.AlertDialog.Builder(context, R.style.DialogTheme)
        builder.setTitle("Nick name")
            .setMessage("Write your NickName")
            .setCancelable(false)

        val name = EditText(context)
        name.setTextColor(R.color.black)
        name.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(name)

        builder.setPositiveButton("Continue") { _, _ ->
            if (name.text.toString().isNullOrBlank()) {
                Toast.makeText(context, "You must add a nickname to continue", Toast.LENGTH_SHORT)
                    .show()
                setUserName()
            } else {
                GlobalData.userName = name.text.toString()
                binding.txvNickName.text = GlobalData.userName
                binding.txvNickName.visibility = View.VISIBLE
            }

        }
        builder.show()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        binding.apply {
            when (item?.itemId) {
                R.id.ItemOwner -> {
                    filterBy = "owner"
                    etxFilterValue.hint = "Search by owner"
                }
                R.id.ItemGameId -> {
                    filterBy = "gameid"
                    etxFilterValue.hint = "Search by game id"
                }
                R.id.ItemStatus -> {
                    filterBy = "status"
                    etxFilterValue.hint = "Search by status"
                }

                else -> return true
            }

        }
        return true
    }

}