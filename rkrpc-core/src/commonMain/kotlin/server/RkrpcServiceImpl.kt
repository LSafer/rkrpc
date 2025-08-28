package net.lsafer.rkrpc.core.server

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import net.lsafer.rkrpc.core.RkrpcInternalApi
import net.lsafer.rkrpc.core.internal.RkrpcService
import net.lsafer.rkrpc.core.internal.RkrpcTransport
import net.lsafer.rkrpc.core.internal.RkrpcTransportMessage

@RkrpcInternalApi
class RkrpcServiceImpl(val transport: RkrpcTransport) : RkrpcService {
    override fun subscribe(): Flow<RkrpcTransportMessage> {
        return transport.outChannel.consumeAsFlow()
    }

    override suspend fun send(message: RkrpcTransportMessage) {
        transport.inChannel.send(message)
    }
}
