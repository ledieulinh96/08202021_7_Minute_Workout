package com.example.a08202021_7_minute_workout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_exercise.*
import java.util.*
import kotlin.collections.ArrayList

class ExerciseActivity : AppCompatActivity(),  TextToSpeech.OnInitListener{

    private var restTimer: CountDownTimer? = null
    private var restProgress = 0
    private var exerciseTimer: CountDownTimer? = null
    private var exerciseProgress = 0
    private var exerciseTimerDuration: Long = 30

    private var exerciseList: ArrayList<ExerciseModel>? = null
    private var currentExercisePosition = -1

    private var tts: TextToSpeech?= null //Variable for TextTOSpeech

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
        setupRestView()

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

        super.onDestroy()
    }

    //hard code 10s
    private fun setRestProgressBar() {
        progressBar.progress = restProgress
        restTimer = object : CountDownTimer(10000, 1000){
            override fun onTick(millisUntilFinished: Long) {
                restProgress++
                progressBar.progress = 10-restProgress
                tvTimer.text = (10- restProgress).toString()

                if (10-restProgress == 5) {
                    speakOut( exerciseList!![currentExercisePosition + 1].name)
                }
                if (10-restProgress in 1..3) {
                    speakOut((10 - restProgress).toString())
                }
            }

            override fun onFinish() {
                currentExercisePosition++;
                setupExerciseView()
            }
        }.start()
    }

    private fun setExerciseProgressBar() {
        progressBarExercise.progress = exerciseProgress
        exerciseTimer = object : CountDownTimer(exerciseTimerDuration*1000, 1000){
            override fun onTick(millisUntilFinished: Long) {
                exerciseProgress++
                progressBarExercise.progress = exerciseTimerDuration.toInt() - exerciseProgress
                tvExerciseTimer.text = (exerciseTimerDuration- exerciseProgress).toString()
                if (exerciseTimerDuration - exerciseProgress in 1 .. 5) {
                    speakOut((exerciseTimerDuration - exerciseProgress).toString())
                }
            }

            override fun onFinish() {
                if (currentExercisePosition < exerciseList?.size!! - 1) {
                    setupRestView()
                } else {
                    Toast.makeText(this@ExerciseActivity, "Congratulations! You have completed the 7 minuts workout.", Toast.LENGTH_SHORT).show()
                }

            }
        }.start()
    }

    private fun speakOut(text: String) {
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    private fun setupRestView() {
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
}
