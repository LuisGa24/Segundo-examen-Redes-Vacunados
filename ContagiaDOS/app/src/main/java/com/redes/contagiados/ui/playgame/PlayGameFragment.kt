package com.redes.contagiados.ui.playgame

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.redes.contagiados.R
import com.redes.contagiados.api.APIService
import com.redes.contagiados.api.ConexiónAPI
import com.redes.contagiados.databinding.FragmentPlayGameBinding
import com.redes.contagiados.entidades.Player
import com.redes.contagiados.entidades.Psycho
import com.redes.contagiados.ui.playgame.workteamlist.GroupListAdapter
import com.redes.contagiados.util.GlobalData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response
import java.net.ConnectException


class PlayGameFragment : Fragment() {

    private lateinit var adapter: GroupListAdapter
    private var _binding: FragmentPlayGameBinding? = null
    private val binding get() = _binding!!
    private var dialog: Dialog? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {

            val builder = AlertDialog.Builder(binding.root.context)
            builder.setMessage("Are you sure you want to Exit?")
                .setCancelable(false)
                .setPositiveButton(
                    "Yes"
                ) { _, _ ->
                    view?.findNavController()
                        ?.navigate(PlayGameFragmentDirections.actionPlayGameFragmentToHomeFragment())
                    this.remove()
                }
                .setNegativeButton("No") { dialog, _ -> dialog.cancel() }
            val alert = builder.create()
            alert.show()

        }


