package dev.minjae.loginpacketspammer

import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

val logger = LoggerFactory.getLogger("[LoginPacketSpammer - Main]")

fun main(args: Array<String>) {
    val serverAddress: String = getInput("Server Address", String::class)
    val serverPort: Int = getInput("Server Port", Int::class)
    val clientCount: Int = getInput("Client Count", Int::class)

    logger.info("Server Address: $serverAddress")
    logger.info("Server Port: $serverPort")

    logger.info("Start spamming packets to $serverAddress:$serverPort")

    Spammer.startSpamming(serverAddress, serverPort, clientCount)

    Runtime.getRuntime().addShutdownHook(
        Thread {
            logger.info("Terminating all clients...")
            Spammer.interruptAll()
        }
    )

    try {
        while (true) {
            Thread.sleep(1000)
            Spammer.checkThreads(serverAddress, serverPort, clientCount)
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
