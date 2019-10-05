package com.simplecityapps.playback.chromecast

import android.net.Uri
import fi.iki.elonen.NanoHTTPD
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

class HttpServer(private val castService: CastService) : NanoHTTPD(5000) {

    override fun serve(session: IHTTPSession): Response {

        val uri = Uri.parse(session.uri)

        val paths = uri.pathSegments
        if (paths.contains("songs")) {
            val songId = paths[paths.indexOf("songs") + 1].toLong()

            when (uri.lastPathSegment) {
                "audio" -> {
                    castService.getAudio(songId)?.let { audioStream ->
                        return serveAudio(session.headers, audioStream.stream, audioStream.length, audioStream.mimeType)
                    }
                }
                "artwork" -> {
                    castService.getArtwork(songId)?.let { byteArray ->
                        return serveArtwork(ByteArrayInputStream(byteArray), "image/jpeg", byteArray.size.toLong())
                    }
                }
            }
        }
        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html", "File not found")
    }

    private fun serveAudio(headers: MutableMap<String, String>, inputStream: InputStream, length: Long, mimeType: String): Response {
        try {
            var range: String? = null
            for (key in headers.keys) {
                if ("range" == key) {
                    range = headers[key]
                }
            }

            if (range == null) {
                range = "bytes=0-"
                headers["range"] = range
            }

            val start: Long
            var end: Long

            val rangeValue = range.trim { character -> character <= ' ' }.substring("bytes=".length)

            if (rangeValue.startsWith("-")) {
                end = length - 1
                start = length - 1 - java.lang.Long.parseLong(rangeValue.substring("-".length))
            } else {
                val ranges = rangeValue.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                start = java.lang.Long.parseLong(ranges[0])
                end = if (ranges.size > 1) java.lang.Long.parseLong(ranges[1]) else length - 1
            }
            if (end > length - 1) {
                end = length - 1
            }

            if (start <= end) {
                val contentLength = end - start + 1
                inputStream.skip(start)
                val response = newFixedLengthResponse(Response.Status.PARTIAL_CONTENT, mimeType, inputStream, contentLength)
                response.addHeader("Content-Length", contentLength.toString() + "")
                response.addHeader("Content-Range", "bytes $start-$end/$length")
                response.addHeader("Content-Type", mimeType)
                return response
            } else {
                return newFixedLengthResponse(Response.Status.RANGE_NOT_SATISFIABLE, "text/html", range)
            }
        } catch (e: IOException) {
            Timber.e(e, "Error serving audio")
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html", "File not found")
    }

    private fun serveArtwork(inputStream: InputStream, mimeType: String, length: Long): Response {
        return newFixedLengthResponse(Response.Status.OK, mimeType, inputStream, length)
    }
}