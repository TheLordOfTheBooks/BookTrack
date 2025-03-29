package com.example.booktrack;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.Context;
import android.widget.Button;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Build;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.Log;
import com.bumptech.glide.Glide;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.util.List;

import androidx.core.app.ActivityCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Arrays;


public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private final List<Book> bookList;
    private final Context context;



    public BookAdapter(Context context, List<Book> bookList) {
        this.context = context;
        this.bookList = bookList;


    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.book_item, parent, false);
        return new BookViewHolder(view);


    }



    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.bookName.setText(book.getName());
        holder.bookAuthor.setText(book.getAuthor());



        if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(book.getImageUrl())
                    .placeholder(android.R.color.white)
                    .error(android.R.color.white)
                    .into(holder.bookImage);
        } else {
            holder.bookImage.setImageDrawable(null); // clear image
            holder.bookImage.setBackgroundColor(
                    holder.itemView.getResources().getColor(android.R.color.white)
            );
        }

        holder.itemView.setOnLongClickListener(v -> {
            showBookDetailsDialog(book);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView bookImage;
        TextView bookName, bookAuthor;


        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            bookImage = itemView.findViewById(R.id.book_image);
            bookName = itemView.findViewById(R.id.book_name);
            bookAuthor = itemView.findViewById(R.id.book_author);
        }
    }


    private void showBookDetailsDialog(Book book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_book_details, null);
        builder.setView(dialogView);

        ImageView imageView = dialogView.findViewById(R.id.dialog_book_image);
        TextView nameText = dialogView.findViewById(R.id.dialog_book_name);
        TextView authorText = dialogView.findViewById(R.id.dialog_book_author);
        TextView genreText = dialogView.findViewById(R.id.dialog_book_genre);
        TextView situationText = dialogView.findViewById(R.id.dialog_book_situation);
        TextView pageCountText = dialogView.findViewById(R.id.dialog_book_pagecount);
        Button closeBtn = dialogView.findViewById(R.id.dialog_close_button);
        Button deleteBtn = dialogView.findViewById(R.id.dialog_delete_button);
        Button editBtn = dialogView.findViewById(R.id.dialog_edit_button);

        nameText.setText(book.getName());
        String author = book.getAuthor();
        authorText.setText("Author: " + (author == null || author.trim().isEmpty() ? "Unknown" : author));
        genreText.setText("Genre: " + book.getGenre());
        situationText.setText("Status: " + book.getSituation());
        pageCountText.setText("Pages: " + book.getPageCount());

        if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
            Glide.with(context).load(book.getImageUrl()).into(imageView);
        } else {
            imageView.setImageDrawable(null);
            imageView.setBackgroundColor(context.getResources().getColor(android.R.color.white));
        }

        AlertDialog dialog = builder.create();

        closeBtn.setOnClickListener(v -> dialog.dismiss());

        deleteBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Book")
                    .setMessage("Are you sure you want to delete this book?")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user == null) return;

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        FirebaseStorage storage = FirebaseStorage.getInstance();

                        // Step 1: Delete Firestore book
                        db.collection("users")
                                .document(user.getUid())
                                .collection("books")
                                .whereEqualTo("name", book.getName())
                                .whereEqualTo("author", book.getAuthor())
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                        doc.getReference().delete(); // delete doc
                                        bookList.remove(book); // remove from local list
                                        notifyDataSetChanged(); // refresh UI

                                        // Step 2: Delete from Storage if image exists
                                        if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
                                            StorageReference photoRef = storage.getReferenceFromUrl(book.getImageUrl());
                                            photoRef.delete()
                                                    .addOnSuccessListener(unused -> Toast.makeText(context, "Book deleted", Toast.LENGTH_SHORT).show())
                                                    .addOnFailureListener(e -> Toast.makeText(context, "Image deletion failed", Toast.LENGTH_SHORT).show());
                                        } else {
                                            Toast.makeText(context, "Book deleted", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete book", Toast.LENGTH_SHORT).show());

                        dialog.dismiss(); // close book dialog
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        editBtn.setOnClickListener(v -> {
            showEditBookDialog(book);
            dialog.dismiss(); // Close view dialog before opening edit

        });


        dialog.show();
    }

    private Uri lastSelectedImageUri = null;
    public ImageView imagePreview = null;




    private void showEditBookDialog(Book book) {
        Log.d("DIALOG", "Dialog adapter instance: " + this.hashCode());
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View editView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_book, null);
        builder.setView(editView);

        EditText nameInput = editView.findViewById(R.id.edit_book_name);
        EditText authorInput = editView.findViewById(R.id.edit_book_author);
        EditText pageCountInput = editView.findViewById(R.id.edit_book_pagecount);
        Spinner genreSpinner = editView.findViewById(R.id.edit_book_genre_spinner);
        Spinner situationSpinner = editView.findViewById(R.id.edit_book_situation_spinner);
        ImageView imageView = editView.findViewById(R.id.edit_book_image);
        Button changeImageBtn = editView.findViewById(R.id.edit_choose_image_btn);
        Button saveBtn = editView.findViewById(R.id.edit_save_button);
        Button cancelBtn = editView.findViewById(R.id.edit_cancel_button);

        this.imagePreview = imageView;
        BookAdapter.this.imagePreview = imageView;

        nameInput.setText(book.getName());
        authorInput.setText(book.getAuthor());
        pageCountInput.setText(String.valueOf(book.getPageCount()));

        String[] genres = {"Fantasy", "Mystery", "Horror", "Romance", "Young Adult", "Others"};
        String[] situations = {"Read", "Currently Reading", "Stopped Reading", "Want to Read"};

        ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, genres);
        ArrayAdapter<String> situationAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, situations);
        genreSpinner.setAdapter(genreAdapter);
        situationSpinner.setAdapter(situationAdapter);

        genreSpinner.setSelection(Arrays.asList(genres).indexOf(book.getGenre()));
        situationSpinner.setSelection(Arrays.asList(situations).indexOf(book.getSituation()));

        if (lastSelectedImageUri != null) {
            Glide.with(context).load(lastSelectedImageUri).into(imageView);
        } else if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
            Glide.with(context).load(book.getImageUrl()).into(imageView);
        }

        changeImageBtn.setOnClickListener(v -> {
            String[] options = {"Choose from Gallery", "Take a Photo"};

            AlertDialog.Builder chooserBuilder = new AlertDialog.Builder(context);
            chooserBuilder.setTitle("Select Book Cover");
            chooserBuilder.setItems(options, (dialog, which) -> {
                if (which == 0) {
                    Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickIntent.setType("image/*");
                    imagePreview = imageView;
                    BookAdapter.this.imagePreview = imageView;

                } else if (which == 1) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA}, 3001);
                            return;
                        }
                    }
                    if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
                        imagePreview = imageView;
                        ((Activity) context).startActivityForResult(takePictureIntent, 2002);
                    }
                }
            });
            chooserBuilder.show();
        });


        AlertDialog editDialog = builder.create();

        cancelBtn.setOnClickListener(v -> editDialog.dismiss());

        saveBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Confirm Save")
                    .setMessage("Are you sure you want to save changes?")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        String newName = nameInput.getText().toString().trim();
                        String newAuthor = authorInput.getText().toString().trim();
                        String newGenre = genreSpinner.getSelectedItem().toString();
                        String newSituation = situationSpinner.getSelectedItem().toString();
                        int newPageCount = Integer.parseInt(pageCountInput.getText().toString().trim());

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user == null) return;

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("users")
                                .document(user.getUid())
                                .collection("books")
                                .whereEqualTo("name", book.getName())
                                .whereEqualTo("author", book.getAuthor())
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                        if (lastSelectedImageUri != null) {
                                            Log.d("IMAGE_UPLOAD", "Preparing to upload image: " + lastSelectedImageUri);

                                            FirebaseStorage storage = FirebaseStorage.getInstance();
                                            StorageReference ref = storage.getReference().child("book_images/" + UUID.randomUUID());

                                            ref.putFile(lastSelectedImageUri)
                                                    .addOnSuccessListener(taskSnapshot ->
                                                            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                                                                String newImageUrl = uri.toString();
                                                                Log.d("IMAGE_UPLOAD", "Upload successful. URL: " + newImageUrl);
                                                                saveBookToFirestore(doc, book, newName, newAuthor, newGenre, newSituation, newPageCount, newImageUrl);
                                                                editDialog.dismiss();
                                                            })
                                                    ).addOnFailureListener(e -> {
                                                        Log.e("IMAGE_UPLOAD", "Upload failed", e);
                                                        Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show();
                                                    });
                                        } else {
                                            Log.d("IMAGE_UPLOAD", "No image selected. Using previous image URL.");
                                            saveBookToFirestore(doc, book, newName, newAuthor, newGenre, newSituation, newPageCount, book.getImageUrl());
                                            editDialog.dismiss();
                                        }
                                    }
                                });
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        editDialog.show();
    }



    private void saveBookToFirestore(DocumentSnapshot doc, Book book, String name, String author,
                                     String genre, String situation, int pageCount, String imageUrl) {

        doc.getReference().update(
                "name", name,
                "author", author,
                "genre", genre,
                "situation", situation,
                "pageCount", pageCount,
                "imageUrl", imageUrl
        ).addOnSuccessListener(unused -> {
            Toast.makeText(context, "Book updated", Toast.LENGTH_SHORT).show();

            book.setName(name);
            book.setAuthor(author);
            book.setGenre(genre);
            book.setSituation(situation);
            book.setPageCount(pageCount);
            book.setImageUrl(imageUrl);

            notifyDataSetChanged();
        });
    }

    public void setSelectedImageUri(Uri uri) {
        Log.d("IMAGE_URI", "Image URI set: " + uri);
        this.lastSelectedImageUri = uri;
        if (imagePreview != null && uri != null) {
            Glide.with(context).load(uri).into(imagePreview);
        }
    }

    public interface ImagePickerCallback {
        void onRequestGallery();
        void onRequestCamera();
    }



}

