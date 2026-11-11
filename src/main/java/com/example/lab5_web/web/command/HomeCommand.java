package com.example.lab5_web.web.command;

import jakarta.servlet.http.HttpServletRequest;

/** Главная страница: меню операций. */
public class HomeCommand implements Command {
  @Override
  public ViewResult execute(HttpServletRequest request) {
    return ViewResult.view("index");
  }
}
