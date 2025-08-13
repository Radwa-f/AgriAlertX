package ma.ensaj.agri_alert.view.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensaj.agri_alert.R
import ma.ensaj.agri_alert.model.CropAnalysis
import ma.ensaj.agri_alert.model.WeatherAnalysisAutoRequest
import ma.ensaj.agri_alert.network.RetrofitClient
import ma.ensaj.agri_alert.util.SharedPreferencesHelper
import java.util.Locale
import java.util.regex.Pattern

import ma.ensaj.agri_alert.view.adapters.CropAnalysisAdapter

class CropAnalysisFragment : Fragment() {

    private lateinit var cropAnalysisRecyclerView: RecyclerView
    private lateinit var cropAnalysisAdapter: CropAnalysisAdapter
    private lateinit var searchEditText: EditText

    private val cropAnalyses = mutableListOf<Pair<String, CropAnalysis>>() // Original data
    private val filteredCropAnalyses = mutableListOf<Pair<String, CropAnalysis>>() // Filtered data

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crop_analysis, container, false)

        cropAnalysisRecyclerView = view.findViewById(R.id.rv_crop_analysis)
        searchEditText = view.findViewById(R.id.et_search)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView
        cropAnalysisAdapter = CropAnalysisAdapter(filteredCropAnalyses)
        cropAnalysisRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        cropAnalysisRecyclerView.adapter = cropAnalysisAdapter

        // Fetch data and populate UI
        fetchCropAnalysisData()

        // Setup Search Functionality
        setupSearchBar()
    }

    private fun setupSearchBar() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterCrops(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterCrops(query: String) {
        val lowerCaseQuery = query.lowercase()

        // Filter the cropAnalyses list
        val filteredList = cropAnalyses.filter {
            it.first.lowercase().contains(lowerCaseQuery)
        }

        // Update the filtered data list
        filteredCropAnalyses.clear()
        filteredCropAnalyses.addAll(filteredList)

        // Notify the adapter about the changes
        cropAnalysisAdapter.notifyDataSetChanged()
    }

    private fun fetchCropAnalysisData() {
        val token = SharedPreferencesHelper.getToken(requireContext())
        if (token.isNullOrEmpty()) {
            Log.e("ProfileAPI", "Authorization token is missing")
            return
        }

        lifecycleScope.launch {
            try {
                // Fetch user profile to get crops
                val profileResponse = RetrofitClient.instance.getUserProfile("Bearer $token")
                if (profileResponse.isSuccessful) {
                    val userProfile = profileResponse.body()
                    if (userProfile != null) {
                        val userCrops = userProfile.crops
                        Log.d("ProfileAPI", "Fetched Crops: $userCrops")

                        // Fetch weather data
                        val latitude = userProfile.location?.latitude
                        val longitude = userProfile.location?.longitude

                        val analysisResponse = RetrofitClient.cropAnalysisBackend.analyzeAuto(
                            WeatherAnalysisAutoRequest(
                                latitude = latitude,
                                longitude = longitude,
                                cropNames = userCrops
                            )
                        )

                        if (analysisResponse.isSuccessful) {
                                val cropAnalysisData = analysisResponse.body()
                                Log.d("WeatherAPI", "Weather Analysis Response: $cropAnalysisData")
                                cropAnalyses.clear()
                                filteredCropAnalyses.clear()
                                if (cropAnalysisData != null) {
                                    cropAnalyses.addAll(cropAnalysisData.cropAnalyses.toList())
                                    filteredCropAnalyses.addAll(cropAnalyses) // Initialize filtered list
                                    cropAnalysisAdapter.notifyDataSetChanged()
                                }

                            } else {
                                Log.e("WeatherAPI", "Error fetching analysis: ${analysisResponse.errorBody()?.string()}")
                            }
                        } else {
                            Log.e("ProfileAPI", "User location is missing for weather analysis")
                        }
                    } else {
                        Log.e("ProfileAPI", "User profile is null")
                    }

            } catch (e: Exception) {
                Log.e("WeatherAPI", "Exception occurred while fetching weather analysis: ${e.message}")
            }
        }
    }
}
