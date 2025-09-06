package net.lsafer.rkrpc.ktor

import kotlinx.rpc.krpc.ktor.client.KtorRpcClient
import kotlinx.rpc.krpc.server.KrpcServer
import net.lsafer.rkrpc.RkrpcInternalApi
import net.lsafer.rkrpc.RkrpcRoute
import net.lsafer.rkrpc.reverseServer

/**
 * Initiate a reverse kRPC connection utilizing this kRPC connection.
 *
 * Note: every kRPC client is only allowed exactly one reverse kRPC server.
 *
 * @param block configuration for the reverse kRPC server. (Can be different from original kRPC client)
 */
@OptIn(RkrpcInternalApi::class)
@Deprecated("This function is unstable and may block indefinitely.")
suspend fun KtorRpcClient.reverseServer(
    block: RkrpcRoute.() -> Unit,
): KrpcServer {
    return reverseServer(webSocketSession.await(), block)
}