        return inflater.inflate(R.layout.fragment_play_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentPlayGameBinding.bind(view)


        binding.apply {




            btnProposeGroup.visibility = View.GONE
            btnGoSafely.visibility = View.GONE
            btnGoUnSafely.visibility = View.GONE
            var firsTime = true
            var gameFinished = false


            GlobalData.actualGame?.observe(viewLifecycleOwner) { game ->

                if (firsTime  && GlobalData.actualGame!!.value!!.rounds.isNotEmpty()) {
                    if (GlobalData.singleDevice) {
                        btnSwitchUser.visibility = View.VISIBLE
                        setProgressDialog(true)
                    } else {
                        btnSwitchUser.visibility = View.GONE
                        setProgressDialog(true, GlobalData.userName)

                    }
                    firsTime = false
                    binding.rootLayout.visibility = View.VISIBLE
                }

                dialog?.dismiss()



                for (i in 0 until game.psychoWin.size) {
                    showRoundWiner(i, game.psychoWin[i])
                }

                if (game.status == "ended" && !gameFinished) {

                    var psychoWins = 0
                    game.psychoWin.forEach {
                        if (it)
                            psychoWins++
                    }
                    dialogEndedGame(psychoWins == 3)
                    gameFinished = true


                }

                if (game.status == "rounds" && (game.rounds.last().group.contains(
                        GlobalData.userName?.let { it1 ->
                            Player(
                                it1,
                                true
                            )
                        }
                    ))
                    || ((game.rounds.isNotEmpty()) && game.rounds.last().group.contains(
                        GlobalData.userName?.let { it1 ->
                            Player(
                                it1, false
                            )
                        }
                    ))
                ) {
                    btnGoSafely.visibility = View.GONE
                    btnGoUnSafely.visibility = View.GONE
                }

                txvMyRole.text = "Model citizen"
                imvRoleImage.setBackgroundResource(R.drawable.ic_vaccines_white_24dp)

                if (game.status != "lobby") {
                    txvRoundNumber.text = "Week ${game.rounds?.size}"
                    txvLeader.text = game.rounds?.last()?.leader

                    txvMyName.text = GlobalData.userName

                    var iAmPsycho = false
                    game.psychos?.forEach { player ->

                        if (player == GlobalData.userName) {
                            iAmPsycho = true
                            txvMyRole.text = "Psycho citizen"
                            imvRoleImage.setBackgroundResource(R.drawable.ic_coronavirus_white_24dp)
                        }
                    }


                    /**Inicializar recycler*/
                    iniciarRecyclerView(context, game.players)
                    var iAmLeader = game.rounds.last().leader == GlobalData.userName

                    if (iAmLeader && game.status == "leader") {
                        btnProposeGroup.visibility = View.VISIBLE
                    } else {
                        btnProposeGroup.visibility = View.GONE
                    }


                    var iAmSelectedToGroup = false

                    game.rounds.last().group.forEach { player: Player ->
                        if (player.name == GlobalData.userName)
                            iAmSelectedToGroup = true
                        return@forEach
                    }



                    if (game.status == "rounds" && iAmSelectedToGroup && !(game.status == "rounds" && (game.rounds.last().group.contains(
                            GlobalData.userName?.let { it1 ->
                                Player(
                                    it1,
                                    true
                                )
                            }
                        ))
                                || (game.rounds.last().group.contains(
                            GlobalData.userName?.let { it1 ->
                                Player(
                                    it1, false
                                )
                            }
                        ))
                                )
                    ) {
                        btnGoSafely.visibility = View.VISIBLE
                        if (iAmPsycho) {
                            btnGoUnSafely.visibility = View.VISIBLE
                        } else {
                            btnGoUnSafely.visibility = View.GONE
                        }
                    } else {
                        btnGoUnSafely.visibility = View.GONE
                        btnGoSafely.visibility = View.GONE
                    }


                }
            }


            btnGoSafely.setOnClickListener {
                btnGoSafely.visibility = View.GONE
                btnGoUnSafely.visibility = View.GONE
                goIntoRound(false)
                setProgressDialog()
            }

            btnGoUnSafely.setOnClickListener {
                btnGoSafely.visibility = View.GONE
                btnGoUnSafely.visibility = View.GONE
                goIntoRound(true)
                setProgressDialog()
            }

            btnShowProfile.setOnClickListener {
                if (cardProfile.visibility == View.VISIBLE) {
                    cardProfile.visibility = View.GONE
                    btnShowProfile.setImageResource(R.drawable.ic_expand_more_white_24dp)
                } else {
                    cardProfile.visibility = View.VISIBLE
                    btnShowProfile.setImageResource(R.drawable.ic_expand_less_white_24dp)
                }
            }

            btnHome.setOnClickListener {
                view.findNavController()
                    .navigate(PlayGameFragmentDirections.actionPlayGameFragmentToHomeFragment())
            }

            btnProposeGroup.setOnClickListener {


                if (GlobalData.temporalGroup.group.size == GlobalData.weeklyGroups[GlobalData.actualGame?.value?.rounds?.size!!.minus(
                        1
                    )][(GlobalData.actualGame?.value?.players?.size)!!.minus(
                        5
                    )]
                ) {

                    setProgressDialog()
                    btnProposeGroup.visibility = View.GONE
                    CoroutineScope(Dispatchers.IO).launch {
                        var result: Response<ResponseBody>? = null
                        try {

                            result =
                                GlobalData.actualGame?.value?.let { it1 ->
                                    GlobalData.userName?.let { it2 ->
                                        GlobalData.actualGame!!.value?.let { it3 ->
                                            ConexiónAPI.getRetrofit().create(APIService::class.java)
                                                .groupProposal(
                                                    it1.gameId,
                                                    GlobalData.temporalGroup,
                                                    it2,
                                                    it3.password
                                                )

                                        }
                                    }
                                }

                            Log.i("games", result.toString())
                        } catch (e: ConnectException) {
                            activity?.runOnUiThread {
                                Toast.makeText(requireContext(), "Error", Toast.LENGTH_LONG).show()
                            }
                        }

                    }

                } else {

                    activity?.runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            "You must select more players for the group",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }


            }

            btnSwitchUser.setOnClickListener {

                switchUser()

            }
        }


    }


    private fun switchUser() {
        val builder = android.app.AlertDialog.Builder(context)

        builder.setTitle("Next player")
            .setItems(
                GlobalData.actualGame!!.value!!.players.toTypedArray()
            ) { _, which ->

                changeUserDialog(GlobalData.actualGame!!.value!!.players[which])

            }
        builder.create().show()
    }

    private fun changeUserDialog(newPlayer: String) {

        GlobalData.userName = newPlayer

        val builder = AlertDialog.Builder(
            binding.root.context,
            android.R.style.ThemeOverlay
        )

        builder.setTitle("Next player: ${GlobalData.userName}")
            .setMessage("\n\n${GlobalData.userName}, when you are ready press OK\n\n")
        builder.setIcon(R.drawable.ic_shift_change_svgrepo_com_blue)
            .setPositiveButton("Ok", null)
        builder.setCancelable(false)
            .setOnDismissListener(null)
            .setOnCancelListener(null)
            .show()

        setProgressDialog()


    }

