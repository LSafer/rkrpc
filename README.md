# Reverse Krpc [![](https://jitpack.io/v/net.lsafer/rkrpc.svg)](https://jitpack.io/#net.lsafer/rkrpc)

Reverse kRPC with optional integration with ktor (aka. Client Services).

### Usage

To start lets define some services. One for client-to-server and one for server-to-client:

```kotlin
// Shared Code
@Rpc
interface HelloService { // client->server
    fun flowHello(): Flow<String>
    suspend fun getHello(): String
}

@Rpc
interface ClientNameService { // server->client
    fun flowName(): Flow<String>
    suspend fun getName(): String
}
```

Now, lets configure the backend's krpc server to use reverse krpc client
and register client-to-server services. (with Ktor as transport layer)

```kotlin
// server
fun KrpcRoute.configureKrpcServer() {
    val rClient = rkrpc { serialization { json() } }
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
}
```

Now, lets configure the frontend's krpc client and register
server-to-client services. (with Ktor as transport layer)

```kotlin
// client
suspend fun KtorRpcClient.configureKrpcReverseServer() {
    rkrpc {
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
}
```

At this point everything is in place, lets test out reverse krpc from the client.

```kotlin
suspend fun useKrpcClient(client: KtorRpcClient) {
    val helloService = client.withService<HelloService>()
    val hello = helloService.getHello()
    val helloList = helloService.flowHello().toList()

    assertEquals("Hello Sxyz", hello)
    assertEquals(listOf("Hello S", "Hello M", "Hello N"), helloList)
}
```

### Install

The main way of installing this library is
using `jitpack.io`

```kts
repositories {
    // ...
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // Replace TAG with the desired version
    implementation("net.lsafer.rkrpc:rkrpc-core:TAG")
    implementation("net.lsafer.rkrpc:rkrpc-ktor-client:TAG") // optional
    implementation("net.lsafer.rkrpc:rkrpc-ktor-server:TAG") // optional
}
```
