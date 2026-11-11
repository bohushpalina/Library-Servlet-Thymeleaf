package com.example.lab5_web.library.dao;

import com.example.lab5_web.library.entity.Loan;
import com.example.lab5_web.library.entity.Loan_;
import com.example.lab5_web.library.entity.Reader;
import com.example.lab5_web.library.exception.LibraryException;
import com.example.lab5_web.library.exception.LibraryException.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

public class ReaderDao {
  private static final Logger log = LoggerFactory.getLogger(ReaderDao.class);

  private final EntityManagerFactory emf;

  public ReaderDao(EntityManagerFactory emf) {
    this.emf = emf;
  }

  // Читатели-должники: есть невозвращённая книга, выданная больше месяца назад
  public List<Reader> findDebtors() throws LibraryException {
    EntityManager em = emf.createEntityManager();
    try {
      CriteriaBuilder cb = em.getCriteriaBuilder();
      CriteriaQuery<Reader> cq = cb.createQuery(Reader.class);
      Root<Reader> reader = cq.from(Reader.class);

      LocalDate limitDate = LocalDate.now().minusMonths(1);

      // подзапрос: просроченная невозвращённая выдача этого читателя
      Subquery<Integer> overdue = cq.subquery(Integer.class);
      Root<Loan> loan = overdue.from(Loan.class);
      overdue.select(cb.literal(1)).where(
          cb.equal(loan.get(Loan_.reader), reader),
          cb.isNull(loan.get(Loan_.returnDate)),
          cb.lessThan(loan.get(Loan_.issueDate), limitDate));

      cq.select(reader).where(cb.exists(overdue));

      List<Reader> debtors = em.createQuery(cq).getResultList();
      log.info("Найдено должников: {}", debtors.size());
      return debtors;
    } catch (PersistenceException e) {
      log.error("Ошибка при поиске должников", e);
      throw new LibraryException(ErrorCode.QUERY_ERROR, "Не удалось получить список должников", e);
    } finally {
      em.close();
    }
  }
}
