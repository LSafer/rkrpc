package net.lsafer.rkrpc.ktor

import kotlinx.rpc.krpc.KrpcConfigBuilder
import kotlinx.rpc.krpc.client.InitializedKrpcClient
import kotlinx.rpc.krpc.client.KrpcClient
import kotlinx.rpc.krpc.ktor.server.KrpcRoute
import kotlinx.rpc.krpc.rpcClientConfig
import net.lsafer.rkrpc.RkrpcInternalApi
import net.lsafer.rkrpc.internal.RkrpcService
import net.lsafer.rkrpc.internal.RkrpcServiceImpl
import net.lsafer.rkrpc.internal.RkrpcTransport

/**
 * Initiate a reverse kRPC connection utilizing this kRPC connection.
 *
 * Note: every kRPC server is only allowed exactly one reverse kRPC client.
 * Also Note: the client won't be available before server RPC registrations are finished.
 *
 * @param block configuration for the reverse kRPC client. (Can be different from original kRPC server)
 */
@OptIn(RkrpcInternalApi::class)
fun KrpcRoute.reverseClient(
    block: KrpcConfigBuilder.Client.() -> Unit,
): KrpcClient {
    // Transport Setup
    val transport = RkrpcTransport(this)

    registerService<RkrpcService> {
        RkrpcServiceImpl(transport)
    }

    // Client Construction
    val config = rpcClientConfig { block(this) }
    val client = object : InitializedKrpcClient(config, transport) {}

    return client
}
