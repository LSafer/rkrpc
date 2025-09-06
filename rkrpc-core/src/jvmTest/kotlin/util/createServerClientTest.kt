package net.lsafer.rkrpc.test.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.job
import kotlinx.rpc.krpc.KrpcTransport
import kotlinx.rpc.krpc.KrpcTransportMessage
import kotlinx.rpc.krpc.client.InitializedKrpcClient
import kotlinx.rpc.krpc.client.KrpcClient
import kotlinx.rpc.krpc.rpcClientConfig
import kotlinx.rpc.krpc.rpcServerConfig
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.rpc.krpc.server.KrpcServer
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

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
    serverBlock: suspend KrpcServer.(CoroutineScope) -> Unit,
    clientBlock: suspend (KrpcClient, CoroutineScope) -> Unit,
) {
    val inChannel = Channel<KrpcTransportMessage>()
    val outChannel = Channel<KrpcTransportMessage>()
    var server: KrpcServer? = null
    var client: KrpcClient? = null
    val serverJob = SupervisorJob()
    val clientJob = SupervisorJob()
    val serverScope = CoroutineScope(serverJob)
    val clientScope = CoroutineScope(clientJob)
    try {
        server = object : KrpcServer(
            rpcServerConfig { serialization { json() } },
            TestTransport(inChannel, outChannel, serverScope),
        ) {}
        serverBlock(server, serverScope)
        client = object : InitializedKrpcClient(
            rpcClientConfig { serialization { json() } },
            TestTransport(outChannel, inChannel, clientScope),
        ) {}
        clientBlock(client, clientScope)
        serverJob.complete()
        clientJob.complete()
    } catch (e: Throwable) {
        serverJob.cancel()
        clientJob.cancel()
        throw e
    } finally {
        server?.close()
        client?.close()
        serverJob.join()
        clientJob.join()
    }
}

fun CoroutineScope.supervisorChildScope(extra: CoroutineContext = EmptyCoroutineContext): CoroutineScope =
    CoroutineScope(coroutineContext + SupervisorJob(coroutineContext.job) + extra)

fun CoroutineScope.childScope(extra: CoroutineContext = EmptyCoroutineContext): CoroutineScope =
    CoroutineScope(coroutineContext + Job(coroutineContext.job) + extra)
