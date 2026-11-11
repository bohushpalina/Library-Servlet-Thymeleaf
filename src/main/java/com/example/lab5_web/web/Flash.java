package com.example.lab5_web.web;

import jakarta.servlet.http.HttpSession;

import java.util.Map;

/** Одноразовое сообщение пользователю, хранится в сессии до следующего показа (шаблон Post/Redirect/Get). */
public final class Flash {
  private static final String TEXT = "flash.text";
  private static final String TYPE = "flash.type";

  private Flash() {}

  public static void ok(HttpSession session, String message) {
    set(session, "ok", message);
  }

  public static void error(HttpSession session, String message) {
    set(session, "error", message);
  }

  private static void set(HttpSession session, String type, String message) {
    session.setAttribute(TYPE, type);
    session.setAttribute(TEXT, message);
  }

  /** Переносит сообщение из сессии в модель страницы и удаляет его из сессии. */
  static void moveToModel(HttpSession session, Map<String, Object> model) {
    Object text = session.getAttribute(TEXT);
    if (text != null) {
      model.put("flash", text);
      model.put("flashType", session.getAttribute(TYPE));
      session.removeAttribute(TEXT);
      session.removeAttribute(TYPE);
    }
  }
}
