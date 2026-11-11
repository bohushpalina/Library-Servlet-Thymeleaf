package com.example.lab5_web.web.command;

import com.example.lab5_web.library.dao.LookupDao;
import com.example.lab5_web.library.exception.LibraryException;
import jakarta.servlet.http.HttpServletRequest;

/** Операция 4 (ЛР3), шаг 1: форма списания со списком несписанных экземпляров. */
public class WriteOffFormCommand implements Command {
  private final LookupDao lookupDao;

  public WriteOffFormCommand(LookupDao lookupDao) {
    this.lookupDao = lookupDao;
  }

  @Override
  public ViewResult execute(HttpServletRequest request) throws LibraryException {
    return ViewResult.view("writeoff").with("copies", lookupDao.activeCopies());
  }
}
