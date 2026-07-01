# Mock RPC

Mock kRPC that enables users to establish mock clients and servers.

### Usage

To start lets define some services:

```kotlin
// Shared Code
@Rpc
interface HelloService { // client->server
    fun flowHello(): Flow<String>
    suspend fun getHello(): String
}
```

Now, lets configure create the mock server and use it:

```kotlin
fun main() {
    val client = krpc(scope) {
        // shared
        rpcConfig { serialization { json() } }

        // server
        registerService<HelloService> {
            object : HelloService {
                override suspend fun getHello(): String {
                    return "Hello Sxyz"
                }

                override fun flowHello(): Flow<String> {
                    return flowOf("S", "M", "N")
                        .map { "Hello $it" }
                }
            }
        }
    }

    val helloService = client.withService<HelloService>()
    val hello = helloService.getHello()
    val helloList = helloService.flowHello().toList()

    assertEquals("Hello Sxyz", hello)
    assertEquals(listOf("Hello S", "Hello M", "Hello N"), helloList)
}
```
