package com.dataserver

import com.dataserver.plugins.*
import com.dataserver.plugins.newsapiorg.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureDatabases()
    configureRouting()
    configureNewsAPIOrg(NewsAPIClient("1993f371257c43b6981695d55e11a47b"))
}
