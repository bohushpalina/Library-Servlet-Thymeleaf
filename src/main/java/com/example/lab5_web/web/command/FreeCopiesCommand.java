package com.example.lab5_web.web.command;

import com.example.lab5_web.library.dao.BookDao;
import com.example.lab5_web.library.dao.LookupDao;
import com.example.lab5_web.library.exception.LibraryException;
import jakarta.servlet.http.HttpServletRequest;

/** Операция 2 (ЛР3): найти свободные экземпляры книги по названию. */
public class FreeCopiesCommand implements Command {
  private final BookDao bookDao;
  private final LookupDao lookupDao;

  public FreeCopiesCommand(BookDao bookDao, LookupDao lookupDao) {
    this.bookDao = bookDao;
    this.lookupDao = lookupDao;
  }

  @Override
  public ViewResult execute(HttpServletRequest request) throws LibraryException {
    ViewResult result = ViewResult.view("copies").with("books", lookupDao.books());

    String title = request.getParameter("title");
    if (title != null && !title.isBlank()) {
      title = title.trim();
      result.with("searched", true)
          .with("selectedTitle", title)
          .with("copies", bookDao.findFreeCopies(title));
    }
    return result;
  }
}
