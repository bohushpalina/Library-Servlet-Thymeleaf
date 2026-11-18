package com.example.lab5_web.library.exception;

public class LibraryException extends Exception {

  public enum ErrorCode {
    QUERY_ERROR,
    TRANSACTION_ERROR,
    COPY_NOT_FOUND,
    COPY_NOT_AVAILABLE,
    DB_UNAVAILABLE
  }

  private final ErrorCode errorCode;

  public LibraryException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public LibraryException(ErrorCode errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }

  /** Краткое понятное сообщение для пользователя. */
  public String getUserMessage() {
    if (errorCode == ErrorCode.DB_UNAVAILABLE) {
      return "Не удалось подключиться к базе данных. Попробуйте повторить попытку позже.";
    }
    return getMessage();
  }
}