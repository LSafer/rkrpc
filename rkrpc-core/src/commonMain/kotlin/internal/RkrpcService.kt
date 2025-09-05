package net.lsafer.rkrpc.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc
import net.lsafer.rkrpc.RkrpcInternalApi

@Rpc
@RkrpcInternalApi
interface RkrpcService {
    fun subscribe(): Flow<RkrpcTransportMessage>
    suspend fun send(message: RkrpcTransportMessage)
}
