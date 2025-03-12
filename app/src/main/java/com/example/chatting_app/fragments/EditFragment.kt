package com.example.chatting_app.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.chatting_app.databinding.FragmentEditBinding
import com.example.chatting_app.dataclass.User
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.android.material.snackbar.Snackbar
import java.io.IOException
import java.util.Locale

@Suppress("DEPRECATION")
class EditFragment : Fragment() {

    private lateinit var binding: FragmentEditBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var currentLat: Double? = null
    private var currentLng: Double? = null
    private var currentAddress: String? = null

    // Activity Result Launcher for permission request
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission is required", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditBinding.inflate(inflater, container, false)

        // Initialize Firebase and Location Services
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Load user data and request location
        loadUserData()
        requestUserLocation()

        binding.saveBorder.setOnClickListener {
            updateUserData()
        }

        return binding.root
    }

    // Load user data from Firebase
    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        database.child("users").child(uid).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    binding.edtName.setText(user.name)
                    binding.edtLoc.setText(user.address)
                }
            }
        }.addOnFailureListener {
            Log.e("EditFragment", "Failed to load user data: ${it.message}")
        }
    }

    // Request location permission and get current location
    private fun requestUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getCurrentLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLat = location.latitude
                currentLng = location.longitude
                convertLatLngToAddress(currentLat!!, currentLng!!)
            } else {
                Snackbar.make(binding.root, "Failed to get location", Snackbar.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Log.e("EditFragment", "Failed to get location: ${e.message}")
            Snackbar.make(binding.root, "Failed to get location: ${e.message}", Snackbar.LENGTH_SHORT).show()
        }
    }

    // Convert latitude and longitude to address using Geocoder
    @SuppressLint("SetTextI18n")
    private fun convertLatLngToAddress(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                currentAddress = addresses[0].getAddressLine(0)
                binding.edtLoc.setText(currentAddress)
            } else {
                binding.edtLoc.setText("Unable to fetch address")
            }
        } catch (e: IOException) {
            Log.e("EditFragment", "Geocoder error: ${e.message}")
            binding.edtLoc.setText("Failed to convert location")
        }
    }

    // Update user data in Firebase
    private fun updateUserData() {
        val name = binding.edtName.text.toString().trim()
        val address = binding.edtLoc.text.toString().trim()

        if (name.isEmpty()) {
            binding.edtName.error = "Name cannot be empty"
            return
        }
        if (address.isEmpty()) {
            binding.edtLoc.error = "Address cannot be empty"
            return
        }

        val uid = auth.currentUser?.uid ?: return
        val email = auth.currentUser?.email ?: ""

        // Create updated user object
        val updatedUser = User(
            name = name,
            address = address,
            city = currentAddress ?: "",
            email = email,
            latitude = currentLat ?: 0.0,
            longitude = currentLng ?: 0.0
        )

        // Update data in Firebase
        database.child("users").child(uid).setValue(updatedUser)
            .addOnSuccessListener {
                Snackbar.make(binding.root, "Profile updated successfully", Snackbar.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Snackbar.make(binding.root, "Failed to update profile: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
    }

}
