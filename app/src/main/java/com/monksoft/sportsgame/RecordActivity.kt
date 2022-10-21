package com.monksoft.sportsgame

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.monksoft.sportsgame.LoginActivity.Companion.userEmail
import com.monksoft.sportsgame.MainActivity.Companion.mainContext
import com.monksoft.sportsgame.databinding.ActivityRecordBinding

class RecordActivity : AppCompatActivity() {

    private var sportSelected : String = "Running"

    private lateinit var binding : ActivityRecordBinding
    
    private lateinit var runsArrayList : ArrayList<Runs>
    private lateinit var myAdapter: RunsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_record)
        setSupportActionBar(toolbar)

        toolbar.title = getString(R.string.bar_title_record)
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.gray_dark))
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_light))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        binding.rvRecords.layoutManager = LinearLayoutManager(this)
        binding.rvRecords.setHasFixedSize(true)

        runsArrayList = arrayListOf()
        myAdapter = RunsAdapter(runsArrayList)
        binding.rvRecords.adapter = myAdapter
    }

    override fun onResume() {
        super.onResume()
        loadRecyclerView("date", Query.Direction.DESCENDING)
    }

    override fun onPause() {
        super.onPause()
        runsArrayList.clear()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.order_records_by, menu)
        return true //super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        var order: Query.Direction = Query.Direction.DESCENDING

        when (item.itemId){
            R.id.orderby_date -> {
                if (item.title == getString(R.string.orderby_dateZA)){
                    item.title = getString(R.string.orderby_dateAZ)
                    order = Query.Direction.DESCENDING
                }
                else{
                    item.title = getString(R.string.orderby_dateZA)
                    order = Query.Direction.ASCENDING
                }
                loadRecyclerView("date", order)
                return true
            }
            R.id.orderby_duration ->{
                var option = getString(R.string.orderby_durationZA)
                if (item.title == getString(R.string.orderby_durationZA)){
                    item.title = getString(R.string.orderby_durationAZ)
                    order = Query.Direction.DESCENDING
                }
                else{
                    item.title = getString(R.string.orderby_durationZA)
                    order = Query.Direction.ASCENDING
                }
                loadRecyclerView("duration", order)
                return true
            }

            R.id.orderby_distance ->{
                val option = getString(R.string.orderby_distanceZA)
                if (item.title == option){
                    item.title = getString(R.string.orderby_distanceAZ)
                    order = Query.Direction.ASCENDING
                }
                else{
                    item.title = getString(R.string.orderby_distanceZA)
                    order = Query.Direction.DESCENDING
                }
                loadRecyclerView("distance", order)
                return true
            }

            R.id.orderby_avgspeed ->{
                var option = getString(R.string.orderby_avgspeedZA)
                if (item.title == getString(R.string.orderby_avgspeedZA)){
                    item.title = getString(R.string.orderby_avgspeedAZ)
                    order = Query.Direction.ASCENDING
                }
                else{
                    item.title = getString(R.string.orderby_avgspeedZA)
                    order = Query.Direction.DESCENDING
                }
                loadRecyclerView("avgSpeed", order)
                return true
            }

            R.id.orderby_maxspeed ->{
                var option = getString(R.string.orderby_maxspeedZA)
                if (item.title == getString(R.string.orderby_maxspeedZA)){
                    item.title = getString(R.string.orderby_maxspeedAZ)
                    order = Query.Direction.ASCENDING
                }
                else{
                    item.title = getString(R.string.orderby_maxspeedZA)
                    order = Query.Direction.DESCENDING
                }
                loadRecyclerView("maxSpeed", order)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadRecyclerView(field: String, order: Query.Direction){
        runsArrayList.clear()

        val dbRuns = FirebaseFirestore.getInstance()
        dbRuns.collection("runs$sportSelected").orderBy(field, order)
            .whereEqualTo("user", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                for (run in documents)
                    runsArrayList.add(run.toObject(Runs::class.java))

                myAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents WHERE EQUAL TO: ", exception)
            }
    }

    fun loadRunsBike(v: View){
        sportSelected = "Bike"
        loadOption()
    }

    fun loadRunsRollerSkate(v: View){
        sportSelected = "RollerSkate"
        loadOption()
    }

    fun loadRunsRunning(v: View){
        sportSelected = "Running"
        loadOption()
    }

    fun callHome(v: View){
        startActivity(Intent(this, MainActivity::class.java))
    }

    fun loadOption(){
        binding.ivBike.setBackgroundColor(ContextCompat.getColor(mainContext, if (sportSelected == "Bike") R.color.orange else R.color.gray_medium))
        binding.ivRollerSkate.setBackgroundColor(ContextCompat.getColor(mainContext, if (sportSelected == "RollerSkate") R.color.orange else R.color.gray_medium))
        binding.ivRunning.setBackgroundColor(ContextCompat.getColor(mainContext, if (sportSelected == "Running") R.color.orange else R.color.gray_medium))

        loadRecyclerView("date", Query.Direction.DESCENDING)
    }
}