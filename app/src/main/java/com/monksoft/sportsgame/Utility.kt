package com.monksoft.sportsgame

import android.animation.ObjectAnimator
import android.content.ContentValues
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.monksoft.sportsgame.LoginActivity.Companion.userEmail
import com.monksoft.sportsgame.MainActivity.Companion.activatedGPS
import com.monksoft.sportsgame.MainActivity.Companion.totalsBike
import com.monksoft.sportsgame.MainActivity.Companion.totalsRollerSkate
import com.monksoft.sportsgame.MainActivity.Companion.totalsRunning
import com.monksoft.sportsgame.MainActivity.Companion.totalsSelectedSport
import java.util.concurrent.TimeUnit

object Utility {

    private var totalsChecked: Int = 0

    fun getSecFromWatch (watch: String): Int{
        var secs = 0
        var w: String = watch
        if (w.length == 5) w= "00:$w"

        // 00:00:00
        secs += w.subSequence(0,2).toString().toInt() * 3600
        secs += w.subSequence(3,5).toString().toInt() * 60
        secs += w.subSequence(6,8).toString().toInt()

        return secs
    }

    /* FUNCIONES DE ANIMACION Y CAMBIOS DE ATRIBUTOS */
    fun setHeightLinearLayout(ly: LinearLayout, value: Int){
        val params: LinearLayout.LayoutParams = ly.layoutParams as LinearLayout.LayoutParams
        params.height = value
        ly.layoutParams = params
    }

    fun animateViewofInt(v: View, attr: String, value: Int, time: Long){
        ObjectAnimator.ofInt(v, attr, value).apply{
            duration = time
            start()
        }
    }

    fun animateViewofFloat(v: View, attr: String, value: Float, time: Long){
        ObjectAnimator.ofFloat(v, attr, value).apply{
            duration = time
            start()
        }
    }

    fun getFormattedStopWatch(ms: Long): String {
        var milliseconds = ms
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)

