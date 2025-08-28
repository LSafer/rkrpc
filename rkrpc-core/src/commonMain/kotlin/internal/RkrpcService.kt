package net.lsafer.rkrpc.core.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc
import net.lsafer.rkrpc.core.RkrpcInternalApi

@Rpc
@RkrpcInternalApi
interface RkrpcService {
    fun subscribe(): Flow<RkrpcTransportMessage>
    suspend fun send(message: RkrpcTransportMessage)
}
