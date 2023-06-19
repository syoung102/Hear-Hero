package com.elbehiry.delish.presentation.navigation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.*
import app.delish.bookmark.BookMarkScreen
import app.delish.discover.ui.Discover
import app.delish.onboarding.OnBoardingScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String,
    width: Int,
    bottomBarPadding: PaddingValues,
    bottomBarState: MutableState<Boolean>
) {
    AnimatedNavHost(
        navController = navController,
        startDestination = startDestination,
        builder = {
            onBoardingScreen(navController, bottomBarState)
            mainScreenScreen(navController, bottomBarPadding, bottomBarState)
            soundScreen(navController, bottomBarState, bottomBarPadding)
            mapScreen(navController, bottomBarPadding, bottomBarState)
        }
    )
}

fun NavGraphBuilder.onBoardingScreen(
    navController: NavController,
    bottomBarState: MutableState<Boolean>
) {
    composable(
        route = Navigate.Screen.OnBoardingWelcome.route
    ) {
        bottomBarState.value = false
        OnBoardingScreen {
            navController.navigate(Navigate.Screen.Main.route)
        }
    }
}

fun NavGraphBuilder.mainScreenScreen(
    navController: NavController,
    bottomBarPadding: PaddingValues,
    bottomBarState: MutableState<Boolean>
) {
    composable(
        route = Navigate.Screen.Main.route
    ) {
        bottomBarState.value = true
        val systemUiController = rememberSystemUiController()
        SideEffect {
            systemUiController.setStatusBarColor(
                color = Color(0xFF2B292B),
                darkIcons = false
            )
            systemUiController.setNavigationBarColor(
                color = Color(0xFF2B292B),
                darkIcons = false
            )
        }
        val activity = (LocalContext.current as? Activity)

        BackHandler(true) {
            activity?.finish()
        }
        Discover(
            bottomBarPadding = bottomBarPadding,
            onDetails = {
                navController.navigate(Navigate.Screen.Details.route + "/$it")
            },
            onIngredients = {
                navController.navigate(Navigate.BottomSheet.Ingredients.route)
            }
        ) {
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.soundScreen(
    navController: NavController,
    bottomBarState: MutableState<Boolean>,
    bottomBarPadding: PaddingValues
) {

    composable(
        route = Navigate.Screen.BookMark.route,
    ) {
        bottomBarState.value = true
        BookMarkScreen(
            bottomBarPadding = bottomBarPadding
        ) {
            navController.navigate(Navigate.Screen.Details.route + "/$it")
        }
    }
}
fun sendSoundToServer(context: Context, file1: File, file2: File) {
    val client = OkHttpClient()

    val json = JSONObject()
    val prefs = context.getSharedPreferences("delish_app", Context.MODE_PRIVATE)
    val type = object : TypeToken<MutableMap<String, Boolean>>() {}.type
    val soundStatusHashMap = Gson().fromJson<MutableMap<String, Boolean>>(prefs.getString("sound_status", null), type)
    val soundStatusMap = mutableMapOf<String, Boolean>()

    val allSoundsList = listOf(
        "KNOCKING", "BARKING", "CAR HORN", "SIREN", "RINGTONE", "SCREAMING", "FIRE ALARM"
    )
    allSoundsList.forEach { soundName ->
        soundStatusMap[soundName] = false
    }
    if(soundStatusHashMap != null){
        for(soundStatus in soundStatusHashMap){
            soundStatusMap[soundStatus.key] = soundStatus.value
        }
    }
    //val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    /*val onSoundsSet = sharedPreferences.getStringSet("on_sounds", setOf())
    val onSoundsList = onSoundsSet?.toList() ?: emptyList()

    // Create a map to hold sound names and their status
    val soundStatusMap = mutableMapOf<String, Boolean>()

    // Initialize all sound names to false


    // Update the sound status map based on the user's selection
    onSoundsList.forEach { soundName ->
        val isOn = sharedPreferences.getBoolean(soundName, false)
        soundStatusMap[soundName] = isOn
    }*/

    // Add the sound status map to the JSON object
    val soundJson = JSONObject()
    soundStatusMap.forEach { (soundName, isOn) ->
        soundJson.put(soundName, isOn)
    }
    json.put("s_sound", soundJson)

    val multiBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("record1", file1.name, file1.asRequestBody("audio/*".toMediaTypeOrNull()))
        .addFormDataPart("record2", file2.name, file2.asRequestBody("audio/*".toMediaTypeOrNull()))
        .addFormDataPart("s_sound", json.toString())
        .build()
    Log.e("mergedJson", "json: $json")

    val request = Request.Builder()
        .url("http://165.246.243.4:5000/direction")
        .post(multiBody)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful)
            throw IOException("Unexpected code $response")
        else{
            val responseString = response.body?.string()
            println(responseString)
            try {
                val jsonObject = JSONObject(responseString.toString())
                val result = jsonObject.getInt("d_result")
                if (result != -1 && result != -2)
                    s_sound.value = result
            } catch (e: JSONException) {
                // Handle error for invalid response
            }
        }
    }
}

private suspend fun getSoundsListFromServer(): List<Int> = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("http://165.246.243.4:5000/direction")
        .build()

    val response = client.newCall(request).execute()
    val responseString = response.body?.string()

    if (responseString.isNullOrEmpty()) {
        return@withContext emptyList()
    }

    val soundsList = mutableListOf<Int>()
    try {
        val jsonObject = JSONObject(responseString)
        val result = jsonObject.getInt("d_result")
        val soundStatus =
            if (result == -1 || result == -2)
                return@withContext emptyList()
            else result
        soundsList.add(soundStatus)
        Log.e("jsonObject", "$jsonObject")
        Log.e("result", "$result")
        Log.e("soundsList", "$soundsList")

    } catch (e: JSONException) {
        // Handle error for invalid response
    }
    return@withContext soundsList
}

