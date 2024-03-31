import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import net.dv8tion.jda.api.*
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent

val BOT_TOKEN = ""

class DiscordBot(private val channelId: String) : ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val author = event.author
        val message = event.message
        val channel = event.channel
        if (channel.id == channelId && author.name != "KtorBot") {
            println("Wiadomość od ${author.name}: ${message.contentRaw}")
        }
    }
}

suspend fun main(args: Array<String>) {

    if (args.size < 1) {
        println("This app requires 1 parameter  - <channel_id>")
        return
    }

    val channelId = args[0]

    JDABuilder.createDefault(BOT_TOKEN)
        .addEventListeners(DiscordBot(channelId))
        .enableIntents(GatewayIntent.MESSAGE_CONTENT)
        .build()

    val apiUrl = "http://localhost:9000"
    val discordURL = "https://discord.com/api/v9/channels/$channelId/messages"

    while (true) {
        val userInput = readLine()

        if (userInput == "API") {
            handleAPIRequests(apiUrl)
        } else {
            sendMessageToDiscord(userInput, discordURL)
        }
    }
}

suspend fun handleAPIRequests(apiUrl: String) {
    val client = HttpClient(CIO)

    try {

        val res: HttpResponse = client.get("$apiUrl/brands")
        println("getAllBrands(Categories)")
        println(res.bodyAsText())

        val clientBrands = HttpClient(CIO)
        val resClothes: HttpResponse = clientBrands.get("$apiUrl/clothes/brands/Nike")
        println("getAllClothesByCategory(brand Nike)")
        println(resClothes.bodyAsText())
        println()

    } catch (e: Exception) {
        println("Error: ${e.message}")
    } finally {
        client.close()
    }
}

suspend fun sendMessageToDiscord(message: String?, discordURL: String) {

    val client = HttpClient(CIO)

    try {
        client.post(discordURL) {
            headers {
                append(HttpHeaders.Authorization, "Bot $BOT_TOKEN")
                append(HttpHeaders.ContentType, ContentType.Application.Json)
            }
            setBody(
                "{\n" +
                        "    \"content\": \"$message\"\n" +
                        "}"
            )
        }
    } catch (e: Exception) {
        println("Error: ${e.message}")
    } finally {
        client.close()
    }
}