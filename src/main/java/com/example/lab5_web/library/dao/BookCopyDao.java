package com.example.lab5_web.library.dao;

import com.example.lab5_web.library.entity.BookCopy;
import com.example.lab5_web.library.entity.BookCopy_;
import com.example.lab5_web.library.exception.LibraryException;
import com.example.lab5_web.library.exception.LibraryException.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BookCopyDao {
  private static final Logger log = LoggerFactory.getLogger(BookCopyDao.class);

  private final EntityManagerFactory emf;

  public BookCopyDao(EntityManagerFactory emf) {
    this.emf = emf;
  }

  // Списывает экземпляр по id; если его нет, выбрасывает COPY_NOT_FOUND
  public void writeOff(int copyId) throws LibraryException {
    EntityManager em = emf.createEntityManager();
    EntityTransaction tx = em.getTransaction();
    try {
      tx.begin();

      CriteriaBuilder cb = em.getCriteriaBuilder();
      CriteriaUpdate<BookCopy> update = cb.createCriteriaUpdate(BookCopy.class);
      Root<BookCopy> copy = update.from(BookCopy.class);
      update.set(copy.get(BookCopy_.writtenOff), true)
            .where(cb.equal(copy.get(BookCopy_.id), copyId));

      int updated = em.createQuery(update).executeUpdate();

      if (updated == 0) {
        log.warn("Экземпляр книги с ID {} не найден для списания", copyId);
        throw new LibraryException(ErrorCode.COPY_NOT_FOUND,
            "Экземпляр книги с ID " + copyId + " не найден");
      }
      tx.commit();
      log.info("Экземпляр книги с ID {} успешно списан", copyId);
    } catch (PersistenceException e) {
      log.error("Ошибка при списании экземпляра книги {}", copyId, e);
      throw new LibraryException(ErrorCode.QUERY_ERROR,
          "Не удалось списать экземпляр книги " + copyId, e);
    } finally {
      if (tx.isActive()) {
        tx.rollback();
      }
      em.close();
    }
  }
}
