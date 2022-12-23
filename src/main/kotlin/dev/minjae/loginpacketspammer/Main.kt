package dev.minjae.loginpacketspammer

import com.nukkitx.protocol.bedrock.BedrockClient
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

val logger = LoggerFactory.getLogger("[LoginPacketSpammer - Main]")

fun main(args: Array<String>) {
    val serverAddress: String = getInput("Server Address", String::class)
    val serverPort: Int = getInput("Server Port", Int::class)
    val clientCount: Int = getInput("Client Count", Int::class)
    val spamInterval: Float = getInput("Spam Interval", Float::class)

    logger.info("Server Address: $serverAddress")
    logger.info("Server Port: $serverPort")
    logger.info("Client Count: $clientCount")
    logger.info("Spam Interval: $spamInterval")

    logger.info("Start spamming packets to $serverAddress:$serverPort")

    logger.info("Pinging server to make sure server is turned on...")

    val client = BedrockClient(InetSocketAddress(0))
    client.bind().join()
    try {
        val pong = client.ping(InetSocketAddress(serverAddress, serverPort)).get(10, TimeUnit.SECONDS)
        logger.info("Confirmed server is on. (Server MOTD: ${pong.motd})")
    } catch (e: Exception) {
        logger.error("Failed to ping server. Please make sure server is turned on.")
        e.printStackTrace()
        return
    }

    client.close()

    Spammer.startSpamming(serverAddress, serverPort, clientCount, spamInterval)

    Runtime.getRuntime().addShutdownHook(
        Thread {
            logger.info("Terminating all clients...")
            Spammer.interruptAll()
        }
    )

    try {
        while (true) {
            Thread.sleep(1000)
            Spammer.checkThreads(serverAddress, serverPort, clientCount, spamInterval)
        }
    } catch (e: InterruptedException) {
        logger.info("Terminating all clients...")
        Spammer.interruptAll()
    }
}

fun <T : Any> getInput(prompt: String, expectedType: KClass<T>): T {
    logger.info("$prompt: ")
    val input = readlnOrNull()
    if (input == null) {
        logger.error("Input was null!, $prompt")
        return getInput(prompt, expectedType)
    }
    return try {
        expectedType.javaObjectType.getDeclaredConstructor(String::class.java).newInstance(input)
    } catch (e: Exception) {
        logger.error("Input was not of type ${expectedType.simpleName}!, $prompt")
        getInput(prompt, expectedType)
    }
}
