package io.mohammedalaamorsi.followy.shared

import android.content.Context
import android.os.Build

class AndroidPlatform(override val context: Context) : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}
