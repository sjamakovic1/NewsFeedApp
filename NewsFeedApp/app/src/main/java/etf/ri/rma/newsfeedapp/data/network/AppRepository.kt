package etf.ri.rma.newsfeedapp.data.network

import android.content.Context
import etf.ri.rma.newsfeedapp.data.NewsDatabase

object AppRepository {
    lateinit var newsDAO: NewsDAO
        private set

    val imagaDAO = ImagaDAO()

    fun init(context: Context) {
        val savedNewsDao = NewsDatabase
            .getDatabase(context)
            .savedNewsDAO()
        newsDAO = NewsDAO(savedNewsDao)
    }
}
