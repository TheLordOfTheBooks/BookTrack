package com.example.booktrack;

public class AlarmItem {
    private String alarmId;
    private String bookId;
    private String bookName;
    private String bookImageUrl;
    private long triggerMillis;
    private String message;

    // Required empty constructor for Firestore or Room
    public AlarmItem() {}

    public AlarmItem(String alarmId, String bookId, String bookName, String bookImageUrl, long triggerMillis, String message) {
        this.alarmId = alarmId;
        this.bookId = bookId;
        this.bookName = bookName;
        this.bookImageUrl = bookImageUrl;
        this.triggerMillis = triggerMillis;
        this.message = message;
    }

    public String getAlarmId() { return alarmId; }
    public void setAlarmId(String alarmId) { this.alarmId = alarmId; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }

    public String getBookImageUrl() { return bookImageUrl; }
    public void setBookImageUrl(String bookImageUrl) { this.bookImageUrl = bookImageUrl; }

    public long getTriggerMillis() { return triggerMillis; }
    public void setTriggerMillis(long triggerMillis) { this.triggerMillis = triggerMillis; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String toString() {
        return bookName;
    }
}
