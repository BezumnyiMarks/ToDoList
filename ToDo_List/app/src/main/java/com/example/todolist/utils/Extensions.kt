package com.example.todolist.utils

import android.content.Context
import android.icu.util.Calendar
import android.widget.DatePicker

fun DatePicker.updateDateExt(calendar: Calendar){
    this.updateDate(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DATE)
    )
}

fun Calendar.getMonthExt(): String{
    return when(this.get(Calendar.MONTH)){
        0 -> "Января"
        1 -> "Фeвраля"
        2 -> "Марта"
        3 -> "Апрeля"
        4 -> "Мая"
        5 -> "Июня"
        6 -> "Июля"
        7 -> "Августа"
        8 -> "Сeнтября"
        9 -> "Октября"
        10 -> "Ноября"
        else ->"Декабря"
    }
}

fun Calendar.getHourExt(): String{
    return if(this.get(Calendar.HOUR_OF_DAY) < 10) "0${this.get(Calendar.HOUR_OF_DAY)}"
    else this.get(Calendar.HOUR_OF_DAY).toString()
}
fun Calendar.getMinuteExt(): String{
    return if(this.get(Calendar.MINUTE) < 10) "0${this.get(Calendar.MINUTE)}"
    else this.get(Calendar.MINUTE).toString()
}