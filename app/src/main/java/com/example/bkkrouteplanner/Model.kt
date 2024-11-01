package com.example.bkkrouteplanner

import android.content.Context
import android.util.Log
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import java.util.Calendar
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.time.Duration
import kotlinx.coroutines.CompletableDeferred
class Model {

    private var apiKey = BuildConfig.MAPS_API_KEY
    private lateinit var id: String
    private lateinit var planName: String
    private lateinit var startPlaceID: place
    private var latlng: com.google.android.gms.maps.model.LatLng? = null
    private var destList: MutableList<place> = mutableListOf()
    private lateinit var date: String
    private lateinit var timeString: String
    private lateinit var time: LocalTime
    private lateinit var itinerary: MutableList<Pair<place, LocalTime>>
    init {
        itinerary = mutableListOf()
    }
    private lateinit var placesClient: PlacesClient
    private val placeInfoList: MutableList<PlaceInfo> = mutableListOf()
    private val itineraryDeferred = CompletableDeferred<List<Pair<place, LocalTime>>>()

    private val weightDistance: Double = 0.3
    private val weightOpenTime: Double = 0.4
    private val weightPopularTime: Double = 0.2

    fun initializePlacesClient(context: Context) {
        placesClient = Places.createClient(context)
    }

    suspend fun getTrip(): Plan {
        val finalItinerary = itineraryDeferred.await().toMutableList()
        val newPlan = Plan(
            id = id,
            planName = planName,
            start = startPlaceID,
            latlng = null,
            destination = destList,
            date = date,
            time = time.toString(),
            itinerary = finalItinerary
        )
        Log.d("getTrip","${newPlan.itinerary}")
        return newPlan
    }

    suspend fun makePlan(plan: Plan) {
        id = plan.id
        planName = plan.planName
        startPlaceID = plan.start!!
        latlng = plan.latlng
        destList = plan.destination
        date = plan.date
        timeString = plan.time

        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm a")
        time = LocalTime.parse(timeString, timeFormatter)

        withContext(Dispatchers.IO) {
            destList.forEach { destination ->
                val placeInfo = fetchPlaceInfo(destination.id, destination.name)
                if (placeInfo != null) {
                    placeInfoList.add(placeInfo)
                }
            }
            planItinerary() // เริ่มแผนการเดินทาง
            itineraryDeferred.complete(itinerary) // เสร็จแล้วปลดบล็อก
        }
    }

    suspend fun fetchPlaceInfo(placeId: String, placeName: String): PlaceInfo? {
        val api = PopularTimesApi()
        return withTimeoutOrNull(10000L) { // ตั้งเวลา timeout เป็น 10 วินาที
            suspendCancellableCoroutine { continuation ->
                api.getPopularTimesById(placeId) { jsonObject ->
                    val popularTimesList = api.parsePopularTimes(jsonObject)
                    fetchOpeningHours(placeId) { openingHours ->
                        if (popularTimesList != null && openingHours != null) {
                            val placeInfo = PlaceInfo(
                                id = placeId,
                                name = placeName,
                                openingHours = openingHours,
                                popularTimes = popularTimesList
                            )
                            continuation.resume(placeInfo) {}
                        } else {
                            Log.e("fetchPlaceInfo", "Failed to retrieve complete data for place ID: $placeId")
                            continuation.resume(null) {}
                        }
                    }
                }
            }
        }
    }


