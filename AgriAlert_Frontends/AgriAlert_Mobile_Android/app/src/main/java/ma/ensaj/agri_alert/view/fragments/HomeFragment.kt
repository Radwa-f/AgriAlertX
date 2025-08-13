package ma.ensaj.agri_alert.view.fragments

import android.content.Intent
import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.applandeo.materialcalendarview.CalendarDay
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensaj.agri_alert.AlertsActivity
import ma.ensaj.agri_alert.R
import ma.ensaj.agri_alert.databinding.FragmentHomeBinding
import ma.ensaj.agri_alert.model.WeatherResponse
import ma.ensaj.agri_alert.ChatBotActivity
import ma.ensaj.agri_alert.CropsDetailsActivity
import ma.ensaj.agri_alert.RemindersActivity
import ma.ensaj.agri_alert.WeatherActivity
import ma.ensaj.agri_alert.model.Alert
import ma.ensaj.agri_alert.model.Crop
import ma.ensaj.agri_alert.model.CropAnalysisResponse
import ma.ensaj.agri_alert.model.WeatherAnalysisAutoRequest
import ma.ensaj.agri_alert.network.RetrofitClient
import ma.ensaj.agri_alert.util.SharedPreferencesHelper
import ma.ensaj.agri_alert.view.adapters.AlertsAdapter
import ma.ensaj.agri_alert.view.adapters.CropsAdapter
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var maxRainfall: Double = 0.0
    private var minRainfall: Double = 0.0



    private var aWeatherResponse: WeatherResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermission()
        }

        // Navigate to WeatherActivity
        binding.weatherCard.setOnClickListener {
            val token = SharedPreferencesHelper.getToken(requireContext())
            if (!token.isNullOrEmpty()) {
                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.instance.getUserProfile("Bearer $token")
                        if (response.isSuccessful) {
                            val userProfile = response.body()
                            if (userProfile != null && userProfile.location != null) {
                                val latitude = userProfile.location.latitude
                                val longitude = userProfile.location.longitude

                                Log.d("HomeFragment", "Passing coordinates: Lat = $latitude, Lng = $longitude")
                                val intent = Intent(requireContext(), WeatherActivity::class.java)
                                intent.putExtra("latitude", latitude)
                                intent.putExtra("longitude", longitude)
                                startActivity(intent)
                            } else {
                                Log.e("HomeFragment", "User location is missing")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("HomeFragment", "Error fetching user profile: ${e.message}")
                    }
                }
            } else {
                Log.e("HomeFragment", "Authorization token is missing")
            }
        }


        val crops = listOf(
            Crop("Wheat", "Growing", R.drawable.ic_wheat),
            Crop("Corn", "Ready to Harvest", R.drawable.ic_corn),
            Crop("Rice", "Planting", R.drawable.ic_rice)
        )
        binding.rvCrops.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        binding.materialCalendarView.setOnCalendarDayClickListener(object : com.applandeo.materialcalendarview.listeners.OnCalendarDayClickListener {
            override fun onClick(calendarDay: CalendarDay) {
                val clickedDate = calendarDay.calendar
                val formattedDate = String.format(
                    "%04d-%02d-%02d",
                    clickedDate.get(Calendar.YEAR),
                    clickedDate.get(Calendar.MONTH) + 1, // Months are 0-indexed
                    clickedDate.get(Calendar.DAY_OF_MONTH)
                )

                Log.d("CalendarView", "Selected date: $formattedDate")

                // Navigate to RemindersActivity with the selected date
                val intent = Intent(requireContext(), RemindersActivity::class.java)
                intent.putExtra("selected_date", formattedDate)
                startActivity(intent)
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {
            // Fetch weather data and wait for it to complete
            fetchWeatherData()

            Log.d("Data", "Response: $aWeatherResponse")
            // Only proceed to fetch user crops after weather data is fetched
            fetchUserCrops()

            fetchWeatherAnalysis()
            // Fetch and display alerts

        }

        binding.rvDailyInsights.setLayoutManager(
            LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        )

        // Navigate to AlertsActivity
        binding.rvDailyInsights.setOnClickListener {
            val intent = Intent(requireContext(), AlertsActivity::class.java)
            startActivity(intent)
        }



    }

    private fun fetchWeatherData() {
        val token = SharedPreferencesHelper.getToken(requireContext())
        if (token.isNullOrEmpty()) {
            Log.e("ProfileAPI", "Authorization token is missing")
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getUserProfile("Bearer $token")
                if (response.isSuccessful) {
                    val userProfile = response.body()
                    if (userProfile != null && userProfile.location != null) {
                        val latitude = userProfile.location.latitude
                        val longitude = userProfile.location.longitude

                        fetchCityName(latitude, longitude)
                        val weatherResponse = withContext(Dispatchers.IO) {
                            RetrofitClient.weatherBackend.getWeather(latitude, longitude)
                        }


                        Log.d("WeatherAPI", "Response: $weatherResponse")
                        updateWeatherCard(weatherResponse)
                        processRainfallForNextDay(weatherResponse)
                        aWeatherResponse = weatherResponse
                        Log.d("Data", "Response: $aWeatherResponse")
                    } else {
                        Log.e("ProfileAPI", "User location is missing")
                    }
                } else {
                    Log.e("ProfileAPI", "Error fetching profile: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("WeatherAPI", "Error fetching weather data: ${e.message}")
            }
        }
    }


    private fun updateWeatherCard(weatherResponse: WeatherResponse) {
        val maxTemperature = weatherResponse.daily.temperatureMax[0]
        val minTemperature = weatherResponse.daily.temperatureMin[0]
        val precipitation = weatherResponse.daily.precipitationSum[0]
        val weatherCondition = when {
            precipitation > 0.0 -> "Rainy"
            maxTemperature > 25 -> "Sunny"
            else -> "Cloudy"
        }

        binding.tvWeatherCondition.text = weatherCondition
        binding.tvTemperatureDetails.text = "$minTemperature°C - $maxTemperature°C"

        val iconRes = when (weatherCondition) {
            "Sunny" -> R.drawable.ic_sunny
            "Rainy" -> R.drawable.ic_rainy
            "Cloudy" -> R.drawable.ic_cloudy
            else -> R.drawable.ic_weather_placeholder
        }
        binding.weatherIcon.setImageResource(iconRes)
    }

    private fun checkLocationPermission(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            false
        } else {
            true
        }
    }


    private fun fetchCityName(latitude: Double, longitude: Double) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(requireContext())
                val addressList = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addressList.isNullOrEmpty()) {
                    val cityName = addressList[0].locality ?: "Unknown City"
                    withContext(Dispatchers.Main) {
                        binding.tvForecastTitle.text = cityName
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        binding.tvForecastTitle.text = "Unknown City"
                    }
                }
            } catch (e: Exception) {
                Log.e("Geocoder", "Error fetching city name: ${e.message}")
                withContext(Dispatchers.Main) {
                    binding.tvForecastTitle.text = "Unknown City"
                }
            }
        }
    }


    private fun fetchUserCrops() {
        val token = SharedPreferencesHelper.getToken(requireContext())
        if (token.isNullOrEmpty()) {
            Log.e("ProfileAPI", "Authorization token is missing")
            return
        }

        lifecycleScope.launch {
            try {
                val profileResponse = RetrofitClient.instance.getUserProfile("Bearer $token")
                if (profileResponse.isSuccessful) {
                    val userProfile = profileResponse.body()
                    if (userProfile != null) {
                        val userCrops = userProfile.crops
                        Log.d("ProfileAPI", "Fetched Crops: $userCrops")

                        // Display crops immediately
                        displayCrops(userCrops)

                    } else {
                        Log.e("ProfileAPI", "User profile is null")
                    }
                } else {
                    Log.e("ProfileAPI", "Error fetching profile: ${profileResponse.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("ProfileAPI", "Exception occurred while fetching user profile: ${e.message}")
            }
        }
    }


    // 3) Use the coords you stored; ensure UI update on Main and make view visible
    private suspend fun fetchWeatherAnalysis() {
        val token = SharedPreferencesHelper.getToken(requireContext()) ?: return
        val profileResp = RetrofitClient.instance.getUserProfile("Bearer $token")
        val user = profileResp.body() ?: return
        val userCrops = user.crops

        // fallback if somehow coords missing
        val lat = latitude ?: user.location?.latitude ?: return
        val lon = longitude ?: user.location?.longitude ?: return

        val resp = RetrofitClient.cropAnalysisBackend.analyzeAuto(
            WeatherAnalysisAutoRequest(latitude = lat, longitude = lon, cropNames = userCrops)
        )

        if (resp.isSuccessful) {
            val data = resp.body()
            val alerts = data?.cropAnalyses?.values?.flatMap { it.alerts }.orEmpty()
            withContext(Dispatchers.Main) {
                if (alerts.isNotEmpty()) {
                    binding.rvDailyInsights.adapter = AlertsAdapter(alerts)
                    binding.rvDailyInsights.visibility = View.VISIBLE   // <- ensure visible
                } else {
                    binding.rvDailyInsights.visibility = View.GONE
                }
            }
        } else {
            Log.e("WeatherAPI", "analysis error: ${resp.errorBody()?.string()}")
        }
    }


    private fun displayCrops(userCrops: List<String>) {
        if (userCrops.isNotEmpty()) {
            val crops = userCrops.map { cropName ->
                Crop(
                    name = cropName,
                    status = "Unknown", // Placeholder status
                    imageRes = R.drawable.ic_crops // Placeholder image
                )
            }

            Log.d("Crops", "Displaying crops in RecyclerView: $crops")

            val cropsAdapter = CropsAdapter(crops, requireContext())
            binding.rvCrops.adapter = cropsAdapter
        } else {
            Log.d("Crops", "No crops found for the user.")
        }
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request the permission
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                false
            } else {
                // Permission already granted
                true
            }
        } else {
            // For devices below Android 13, no permission is needed
            true
        }
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d("Permissions", "Notification permission granted")
            } else {
                Log.e("Permissions", "Notification permission denied")
            }
        }

    private fun processRainfallForNextDay(weatherResponse: WeatherResponse) {
        val hourlyPrecipitation = weatherResponse.hourly.precipitation
        val hourlyTime = weatherResponse.hourly.time

        // Get the timestamps for the next day
        val nextDayStartIndex = hourlyTime.indexOfFirst { it.contains("T00:00") } + 24
        val nextDayEndIndex = nextDayStartIndex + 23 // 24 hours in a day

        if (nextDayStartIndex in hourlyPrecipitation.indices && nextDayEndIndex in hourlyPrecipitation.indices) {
            val nextDayPrecipitation = hourlyPrecipitation.subList(nextDayStartIndex, nextDayEndIndex + 1)

            maxRainfall = nextDayPrecipitation.maxOrNull() ?: 0.0
            minRainfall = nextDayPrecipitation.minOrNull() ?: 0.0

            Log.d("WeatherAPI", "Next Day Rainfall - Max: $maxRainfall, Min: $minRainfall")

            // You can now use maxRainfall and minRainfall for the weather analysis
        } else {
            Log.e("WeatherAPI", "Unable to calculate rainfall for the next day.")
        }
    }

    private fun fetchAndDisplayAlerts(cropAnalysisData: CropAnalysisResponse?) {
        if (cropAnalysisData != null) {
            Log.d("Alerts", "Using provided CropAnalysisResponse: $cropAnalysisData")

            // Extract all alerts from all crop analyses
            val alerts = cropAnalysisData.cropAnalyses.values.flatMap { it.alerts }

            // Log the number and details of alerts
            Log.d("Alerts", "Number of alerts extracted: ${alerts.size}")
            alerts.forEachIndexed { index, alert ->
                Log.d("Alerts", "Alert $index: $alert")
            }

            if (alerts.isNotEmpty()) {
                Log.d("Alerts", "Displaying alerts in RecyclerView")

                // Update RecyclerView with alerts
                val adapter = AlertsAdapter(alerts)
                binding.rvDailyInsights.layoutManager = LinearLayoutManager(
                    context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                binding.rvDailyInsights.adapter = adapter
            } else {
                Log.d("Alerts", "No alerts found to display")
            }
        } else {
            Log.d("Alerts", "Provided CropAnalysisResponse is null. No alerts to display.")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
