package net.blueness.servicebestpractice

/**
 * Created by Blueness on 2017/8/30.
 */
interface DownloadListener{

    fun onProgress(progress: Int)

    fun onSuccess()

    fun onFailed()

    fun onPaused()

    fun onCanceled()
}