package target.casestudy.retail.util

import groovy.util.logging.Slf4j

@Slf4j
trait Threadable {
    Object unwrapExecutionException(Closure action) {
        try {
            return action()
        } catch (Exception e) {
            log.error("Exception occured::action=unwrapExecutionException::exception:${e.message}",e)
            throw e
        }
    }
}
