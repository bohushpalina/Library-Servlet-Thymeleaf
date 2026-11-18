package com.example.lab5_web.library;

import com.example.lab5_web.library.exception.LibraryException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Хранит единственный EntityManagerFactory приложения и выдаёт EntityManager для DAO. */
public final class JpaUtil {
  private static final Logger log = LoggerFactory.getLogger(JpaUtil.class);
  private static final String UNIT_NAME = "library_unit";
  private static EntityManagerFactory emf;

  private JpaUtil() {}

  public static synchronized EntityManagerFactory getEntityManagerFactory() {
    if (emf == null || !emf.isOpen()) {
      log.info("Создание EntityManagerFactory (persistence unit '{}')", UNIT_NAME);
      emf = Persistence.createEntityManagerFactory(UNIT_NAME);
    }
    return emf;
  }

  /**
   * Если БД недоступна, фабрика сбрасывается, чтобы следующая попытка подключилась заново.
   */
  public static synchronized EntityManager createEntityManager() throws LibraryException {
    try {
      return getEntityManagerFactory().createEntityManager();
    } catch (RuntimeException e) {
      log.error("Не удалось создать EntityManager (проблема с подключением к БД?): {}", e.getMessage());
      close();
      throw e;
    }
  }

  public static synchronized void close() {
    if (emf != null) {
      try {
        if (emf.isOpen()) {
          emf.close();
          log.info("EntityManagerFactory закрыта");
        }
      } catch (RuntimeException e) {
        log.warn("Ошибка при закрытии EntityManagerFactory: {}", e.getMessage());
      } finally {
        emf = null;
      }
    }
  }
}
