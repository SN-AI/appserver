package com.dataserver.plugins.newsapiorg

import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.builtins.ListSerializer
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.time.Instant

@Serializable
data class NewsAPIArticle(
    @SerialName("publisher") val publisher: String, 
    @SerialName("title") val title: String, 
    @SerialName("description") val description: String, 
    @SerialName("url") val url: String, 
    @SerialName("publishedAt") val publishedAt: String
)

// News API client
class NewsAPIClient(private val apiKey: String) {
    private val client = HttpClient()
    
    // Get articles from the News API as JSON string
    suspend fun getArticlesJSON(company: String): String = withContext(Dispatchers.IO) {
        //retrieve articles from the News API
        val response: HttpResponse = client.get("https://newsapi.org/v2/everything") {
            parameter("q", company)
            parameter("apiKey", apiKey)
            parameter("pageSize", 5)
        }

        val articlesJSON = response.bodyAsText()

        // log the response
        println(articlesJSON)
        //return the articles as JSON
        return@withContext (articlesJSON)
        
    }

    // Convert JSON string of many articles to list of NewsAPIArticle objects
    suspend fun parseArticlesJSON(json: String): String {
        //extract each article entry from the JSON string including the article publisher, title, description, url, and publishedAt timestamp

        //parse the JSON string
        val jsonElement = Json.parseToJsonElement(json)
        //get the articles from the JSON
        val articlesJson = jsonElement.jsonObject["articles"]!!.jsonArray
        //create list of articles
        val articlesList = articlesJson.map { articleJson ->
            //extract the article publisher, title, description, url, and publishedAt timestamp
            NewsAPIArticle(
                publisher = articleJson.jsonObject["source"]!!.jsonObject["name"]!!.jsonPrimitive.content,
                title = articleJson.jsonObject["title"]!!.jsonPrimitive.content,
                description = articleJson.jsonObject["description"]!!.jsonPrimitive.content,
                url = articleJson.jsonObject["url"]!!.jsonPrimitive.content,
                publishedAt = articleJson.jsonObject["publishedAt"]!!.jsonPrimitive.content
            )
        }

        // log the article list
        
        //create a JSON string from a list of NewsAPIArticle objects
        val articlesJSON = Json.encodeToString(ListSerializer(NewsAPIArticle.serializer()), articlesList)

        //return the JSON String of proper article details
        return articlesJSON
    }


}
