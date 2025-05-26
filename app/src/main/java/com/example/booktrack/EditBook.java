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

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Activity for editing existing book entries in the BookTrack application.
 * This activity provides a comprehensive interface for modifying book information
 * including title, author, page count, genre, reading status, and cover image.
 *
 * The activity supports both gallery image selection and camera capture for
 * updating book cover images, with proper permission handling for Android 13+.
 * All changes are synchronized with Firebase Firestore and Firebase Storage.
 *
 * @author BookTrack Development Team
 * @version 1.0
 * @since 1.0
 */
public class EditBook extends AppCompatActivity {

    /**
     * EditText field for entering the book title.
     */
    private EditText nameInput;

    /**
     * EditText field for entering the book author's name.
     */
    private EditText authorInput;

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
     * ImageView for displaying and allowing selection of the book cover image.
     */
    private ImageView coverImage;

    /**
     * Button for saving the edited book information to Firebase.
     */
    private Button saveButton;

    /**
     * Button for triggering the image selection/capture process.
     */
    private Button changeImageButton;

    /**
     * Main view container for applying background styling.
     */
    private View editBook_view;

    /**
     * Floating action button for navigating back to the previous screen.
     */
    FloatingActionButton arrow;

    /**
     * Unique identifier for the book being edited, retrieved from the intent.
     */
    private String bookId;

    /**
     * Array of available book genres for the spinner selection.
     */
    private final String[] genres = {"Fantasy", "Mystery", "Horror", "Romance", "Young Adult", "Others"};

    /**
     * Array of available reading states for the spinner selection.
     */
    private final String[] states = {"Read", "Currently Reading", "Stopped Reading", "Want to Read"};

    /**
     * Request code used for permission requests related to camera and storage access.
     */
    private static final int REQUEST_PERMISSION = 200;

    /**
     * URI of the selected image from the device gallery.
     */
    private Uri imageUri;

