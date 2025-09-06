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
import net.lsafer.rkrpc.configureSubClient
import net.lsafer.rkrpc.reverseServer
import net.lsafer.rkrpc.reverseClient
import net.lsafer.rkrpc.test.util.createServerClientTest
import net.lsafer.rkrpc.newSubClient
import kotlin.test.Test
import kotlin.test.assertEquals

open class MainTest {
    @Test
    fun `simple client server send receive`() = runTest {
        val scope = CoroutineScope(coroutineContext + Dispatchers.IO + SupervisorJob())

        createServerClientTest(
            coroutineScope = scope,
            serverBlock = {
                val rClient = reverseClient(scope) { serialization { json() } }
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
                val rServer = client.reverseServer(scope) {
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

    @Test
    fun `simple factory and withScope usage`() = runTest {
        val scope = CoroutineScope(coroutineContext + Dispatchers.IO + SupervisorJob())

        createServerClientTest(
            coroutineScope = scope,
            serverBlock = {
                configureSubClient(scope) {
                    rpcConfig { serialization { json() } }
                    registerService<CounterService> {
                        object : CounterService {
                            var counter = 0

                            override suspend fun incrementGet(): Int {
                                return ++counter
                            }
                        }
                    }
                }
            },
            clientBlock = { client ->
                val client1 = client.newSubClient(scope) { serialization { json() } }
                val client2 = client.newSubClient(scope) { serialization { json() } }
                val client3 = client.newSubClient(scope) { serialization { json() } }

                val service1 = client1.withService<CounterService>()
                val service2 = client2.withService<CounterService>()
                val service3 = client3.withService<CounterService>()

                val i = service1.incrementGet()
                val j = service2.incrementGet()
                val k = service3.incrementGet()

                val l = service1.incrementGet()
                val m = service2.incrementGet()
                val n = service3.incrementGet()

                assertEquals(1, i)
                assertEquals(1, j)
                assertEquals(1, k)
                assertEquals(2, l)
                assertEquals(2, m)
                assertEquals(2, n)

                client.close()
            },
        )

        scope.coroutineContext.job.join()
    }
}
