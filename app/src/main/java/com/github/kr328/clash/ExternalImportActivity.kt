package com.github.kr328.clash

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.common.util.setUUID
import com.github.kr328.clash.service.model.Profile
import com.github.kr328.clash.util.withProfile
import com.hiddify.clash.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ExternalImportActivity : Activity(), CoroutineScope by MainScope() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = intent.data ?: return finish()
        val formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
        val current = LocalDateTime.now().format(formatter)


        if ("clash" != uri.scheme){

            launch {
                Utils.addClashProfile(applicationContext,uri.toString())
                startActivity(MainActivity::class.intent)

                finish()
            }
        }
        if (intent.action != Intent.ACTION_VIEW)
            return finish()


        val url = uri.getQueryParameter("url") ?: return finish()

        launch {
            val uuid = withProfile {
                val type = when (uri.getQueryParameter("type")?.lowercase(Locale.getDefault())) {
                    "url" -> Profile.Type.Url
                    "file" -> Profile.Type.File
                    else -> Profile.Type.Url
                }
                val name = uri.getQueryParameter("name") ?: current

                create(type, name).also {
                    patch(it, name, url, 0);
                    queryByUUID(it)?.let { it1 -> setActive(it1) }
                }

            }

            startActivity(PropertiesActivity::class.intent.setUUID(uuid))

            finish()
        }
    }
}