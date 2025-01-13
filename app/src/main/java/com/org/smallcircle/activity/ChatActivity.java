package com.org.smallcircle.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.org.smallcircle.R;
import com.org.smallcircle.adapter.AdapterChat;
import com.org.smallcircle.databinding.ActivityChatBinding;
import com.org.smallcircle.model.ModelChat;
import com.org.smallcircle.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;

    private String receiptUid = "";
    private String receiptFcmToken = "";

    private static final String TAG = "CHAT_TAG";

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    private String myUid = "";
    private String myName = "";
    private String chatPath = "";
    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        receiptUid = getIntent().getStringExtra("receiptUid");
        myUid = firebaseAuth.getUid();

        chatPath = Utils.chatPath(receiptUid, myUid);

        Log.d(TAG, "onCreate: receiptUid: " + receiptUid);
        Log.d(TAG, "onCreate: myUid: " + myUid);
        Log.d(TAG, "onCreate: chatPath: " + chatPath);

        loadMyInfo();
        loadReceiptDetails();
        loadMessages();

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePickDialog();
            }
        });

        binding.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child("" + firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        myName = "" + snapshot.child("name").getValue();
                        Log.d(TAG, "onDataChange: myName: " + myName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadReceiptDetails() {
        Log.d(TAG, "loadReceiptDetails: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(receiptUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            String name = "" + snapshot.child("name").getValue();
                            String profileImageUrl = "" + snapshot.child("profileImageUrl").getValue();
                            receiptFcmToken = "" + snapshot.child("fcmToken").getValue();

                            Log.d(TAG, "onDataChange: name: " + name);
                            Log.d(TAG, "onDataChange: profileImageUrl: " + profileImageUrl);
                            
                            binding.txtName.setText(name);

                            try {
                                Glide.with(ChatActivity.this)
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.ic_person_black)
                                        .error(R.drawable.ic_broken_image_gray)
                                        .into(binding.imgProfile);
                            } catch (Exception e) {
                                Log.e(TAG, "onDataChange: ", e);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: ", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadMessages() {
        Log.d(TAG, "loadMessages: ");

        ArrayList<ModelChat> chatArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        ref.child(chatPath)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        chatArrayList.clear();

                        for (DataSnapshot ds: snapshot.getChildren()) {
                            try {
                                ModelChat modelChat = ds.getValue(ModelChat.class);

                                chatArrayList.add(modelChat);
                            } catch (Exception e) {
                                Log.e(TAG, "onDataChange: ", e);
                            }
                        }

                        // Menyiapkan LinearLayoutManager untuk RecyclerView
                        LinearLayoutManager layoutManager = new LinearLayoutManager(ChatActivity.this);
                        binding.recyclerChat.setLayoutManager(layoutManager);

                        // Membuat adapter dan menghubungkannya ke RecyclerView
                        AdapterChat adapterChat = new AdapterChat(ChatActivity.this, chatArrayList);
                        binding.recyclerChat.setAdapter(adapterChat);

                        // Panggil notifyDataSetChanged untuk memberi tahu RecyclerView bahwa data telah diperbarui
                        adapterChat.notifyDataSetChanged();

                        if (chatArrayList.size() > 0) {
                            binding.recyclerChat.smoothScrollToPosition(chatArrayList.size() - 1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void imagePickDialog() {
        PopupMenu popupMenu = new PopupMenu(new ContextThemeWrapper(this, R.style.PopupMenuWhiteBackground), binding.btnImage);

        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == 1) {
                    Log.d(TAG, "onMenuItemClick: Camera clicked");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        // Device version is TIRAMISU or above, Only need camera permission
                        requestCameraPermissions.launch(new String[]{Manifest.permission.CAMERA});
                    } else {
                        // Device version is below TIRAMISU, Need camera and storage permission
                        requestCameraPermissions.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
                    }
                } else if (itemId == 2) {
                    Log.d(TAG, "onMenuItemClick: ");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pickImageGallery();
                    } else {
                        requestStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                }

                return true;
            }
        });
    }

    private ActivityResultLauncher<String[]> requestCameraPermissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    Log.d(TAG, "onActivityResult: " + result);

                    boolean areAllGranted = true;

                    for (boolean isGranted: result.values()) {
                        areAllGranted = areAllGranted && isGranted;
                    }

                    if (areAllGranted) {
                        // Camera permission is granted, open camera
                        Log.d(TAG, "onActivityResult: ");
                        pickImageCamera();
                    } else  {
                        // Camera permission is denied, show toast
                        Log.d(TAG, "onActivityResult: Camera permission denied");
                        Utils.toast(ChatActivity.this, "Camera permission denied");
                    }
                }
            }
    );

    private ActivityResultLauncher<String> requestStoragePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    Log.d(TAG, "onActivityResult: isGranted: " + isGranted);

                    if (isGranted) {
                        // Storage permission is granted, open gallery
                        Log.d(TAG, "onActivityResult: Storage permission granted");
                        pickImageGallery();
                    } else {
                        // Storage permission is denied, show toast
                        Log.d(TAG, "onActivityResult: Storage permission denied");
                        Utils.toast(ChatActivity.this, "Storage permission denied");
                    }
                }
            }
    );

    private void pickImageCamera() {
        Log.d(TAG, "pickImageCamera: ");

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "CHAT_IMAGE_TEMP");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "CHAT_IMAGE_TEMP_DESCRIPTION");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Image picked, upload to firebase
                        Log.d(TAG, "onActivityResult: imageUri: " + imageUri);
                        uploadToFirebaseStorage();
                    } else {
                        // Image not picked
                        Utils.toast(ChatActivity.this, "Cancelled");
                    }
                }
            }
    );

    private void pickImageGallery() {
        Log.d(TAG, "pickImageGallery: ");

        Intent intent = new Intent(Intent.ACTION_PICK);

        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();

                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult: imageUri" + imageUri);

                        uploadToFirebaseStorage();
                    } else {
                        Utils.toast(ChatActivity.this, "Cancelled");
                    }
                }
            }
    );

    private void uploadToFirebaseStorage() {
        Log.d(TAG, "uploadToFirebaseStorage: ");

        progressDialog.setMessage("Uploading image...");
        progressDialog.show();

        long timestamp = Utils.getTimestamp();
        String filePathAndName = "ChatImages/" + timestamp;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(imageUri)
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        progressDialog.setMessage("Uploading image. Progress: " + (int) progress + "%");
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                        while (!uriTask.isSuccessful());

                        String imageUrl = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()) {
                            sendMessage(Utils.MESSAGE_TYPE_IMAGE, imageUrl, timestamp);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Utils.toast(ChatActivity.this, "Failed to upload image, due to: " + e.getMessage());
                    }
                });
    }

    private void validateData() {
        Log.d(TAG, "validateData: ");

        String message = binding.editMessage.getText().toString().trim();
        long timestamp = Utils.getTimestamp();

        if (message.isEmpty()) {
            Utils.toast(this, "Message cannot be empty");
        } else {
            sendMessage(Utils.MESSAGE_TYPE_TEXT, message, timestamp);
        }
    }

    private void sendMessage(String messageType, String message, long timestamp) {
        Log.d(TAG, "sendMessage: messageType: " + messageType);
        Log.d(TAG, "sendMessage: message: " + message);
        Log.d(TAG, "sendMessage: timestamp: " + timestamp);

        progressDialog.setMessage("Sending message...");
        progressDialog.show();

        DatabaseReference refChat = FirebaseDatabase.getInstance().getReference("Chats");

        String keyId = "" + refChat.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("messageId", "" + keyId);
        hashMap.put("messageType", "" + messageType);
        hashMap.put("message", "" + message);
        hashMap.put("fromUid", "" + myUid);
        hashMap.put("toUid", "" + receiptUid);
        hashMap.put("timestamp", timestamp);

        refChat.child(chatPath)
                .child(keyId)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        binding.editMessage.setText("");
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Utils.toast(ChatActivity.this, "Failed to send message, due to: " + e.getMessage());
                    }
                });
    }
}