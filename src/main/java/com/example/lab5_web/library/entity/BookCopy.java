package com.example.lab5_web.library.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "book_copy")
public class BookCopy implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  // владеющая сторона связи многие к одному (содержит внешний ключ)
  @ManyToOne(optional = false)
  @JoinColumn(name = "book_id")
  private Book book;

  @Column(name = "inventory_number")
  private String inventoryNumber;

  @Column(name = "written_off")
  private Boolean writtenOff = false;

  public BookCopy() {}

  public BookCopy(Book book, String inventoryNumber) {
    this.book = book;
    this.inventoryNumber = inventoryNumber;
  }

  public Integer getId() { return id; }
  public void setId(Integer id) { this.id = id; }
  public Book getBook() { return book; }
  public void setBook(Book book) { this.book = book; }
  public String getInventoryNumber() { return inventoryNumber; }
  public void setInventoryNumber(String inventoryNumber) { this.inventoryNumber = inventoryNumber; }
  public Boolean getWrittenOff() { return writtenOff; }
  public void setWrittenOff(Boolean writtenOff) { this.writtenOff = writtenOff; }
}
