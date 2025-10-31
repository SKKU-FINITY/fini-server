package finity.fini.apiPayload.exception.handler;

import finity.fini.apiPayload.code.BaseErrorCode;
import finity.fini.apiPayload.exception.GeneralException;

public class ProductHandler extends GeneralException {

    public ProductHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}