@Composable
fun LoadingAnimation(
    circleColor: Color = Color(0xFF3B8BEB),
    animationDelay: Int = 3000
) {
    val circles = listOf(
        remember {
            Animatable(initialValue = 0f)
        },
        remember {
            Animatable(initialValue = 0f)
        },
        remember {
            Animatable(initialValue = 0f)
        }
    )

    circles.forEachIndexed { index, animatable ->
        LaunchedEffect(Unit) {
            delay(timeMillis = (animationDelay / 3L) * (index + 1))

            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = animationDelay,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .size(size = 200.dp)
            .background(color = Color.Transparent)
    ) {
        circles.forEachIndexed { index, animatable ->
            Box(
                modifier = Modifier
                    .scale(scale = animatable.value)
                    .size(size = 200.dp)
                    .clip(shape = CircleShape)
                    .background(
                        color = circleColor
                            .copy(alpha = (1 - animatable.value))
                    )
            ) {
            }
        }
    }
}

@Composable
fun CircleWithFragments(direction: String) {
    val strokeWidth = 4.dp
    val stroke = with(LocalDensity.current) { Stroke(strokeWidth.toPx()) }

    val sweepAngle = 45f
    Log.e("d_result4", "$direction")
    val startAngle = when (direction) {
        "Direction1" -> 270f
        "Direction2" -> 315f
        "Direction3" -> 0f
        "Direction4" -> 45f
        "Direction5" -> 90f
        "Direction6" -> 135f
        "Direction7" -> 180f
        "Direction8" -> 225f
        else -> 270f
    }

    Box(
        modifier = Modifier
            .size(200.dp)
            .background(Color.LightGray, shape = CircleShape)
            .drawBehind {
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                val radius = (size.minDimension - stroke.width) / 2f

                val path = Path().apply {
                    arcTo(
                        Rect(
                            centerX - radius,
                            centerY - radius,
                            centerX + radius,
                            centerY + radius
                        ),
                        startAngle,
                        sweepAngle,
                        true
                    )
                    lineTo(centerX, centerY)
                    close()
                }

                drawPath(
                    path = path,
                    color = Color(0xFF3B8BEB),
                    style = Fill,
                )
            }
    )
}

@SuppressLint("MissingPermission")
fun NavGraphBuilder.mapScreen(
    navController: NavController,
    bottomBarPadding: PaddingValues,
    bottomBarState: MutableState<Boolean>,
) {
    composable(
        route = Navigate.Screen.MealPlan.route
    ) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            SoundMapButton()

            var direction by remember { mutableStateOf<Int?>(null) }

            if (showDirection.value) {
                /*LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        try {
                            val sounds = getSoundsListFromServer()
                            //sounds = soundList
                            if (sounds.isNotEmpty()) {
                                direction = sounds[0].toString()
                            }
                        } catch (e: IOException) {
                            direction = "Direction1"
                        }
                    }
                    direction?.let { it1 -> CircleWithFragments(it1) }
                }*/
                //val sounds = s_sound

                Log.e("d_result2", "$s_sound")
                if (s_sound != null) {
                    val directionNames = arrayOf("Direction1", "Direction2", "Direction3", "Direction4", "Direction5", "Direction6", "Direction7", "Direction8")
                    val directionIndex = s_sound.value?.minus(1) ?: 0
                    val directionString = directionNames.getOrNull(directionIndex)
//                    direction = sounds[0];
                    if (directionString != null) {
                        CircleWithFragments(direction = directionString)
                    }
                    Log.e("d_result3", "$directionString")
                }
//                CircleWithFragments(direction = direction.toString())
            } else {
                LoadingAnimation()
            }
        }
    }
}

