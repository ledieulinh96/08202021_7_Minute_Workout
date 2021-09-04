package com.example.a08202021_7_minute_workout

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_exercise.*
import java.util.*
import kotlin.collections.ArrayList

class ExerciseActivity : AppCompatActivity(),  TextToSpeech.OnInitListener{

    private var restTimer: CountDownTimer? = null
    private var restProgress = 0
    private var restTimerDuration: Long = 5

    private var exerciseTimer: CountDownTimer? = null
    private var exerciseProgress = 0
    private var exerciseTimerDuration: Long = 30

    private var exerciseList: ArrayList<ExerciseModel>? = null
    private var currentExercisePosition = -1

    private var tts: TextToSpeech?= null //Variable for TextTOSpeech
    private var player: MediaPlayer? = null
    private var exerciseAdapter: ExerciseStatusAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise)

        setSupportActionBar(toolbar_exercise_activity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        toolbar_exercise_activity.setNavigationOnClickListener {
            onBackPressed()
        }

        tts = TextToSpeech(this, this)

        exerciseList = Constants.defaultExerciseList()
        progressBarRest.max = restTimerDuration.toInt()
        progressBarExercise.max = exerciseTimerDuration.toInt()
        setupRestView()
        setupExerciseStatusRecyclerView()

    }

    override fun onDestroy() {
        if(restTimer != null) {
            restTimer!!.cancel()
            restProgress = 0
        }

        if(exerciseTimer != null) {
            exerciseTimer!!.cancel()
            exerciseProgress = 0
        }
        if(tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }

        if (player != null) {
            player!!.stop()
        }

        super.onDestroy()
    }

    //hard code 10s
    private fun setRestProgressBar() {
        progressBarRest.progress = restProgress
        restTimer = object : CountDownTimer(restTimerDuration*1000, 1000){
            override fun onTick(millisUntilFinished: Long) {
                restProgress++
                val currentRestTime = restTimerDuration.toInt()-restProgress

                progressBarRest.progress = currentRestTime
                tvTimer.text = currentRestTime.toString()

                if (currentRestTime == 5) {
                    speakOut( exerciseList!![currentExercisePosition + 1].name)
                }
                if (currentRestTime in 1..3) {
                    speakOut(currentRestTime.toString())
                }
            }

            override fun onFinish() {
                currentExercisePosition++;

                exerciseList!![currentExercisePosition].isSelected = true
                exerciseAdapter!!.notifyDataSetChanged()

                setupExerciseView()
            }
        }.start()
    }

    private fun setExerciseProgressBar() {
        progressBarExercise.progress = exerciseProgress
        exerciseTimer = object : CountDownTimer(exerciseTimerDuration*1000, 1000){
            override fun onTick(millisUntilFinished: Long) {
                exerciseProgress++
                val currentExerciseTime = exerciseTimerDuration.toInt() - exerciseProgress
                progressBarExercise.progress = currentExerciseTime
                tvExerciseTimer.text = currentExerciseTime.toString()
                if (currentExerciseTime in 1 .. 5) {
                    speakOut(currentExerciseTime.toString())
                }
            }

            override fun onFinish() {
                if (currentExercisePosition < exerciseList?.size!! - 1) {
                    exerciseList!![currentExercisePosition].isSelected = false
                    exerciseList!![currentExercisePosition].isCompleted = true
                    exerciseAdapter!!.notifyDataSetChanged()
                    setupRestView()
                } else {
                    val intent = Intent(this@ExerciseActivity, FinishActivity::class.java)
                    startActivity(intent)
                    finish()
                }

            }
        }.start()
    }

    private fun speakOut(text: String) {
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    private fun setupRestView() {
        try {
            //val soundURI = Uri.parse("android::resource://com.example..." +  R.raw.press_start)
            player = MediaPlayer.create(applicationContext, R.raw.press_start)
            player!!.isLooping = false;
            player!!.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        llRestView.visibility = View.VISIBLE
        llExerciseView.visibility = View.GONE

        if(restTimer!= null) {
            restTimer!!.cancel()
            restProgress = 0
        }
        speakOut("Rest time")
        tvUpcomingExerciseName.text = exerciseList!![currentExercisePosition + 1].name
        setRestProgressBar()
    }

    private fun setupExerciseView() {

        llRestView.visibility = View.GONE
        llExerciseView.visibility = View.VISIBLE

        if(exerciseTimer!= null) {
            exerciseTimer!!.cancel()
            exerciseProgress = 0
        }
        speakOut(exerciseList!![currentExercisePosition].name)
        setExerciseProgressBar()

        ivImage.setImageResource(exerciseList!![currentExercisePosition].image)
        tvExerciseName.text = exerciseList!![currentExercisePosition].name
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("_linh_" + "TTS" + "",": The Language specified is not supported!");
            } else {
               Log.e("_linh_" + "TTS" + "",": Initialization failed!");
            }
        }
    }

    private fun setupExerciseStatusRecyclerView() {
        rvExerciseStatus.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL, false)
        exerciseAdapter = ExerciseStatusAdapter(exerciseList!!, this)
        rvExerciseStatus.adapter = exerciseAdapter
    }
}
