package com.dataserver.plugins.newsapiorg

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import com.dataserver.plugins.newsapiorg.NewsAPIClient

fun Application.configureNewsAPIOrg(newsAPIClient: NewsAPIClient) {
    //val newsAPIClient = NewsAPIClient("1993f371257c43b6981695d55e11a47b")

    routing {
        // Get news JSON for given query

        get("/news/{query}") {
            val query = call.parameters["query"]
            if (query == null) {
                call.respondText("Query parameter required", status = HttpStatusCode.BadRequest)
                return@get
            }

            // Call the suspend function within a coroutine context
            val articlesJSON = withContext(Dispatchers.IO) {
                newsAPIClient.getArticlesJSON(company = query)
            }
            call.respondText(articlesJSON, ContentType.Application.Json)
        }

        // get parsed articles for a given query
        get("/news/parsed/{query}") {
            val query = call.parameters["query"]
            if (query == null) {
                call.respondText("Query parameter required", status = HttpStatusCode.BadRequest)
                return@get
            }

            // Call the suspend function within a coroutine context
            val articlesJSON = withContext(Dispatchers.IO) {
                newsAPIClient.getArticlesJSON(company = query)
            }

            // return list of articles as JSON
            val articles = withContext(Dispatchers.IO) {
                newsAPIClient.parseArticlesJSON(articlesJSON)
            }
            call.respondText(articles, ContentType.Text.Plain)
        }

    }
}