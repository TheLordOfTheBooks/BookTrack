package com.example.booktrack;

/**
 * AlarmItem represents a scheduled alarm or reminder associated with a specific book
 * in the book tracking application. This class encapsulates all the necessary information
 * needed to display and manage reading deadlines, reminders, and notifications.
 *
 * <p>Each alarm item contains metadata about the associated book (ID, name, image),
 * timing information (deadline in milliseconds), and a custom message for the reminder.
 * This class is typically used with Firebase Firestore for data persistence and
 * Android's alarm/notification system for scheduling reminders.</p>
 *
 * <p>The class follows the JavaBean pattern with a default constructor and getter/setter
 * methods, making it compatible with Firebase Firestore's automatic serialization.</p>
 *
 * @author BookTrack Development Team
 * @version 1.0
 * @since 1.0
 */
public class AlarmItem {

    /**
     * Unique identifier for this alarm item.
     * Used to distinguish between different alarms and for database operations.
     */
    private String alarmId;

    /**
     * Unique identifier of the book associated with this alarm.
     * References the book in the user's book collection.
     */
    private String bookId;

    /**
     * Display name of the book associated with this alarm.
     * Used for showing book information in notifications and UI components.
     */
    private String bookName;

    /**
     * URL or path to the book's cover image.
     * Used for displaying book cover in alarm notifications and UI.
     */
    private String bookImageUrl;

    /**
     * Deadline timestamp in milliseconds since Unix epoch.
     * Represents when the alarm should trigger or when the reading deadline occurs.
     */
    private long deadlineMillis;

    /**
     * Custom message to be displayed when the alarm triggers.
     * Contains the reminder text that will be shown to the user.
     */
    private String message;

    /**
     * Default constructor required for Firebase Firestore serialization.
     * Creates an AlarmItem with all fields initialized to their default values.
     */
    public AlarmItem() {}

    /**
     * Full constructor to create an AlarmItem with all properties specified.
     *
     * @param alarmId Unique identifier for this alarm item
     * @param bookId Unique identifier of the associated book
     * @param bookName Display name of the associated book
     * @param bookImageUrl URL or path to the book's cover image
     * @param deadlineMillis Deadline timestamp in milliseconds since Unix epoch
     * @param message Custom message to display when the alarm triggers
     */
    public AlarmItem(String alarmId, String bookId, String bookName, String bookImageUrl, long deadlineMillis, String message) {
        this.alarmId = alarmId;
        this.bookId = bookId;
        this.bookName = bookName;
        this.bookImageUrl = bookImageUrl;
        this.deadlineMillis = deadlineMillis;
        this.message = message;
    }

    /**
     * Gets the unique identifier for this alarm item.
     *
     * @return The alarm ID as a String, or null if not set
     */
    public String getAlarmId() {
        return alarmId;
    }

    /**
     * Sets the unique identifier for this alarm item.
     *
     * @param alarmId The alarm ID to set
     */
    public void setAlarmId(String alarmId) {
        this.alarmId = alarmId;
    }

    /**
     * Gets the unique identifier of the book associated with this alarm.
     *
     * @return The book ID as a String, or null if not set
     */
    public String getBookId() {
        return bookId;
    }

    /**
     * Sets the unique identifier of the book associated with this alarm.
     *
     * @param bookId The book ID to set
     */
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    /**
     * Gets the display name of the book associated with this alarm.
     *
     * @return The book name as a String, or null if not set
     */
    public String getBookName() {
        return bookName;
    }

    /**
     * Sets the display name of the book associated with this alarm.
     *
     * @param bookName The book name to set
     */
    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    /**
     * Gets the URL or path to the book's cover image.
     *
     * @return The book image URL as a String, or null if not set
     */
    public String getBookImageUrl() {
        return bookImageUrl;
    }

    /**
     * Sets the URL or path to the book's cover image.
     *
     * @param bookImageUrl The book image URL to set
     */
    public void setBookImageUrl(String bookImageUrl) {
        this.bookImageUrl = bookImageUrl;
    }

    /**
     * Gets the deadline timestamp in milliseconds since Unix epoch.
     *
     * @return The deadline in milliseconds as a long value
     */
    public long getDeadlineMillis() {
        return deadlineMillis;
    }

    /**
     * Sets the deadline timestamp in milliseconds since Unix epoch.
     *
     * @param deadlineMillis The deadline in milliseconds to set
     */
    public void setDeadlineMillis(long deadlineMillis) {
        this.deadlineMillis = deadlineMillis;
    }

    /**
     * Gets the custom message to be displayed when the alarm triggers.
     *
     * @return The alarm message as a String, or null if not set
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the custom message to be displayed when the alarm triggers.
     *
     * @param message The alarm message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns a string representation of this AlarmItem.
     * For simplicity and display purposes, this returns the book name.
     *
     * @return The book name, or null if the book name is not set
     */
    @Override
    public String toString() {
        return bookName;
    }
}
