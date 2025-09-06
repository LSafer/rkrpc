package net.lsafer.rkrpc

import kotlinx.coroutines.CoroutineScope
import kotlinx.rpc.RpcServer
import kotlinx.rpc.annotations.Rpc
import kotlinx.rpc.krpc.KrpcConfigBuilder
import kotlin.reflect.KClass

class RkrpcRoute(coroutineScope: CoroutineScope) : CoroutineScope by coroutineScope {
    @RkrpcInternalApi
    var configBuilder: KrpcConfigBuilder.Server.() -> Unit = {}
    @RkrpcInternalApi
    val registrations = mutableListOf<(RpcServer) -> Unit>()

    @OptIn(RkrpcInternalApi::class)
    fun rpcConfig(configBuilder: KrpcConfigBuilder.Server.() -> Unit) {
        this.configBuilder = configBuilder
    }

    @OptIn(RkrpcInternalApi::class)
    fun <@Rpc Service : Any> registerService(
        serviceKClass: KClass<Service>,
        serviceFactory: () -> Service,
    ) {
        registrations.add { server ->
            server.registerService(serviceKClass, serviceFactory)
        }
    }

    inline fun <@Rpc reified Service : Any> registerService(
        noinline serviceFactory: () -> Service,
    ) {
        registerService(Service::class, serviceFactory)
    }
}
