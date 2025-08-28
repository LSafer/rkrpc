package net.lsafer.rkrpc.core.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.rpc.krpc.client.KrpcClient
import kotlinx.rpc.krpc.rpcServerConfig
import kotlinx.rpc.krpc.server.KrpcServer
import kotlinx.rpc.withService
import net.lsafer.rkrpc.core.RkrpcInternalApi
import net.lsafer.rkrpc.core.internal.RkrpcService
import net.lsafer.rkrpc.core.internal.RkrpcTransport

/**
 * Initiate a reverse kRPC connection utilizing this kRPC connection.
 *
 * Note: every kRPC client is only allowed exactly one reverse kRPC server.
 *
 * @param coroutineScope the scope of the rkrpc transport. (Pass the original krpc client scope)
 * @param block configuration for the reverse kRPC server. (Can be different from original kRPC client)
 */
@OptIn(RkrpcInternalApi::class)
inline fun KrpcClient.rkrpc(
    coroutineScope: CoroutineScope,
    block: RkrpcRoute.() -> Unit,
): KrpcServer {
    // Transport Setup
    val transport = RkrpcTransport(coroutineScope)
    val rkrpcService = withService<RkrpcService>()

    coroutineScope.launch {
        transport.outChannel.consumeEach {
            rkrpcService.send(it)
        }
    }

    rkrpcService.subscribe()
        .onEach { transport.inChannel.send(it) }
        .launchIn(coroutineScope)

    // Server Construction
    val route = RkrpcRoute()
    route.apply { block() }

    val config = rpcServerConfig { route.configBuilder(this) }
    val server = object : KrpcServer(config, transport) {}

    route.registrations.forEach {
        it(server)
    }

    return server
}
