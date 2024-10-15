package com.dataserver.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import com.dataserver.plugins.Article
import com.dataserver.plugins.ArticleService
import org.slf4j.LoggerFactory
import io.github.cdimascio.dotenv.Dotenv

object DatabaseConfig {
    private val dotenv: Dotenv = Dotenv.load()

    private val host: String = System.getenv("POSTGRES_HOST") ?: "news-database"
    private val port: String = System.getenv("POSTGRES_PORT") ?: "5432"
    private val user: String = System.getenv("POSTGRES_USER") ?: "postgres"
    private val password: String = System.getenv("POSTGRES_PASSWORD") ?: "postgres"
    private val dbName: String = System.getenv("POSTGRES_DB") ?: "news_development"

    val jdbcUrl: String = "jdbc:postgresql://$host:$port/$dbName"

    fun getConnection(): Connection {
        return DriverManager.getConnection(jdbcUrl, user, password)
    }
}

fun Application.configureDatabases() {
    val dbConnection: Connection = connectToPostgres(embedded = false)
    val newsService = ArticleService(dbConnection)
    val logger = LoggerFactory.getLogger(Application::class.java)
    
    routing {
    
        // Create articles
        post("/articles") {
            val article = call.receive<Article>()
            val id = newsService.create(article)
            call.respond(HttpStatusCode.Created, id)
        }
    
        // Read article by ID
        get("/articles/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val article = newsService.read(id)
            if (article != null) {
                call.respond(HttpStatusCode.OK, article)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Read articles of a ticker
        get("/articles/ticker/{ticker}") {
            val ticker = call.parameters["ticker"] ?: throw IllegalArgumentException("Invalid Ticker")
            try {
                val article = newsService.readByTicker(ticker)
                call.respond(HttpStatusCode.OK, article)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Read articles of a ticker and return result with ID
        get("/articles/tickerID/{ticker}") {
            val ticker = call.parameters["ticker"] ?: throw IllegalArgumentException("Invalid Ticker")
            try {
                val article = newsService.readByTickerID(ticker)
                call.respond(HttpStatusCode.OK, article)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    
        // Update article
        put("/articles/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val user = call.receive<Article>()
            newsService.update(id, user)
            call.respond(HttpStatusCode.OK)
        }
    
        // Delete article
        delete("/articles/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            newsService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
    val database = Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        user = "root",
        driver = "org.h2.Driver",
        password = "",
    )
    val userService = UserService(dbConnection)
    routing {
        // Create user
        post("/users") {
            val user = call.receive<ExposedUser>()
            val id = userService.createUser(user)
            call.respond(HttpStatusCode.Created, id)
        }

        // Read all users
        get("/users") {
            val users = userService.readAllUsers()
            call.respond(HttpStatusCode.OK, users)
        }
        
        // Read user
        get("/users/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val user = userService.readUser(id)
            if (user != null) {
                call.respond(HttpStatusCode.OK, user)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        
        // Update user
        put("/users/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val user = call.receive<ExposedUser>()
            userService.updateUser(id, user)
            call.respond(HttpStatusCode.OK)
        }
        
        // Delete user
        delete("/users/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            userService.deleteUser(id)
            call.respond(HttpStatusCode.OK)
        }

        // Add ticker
        post("/users/{id}/tickers") {
            val userId = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val ticker = call.receive<String>()
            val tickerId = userService.addTicker(userId, ticker)
            call.respond(HttpStatusCode.Created, tickerId)
        }

        // Get tickers
        get("/users/{id}/tickers") {
            val userId = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val tickers = userService.getTickers(userId)
            call.respond(HttpStatusCode.OK, tickers)
        }

        // Remove ticker
        delete("/tickers/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            userService.removeTicker(id)
            call.respond(HttpStatusCode.OK)
        }
    }

}

/**
 * Makes a connection to a Postgres database.
 *
 * In order to connect to your running Postgres process,
 * please specify the following parameters in your configuration file:
 * - postgres.url -- Url of your running database process.
 * - postgres.user -- Username for database connection
 * - postgres.password -- Password for database connection
 *
 * If you don't have a database process running yet, you may need to [download]((https://www.postgresql.org/download/))
 * and install Postgres and follow the instructions [here](https://postgresapp.com/).
 * Then, you would be able to edit your url,  which is usually "jdbc:postgresql://host:port/database", as well as
 * user and password values.
 *
 *
 * @param embedded -- if [true] defaults to an embedded database for tests that runs locally in the same process.
 * In this case you don't have to provide any parameters in configuration file, and you don't have to run a process.
 *
 * @return [Connection] that represent connection to the database. Please, don't forget to close this connection when
 * your application shuts down by calling [Connection.close]
 * */
fun Application.connectToPostgres(embedded: Boolean): Connection {
    Class.forName("org.postgresql.Driver")
    if (embedded) {
        return DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "root", "")
    } else {
        return DatabaseConfig.getConnection()
    }
}
