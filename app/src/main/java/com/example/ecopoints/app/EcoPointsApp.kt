package com.example.ecopoints.app

import android.app.Application
import org.osmdroid.config.Configuration
import java.io.File

class EcoPointsApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // osmdroid: identifica la app ante los servidores de mapas y guarda la caché de
        // mapas en el almacenamiento privado (no requiere permisos de almacenamiento).
        Configuration.getInstance().apply {
            userAgentValue = packageName
            osmdroidBasePath = File(cacheDir, "osmdroid")
            osmdroidTileCache = File(osmdroidBasePath, "tiles")
        }
    }
}
