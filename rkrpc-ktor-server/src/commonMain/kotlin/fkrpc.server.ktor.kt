package net.lsafer.rkrpc.ktor

import kotlinx.coroutines.CoroutineScope
import kotlinx.rpc.krpc.ktor.server.KrpcRoute
import kotlinx.rpc.krpc.rpcServerConfig
import net.lsafer.rkrpc.RkrpcInternalApi
import net.lsafer.rkrpc.RkrpcRoute
import net.lsafer.rkrpc.internal.FkrpcService
import net.lsafer.rkrpc.internal.FkrpcServiceImpl
import net.lsafer.rkrpc.newSubClient

/**
 * Register services in [block] to be available in the
 * client returned by [newSubClient].
 */
@OptIn(RkrpcInternalApi::class)
fun KrpcRoute.configureSubClient(
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
