package com.example.booktrack;

public class Book {
    private String name;
    private String author;
    private String genre;
    private String situation;
    private int pageCount;
    private String imageUrl;
    private String docId;

    public Book() {

    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getGenre() {
        return genre;
    }

    public String getSituation() {
        return situation;
    }

    public int getPageCount() {
        return pageCount;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDocId() {
        return docId;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setSituation(String situation) {
        this.situation = situation;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    @Override
    public String toString() {
        return name;
    }

}