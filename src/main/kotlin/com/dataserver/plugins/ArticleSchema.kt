package com.dataserver.plugins

import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement
import org.slf4j.LoggerFactory

@Serializable
data class Article(val ticker: String, val publisher: String, val title: String, val url: String, val timestamp: String)
data class ArticleID(val id: Int, val ticker: String, val publisher: String, val title: String, val url: String, val timestamp: String)

class ArticleService(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_ARTICLES =
            "CREATE TABLE IF NOT EXISTS ARTICLES (ID SERIAL PRIMARY KEY, TICKER VARCHAR(10), PUBLISHER VARCHAR(255), TITLE VARCHAR(255), URL VARCHAR(255), TIMESTAMP VARCHAR(255));"
        private const val SELECT_ARTICLE_BY_ID = "SELECT ticker, publisher, title, url, timestamp FROM articles WHERE id = ?"
        private const val SELECT_ARTICLES_BY_TICKER = "SELECT ticker, publisher, title, url, timestamp FROM articles WHERE ticker = ?"
        private const val SELECT_ARTICLES_BY_TICKER_ID = "SELECT id, ticker, publisher, title, url, timestamp FROM articles WHERE ticker = ?"
        private const val INSERT_ARTICLE = "INSERT INTO articles (ticker, publisher, title, url, timestamp) VALUES (?, ?, ?, ?, ?)"
        private const val UPDATE_ARTICLE = "UPDATE articles SET ticker = ?, publisher = ?, title = ?, url = ?, timestamp = ? WHERE id = ?"
        private const val DELETE_ARTICLE = "DELETE FROM articles WHERE id = ?"

    }
    private val logger = LoggerFactory.getLogger(ArticleService::class.java)

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_ARTICLES)
    }

    private var newArticleId = 0

    // Create new article
    suspend fun create(article: Article): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_ARTICLE, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, article.ticker)
        statement.setString(2, article.publisher)
        statement.setString(3, article.title)
        statement.setString(4, article.url)
        statement.setString(5, article.timestamp)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted article")
        }
    }

    // Read an article
    suspend fun read(id: Int): Article = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_ARTICLE_BY_ID)
        statement.setInt(1, id)
        val resultSet = statement.executeQuery()
        logger.info("Statement = $resultSet")

        if (resultSet.next()) {
            val ticker = resultSet.getString("ticker")
            val publisher = resultSet.getString("publisher")
            val title = resultSet.getString("title")
            val url = resultSet.getString("url")
            val timestamp = resultSet.getString("timestamp")
            return@withContext Article(ticker, publisher, title, url, timestamp)
        } else {
            throw Exception("Record not found")
        }
    }

    // Read articles by ticker
    suspend fun readByTicker(ticker: String): List<Article> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_ARTICLES_BY_TICKER)
        statement.setString(1, ticker.toUpperCase())
        val resultSet = statement.executeQuery()
        val articles = mutableListOf<Article>()

        while (resultSet.next()) {
            articles.add(
                Article(
                    ticker = resultSet.getString("ticker"),
                    publisher = resultSet.getString("publisher"),
                    title = resultSet.getString("title"),
                    url = resultSet.getString("url"),
                    timestamp = resultSet.getString("timestamp")
                )
            )
        }
        return@withContext articles
    }

    // Read articles by ticker and include ID
    suspend fun readByTickerID(ticker: String): List<ArticleID> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_ARTICLES_BY_TICKER_ID)
        statement.setString(1, ticker)
        
        val resultSet = statement.executeQuery()
        val articlesID = mutableListOf<ArticleID>()
        logger.info("Statement = $resultSet")
        while (resultSet.next()) {
            articlesID.add(
                ArticleID(
                    id = resultSet.getInt("id"),
                    ticker = resultSet.getString("ticker"),
                    publisher = resultSet.getString("publisher"),
                    title = resultSet.getString("title"),
                    url = resultSet.getString("url"),
                    timestamp = resultSet.getString("timestamp")
                )
            )
        }
        return@withContext articlesID
    }


    // Update an article
    suspend fun update(id: Int, article: Article) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_ARTICLE)
        statement.setString(1, article.ticker)
        statement.setString(2, article.publisher)
        statement.setString(3, article.title)
        statement.setString(4, article.url)
        statement.setString(5, article.timestamp)
        statement.setInt(6, id)
        statement.executeUpdate()
    }

    // Delete an article
    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_ARTICLE)
        statement.setInt(1, id)
        statement.executeUpdate()
    }
}

