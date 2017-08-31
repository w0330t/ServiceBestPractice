package net.blueness.servicebestpractice

import android.os.AsyncTask
import android.os.Environment
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile

/**
 * Created by Blueness on 2017/8/31.
 */

class DownloadTask: AsyncTask<String, Int, Int>() {

    val FINAL_TYPE_SUCCESS = 0
    val FINAL_TYPE_FAILED = 1
    val FINAL_TYPE_PAUSED = 2
    val FINAL_TYPE_CANCELED = 3

    private var listener: DownloadListener? = null
    private var isCanceled = false
    private var isPaused = false
    private var lastProgress: Int? = null

    override fun doInBackground(vararg params: String?): Int {
        var inputStream: InputStream? = null
        var savedFile: RandomAccessFile? = null
        var file: File? = null
        try {
            var downloadedLength: Long = 0
            val downloadUrl: String? = params[0]
            val directory: String = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
            val fileName = downloadUrl?.substring(downloadUrl.lastIndexOf("/"))

            file = File(directory, fileName)
            if (file.exists()) {
                downloadedLength = file.length()
            }

//            判定已有文件是否和网络的文件相同
            val contentLength: Long = getContentLength(downloadUrl)
            if (contentLength == 0L) {
                return FINAL_TYPE_FAILED
            }
            else if (contentLength == downloadedLength){
                return FINAL_TYPE_SUCCESS
            }

//            判定是否断点续传并开始下载
            val client = OkHttpClient()
            val request = Request.Builder()
                    .addHeader("RANGE", "bytes=" + downloadedLength + "-")
                    .url(downloadUrl)
                    .build()
            val response = client.newCall(request).execute()

//            下载中的判定
            if (response != null) {
                inputStream = response.body()?.byteStream()
                savedFile = RandomAccessFile(file, "rw")
                savedFile.seek(downloadedLength) //跳过已经下载的字节
                val b = ByteArray(1024)
                var total: Int = 0
                var len: Int? = null
                do {
                    if (isCanceled) {
                        return FINAL_TYPE_CANCELED
                    }
                    else if (isPaused) {
                        return FINAL_TYPE_PAUSED
                    }
                    else {
                        total += len!!
                        savedFile!!.write(b, 0, len!!)
//                        计算百分比
                        val progress: Int = ((total + downloadedLength) * 100 / contentLength) as Int
                        publishProgress(progress)
                    }

                    len = inputStream?.read(b)
                } while (len != -1)

                //上面循环完成后下载完毕
                response.body()!!.close()
                return FINAL_TYPE_SUCCESS
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
//            一堆关闭
            if (inputStream != null){
                inputStream.close()
            }
            if (savedFile != null) {
                savedFile.close()
            }
            if (isCanceled && file != null){
                file.delete()
            }
        }
        return FINAL_TYPE_FAILED
    }


    override fun onProgressUpdate(vararg values: Int?) {
//        获取进度，如果更新则重新赋值
        val progress = values[0]
        if (progress != null) {
            if (progress > this.lastProgress!!){
                listener?.onProgress(progress)
                lastProgress = progress
            }

        }
    }

    override fun onPostExecute(status: Int?) {
        when (status){
            FINAL_TYPE_SUCCESS -> listener?.onSuccess()
            FINAL_TYPE_FAILED -> listener?.onFailed()
            FINAL_TYPE_PAUSED -> listener?.onPaused()
            FINAL_TYPE_CANCELED -> listener?.onCanceled()
        }
    }

    fun pauseDownload() {
        isPaused = true
    }

    fun cancelDownload() {
        isCanceled = true
    }


    fun getContentLength(downloadUrl: String?): Long {
        val client = OkHttpClient()
        val request = Request.Builder()
                .url(downloadUrl)
                .build()
        val response = client.newCall(request).execute()
        if (response != null && response.isSuccessful) {
            val contentLength = response.body()?.contentLength()
            response.body()?.close()
            return contentLength
        }
        return 0
    }
}