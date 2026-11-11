package com.example.lab5_web.library.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "loan")
public class Loan implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "copy_id")
  private BookCopy bookCopy;

  @ManyToOne(optional = false)
  @JoinColumn(name = "reader_id")
  private Reader reader;

  @ManyToOne(optional = false)
  @JoinColumn(name = "librarian_id")
  private Librarian librarian;

  @Column(name = "issue_date", nullable = false)
  private LocalDate issueDate;

  @Column(name = "return_date")
  private LocalDate returnDate;

  public Loan() {}

  public Loan(BookCopy bookCopy, Reader reader, Librarian librarian, LocalDate issueDate) {
    this.bookCopy = bookCopy;
    this.reader = reader;
    this.librarian = librarian;
    this.issueDate = issueDate;
  }

  public Integer getId() { return id; }
  public void setId(Integer id) { this.id = id; }
  public BookCopy getBookCopy() { return bookCopy; }
  public void setBookCopy(BookCopy bookCopy) { this.bookCopy = bookCopy; }
  public Reader getReader() { return reader; }
  public void setReader(Reader reader) { this.reader = reader; }
  public Librarian getLibrarian() { return librarian; }
  public void setLibrarian(Librarian librarian) { this.librarian = librarian; }
  public LocalDate getIssueDate() { return issueDate; }
  public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
  public LocalDate getReturnDate() { return returnDate; }
  public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
}
