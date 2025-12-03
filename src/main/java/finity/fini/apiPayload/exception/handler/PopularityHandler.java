package finity.fini.apiPayload.exception.handler;

import finity.fini.apiPayload.code.BaseErrorCode;
import finity.fini.apiPayload.exception.GeneralException;

public class PopularityHandler extends GeneralException {

    public PopularityHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}