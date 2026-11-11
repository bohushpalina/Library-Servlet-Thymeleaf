package com.example.lab5_web.library.dao;

import com.example.lab5_web.library.entity.*;
import com.example.lab5_web.library.exception.LibraryException;
import com.example.lab5_web.library.exception.LibraryException.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.criteria.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class BookDao {
  private static final Logger log = LoggerFactory.getLogger(BookDao.class);

  private final EntityManagerFactory emf;

  public BookDao(EntityManagerFactory emf) {
    this.emf = emf;
  }

  // Свободные экземпляры книги по названию: не списанные и не выданные на руки
  public List<BookCopy> findFreeCopies(String title) throws LibraryException {
    EntityManager em = emf.createEntityManager();
    try {
      CriteriaBuilder cb = em.getCriteriaBuilder();
      CriteriaQuery<BookCopy> cq = cb.createQuery(BookCopy.class);
      Root<BookCopy> copy = cq.from(BookCopy.class);
      Join<BookCopy, Book> book = copy.join(BookCopy_.book);

      // подзапрос: открытая выдача (return_date IS NULL) этого экземпляра
      Subquery<Integer> openLoan = cq.subquery(Integer.class);
      Root<Loan> loan = openLoan.from(Loan.class);
      openLoan.select(cb.literal(1))
          .where(cb.equal(loan.get(Loan_.bookCopy), copy),
                 cb.isNull(loan.get(Loan_.returnDate)));

      cq.select(copy).where(
          cb.equal(book.get(Book_.title), title),
          cb.or(cb.isNull(copy.get(BookCopy_.writtenOff)),
                cb.isFalse(copy.get(BookCopy_.writtenOff))),
          cb.not(cb.exists(openLoan)));

      List<BookCopy> copies = em.createQuery(cq).getResultList();
      log.info("Найдено {} свободных экземпляров для книги '{}'", copies.size(), title);
      return copies;
    } catch (PersistenceException e) {
      log.error("Ошибка при поиске свободных экземпляров книги '{}'", title, e);
      throw new LibraryException(ErrorCode.QUERY_ERROR,
          "Не удалось найти свободные экземпляры книги '" + title + "'", e);
    } finally {
      em.close();
    }
  }

  // Книги автора с указанным ФИО. Условие добавляется динамически:
  // если ФИО пустое, возвращаются все книги
  public List<Book> findByAuthor(String authorName) throws LibraryException {
    EntityManager em = emf.createEntityManager();
    try {
      CriteriaBuilder cb = em.getCriteriaBuilder();
      CriteriaQuery<Book> cq = cb.createQuery(Book.class);
      Root<Book> book = cq.from(Book.class);

      List<Predicate> conditions = new ArrayList<>();
      if (authorName != null && !authorName.isBlank()) {
        Join<Book, Author> author = book.join(Book_.authors);
        conditions.add(cb.equal(author.get(Author_.fullName), authorName));
      }

      cq.select(book).distinct(true)
          .where(conditions.toArray(new Predicate[0]))
          .orderBy(cb.asc(book.get(Book_.title)));

      List<Book> books = em.createQuery(cq).getResultList();
      log.info("Найдено {} книг для автора '{}'", books.size(), authorName);
      return books;
    } catch (PersistenceException e) {
      log.error("Ошибка при поиске книг автора '{}'", authorName, e);
      throw new LibraryException(ErrorCode.QUERY_ERROR,
          "Не удалось найти книги автора '" + authorName + "'", e);
    } finally {
      em.close();
    }
  }
}
