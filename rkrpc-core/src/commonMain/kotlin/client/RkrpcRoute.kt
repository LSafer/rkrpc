package net.lsafer.rkrpc.core.client

import kotlinx.rpc.RpcServer
import kotlinx.rpc.annotations.Rpc
import kotlinx.rpc.krpc.KrpcConfigBuilder
import kotlin.reflect.KClass

class RkrpcRoute {
    @PublishedApi
    internal var configBuilder: KrpcConfigBuilder.Server.() -> Unit = {}
    @PublishedApi
    internal val registrations = mutableListOf<(RpcServer) -> Unit>()

    fun rpcConfig(configBuilder: KrpcConfigBuilder.Server.() -> Unit) {
        this.configBuilder = configBuilder
    }

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
