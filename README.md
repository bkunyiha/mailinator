# Mailinator Example Restful HTTP Service

## Non Effectful Service And Repository(Model) Layers
Since the application uses an in-memory store and does not interact with any external service, it's synchronous and has no side effects(no exceptions in this case).
The store being synchronous and effect free allows us to use `Either` data type for the the service and repository(model) layers instead of an `IO` data type like `cats-effect` or `scalaz-zio`


## Running the app
Then run the app using the following command

`$ sbt run`


## Web interface
http://localhost:8080