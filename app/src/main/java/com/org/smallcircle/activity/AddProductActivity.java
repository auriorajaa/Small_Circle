package com.org.smallcircle.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.internal.TextWatcherAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.org.smallcircle.R;
import com.org.smallcircle.adapter.AdapterImagePicked;
import com.org.smallcircle.databinding.ActivityAddProductBinding;
import com.org.smallcircle.location.LocationPickerActivity;
import com.org.smallcircle.model.ModelImagePicked;
import com.org.smallcircle.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AddProductActivity extends AppCompatActivity {

    private ActivityAddProductBinding binding;

    private static final String TAG = "ADD_PRODUCT_TAG";

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    private Uri imageUri = null;

    private String brand = "";
    private String category = "";
    private String condition = "";
    private String address = "";
    private String price = "";
    private String title = "";
    private String description = "";
    private double latitude = 0;
    private double longitude = 0;

    private ArrayList<ModelImagePicked> imagePickedArrayList;
    private AdapterImagePicked adapterImagePicked;

    private boolean isEditMode = false;
    private String productIdForEditing = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        ArrayAdapter<String> adapterCategories = new ArrayAdapter<>(this, R.layout.row_category_auto_complete, Utils.categories);
        binding.categoryAutoComplete.setAdapter(adapterCategories);

        ArrayAdapter<String> adapterCondition = new ArrayAdapter<>(this, R.layout.row_condition, Utils.condition);
        binding.conditionAutoComplete.setAdapter(adapterCondition);

        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("isEditMode", false);
        Log.d(TAG, "onCreate: isEditMode: " + isEditMode);

        if (isEditMode) {
            productIdForEditing = intent.getStringExtra("productId");
            loadProductDetail();

            binding.toolbarTitle.setText("Edit Product");
            binding.submitListingButton.setText("Update Product");
        } else {
            binding.toolbarTitle.setText("Add Product");
            binding.submitListingButton.setText("Add Product");
        }

        imagePickedArrayList = new ArrayList<>();
        loadImages();

        setFieldListeners();

        binding.toolbarBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.imageUploadCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickOptions();
            }
        });

        binding.selectImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickOptions();
            }
        });

        binding.locationAutoComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddProductActivity.this, LocationPickerActivity.class);
                locationPickerActivityResultLauncher.launch(intent);
            }
        });

        binding.submitListingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private ActivityResultLauncher<Intent> locationPickerActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "onActivityResult: ");

                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();

                        if (data != null) {
                            latitude = data.getDoubleExtra("latitude", 0.0);
                            longitude = data.getDoubleExtra("longitude", 0.0);
                            address = data.getStringExtra("address");

                            Log.d(TAG, "onActivityResult: latitude: " + latitude);
                            Log.d(TAG, "onActivityResult: longitude: " + longitude);
                            Log.d(TAG, "onActivityResult: address: " + address);

                            binding.locationAutoComplete.setText(address);
                        }

                    } else {
                        Log.d(TAG, "onActivityResult: Cancelled");
                        Utils.toast(AddProductActivity.this, "Cancelled!");
                    }
                }
            }
    );

    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder.setTitle("Success")
                .setMessage("Product added successfully!")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(AddProductActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
            }
        });

        dialog.show();
    }

    @SuppressLint("RestrictedApi")
    private void setFieldListeners() {
        binding.brandEditText.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                binding.brandInputLayout.setError(null);
            }
        });

        binding.categoryAutoComplete.setOnItemClickListener((parent, view, position, id) ->
                binding.categoryInputLayout.setError(null));

        binding.conditionAutoComplete.setOnItemClickListener((parent, view, position, id) ->
                binding.conditionInputLayout.setError(null));

        binding.descriptionEditText.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                binding.descriptionInputLayout.setError(null);
            }
        });
    }

    private void validateData() {
        Log.d(TAG, "validateData: ");

        brand = binding.brandEditText.getText().toString().trim();
        category = binding.categoryAutoComplete.getText().toString().trim();
        condition = binding.conditionAutoComplete.getText().toString().trim();
        address = binding.locationAutoComplete.getText().toString().trim();
        price = binding.priceEditText.getText().toString().trim();
        title = binding.titleEditText.getText().toString().trim();
        description = binding.descriptionEditText.getText().toString().trim();

        boolean isValid = true;

        if (brand.isEmpty()) {
            Utils.setErrorState(binding.brandInputLayout, "Please input brand name!");
            isValid = false;
        }
        if (category.isEmpty()) {
            Utils.setErrorState(binding.categoryInputLayout, "Please choose product category!");
            isValid = false;
        }
        if (condition.isEmpty()) {
            Utils.setErrorState(binding.conditionInputLayout, "Please choose product condition!");
            isValid = false;
        }
        if (price.isEmpty()) {
            Utils.setErrorState(binding.conditionInputLayout, "Please enter price product!");
            isValid = false;
        }
        if (title.isEmpty()) {
            Utils.setErrorState(binding.descriptionInputLayout, "Please input product title!");
            isValid = false;
        }
        if (description.isEmpty()) {
            Utils.setErrorState(binding.descriptionInputLayout, "Please input product description!");
            isValid = false;
        }
        if (imagePickedArrayList.isEmpty()) {
            Utils.toast(this, "Pick at least one product image");
            isValid = false;
        }

        if (isValid) {
            if (isEditMode) {
                updateProduct();
            } else {
                addProduct();
            }
        }
    }

    private void addProduct() {
        Log.d(TAG, "submitProduct: ");

        progressDialog.setMessage("Adding Product");
        progressDialog.show();

        long timestamp = Utils.getTimestamp();
        DatabaseReference refProduct = FirebaseDatabase.getInstance().getReference("Product");
        String keyId = refProduct.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", "" + keyId);
        hashMap.put("uid", "" + firebaseAuth.getUid());
        hashMap.put("brand", "" + brand);
        hashMap.put("category", "" + category);
        hashMap.put("condition", "" + condition);
        hashMap.put("price", "" + price);
        hashMap.put("address", "" + address);
        hashMap.put("title", "" + title);
        hashMap.put("description", "" + description);
        hashMap.put("status", "" + Utils.PRODUCT_STATUS_AVAILABLE);
        hashMap.put("timestamp", timestamp);
        hashMap.put("latitude", latitude);
        hashMap.put("longitude", longitude);

        refProduct.child(keyId)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Product added");
                        uploadImageStorage(keyId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);

                        progressDialog.dismiss();
                        Utils.toast(AddProductActivity.this, e.getMessage());
                    }
                });
    }

    private void updateProduct() {
        Log.d(TAG, "updateProduct: ");

        progressDialog.setMessage("Updating Product");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("brand", "" + brand);
        hashMap.put("category", "" + category);
        hashMap.put("condition", "" + condition);
        hashMap.put("price", "" + price);
        hashMap.put("address", "" + address);
        hashMap.put("title", "" + title);
        hashMap.put("description", "" + description);
        hashMap.put("latitude", latitude);
        hashMap.put("longitude", longitude);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Product");
        ref.child(productIdForEditing)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "updateProduct: Product details updated successfully");

                        uploadImageStorage(productIdForEditing);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Utils.toast(AddProductActivity.this, e.getMessage());
                    }
                });
    }

    private void uploadImageStorage(String productId) {
        Log.d(TAG, "uploadImageStorage: Starting upload for " + imagePickedArrayList.size() + " images");

        if (imagePickedArrayList.isEmpty()) {
            progressDialog.dismiss();
            showSuccessDialog();
            return;
        }

        final AtomicInteger successfulUploads = new AtomicInteger(0);
        final AtomicInteger failedUploads = new AtomicInteger(0);
        final int totalImages = imagePickedArrayList.size();

        DatabaseReference imagesRef = FirebaseDatabase.getInstance()
                .getReference("Product")
                .child(productId)
                .child("Images");

        HashMap<String, Object> allImagesMap = new HashMap<>();

        // Handle existing images first
        if (isEditMode) {
            for (ModelImagePicked existingImage : imagePickedArrayList) {
                if (existingImage.getFromInternet()) {
                    HashMap<String, Object> imageData = new HashMap<>();
                    imageData.put("id", existingImage.getId());
                    imageData.put("imageUrl", existingImage.getImageUrl()); // Use imageUrl instead of imageUri
                    imageData.put("timestamp", existingImage.getTimestamp());

                    allImagesMap.put(existingImage.getId(), imageData);
                    successfulUploads.incrementAndGet();
                }
            }
        }

        // Count new images
        int newImagesCount = 0;
        for (ModelImagePicked image : imagePickedArrayList) {
            if (!image.getFromInternet()) {
                newImagesCount++;
            }
        }
        final int totalNewImages = newImagesCount;

        // If no new images and we have existing images, update database
        if (totalNewImages == 0) {
            if (!allImagesMap.isEmpty()) {
                imagesRef.setValue(allImagesMap) // Use setValue instead of updateChildren
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Successfully updated database with existing images");
                            progressDialog.dismiss();
                            showSuccessDialog();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to update database with existing images", e);
                            progressDialog.dismiss();
                            Utils.toast(AddProductActivity.this, "Failed to update images: " + e.getMessage());
                        });
                return;
            } else {
                // If no images at all, clear the Images node
                imagesRef.removeValue()
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Successfully cleared Images node");
                            progressDialog.dismiss();
                            showSuccessDialog();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to clear Images node", e);
                            progressDialog.dismiss();
                            Utils.toast(AddProductActivity.this, "Failed to update images: " + e.getMessage());
                        });
                return;
            }
        }

        // Upload new images
        for (int i = 0; i < imagePickedArrayList.size(); i++) {
            final int index = i;
            ModelImagePicked modelImagePicked = imagePickedArrayList.get(i);

            if (!modelImagePicked.getFromInternet()) {
                String imageName = System.currentTimeMillis() + "_" + index;
                Uri imageUri = modelImagePicked.getImageUri();

                if (imageUri == null) {
                    Log.e(TAG, "Image URI is null for index: " + index);
                    failedUploads.incrementAndGet();
                    checkUploadCompletion(productId, totalImages, successfulUploads.get(), failedUploads.get());
                    continue;
                }

                File compressedFile = compressImage(imageUri);
                if (compressedFile == null) {
                    Log.e(TAG, "Failed to compress image: " + imageUri);
                    failedUploads.incrementAndGet();
                    checkUploadCompletion(productId, totalImages, successfulUploads.get(), failedUploads.get());
                    continue;
                }

                String filePathAndName = "Product/" + imageName;
                StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);

                progressDialog.setMessage("Uploading image " + (index + 1) + " of " + totalNewImages);

                storageReference.putFile(Uri.fromFile(compressedFile))
                        .addOnProgressListener(snapshot -> {
                            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                            progressDialog.setMessage("Uploading image " + (index + 1) + " of " + totalNewImages +
                                    "\nProgress: " + (int) progress + "%");
                        })
                        .addOnSuccessListener(taskSnapshot -> {
                            taskSnapshot.getStorage().getDownloadUrl()
                                    .addOnSuccessListener(uploadedImageUrl -> {
                                        HashMap<String, Object> imageData = new HashMap<>();
                                        imageData.put("id", imageName);
                                        imageData.put("imageUrl", uploadedImageUrl.toString());
                                        imageData.put("timestamp", System.currentTimeMillis());

                                        allImagesMap.put(imageName, imageData);

                                        int currentSuccess = successfulUploads.incrementAndGet();

                                        if (currentSuccess == (totalNewImages + (isEditMode ? totalImages - totalNewImages : 0))) {
                                            imagesRef.setValue(allImagesMap)
                                                    .addOnSuccessListener(aVoid -> {
                                                        checkUploadCompletion(productId, totalImages, currentSuccess, failedUploads.get());
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        failedUploads.set(totalImages);
                                                        checkUploadCompletion(productId, totalImages, 0, totalImages);
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        failedUploads.incrementAndGet();
                                        checkUploadCompletion(productId, totalImages, successfulUploads.get(), failedUploads.get());
                                    });
                        })
                        .addOnFailureListener(e -> {
                            failedUploads.incrementAndGet();
                            checkUploadCompletion(productId, totalImages, successfulUploads.get(), failedUploads.get());
                        });
            }
        }
    }
    private void checkUploadCompletion(String productId, int totalImages, int successfulUploads, int failedUploads) {
        Log.d(TAG, "checkUploadCompletion: Success=" + successfulUploads +
                ", Failed=" + failedUploads + ", Total=" + totalImages);

        if (successfulUploads + failedUploads == totalImages) {
            progressDialog.dismiss();

            if (failedUploads > 0) {
                // Some uploads failed
                runOnUiThread(() -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Upload Status")
                            .setMessage("Uploaded " + successfulUploads + " out of " + totalImages +
                                    " images successfully. " + failedUploads + " images failed to upload.")
                            .setPositiveButton("OK", (dialog, which) -> {
                                if (successfulUploads > 0) {
                                    showSuccessDialog();
                                }
                            })
                            .show();
                });
            } else {
                // All uploads successful
                showSuccessDialog();
            }
        }
    }

    // Metode untuk mengompresi gambar
    private File compressImage(Uri imageUri) {
        try {
            // Get bitmap with reduced size
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2; // Reduce image size by factor of 2

            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            // Create temporary file
            File tempFile = new File(getCacheDir(), "temp_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream out = new FileOutputStream(tempFile);

            // Compress with medium quality
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

            return tempFile;
        } catch (IOException e) {
            Log.e(TAG, "Error compressing image: " + e.getMessage());
            return null;
        }
    }

    private void loadImages() {
        Log.d(TAG, "loadImages: ");

        adapterImagePicked = new AdapterImagePicked(this, imagePickedArrayList, productIdForEditing);
        binding.uploadedImagesRecyclerView.setAdapter(adapterImagePicked);
    }

    @SuppressLint("RestrictedApi")
    private void showImagePickOptions() {
        Log.d(TAG, "showImagePickOptions: ");

        PopupMenu popupMenu = new PopupMenu(new ContextThemeWrapper(this, R.style.PopupMenuWhiteBackground), binding.imageUploadCard);

        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");

        // Tambahkan style putih
        if (popupMenu.getMenu() instanceof MenuBuilder) {
            MenuBuilder menuBuilder = (MenuBuilder) popupMenu.getMenu();
            menuBuilder.setOptionalIconsVisible(true);
        }

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int itemId = item.getItemId();

                if (itemId == 1) {
                    Log.d(TAG, "onMenuItemClick: Camera clicked, check if camera permission is granted or not");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        String[] cameraPermission = new String[]{Manifest.permission.CAMERA};
                        requestCameraPermissions.launch(cameraPermission);
                    } else {
                        String[] cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestCameraPermissions.launch(cameraPermission);
                    }
                } else if (itemId == 2) {
                    Log.d(TAG, "onMenuItemClick: Check if storage permission is granted or not");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pickImageFromGallery();
                    } else {
                        String storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                        requestStoragePermission.launch(storagePermission);
                    }
                }

                return true;
            }
        });
    }

    private ActivityResultLauncher<String> requestStoragePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    Log.d(TAG, "onActivityResult: isGranted: " + isGranted);

                    if (isGranted) {
                        pickImageFromGallery();
                    } else {
                        Utils.toast(AddProductActivity.this, "Storage permission is denied...");
                    }
                }
            }
    );

    private ActivityResultLauncher<String[]> requestCameraPermissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    Log.d(TAG, "onActivityResult: " + result.toString());

                    boolean areAllGranted = true;

                    for (Boolean isGranted: result.values()) {
                        areAllGranted = areAllGranted && isGranted;
                    }

                    if (areAllGranted) {
                        Log.d(TAG, "onActivityResult: All premission is granted");
                        pickImageFromCamera();
                    } else {
                        Log.d(TAG, "onActivityResult: All or either permission is denied");

                        Utils.toast(AddProductActivity.this, "Camera or storage or both permission is denied...");
                    }
                }
            }
    );

    private void pickImageFromGallery() {
        Log.d(TAG, "pickImageFromGallery: ");

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        galleryActivityResultLauncher.launch(intent);
    }

    private void pickImageFromCamera() {
        Log.d(TAG, "pickImageFromCamera: ");

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMP_TITLE");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMP_DESCRIPTION");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "onActivityResult: ");

                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();

                        if (data.getClipData() != null) { // Jika multiple gambar dipilih
                            int count = data.getClipData().getItemCount();
                            for (int i = 0; i < Math.min(count, 10); i++) { // Batasi hingga 10 gambar
                                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                String timestamp = "" + Utils.getTimestamp();
                                ModelImagePicked modelImagePicked = new ModelImagePicked(timestamp, imageUri, null, false);
                                imagePickedArrayList.add(modelImagePicked);
                            }
                        } else if (data.getData() != null) { // Jika hanya satu gambar
                            Uri imageUri = data.getData();
                            String timestamp = "" + Utils.getTimestamp();
                            ModelImagePicked modelImagePicked = new ModelImagePicked(timestamp, imageUri, null, false);
                            imagePickedArrayList.add(modelImagePicked);
                        }

                        loadImages();
                    } else {
                        Utils.toast(AddProductActivity.this, "Cancelled");
                    }
                }
            }
    );

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "onActivityResult: ");

                    if (result.getResultCode() == Activity.RESULT_OK) {

                        Log.d(TAG, "onActivityResult: Image picked from gallery: " + imageUri);

                        String timestamp = "" + Utils.getTimestamp();

                        ModelImagePicked modelImagePicked = new ModelImagePicked(timestamp, imageUri, null, false);
                        imagePickedArrayList.add(modelImagePicked);

                        loadImages();
                    } else {
                        Utils.toast(AddProductActivity.this, "Cancelled");
                    }
                }
            }
    );

    private void loadProductDetail() {
        Log.d(TAG, "loadProductDetail: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Product");
        ref.child(productIdForEditing)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String brand = "" + snapshot.child("brand").getValue();
                        String category = "" + snapshot.child("category").getValue();
                        String condition = "" + snapshot.child("condition").getValue();
                        String price = "" + snapshot.child("price").getValue();
                        latitude = (double) snapshot.child("latitude").getValue();
                        longitude = (double) snapshot.child("longitude").getValue();
                        address = "" + snapshot.child("address").getValue();
                        title = "" + snapshot.child("title").getValue();
                        description = "" + snapshot.child("description").getValue();

                        binding.brandEditText.setText(brand);
                        binding.categoryAutoComplete.setText(category);
                        binding.conditionAutoComplete.setText(condition);
                        binding.priceEditText.setText(price);
                        binding.locationAutoComplete.setText(address);
                        binding.titleEditText.setText(title);
                        binding.descriptionEditText.setText(description);

                        DatabaseReference refImages = snapshot.child("Images").getRef();
                        refImages.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds: snapshot.getChildren()) {
                                    String id = "" + ds.child("id").getValue();
                                    String imageUrl = "" + ds.child("imageUrl").getValue();

                                    ModelImagePicked modelImagePicked = new ModelImagePicked(id, null, imageUrl, true);
                                    imagePickedArrayList.add(modelImagePicked);
                                }

                                loadImages();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}