    suspend fun planItinerary() = withContext(Dispatchers.Default) {
        var currentTime = time
        var currentPlace = startPlaceID
        val remainingPlaces = destList.toMutableList()
        val selectedItinerary = mutableListOf<Pair<place, LocalTime>>()

        while (remainingPlaces.isNotEmpty()) {
            val sumScores = mutableListOf<Pair<place, Double>>()

            val (openTimeScoreAllPlace, openingTime) = calculateOpeningHoursScore(currentTime, remainingPlaces)
            Log.d("Itinerary", "Open Time Scores: $openTimeScoreAllPlace")
            val distanceScoreAllPlace = calculateDistanceScore(currentPlace, remainingPlaces)
            Log.d("Itinerary", "Distance Scores: $distanceScoreAllPlace")
            val popularTimeScoreAllPlace = calculatePopularTimeScore(currentTime, remainingPlaces)
            Log.d("Itinerary", "Popular Time Scores: $popularTimeScoreAllPlace")

            for ((destination, openingScore) in openTimeScoreAllPlace) {
                val distanceScore = distanceScoreAllPlace.find { it.first == destination }?.second ?: 0.0
                val popularTimeScore = popularTimeScoreAllPlace.find { it.first == destination }?.second ?: 0.0
                val totalScore: Double = (distanceScore * weightDistance) + (openingScore * weightOpenTime) + (popularTimeScore * weightPopularTime)
                sumScores.add(Pair(destination, totalScore))
            }

            Log.d("Itinerary", "Sum Scores: $sumScores")
            val nextPlace = sumScores.maxByOrNull { it.second }?.first
            if (nextPlace == null) {
                Log.d("Itinerary", "No next place found, breaking loop.")
                break
            }

            nextPlace.let {
                currentPlace = it
                val openTime = openingTime.find { opening -> opening.first == currentPlace }?.second
                if (openTime != null) {
                    val duration = Duration.between(currentTime, openTime).toHours()
                    if (duration > 0) {
                        currentTime = currentTime.plusHours(duration)
                        Log.d("Itinerary", "currentPlace : ${currentPlace}")
                        Log.d("Itinerary", "currentTime : ${currentTime}")
                        Log.d("Itinerary", "duration : ${duration}")
                    }
                    selectedItinerary.add(Pair(it, currentTime))
                } else {
                    Log.d("Itinerary", "Open time not found for ${currentPlace.name}, adding default time.")
                    selectedItinerary.add(Pair(it, currentTime))
                }
                currentTime = currentTime.plusHours(2)
                remainingPlaces.remove(it)
                Log.d("remainingPlaces", "${remainingPlaces}")
            }
        }

        itinerary = selectedItinerary
        itineraryDeferred.complete(itinerary)
        Log.d("Itinerary", "Final itinerary: $selectedItinerary")
        Log.d("Itinerary", "Final itinerary: $itinerary")

    }

    suspend fun calculateDistanceScore(
        startPlace: place,
        remainingPlaces: MutableList<place>
    ): MutableList<Pair<place, Double>> = withContext(Dispatchers.IO) {

        val distanceScores = mutableListOf<Pair<place, Double>>()
        val distanceAllPlace = mutableListOf<Pair<place, Int>>()

        for (destination in remainingPlaces) {
            val distance = fetchDirectionsByPlaceId(startPlace.id, destination.id)
            distanceAllPlace.add(Pair(destination, distance))
        }

        if (distanceAllPlace.isEmpty()) {
            Log.d("calculateDistanceScore", "No distances calculated")
        } else {
            distanceAllPlace.sortBy { it.second }
            var score = 10.0
            for ((place, _) in distanceAllPlace) {
                distanceScores.add(Pair(place, score))
                score = (score - 1).coerceAtLeast(1.0)
            }
        }

        Log.d("calculateDistanceScore", "Distance Scores: $distanceScores")
        return@withContext distanceScores
    }

