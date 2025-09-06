package net.lsafer.rkrpc.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.rpc.RpcServer
import kotlinx.rpc.krpc.KrpcConfig
import kotlinx.rpc.krpc.server.KrpcServer
import net.lsafer.rkrpc.RkrpcInternalApi

@RkrpcInternalApi
class FkrpcServiceImpl(
    val coroutineScope: CoroutineScope,
    val config: KrpcConfig.Server,
    val registrations: List<(RpcServer) -> Unit>
) : FkrpcService {
    override fun subscribe(key: String): Flow<RkrpcTransportMessage> {
        return flow {
            val transport = getOrCreateTransport(key)
            emitAll(transport.outChannel.consumeAsFlow())
        }
    }

    override suspend fun send(key: String, message: RkrpcTransportMessage) {
        val transport = getOrCreateTransport(key)
        transport.inChannel.send(message)
    }

    /* ---------- INTERNAL COMPONENTS ---------- */

    private val mutex = Semaphore(permits = 1)
    private val transportMap = mutableMapOf<String, RkrpcTransport>()

    private suspend fun getOrCreateTransport(key: String): RkrpcTransport {
        return mutex.withPermit {
            transportMap[key]?.let { return it }

            // Transport Setup
            val tempScope = CoroutineScope(
                coroutineScope.coroutineContext +
                        Job(coroutineScope.coroutineContext.job)
            )
            val transport = RkrpcTransport(tempScope)

            // Server Construction
            val server = object : KrpcServer(config, transport) {}

            registrations.forEach {
                it(server)
            }

            transportMap[key] = transport

            coroutineScope.launch {
                server.awaitCompletion()

                mutex.withPermit {
                    if (transportMap[key] == transport) {
                        transportMap.remove(key)
                    }
                }
            }

            transport
        }
    }
}
