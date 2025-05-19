package com.example.booktrack;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddMyBook extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 100;
    private static final int REQUEST_CAMERA = 101;
    private static final int REQUEST_PERMISSION = 200;

    private EditText bookNameInput, authorNameInput, pageCountInput;
    private Spinner genreSpinner, stateSpinner;
    private ImageView coverPreview;
    private Button chooseImageBtn, addBookBtn;
    private Uri imageUri;
    private String imageUrl;
    private View addBook_view;
    FloatingActionButton arrow;

    private final String[] genres = {"Fantasy", "Mystery", "Horror", "Romance", "Young Adult", "Others"};
    private final String[] states = {"Read", "Currently Reading", "Stopped Reading", "Want to Read"};

    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Intent> takePhotoLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_my_book);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bookNameInput = findViewById(R.id.book_name);
        authorNameInput = findViewById(R.id.author_name);
        pageCountInput = findViewById(R.id.page_count);
        genreSpinner = findViewById(R.id.genre_spinner);
        stateSpinner = findViewById(R.id.state_spinner);
        coverPreview = findViewById(R.id.cover_preview);
        chooseImageBtn = findViewById(R.id.choose_image_btn);
        addBookBtn = findViewById(R.id.add_book_btn);
        addBook_view = findViewById(R.id.main);
        arrow = findViewById(R.id.arrow);

        chooseImageBtn.setBackgroundColor(Color.parseColor("#FAF0E6"));
        chooseImageBtn.setTextColor(Color.BLACK);
        addBookBtn.setBackgroundColor(Color.parseColor("#FAF0E6"));
        addBookBtn.setTextColor(Color.BLACK);
        addBook_view.setBackgroundColor(Color.parseColor("#eed9c4"));
        coverPreview.setBackgroundColor(Color.parseColor("#eed9c4"));

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
        chooseImageBtn.setOnClickListener(v -> checkPermissions());
        addBookBtn.setOnClickListener(v -> validateAndSubmit());
        arrow.setOnClickListener(v -> finish());

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            imageUri = selectedImageUri;
                            coverPreview.setVisibility(View.VISIBLE);
                            Glide.with(this).load(imageUri).into(coverPreview);
                        }
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
                            coverPreview.setVisibility(View.VISIBLE);
                            Glide.with(this).load(imageUri).into(coverPreview);
                        }
                    }
                });
    }

    private void checkPermissions() {
        if ((android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
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
                Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            coverPreview.setVisibility(View.VISIBLE);
            Glide.with(this).load(imageUri).into(coverPreview);
        } else if (requestCode == REQUEST_CAMERA && resultCode == Activity.RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                Uri imageUri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), imageBitmap, "title", null));
                coverPreview.setVisibility(View.VISIBLE);
                Glide.with(this).load(imageUri).into(coverPreview);
            }
        }
    }

    private void validateAndSubmit () {
        String name = bookNameInput.getText().toString().trim();
        String author = authorNameInput.getText().toString().trim();
        String genre = genreSpinner.getSelectedItem().toString();
        String state = stateSpinner.getSelectedItem().toString();
        String pages = pageCountInput.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(pages)) {
            Toast.makeText(this, "* Please fill all required fields (marked with asterisk)", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadImageAndSaveBook(name, author, genre, state, pages);
    }

    private void uploadImageAndSaveBook (String name, String author, String genre, String state, String pages){
        if (imageUri == null) {
            imageUrl = null;
            saveBookToFirestore(name, author, genre, state, pages);
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference ref = storage.getReference().child("book_images/" + UUID.randomUUID());

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            imageUrl = uri.toString();
                            saveBookToFirestore(name, author, genre, state, pages);
                        })
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                );
    }

    private void saveBookToFirestore (String name, String author, String genre, String state, String pages){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> book = new HashMap<>();
        book.put("name", name);
        book.put("author", author);
        book.put("genre", genre);
        book.put("situation", state);
        book.put("pageCount", Integer.parseInt(pages));
        if (imageUrl != null) {
            book.put("imageUrl", imageUrl);
        }

        db.collection("users")
                .document(uid)
                .collection("books")
                .add(book)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Book added successfully!", Toast.LENGTH_SHORT).show();
                    Intent si = new Intent();
                    setResult(Activity.RESULT_OK, si);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save book", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION) {
            boolean granted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }

            if (granted) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission denied. Can't open camera or gallery.", Toast.LENGTH_LONG).show();
            }
        }
    }

}