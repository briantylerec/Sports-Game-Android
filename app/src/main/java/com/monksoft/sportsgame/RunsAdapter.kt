package com.monksoft.sportsgame

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.monksoft.sportsgame.LoginActivity.Companion.userEmail
import com.monksoft.sportsgame.Utility.animateViewofFloat
import com.monksoft.sportsgame.Utility.deleteRunAndLinkedData
import com.monksoft.sportsgame.Utility.setHeightLinearLayout
import java.io.File

class RunsAdapter(private val runsList: ArrayList<Runs>) :RecyclerView.Adapter<RunsAdapter.MyViewHolder>() {

    private var minimized = true
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunsAdapter.MyViewHolder {
        context = parent.context
        val itemView = LayoutInflater.from (context).inflate(R.layout.card_run, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RunsAdapter.MyViewHolder, position: Int) {

        val run: Runs = runsList[position]

        setHeightLinearLayout(holder.lyDataRunBody, 0)
        holder.lyDataRunBodyContainer.translationY = -200f

        holder.ivHeaderOpenClose.setOnClickListener{
            if (minimized){
                setHeightLinearLayout(holder.lyDataRunBody, 600)
                animateViewofFloat(holder.lyDataRunBodyContainer, "translationY", 0f, 300L)
                holder.ivHeaderOpenClose.rotation = 180f
                minimized = false
            } else {
                holder.lyDataRunBodyContainer.translationY = -200f
                setHeightLinearLayout(holder.lyDataRunBody, 0)
                holder.ivHeaderOpenClose.rotation = 0f
                minimized = true
            }
        }

        val day = run.date?.subSequence(8, 10)
        val months = arrayOf("ENE", "FEB","MAR","ABR","MAY","JUN","JUL","AGO","SEP","OCT","NOV","DEC")
        val nMonth = run.date?.subSequence(5, 7)
        val month: String = months[nMonth.toString().toInt()]
        val year = run.date?.subSequence(0, 4)

        val date: String = "$day-$month-$year"

        holder.tvDate.text = date
        holder.tvHeaderDate.text = date

        holder.tvStartTime.text = run.startTime?.subSequence(0,5)
        holder.tvDurationRun.text = run.duration
        holder.tvHeaderDuration.text = run.duration!!.subSequence(0,5).toString()+"HH"

        if (!run.challengeDuration.isNullOrEmpty()) holder.tvChallengeDurationRun.text = run.challengeDuration
        else setHeightLinearLayout(holder.lyChallengeDurationRun, 0)

        if (run.challengeDistance != null) holder.tvChallengeDistanceRun.text = run.challengeDistance.toString()
        else setHeightLinearLayout(holder.lyChallengeDistance, 0)

        if (run.intervalMode != null){
            var details: String = "${run.intervalDuration}mins. ("
            details += "${run.runningTime}/${run.walkingTime})"
            holder.tvIntervalRun.text = details

        } else setHeightLinearLayout(holder.lyIntervalRun, 0)

        holder.tvDistanceRun.setText(run.distance.toString())
        holder.tvHeaderDistance.setText(run.distance.toString() + "KM")

        holder.tvMaxUnevennessRun.setText(run.maxAltitude.toString())
        holder.tvMinUnevennessRun.setText(run.minAltitude.toString())

        holder.tvAvgSpeedRun.setText(run.avgSpeed.toString())
        holder.tvHeaderAvgSpeed.setText(run.avgSpeed.toString()+"KM/H")
        holder.tvMaxSpeedRun.setText(run.maxSpeed.toString())

        when(run.medalDistance){
            "gold"->{
                holder.apply {
                    ivMedalDistance.setImageResource(R.drawable.medalgold)
                    ivHeaderMedalDistance.setImageResource(R.drawable.medalgold)
                    tvMedalDistanceTitle.setText(R.string.CardMedalDistance)
                }
            }
            "silver"->{
                holder.apply {
                    ivMedalDistance.setImageResource(R.drawable.medalsilver)
                    ivHeaderMedalDistance.setImageResource(R.drawable.medalsilver)
                    tvMedalDistanceTitle.setText(R.string.CardMedalDistance)
                }
            }
            "bronze"->{
                holder.apply {
                    ivMedalDistance.setImageResource(R.drawable.medalbronze)
                    ivHeaderMedalDistance.setImageResource(R.drawable.medalbronze)
                    tvMedalDistanceTitle.setText(R.string.CardMedalDistance)
                }
            }
        }
        when(run.medalAvgSpeed){
            "gold"->{
                holder.apply {
                    ivMedalAvgSpeed.setImageResource(R.drawable.medalgold)
                    ivHeaderMedalAvgSpeed.setImageResource(R.drawable.medalgold)
                    tvMedalAvgSpeedTitle.setText(R.string.CardMedalAvgSpeed)
                }
            }
            "silver"->{
                holder.apply {
                    ivMedalAvgSpeed.setImageResource(R.drawable.medalsilver)
                    ivHeaderMedalAvgSpeed.setImageResource(R.drawable.medalsilver)
                    tvMedalAvgSpeedTitle.setText(R.string.CardMedalAvgSpeed)
                }
            }
            "bronze"->{
                holder.apply {
                    ivMedalAvgSpeed.setImageResource(R.drawable.medalbronze)
                    ivHeaderMedalAvgSpeed.setImageResource(R.drawable.medalbronze)
                    tvMedalAvgSpeedTitle.setText(R.string.CardMedalAvgSpeed)
                }
            }
        }
        when(run.medalMaxSpeed){
            "gold"->{
                holder.apply {
                    ivMedalMaxSpeed.setImageResource(R.drawable.medalgold)
                    ivHeaderMedalMaxSpeed.setImageResource(R.drawable.medalgold)
                    tvMedalMaxSpeedTitle.setText(R.string.CardMedalMaxSpeed)
                }
            }
            "silver"->{
                holder.apply {
                    ivMedalMaxSpeed.setImageResource(R.drawable.medalsilver)
                    ivHeaderMedalMaxSpeed.setImageResource(R.drawable.medalsilver)
                    tvMedalMaxSpeedTitle.setText(R.string.CardMedalMaxSpeed)
                }
            }
            "bronze"->{
                holder.apply {
                    ivMedalMaxSpeed.setImageResource(R.drawable.medalbronze)
                    ivHeaderMedalMaxSpeed.setImageResource(R.drawable.medalbronze)
                    tvMedalMaxSpeedTitle.setText(R.string.CardMedalMaxSpeed)
                }
            }
        }

        if(run.lastimage!="") {

            val path = run.lastimage
            val storageRef = FirebaseStorage.getInstance().reference.child(path!!)
            val localfile = File.createTempFile("tempImage", "jpg")

            storageRef.getFile(localfile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
                val metaRef = FirebaseStorage.getInstance().getReference(run.lastimage!!)
                val metadata: Task<StorageMetadata> = metaRef.metadata

                metadata.addOnSuccessListener {

                    val or = it.getCustomMetadata("orientation")
                    if (or == "horizontal") {

                        val porcent = 100 / bitmap.width.toFloat()

                        setHeightLinearLayout(holder.lyPicture, (bitmap.width * porcent).toInt())
                        holder.ivPicture.setImageBitmap(bitmap)

                    } else {
                        val porcent = 100 / bitmap.height.toFloat()

                        setHeightLinearLayout(holder.lyPicture, (bitmap.width * porcent).toInt())
                        holder.ivPicture.setImageBitmap(bitmap)
                        holder.ivPicture.rotation = 90f
                    }
                }
                metadata.addOnFailureListener { }
            }.addOnFailureListener {
                Toast.makeText(context, "fallo al cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        }

        holder.tvDelete.setOnClickListener{
            val id:String = (userEmail + run.date + run.startTime).replace(":", "").replace("/", "")

            deleteRunAndLinkedData(id, run.sport!!, holder.lyDataRunHeader, run)

            runsList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun getItemCount(): Int {
        return runsList.size
    }

    public class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        val lyDataRunHeader: LinearLayout = itemView.findViewById(R.id.lyDataRunHeader)
        val tvHeaderDate: TextView = itemView.findViewById(R.id.tvHeaderDate)
        val tvHeaderDuration: TextView = itemView.findViewById(R.id.tvHeaderDuration)
        val tvHeaderDistance: TextView = itemView.findViewById(R.id.tvHeaderDistance)
        val tvHeaderAvgSpeed: TextView = itemView.findViewById(R.id.tvHeaderAvgSpeed)
        val ivHeaderMedalDistance: ImageView = itemView.findViewById(R.id.ivHeaderMedalDistance)
        val ivHeaderMedalAvgSpeed: ImageView = itemView.findViewById(R.id.ivHeaderMedalAvgSpeed)
        val ivHeaderMedalMaxSpeed: ImageView = itemView.findViewById(R.id.ivHeaderMedalMaxSpeed)
        val ivHeaderOpenClose: ImageView = itemView.findViewById(R.id.ivHeaderOpenClose)

        val lyDataRunBody: LinearLayout = itemView.findViewById(R.id.lyDataRunBody)
        val lyDataRunBodyContainer: LinearLayout = itemView.findViewById(R.id.lyDataRunBodyContainer)

        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvStartTime: TextView = itemView.findViewById(R.id.tvStartTime)


        val tvDurationRun: TextView = itemView.findViewById(R.id.tvDurationRun)
        val lyChallengeDurationRun: LinearLayout = itemView.findViewById(R.id.lyChallengeDurationRun)
        val tvChallengeDurationRun: TextView = itemView.findViewById(R.id.tvChallengeDurationRun)
        val lyIntervalRun: LinearLayout = itemView.findViewById(R.id.lyIntervalRun)
        val tvIntervalRun: TextView = itemView.findViewById(R.id.tvIntervalRun)


        val tvDistanceRun: TextView = itemView.findViewById(R.id.tvDistanceRun)
        val lyChallengeDistance: LinearLayout = itemView.findViewById(R.id.lyChallengeDistance)
        val tvChallengeDistanceRun: TextView = itemView.findViewById(R.id.tvChallengeDistanceRun)
        val lyUnevennessRun: LinearLayout = itemView.findViewById(R.id.lyUnevennessRun)
        val tvMaxUnevennessRun: TextView = itemView.findViewById(R.id.tvMaxUnevennessRun)
        val tvMinUnevennessRun: TextView = itemView.findViewById(R.id.tvMinUnevennessRun)


        val tvAvgSpeedRun: TextView = itemView.findViewById(R.id.tvAvgSpeedRun)
        val tvMaxSpeedRun: TextView = itemView.findViewById(R.id.tvMaxSpeedRun)

        val ivMedalDistance: ImageView = itemView.findViewById(R.id.ivMedalDistance)
        val tvMedalDistanceTitle: TextView = itemView.findViewById(R.id.tvMedalDistanceTitle)
        val ivMedalAvgSpeed: ImageView = itemView.findViewById(R.id.ivMedalAvgSpeed)
        val tvMedalAvgSpeedTitle: TextView = itemView.findViewById(R.id.tvMedalAvgSpeedTitle)
        val ivMedalMaxSpeed: ImageView = itemView.findViewById(R.id.ivMedalMaxSpeed)
        val tvMedalMaxSpeedTitle: TextView = itemView.findViewById(R.id.tvMedalMaxSpeedTitle)


        val ivPicture: ImageView = itemView.findViewById(R.id.ivPicture)

        val lyPicture: LinearLayout = itemView.findViewById(R.id.lyPicture)
        val tvPlay: TextView = itemView.findViewById(R.id.tvPlay)
        val tvDelete: TextView = itemView.findViewById(R.id.tvDelete)
    }
}