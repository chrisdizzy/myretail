package target.casestudy.retail.advice

import groovy.util.logging.Slf4j
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import target.casestudy.retail.contract.ErrorBody
import target.casestudy.retail.contract.ErrorResponse
import target.casestudy.retail.exception.ProductServiceException

import javax.servlet.http.HttpServletRequest

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR

@Slf4j
@ControllerAdvice
class ProductControllerAdvice {

    @ExceptionHandler
    @Order(Ordered.LOWEST_PRECEDENCE)
    @ResponseStatus(value = INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse defaultHandler(Throwable e, HttpServletRequest request) {

        ErrorResponse errorResponse = null
        if(e.getCause() instanceof ProductServiceException){
            String errorMsg = ((ProductServiceException)e.getCause()).errorMsg
            String errorCode = ((ProductServiceException)e.getCause()).errorCode
            log.error("Exception=ProductServiceException,uri=${request.getRequestURI()},httpStatus=${INTERNAL_SERVER_ERROR.value()},httpReason=${errorCode},message=ProductServiceException occured :${errorMsg}", e)
            errorResponse =  new ErrorResponse(
                    errors : [new ErrorBody(errorCode:"INTERNAL_SERVER_ERROR" ,errorMsg:errorMsg    )] )

        } else{
            log.error("Exception=${e?.class?.simpleName},uri=${request.getRequestURI()},httpStatus=${INTERNAL_SERVER_ERROR.value()},httpReason=${INTERNAL_SERVER_ERROR.reasonPhrase},message=Unexpected error: ${e?.message}", e)
            errorResponse = new ErrorResponse(
                    errors : [new ErrorBody(errorCode:"INTERNAL_SERVER_ERROR" ,errorMsg: "${e.getClass().name}: ${e.getMessage()}")]
            )
        }

        return errorResponse
    }

}
