package ru.skillbranch.skillarticles.extensions

import kotlin.math.*

fun List<Pair<Int, Int>>.groupByBounds(bounds: List<Pair<Int, Int>>): List<List<Pair<Int, Int>>> = bounds.map{boundary ->
     this.filter { it.second >boundary.first && it.first < boundary.second }
          .map { when{it.first < boundary.first -> Pair(boundary.first, it.second)
               it.second > boundary.second -> Pair(it.first, boundary.second)
               else -> it
          }
      }
}

/*fun List<Pair<Int, Int>>.groupByBounds(bouds: List<Pair<Int, Int>>): List<List<Pair<Int, Int>>> {
     val result =  bouds.map { (start, end) -> this.filter {  it.first >=start && it.second<=end } }
     return result
}*/


/*fun List<Pair<Int,Int>>.groupByBounds(bounds: List<Pair<Int,Int>>): List<List<Pair<Int, Int>>> {
     val result  = mutableListOf<List<Pair<Int,Int>>>()
     bounds.forEach {(intervalLow, intervalHigh) ->
          result.add(this
               .filter{(foundLow, foundHigh) -> foundLow < intervalHigh && foundHigh > intervalLow}
               .map{(low, high) -> Pair(max(intervalLow, low), min(intervalHigh, high))})}
     return result
}*/
