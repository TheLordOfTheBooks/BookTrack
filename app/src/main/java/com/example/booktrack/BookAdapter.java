package com.example.booktrack;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

/**
 * RecyclerView adapter for displaying a list of books in the BookTrack application.
 * This adapter handles the binding of book data to view holders, manages user interactions
 * such as viewing book details and editing, and provides functionality for deleting books
 * from both Firestore and Firebase Storage.
 *
 * <p>Key features include:
 * <ul>
 *   <li>Display of book information in a RecyclerView</li>
 *   <li>Book cover image loading using Glide</li>
 *   <li>Long-press to view detailed book information</li>
 *   <li>Click to edit book details</li>
 *   <li>Book deletion with confirmation dialog</li>
 *   <li>Firebase Firestore and Storage integration</li>
 *   <li>Image URI handling for previews</li>
 * </ul>
 *
 * <p>User Interactions:
 * <ul>
 *   <li><b>Single tap:</b> Opens EditBook activity with book details</li>
 *   <li><b>Long press:</b> Shows detailed book information dialog with delete option</li>
 * </ul>
 *
 * @author BookTrack Development Team
 * @version 1.0
 * @since 1.0
 */
public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    /** List of books to be displayed in the RecyclerView */
    private final List<Book> bookList;

    /** Context reference for accessing resources and starting activities */
    private final Context context;

    /** URI of the last selected image for preview purposes */
    private Uri lastSelectedImageUri = null;

    /** ImageView reference for displaying image previews */
    public ImageView imagePreview = null;

    /**
     * Constructs a new BookAdapter with the specified context and book list.
     *
     * @param context  the context in which the adapter will be used, typically an Activity
     * @param bookList the list of Book objects to display in the RecyclerView
     */
    public BookAdapter(Context context, List<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
    }

    /**
     * Creates a new ViewHolder by inflating the book item layout.
     * This method is called when the RecyclerView needs a new ViewHolder to represent an item.
     *
     * @param parent   the ViewGroup into which the new View will be added after it is bound to an adapter position
     * @param viewType the view type of the new View (not used in this implementation)
     * @return a new BookViewHolder that holds a View of the given view type
     */
    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.book_item, parent, false);
        return new BookViewHolder(view);
    }

    /**
     * Binds the book data to the ViewHolder at the specified position.
     * This method sets up the book information display, image loading, and click listeners
     * for both single tap (edit) and long press (details) interactions.
     *
     * @param holder   the ViewHolder which should be updated to represent the contents of the item
     * @param position the position of the item within the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.bookName.setText(book.getName());
        holder.bookAuthor.setText(book.getAuthor());

        // Load book cover image using Glide
        if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(book.getImageUrl())
                    .placeholder(android.R.color.white)
                    .error(android.R.color.white)
                    .into(holder.bookImage);
        } else {
            holder.bookImage.setImageDrawable(null);
            holder.bookImage.setBackgroundColor(Color.parseColor("#c3b091"));
        }

        // Set long click listener for showing book details dialog
        holder.itemView.setOnLongClickListener(v -> {
            showBookDetailsDialog(book);
            return true;
        });

        // Set click listener for editing book
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditBook.class);
            intent.putExtra("bookId", book.getDocId()); // assuming you store Firestore doc ID
            intent.putExtra("name", book.getName());
            intent.putExtra("author", book.getAuthor());
            intent.putExtra("genre", book.getGenre());
            intent.putExtra("situation", book.getSituation());
            intent.putExtra("pageCount", book.getPageCount());
            intent.putExtra("imageUrl", book.getImageUrl());
            context.startActivity(intent);
        });
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return the total number of books in the book list
     */
    @Override
    public int getItemCount() {
        return bookList.size();
    }

    /**
     * ViewHolder class for book items in the RecyclerView.
     * Holds references to the UI components for each book item to avoid repeated findViewById calls.
     * This inner class is static to prevent memory leaks by not holding implicit references
     * to the outer adapter class.
     */
    static class BookViewHolder extends RecyclerView.ViewHolder {

        /** ImageView for displaying the book cover */
        ImageView bookImage;

        /** TextView for displaying the book name/title */
        TextView bookName;

        /** TextView for displaying the book author */
        TextView bookAuthor;

        /**
         * Constructs a BookViewHolder and initializes the view references.
         *
         * @param itemView the root view of the book item layout
         */
        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            bookImage = itemView.findViewById(R.id.book_image);
            bookName = itemView.findViewById(R.id.book_name);
            bookAuthor = itemView.findViewById(R.id.book_author);
        }
    }

    /**
     * Displays a detailed information dialog for the specified book.
     * The dialog shows comprehensive book information including cover image, name, author,
     * genre, reading status, and page count. It also provides options to close the dialog
     * or delete the book with confirmation.
     *
     * @param book the Book object whose details should be displayed in the dialog
     */
    private void showBookDetailsDialog(Book book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_book_details, null);
        builder.setView(dialogView);

        // Initialize dialog UI components
        ImageView imageView = dialogView.findViewById(R.id.dialog_book_image);
        TextView nameText = dialogView.findViewById(R.id.dialog_book_name);
        TextView authorText = dialogView.findViewById(R.id.dialog_book_author);
        TextView genreText = dialogView.findViewById(R.id.dialog_book_genre);
        TextView situationText = dialogView.findViewById(R.id.dialog_book_situation);
        TextView pageCountText = dialogView.findViewById(R.id.dialog_book_pagecount);
        Button closeBtn = dialogView.findViewById(R.id.dialog_close_button);
        Button deleteBtn = dialogView.findViewById(R.id.dialog_delete_button);

        // Populate dialog with book information
        nameText.setText(book.getName());
        String author = book.getAuthor();
        authorText.setText("Author: " + (author == null || author.trim().isEmpty() ? "Unknown" : author));
        genreText.setText("Genre: " + book.getGenre());
        situationText.setText("Status: " + book.getSituation());
        pageCountText.setText("Pages: " + book.getPageCount());

        // Load book cover image
        if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
            Glide.with(context).load(book.getImageUrl()).into(imageView);
        } else {
            imageView.setImageDrawable(null);
            imageView.setBackgroundColor(Color.parseColor("#eed9c4"));
        }

        AlertDialog dialog = builder.create();

        // Set up close button
        closeBtn.setOnClickListener(v -> dialog.dismiss());

        // Set up delete button with confirmation
        deleteBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Book")
                    .setMessage("Are you sure you want to delete this book?")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        deleteBookFromFirestore(book, dialog);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        dialog.show();
    }

    /**
     * Deletes a book from Firebase Firestore and associated image from Firebase Storage.
     * This method performs a query to find the book document by name and author,
     * then deletes both the document and the associated cover image if it exists.
     * Updates the adapter's data set and UI upon successful deletion.
     *
     * @param book   the Book object to be deleted
     * @param dialog the AlertDialog to dismiss after deletion
     */
    private void deleteBookFromFirestore(Book book, AlertDialog dialog) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        db.collection("users")
                .document(user.getUid())
                .collection("books")
                .whereEqualTo("name", book.getName())
                .whereEqualTo("author", book.getAuthor())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete();
                        bookList.remove(book);
                        notifyDataSetChanged();

                        // Delete associated image from Firebase Storage
                        if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
                            StorageReference photoRef = storage.getReferenceFromUrl(book.getImageUrl());
                            photoRef.delete()
                                    .addOnSuccessListener(unused ->
                                            Toast.makeText(context, "Book deleted", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e ->
                                            Toast.makeText(context, "Image deletion failed", Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(context, "Book deleted", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed to delete book", Toast.LENGTH_SHORT).show());

        dialog.dismiss();
    }

    /**
     * Sets the URI of the selected image for preview purposes.
     * This method is used to handle image selection and preview functionality,
     * automatically loading the image into the preview ImageView if it's available.
     *
     * @param uri the URI of the selected image to set and preview
     */
    public void setSelectedImageUri(Uri uri) {
        Log.d("IMAGE_URI", "Image URI set: " + uri);
        this.lastSelectedImageUri = uri;
        if (imagePreview != null && uri != null) {
            Glide.with(context).load(uri).into(imagePreview);
        }
    }
}