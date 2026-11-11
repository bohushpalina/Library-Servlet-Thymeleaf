package com.example.lab5_web.library.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reader")
public class Reader implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  private String phone;

  private String address;

  @OneToMany(mappedBy = "reader")
  private List<Loan> loans = new ArrayList<>();

  public Reader() {}

  public Reader(String fullName, String phone, String address) {
    this.fullName = fullName;
    this.phone = phone;
    this.address = address;
  }

  public Integer getId() { return id; }
  public void setId(Integer id) { this.id = id; }
  public String getFullName() { return fullName; }
  public void setFullName(String fullName) { this.fullName = fullName; }
  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }
  public String getAddress() { return address; }
  public void setAddress(String address) { this.address = address; }
  public List<Loan> getLoans() { return loans; }
  public void setLoans(List<Loan> loans) { this.loans = loans; }
}
