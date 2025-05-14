package com.example.booktrack;

import android.graphics.Color;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import android.util.Log;
import com.bumptech.glide.Glide;
import androidx.activity.result.ActivityResultLauncher;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.Manifest;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;


import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResult;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.booktrack.Book;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public class BookListFragment extends Fragment {


    private static final String ARG_SITUATION = "situation";
    private String situation;
    private List<Book> bookList;
    private BookAdapter adapter;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;


    public static BookListFragment newInstance(String situation) {
        BookListFragment fragment = new BookListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SITUATION, situation);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_book_list, container, false);
        return view;



    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        bookList = new ArrayList<>();
        adapter = new BookAdapter(requireContext(), bookList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        if (getArguments() != null) {
            situation = getArguments().getString(ARG_SITUATION);
            loadBooksBySituation(situation);
        }

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        if (adapter != null) adapter.setSelectedImageUri(selectedImage);
                    }
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            Bitmap photo = (Bitmap) extras.get("data");
                            String path = MediaStore.Images.Media.insertImage(requireContext().getContentResolver(), photo, "book_cover", null);
                            if (path != null) {
                                Uri imageUri = Uri.parse(path);
                                if (adapter != null) adapter.setSelectedImageUri(imageUri);
                            }
                        }
                    }
                });



    }

    private void loadBooksBySituation(String situation) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(user.getUid())
                .collection("books")
                .whereEqualTo("situation", situation)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null) {
                        bookList.clear();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Book book = doc.toObject(Book.class);
                            if (book != null) {
                                book.setDocId(doc.getId()); // save Firestore doc ID
                                bookList.add(book);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }









}