package net.blueness.servicebestpractice

import android.content.Context
import android.widget.Toast

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