package com.example.lab5_web.library.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "author")
public class Author implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  // обратная сторона связи многие ко многим (владеющая сторона - Book)
  @ManyToMany(mappedBy = "authors")
  private List<Book> books = new ArrayList<>();

  public Author() {}

  public Author(String fullName) {
    this.fullName = fullName;
  }

  public Integer getId() { return id; }
  public void setId(Integer id) { this.id = id; }
  public String getFullName() { return fullName; }
  public void setFullName(String fullName) { this.fullName = fullName; }
  public List<Book> getBooks() { return books; }
  public void setBooks(List<Book> books) { this.books = books; }
}
