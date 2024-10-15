package com.dataserver.plugins

import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement
import org.slf4j.LoggerFactory

@Serializable
data class ExposedUser(val userId: Int? = null, val username: String)

@Serializable
data class Ticker(val id: Int? = null, val userId: Int, val ticker: String)

class UserService(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_USERS =
            "CREATE TABLE IF NOT EXISTS USERS (USERID SERIAL PRIMARY KEY, USERNAME VARCHAR(255));"
        private const val CREATE_TABLE_TICKERS =
            "CREATE TABLE IF NOT EXISTS TICKERS (ID SERIAL PRIMARY KEY, USER_ID INT REFERENCES USERS(USERID), TICKER VARCHAR(10));"
        private const val CREATE_USER = "INSERT INTO USERS (USERNAME) VALUES (?)"
        private const val READ_USER = "SELECT userid, username FROM users WHERE userid = ?"
        private const val READ_ALL_USERS = "SELECT userid, username FROM users"
        private const val UPDATE_USER = "UPDATE users SET username = ? WHERE userid = ?"
        private const val DELETE_USER = "DELETE FROM users WHERE userid = ?"
        private const val ADD_TICKER = "INSERT INTO tickers (user_id, ticker) VALUES (?, ?)"
        private const val GET_TICKERS = "SELECT id, user_id, ticker FROM tickers WHERE user_id = ?"
        private const val REMOVE_TICKER = "DELETE FROM tickers WHERE id = ?"
    }
    private val logger = LoggerFactory.getLogger(ArticleService::class.java)

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_USERS)
        statement.executeUpdate(CREATE_TABLE_TICKERS)
    }

    suspend fun createUser(user: ExposedUser): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(CREATE_USER, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, user.username)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted user")
        }
    }

    suspend fun readAllUsers(): List<ExposedUser> = withContext(Dispatchers.IO) {
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery(READ_ALL_USERS)
        val users = mutableListOf<ExposedUser>()

        while (resultSet.next()) {
            users.add(
                ExposedUser(
                    userId = resultSet.getInt("userid"),
                    username = resultSet.getString("username")
                )
            )
        }

        return@withContext users
    }

    suspend fun readUser(userId: Int): ExposedUser = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(READ_USER)
        statement.setInt(1, userId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext ExposedUser(resultSet.getInt("userid"), resultSet.getString("username"))
        } else {
            throw Exception("User not found")
        }
    }

    suspend fun updateUser(userId: Int, user: ExposedUser) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_USER)
        statement.setString(1, user.username)
        statement.setInt(2, userId)
        statement.executeUpdate()
    }

    suspend fun deleteUser(userId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_USER)
        statement.setInt(1, userId)
        statement.executeUpdate()
    }

    suspend fun addTicker(userId: Int, ticker: String): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(ADD_TICKER, Statement.RETURN_GENERATED_KEYS)
        statement.setInt(1, userId)
        statement.setString(2, ticker)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted ticker")
        }
    }

    suspend fun getTickers(userId: Int): List<Ticker> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(GET_TICKERS)
        statement.setInt(1, userId)
        val resultSet = statement.executeQuery()
        val tickers = mutableListOf<Ticker>()

        while (resultSet.next()) {
            tickers.add(
                Ticker(
                    id = resultSet.getInt("id"),
                    userId = resultSet.getInt("user_id"),
                    ticker = resultSet.getString("ticker")
                )
            )
        }

        return@withContext tickers
    }

    suspend fun removeTicker(id: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(REMOVE_TICKER)
        statement.setInt(1, id)
        statement.executeUpdate()
    }

}

