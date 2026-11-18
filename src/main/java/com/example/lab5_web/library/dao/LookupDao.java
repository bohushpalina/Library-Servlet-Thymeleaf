package com.example.lab5_web.library.dao;

import com.example.lab5_web.library.JpaUtil;
import com.example.lab5_web.library.entity.*;
import com.example.lab5_web.library.exception.LibraryException;
import com.example.lab5_web.library.exception.LibraryException.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/** Справочные выборки (списки с ID) для выпадающих списков на страницах. */
public class LookupDao {
  private static final Logger log = LoggerFactory.getLogger(LookupDao.class);

  public List<Author> authors() throws LibraryException {
    return query("авторов", "SELECT a FROM Author a ORDER BY a.fullName", Author.class);
  }

  public List<Book> books() throws LibraryException {
    return query("книг", "SELECT b FROM Book b ORDER BY b.title", Book.class);
  }

  public List<Reader> readers() throws LibraryException {
    return query("читателей", "SELECT r FROM Reader r ORDER BY r.fullName", Reader.class);
  }

  public List<Librarian> librarians() throws LibraryException {
    return query("библиотекарей", "SELECT l FROM Librarian l ORDER BY l.fullName", Librarian.class);
  }

  // свободные экземпляры: не списаны и нет открытой выдачи
  public List<BookCopy> freeCopies() throws LibraryException {
    return query("свободных экземпляров",
        "SELECT bc FROM BookCopy bc JOIN FETCH bc.book b "
            + "WHERE (bc.writtenOff IS NULL OR bc.writtenOff = false) "
            + "AND NOT EXISTS (SELECT l FROM Loan l WHERE l.bookCopy = bc AND l.returnDate IS NULL) "
            + "ORDER BY b.title, bc.id", BookCopy.class);
  }

  // все несписанные экземпляры (для списания)
  public List<BookCopy> activeCopies() throws LibraryException {
    return query("несписанных экземпляров",
        "SELECT bc FROM BookCopy bc JOIN FETCH bc.book b "
            + "WHERE (bc.writtenOff IS NULL OR bc.writtenOff = false) "
            + "ORDER BY b.title, bc.id", BookCopy.class);
  }

  // поиск одной сущности по id (для сообщений пользователю)
  public <T> T find(Class<T> type, int id) throws LibraryException {
    log.info("БД: поиск {} с id {}", type.getSimpleName(), id);
    EntityManager em = null;
    try {
      em = JpaUtil.createEntityManager();
      return em.find(type, id);
    } catch (PersistenceException e) {
      log.error("БД: ошибка при поиске {} с id {}", type.getSimpleName(), id, e);
      throw new LibraryException(ErrorCode.QUERY_ERROR, "Не удалось получить данные", e);
    } finally {
      if (em != null) {
        em.close();
      }
    }
  }

  private <T> List<T> query(String what, String jpql, Class<T> type) throws LibraryException {
    EntityManager em = null;
    try {
      em = JpaUtil.createEntityManager();
      List<T> result = em.createQuery(jpql, type).getResultList();
      log.info("БД: загружен список {} ({} записей)", what, result.size());
      return result;
    } catch (PersistenceException e) {
      log.error("БД: ошибка при загрузке списка {}", what, e);
      throw new LibraryException(ErrorCode.QUERY_ERROR, "Не удалось получить список " + what, e);
    } finally {
      if (em != null) {
        em.close();
      }
    }
  }
}
