package com.example.booktrack;

/**
 * Data model class representing a reading goal in the BookTrack application.
 * This class encapsulates all information related to user-created reading objectives,
 * including goal details, deadlines, and optional book state transition configurations.
 *
 * <p>GoalItem serves as the primary data structure for goal management throughout
 * the application, providing a standardized way to store and transfer goal information
 * between activities, adapters, and Firebase Firestore operations.</p>
 *
 * <p>Key features of the goal system include:
 * <ul>
 *   <li>Flexible goal descriptions supporting both predefined and custom objectives</li>
 *   <li>Precise deadline tracking with millisecond accuracy</li>
 *   <li>Optional automatic book state transitions upon goal completion</li>
 *   <li>Integration with book metadata for comprehensive tracking</li>
 *   <li>Firebase Firestore compatibility for cloud synchronization</li>
 * </ul></p>
 *
 * <p>The class supports two primary goal types:
 * <ul>
 *   <li><strong>Simple Goals</strong> - Basic objectives with descriptions and deadlines</li>
 *   <li><strong>State-Changing Goals</strong> - Goals that automatically update book status upon completion</li>
 * </ul></p>
 *
 * <p>This class is designed to work seamlessly with Firebase Firestore's automatic
 * serialization and deserialization, requiring a no-argument constructor and following
 * JavaBean conventions for property access.</p>
 *
 * @author BookTrack Development Team
 * @version 1.0
 * @since 1.0
 */
public class GoalItem {

    /** The description or title of the reading goal */
    private String description;

    /** The deadline for the goal completion in Unix timestamp (milliseconds) */
    private long deadlineMillis;

    /** Whether this goal should automatically change the associated book's reading state */
    private boolean changeState;

    /** The new reading state to apply to the book if changeState is true */
    private String newState;

    /** URL of the associated book's cover image */
    private String bookImageUrl;

    /** Unique identifier of the associated book */
    private String bookId;

    /** Display name of the associated book */
    private String bookName;

    /** Unique identifier for this goal item */
    private String id;

    /**
     * Default no-argument constructor required for Firebase Firestore serialization.
     * This constructor creates an empty GoalItem that can be populated through
     * setter methods or Firebase's automatic deserialization process.
     */
    public GoalItem() {}

    /**
     * Constructs a new GoalItem with core goal information.
     * This constructor is used for creating goals with essential properties,
     * while book-related metadata can be set separately through setter methods.
     *
     * @param description    The description or title of the reading goal
     * @param deadlineMillis The deadline for goal completion in Unix timestamp (milliseconds)
     * @param changeState    Whether this goal should automatically change the book's reading state
     * @param newState       The new reading state to apply if changeState is true (can be null)
     */
    public GoalItem(String description, long deadlineMillis, boolean changeState, String newState) {
        this.description = description;
        this.deadlineMillis = deadlineMillis;
        this.changeState = changeState;
        this.newState = newState;
    }

    /**
     * Gets the description or title of the reading goal.
     *
     * @return The goal description, such as "Finish book" or a custom user-defined objective
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the deadline for goal completion.
     *
     * @return The goal deadline as a Unix timestamp in milliseconds
     */
    public long getDeadlineMillis() {
        return deadlineMillis;
    }

    /**
     * Checks whether this goal should automatically change the associated book's reading state.
     *
     * @return {@code true} if the goal should update book state upon completion, {@code false} otherwise
     */
    public boolean isChangeState() {
        return changeState;
    }

    /**
     * Gets the new reading state to apply to the book upon goal completion.
     * This value is only meaningful if {@link #isChangeState()} returns {@code true}.
     *
     * @return The new reading state (e.g., "Read", "Currently Reading") or {@code null} if no state change
     */
    public String getNewState() {
        return newState;
    }

    /**
     * Gets the URL of the associated book's cover image.
     *
     * @return The book cover image URL, or {@code null} if no image is available
     */
    public String getBookImageUrl() {
        return bookImageUrl;
    }

    /**
     * Gets the unique identifier of the associated book.
     *
     * @return The book ID used to reference the book in Firebase Firestore
     */
    public String getBookId() {
        return bookId;
    }

    /**
     * Gets the unique identifier for this goal item.
     *
     * @return The goal ID used for Firebase Firestore document identification
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the URL of the associated book's cover image.
     *
     * @param bookImageUrl The book cover image URL
     */
    public void setBookImageUrl(String bookImageUrl) {
        this.bookImageUrl = bookImageUrl;
    }

    /**
     * Sets the unique identifier of the associated book.
     *
     * @param bookId The book ID used to reference the book in Firebase Firestore
     */
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    /**
     * Sets the deadline for goal completion.
     *
     * @param deadlineMillis The goal deadline as a Unix timestamp in milliseconds
     */
    public void setDeadlineMillis(long deadlineMillis) {
        this.deadlineMillis = deadlineMillis;
    }

    /**
     * Sets the unique identifier for this goal item.
     *
     * @param id The goal ID used for Firebase Firestore document identification
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the display name of the associated book.
     *
     * @return The book name as it appears in the user interface
     */
    public String getBookName() {
        return bookName;
    }

    /**
     * Sets the display name of the associated book.
     *
     * @param bookName The book name as it should appear in the user interface
     */
    public void setBookName(String bookName) {
        this.bookName = bookName;
    }
}