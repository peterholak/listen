package net.holak.listen.history

interface History {
    fun wasListened(id: String): Boolean
    fun setAsListened(id: String)
    fun resetAll()
    fun listListened(): List<String>
}