    private fun calculateOpeningHoursScore(currentTime: LocalTime, remainingPlaces: MutableList<place>):  Pair<MutableList<Pair<place, Double>>, MutableList<Pair<place, LocalTime>>> {
        val sumOfOpeningHoursScores = mutableListOf<Pair<place, Double>>()
        val day = getDayOfWeek(date)
        val timeOpeningScores = mutableListOf<Pair<place, Double>>()
        val timeOpening = mutableListOf<Pair<place, LocalTime>>()
        val openingHoursScores = mutableListOf<Pair<place, Double>>()
        val openingHoursAllPlace = mutableListOf<Pair<place, Long>>()

        val placesToRemove = mutableListOf<place>() // สร้าง List เพื่อเก็บสถานที่ที่ต้องการลบ

        for (destination in remainingPlaces) {
            val thisPlaceInfo = placeInfoList.find { it.name == destination.name }

            if (thisPlaceInfo == null) {
                Log.e("calculateOpeningHoursScore", "PlaceInfo not found for destination: ${destination.name}")
                continue
            }

            val dayHoursMap = parseOpeningHours(thisPlaceInfo.openingHours)
            val hoursToday = dayHoursMap[day]
            if (hoursToday == null) {
                placesToRemove.add(destination) // เพิ่ม destination ที่ต้องการลบไปยัง List
                continue
            }

            val openTime = hoursToday.first
            val closeTime = hoursToday.second
            timeOpening.add(Pair(destination, openTime))
            val duration = Duration.between(currentTime, openTime).toMinutes()

            val timeOpenScore = when {
                duration <= 0 -> 5.0
                duration <= 60 -> 4.0
                duration <= 180 -> 3.0
                else -> 2.0
            }

            timeOpeningScores.add(Pair(destination, timeOpenScore))

            val durationOpenHours = Duration.between(openTime, closeTime).toHours()
            openingHoursAllPlace.add(Pair(destination, durationOpenHours))
        }

        remainingPlaces.removeAll(placesToRemove)

        openingHoursAllPlace.sortBy { it.second }
        var score = 5.0
        for ((place, _) in openingHoursAllPlace) {
            openingHoursScores.add(Pair(place, score))
            score = (score - 1).coerceAtLeast(1.0)
        }

        for ((destination, timeScore) in timeOpeningScores) {
            val openingScore = openingHoursScores.find { it.first == destination }?.second ?: 0.0
            val totalScore = timeScore + openingScore
            sumOfOpeningHoursScores.add(Pair(destination, totalScore))
        }

        return Pair(sumOfOpeningHoursScores, timeOpening)
    }



    private fun calculatePopularTimeScore(
        currentTime: LocalTime,
        remainingPlaces: MutableList<place>
    ): MutableList<Pair<place, Double>> {

        val popularTimeScores = mutableListOf<Pair<place, Double>>()
        val currentHour = currentTime.hour
        val currentDay = getDayOfWeek(date)

        for (destination in remainingPlaces) {
            val thisPlaceInfo = placeInfoList.find { it.name == destination.name }

            if (thisPlaceInfo != null) {
                val popularTimeForToday = thisPlaceInfo.popularTimes.find { it.day == currentDay }
                val timeSlot = popularTimeForToday?.hours?.find { it.hour == currentHour }

                val score = if (timeSlot != null && timeSlot.popularity > 50) 2.0 else 5.0
                popularTimeScores.add(Pair(destination, score))
            } else {
                Log.d("calculatePopularTimeScore", "PlaceInfo not found for ${destination.name}")
            }
        }

        Log.d("calculatePopularTimeScore", "Popular Time Scores: $popularTimeScores")
        return popularTimeScores
    }

