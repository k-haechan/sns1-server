package com.mysite.sns1_server.global.exception;

import com.mysite.sns1_server.global.response.code.ErrorCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
  private final ErrorCode errorCode;

  public CustomException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}
