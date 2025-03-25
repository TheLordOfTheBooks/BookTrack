/*package com.example.booktrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booktrack.R;
import com.example.booktrack.models.Book;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private Context context;
    private List<Book> bookList;
    private OnBookClickListener listener;

    // Interface for item click
    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    public BookAdapter(Context context, List<Book> bookList, OnBookClickListener listener) {
        this.context = context;
        this.bookList = bookList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);

        holder.titleText.setText(book.getTitle());
        holder.ratingText.setText("Rating: " + (book.getRating() == 0 ? "N/A" : book.getRating()));

        // Load image with Picasso
        if (!book.getThumbnail().isEmpty()) {
            Picasso.get().load(book.getThumbnail()).into(holder.coverImage);
        } else {
            holder.coverImage.setImageResource(R.drawable.book_placeholder);
        }

        holder.itemView.setOnClickListener(v -> listener.onBookClick(book));
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    // ViewHolder class
    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView coverImage;
        TextView titleText, ratingText;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            coverImage = itemView.findViewById(R.id.book_cover);
            titleText = itemView.findViewById(R.id.book_title);
        }
    }

}*/
