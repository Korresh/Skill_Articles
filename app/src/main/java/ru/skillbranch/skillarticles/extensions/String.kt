package ru.skillbranch.skillarticles.extensions

import java.util.*


fun String?.indexesOf(substr: String, ignoreCase: Boolean = true): List<Int>{
    this ?: return listOf()
    if (substr.isBlank()) return listOf()
    val searchResults = mutableListOf<Int>()
    val searchString = if (ignoreCase)this.toLowerCase(Locale.getDefault()) else this
    for (index in 0 until(searchString.length - substr.length)){
      if (searchString.substring(index,index+substr.length) == substr ){
           searchResults.add(index)
        }
    }
    return searchResults
}