        return "${if (hours < 10) "0" else ""}$hours:" +
                "${if (minutes < 10) "0" else ""}$minutes:" +
                "${if (seconds < 10) "0" else ""}$seconds"
    }

    fun roundNumber(data: String, decimals: Int) : String {
        var d : String = data
        val p = d.indexOf(".", 0)

        if (p != null){
            var limit: Int = p+decimals +1
            if (d.length <= p+decimals+1) limit = d.length //-1
            d = d.subSequence(0, limit).toString()
        }

        return d
    }

    fun getFormattedTotalTime(secs: Long): String {
        var seconds: Long = secs
        var total: String =""

        //1 dia = 86400s
        //1 mes (30 dias) = 2592000s
        //365 dias = 31536000s

        var years: Int = 0
        while (seconds >=  31536000) { years++; seconds-=31536000; }

        var months: Int = 0
        while (seconds >=  2592000) { months++; seconds-=2592000; }

        var days: Int = 0
        while (seconds >=  86400) { days++; seconds-=86400; }

        if (years > 0) total += "${years}y "
        if (months > 0) total += "${months}m "
        if (days > 0) total += "${days}d "

        total += getFormattedStopWatch(seconds*1000)

        return total
    }

    fun deleteRunAndLinkedData(idRun: String, sport: String, ly: LinearLayout, cr: Runs){

        if (activatedGPS) deleteLocations(idRun, userEmail)
        //si habia fotos, borramos todas las fotos
        updateTotals(cr)
        checkRecords(cr, sport, userEmail)
        deleteRun(idRun, sport, ly)
    }

    private fun deleteLocations(idRun: String, user: String){
        val idLocations = idRun.subSequence(user.length, idRun.length).toString()

        val dbLocations = FirebaseFirestore.getInstance()
        dbLocations.collection("locations/$user/$idLocations")
            .get()
            .addOnSuccessListener { documents->
                for (docLocation in documents){
                    val dbLoc = FirebaseFirestore.getInstance()
                    dbLoc.collection("locations/$user/$idLocations").document(docLocation.id)
                        .delete()
                }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)

            }
    }

    private fun updateTotals(cr: Runs){
        totalsSelectedSport.totalDistance = totalsSelectedSport.totalDistance!! - cr.distance!!
        totalsSelectedSport.totalRuns = totalsSelectedSport.totalRuns!! - 1
        totalsSelectedSport.totalTime = totalsSelectedSport.totalTime!! - getSecFromWatch(cr.duration!!)
    }

    private fun checkRecords(cr: Runs, sport: String, user: String){

        totalsChecked = 0

        checkDistanceRecord(cr, sport, user)
        checkAvgSpeedRecord(cr, sport, user)
        checkMaxSpeedRecord(cr, sport, user)
    }

    private fun checkDistanceRecord(cr: Runs, sport: String, user: String){
        if (cr.distance!! == totalsSelectedSport.recordDistance){
            val dbRecords = FirebaseFirestore.getInstance()
            dbRecords.collection("runs$sport")
                .orderBy("distance", Query.Direction.DESCENDING)
                .whereEqualTo("user", user)
                .get()
                .addOnSuccessListener { documents ->

                    if (documents.size() == 1)  totalsSelectedSport.recordDistance = 0.0
                    else  totalsSelectedSport.recordDistance = documents.documents[1].get("distance").toString().toDouble()

                    val collection = "totals$sport"
                    val dbUpdateTotals = FirebaseFirestore.getInstance()
                    dbUpdateTotals.collection(collection).document(user)
                        .update("recordDistance", totalsSelectedSport.recordDistance)

                    totalsChecked++
                    if (totalsChecked == 3) refreshTotalsSport(sport)

                }
                .addOnFailureListener { exception ->
                    Log.w(ContentValues.TAG, "Error getting documents WHERE EQUAL TO: ", exception)
                }
        }
    }

    private fun checkAvgSpeedRecord(cr: Runs, sport: String, user: String){
        if (cr.avgSpeed!! == totalsSelectedSport.recordAvgSpeed){
            val dbRecords = FirebaseFirestore.getInstance()
            dbRecords.collection("runs$sport")
                .orderBy("avgSpeed", Query.Direction.DESCENDING)
                .whereEqualTo("user", user)
                .get()
                .addOnSuccessListener { documents ->

                    if (documents.size() == 1)  totalsSelectedSport.recordAvgSpeed = 0.0
                    else  totalsSelectedSport.recordAvgSpeed = documents.documents[1].get("avgSpeed").toString().toDouble()

                    val collection = "totals$sport"
                    val dbUpdateTotals = FirebaseFirestore.getInstance()
                    dbUpdateTotals.collection(collection).document(user)
                        .update("recordAvgSpeed", totalsSelectedSport.recordAvgSpeed)

                    totalsChecked++
                    if (totalsChecked == 3) refreshTotalsSport(sport)

                }
                .addOnFailureListener { exception ->
                    Log.w(ContentValues.TAG, "Error getting documents WHERE EQUAL TO: ", exception)
                }
        }
    }


    private fun checkMaxSpeedRecord(cr: Runs, sport: String, user: String){
        if (cr.maxSpeed!! == totalsSelectedSport.recordSpeed){
            val dbRecords = FirebaseFirestore.getInstance()
            dbRecords.collection("runs$sport")
                .orderBy("maxSpeed", Query.Direction.DESCENDING)
                .whereEqualTo("user", user)
                .get()
                .addOnSuccessListener { documents ->

                    if (documents.size() == 1)  totalsSelectedSport.recordSpeed = 0.0
                    else  totalsSelectedSport.recordSpeed = documents.documents[1].get("maxSpeed").toString().toDouble()

                    val collection = "totals$sport"
                    val dbUpdateTotals = FirebaseFirestore.getInstance()
                    dbUpdateTotals.collection(collection).document(user)
                        .update("recordSpeed", totalsSelectedSport.recordSpeed)

                    totalsChecked++
                    if (totalsChecked == 3) refreshTotalsSport(sport)

                }
                .addOnFailureListener { exception ->
                    Log.w(ContentValues.TAG, "Error getting documents WHERE EQUAL TO: ", exception)
                }
        }
    }

    private fun refreshTotalsSport(sport: String){
        when (sport){
            "Bike"-> totalsBike = totalsSelectedSport
            "RollerSkate"-> totalsRollerSkate = totalsSelectedSport
            "Running"-> totalsRunning = totalsSelectedSport
        }
    }

    private fun deleteRun(idRun: String, sport: String, ly: LinearLayout){
        val dbRun = FirebaseFirestore.getInstance()
        dbRun.collection("runs$sport").document(idRun)
            .delete()
            .addOnSuccessListener {
                Snackbar.make(ly, "Registro Borrado", Snackbar.LENGTH_LONG).setAction("OK"){
                    ly.setBackgroundColor(Color.CYAN)
                }.show()
            }
            .addOnFailureListener {
                Snackbar.make(ly, "Error al borrar el registro", Snackbar.LENGTH_LONG).setAction("OK"){
                    ly.setBackgroundColor(Color.CYAN)
                }.show()
            }
    }
}