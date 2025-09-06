package net.lsafer.rkrpc.ktor

import kotlinx.rpc.krpc.ktor.server.KrpcRoute
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
    block: RkrpcRoute.() -> Unit,
) {
    registerService<FkrpcService> {
        FkrpcServiceImpl(
            coroutineScope = this,
            configBlock = block,
        )
    }
}
