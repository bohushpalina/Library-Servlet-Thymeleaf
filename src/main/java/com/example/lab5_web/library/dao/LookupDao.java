package com.example.lab5_web.library.dao;

import com.example.lab5_web.library.entity.*;
import com.example.lab5_web.library.exception.LibraryException;
import com.example.lab5_web.library.exception.LibraryException.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;

import java.util.List;

/** Справочные выборки (списки с ID) для выпадающих списков на страницах. */
public class LookupDao {
  private final EntityManagerFactory emf;

  public LookupDao(EntityManagerFactory emf) {
    this.emf = emf;
  }

  public List<Author> authors() throws LibraryException {
    return query("SELECT a FROM Author a ORDER BY a.fullName", Author.class);
  }

  public List<Book> books() throws LibraryException {
    return query("SELECT b FROM Book b ORDER BY b.title", Book.class);
  }

  public List<Reader> readers() throws LibraryException {
    return query("SELECT r FROM Reader r ORDER BY r.fullName", Reader.class);
  }

  public List<Librarian> librarians() throws LibraryException {
    return query("SELECT l FROM Librarian l ORDER BY l.fullName", Librarian.class);
  }

  // свободные экземпляры: не списаны и нет открытой выдачи
  public List<BookCopy> freeCopies() throws LibraryException {
    return query("SELECT bc FROM BookCopy bc JOIN FETCH bc.book b "
        + "WHERE (bc.writtenOff IS NULL OR bc.writtenOff = false) "
        + "AND NOT EXISTS (SELECT l FROM Loan l WHERE l.bookCopy = bc AND l.returnDate IS NULL) "
        + "ORDER BY b.title, bc.id", BookCopy.class);
  }

  // все несписанные экземпляры (для списания)
  public List<BookCopy> activeCopies() throws LibraryException {
    return query("SELECT bc FROM BookCopy bc JOIN FETCH bc.book b "
        + "WHERE (bc.writtenOff IS NULL OR bc.writtenOff = false) "
        + "ORDER BY b.title, bc.id", BookCopy.class);
  }

  // поиск одной сущности по id (для сообщений пользователю)
  public <T> T find(Class<T> type, int id) throws LibraryException {
    EntityManager em = emf.createEntityManager();
    try {
      return em.find(type, id);
    } catch (PersistenceException e) {
      throw new LibraryException(ErrorCode.QUERY_ERROR, "Не удалось получить данные", e);
    } finally {
      em.close();
    }
  }

  private <T> List<T> query(String jpql, Class<T> type) throws LibraryException {
    EntityManager em = emf.createEntityManager();
    try {
      return em.createQuery(jpql, type).getResultList();
    } catch (PersistenceException e) {
      throw new LibraryException(ErrorCode.QUERY_ERROR, "Не удалось получить список", e);
    } finally {
      em.close();
    }
  }
}
