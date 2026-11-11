package com.example.lab5_web.web.command;

import java.util.HashMap;
import java.util.Map;

/** Результат работы контроллера: либо имя шаблона Thymeleaf с данными, либо перенаправление. */
public final class ViewResult {
  private final String view;
  private final String redirect;
  private final Map<String, Object> model = new HashMap<>();

  private ViewResult(String view, String redirect) {
    this.view = view;
    this.redirect = redirect;
  }

  public static ViewResult view(String templateName) {
    return new ViewResult(templateName, null);
  }
  public static ViewResult redirect(String path) {
    return new ViewResult(null, path);
  }
  public ViewResult with(String key, Object value) {
    model.put(key, value);
    return this;
  }

  public String getView() { return view; }
  public String getRedirect() { return redirect; }
  public boolean isRedirect() { return redirect != null; }
  public Map<String, Object> getModel() { return model; }
}
