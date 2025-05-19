package com.example.booktrack;

public class AlarmItem {
    private String alarmId;
    private String bookId;
    private String bookName;
    private String bookImageUrl;
    private long deadlineMillis;
    private String message;

    public AlarmItem() {}

    public AlarmItem(String alarmId, String bookId, String bookName, String bookImageUrl, long deadlineMillis, String message) {
        this.alarmId = alarmId;
        this.bookId = bookId;
        this.bookName = bookName;
        this.bookImageUrl = bookImageUrl;
        this.deadlineMillis = deadlineMillis;
        this.message = message;
    }

    public String getAlarmId() {
        return alarmId;
    }
    public void setAlarmId(String alarmId) {
        this.alarmId = alarmId;
    }

    public String getBookId() {
        return bookId;
    }
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getBookName() {
        return bookName;
    }
    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getBookImageUrl() {
        return bookImageUrl;
    }
    public void setBookImageUrl(String bookImageUrl) {
        this.bookImageUrl = bookImageUrl;
    }

    public long getDeadlineMillis() {
        return deadlineMillis;
    }
    public void setDeadlineMillis(long deadlineMillis) {
        this.deadlineMillis = deadlineMillis;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String toString() {
        return bookName;
    }
}
