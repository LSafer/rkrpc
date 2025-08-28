package net.lsafer.rkrpc.core.test

import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
interface HelloService {
    fun flowHello(): Flow<String>
    suspend fun getHello(): String
}

@Rpc
interface ClientNameService {
    fun flowName(): Flow<String>
    suspend fun getName(): String
}
