package com.hti.smpp.personalservice.modal;

import lombok.Data;

@Data
public class Book {
    private Integer id;
    private String name;
    private Integer pageCount;
    private Author author;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getPageCount() {
		return pageCount;
	}
	public void setPageCount(Integer pageCount) {
		this.pageCount = pageCount;
	}
	public Author getAuthor() {
		return author;
	}
	public void setAuthor(Author author) {
		this.author = author;
	}
	public Book() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Book(Integer id, String name, Integer pageCount, Author author) {
		super();
		this.id = id;
		this.name = name;
		this.pageCount = pageCount;
		this.author = author;
	}
    
    
}
