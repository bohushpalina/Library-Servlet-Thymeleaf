package com.example.lab5_web.web.command;

import jakarta.servlet.http.HttpServletRequest;

final class Params {
  private Params() {}

  /** Целочисленный параметр формы; NumberFormatException, если не задан или не число. */
  static int intParam(HttpServletRequest request, String name) {
    String value = request.getParameter(name);
    if (value == null || value.isBlank()) {
      throw new NumberFormatException("Параметр '" + name + "' не задан");
    }
    return Integer.parseInt(value.trim());
  }
}
