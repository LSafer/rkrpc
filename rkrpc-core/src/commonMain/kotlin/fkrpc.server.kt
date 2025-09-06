package net.lsafer.rkrpc

import kotlinx.coroutines.CoroutineScope
import kotlinx.rpc.RpcServer
import kotlinx.rpc.krpc.rpcServerConfig
import kotlinx.rpc.registerService
import net.lsafer.rkrpc.internal.FkrpcService
import net.lsafer.rkrpc.internal.FkrpcServiceImpl

/**
 * Register services in [block] to be available for [newSubClient].
 *
 * Note: every kRPC server is allowed to invoke this function only once.
 *
 * @param coroutineScope the scope of the sub client host transport. (Pass the original krpc server scope)
 * @param block configuration for the sub client host server. (Can be different from original kRPC server)
 */
@OptIn(RkrpcInternalApi::class)
fun RpcServer.configureSubClient(
    coroutineScope: CoroutineScope,
    block: RkrpcRoute.() -> Unit,
) {
    registerService<FkrpcService> {
        val route = RkrpcRoute()
        route.apply { block() }

        val config = rpcServerConfig { route.configBuilder(this) }

        FkrpcServiceImpl(
            coroutineScope = coroutineScope,
            config = config,
            registrations = route.registrations,
        )
    }
}
