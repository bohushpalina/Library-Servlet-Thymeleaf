package com.example.lab5_web.library.dao;

import com.example.lab5_web.library.JpaUtil;
import com.example.lab5_web.library.entity.*;
import com.example.lab5_web.library.exception.LibraryException;
import com.example.lab5_web.library.exception.LibraryException.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

public class LoanDao {
  private static final Logger log = LoggerFactory.getLogger(LoanDao.class);

  // Выдаёт экземпляр читателю в одной транзакции.
  // Экземпляр недоступен: COPY_NOT_AVAILABLE. Ошибка БД: TRANSACTION_ERROR
  public void issueBook(int copyId, int readerId, int librarianId) throws LibraryException {
    log.info("БД: выдача экземпляра {} читателю {} (библиотекарь {})", copyId, readerId, librarianId);
    EntityManager em = null;
    EntityTransaction tx = null;
    try {
      em = JpaUtil.createEntityManager();
      tx = em.getTransaction();
      tx.begin();

      // SELECT ... FOR UPDATE: защита от одновременной выдачи одной книги двум читателям
      BookCopy copy = em.find(BookCopy.class, copyId, LockModeType.PESSIMISTIC_WRITE);

      boolean available = copy != null
          && !Boolean.TRUE.equals(copy.getWrittenOff())
          && countOpenLoans(em, copyId) == 0;

      if (!available) {
        log.warn("БД: экземпляр {} недоступен для выдачи", copyId);
        throw new LibraryException(ErrorCode.COPY_NOT_AVAILABLE,
            "Экземпляр " + copyId + " недоступен: он списан, уже выдан или не существует");
      }

      Loan loan = new Loan(copy,
          em.getReference(Reader.class, readerId),
          em.getReference(Librarian.class, librarianId),
          LocalDate.now());
      em.persist(loan);

      tx.commit();
      log.info("БД: экземпляр {} выдан читателю {} (библиотекарь {})", copyId, readerId, librarianId);
    } catch (PersistenceException e) {
      log.error("БД: не удалось выдать экземпляр {} читателю {}", copyId, readerId, e);
      throw new LibraryException(ErrorCode.TRANSACTION_ERROR,
          "Не удалось выдать экземпляр " + copyId + " читателю " + readerId, e);
    } finally {
      if (tx != null && tx.isActive()) {
        tx.rollback();
        log.warn("БД: транзакция выдачи экземпляра {} откатана", copyId);
      }
      if (em != null) {
        em.close();
      }
    }
  }

  // Число открытых выдач экземпляра (Criteria API)
  private long countOpenLoans(EntityManager em, int copyId) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    Root<Loan> loan = cq.from(Loan.class);
    cq.select(cb.count(loan)).where(
        cb.equal(loan.get(Loan_.bookCopy).get(BookCopy_.id), copyId),
        cb.isNull(loan.get(Loan_.returnDate)));
    return em.createQuery(cq).getSingleResult();
  }
}
