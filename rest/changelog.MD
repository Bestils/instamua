CHANGE LOG
------
##20Jun
1. Actor class like ListingActor, UserActor etc can be extended with 'with ActorExecutionContextProvider' instead of 
declare explicitly executionContext = context.dispatcher (minor)
2. Using logback+scalalogging for both actor/non-actor code. with ActorLogging should not be used. 
To use scala-logging, just need to import com.typesafe.scalalogging.LazyLogging and inherit from LazyLogging which provide logger instance to call. eg. logger.info
