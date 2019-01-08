# Mailinator Example Restful HTTP Service

## Non Effectfull
Since the application uses an in-memory stores, it has no side effects(no exceptions in this case) and is synchronous.
The store being synchronous and effect free allows us to use either data type for the the service and repository(model) layers instead of an IO data type like cats-effect or scalaz-zio


