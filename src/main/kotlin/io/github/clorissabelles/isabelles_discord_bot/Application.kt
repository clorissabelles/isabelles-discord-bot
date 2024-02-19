package io.github.clorissabelles.isabelles_discord_bot

suspend fun main(args: Array<String>) {
    Application.init(args)
}

object Application {
    suspend fun init(args: Array<String>) {
        println("Hello World!")
    }
}