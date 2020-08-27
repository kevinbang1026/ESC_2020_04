package com.example.esc_2020_04

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private var startup = true

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_main)
        transparentStatusAndNavigation()

        //Toast.makeText(this, "Updating location...", Toast.LENGTH_SHORT).show()

        val intent = intent

        //val loc = getLoc()
        humidity.text = intent.getStringExtra("humid")
        wind.text = intent.getStringExtra("wind")
        cloud.text = intent.getStringExtra("cloud")
        location.text = intent.getStringExtra("location")
        temperature.text = intent.getStringExtra("temp")

        when (intent.getIntExtra("weath", 800)) {
            in 200..232 -> stormy()
            in 300..531 -> rainy()
            in 600..622 -> snowy()
            800 -> sunny()
            else -> cloudy()
        }

        refresh.setOnClickListener {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
            Toast.makeText(this, "Updating location...", Toast.LENGTH_SHORT).show()

            val loc = getLoc()

            volley(loc)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()

        if (!startup) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)

            Toast.makeText(this, "Updating location...", Toast.LENGTH_SHORT).show()

            val loc = getLoc()

            volley(loc)
        }
        startup = false

    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun volley(loc: Location?) {
        var reqQue = Volley.newRequestQueue(this)

        val lat = loc?.latitude
        val lon = loc?.longitude

        val url: String

        if (loc != null) {
            url = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=f474f8fb405de7461172e1ee260a10d8&units=metric"
        } else {
            url = "https://api.openweathermap.org/data/2.5/weather?q=Seoul&appid=f474f8fb405de7461172e1ee260a10d8&units=metric"
        }

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    humidity.text = response.getJSONObject("main").getInt("humidity").toString() + "%"
                    wind.text = response.getJSONObject("wind").getDouble("speed").toString() + "m/s"
                    cloud.text = response.getJSONObject("clouds").getInt("all").toString() + "%"
                    location.text = response.getString("name")
                    temperature.text = response.getJSONObject("main").getInt("temp").toString() + "°C"

                    when (response.getJSONArray("weather").getJSONObject(0).getInt("id")) {
                        in 200..232 -> stormy()
                        in 300..531 -> rainy()
                        in 600..622 -> snowy()
                        800 -> sunny()
                        else -> cloudy()
                    }

                    if (loc == null) Toast.makeText(
                        this,
                        "Can't find your location",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: JSONException) {
                    Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }) { error -> Toast.makeText(this, error.localizedMessage, Toast.LENGTH_SHORT).show()
        }

        reqQue.add(request)
    }

    private fun transparentStatusAndNavigation() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        setWindowFlag(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, false
        )
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }

    private fun setWindowFlag(bits: Int, on: Boolean) {
        val win = window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
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

        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
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

    private var locationListener = object : LocationListener{
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun sunny(){
        when (Random.nextInt(4)) {
            0 -> {
                background.setImageResource(R.drawable.sunny01)
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
            }
            1 -> background.setImageResource(R.drawable.sunny02)
            2 -> background.setImageResource(R.drawable.sunny03)
            else -> background.setImageResource(R.drawable.sunny04)
        }
    }

    private fun cloudy(){
        when (Random.nextInt(3)) {
            0 -> {
                background.setImageResource(R.drawable.cloudy01)
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
            }
            1 -> background.setImageResource(R.drawable.cloudy02)
            2 -> background.setImageResource(R.drawable.cloudy03)
        }
    }

    private fun rainy(){
        when (Random.nextInt(5)) {
            0 -> background.setImageResource(R.drawable.rainy01)
            1 -> background.setImageResource(R.drawable.rainy02)
            2 -> background.setImageResource(R.drawable.rainy03)
            3 -> background.setImageResource(R.drawable.rainy04)
            4 -> background.setImageResource(R.drawable.rainy05)
        }
    }

    private fun snowy(){
        when (Random.nextInt(3)) {
            0 -> background.setImageResource(R.drawable.snowy01)
            1 -> background.setImageResource(R.drawable.snowy02)
            2 -> {
                background.setImageResource(R.drawable.snowy03)
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
            }
        }
    }

    private fun stormy(){
        background.setImageResource((R.drawable.stomy01))
    }

}