package net.lsafer.rkrpc.ktor.client

import kotlinx.rpc.krpc.ktor.client.KtorRpcClient
import kotlinx.rpc.krpc.server.KrpcServer
import net.lsafer.rkrpc.core.RkrpcInternalApi
import net.lsafer.rkrpc.core.client.RkrpcRoute
import net.lsafer.rkrpc.core.client.rkrpc

/**
 * Initiate a reverse kRPC connection utilizing this kRPC connection.
 *
 * Note: every kRPC client is only allowed exactly one reverse kRPC server.
 *
 * @param block configuration for the reverse kRPC server. (Can be different from original kRPC client)
 */
@OptIn(RkrpcInternalApi::class)
suspend fun KtorRpcClient.rkrpc(
    block: RkrpcRoute.() -> Unit,
): KrpcServer {
    return rkrpc(webSocketSession.await(), block)
}
