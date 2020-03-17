package ru.skillbranch.skillarticles.extensions

fun List<Pair<Int, Int>>.groupByBounds(bouds: List<Pair<Int, Int>>): List<List<Pair<Int, Int>>> {
     val result =  bouds.map { (start, end) -> this.filter {  it.first >=start && it.second<=end } }
     return result
}
