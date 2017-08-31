package net.blueness.servicebestpractice

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.Button
import android.widget.Toast
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import java.util.jar.Manifest

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val downloadBinder: DownloadService.DownloadBinder? = null
    private val connection = object : ServiceConnection{
        override fun onServiceDisconnected(name: ComponentName?) {}

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val downloadBinder = service as DownloadService.DownloadBinder
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startDownload: Button = find(R.id.start_download)
        val pauseDownload: Button = find(R.id.pause_download)
        val cancelDownload: Button = find(R.id.cancel_download)
        startDownload.setOnClickListener(this)
        pauseDownload.setOnClickListener(this)
        cancelDownload.setOnClickListener(this)
        val intent = Intent(this, DownloadService::class.java)
        startService(intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
    }

    override fun onClick(v: View?) {
        if (downloadBinder == null) {
            return
        }
        when (v?.id) {
            R.id.start_download -> downloadBinder.startDownload("http://mirrors.163.com/archlinux/iso/2017.08.01/archlinux-2017.08.01-x86_64.iso")
            R.id.pause_download -> downloadBinder.pauseDownload()
            R.id.cancel_download -> downloadBinder.cancelDownload()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            1 ->
                    if (grantResults.size > 0 && grantResults.get(0) != PackageManager.PERMISSION_GRANTED){
                        toast("拒绝权限将无法使用该程序")
                        finish()
                    }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }
}
