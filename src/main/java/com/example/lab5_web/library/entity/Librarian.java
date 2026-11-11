package com.example.lab5_web.library.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "librarian")
public class Librarian implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  @OneToMany(mappedBy = "librarian")
  private List<Loan> loans = new ArrayList<>();

  public Librarian() {}

  public Librarian(String fullName) {
    this.fullName = fullName;
  }

  public Integer getId() { return id; }
  public void setId(Integer id) { this.id = id; }
  public String getFullName() { return fullName; }
  public void setFullName(String fullName) { this.fullName = fullName; }
  public List<Loan> getLoans() { return loans; }
  public void setLoans(List<Loan> loans) { this.loans = loans; }
}
