package com.example.booktrack;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * BookListFragment displays a list of books filtered by reading situation/status.
 * This fragment integrates with Firebase Firestore to load and display books based on
 * their reading status (e.g., "Read", "Currently Reading", "Stopped Reading", "Want to Read").
 *
 * <p>The fragment also provides functionality for image selection from gallery or camera,
 * which can be used for adding book cover images through the BookAdapter.</p>
 *
 * <p>Key features include:</p>
 * <ul>
 *   <li>Dynamic book loading based on reading situation</li>
 *   <li>Firebase Firestore integration for data persistence</li>
 *   <li>Image selection capabilities (gallery and camera)</li>
 *   <li>RecyclerView with custom adapter for book display</li>
 * </ul>
 *
 * @author BookTrack Development Team
 * @version 1.0
 * @since 1.0
 */
public class BookListFragment extends Fragment {

    /**
     * Bundle argument key for passing the reading situation/status to the fragment.
     */
    private static final String ARG_SITUATION = "situation";

    /**
     * The current reading situation/status filter for displaying books.
     * Examples: "Read", "Currently Reading", "Stopped Reading", "Want to Read"
     */
    private String situation;

    /**
     * List containing Book objects to be displayed in the RecyclerView.
     */
    private List<Book> bookList;

    /**
     * Adapter for binding book data to the RecyclerView and handling user interactions.
     */
    private BookAdapter adapter;

    /**
     * Activity result launcher for handling gallery image selection.
     * Manages the intent result when user selects an image from the device gallery.
     */
    private ActivityResultLauncher<Intent> galleryLauncher;

    /**
     * Activity result launcher for handling camera image capture.
     * Manages the intent result when user captures a photo using the device camera.
     */
    private ActivityResultLauncher<Intent> cameraLauncher;

    /**
     * Factory method to create a new instance of BookListFragment with the specified reading situation.
     * This method follows the recommended pattern for fragment instantiation with arguments.
     *
     * @param situation The reading situation/status to filter books by
     *                  (e.g., "Read", "Currently Reading", "Stopped Reading", "Want to Read")
     * @return A new instance of BookListFragment configured with the specified situation
     */
    public static BookListFragment newInstance(String situation) {
        BookListFragment fragment = new BookListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SITUATION, situation);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called to create the view hierarchy associated with the fragment.
     * Inflates the fragment layout from the XML resource file.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return The View for the fragment's UI, or null
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_list, container, false);
        return view;
    }

    /**
     * Called immediately after onCreateView has returned, but before any saved state has been restored.
     * This method sets up the RecyclerView, initializes the adapter, configures activity result launchers
     * for image selection, and loads books based on the specified situation.
     *
     * <p>The method performs the following setup operations:</p>
     * <ul>
     *   <li>Initializes RecyclerView with LinearLayoutManager and BookAdapter</li>
     *   <li>Retrieves the situation argument and loads corresponding books</li>
     *   <li>Sets up gallery launcher for image selection from device storage</li>
     *   <li>Sets up camera launcher for capturing new images</li>
     * </ul>
     *
     * @param view The View returned by onCreateView(LayoutInflater, ViewGroup, Bundle)
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup RecyclerView with adapter and layout manager
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        bookList = new ArrayList<>();
        adapter = new BookAdapter(requireContext(), bookList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Load books for the specified situation
        if (getArguments() != null) {
            situation = getArguments().getString(ARG_SITUATION);
            loadBooksBySituation(situation);
        }

        // Setup gallery image selection launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        if (adapter != null) adapter.setSelectedImageUri(selectedImage);
                    }
                });

        // Setup camera image capture launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            Bitmap photo = (Bitmap) extras.get("data");
                            // Save captured photo to MediaStore and get URI
                            String path = MediaStore.Images.Media.insertImage(
                                    requireContext().getContentResolver(),
                                    photo,
                                    "book_cover",
                                    null
                            );
                            if (path != null) {
                                Uri imageUri = Uri.parse(path);
                                if (adapter != null) adapter.setSelectedImageUri(imageUri);
                            }
                        }
                    }
                });
    }

    /**
     * Loads books from Firebase Firestore based on the specified reading situation.
     * This method queries the user's book collection and filters by the situation field.
     *
     * <p>The method performs the following operations:</p>
     * <ul>
     *   <li>Authenticates the current Firebase user</li>
     *   <li>Queries Firestore for books matching the specified situation</li>
     *   <li>Converts Firestore documents to Book objects</li>
     *   <li>Updates the RecyclerView adapter with the loaded books</li>
     * </ul>
     *
     * <p>If no user is authenticated, the method returns early without loading books.
     * The Firestore document ID is preserved in each Book object for future operations.</p>
     *
     * @param situation The reading situation to filter books by
     *                  (e.g., "Read", "Currently Reading", "Stopped Reading", "Want to Read")
     */
    private void loadBooksBySituation(String situation) {
        // Check if user is authenticated
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // Query Firestore for books matching the situation
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(user.getUid())
                .collection("books")
                .whereEqualTo("situation", situation)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null) {
                        bookList.clear();
                        // Convert Firestore documents to Book objects
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Book book = doc.toObject(Book.class);
                            if (book != null) {
                                book.setDocId(doc.getId()); // Preserve Firestore document ID
                                bookList.add(book);
                            }
                        }
                        // Notify adapter of data changes
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}