package net.lsafer.rkrpc.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.job
import kotlinx.rpc.krpc.KrpcTransport
import kotlinx.rpc.krpc.KrpcTransportMessage
import net.lsafer.rkrpc.RkrpcInternalApi

@RkrpcInternalApi
class RkrpcTransport(coroutineScope: CoroutineScope) : KrpcTransport,
    CoroutineScope by coroutineScope {
    private val _outChannel = Channel<RkrpcTransportMessage>()
    private val _inChannel = Channel<RkrpcTransportMessage>()

    val outChannel: ReceiveChannel<RkrpcTransportMessage> get() = _outChannel
    val inChannel: SendChannel<RkrpcTransportMessage> get() = _inChannel

    init {
        coroutineScope.coroutineContext.job.invokeOnCompletion {
            _outChannel.close()
            _inChannel.close()
        }
    }

    override suspend fun send(message: KrpcTransportMessage) {
        _outChannel.send(message.encode())
    }

    override suspend fun receive(): KrpcTransportMessage {
        return _inChannel.receive().decode()
    }
}
