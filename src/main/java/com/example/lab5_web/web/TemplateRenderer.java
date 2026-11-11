package com.example.lab5_web.web;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.util.Map;

public class TemplateRenderer {
  private final TemplateEngine engine;
  private final JakartaServletWebApplication application;

  public TemplateRenderer(ServletContext servletContext) {
    this.application = JakartaServletWebApplication.buildApplication(servletContext);

    ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
    resolver.setPrefix("templates/");
    resolver.setSuffix(".html");
    resolver.setTemplateMode(TemplateMode.HTML);
    resolver.setCharacterEncoding("UTF-8");
    resolver.setCacheable(false);

    this.engine = new TemplateEngine();
    this.engine.setTemplateResolver(resolver);
  }

  public void render(HttpServletRequest request, HttpServletResponse response,
                     String view, Map<String, Object> model) throws IOException {
    response.setContentType("text/html;charset=UTF-8");
    IWebExchange exchange = application.buildExchange(request, response);
    WebContext context = new WebContext(exchange, request.getLocale());
    context.setVariables(model);
    engine.process(view, context, response.getWriter());
  }
}
