package target.casestudy.retail.exception


class ProductServiceException extends RuntimeException{

    String errorMsg

    String errorCode

    ProductServiceException(String errorCode, String errorMsg){
        this.errorMsg =errorMsg
        this.errorCode=errorCode

    }
    String getErrorCode() {
        return errorCode
    }

    void setErrorCode(String errorCode) {
        this.errorCode = errorCode
    }

    String getErrorMsg() {
        return errorMsg
    }

    void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg
    }

}
