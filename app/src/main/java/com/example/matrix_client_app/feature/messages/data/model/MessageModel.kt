package com.example.matrix_client_app.feature.messages.data.model

import android.R.attr.timeZone
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.compareTo
import kotlin.text.toInt
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@JsonClass(generateAdapter = true)
data class MessageResponse(
    @Json(name = "chunk")
    val chunk: List<ClientEvent>,

    @Json(name = "end")
    val end: String,

    @Json(name = "start")
    val start: String,

)

@JsonClass(generateAdapter = true)
data class ClientEvent(
    @Json(name = "content")
    val content: Map<String,Any?>,

    @Json(name = "event_id")
    val eventId: String,

    @Json(name = "origin_server_ts")
    val originServerTs: Long,

    @Json(name = "room_id")
    val roomId: String,

    @Json(name = "sender")
    val sender: String,

    @Json(name = "type")
    val type: String
) {
    fun getSenderDisplayName(): String {
        return sender.removePrefix("@").substringBefore(":")
    }

    fun getMessageBody(): String {
        return when (type) {
            "m.room.message" -> {
                val body = content["body"] as? String
                val msgtype = content["msgtype"] as? String

                when (msgtype) {
                    "m.text" -> body ?: "[Empty message]"
                    "m.image" -> "📷 Image"
                    "m.file" -> "📎 File"
                    "m.video" -> "🎥 Video"
                    "m.audio" -> "🎵 Audio"
                    else -> body ?: "[Unknown message type]"
                }
            }
            "m.room.member" -> {
                // Membership events (user joined, left, etc.)
                val membership = content["membership"] as? String
                when (membership) {
                    "join" -> "${getSenderDisplayName()} joined the room"
                    "leave" -> "${getSenderDisplayName()} left the room"
                    "invite" -> "${getSenderDisplayName()} was invited"
                    else -> "[Membership event]"
                }
            }
            "m.room.create" -> "[Room created]"
            "m.room.name" -> {
                val name = content["name"] as? String
                "Room name changed to: $name"
            }
            "m.room.topic" -> {
                val topic = content["topic"] as? String
                "Room topic: $topic"
            }
            else -> "[System event: $type]"
        }
    }

    fun isTextMessage(): Boolean {
        return type == "m.room.message" && content["msgtype"] == "m.text"
    }

    @OptIn(ExperimentalTime::class)
    fun getFormattedTime(): String {
        // Step 1: Get current time as Instant
        val now = Clock.System.now()

        // Step 2: Convert message timestamp to Instant
        // originServerTs is in milliseconds, Instant.fromEpochMilliseconds expects milliseconds
        val messageInstant = Instant.fromEpochMilliseconds(originServerTs)

        // Step 3: Calculate time difference
        val timeDiff = now - messageInstant

        // Step 4: Get system timezone
        val timeZone = TimeZone.currentSystemDefault()

        // Step 5: Convert to LocalDateTime for formatting
        val messageDateTime = messageInstant.toLocalDateTime(timeZone)
        val nowDateTime = now.toLocalDateTime(timeZone)

        // Step 6: Format based on how old the message is
        return when {
            // Less than 1 minute ago
            timeDiff < 1.minutes -> "Just now"

            // Less than 1 hour ago
            timeDiff < 1.hours -> {
                val mins = (timeDiff.inWholeMinutes).toInt()
                "$mins min ago"
            }

            // Same day (today)
            messageDateTime.date == nowDateTime.date -> {
                formatTime(messageDateTime)
            }

            // Yesterday
            messageDateTime.date == nowDateTime.date.minus(1, kotlinx.datetime.DateTimeUnit.DAY) -> {
                "Yesterday ${formatTime(messageDateTime)}"
            }

            // Within last 7 days (this week)
            timeDiff < 7.days -> {
                "${getDayName(messageDateTime.dayOfWeek)} ${formatTime(messageDateTime)}"
            }

            // Older than a week
            else -> {
                formatDate(messageDateTime)
            }
        }
    }

    private fun formatTime(dateTime: LocalDateTime): String {
        val hour = dateTime.hour.toString().padStart(2, '0')
        val minute = dateTime.minute.toString().padStart(2, '0')
        return "$hour:$minute"
    }

    private fun formatDate(dateTime: LocalDateTime): String {
        val month = getMonthAbbreviation(dateTime.monthNumber)
        val day = dateTime.dayOfMonth
        return "$month $day"
    }

    private fun getMonthAbbreviation(monthNumber: Int): String {
        return when (monthNumber) {
            1 -> "Jan"
            2 -> "Feb"
            3 -> "Mar"
            4 -> "Apr"
            5 -> "May"
            6 -> "Jun"
            7 -> "Jul"
            8 -> "Aug"
            9 -> "Sep"
            10 -> "Oct"
            11 -> "Nov"
            12 -> "Dec"
            else -> "???"
        }
    }

    private fun getDayName(dayOfWeek: kotlinx.datetime.DayOfWeek): String {
        return when (dayOfWeek) {
            kotlinx.datetime.DayOfWeek.MONDAY -> "Mon"
            kotlinx.datetime.DayOfWeek.TUESDAY -> "Tue"
            kotlinx.datetime.DayOfWeek.WEDNESDAY -> "Wed"
            kotlinx.datetime.DayOfWeek.THURSDAY -> "Thu"
            kotlinx.datetime.DayOfWeek.FRIDAY -> "Fri"
            kotlinx.datetime.DayOfWeek.SATURDAY -> "Sat"
            kotlinx.datetime.DayOfWeek.SUNDAY -> "Sun"
        }
    }

}