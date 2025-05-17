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

                        dialog.dismiss();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });




        dialog.show();
    }

    private Uri lastSelectedImageUri = null;
    public ImageView imagePreview = null;

    public void setSelectedImageUri(Uri uri) {
        Log.d("IMAGE_URI", "Image URI set: " + uri);
        this.lastSelectedImageUri = uri;
        if (imagePreview != null && uri != null) {
            Glide.with(context).load(uri).into(imagePreview);
        }
    }



}

