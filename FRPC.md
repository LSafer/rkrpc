# Factory RPC

Factory kRPC that enables users to establish sub clients.

### Usage

To start lets define some services:

```kotlin
// Shared Code
@Rpc
interface CounterService {
    suspend fun incrementGet(): Int
}
```

Now, lets configure the backend's krpc server and register
the services that supports sub clients feature.

```kotlin
// server
fun KrpcRoute.configureKrpcServer() {
    configureSubClient {
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
}
```

At this point everything is in place, lets test out the multi sub clients feature:

```kotlin
suspend fun useKrpcClient(client: KtorRpcClient) {
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

    client1.close()
    client2.close()
    client3.close()
}
```
