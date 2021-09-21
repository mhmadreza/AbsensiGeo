package com.mr.attendanceapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.skyfishjy.library.RippleBackground
//import com.google.firebase.database.FirebaseDatabase
//import kotlinx.android.synthetic.main.activity_main.*
//import kotlinx.android.synthetic.main.layout_dialog_form.view.*
import java.lang.Math.toRadians
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val ID_LOCATION_PERMISSION = 0
    }

    /**
     * Update code berikut ini, agar mendapakan lokasi kita saat ini, di update secara berkala
     * sesuai interval yang akan kita kasih nantinya
     * TODO untuk menambahkan beberapa update code, ikuti step by step dari TODO
     * */

    /**
     * TODO 1. Tambahkan variabel berikut ini
     * */
    private lateinit var locationRequest: LocationRequest
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**
         * TODO 2. Tambahkan function initLocation()
         * */
        initLocation()
        checkPermissionLocation()
        onClick()
    }

    /**
     * TODO 3. Tambahkan function initLocation() dan tambahkan kodingan dibawah ini
     * */
    private fun initLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        /**
         * interval berfungsi untuk melakukan update lokasi secara berkala sesuai dengan waktu yang diberikan
         * fastInterval berfungsi untuk melakukan
         * note: waktu menggunakan milisecond : 1000ms = 1 detik
         * */
        locationRequest = LocationRequest.create().apply {
            interval = 1000 * 5
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>, 
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ID_LOCATION_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED ||
                grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, "Berhasil di ijinkan", Toast.LENGTH_SHORT).show()

                if (!isLocationEnabled()) {
                    Toast.makeText(this, "Please turn on your location", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            } else {
                Toast.makeText(this, "Gagal di ijinkan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissionLocation() {
        if (checkPermission()) {
            if (!isLocationEnabled()) {
                Toast.makeText(this, "Please turn on your location", Toast.LENGTH_SHORT).show()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        } else {
            requestPermission()
        }
    }

    private fun checkPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        ) {
            return true
        }
        return false
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            ID_LOCATION_PERMISSION
        )
    }

    private fun onClick() {
        val fab = findViewById<FloatingActionButton>(R.id.fabCheckIn)
        fab.setOnClickListener {
            loadScanLocation()
            /**
             * Old Code
             * */
//            Handler().postDelayed({
//                getLastLocation()
//            },4000)
            /**
             * NEW CODE
             * For handler change to this
             * */
            Handler(Looper.getMainLooper()).postDelayed({
                getLastLocation()
            }, 2000)
        }
    }

    private fun loadScanLocation() {
        val rb = findViewById<RippleBackground>(R.id.rippleBackground)
        val tvScanning = findViewById<TextView>(R.id.tvScanning)
        val tvCheckInSuccess = findViewById<TextView>(R.id.tvCheckInSuccess)

        rb.startRippleAnimation()
        tvScanning.visibility = View.VISIBLE
        tvCheckInSuccess.visibility = View.GONE
    }

    private fun stopScanLocation() {
        val rb = findViewById<RippleBackground>(R.id.rippleBackground)
        val tvScanning = findViewById<TextView>(R.id.tvScanning)
        rb.stopRippleAnimation()
        tvScanning.visibility = View.GONE
    }

    private fun getLastLocation() {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                /**
                 * OLD CODE
                 * */
//                LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener {location ->
//                    val currentLat = location.latitude
//                    val currentLong = location.longitude
//
//                    val distance = calculateDistance(
//                        currentLat,
//                        currentLong,
//                        getAddresses()[0].latitude,
//                        getAddresses()[0].longitude) * 1000
//
//                    if (distance < 10.0){
//                        showDialogForm()
//                    }else{
//                        tvCheckInSuccess.visibility = View.VISIBLE
//                        tvCheckInSuccess.text = "Out of range"
//                    }
//
//                    stopScanLocation()
//                }
                /**
                 * NEW CODE
                 * TODO 4. tambahkan code dibawah ini
                 * */
                val locationCallBack = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                        val location = locationResult.lastLocation
                        val currentLat = location.latitude
                        val currentLong = location.longitude

                        /**
                         * Untuk lokasi tujuan, bisa itu kantor atau sekolah, dsb.
                         * bisa menggunakan function getAddress()
                         * ataupun bisa menggunakan coordinate
                         * */
                        // Tambahkan code dibawah ini jika ingin menggunakan geocode
//                        val destinationLat = getAddresses()[0].latitude
//                        val destinationLon = getAddresses()[0].longitude

                        /**
                         * Jika ingin menggunakan coordinate bisa tambahkan code dibawah ini
                         * */
                        // Ini didapat dari google maps, coordinate berikut ini, merupakan lokasi kantor codepolitan
                        val destinationLat = -6.225313
                        val destinationLon = 106.828162

                        val distance = calculateDistance(
                            currentLat,
                            currentLong,
                            destinationLat,
                            destinationLon
                        ) * 1000

                        Log.d("MainActivity", "[onLocationResult] - $distance")
                        if (distance < 5.0) {
                            showDialogForm()
                            Toast.makeText(this@MainActivity, "SUCCESSS", Toast.LENGTH_SHORT).show()
                            //startActivity(Intent(this, SuccessPage::class))
                            /*val pindahKeSuccesPage = Intent(this@MainActivity, SuccessPage::class.java)
                            startActivity(pindahKeSuccesPage)*/

                        } else {
                            val tvCheckInSuccess = findViewById<TextView>(R.id.tvCheckInSuccess)

                            //tvCheckInSuccess.visibility = View.VISIBLE
                            //tvCheckInSuccess.text = "Lokasi tidak dapat Dijangkau"

                            val pindahKeFailedPage = Intent(this@MainActivity, FailedPage::class.java)
                            startActivity(pindahKeFailedPage)
                        }

                        /**
                         * Agar tidak terjadi pengualangan untuk menampilkan dialog
                         * kita dapat menghentikan scanning lokasi kita saat ini
                         * dengan cara tambahkan kodingan berikut ini
                         * */
                        // Menghentikan scan lokasi
                        fusedLocationProviderClient?.removeLocationUpdates(this)
                        //Menghentikan animasi ripple background
                        stopScanLocation()
                    }
                }
                fusedLocationProviderClient?.requestLocationUpdates(
                    locationRequest,
                    locationCallBack,
                    Looper.getMainLooper()
                )
            } else {
                Toast.makeText(this, "Please turn on your location", Toast.LENGTH_SHORT).show()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        } else {
            requestPermission()
        }
    }

    private fun showDialogForm() {
        val dialogForm = LayoutInflater.from(this).inflate(R.layout.layout_dialog_form, null)
        //val etName = findViewById<EditText>(R.id.etName)
        AlertDialog.Builder(this)
            .setView(dialogForm)
            .setCancelable(false)
            .setPositiveButton("Submit") { dialog, _ ->

                val name = dialogForm.findViewById<EditText>(R.id.etName).text.toString()
                Toast.makeText(this, "Berhasil Absen dengan nama : $name", Toast.LENGTH_SHORT).show()
                inputDataToFireBase(name)
                //inputDataToFireBase(name)
                dialog.dismiss()

            }
            .setNegativeButton("Cancel") { dialog, _ ->
                Toast.makeText(this, "Gagal melakukan Absen", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .show()
    }

    private fun inputDataToFireBase(name: String) {
        //lateinit var database: DatabaseReference
        val user = User(name, getCurrentDate())

        val databaseReference = FirebaseDatabase.getInstance()
        val attendanceRef = databaseReference.getReference("log_attendance")

        attendanceRef.child(name).setValue(user)
            .addOnSuccessListener {
                val tvCheckInSuccess = findViewById<TextView>(R.id.tvCheckInSuccess)
                tvCheckInSuccess.visibility = View.VISIBLE
                tvCheckInSuccess.text = "Absen Success"
            }
            .addOnFailureListener {
                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getCurrentDate(): String {
        val currentTime = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        return dateFormat.format(currentTime)
    }

    /*NOTE jika ingin menggunakan geocode, tambahkan function berikut ini*/

    private fun getAddresses(): List<Address> {
        val destinationPlace = "Bisnis Integrasi Global"
        val geocode = Geocoder(this, Locale.getDefault())
        return geocode.getFromLocationName(destinationPlace, 100)
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 10.0 // in kilometers
        //val r = 6372.8 // in kilometers

        val radiansLat1 = toRadians(lat1)
        val radiansLat2 = toRadians(lat2)
        val dLat = toRadians(lat2 - lat1)
        val dLon = toRadians(lon2 - lon1)
        return 2 * r * asin(
            sqrt(
                sin(dLat / 2).pow(2.0) + sin(dLon / 2).pow(2.0) * cos(radiansLat1) * cos(
                    radiansLat2
                )
            )
        )
    }
}