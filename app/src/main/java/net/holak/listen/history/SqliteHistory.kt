package net.holak.listen.history

import android.content.Context
import org.jetbrains.anko.db.*

val HistoryTable = "history"
val IdColumn = "id"

class SqliteHistory(val context: Context) : History {
    override fun wasListened(id: String): Boolean {
        return context.database.use {
            select(HistoryTable).where("$IdColumn = {id}", "id" to id).exec {
                count > 0
            }
        }
    }

    override fun setAsListened(id: String) {
        context.database.use { insert(HistoryTable, IdColumn to id) }
    }

    override fun resetAll() {
        context.database.use { delete(HistoryTable) }
    }

    override fun listListened(): List<String> {
        return context.database.use { select(HistoryTable).exec { parseList(StringParser) } }
    }
}
