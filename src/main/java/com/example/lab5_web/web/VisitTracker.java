package com.example.lab5_web.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public final class VisitTracker {
  private static final String COOKIE_LAST = "lastVisit";
  private static final String COOKIE_COUNT = "visitCount";
  private static final int ONE_YEAR = 60 * 60 * 24 * 365;
  private static final DateTimeFormatter FORMAT =
      DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(ZoneId.systemDefault());

  private VisitTracker() {}

  public static void track(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
    long now = System.currentTimeMillis();

    if (session.getAttribute("visitCount") == null) {
      // первый запрос нового сеанса: это новый визит
      long previousCount = parseLong(cookie(request, COOKIE_COUNT), 0);
      long previousVisit = parseLong(cookie(request, COOKIE_LAST), 0);
      long count = previousCount + 1;

      session.setAttribute("visitCount", count);
      session.setAttribute("previousVisit", previousVisit > 0 ? format(previousVisit) : null);
      session.setAttribute("sessionStart", format(now));
      session.setAttribute("requests", 0);
      addCookie(response, COOKIE_COUNT, String.valueOf(count));
    }

    session.setAttribute("requests", (Integer) session.getAttribute("requests") + 1);
    // время последнего обращения обновляется при каждом запросе
    addCookie(response, COOKIE_LAST, String.valueOf(now));
  }

  /** Данные сессии для отображения на каждой странице. */
  static void moveToModel(HttpSession session, Map<String, Object> model) {
    model.put("visitCount", session.getAttribute("visitCount"));
    model.put("previousVisit", session.getAttribute("previousVisit"));
    model.put("sessionStart", session.getAttribute("sessionStart"));
    model.put("requests", session.getAttribute("requests"));
  }

  private static String format(long millis) {
    return FORMAT.format(Instant.ofEpochMilli(millis));
  }

  private static String cookie(HttpServletRequest request, String name) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie c : cookies) {
        if (name.equals(c.getName())) {
          return c.getValue();
        }
      }
    }
    return null;
  }

  private static long parseLong(String value, long fallback) {
    try {
      return value == null ? fallback : Long.parseLong(value);
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  private static void addCookie(HttpServletResponse response, String name, String value) {
    Cookie cookie = new Cookie(name, value);
    cookie.setPath("/");
    cookie.setMaxAge(ONE_YEAR);
    cookie.setHttpOnly(true);
    response.addCookie(cookie);
  }
}
