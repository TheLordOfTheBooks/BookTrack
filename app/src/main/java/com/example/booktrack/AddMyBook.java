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
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
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

/**
 * Activity for adding new books to the user's collection in the BookTrack application.
 * This activity provides a comprehensive form interface for entering book information
 * including title, author, page count, genre, reading status, and cover image.
 *
 * The activity supports both gallery image selection and camera capture for
 * book cover images, with proper permission handling for Android 13+.
 * All book data is saved to Firebase Firestore with optional image upload to Firebase Storage.
 *
 * @author BookTrack Development Team
 * @version 1.0
 * @since 1.0
 */
public class AddMyBook extends AppCompatActivity {

    /**
     * Request code for image picking from gallery (legacy implementation).
     */
    private static final int REQUEST_IMAGE_PICK = 100;

    /**
     * Request code for camera photo capture (legacy implementation).
     */
    private static final int REQUEST_CAMERA = 101;

    /**
     * Request code for permission requests related to camera and storage access.
     */
    private static final int REQUEST_PERMISSION = 200;

    /**
     * EditText field for entering the book title.
     */
    private EditText bookNameInput;

    /**
     * EditText field for entering the author's name.
     */
    private EditText authorNameInput;

    /**
     * EditText field for entering the total number of pages.
     */
    private EditText pageCountInput;

    /**
     * Spinner for selecting the book's genre from predefined options.
     */
    private Spinner genreSpinner;

    /**
     * Spinner for selecting the current reading status of the book.
     */
    private Spinner stateSpinner;

    /**
     * ImageView for displaying the selected or captured book cover image.
     */
    private ImageView coverPreview;

    /**
     * Button for triggering the image selection/capture process.
     */
    private Button chooseImageBtn;

    /**
     * Button for saving the new book information to Firebase.
     */
    private Button addBookBtn;

    /**
     * URI of the selected or captured image.
     */
    private Uri imageUri;

    /**
     * URL of the uploaded image stored in Firebase Storage.
     */
    private String imageUrl;

    /**
     * Main view container for applying background styling.
     */
    private View addBook_view;

    /**
     * Floating action button for navigating back to the previous screen.
     */
    FloatingActionButton arrow;

    /**
     * Array of available book genres for the spinner selection.
     */
    private final String[] genres = {"Fantasy", "Mystery", "Horror", "Romance", "Young Adult", "Others"};

    /**
     * Array of available reading states for the spinner selection.
     */
    private final String[] states = {"Read", "Currently Reading", "Stopped Reading", "Want to Read"};

    /**
     * Activity result launcher for handling image selection from gallery.
     */
    private ActivityResultLauncher<Intent> pickImageLauncher;

    /**
     * Activity result launcher for handling camera photo capture.
     */
    private ActivityResultLauncher<Intent> takePhotoLauncher;

    /**
     * Called when the activity is first created. Initializes the UI components,
     * sets up edge-to-edge display, configures spinners with genre and state options,
     * establishes click listeners, and prepares activity result launchers for image handling.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                          previously being shut down, this Bundle contains the data
     *                          it most recently supplied in onSaveInstanceState(Bundle).
     *                          Note: Otherwise it is null.
     */
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
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri selectedImageUri = result.getData().getData();
                            if (selectedImageUri != null) {
                                imageUri = selectedImageUri;
                                coverPreview.setVisibility(View.VISIBLE);
                                Glide.with(AddMyBook.this).load(imageUri).into(coverPreview);
                            }
                        }
                    }
                });

        takePhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                            if (photo != null) {
                                coverPreview.setVisibility(View.VISIBLE);
                                Glide.with(AddMyBook.this).load(photo).into(coverPreview);
                                imageUri = getImageUriFromBitmap(photo);
                            }
                        }
                    }
                });
    }

    /**
     * Converts a bitmap image to a URI by inserting it into the device's MediaStore.
     * This method is used to create a URI reference for camera-captured images.
     *
     * @param bitmap The bitmap image to convert to URI
     * @return URI pointing to the inserted image in MediaStore
     */
    private Uri getImageUriFromBitmap(Bitmap bitmap) {
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "CapturedImage", null);
        return Uri.parse(path);
    }

    /**
     * Checks and requests necessary permissions for camera and storage access.
     * Handles different permission requirements for Android 13+ (READ_MEDIA_IMAGES)
     * versus older versions (READ_EXTERNAL_STORAGE). If all permissions are granted,
     * opens the image picker dialog.
     */
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

    /**
     * Displays an AlertDialog allowing the user to choose between selecting an image
     * from the gallery or taking a new photo with the camera. Launches the appropriate
     * activity result launcher based on the user's selection.
     */
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

    /**
     * Legacy method for handling activity results from image picking and camera capture.
     * This method is maintained for backwards compatibility alongside the newer
     * ActivityResultLauncher implementations.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult()
     * @param resultCode The integer result code returned by the child activity
     * @param data An Intent which can return result data to the caller
     */
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

    /**
     * Validates user input for required fields and initiates the book submission process.
     * Checks that the book name and page count fields are not empty before proceeding
     * to upload the image and save the book data.
     */
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

    /**
     * Handles the image upload process to Firebase Storage before saving book data.
     * If no image is selected, proceeds directly to save the book without an image URL.
     * Otherwise, uploads the image to Firebase Storage and retrieves the download URL.
     *
     * @param name The book title
     * @param author The author's name
     * @param genre The selected genre
     * @param state The reading status
     * @param pages The page count as a string
     */
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

    /**
     * Saves the book information to Firebase Firestore under the authenticated user's collection.
     * Creates a new document in the user's books subcollection with all provided book data.
     * Upon successful save, displays a success message and finishes the activity.
     *
     * @param name The book title
     * @param author The author's name
     * @param genre The selected genre
     * @param state The reading status
     * @param pages The page count as a string (converted to integer)
     */
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

    /**
     * Handles the result of permission requests for camera and storage access.
     * If all permissions are granted, opens the image picker dialog.
     * Otherwise, displays an error message to the user.
     *
     * @param requestCode The request code passed to requestPermissions()
     * @param permissions The requested permissions
     * @param grantResults The grant results for the corresponding permissions
     */
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