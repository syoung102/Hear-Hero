package app.delish.bookmark

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color.*
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.delish.bookmark.vm.BookMarkViewModel
import app.delish.compose.view.EmptyView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BookMarkScreen(
    bottomBarPadding: PaddingValues,
    onDetails: (Int) -> Unit
) {
    BookMark(
        viewModel = hiltViewModel(),
        bottomBarPadding = bottomBarPadding,
        onDetails = onDetails
    )
}
private val onSounds = mutableSetOf<String>()

private var lastSelectedSound: String = ""

@RequiresApi(Build.VERSION_CODES.O)
@Composable
internal fun BookMark(
    viewModel: BookMarkViewModel,
    bottomBarPadding: PaddingValues,
    onDetails: (Int) -> Unit
) {
    val viewState by viewModel.states.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottomBarPadding)
    ) {
        AnimatedVisibility(visible = !viewState.hasError) {
            LazyColumn(
                modifier = Modifier
                    .statusBarsPadding()
            ) {
                item {
                    MainView(
                        imageResourceId = R.drawable.sounds
                    )
                }

                //item { SoundDetectionNotificationPreview() }
                item { SoundsList() }
            }
        }
        AnimatedVisibility(visible = viewState.hasError) {
            EmptyView(
                titleText = stringResource(id = R.string.ops),
                descText = stringResource(id = R.string.something_went_wrong),
                imageResourceId = R.drawable.recipe_empty
            )
        }
    }
}

@Composable
fun MainView(
    modifier: Modifier = Modifier,
    imageResourceId : Int,
) {
    ConstraintLayout(modifier = modifier.fillMaxSize()) {
        val (image) = createRefs()
        Image(
            painter = painterResource(id = imageResourceId),
            contentDescription = "empty",
            modifier = Modifier
                .constrainAs(image) {
                    linkTo(
                        start = parent.start,
                        end = parent.end,
                    )
                    width = Dimension.ratio("1:2")
                    height = Dimension.ratio("1:0.5")
                }
        )
    }
}

@SuppressLint("LongLogTag")
fun sendSoundToServer(context: Context, file: File, soundName: String, isOn: Boolean) {
    val client = OkHttpClient()

    // Get the previous sound status from SharedPreferences
    val prefs = context.getSharedPreferences("delish_app", Context.MODE_PRIVATE)
    val type = object : TypeToken<HashMap<String, Boolean>>() {}.type
    val soundStatusStr = Gson().fromJson<HashMap<String, Boolean>>(prefs.getString("sound_status", null), type)

    //val soundStatusJson = if (soundStatusStr != null) JSONObject(soundStatusStr) else JSONObject()
    val soundStatusJson = JSONObject()
    // Update the status of the specified sound without overwriting the other values
    //val soundsJson = soundStatusJson.optJSONObject("sound") ?: JSONObject()
    //soundsJson.put(soundName, isOn)
    // Update the status of all sounds
    val allSoundsJson = JSONObject()
    /*allSoundsJson.put("KNOCKING", soundsJson.optBoolean("KNOCKING", false))
    allSoundsJson.put("BARKING", soundsJson.optBoolean("BARKING", false))
    allSoundsJson.put("CAR HORN", soundsJson.optBoolean("CAR HORN", false))
    allSoundsJson.put("SIREN", soundsJson.optBoolean("SIREN", false))
    allSoundsJson.put("RINGTONE", soundsJson.optBoolean("RINGTONE", false))
    allSoundsJson.put("SCREAMING", soundsJson.optBoolean("SCREAMING", false))
    allSoundsJson.put("FIRE ALARM", soundsJson.optBoolean("FIRE ALARM", false))*/
    allSoundsJson.put("KNOCKING", false)
    allSoundsJson.put("BARKING", false)
    allSoundsJson.put("CAR HORN", false)
    allSoundsJson.put("SIREN", false)
    allSoundsJson.put("RINGTONE", false)
    allSoundsJson.put("SCREAMING",false)
    allSoundsJson.put("FIRE ALARM",false)
    for(sound in soundStatusStr){
        allSoundsJson.put(sound.key, sound.value)
    }
    soundStatusJson.put("s_sound", allSoundsJson)

    // Save the updated sound status to SharedPreferences
    //prefs.edit().putString("sound_status", Gson().toJson(soundStatusStr)).apply()

    // Create the request body with the sound status and file data
    val sSoundJson = JSONObject()
    sSoundJson.put("s_sound", allSoundsJson)
    val multiBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("record", file.name, file.asRequestBody("audio/*".toMediaTypeOrNull()))
        .addFormDataPart("s_sound", sSoundJson.toString())
        .build()
    Log.e("multiBody", "multiBody: $file")
    Log.e("multiBody", "multiBody: $sSoundJson")

    // Create the HTTP request and execute it
    val request = Request.Builder()
        .url("http://165.246.243.4:5000/mfcc")
//      .url("http://테스트")
        .post(multiBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("Network Failure", e.message!!)
        }

        override fun onResponse(call: Call, response: Response) {
            if (!response.isSuccessful) {
                Log.e("Unexpected Code", response.code.toString())
                return
            }
            val responseBody = response.body!!.string()
            println(responseBody)

            if (response.code == 200) {
                val jsonObject = JSONObject(responseBody)
                val soundStatus = jsonObject.getInt("r_result")
                SoundDetectionNotificationPreview(context, soundStatuses = listOf(soundStatus))
                Log.e("Server received well", "Server received well")
            } else {
                Log.e("Server didn't receive well", "$response.code")
            }
        }
    })
}

