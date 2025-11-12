package takagi.ru.fleur

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import takagi.ru.fleur.di.WorkManagerModule
import javax.inject.Inject

/**
 * Fleur Application class with Hilt dependency injection
 */
@HiltAndroidApp
class FleurApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    @WorkManagerModule.EmailSyncWorkName
    lateinit var emailSyncWorkName: String
    
    @Inject
    @WorkManagerModule.SyncQueueWorkName
    lateinit var syncQueueWorkName: String
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
