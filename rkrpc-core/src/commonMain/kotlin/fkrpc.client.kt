package net.lsafer.rkrpc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.rpc.krpc.KrpcConfigBuilder
import kotlinx.rpc.krpc.client.InitializedKrpcClient
import kotlinx.rpc.krpc.client.KrpcClient
import kotlinx.rpc.krpc.rpcClientConfig
import kotlinx.rpc.withService
import net.lsafer.rkrpc.internal.FkrpcService
import net.lsafer.rkrpc.internal.RkrpcTransport
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Return a new client that can obtain services registered via [configureSubClient].
 *
 * Note: multiple sub clients are allowed.
 *
 * @param coroutineScope the scope of the sub client transport. (Pass the original kRPC client scope)
 * @param block configuration for the sub client. (Can be different from original kRPC client)
 */
@OptIn(ExperimentalUuidApi::class, RkrpcInternalApi::class)
fun KrpcClient.newSubClient(
    coroutineScope: CoroutineScope,
    block: KrpcConfigBuilder.Client.() -> Unit,
): KrpcClient {
    val id = Uuid.random().toString()
    val transport = RkrpcTransport(coroutineScope)
    val fkrpcService = withService<FkrpcService>()

    fkrpcService.subscribe(id)
        .onEach { transport.inChannel.send(it) }
        .launchIn(coroutineScope)

    coroutineScope.launch {
        transport.outChannel.consumeEach {
            fkrpcService.send(id, it)
        }
    }

    val config = rpcClientConfig { block() }
    return object : InitializedKrpcClient(config, transport) {}
}
