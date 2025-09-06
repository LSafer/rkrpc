package net.lsafer.rkrpc.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
interface FkrpcService {
    fun subscribe(key: String): Flow<RkrpcTransportMessage>
    suspend fun send(key: String, message: RkrpcTransportMessage)
}