private fun getSoundsListFromServer(): List<Int> {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("http://165.246.243.4:5000/mfcc")
//      .url("http://테스트")
        .build()

    val response = client.newCall(request).execute()
    val jsonStr = response.body?.string()

    if (jsonStr.isNullOrEmpty()) {
        return emptyList()
    }

    try {
        val jsonObject = JSONObject(jsonStr)
        val soundStatus = jsonObject.getInt("r_result")
        Log.e("jsonObject", "$jsonObject")
        Log.e("soundStatus", "$soundStatus")
        return if (soundStatus == -1) {
            emptyList()
        } else {
            listOf(soundStatus)
        }
    } catch (e: JSONException) {
        e.printStackTrace()
        return emptyList()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SoundsList() {
    val sounds = listOf(
        "KNOCKING", "BARKING", "CAR HORN", "SIREN", "RINGTONE", "SCREAMING", "FIRE ALARM"
    )

    val selectedSounds = remember { mutableStateMapOf<String, Boolean>() }
    val mContext = LocalContext.current
    val prefs = mContext.getSharedPreferences("delish_app", Context.MODE_PRIVATE)
    val type = object : TypeToken<HashMap<String, Boolean>>() {}.type
    val soundHashMap = Gson().fromJson<HashMap<String, Boolean>>(prefs.getString("sound_status", null), type)
    if(soundHashMap != null){
        for(soundMap in soundHashMap){
            selectedSounds[soundMap.key] = soundMap.value
        }
    }

    suspend fun recordSound(sound: String, callback: (String?) -> Unit) {
        val fileName = "${sound}_${System.currentTimeMillis()}.3gp"
        val file = File(mContext.getExternalFilesDir(null), fileName)

        try {
            val recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.UNPROCESSED)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
                println("Recording $sound....")
            }

            Thread.sleep(2000)

            recorder.stop()
            recorder.release()

            Thread.sleep(2000)

            // Send sound status to the server
            try {
                Log.e("SendTest", "SendTest")
                GlobalScope.launch(Dispatchers.IO) {
                    sendSoundToServer(mContext, file, sound, selectedSounds[sound] == true)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            callback(file.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            callback(null)
        }
    }
    fun onRecordSoundCompleted(result: String?) {
        if (result != null) {
        } else {
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sounds List",
            fontSize = 24.sp,
            modifier = Modifier.padding(35.dp),
            style = TextStyle(
            ),
            fontWeight = FontWeight.Black
        )
        LazyColumn(
            modifier = Modifier.height(500.dp)
        ) {
            items(sounds) { sound ->
//                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LocalContext.current)
//                val savedOnSounds = sharedPreferences.getStringSet("on_sounds", emptySet())
//                onSounds.addAll(savedOnSounds ?: emptySet())

                val isSelected = selectedSounds[sound] ?: false
                Rows(name = sound, change = isSelected) { selected ->
                    selectedSounds[sound] = selected
                    prefs.edit().putString("sound_status", Gson().toJson(selectedSounds)).apply()
                    if (selected) {
                        // Record sound when the switch is turned on
                        lastSelectedSound = sound
                        GlobalScope.launch {
                            recordSound(sound, ::onRecordSoundCompleted)
                            onSounds.add(sound)
                            while (lastSelectedSound.isNotEmpty()) {
                                recordSound(lastSelectedSound, ::onRecordSoundCompleted)
                            }
                        }
                    } else {
                        if(sound == lastSelectedSound){
                            lastSelectedSound = ""
                        }
                        onSounds.remove(sound)
                    }
                }
                Divider()
            }
        }
    }
//    // Save the list of currently playing sounds to shared preferences
//    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
//    val editor = sharedPreferences.edit()
//    editor.putStringSet("on_sounds", onSounds.toSet())
//    editor.apply()
}

fun SoundDetectionNotification(context: Context, sounds: List<String>) {
    val validSounds = listOf(
        "KNOCKING", "BARKING", "CAR HORN", "SIREN", "RINGTONE", "SCREAMING", "FIRE ALARM"
    )

    val filteredSounds = sounds.filter { validSounds.contains(it) }

    if (filteredSounds.isNotEmpty()) {
        val detectedSounds = filteredSounds.joinToString(separator = " & ")

        if (context is Activity) {
            context.runOnUiThread {
                Toast.makeText(context, "Detected: $detectedSounds", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

fun SoundDetectionNotificationPreview(context: Context, soundStatuses: List<Int>) {
    /*var soundStatuses by remember { mutableStateOf(listOf<Int>()) }
    Thread {
        try {
            soundStatuses = getSoundsListFromServer()
        } catch (e: Exception) {
            Log.e("NotificationPreview", "Error: ${e.message}")
            soundStatuses = emptyList()
        }
    }.start()*/
    if (soundStatuses.isNotEmpty()) {
        val sounds = listOf(
            "KNOCKING", "BARKING", "CAR HORN", "SIREN", "RINGTONE", "SCREAMING", "FIRE ALARM"
        )
        val soundNames = soundStatuses.map { sounds.getOrElse(it - 1) { "" } }
        SoundDetectionNotification(context, soundNames)
    }
}

@Composable
private fun Rows(name:String, change:Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = name,
            modifier = Modifier
                .padding(15.dp)
                .weight(3f),
            textAlign = TextAlign.Start,
        )

        Switch(
            checked = change,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                uncheckedThumbColor = Color.White,
                checkedTrackColor = Color.Green,
                uncheckedTrackColor = Color.Gray
            ),
            modifier = Modifier.weight(1f)
        )
    }
}
