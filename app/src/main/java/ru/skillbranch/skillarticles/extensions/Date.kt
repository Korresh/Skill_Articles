package ru.skillbranch.skillarticles.extensions

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

const val SECOND = 1000L
const val MINUTE = 60 * SECOND
const val HOUR = 60 * MINUTE
const val DAY = 24 * HOUR


fun Date.format(pattern:String="HH:mm:ss dd.MM.yy"):String{
    val dateformat = SimpleDateFormat(pattern, Locale("ru"))
    return dateformat.format(this)
}

fun Date.shortFormat(): String {
    val pattern = if(this.isSameDay(Date()))  "HH:mm" else "dd.MM.yy"
    val dateFormat = SimpleDateFormat(pattern, Locale("ru"))
    return dateFormat.format(this)

}

fun Date.isSameDay(date:Date): Boolean {
    val day1 = this.time/ DAY
    val day2 = date.time/ DAY
    return day1 == day2

}

fun Date.add(value:Int, units: TimeUnits = TimeUnits.SECOND) : Date{
    var time  = this.time

    time +=when(units){
        TimeUnits.SECOND -> value * SECOND
        TimeUnits.MINUTE -> value * MINUTE
        TimeUnits.HOUR -> value * HOUR
        TimeUnits.DAY ->  value * DAY
    }
    this.time = time
    return this
}

fun Date.humanizeDiff(date: Date = Date()): String {

    val diff = abs(date.time - this.time)
    val seconds=(diff/1000).toInt()
    val minutes = (diff/60000).toInt()
    val hours = (diff/3600000).toInt()
    val days = (diff/86000400).toInt()
    val bolee = date.time > this.time
//   println("diff: $diff ,seconds: $seconds ,minutes: $minutes ,hour: $hours ,days: $days")
    return if(bolee) when {
        days > 360 -> "более года назад"
        hours > 26 -> "${TimeUnits.DAY.plural(days)} назад"
        hours in 23..26 -> "день назад"
        hours<=22&& minutes>75 ->"${TimeUnits.HOUR.plural(hours)} назад"
        minutes in 46..75 ->"час назад"
        minutes<=45&& seconds>75 ->"${TimeUnits.MINUTE.plural(minutes)} назад"
        seconds in 46..75 ->"минуту назад"
        seconds in 2..45 ->"несколько секунд назад"
        else ->"только что"
    }else when{
        days >360 -> "более чем через год"
        hours > 26 -> "через ${TimeUnits.DAY.plural(days)}"
        hours in 23..26 -> "через день"
        hours<=22&& minutes>75 ->"через ${TimeUnits.HOUR.plural(hours)}"
        minutes in 46..75 ->"через час"
        minutes<=45&& seconds>75 ->"через ${TimeUnits.MINUTE.plural(minutes)}"
        seconds in 46..75 ->"через минуту"
        seconds in 2..45 ->"через несколько секунд"
        else ->"только что"
    }

}


enum class TimeUnits(val first: String, val few: String, val many: String) {
    SECOND("секунду", "секунды", "секунд"),
    MINUTE("минуту", "минуты", "минут"),
    HOUR("час", "часа", "часов"),
    DAY("день", "дня", "дней");

    fun plural(value: Int): String {
        val svalue = abs(value)
        return when {
            svalue % 100 in 5..20 -> "$svalue $many"
            svalue % 10 == 1 -> "$svalue $first"
            svalue % 10 in 2..4->"$svalue $few"
            else -> "$svalue $many"
        }
    }
}