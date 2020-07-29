# JStorage-Client
###### Java Client For JStorage
>Version: 1.1.0_4  
>ServerVersion: 1.7.1_25

## Usage
Create a new client
```java
JStorageClient client = JStorageClient.getClient();
// Using one of those methods
        getClientSimple(String host, int port, String userId, String password)
        getClientSimple(String host, int port, String loginToken)
        getClientAdvanced(String host, int port, String userId, String password, int baseThreads, int additionalThreads, int maxWaitingTasks)
        getClientAdvanced(String host, int port, String loginToken, int baseThreads, int additionalThreads, int maxWaitingTasks)
        getDevClient(String host, int port, String userId, String password)
        getDevClient(String host, int port, String loginToken)
        getClient(String host, int port, String userId, String password, String loginToken, int baseThreads, int additionalThreads, int maxWaitingTasks, int keepAliveTime, TimeUnit timeUnit)
```

Create a new request
```java
JStorageRequestBuilder builder = client.newRequest(RequestType requestType);
// add url args
builder.setArg(key, value);
// add a json object as payload
builder.setPayload(jsonObject);
// build request
JStorageRequest request = builder.build();
```

Execute request
```java
  // sync
  JStorageResult result = request.complete();
  // async
  void request.queue();
  void request.queue(Consumer<JStorageResult>);
  void request.queue(Consumer<JStorageResult>, Consumer<JStorageException>);
  CompletableFuture<JStorageResult> cf = request.submit();
```

Use result
```java
JSONObject jsonObject = result.getResult();
```