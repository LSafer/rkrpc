package net.lsafer.rkrpc.test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.job
import kotlinx.coroutines.test.runTest
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.rpc.registerService
import kotlinx.rpc.withService
import net.lsafer.rkrpc.rkrpc
import net.lsafer.rkrpc.test.util.createServerClientTest
import kotlin.test.Test
import kotlin.test.assertEquals

open class MainTest {
    @Test
    fun `simple client server send receive`() = runTest {
        val scope = CoroutineScope(coroutineContext + Dispatchers.IO + SupervisorJob())

        createServerClientTest(
            coroutineScope = scope,
            serverBlock = {
                val rClient = rkrpc(scope) { serialization { json() } }
                val nameService = rClient.withService<ClientNameService>()

                registerService<HelloService> {
                    object : HelloService {
                        override fun flowHello(): Flow<String> {
                            return nameService.flowName()
                                .map { "Hello $it" }
                        }

                        override suspend fun getHello(): String {
                            val name = nameService.getName()
                            return "Hello $name"
                        }
                    }
                }
            },
            clientBlock = { client ->
                val rServer = client.rkrpc(scope) {
                    rpcConfig { serialization { json() } }
                    registerService<ClientNameService> {
                        object : ClientNameService {
                            override fun flowName(): Flow<String> {
                                return flowOf("S", "M", "N")
                            }

                            override suspend fun getName(): String {
                                return "Sxyz"
                            }
                        }
                    }
                }

                val helloService = client.withService<HelloService>()

                val hello = helloService.getHello()
                val helloList = helloService.flowHello().toList()

                assertEquals("Hello Sxyz", hello)
                assertEquals(listOf("Hello S", "Hello M", "Hello N"), helloList)

                rServer.close()
            },
        )

        scope.coroutineContext.job.join()
    }
}
