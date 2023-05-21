package me.rhunk.snapchatdark

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.Switch
import android.widget.Toast

class MainActivity : Activity() {
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toggleSwitch = findViewById<Switch>(R.id.toggleDarkMode)
        toggleSwitch.isChecked = runCatching {
            Runtime.getRuntime().exec("su -c cmd overlay list").inputStream.bufferedReader().readText().contains("[x] $packageName")
        }.onFailure {
            Toast.makeText(this, "No root access $it", Toast.LENGTH_LONG).show()
            finish()
        }.getOrNull() ?: false

        toggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            val process = Runtime.getRuntime().exec("su -c cmd overlay ${if (isChecked) "enable" else "disable"} $packageName")
            process.waitFor()
            if (process.exitValue() != 0) {
                Toast.makeText(this, "Failed ${process.errorStream.bufferedReader().readText()}", Toast.LENGTH_LONG).show()
            }
        }
    }
}