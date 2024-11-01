package com.example.bkkrouteplanner

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class PopularTimesApi {
    private val client = OkHttpClient()

    fun getPopularTimesById(placeId: String, callback: (JSONObject?) -> Unit) {
        val apiKey = BuildConfig.MAPS_API_KEY
        val url = "http://192.168.1.105:5000/popular-times?place_id=$placeId&api_key=$apiKey"
        Log.d("checkApitest","http://192.168.1.105:5000/popular-times?place_id=$placeId&api_key=$apiKey")
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("PopularTimes", "Request failed: ${e.message}")
                e.printStackTrace()
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback(null)
                    } else {
                        val jsonResponse = response.body?.string()
                        val jsonObject = JSONObject(jsonResponse)
                        callback(jsonObject)
                        parsePopularTimes(jsonObject)
                    }
                }
            }
        })
    }

    fun parseAndLogPopularTimes(jsonObject: JSONObject?): String {
        val result = StringBuilder()

        jsonObject?.let {
            val name = it.optString("name", "Unknown Place")
            val popularTimes = it.optJSONArray("populartimes")

            popularTimes?.let { times ->
                for (i in 0 until times.length()) {
                    val dayData = times.getJSONObject(i)
                    val day = dayData.getString("name")
                    val data = dayData.getJSONArray("data")

                    val dataList = mutableListOf<String>()
                    for (j in 0 until data.length()) {
                        dataList.add(data.getString(j))
                    }

                    val dataString = dataList.joinToString(", ")
                    result.append("$day: $dataString\n")
                }
            } ?: result.append("No Popular Times\n")
        } ?: result.append("Can't Receive Popular Times\n")

        return result.toString()
    }

    fun parsePopularTimes(jsonObject: JSONObject?): List<PopularTime>? {
        val popularTimes = mutableListOf<PopularTime>()

        try {
            val popularTimesJsonArray = jsonObject?.optJSONArray("populartimes")

            if (popularTimesJsonArray != null) {
                for (i in 0 until popularTimesJsonArray.length()) {
                    val dayJson = popularTimesJsonArray.optJSONObject(i)
                    val day = dayJson?.optString("name") ?: "Unknown"
                    val times = dayJson?.optJSONArray("data")

                    val timeSlots = times?.let {
                        (0 until it.length()).map { hour ->
                            TimeSlot(hour, it.optInt(hour, 0)) // Default to 0 if not found
                        }
                    } ?: emptyList()

                    popularTimes.add(PopularTime(day, timeSlots))
                }
            }
            return popularTimes
        } catch (e: Exception) {
            Log.e("ParseError", "Error parsing popular times: ${e.message}")
            return null
        }
    }

}
