package com.example.lab5_web.web.command;

import com.example.lab5_web.library.dao.LoanDao;
import com.example.lab5_web.library.dao.LookupDao;
import com.example.lab5_web.library.entity.BookCopy;
import com.example.lab5_web.library.entity.Librarian;
import com.example.lab5_web.library.entity.Reader;
import com.example.lab5_web.library.exception.LibraryException;
import com.example.lab5_web.web.Flash;
import jakarta.servlet.http.HttpServletRequest;

/** Операция 3 (ЛР3), шаг 2: оформить выдачу экземпляра читателю (POST). */
public class IssueCommand implements Command {
  private final LoanDao loanDao;
  private final LookupDao lookupDao;

  public IssueCommand(LoanDao loanDao, LookupDao lookupDao) {
    this.loanDao = loanDao;
    this.lookupDao = lookupDao;
  }

  @Override
  public ViewResult execute(HttpServletRequest request) {
    try {
      int copyId = Params.intParam(request, "copyId");
      int readerId = Params.intParam(request, "readerId");
      int librarianId = Params.intParam(request, "librarianId");

      BookCopy copy = lookupDao.find(BookCopy.class, copyId);
      Reader reader = lookupDao.find(Reader.class, readerId);
      Librarian librarian = lookupDao.find(Librarian.class, librarianId);

      loanDao.issueBook(copyId, readerId, librarianId);

      String title = copy != null ? copy.getBook().getTitle() : "экземпляр " + copyId;
      String readerName = reader != null ? reader.getFullName() : "читатель " + readerId;
      String librarianName = librarian != null ? librarian.getFullName() : "библиотекарь " + librarianId;
      Flash.ok(request.getSession(), "Книга «" + title + "» выдана читателю " + readerName
          + " (оформил: " + librarianName + ").");
    } catch (NumberFormatException e) {
      Flash.error(request.getSession(), "Выберите экземпляр, читателя и библиотекаря из списков.");
    } catch (LibraryException e) {
      Flash.error(request.getSession(), e.getMessage());
    }
    return ViewResult.redirect("/issue");          // Post/Redirect/Get
  }
}
