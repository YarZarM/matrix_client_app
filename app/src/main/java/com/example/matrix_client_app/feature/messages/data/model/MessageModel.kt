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
                    "m.image" -> "Image"
                    "m.file" -> "File"
                    "m.video" -> "Video"
                    "m.audio" -> "Audio"
                    else -> body ?: "[Unknown message type]"
                }
            }
            "m.room.member" -> {
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

    fun isVisibleEvent(): Boolean {
        return when (type) {
            "m.room.message" -> true
            "m.room.member" -> true
            "m.room.name" -> true
            "m.room.topic" -> true
            "m.room.avatar" -> true
            "m.room.create" -> true
            else -> false
        }
    }

    @OptIn(ExperimentalTime::class)
    fun getFormattedTime(): String {
        val now = Clock.System.now()
        val messageInstant = Instant.fromEpochMilliseconds(originServerTs)
        val timeDiff = now - messageInstant
        val timeZone = TimeZone.currentSystemDefault()
        val messageDateTime = messageInstant.toLocalDateTime(timeZone)
        val nowDateTime = now.toLocalDateTime(timeZone)

        return when {
            timeDiff < 1.minutes -> "Just now"

            timeDiff < 1.hours -> {
                val mins = (timeDiff.inWholeMinutes).toInt()
                "$mins min ago"
            }

            messageDateTime.date == nowDateTime.date -> {
                formatTime(messageDateTime)
            }

            messageDateTime.date == nowDateTime.date.minus(1, kotlinx.datetime.DateTimeUnit.DAY) -> {
                "Yesterday ${formatTime(messageDateTime)}"
            }

            timeDiff < 7.days -> {
                "${getDayName(messageDateTime.dayOfWeek)} ${formatTime(messageDateTime)}"
            }

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