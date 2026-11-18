package com.example.lab5_web.web.command;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Страница ошибки для ошибок уровня контейнера (маршрут /error из web.xml). */
public class ErrorCommand implements Command {
  private static final Logger log = LoggerFactory.getLogger(ErrorCommand.class);

  @Override
  public ViewResult execute(HttpServletRequest request) {
    Object code = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    if (code == null) {
      return ViewResult.redirect("/");           // прямое обращение к /error без ошибки
    }
    int status = (Integer) code;
    Object uri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
    Throwable cause = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
    log.error("Ошибка контейнера: статус {}, адрес {}", status, uri, cause);

    String message = switch (status) {
      case 404 -> "Страница не найдена.";
      case 405 -> "Эта операция недоступна для данного адреса.";
      case 503 -> "Сервис временно недоступен. Попробуйте позже.";
      default -> "Произошла внутренняя ошибка. Попробуйте повторить попытку позже.";
    };
    return ViewResult.view("error").with("status", status).with("message", message);
  }
}
