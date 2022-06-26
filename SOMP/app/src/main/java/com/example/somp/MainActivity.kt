package com.example.somp

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.somp.fragments.LibraryFragment
import com.example.somp.fragments.SearchFragment
import com.example.somp.fragments.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import androidx.media.app.NotificationCompat as MediaNotificationCompat


class MainActivity : AppCompatActivity() {

    private val libraryFragment = LibraryFragment()
    private val searchFragment = SearchFragment()
    private val settingsFragment = SettingsFragment()

    private val mediaPlayer: MediaPlayer = MediaPlayer()

    private var isPaused = false
    private var isRandom = false

    private lateinit var audioList : MutableList<AudioClass.Audio>

    private lateinit var receiver : BroadcastReceiver
    private lateinit var receiver2 : BroadcastReceiver
    private lateinit var receiver3 : BroadcastReceiver

    companion object {
        const val NOTIFICATION_ID = 101
        const val CHANNEL_ID = "1111"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // Разрешение предоставлено.
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                } else {
                    // Объяснение пользователю, что функция недоступна, потому что
                    // функции требуют разрешения, которое пользователь запретил.
                    createAlertMessage()
                }
            }
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {

                audioList = AudioClass().mp3ReaderNew(application = this.applicationContext as Application).toMutableList()

                replaceFragment(libraryFragment, "Библиотека")

                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

                val bot = findViewById<BottomNavigationView>(R.id.bottom_navigation)

                bot.setOnNavigationItemSelectedListener{
                    when(it.itemId){
                        R.id.ic_library -> replaceFragment(libraryFragment, "Библиотека")
                        R.id.ic_search -> replaceFragment(searchFragment, "Поиск")
                        R.id.ic_settings -> replaceFragment(settingsFragment, "Настройки")
                    }
                    true
                }

                val imageButton = findViewById<ImageButton>(R.id.imageButton)
                imageButton.setOnClickListener { btnPause() }

                val btnNext = findViewById<ImageButton>(R.id.imageNext)
                btnNext.setOnClickListener { setMusicNext() }

                val btnPrevious = findViewById<ImageButton>(R.id.imagePrevious)
                btnPrevious.setOnClickListener { setMusicPrevious() }

                val btnRandom = findViewById<ImageButton>(R.id.imageRandom)
                btnRandom.setOnClickListener { randomMusic() }


                mediaPlayer.setOnCompletionListener {
                    setMusicNext()
                }
                createNotificationChannel()

                receiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        when(intent.action){
                            "previous" -> setMusicPrevious()
                        }
                    }
                }
                receiver2 = object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        when(intent.action){
                            "pause" -> btnPause()
                        }
                    }
                }
                receiver3 = object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        when(intent.action){
                            "next" -> setMusicNext()
                        }
                    }
                }
                this.registerReceiver(receiver, IntentFilter("previous"))
                this.registerReceiver(receiver2, IntentFilter("pause"))
                this.registerReceiver(receiver3, IntentFilter("next"))

            }
            else -> {
                // Напрямую запрашиваем разрешение.
                // Зарегистрированный ActivityResultCallback получает результат этого запроса.
                requestPermissionLauncher.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }
     private fun unRegisterReceiver(r1:BroadcastReceiver, r2:BroadcastReceiver, r3:BroadcastReceiver){
         this.unregisterReceiver(r1)
         this.unregisterReceiver(r2)
         this.unregisterReceiver(r3)
     }
    override fun onDestroy() {
        super.onDestroy()
        unRegisterReceiver(receiver, receiver2, receiver3)
    }
    private fun createAlertMessage(){
        val builder: AlertDialog.Builder = this.let {
            AlertDialog.Builder(it)
        }

        builder.setMessage("Без этого разрешения приложение не сможет просматривать музыку с вашего устройства. Вы хотите дать разрешение?")
            ?.setTitle("Доступ запрещён")

        builder.apply {
            setPositiveButton("Предоставить разрешение"
            ) { _, _ ->
                // Пользователь нажал кнопку ОК
                val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts(
                    "package",
                    packageName, null
                )
                intent.data = uri
                startActivity(intent)
            }
            setNegativeButton("Отмена"
            ) { _, _ ->
                //Пользователь отменил диалог
            }
        }

        builder.create()
        builder.show()
    }

    private fun createNotificationChannel() {
        // Создаем NotificationChannel, но только в API 26+, потому что
        // класс NotificationChannel новый, его нет в библиотеке поддержки
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Music"
            val descriptionText = "Music"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Регистрируем канал в системе
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun createMusicNotification(musicName: String, musicData: String){

        val pauseIntent = Intent()
        pauseIntent.action = "pause"
        val pausePendingIntent: PendingIntent = PendingIntent.getBroadcast(this, 0, pauseIntent, 0)
        val previousIntent = Intent()
        previousIntent.action = "previous"
        val previousPendingIntent: PendingIntent = PendingIntent.getBroadcast(this, 0, previousIntent, 0)
        val nextIntent = Intent()
        nextIntent.action = "next"
        val nextPendingIntent: PendingIntent = PendingIntent.getBroadcast(this, 0, nextIntent, 0)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            // Показываем элементы управления на экране блокировки, даже если пользователь скрывает конфиденциальный контент.
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic__music_note)
            .addAction(R.drawable.ic_skip_previous, "previous", previousPendingIntent)
            .addAction(R.drawable.ic_baseline_pause_24, "pause", pausePendingIntent)
            .addAction(R.drawable.ic_skip_next, "next", nextPendingIntent)
            .setStyle(MediaNotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2))
            // Применяем медиа стиль уведомления.
            .setContentTitle(musicName)
            .setContentText(musicData)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)


        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, notification.build()) // посылаем уведомление
        }
    }
    private fun randomMusic(){
        val btnRandom = findViewById<ImageButton>(R.id.imageRandom)
        isRandom = if (!isRandom){
            audioList.shuffle()
            btnRandom.setColorFilter(Color.GRAY)
            true
        }
        else{
            audioList.sortBy { it.name }
            btnRandom.setColorFilter(Color.WHITE)
            false
        }
    }

    private fun setMusicNext(){
        val musicName = textView.text.toString().plus(".mp3")
        val num = audioList.indexOf(audioList.find { it.name == musicName })
        if (num < audioList.count()-1){
            val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioList[num+1].id)
            mediaPlayer.stop()
            mediaPlayer.reset()
            mediaPlayer.setDataSource(this, uri)
            mediaPlayer.prepare()
            mediaPlayer.start()
            textView.text = audioList[num+1].name.replace(".mp3", "")

            val drawable = getDrawable(R.drawable.ic_baseline_pause_24)
            imageButton.setImageDrawable(drawable)

            createMusicNotification(audioList[num+1].name.replace(".mp3", ""), audioList[num+1].artist)
        }
        else{
            setMusic(audioList[0].name, audioList)
        }
    }

    private fun setMusicPrevious(){
        val musicName = textView.text.toString().plus(".mp3")
        val num = audioList.indexOf(audioList.find { it.name == musicName })
        if (num > 0){
            val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioList[num-1].id)
            mediaPlayer.stop()
            mediaPlayer.reset()
            mediaPlayer.setDataSource(this, uri)
            mediaPlayer.prepare()
            mediaPlayer.start()
            textView.text = audioList[num-1].name.replace(".mp3", "")


            val drawable = getDrawable(R.drawable.ic_baseline_pause_24)
            imageButton.setImageDrawable(drawable)

            createMusicNotification(audioList[num-1].name.replace(".mp3", ""), audioList[num-1].artist)
        }
        else{
            setMusic(audioList[audioList.count()-1].name, audioList)
        }
    }

    fun setMusic(songName: String, audioList: List<AudioClass.Audio>){
        try {
            mediaPlayer.stop()
            mediaPlayer.reset()
            val num = audioList.indexOf(audioList.find { it.name == songName })
            val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioList[num].id)
            mediaPlayer.setDataSource(this, uri)
            mediaPlayer.prepare()
            mediaPlayer.start()
            textView.text = audioList[num].name.replace(".mp3", "")

            val drawable = getDrawable(R.drawable.ic_baseline_pause_24)
            imageButton.setImageDrawable(drawable)

            createMusicNotification(audioList[num].name.replace(".mp3", ""), audioList[num].artist)
        }
        catch (e: Exception){
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }
    }

    fun btnPause(){
        try {
            isPaused = if(!mediaPlayer.isPlaying && mediaPlayer.duration != 0) {
                val drawable = getDrawable(R.drawable.ic_baseline_pause_24)
                imageButton.setImageDrawable(drawable)
                mediaPlayer.start()
                false
            } else {
                val drawable = getDrawable(R.drawable.ic_play_arrow)
                imageButton.setImageDrawable(drawable)
                mediaPlayer.pause()
                true
            }
        }
        catch (e: Exception){
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private fun replaceFragment(fragment: Fragment, title: String){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, fragment)
        transaction.commit()

        supportActionBar?.title = title
    }

}