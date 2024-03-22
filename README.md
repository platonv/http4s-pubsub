# http4s-pubsub

This is a simple example of a pubsub server using http4s and fs2 with Cats-Effect.
You can subscribe to a topic and publish messages to it.
Subscription is done via Server-Sent Events. 
Publishing to a topic will broadcast the message to all subscribers.

## Endpoints:

`GET /subscribe/{topic}`

Subscribe to a topic. The server will send messages to the client via Server-Sent Events.

`POST /publish/{topic}`

Publish a message to a topic. The message(request body) will be broadcasted to all subscribers.
