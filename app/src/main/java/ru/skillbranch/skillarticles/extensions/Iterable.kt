package ru.skillbranch.skillarticles.extensions

fun <T> List<T>.dropLastUntil(predicate: (T) -> Boolean): List<T> {
    if (this.isNotEmpty()) {
        val iterate = listIterator(this.size)
        while (iterate.hasPrevious()) {
            if (predicate(iterate.previous())) {
                return take(iterate.nextIndex())
            }
        }
    }
    return emptyList()
}