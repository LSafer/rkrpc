package net.lsafer.rkrpc.core.test.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.rpc.krpc.KrpcTransport
import kotlinx.rpc.krpc.KrpcTransportMessage
import kotlinx.rpc.krpc.client.InitializedKrpcClient
import kotlinx.rpc.krpc.client.KrpcClient
import kotlinx.rpc.krpc.rpcClientConfig
import kotlinx.rpc.krpc.rpcServerConfig
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.rpc.krpc.server.KrpcServer

private class TestTransport(
    val inChannel: Channel<KrpcTransportMessage>,
    val outChannel: Channel<KrpcTransportMessage>,
    coroutineScope: CoroutineScope,
) : KrpcTransport, CoroutineScope by coroutineScope {
    override suspend fun send(message: KrpcTransportMessage) {
        outChannel.send(message)
    }

    override suspend fun receive(): KrpcTransportMessage {
        return inChannel.receive()
    }
}

suspend fun createServerClientTest(
    coroutineScope: CoroutineScope,
    serverBlock: suspend KrpcServer.() -> Unit,
    clientBlock: suspend (KrpcClient) -> Unit,
) {
    val inChannel = Channel<KrpcTransportMessage>()
    val outChannel = Channel<KrpcTransportMessage>()
    var server: KrpcServer? = null
    var client: KrpcClient? = null
    try {
        server = object : KrpcServer(
            rpcServerConfig { serialization { json() } },
            TestTransport(inChannel, outChannel, coroutineScope),
        ) {}
        serverBlock(server)
        client = object : InitializedKrpcClient(
            rpcClientConfig { serialization { json() } },
            TestTransport(outChannel, inChannel, coroutineScope),
        ) {}
        clientBlock(client)
    } finally {
        server?.close()
        client?.close()
    }
}
