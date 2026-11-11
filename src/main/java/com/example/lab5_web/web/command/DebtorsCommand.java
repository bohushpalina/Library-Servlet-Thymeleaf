package com.example.lab5_web.web.command;

import com.example.lab5_web.library.dao.ReaderDao;
import com.example.lab5_web.library.exception.LibraryException;
import jakarta.servlet.http.HttpServletRequest;

/** Операция 5 (ЛР3): показать должников. */
public class DebtorsCommand implements Command {
  private final ReaderDao readerDao;

  public DebtorsCommand(ReaderDao readerDao) {
    this.readerDao = readerDao;
  }

  @Override
  public ViewResult execute(HttpServletRequest request) throws LibraryException {
    return ViewResult.view("debtors").with("debtors", readerDao.findDebtors());
  }
}
