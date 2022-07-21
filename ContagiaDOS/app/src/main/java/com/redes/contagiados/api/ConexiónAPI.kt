package com.redes.contagiados.api

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.redes.contagiados.entidades.GameHeader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.ConnectException

class ConexiónAPI {

    companion object {

        private var baseUrl = "https://vacunados.meseguercr.com/"

        fun getRetrofit(url: String? = baseUrl): Retrofit {
            val client = OkHttpClient.Builder().build()
            return Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }

        fun setNewBaseUrl(url: String, context: Context) {

            var activity = context as Activity

            CoroutineScope(Dispatchers.IO).launch {
                var call: Response<List<GameHeader>>? = null
                try {
                    call =
                        getRetrofit(url).create(APIService::class.java).getGameList()

                    if (call.code() == 200 || call.code() == 204) {
                        activity?.runOnUiThread {
                            Toast.makeText(context, "API updated successfully", Toast.LENGTH_SHORT)
                                .show()


                        }
                        baseUrl = url
                    }


                } catch (e: ConnectException) {
                    activity?.runOnUiThread {
                        Toast.makeText(
                            context,
                            "Couldn't connect to new API, configuration not applied",
                            Toast.LENGTH_SHORT
                        )
                            .show()

                        Log.i("games", e.message.toString())
                    }
                } catch (e: Exception) {
                    activity?.runOnUiThread {
                        Toast.makeText(
                            context,
                            "Couldn't connect to new API, configuration not applied",
                            Toast.LENGTH_LONG
                        )
                            .show()
                        Log.i("games", e.message.toString())
                    }
                }


            }

        }
    }
}


