package com.example.lab5_web.library.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "book")
public class Book implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false)
  private String title;

  private String publisher;

  @Column(name = "publish_year")
  private String publishYear;

  @ManyToMany
  @JoinTable(name = "book_author",
          joinColumns = @JoinColumn(name = "book_id"),
          inverseJoinColumns = @JoinColumn(name = "author_id"))
  private List<Author> authors = new ArrayList<>();

  @OneToMany(mappedBy = "book", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private List<BookCopy> copies = new ArrayList<>();

  public Book() {}

  public Book(String title, String publisher, String publishYear) {
    this.title = title;
    this.publisher = publisher;
    this.publishYear = publishYear;
  }

  public Integer getId() { return id; }
  public void setId(Integer id) { this.id = id; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getPublisher() { return publisher; }
  public void setPublisher(String publisher) { this.publisher = publisher; }
  public String getPublishYear() { return publishYear; }
  public void setPublishYear(String publishYear) { this.publishYear = publishYear; }
  public List<Author> getAuthors() { return authors; }
  public void setAuthors(List<Author> authors) { this.authors = authors; }
  public List<BookCopy> getCopies() { return copies; }
  public void setCopies(List<BookCopy> copies) { this.copies = copies; }
}
