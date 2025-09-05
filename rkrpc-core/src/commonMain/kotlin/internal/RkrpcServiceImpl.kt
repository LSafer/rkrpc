package net.lsafer.rkrpc.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import net.lsafer.rkrpc.RkrpcInternalApi

@RkrpcInternalApi
class RkrpcServiceImpl(val transport: RkrpcTransport) : RkrpcService {
    override fun subscribe(): Flow<RkrpcTransportMessage> {
        return transport.outChannel.consumeAsFlow()
    }

    override suspend fun send(message: RkrpcTransportMessage) {
        transport.inChannel.send(message)
    }
}
