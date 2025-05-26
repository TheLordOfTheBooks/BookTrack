package com.example.booktrack;

/**
 * Represents a book entity in the BookTrack application.
 * This class serves as a data model for storing book information including
 * metadata, reading status, and Firebase document reference.
 *
 * <p>The Book class is designed to work seamlessly with Firebase Firestore
 * for data persistence and retrieval. It includes all necessary fields to
 * represent a complete book record in the user's personal library.
 *
 * <p>Key features:
 * <ul>
 *   <li>Complete book metadata storage (name, author, genre, page count)</li>
 *   <li>Reading status tracking</li>
 *   <li>Book cover image URL storage</li>
 *   <li>Firebase Firestore document ID reference</li>
 *   <li>Default constructor for Firebase deserialization</li>
 * </ul>
 *
 * @author BookTrack Development Team
 * @version 1.0
 * @since 1.0
 */
public class Book {

    /** The title/name of the book */
    private String name;

    /** The author of the book */
    private String author;

    /** The genre/category of the book (Fantasy, Mystery, Horror, Romance, Young Adult, Others) */
    private String genre;

    /** The current reading status (Read, Currently Reading, Stopped Reading, Want to Read) */
    private String situation;

    /** The total number of pages in the book */
    private int pageCount;

    /** The URL of the book cover image stored in Firebase Storage */
    private String imageUrl;

    /** The Firestore document ID for this book record */
    private String docId;

    /**
     * Default no-argument constructor required for Firebase Firestore deserialization.
     * Creates an empty Book object with all fields set to their default values.
     *
     * <p><b>Note:</b> This constructor is primarily used by Firebase Firestore
     * when converting documents to Book objects. For creating new Book instances
     * in application code, consider using the setter methods to populate fields.
     */
    public Book() {
        // Empty constructor required for Firebase
    }

    /**
     * Gets the name/title of the book.
     *
     * @return the book's title, or null if not set
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the author of the book.
     *
     * @return the book's author, or null if not set
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Gets the genre/category of the book.
     *
     * @return the book's genre from the predefined list (Fantasy, Mystery, Horror,
     *         Romance, Young Adult, Others), or null if not set
     */
    public String getGenre() {
        return genre;
    }

    /**
     * Gets the current reading status/situation of the book.
     *
     * @return the reading status from the predefined list (Read, Currently Reading,
     *         Stopped Reading, Want to Read), or null if not set
     */
    public String getSituation() {
        return situation;
    }

    /**
     * Gets the total number of pages in the book.
     *
     * @return the page count as an integer, 0 if not set
     */
    public int getPageCount() {
        return pageCount;
    }

    /**
     * Gets the URL of the book cover image.
     *
     * @return the Firebase Storage URL of the book cover image, or null if no image is set
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Gets the Firestore document ID for this book record.
     *
     * @return the unique document identifier from Firestore, or null if not set
     */
    public String getDocId() {
        return docId;
    }

    /**
     * Sets the name/title of the book.
     *
     * @param name the book's title to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the author of the book.
     *
     * @param author the book's author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Sets the genre/category of the book.
     *
     * @param genre the book's genre to set. Should be one of the predefined values:
     *              Fantasy, Mystery, Horror, Romance, Young Adult, or Others
     */
    public void setGenre(String genre) {
        this.genre = genre;
    }

    /**
     * Sets the current reading status/situation of the book.
     *
     * @param situation the reading status to set. Should be one of the predefined values:
     *                  Read, Currently Reading, Stopped Reading, or Want to Read
     */
    public void setSituation(String situation) {
        this.situation = situation;
    }

    /**
     * Sets the total number of pages in the book.
     *
     * @param pageCount the total page count to set. Should be a positive integer
     */
    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    /**
     * Sets the URL of the book cover image.
     *
     * @param imageUrl the Firebase Storage URL of the book cover image to set
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Sets the Firestore document ID for this book record.
     *
     * @param docId the unique document identifier from Firestore to set
     */
    public void setDocId(String docId) {
        this.docId = docId;
    }

    /**
     * Returns a string representation of the book object.
     * This method is commonly used for display purposes in UI components
     * such as lists, spinners, and adapters.
     *
     * @return the book's name/title, or null if the name is not set
     */
    @Override
    public String toString() {
        return name;
    }
}