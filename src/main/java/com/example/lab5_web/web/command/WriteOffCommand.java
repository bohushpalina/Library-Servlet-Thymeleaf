package com.example.lab5_web.web.command;

import com.example.lab5_web.library.dao.BookCopyDao;
import com.example.lab5_web.library.dao.LookupDao;
import com.example.lab5_web.library.entity.BookCopy;
import com.example.lab5_web.library.exception.LibraryException;
import com.example.lab5_web.web.Flash;
import jakarta.servlet.http.HttpServletRequest;

/** Операция 4 (ЛР3), шаг 2: списать экземпляр (POST). */
public class WriteOffCommand implements Command {
  private final BookCopyDao bookCopyDao;
  private final LookupDao lookupDao;

  public WriteOffCommand(BookCopyDao bookCopyDao, LookupDao lookupDao) {
    this.bookCopyDao = bookCopyDao;
    this.lookupDao = lookupDao;
  }

  @Override
  public ViewResult execute(HttpServletRequest request) {
    try {
      int copyId = Params.intParam(request, "copyId");
      BookCopy copy = lookupDao.find(BookCopy.class, copyId);

      bookCopyDao.writeOff(copyId);

      String what = copy != null
          ? "«" + copy.getBook().getTitle() + "» (инв. " + copy.getInventoryNumber() + ")"
          : "экземпляр " + copyId;
      Flash.ok(request.getSession(), "Списан: " + what + ".");
    } catch (NumberFormatException e) {
      Flash.error(request.getSession(), "Выберите экземпляр из списка.");
    } catch (LibraryException e) {
      Flash.error(request.getSession(), e.getMessage());
    }
    return ViewResult.redirect("/writeoff");       // Post/Redirect/Get
  }
}
