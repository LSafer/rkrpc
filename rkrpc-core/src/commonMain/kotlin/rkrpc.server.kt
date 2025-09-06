package net.lsafer.rkrpc

import kotlinx.coroutines.CoroutineScope
import kotlinx.rpc.krpc.KrpcConfigBuilder
import kotlinx.rpc.krpc.client.InitializedKrpcClient
import kotlinx.rpc.krpc.client.KrpcClient
import kotlinx.rpc.krpc.rpcClientConfig
import kotlinx.rpc.krpc.server.KrpcServer
import kotlinx.rpc.registerService
import net.lsafer.rkrpc.internal.RkrpcService
import net.lsafer.rkrpc.internal.RkrpcServiceImpl
import net.lsafer.rkrpc.internal.RkrpcTransport

/**
 * Initiate a reverse kRPC connection utilizing this kRPC connection.
 *
 * Note: every kRPC server is only allowed exactly one reverse kRPC client.
 * Also Note: the client won't be available before server RPC registrations are finished.
 *
 * @param coroutineScope the scope of the rkrpc transport. (Pass the original krpc server scope)
 * @param block configuration for the reverse kRPC client. (Can be different from original kRPC server)
 */
@OptIn(RkrpcInternalApi::class)
fun KrpcServer.reverseClient(
    coroutineScope: CoroutineScope,
    block: KrpcConfigBuilder.Client.() -> Unit,
): KrpcClient {
    // Transport Setup
    val transport = RkrpcTransport(coroutineScope)

    registerService<RkrpcService> {
        RkrpcServiceImpl(transport)
    }

    // Client Construction
    val config = rpcClientConfig { block(this) }
    val client = object : InitializedKrpcClient(config, transport) {}

    return client
}
