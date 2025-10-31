package finity.fini.apiPayload.exception.handler;

import finity.fini.apiPayload.code.BaseErrorCode;
import finity.fini.apiPayload.exception.GeneralException;

public class BankHandler extends GeneralException {

  public BankHandler(BaseErrorCode errorCode) {
    super(errorCode);
  }
}