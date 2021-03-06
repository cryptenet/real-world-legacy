package com.cryptenet.real_world_legacy

import android.content.Context
import com.google.android.play.core.splitcompat.SplitCompatApplication
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.androidXModule
import timber.log.Timber

@Suppress("unused")
class RealWorldLegacyApplication : SplitCompatApplication(), DIAware {
    override val di by DI.lazy {
        import(androidXModule(this@RealWorldLegacyApplication))
    }

    private lateinit var context: Context

    override fun onCreate() {
        super.onCreate()

        context = this

        initTimber()
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
