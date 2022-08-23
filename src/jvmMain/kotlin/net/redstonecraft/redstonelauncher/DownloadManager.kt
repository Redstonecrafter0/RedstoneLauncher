package net.redstonecraft.redstonelauncher

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import net.redstonecraft.redstonelauncher.api.OS
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.File
import java.io.FileInputStream
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.zip.ZipFile
import kotlin.io.path.createTempDirectory
import kotlin.io.path.createTempFile

object DownloadManager {

    val runningDownloads = ConcurrentHashMap<String, Double>()

    val threadPool = Executors.newCachedThreadPool()

    fun start(url: String, key: String, destination: File = createTempFile().toFile(), onError: () -> Unit = {}, postProcess: (destination: File) -> Unit) {
        threadPool.submit {
            try {
                runningDownloads += key to .0
                val buffer = ByteArray(8192)
                var progress = 0
                val conn = URL(url).openConnection()
                val total = conn.getHeaderField("Content-Length").toInt()
                destination.outputStream().use { out ->
                    conn.inputStream.use {
                        while (progress < total) {
                            val len = it.read(buffer)
                            progress += len
                            out.write(buffer, 0, len)
                            runningDownloads[key] = (progress.toDouble() / total.toDouble())
                        }
                    }
                }
                runningDownloads[key] = -1.0
                runningDownloads -= key
                postProcess(destination)
            } catch (e: Throwable) {
                e.printStackTrace()
                runningDownloads -= key
                onError()
            }
            destination.deleteRecursively()
        }
    }
}

fun File.unpackToTempDir(): File {
    try {
    val dir = createTempDirectory().toFile()
    val dirPath = dir.canonicalPath
    when (OS.current) {
        OS.WINDOWS -> ZipFile(this).use {
            for (entry in it.entries()) {
                val file = File(dir, entry.name)
                if (!entry.isDirectory && file.canonicalPath.startsWith(dirPath)) {
                    if (!(file.parentFile.exists() && file.parentFile.isDirectory)) file.parentFile.mkdirs()
                    it.getInputStream(entry).use { stream ->
                        file.writeBytes(stream.readAllBytes())
                    }
                }
            }
        }
        OS.LINUX -> TarArchiveInputStream(GzipCompressorInputStream(FileInputStream(this))).use {
            var entry = it.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    val buffer = ByteArray(entry.size.toInt())
                    it.read(buffer, 0, buffer.size)
                    val file = File(dir, entry.name)
                    if (file.canonicalPath.startsWith(dirPath)) {
                        if (!(file.parentFile.exists() && file.parentFile.isDirectory)) file.parentFile.mkdirs()
                        file.writeBytes(buffer)
                    }
                }
                entry = it.nextEntry
            }
        }
    }
    return dir
    } catch (e: Throwable) {
        e.printStackTrace()
        throw e
    }
}
