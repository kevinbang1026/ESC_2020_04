package com.example.esc_2020_04

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, MainActivity::class.java)

        var reqQue = Volley.newRequestQueue(this)

        val loc = getLoc()
        val lat = loc?.latitude
        val lon = loc?.longitude

        val url = if (loc != null) {
            "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=f474f8fb405de7461172e1ee260a10d8&units=metric"
        } else {
            "https://api.openweathermap.org/data/2.5/weather?q=Seoul&appid=f474f8fb405de7461172e1ee260a10d8&units=metric"
        }

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    intent.putExtra("location", response.getString("name"))
                    intent.putExtra("temp", response.getJSONObject("main").getInt("temp").toString() + "°C")
                    intent.putExtra("humid", response.getJSONObject("main").getInt("humidity").toString() + "%")
                    intent.putExtra("cloud", response.getJSONObject("clouds").getInt("all").toString() + "%")
                    intent.putExtra("wind", response.getJSONObject("wind").getDouble("speed").toString() + "m/s")
                    intent.putExtra("weath", response.getJSONArray("weather").getJSONObject(0).getInt("id"))

                    if (loc == null) Toast.makeText(this, "Can't find your location", Toast.LENGTH_SHORT).show()

                    startActivity(intent)
                    finish()
                } catch (e: JSONException) {
                    Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }) { error -> Toast.makeText(this, error.localizedMessage, Toast.LENGTH_SHORT).show()
        }
        reqQue.add(request)

    }

    @SuppressLint("MissingPermission")
    private fun getLoc(): Location? {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        var time = 0
        while (time < 200) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
            time++
        }
        lm.removeUpdates(locationListener)

        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        } else {
            when {
                isNetworkEnabled && lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null-> {
                    return lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }
                isGpsEnabled -> {
                    return lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                }
                else -> {}
            }
        }
        return null
    }

    private var locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location?) {
            val latitude = location!!.latitude
            val longitude = location.longitude
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

        override fun onProviderEnabled(provider: String?) {
        }

        override fun onProviderDisabled(provider: String?) {
        }

    }
}