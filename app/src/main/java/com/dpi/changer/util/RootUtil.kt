package com.dpi.changer.util

import java.io.BufferedReader
import java.io.InputStreamReader

object RootUtil {
    
    fun isDeviceRooted(): Boolean {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3()
    }

    private fun checkRootMethod1(): Boolean {
        val buildTags = android.os.Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    private fun checkRootMethod2(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        return paths.any { java.io.File(it).exists() }
    }

    private fun checkRootMethod3(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            bufferedReader.readLine() != null
        } catch (t: Throwable) {
            false
        } finally {
            process?.destroy()
        }
    }

    fun executeWithRoot(command: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val os = process.outputStream
            os.write("$command\n".toByteArray())
            os.write("exit\n".toByteArray())
            os.flush()
            process.waitFor() == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun setDPI(dpi: Int): Boolean {
        return executeWithRoot("wm density $dpi")
    }

    fun resetDPI(): Boolean {
        return executeWithRoot("wm density reset")
    }

    fun getCurrentDPI(): Int? {
        return try {
            val process = Runtime.getRuntime().exec("su -c wm density")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            process.waitFor()
            
            // Parse output: "Physical density: XXX" or "Override density: XXX"
            val regex = Regex("""density: (\d+)""")
            regex.find(output)?.groupValues?.get(1)?.toInt()
        } catch (e: Exception) {
            null
        }
    }
}