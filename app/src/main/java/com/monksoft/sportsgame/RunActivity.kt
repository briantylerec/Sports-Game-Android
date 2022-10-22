package com.monksoft.sportsgame

import android.content.ContentValues
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.monksoft.sportsgame.Utility.setHeightLinearLayout
import com.monksoft.sportsgame.databinding.ActivityRunBinding
import me.tankery.lib.circularseekbar.CircularSeekBar
import java.io.File

class RunActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private var user: String? = null
    private var idRun: String? = null

    private var centerLat: Double? = null
    private var centerLong: Double? = null

    private lateinit var binding : ActivityRunBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRunBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createMapFragment()
        loadDatas()
    }

    private fun createMapFragment(){
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(centerLat!!, centerLong!!),
                16f), 1000, null)
        loadLocations()
    }

    private fun loadLocations(){

        val collection = "locations/$user/$idRun"
        var point: LatLng
        val listPoints: Iterable<LatLng>
        listPoints = arrayListOf()
        listPoints.clear()

        val dbLocations: FirebaseFirestore = FirebaseFirestore.getInstance()
        dbLocations.collection(collection)
            .orderBy("time")
            .get()
            .addOnSuccessListener { documents ->

                for (docLocation in documents) {
                    val position = docLocation.toObject(Location::class.java)
                    //listPosition.add(position!!)
                    point = LatLng(position.latitude!!, position.longitude!!)
                    listPoints.add(point)
                }
                paintRun(listPoints)
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting locations: ", exception)
            }
    }

    private fun paintRun(listPosition:  Iterable<LatLng>){
        val polylineOptions = PolylineOptions()
            .width(25f)
            .color(ContextCompat.getColor(this, R.color.salmon_dark))
            .addAll(listPosition)

        val polyline = map.addPolyline(polylineOptions)
        polyline.startCap = RoundCap()
    }

    fun changeTypeMap(v: View){
        if (map.mapType == GoogleMap.MAP_TYPE_HYBRID){
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            binding.ivTypeMap.setImageResource(R.drawable.map_type_hybrid)
        } else {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            binding.ivTypeMap.setImageResource(R.drawable.map_type_normal)
        }
    }

    private fun loadDatas(){
        //RECEPCION DE PARAMETRO
        val bundle = intent.extras

        user = bundle?.getString("user")
        idRun = bundle?.getString("idRun")
        centerLat = bundle?.getDouble("centerLatitude")
        centerLong = bundle?.getDouble("centerLongitude")

        if (bundle?.getDouble("distanceTarget") == 0.0){
            setHeightLinearLayout(binding.lyCurrentLevel, 0)
        } else {

            val levelText = "${getString(R.string.level)} ${bundle?.getString("image_level")!!.subSequence(6,7).toString()}"
            binding.tvNumberLevel.text = levelText

            when (bundle.getString("image_level")){
                "level_1" -> binding.ivCurrentLevel.setImageResource(R.drawable.level_1)
                "level_2" -> binding.ivCurrentLevel.setImageResource(R.drawable.level_2)
                "level_3" -> binding.ivCurrentLevel.setImageResource(R.drawable.level_3)
                "level_4" -> binding.ivCurrentLevel.setImageResource(R.drawable.level_4)
                "level_5" -> binding.ivCurrentLevel.setImageResource(R.drawable.level_5)
                "level_6" -> binding.ivCurrentLevel.setImageResource(R.drawable.level_6)
                "level_7" -> binding.ivCurrentLevel.setImageResource(R.drawable.level_7)
            }

            val csbDistanceLevel = findViewById<CircularSeekBar>(R.id.csbDistanceLevel)
            csbDistanceLevel.max = bundle.getDouble("distanceTarget").toFloat()
            csbDistanceLevel.progress = bundle.getDouble("distanceTotal").toFloat()

            val td = bundle.getDouble("distanceTotal")
            var td_k: String = td.toString()
            if (td > 1000) td_k = (td/1000).toInt().toString() + "K"
            val ld = bundle.getDouble("distanceTotal").toDouble()
            var ld_k: String = ld.toInt().toString()
            if (ld > 1000) ld_k = (ld/1000).toInt().toString() + "K"

            binding.tvTotalDistance.text = "${td_k}/${ld_k} kms"
            //tvTotalDistance.text = "${totalsSelectedSport.totalDistance!!}/${levelSelectedSport.DistanceTarget!!} kms"

            val porcent = (bundle.getDouble("distanceTotal") *100 / bundle.getDouble("distanceTarget")).toInt()
            binding.tvTotalDistanceLevel.text = "$porcent%"

            binding.csbRunsLevel.max = bundle.getInt("runsTarget").toString().toFloat()
            binding.csbRunsLevel.max = bundle.getInt("runsTotal").toString().toFloat()

            val tvTotalRunsLevel = findViewById<TextView>(R.id.tvTotalRunsLevel)
            tvTotalRunsLevel.text = "${bundle.getInt("runsTotal")}/${bundle.getInt("runsTarget")}"
        }

        if (bundle.getInt("countPhotos") > 0){
            val ivPicture = findViewById<ImageView>(R.id.ivPicture)
            val path = bundle.getString("lastimage")

            val storageRef = FirebaseStorage.getInstance().reference.child(path!!) //.jpg")
            val localfile = File.createTempFile("tempImage", "jpg")
            storageRef.getFile(localfile).addOnSuccessListener {

                val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
                ivPicture.setImageBitmap(bitmap)

                val metaRef = FirebaseStorage.getInstance().reference.child(path)
                metaRef.metadata.addOnSuccessListener { metadata ->
                    if (metadata.getCustomMetadata("orientation") == "horizontal"){
                        ivPicture.updateLayoutParams {
                            height = bitmap.height
                            ivPicture.translationX = 20f
                            ivPicture.translationY = -200f
                        }
                    } else {
                        ivPicture.rotation = 90f
                        ivPicture.translationY = -500f
                        ivPicture.translationX = -80f
                        ivPicture.updateLayoutParams {
                            height = bitmap.width
                        }
                    }
                }.addOnFailureListener { }
            }.addOnFailureListener{
                Toast.makeText(this, "fallo al cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        }

        when (bundle.getString("sport")){
            "Bike" ->  binding.ivSportSelected.setImageResource(R.mipmap.bike)
            "RollerSkate" -> binding.ivSportSelected.setImageResource(R.mipmap.rollerskate)
            "Running" -> binding.ivSportSelected.setImageResource(R.mipmap.running)
        }

        val activatedGPS = bundle.getBoolean("activatedGPS")

        if (!activatedGPS){ //quitamos el mapa y los datos de mediciones
            setHeightLinearLayout(binding.lyRun, 0)
            setHeightLinearLayout(binding.lyDatas, 0)
        } else {

            val medalDistance = bundle.getString("medalDistance")
            val medalAvgSpeed = bundle.getString("medalAvgSpeed")
            val medalMaxSpeed = bundle.getString("medalMaxSpeed")

            if (medalDistance == "none"
                && medalAvgSpeed == "none"
                && medalMaxSpeed == "none"){

                setHeightLinearLayout(binding.lyMedalsRun, 0)
            } else {
                when (medalDistance){
                    "gold" -> {
                        binding.ivMedalDistance.setImageResource(R.drawable.medalgold)
                        binding.tvMedalDistanceTitle.setText(R.string.medalDistanceDescription)
                    }
                    "silver" -> {
                        binding.ivMedalDistance.setImageResource(R.drawable.medalsilver)
                        binding.tvMedalDistanceTitle.setText(R.string.medalDistanceDescription)
                    }
                    "bronze" -> {
                        binding.ivMedalDistance.setImageResource(R.drawable.medalbronze)
                        binding.tvMedalDistanceTitle.setText(R.string.medalDistanceDescription)
                    }
                }

                when (medalAvgSpeed){
                    "gold" -> {
                        binding.ivMedalAvgSpeed.setImageResource(R.drawable.medalgold)
                        binding.tvMedalAvgSpeedTitle.setText(R.string.medalDistanceDescription)
                    }
                    "silver" -> {
                        binding.ivMedalAvgSpeed.setImageResource(R.drawable.medalsilver)
                        binding.tvMedalAvgSpeedTitle.setText(R.string.medalDistanceDescription)
                    }
                    "bronze" -> {
                        binding.ivMedalAvgSpeed.setImageResource(R.drawable.medalbronze)
                        binding.tvMedalAvgSpeedTitle.setText(R.string.medalDistanceDescription)
                    }
                }

                when (medalMaxSpeed){
                    "gold" -> {
                        binding.ivMedalMaxSpeed.setImageResource(R.drawable.medalgold)
                        binding.tvMedalMaxSpeedTitle.setText(R.string.medalDistanceDescription)
                    }
                    "silver" -> {
                        binding.ivMedalMaxSpeed.setImageResource(R.drawable.medalsilver)
                        binding.tvMedalMaxSpeedTitle.setText(R.string.medalDistanceDescription)
                    }
                    "bronze" -> {
                        binding.ivMedalMaxSpeed.setImageResource(R.drawable.medalbronze)
                        binding.tvMedalMaxSpeedTitle.setText(R.string.medalDistanceDescription)
                    }
                }
            }

            binding.tvDurationRun.text = bundle.getString("duration")

            if (bundle.getInt("challengeDuration") == 0){
                setHeightLinearLayout(binding.lyChallengeDurationRun, 0)
            } else {
                binding.tvChallengeDurationRun.text = bundle.getString("challengeDuration")
            }

            if (!bundle.getBoolean("intervalMode")){
                setHeightLinearLayout(binding.lyIntervalRun, 0)
            } else {
                val details: String = "${bundle.getInt("intervalDuration")}mins. (${bundle.getString("runningTime")} / ${bundle.getString("walkingTime")})"
                binding.tvIntervalRun.text = details
            }

            binding.tvDistanceRun.text = bundle.getDouble("distance").toString()

            if (bundle.getDouble("challengeDistance") == 0.0){
                setHeightLinearLayout(binding.lyChallengeDistancePopUp, 0)
            } else {
                binding.tvChallengeDistanceRun.text = bundle.getDouble("challengeDistance").toString()
            }

            if (bundle.getDouble("minAltitude") == 0.0){
                setHeightLinearLayout(binding.lyUnevennessRun, 0)
            } else {
                binding.tvMaxUnevennessRun.text = bundle.getDouble("maxAltitude").toInt().toString()
                binding.tvMinUnevennessRun.text = bundle.getDouble("minAltitude").toInt().toString()
            }

            binding.tvAvgSpeedRun.text = bundle.getDouble("avgSpeed").toString()
            binding.tvMaxSpeedRun.text = bundle.getDouble("maxSpeed").toString()
        }
    }
}