    suspend fun fetchDirectionsByPlaceId(startPlaceId: String, endPlaceId: String): Int {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=place_id:$startPlaceId&destination=place_id:$endPlaceId&mode=driving&key=$apiKey"

            val request = Request.Builder().url(url).build()
            try {
                val response = client.newCall(request).execute() // รอจนกว่าคำขอ API จะเสร็จ
                val jsonData = response.body?.string()
                val jsonObject = JSONObject(jsonData ?: "")
                val routes = jsonObject.getJSONArray("routes")

                if (routes.length() > 0) {
                    val legs = routes.getJSONObject(0).getJSONArray("legs")
                    legs.getJSONObject(0).getJSONObject("distance").getInt("value") // คืนระยะทาง
                } else {
                    0 // กรณีไม่มีเส้นทาง
                }
            } catch (e: IOException) {
                e.printStackTrace()
                0 // กรณีเกิดข้อผิดพลาด
            }
        }
    }

    private fun fetchOpeningHours(placeId: String, callback: (String?) -> Unit) {
        val request = FetchPlaceRequest.newInstance(placeId, listOf(Place.Field.OPENING_HOURS))

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val openingHours = response.place.openingHours?.weekdayText?.joinToString("\n")
                callback(openingHours)
            }
            .addOnFailureListener { exception ->
                Log.e("FetchPlaceError", "Place not found: ${exception.message}")
                callback(null)
            }
    }

    fun getDayOfWeek(date: String): String {
        val parts = date.split("/")
        if (parts.size == 3) {
            val calendar = Calendar.getInstance().apply {
                set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
            }
            return when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "Sunday"
                Calendar.MONDAY -> "Monday"
                Calendar.TUESDAY -> "Tuesday"
                Calendar.WEDNESDAY -> "Wednesday"
                Calendar.THURSDAY -> "Thursday"
                Calendar.FRIDAY -> "Friday"
                Calendar.SATURDAY -> "Saturday"
                else -> "Unknown Day"
            }
        }
        return "Invalid Date Format"
    }

    private fun logUnicodeCharacters(text: String) {
        text.forEach { char ->
            Log.d("UnicodeLog", "Character: '$char', Unicode: U+${char.code.toString(16).uppercase()}")
        }
    }

    private fun parseOpeningHours(openingHours: String): Map<String, Pair<LocalTime, LocalTime>> {
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        val dayHoursMap = mutableMapOf<String, Pair<LocalTime, LocalTime>>()

        // แยกข้อมูลแต่ละวันด้วยการตัดบรรทัดใหม่
        val days = openingHours.split("\n")

        for (day in days) {
            // แทนที่ Unicode พิเศษที่อาจทำให้แยกข้อความผิดพลาด
            val sanitizedDay = day
                .replace("\u00A0", " ")       // แทนที่ non-breaking space ด้วย space ปกติ
                .replace("\u202F", " ")       // แทนที่ narrow no-break space
                .replace("\u2009", " ")       // แทนที่ thin space
                .replace("\u2013", "-")       // แทนที่ en dash ด้วย hyphen ปกติ
            logUnicodeCharacters(sanitizedDay)

            if (sanitizedDay.contains("Open 24 hours", ignoreCase = true)) {
                val dayName = sanitizedDay.split(":")[0].trim()
                dayHoursMap[dayName] = Pair(LocalTime.MIDNIGHT, LocalTime.MIDNIGHT)
                Log.d("ParseCheck", "$dayName: Open 24 hours")
            } else {
                val parts = sanitizedDay.split(": ", " - ")
                Log.d("ParseCheck", "Parts: $parts")

                if (parts.size == 3) {
                    val dayName = parts[0].trim()
                    val openTime = LocalTime.parse(parts[1].trim(), formatter)
                    val closeTime = LocalTime.parse(parts[2].trim(), formatter)
                    dayHoursMap[dayName] = Pair(openTime, closeTime)
                } else {
                    Log.d("ParseCheck", "Incorrect format for line: $sanitizedDay")
                }
            }
        }

        Log.d("parseOpeningHours", "Parsed dayHoursMap: $dayHoursMap")
        return dayHoursMap
    }


    private fun isPlaceOpenByDay(dayOfWeek: String, placeInfo: PlaceInfo): Boolean {
        val openingHours = placeInfo.openingHours

        val dayOpeningHours = openingHours.lines().find { it.startsWith(dayOfWeek) } ?: run {
            Log.d("Opening Hours Check", "No opening hours entry found for $dayOfWeek.")
            return false
        }

        Log.d("Opening Hours Check", "Opening hours for $dayOfWeek: $dayOpeningHours")

        val cleanedTimeRange = dayOpeningHours.replace("–", "-").replace("—", "-").replace(" ", "")

        val isOpen = cleanedTimeRange.contains("Open24hours") || cleanedTimeRange.contains("-")
        Log.d("Opening Hours Check", "Is open on $dayOfWeek: $isOpen")

        return isOpen
    }




}
