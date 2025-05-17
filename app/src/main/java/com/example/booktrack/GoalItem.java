package com.example.booktrack;

public class GoalItem {
    private String description;
    private long deadlineMillis;
    private boolean changeState;
    private String newState;
    private String bookImageUrl;
    private String bookId;
    private String bookName;
    private String id;

    public GoalItem() {}  // Required for Firestore

    public GoalItem(String description, long deadlineMillis, boolean changeState, String newState) {
        this.description = description;
        this.deadlineMillis = deadlineMillis;
        this.changeState = changeState;
        this.newState = newState;

    }

    public String getDescription() { return description; }
    public long getDeadlineMillis() {
        return deadlineMillis;
    }
    public boolean isChangeState() { return changeState; }
    public String getNewState() { return newState; }

    public String getBookImageUrl() {
        return bookImageUrl;
    }

    public String getBookId() {
        return bookId;
    }



    public String getId() {
        return id;
    }

    // Setters (if needed)
    public void setBookImageUrl(String bookImageUrl) {
        this.bookImageUrl = bookImageUrl;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public void setDeadlineMillis(long deadlineMillis) {
        this.deadlineMillis = deadlineMillis;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }
}
