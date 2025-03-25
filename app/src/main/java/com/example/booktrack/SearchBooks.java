/*package com.example.booktrack;

import android.os.Bundle;

import android.content.Intent;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.booktrack.adapters.BookAdapter;
import com.example.booktrack.models.Book;

import java.util.ArrayList;

public class SearchBooks extends AppCompatActivity implements BookAdapter.OnBookClickListener {


    private EditText searchInput;
    private Button searchButton;
    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private ArrayList<Book> bookList;
    private static final String API_KEY = "AIzaSyBBWcrDwrOQXEw3q_BWQMKp1K1OE2O3pi8";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_books);

        searchInput = findViewById(R.id.search_input);
        searchButton = findViewById(R.id.search_button);
        recyclerView = findViewById(R.id.books_recycler_view);

        bookList = new ArrayList<>();
        bookAdapter = new BookAdapter(this, bookList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(bookAdapter);

        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                searchBooks(query);
            } else {
                Toast.makeText(this, "Please enter a book name", Toast.LENGTH_SHORT).show();
            }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

        private void searchBooks(String query) {
            bookList.clear();
            String url = "https://www.googleapis.com/books/v1/volumes?q=intitle:" + query +
                    "&orderBy=relevance&maxResults=20&key=" + API_KEY;

            RequestQueue queue = Volley.newRequestQueue(this);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            JSONArray items = response.getJSONArray("items");

                            for (int i = 0; i < items.length(); i++) {
                                JSONObject bookObj = items.getJSONObject(i).getJSONObject("volumeInfo");

                                String title = bookObj.optString("title", "No Title");
                                String thumbnail = "";
                                double rating = bookObj.optDouble("averageRating", 0);

                                if (bookObj.has("imageLinks")) {
                                    thumbnail = bookObj.getJSONObject("imageLinks").optString("thumbnail", "");
                                }

                                Book book = new Book(title, thumbnail, rating);
                                bookList.add(book);
                            }

                            bookAdapter.notifyDataSetChanged();

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(this, "Failed to load books", Toast.LENGTH_SHORT).show()
            );

            queue.add(request);
        }

        @Override
        public void onBookClick(Book book) {
            // Open book info screen and send data
            Intent intent = new Intent(this, BookInfo.class);
            intent.putExtra("title", book.getTitle());
            intent.putExtra("thumbnail", book.getThumbnail());
            startActivity(intent);
        }
}*/