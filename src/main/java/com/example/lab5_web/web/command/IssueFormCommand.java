package com.example.lab5_web.web.command;

import com.example.lab5_web.library.dao.LookupDao;
import com.example.lab5_web.library.exception.LibraryException;
import jakarta.servlet.http.HttpServletRequest;

/** Операция 3 (ЛР3), шаг 1: форма выдачи книги со списками экземпляров, читателей и библиотекарей. */
public class IssueFormCommand implements Command {
  private final LookupDao lookupDao;

  public IssueFormCommand(LookupDao lookupDao) {
    this.lookupDao = lookupDao;
  }

  @Override
  public ViewResult execute(HttpServletRequest request) throws LibraryException {
    return ViewResult.view("issue")
        .with("copies", lookupDao.freeCopies())
        .with("readers", lookupDao.readers())
        .with("librarians", lookupDao.librarians());
  }
}