    private fun dialogEndedGame(b: Boolean) {
        binding.apply {
            btnProposeGroup.visibility = View.GONE
            btnGoSafely.visibility = View.GONE
            btnGoUnSafely.visibility = View.GONE
        }

        binding.root.isClickable = false
        AlertDialog.Builder(binding.root.context, R.style.DialogTheme)
            .setPositiveButton(
                "Home"
            ) { _, _ ->
                view?.findNavController()
                    ?.navigate(PlayGameFragmentDirections.actionPlayGameFragmentToHomeFragment())
            }.setNegativeButton(
                "Game Overview"
            ) { dialog, _ ->
                /***/
                binding.apply {
                    btnHome.visibility = View.VISIBLE

                }
                dialog.dismiss()
            }
            .setTitle("Game ended") // El título
            .setMessage(if (b) "Psychos won" else "Model Citizens Won") // El mensaje
            .setIcon(if (b) R.drawable.ic_coronavirus_white_24dp else R.drawable.ic_vaccines_white_24dp)
            .setCancelable(false)
            .create()
            .show()
    }

    private fun showRoundWiner(i: Int, b: Boolean) {
        binding.apply {
            when (i) {
                0 -> setRoundWinner(imvR1, b)
                1 -> setRoundWinner(imvR2, b)
                2 -> setRoundWinner(imvR3, b)
                3 -> setRoundWinner(imvR4, b)
                4 -> setRoundWinner(imvR5, b)
            }
        }
    }

    private fun setRoundWinner(imvR: ImageView, b: Boolean) {
        imvR.visibility = View.VISIBLE
        if (b) {
            imvR.setBackgroundResource(R.drawable.ic_coronavirus_white_24dp)
        } else {
            imvR.setBackgroundResource(R.drawable.ic_vaccines_white_24dp)
        }
    }

    private fun goIntoRound(unsafeMode: Boolean) {

        CoroutineScope(Dispatchers.IO).launch {
            var result: Response<ResponseBody>? = null
            try {
                result = GlobalData.actualGame?.value?.let { it1 ->
                    GlobalData.userName?.let { it2 ->
                        GlobalData.actualGame!!.value?.let { it3 ->
                            ConexiónAPI.getRetrofit().create(APIService::class.java)
                                .goIntoRound(it1.gameId, Psycho(unsafeMode), it2, it3.password)
                        }
                    }
                }




                Log.i("games", result.toString())
            } catch (e: ConnectException) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Error", Toast.LENGTH_LONG).show()
                }
            }

        }

    }

    private fun iniciarRecyclerView(context: Context?, players: List<String>) {
        adapter = GroupListAdapter(players, viewLifecycleOwner)
        val layoutManager = LinearLayoutManager(context)
        binding.recyclerWorkTeam.layoutManager = layoutManager
        binding.recyclerWorkTeam.adapter = adapter


    }

    private fun setProgressDialog(fistTime: Boolean = false, player: String? = null) {
        val llPadding = 30
        val ll = LinearLayout(context)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam
        val progressBar = ProgressBar(context)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam
        llParam = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER
        val tvText = TextView(context)
        tvText.text = "Wait a moment ..."
        tvText.setTextColor(Color.parseColor("#000000"))
        tvText.textSize = 20f
        tvText.layoutParams = llParam
        ll.addView(progressBar)
        ll.addView(tvText)
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(context)
        builder.setView(ll)
        builder.setCancelable(false)
        val dialog: android.app.AlertDialog? = builder.create()
        dialog?.setOnDismissListener {
            if (fistTime) {
                if (player != null) {
                    changeUserDialog(player)
                } else {
                    changeUserDialog(GlobalData.actualGame!!.value!!.rounds.last().leader)
                }

            }
        }
        this.dialog = dialog
        dialog?.show()
        val window: Window? = dialog?.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            if (dialog != null) {
                layoutParams.copyFrom(dialog.window!!.attributes)
            }
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            if (dialog != null) {
                dialog.window!!.attributes = layoutParams
            }
        }
    }
}