    /**
     * Bitmap of the captured image from the camera.
     */
    private Bitmap capturedBitmap; // Store the captured bitmap

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
     * establishes click listeners, loads existing book data, and prepares image launchers.
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
        setContentView(R.layout.activity_edit_book);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupUI();
        setupSpinners();
        setupListeners();
        loadBookData();
        setupImageLaunchers();
    }

    /**
     * Initializes all UI components by finding their corresponding view references.
     * This method sets up the connection between the Java code and the XML layout elements.
     */
    private void initializeViews() {
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
    }

    /**
     * Configures the visual styling of UI components with a consistent color scheme.
     * Applies beige background (#FAF0E6) to buttons and cream background (#eed9c4)
     * to the main view and cover image placeholder.
     */
    private void setupUI() {
        changeImageButton.setBackgroundColor(Color.parseColor("#FAF0E6"));
        changeImageButton.setTextColor(Color.BLACK);
        saveButton.setBackgroundColor(Color.parseColor("#FAF0E6"));
        saveButton.setTextColor(Color.BLACK);
        editBook_view.setBackgroundColor(Color.parseColor("#eed9c4"));
        coverImage.setBackgroundColor(Color.parseColor("#eed9c4"));
    }

    /**
     * Configures the genre and state spinners with their respective data arrays.
     * Sets up ArrayAdapters for both spinners and applies custom dropdown styling.
     */
    private void setupSpinners() {
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
    }

    /**
     * Establishes click listeners for interactive UI elements including the back arrow,
     * cover image selection, save functionality, and image change button.
     */
    private void setupListeners() {
        arrow.setOnClickListener(v -> finish());
        coverImage.setOnClickListener(v -> checkPermissions());
        saveButton.setOnClickListener(v -> saveUpdatedBook());
        changeImageButton.setOnClickListener(v -> checkPermissions());
    }

    /**
     * Loads existing book data from the intent extras and populates the UI fields.
     * Retrieves book information including title, author, page count, genre, reading state,
     * and cover image URL. Validates user authentication and book ID before proceeding.
     * Uses Glide library to load the existing cover image if available.
     */
    private void loadBookData() {
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e("FIREBASE", "User is null");
            finish();
            return;
        }

        if (TextUtils.isEmpty(bookId)) {
            Toast.makeText(this, "Book ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load existing book data
        nameInput.setText(intent.getStringExtra("name"));
        authorInput.setText(intent.getStringExtra("author"));
        pageCountInput.setText(String.valueOf(intent.getIntExtra("pageCount", 0)));

        String genre = intent.getStringExtra("genre");
        String state = intent.getStringExtra("situation");
        String imageUrl = intent.getStringExtra("imageUrl");

        if (genre != null) genreSpinner.setSelection(getSpinnerIndex(genreSpinner, genre));
        if (state != null) stateSpinner.setSelection(getSpinnerIndex(stateSpinner, state));
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(coverImage);
        }
    }

    /**
     * Configures activity result launchers for handling image selection from gallery
     * and photo capture from camera. Sets up callbacks to process the selected or
     * captured images and update the cover image view accordingly.
     */
    private void setupImageLaunchers() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        capturedBitmap = null; // Clear any previous bitmap
                        if (imageUri != null) {
                            Glide.with(this).load(imageUri).into(coverImage);
                        }
                    }
                });

        takePhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            Bitmap photo = (Bitmap) extras.get("data");
                            if (photo != null) {
                                capturedBitmap = photo;
                                imageUri = null; // Clear any previous URI
                                coverImage.setImageBitmap(photo);
                            }
                        }
                    }
                });
    }

    /**
     * Checks and requests necessary permissions for camera and storage access.
     * Handles different permission requirements for Android 13+ (READ_MEDIA_IMAGES)
     * versus older versions (READ_EXTERNAL_STORAGE). If all permissions are granted,
     * opens the image picker dialog.
     */
    private void checkPermissions() {
        String[] permissions;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA};
        } else {
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        }

        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (!allGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION);
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

    /**
     * Finds the index of a specific value within a spinner's items.
     * Used to set the correct selection for genre and state spinners when
     * loading existing book data.
     *
     * @param spinner The spinner to search within
     * @param value The value to find in the spinner's items
     * @return The index of the matching item, or 0 if not found
     */
    private int getSpinnerIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Validates and saves the updated book information to Firebase Firestore.
     * Performs input validation for required fields and numeric page count.
     * If a new image is selected, uploads it to Firebase Storage before updating
     * the document. Otherwise, updates only the text-based book information.
     */
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

        // Check if we have a new image (either from gallery or camera)
        if (imageUri != null || capturedBitmap != null) {
            uploadImageAndUpdateBook(docRef, updatedBook);
        } else {
            // No new image, just update the book data
            docRef.update(updatedBook)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Book updated!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Handles the image upload process to Firebase Storage and subsequent book document update.
     * Determines whether to upload a bitmap from camera capture or a URI from gallery selection,
     * then calls the appropriate upload method.
     *
     * @param docRef Reference to the Firestore document to be updated
     * @param updatedBook Map containing the updated book information
     */
    private void uploadImageAndUpdateBook(DocumentReference docRef, Map<String, Object> updatedBook) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference ref = storage.getReference().child("book_images/" + UUID.randomUUID());

        if (capturedBitmap != null) {
            // Upload bitmap from camera
            uploadBitmapToFirebase(ref, capturedBitmap, docRef, updatedBook);
        } else if (imageUri != null) {
            // Upload URI from gallery
            ref.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                                updatedBook.put("imageUrl", uri.toString());
                                updateBookDocument(docRef, updatedBook);
                            })
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Uploads a bitmap image to Firebase Storage by converting it to a compressed JPEG byte array.
     * Upon successful upload, retrieves the download URL and updates the book document.
     *
     * @param ref Storage reference where the image will be uploaded
     * @param bitmap The bitmap image to upload
     * @param docRef Reference to the Firestore document to be updated
     * @param updatedBook Map containing the updated book information
     */
    private void uploadBitmapToFirebase(StorageReference ref, Bitmap bitmap,
                                        DocumentReference docRef, Map<String, Object> updatedBook) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] data = baos.toByteArray();

        ref.putBytes(data)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            updatedBook.put("imageUrl", uri.toString());
                            updateBookDocument(docRef, updatedBook);
                        })
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show());
    }

    /**
     * Updates the book document in Firestore with the provided updated information.
     * Displays success or failure messages to the user and finishes the activity upon success.
     *
     * @param docRef Reference to the Firestore document to be updated
     * @param updatedBook Map containing all the updated book information including image URL
     */
    private void updateBookDocument(DocumentReference docRef, Map<String, Object> updatedBook) {
        docRef.update(updatedBook)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Book updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show());
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission denied. Can't access camera or gallery.", Toast.LENGTH_LONG).show();
            }
        }
    }
}