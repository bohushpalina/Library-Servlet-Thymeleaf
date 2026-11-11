package com.example.lab5_web.library;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class JpaUtil {
  private static final String UNIT_NAME = "library_unit";
  private static EntityManagerFactory emf;

  private JpaUtil() {}

  public static synchronized EntityManagerFactory getEntityManagerFactory() {
    if (emf == null || !emf.isOpen()) {
      emf = Persistence.createEntityManagerFactory(UNIT_NAME);
    }
    return emf;
  }

  public static synchronized void close() {
    if (emf != null && emf.isOpen()) {
      emf.close();
    }
  }
}
