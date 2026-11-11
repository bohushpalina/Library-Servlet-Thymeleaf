package com.example.lab5_web.web.command;

import jakarta.servlet.http.HttpServletRequest;

public interface Command {
  ViewResult execute(HttpServletRequest request) throws Exception;
}
