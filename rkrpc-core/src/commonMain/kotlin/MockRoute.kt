package net.lsafer.rkrpc

import kotlinx.coroutines.CoroutineScope
import kotlinx.rpc.RpcServer
import kotlinx.rpc.annotations.Rpc
import kotlinx.rpc.krpc.KrpcConfigBuilder
import kotlin.reflect.KClass

class MockRoute(coroutineScope: CoroutineScope) : CoroutineScope by coroutineScope {
    @RkrpcInternalApi
    var commonConfig: KrpcConfigBuilder.() -> Unit = {}

    @RkrpcInternalApi
    var serverConfig: KrpcConfigBuilder.Server.() -> Unit = {}

    @RkrpcInternalApi
    var clientConfig: KrpcConfigBuilder.Client.() -> Unit = {}

    @RkrpcInternalApi
    val registrations = mutableListOf<(RpcServer) -> Unit>()

    @OptIn(RkrpcInternalApi::class)
    fun rpcConfig(block: KrpcConfigBuilder.() -> Unit) {
        this.commonConfig = block
    }

    @OptIn(RkrpcInternalApi::class)
    fun rpcServerConfig(block: KrpcConfigBuilder.Server.() -> Unit) {
        this.serverConfig = block
    }

    @OptIn(RkrpcInternalApi::class)
    fun rpcClientConfig(block: KrpcConfigBuilder.Client.() -> Unit) {
        this.clientConfig = block
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
