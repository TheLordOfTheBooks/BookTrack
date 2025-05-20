package com.example.booktrack;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditBook extends AppCompatActivity {

    private EditText nameInput, authorInput, pageCountInput;
    private Spinner genreSpinner, stateSpinner;
    private ImageView coverImage;
    private Button saveButton;
    private Button changeImageButton;
    private View editBook_view;
    FloatingActionButton arrow;

    private String bookId;
    private final String[] genres = {"Fantasy", "Mystery", "Horror", "Romance", "Young Adult", "Others"};
    private final String[] states = {"Read", "Currently Reading", "Stopped Reading", "Want to Read"};

    private static final int REQUEST_IMAGE_PICK = 100;
    private static final int REQUEST_CAMERA = 101;
    private static final int REQUEST_PERMISSION = 200;

    private Uri imageUri;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Intent> takePhotoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_book);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nameInput = findViewById(R.id.edit_book_title);
        authorInput = findViewById(R.id.edit_author);
        pageCountInput = findViewById(R.id.edit_page_count);
        genreSpinner = findViewById(R.id.edit_genre);
        stateSpinner = findViewById(R.id.edit_state);
        coverImage = findViewById(R.id.edit_cover);
        editBook_view = findViewById(R.id.main);
        saveButton = findViewById(R.id.save_book_button);
        changeImageButton = findViewById(R.id.change_image_button);
        arrow = findViewById(R.id.arrow);

        arrow.setOnClickListener(v -> finish());
        coverImage.setOnClickListener(v -> {
            Toast.makeText(this, "Image button clicked", Toast.LENGTH_SHORT).show();
            checkPermissions();
        });
        saveButton.setOnClickListener(v -> saveUpdatedBook());
        changeImageButton.setOnClickListener(v -> checkPermissions());

        changeImageButton.setBackgroundColor(Color.parseColor("#FAF0E6"));
        changeImageButton.setTextColor(Color.BLACK);
        saveButton.setBackgroundColor(Color.parseColor("#FAF0E6"));
        saveButton.setTextColor(Color.BLACK);
        editBook_view.setBackgroundColor(Color.parseColor("#eed9c4"));
        coverImage.setBackgroundColor(Color.parseColor("#eed9c4"));

        genreSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genres));
        stateSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, states));

        ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                genres
        );
        genreAdapter.setDropDownViewResource(R.layout.spinner_items);
        genreSpinner.setAdapter(genreAdapter);

        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                states
        );
        stateAdapter.setDropDownViewResource(R.layout.spinner_items);
        stateSpinner.setAdapter(stateAdapter);

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d("USER_CHECK", "Logged in UID: " + user.getUid());
        } else {
            Log.e("USER_CHECK", "User is NULL");
        }
        if (user == null) {
            Log.e("FIREBASE", "User is null even though they should be logged in");
            return;
        }

        if (TextUtils.isEmpty(bookId)) {
            Toast.makeText(this, "Book ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        nameInput.setText(intent.getStringExtra("name"));
        authorInput.setText(intent.getStringExtra("author"));
        pageCountInput.setText(String.valueOf(intent.getIntExtra("pageCount", 0)));

        String genre = intent.getStringExtra("genre");
        String state = intent.getStringExtra("situation");
        String imageUrl = intent.getStringExtra("imageUrl");

        if (genre != null) genreSpinner.setSelection(getSpinnerIndex(genreSpinner, genre));
        if (state != null) stateSpinner.setSelection(getSpinnerIndex(stateSpinner, state));
        if (imageUrl != null && !imageUrl.isEmpty()) Glide.with(this).load(imageUrl).into(coverImage);

        saveButton.setOnClickListener(v -> saveUpdatedBook());

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        Glide.with(this).load(imageUri).into(coverImage);
                    }
                });

        takePhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                        if (photo != null) {
                            String path = MediaStore.Images.Media.insertImage(getContentResolver(), photo, "title", null);
                            imageUri = Uri.parse(path);
                            Glide.with(this).load(imageUri).into(coverImage);
                        }
                    }
                });
    }

    private void checkPermissions() {
        if ((android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) ||
                (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU ?
                            new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA} :
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    REQUEST_PERMISSION);
        } else {
            openImagePicker();
        }
    }

    private void openImagePicker() {
        String[] options = {"Choose from Gallery", "Take a Photo"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Book Cover");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                Intent pickIntent = new Intent(Intent.ACTION_PICK);
                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                pickImageLauncher.launch(pickIntent);
            } else if (which == 1) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    takePhotoLauncher.launch(takePictureIntent);
                }
            }
        });
        builder.show();
    }



    private int getSpinnerIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0;
    }

    private void saveUpdatedBook() {
        String name = nameInput.getText().toString().trim();
        String author = authorInput.getText().toString().trim();
        String genre = genreSpinner.getSelectedItem().toString();
        String state = stateSpinner.getSelectedItem().toString();
        String pageCountText = pageCountInput.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(pageCountText)) {
            Toast.makeText(this, "* Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int pageCount;
        try {
            pageCount = Integer.parseInt(pageCountText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Page count must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = user.getUid();

        DocumentReference docRef = db.collection("users")
                .document(user.getUid())
                .collection("books")
                .document(bookId);



        Map<String, Object> updatedBook = new HashMap<>();
        updatedBook.put("name", name);
        updatedBook.put("author", author);
        updatedBook.put("genre", genre);
        updatedBook.put("situation", state);
        updatedBook.put("pageCount", pageCount);

        if (imageUri != null) {
            uploadImageAndUpdateBook(docRef, updatedBook);
        } else {
            docRef.update(updatedBook)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Book updated!", Toast.LENGTH_SHORT).show();
                        finish(); // go back
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show());
        }
    }

    private void uploadImageAndUpdateBook(DocumentReference docRef, Map<String, Object> updatedBook) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference ref = storage.getReference().child("book_images/" + UUID.randomUUID());

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            updatedBook.put("imageUrl", uri.toString());
                            docRef.update(updatedBook)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(this, "Book updated!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    });
                        })
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show());
    }
}