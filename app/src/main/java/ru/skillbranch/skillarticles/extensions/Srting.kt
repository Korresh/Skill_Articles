package ru.skillbranch.skillarticles.extensions


fun String?.indexesOf(substr: String, ignoreCase: Boolean = true): List<Int>{
    this ?: return listOf()
    if (substr.isBlank()) return listOf()
    var searchResults = mutableListOf<Int>()
    var searchString = if (ignoreCase)this?.toLowerCase() else this
    for (index in 0 until(searchString.length - substr.length)){
      if (searchString.substring(index,index+substr.length) == substr ){
           searchResults.add(index)
        }
    }
    return searchResults
}