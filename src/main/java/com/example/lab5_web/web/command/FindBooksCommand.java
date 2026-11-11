package com.example.lab5_web.web.command;

import com.example.lab5_web.library.dao.BookDao;
import com.example.lab5_web.library.dao.LookupDao;
import com.example.lab5_web.library.exception.LibraryException;
import jakarta.servlet.http.HttpServletRequest;

/** Операция 1 (ЛР3): найти книги по автору. */
public class FindBooksCommand implements Command {
  private final BookDao bookDao;
  private final LookupDao lookupDao;

  public FindBooksCommand(BookDao bookDao, LookupDao lookupDao) {
    this.bookDao = bookDao;
    this.lookupDao = lookupDao;
  }

  @Override
  public ViewResult execute(HttpServletRequest request) throws LibraryException {
    ViewResult result = ViewResult.view("books").with("authors", lookupDao.authors());

    String author = request.getParameter("author");
    if (author != null) {                       // форма отправлена: выполняем поиск
      author = author.trim();                   // пустое значение = все книги
      result.with("searched", true)
          .with("selectedAuthor", author)
          .with("books", bookDao.findByAuthor(author));
    }
    return result;
  }
}
