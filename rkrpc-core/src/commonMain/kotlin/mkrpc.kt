package net.lsafer.rkrpc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.job
import kotlinx.rpc.internal.utils.InternalRpcApi
import kotlinx.rpc.krpc.client.InitializedKrpcClient
import kotlinx.rpc.krpc.client.KrpcClient
import kotlinx.rpc.krpc.rpcClientConfig
import kotlinx.rpc.krpc.rpcServerConfig
import kotlinx.rpc.krpc.server.KrpcServer
import net.lsafer.rkrpc.internal.RkrpcTransport

@OptIn(RkrpcInternalApi::class)
fun krpc(
    coroutineScope: CoroutineScope,
    block: MockRoute.() -> Unit,
): KrpcClient {
    val route = MockRoute(coroutineScope)
    route.apply { block() }

    // Transport Setup
    val serverTransport = RkrpcTransport(coroutineScope)
    val clientTransport = serverTransport.flipped()

    // Server Construction
    val serverConfig = rpcServerConfig {
        route.commonConfig(this)
        route.serverConfig(this)
    }
    val server = object : KrpcServer(serverConfig, serverTransport) {}

    @OptIn(InternalRpcApi::class)
    server.internalScope.coroutineContext.job.invokeOnCompletion {
        println("Server Closed")
    }

    route.registrations.forEach {
        it(server)
    }

    // Client Construction

    val clientConfig = rpcClientConfig {
        route.commonConfig(this)
        route.clientConfig(this)
    }

    return object : InitializedKrpcClient(clientConfig, clientTransport) {}
}