private val showDirection = mutableStateOf(false)
private val soundList = mutableListOf<Int>()
private var s_sound: MutableState<Int?> = mutableStateOf(null)

@Composable
fun SoundMapButton() {
    val mContext = LocalContext.current
    val recordingInProgress = remember { mutableStateOf(false) }
    val recordingFiles = remember { mutableStateListOf<File>() }
    val buttonText = remember { mutableStateOf("Start the first recording") }
    val fileName_1 = "recording_1_${System.currentTimeMillis()}.3gp"
    val fileName_2 = "recording_2_${System.currentTimeMillis()}.3gp"
    val audioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun startRecording(outputFile: File) {
        GlobalScope.launch {
            showDirection.value = false
            recordingInProgress.value = true
            buttonText.value = "Recording..."
            val recorder = MediaRecorder()
            audioManager.setParameters("audio_source=camcorder")

            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setAudioEncodingBitRate(128000)
            recorder.setAudioSamplingRate(44100)
            recorder.setAudioChannels(2)
            recorder.setOutputFile(outputFile.absolutePath)

            recorder.prepare()
            recorder.start()

            delay(4000)

            recorder.stop()
            recorder.release()
            recordingInProgress.value = false
            recordingFiles.add(outputFile)
            buttonText.value = "Turn your phone to the left"
            delay(2000)
            buttonText.value = "Start the second recording"
        }
    }

    fun startSecondRecording(outputFile: File) {
        GlobalScope.launch {
            recordingInProgress.value = true
            buttonText.value = "Recording..."
            val recorder = MediaRecorder()
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setAudioEncodingBitRate(128000)
            recorder.setAudioSamplingRate(44100)
            recorder.setAudioChannels(2)
            recorder.setOutputFile(outputFile.absolutePath)

            recorder.prepare()
            recorder.start()

            delay(4000)

            recorder.stop()
            recorder.release()
            recordingInProgress.value = false
            recordingFiles.add(outputFile)
            try {
                sendSoundToServer(mContext, recordingFiles[0], recordingFiles[1])
            } catch (e: IOException) {
                e.printStackTrace()
            }
            buttonText.value = "Start the first recording"
            withContext(Dispatchers.Default) {
                delay(2000)
            }
            showDirection.value = true
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(
                    top = 450.dp,
                )
                .height(60.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(15, 82, 186),
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 4.dp),
            onClick = {
                if (!recordingInProgress.value) {
                    if (buttonText.value == "Start the first recording") {
                        val outputFile = File(mContext.getExternalFilesDir(null), fileName_1)
                        startRecording(outputFile)
                    } else if (buttonText.value == "Start the second recording") {
                        val outputFile = File(mContext.getExternalFilesDir(null), fileName_2)
                        startSecondRecording(outputFile)
                    }
                }
            }
        ) {
            ConstraintLayout(modifier = Modifier.fillMaxWidth(0.8f)) {
                val (startIcon, text, endIcon) = createRefs()
                Text(
                    fontSize = 23.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .constrainAs(text) {
                            start.linkTo(startIcon.end, margin = 8.dp)
                            bottom.linkTo(parent.bottom)
                            top.linkTo(parent.top)
                        }
                        .fillMaxWidth(),
                    text = buttonText.value
                )
            }
        }
    }
}

