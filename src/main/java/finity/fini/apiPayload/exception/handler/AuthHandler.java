package finity.fini.apiPayload.exception.handler;

import finity.fini.apiPayload.code.BaseErrorCode;
import finity.fini.apiPayload.exception.GeneralException;

public class AuthHandler extends GeneralException {

    public AuthHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}