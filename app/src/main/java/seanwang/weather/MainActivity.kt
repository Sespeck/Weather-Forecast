package seanwang.weather

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.ScriptGroup
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject
import seanwang.weather.adapter.WeatherAdapter
import seanwang.weather.databinding.ActivityMainBinding
import seanwang.weather.model.Weather
import java.io.IOError
import java.io.IOException
import java.util.*
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val myDataset: MutableList<Weather> =  arrayListOf()
    lateinit var recyclerView: RecyclerView
    private val PERMISSION_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        // Full Screen
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        setContentView(binding.root)

        recyclerView = findViewById<RecyclerView>(R.id.weather_recycler_view)
        recyclerView.adapter = WeatherAdapter(this, myDataset)
        recyclerView.setHasFixedSize(true)

        val locationManager:LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ContextCompat.checkSelfPermission(this@MainActivity,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSION_CODE)
        }
        val location: Location? = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        val cityName: String = getCityName(location?.longitude ?: 0.0, location?.latitude ?: 0.0 )
        getWeatherInfo(cityName,location?.longitude ?: 0.0, location?.latitude ?: 0.0)
        binding.search.setOnClickListener {
            val city = binding.editCity.text.toString()
            if (city.isEmpty()){
                Toast.makeText(this@MainActivity, "Please Enter City Name",Toast.LENGTH_SHORT).show()
            }
            else{
                binding.cityName.text = city
                getWeatherInfo(city)
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE){
            if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this, "Please grant the permissions", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    private fun getCityName(longitude: Double,  latitude:Double): String {
        var cityName = "Not found"
        val gcd = Geocoder(this, Locale.getDefault())
        try {
            val addresses: List<Address> = gcd.getFromLocation(latitude, longitude, 10)
            for (adr in addresses){
                val city: String = adr.locality
                if (!city.equals("")){
                    cityName = city
                    Log.d("getCityName", cityName)
                }
                else{
                    Log.d("getCityName", "City not found")
                    Toast.makeText(this,"User City Not Found ...", Toast.LENGTH_SHORT).show()
                }

            }

        }catch (e: IOException){
            e.printStackTrace()
        }
        return cityName
    }
    private fun getWeatherInfo( cityName: String){
        val url:String = "https://api.weatherapi.com/v1/forecast.json?key=797da4cf2b63486ca5025620221207&q="+cityName+"&days=1&aqi=yes&alerts=yes"
        Log.d("getWeatherInfo1", cityName)
        getWeatherInfobyURL(url)
    }
    private fun getWeatherInfo( cityName: String, latitude: Double, longitude: Double) {
        binding.cityName.text = cityName
        Log.d("getWeatherInfo2", cityName+ latitude+ longitude)
        val url:String = "https://api.weatherapi.com/v1/forecast.json?key=797da4cf2b63486ca5025620221207&q="+latitude+","+longitude+"&days=1&aqi=yes&alerts=yes"
        getWeatherInfobyURL(url)
    }
    private fun getWeatherInfobyURL(url:String){
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)
        Log.d("getWeatherInfobyURL", url)
        val request = JsonObjectRequest(Request.Method.GET, url, null, {
                response ->try {
            binding.loadingBar.visibility = View.GONE
            binding.relativeLayout1.visibility = View.VISIBLE
            myDataset.clear()
            Log.d("getWeatherInfobyURL", "myDataset.clear()")
            val temperature = response.getJSONObject("current").getString("temp_c")
            binding.temperature.text = temperature + "°C"
            val isDay = response.getJSONObject("current").getInt("is_day")
            if(isDay == 1){
                // Morning
                Picasso.get().load("https://images.unsplash.com/photo-1567941085898-d5bf3ff007ad?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=764&q=80").into(binding.background)
                window.setStatusBarColor(ContextCompat.getColor(this,R.color.status_bar_day));
            }else{
                Picasso.get().load("https://images.unsplash.com/photo-1513628253939-010e64ac66cd?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=687&q=80").into(binding.background)
                window.setStatusBarColor(ContextCompat.getColor(this,R.color.status_bar_night));
            }
            val condition = response.getJSONObject("current").getJSONObject("condition").getString("text")
            binding.condition.text = condition
            val icon = response.getJSONObject("current").getJSONObject("condition").getString("icon")
            Picasso.get().load("https:"+icon).resize(256, 256) .into(binding.icon)

            val forecastObject = response.getJSONObject("forecast")
            val forecast0 = forecastObject.getJSONArray("forecastday").getJSONObject(0);
            val hourArray = forecast0.getJSONArray("hour")

            var count = 0
            repeat(hourArray.length()){
                val hourObj = hourArray.getJSONObject(count)
                val time_f = hourObj.getString("time")
                val temp_f = hourObj.getString("temp_c")
                val icon_f = hourObj.getJSONObject("condition").getString("icon")
                val wind_f = hourObj.getString("wind_kph")
                myDataset.add(Weather(time_f,temp_f,icon_f,wind_f))
                count++
            }
            this.recyclerView.adapter?.notifyDataSetChanged()

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        }, { error ->
            run {
                error.printStackTrace()
                Toast.makeText(this, "Please enter valid city name", Toast.LENGTH_SHORT).show()
            }
        })
        requestQueue?.add(request)
    }

}