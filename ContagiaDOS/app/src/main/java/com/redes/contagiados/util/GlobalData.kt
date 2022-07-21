package com.redes.contagiados.util

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.redes.contagiados.api.APIService
import com.redes.contagiados.api.ConexiónAPI
import com.redes.contagiados.entidades.Game
import com.redes.contagiados.entidades.Group
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import retrofit2.Response
import java.net.ConnectException


class GlobalData() {

    companion object {

        var userName: String? = null
        var actualGame: MutableLiveData<Game>? = MutableLiveData(Game())
        var gameObservation: Boolean = false
        var temporalGroup = Group()
        private var myHandler : Handler? = null
        private var runnable : Runnable? = null
        var singleDevice = false

        val weeklyGroups = arrayOf(
            intArrayOf(2, 2, 2, 3, 3, 3),
            intArrayOf(3, 3, 3, 4, 4, 4),
            intArrayOf(2, 4, 3, 4, 4, 4),
            intArrayOf(3, 3, 4, 5, 5, 5),
            intArrayOf(3, 4, 4, 5, 5, 5),
        )
        fun startGameObservation(context: Context) {

            if (!gameObservation) {

                gameObservation = true
                myHandler = Handler(Looper.getMainLooper())

                myHandler!!.post(object : Runnable {
                    override fun run() {

                        updateGame(context)

                        Log.i("games", actualGame?.value.toString())


                        myHandler?.postDelayed(this, 3000 /*3 segundos*/)
                        runnable = this


                    }
                })

            }

        }

        fun stopGameObservation() {
            this.runnable?.let { myHandler?.removeCallbacks(it) }
            gameObservation = false
        }

        fun resetData(){
            actualGame= MutableLiveData(Game())
            gameObservation = false
            temporalGroup = Group()
            myHandler= null
            runnable = null
            singleDevice = false
        }

        private fun updateGame(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                var response: Response<Game>? = null
                try {
                    response =
                        actualGame?.value?.let {
                            ConexiónAPI.getRetrofit().create(APIService::class.java).getGame(
                                it.gameId, it.owner, it.password
                            )
                        }


                    (context as Activity).runOnUiThread {
                        actualGame?.value = response?.body()
                    }

                    if(actualGame?.value?.status == "ended"){
                        stopGameObservation()
                    }

                } catch (e: ConnectException) {
                    (context as Activity).runOnUiThread {
                        Toast.makeText(context, "Error", Toast.LENGTH_LONG).show()
                    }
                }catch (e : Exception){
                    (context as Activity).runOnUiThread {
                        Toast.makeText(context, "Exception", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}




