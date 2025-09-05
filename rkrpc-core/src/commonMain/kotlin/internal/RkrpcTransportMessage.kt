package net.lsafer.rkrpc.internal

import kotlinx.rpc.krpc.KrpcTransportMessage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.lsafer.rkrpc.RkrpcInternalApi

@Serializable
sealed interface RkrpcTransportMessage {
    @SerialName("0")
    @Serializable
    data class StringMessage(val value: String) : RkrpcTransportMessage

    @Suppress("ArrayInDataClass")
    @SerialName("1")
    @Serializable
    data class BinaryMessage(val value: ByteArray) : RkrpcTransportMessage
}

@RkrpcInternalApi
fun RkrpcTransportMessage.decode(): KrpcTransportMessage = when (this) {
    is RkrpcTransportMessage.StringMessage ->
        KrpcTransportMessage.StringMessage(value)

    is RkrpcTransportMessage.BinaryMessage ->
        KrpcTransportMessage.BinaryMessage(value)
}

@RkrpcInternalApi
fun KrpcTransportMessage.encode(): RkrpcTransportMessage = when (this) {
    is KrpcTransportMessage.StringMessage ->
        RkrpcTransportMessage.StringMessage(value)

    is KrpcTransportMessage.BinaryMessage ->
        RkrpcTransportMessage.BinaryMessage(value)
}
