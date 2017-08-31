package net.blueness.servicebestpractice

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Environment
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.app.ServiceCompat
import android.widget.Toast
import java.io.File

class DownloadService : Service() {

    private var downloadTask: DownloadTask? = null
    private var downloadUrl: String? = null

    val listener = object : DownloadListener{

        override fun onProgress(progress: Int) {
            notificationManager.notify(1, notification("Downloading...", progress))
        }

        override fun onSuccess(){
            downloadTask = null
            stopForeground(true)
            notificationManager.notify(1, notification("Download Success", -1))
            Toast.makeText(this@DownloadService, "Download Success", Toast.LENGTH_SHORT).show()
        }

        override fun onFailed() {
            downloadTask = null
            stopForeground(true)
            notificationManager.notify(1, notification("Downlaod Failed", -1))
            Toast.makeText(this@DownloadService, "Downlaod Failed", Toast.LENGTH_SHORT).show()
        }

        override fun onPaused() {
            downloadTask = null
            Toast.makeText(this@DownloadService, "Paused", Toast.LENGTH_SHORT).show()
        }

        override fun onCanceled() {
            downloadTask = null
            stopForeground(true)
            Toast.makeText(this@DownloadService, "Canceled", Toast.LENGTH_SHORT).show()
        }
    }

    val mBinder: DownloadBinder = DownloadBinder()


    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    inner class DownloadBinder() : Binder(){
        fun startDownload(url: String){
            if(downloadTask == null){
                downloadUrl = url
                downloadTask = DownloadTask(listener)
                downloadTask!!.execute(downloadUrl)
                startForeground(1, notification("Downloading...", 0))
                Toast.makeText(this@DownloadService, "Downloading...", Toast.LENGTH_SHORT).show()
            }
        }

        fun pauseDownload() {
            if (downloadTask != null) {
                downloadTask?.pauseDownload()
            }
        }

        fun cancelDownload(){
            if (downloadTask != null){
                downloadTask?.cancelDownload()
            }
            if (downloadUrl != null){
//                删除文件，关闭通知
                val fileName = downloadUrl!!.substring(downloadUrl!!.lastIndexOf("/"))
                val directory: String = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
                val file = File(directory + fileName)
                if (file.exists()) {
                    file.delete()
                }
                notificationManager.cancel(1)
                stopForeground(true)
                Toast.makeText(this@DownloadService, "Canceled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val notificationManager: NotificationManager
        get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val notification: (String, Int) -> Notification ={
        title: String, progress: Int ->
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val builder = NotificationCompat.Builder(this)
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
        builder.setContentIntent(pendingIntent)
        builder.setContentTitle(title)
        if (progress >= 0){
            builder.setContentText("$progress%")
            builder.setProgress(100, progress, false)
        }
        builder.build()
    }
}

