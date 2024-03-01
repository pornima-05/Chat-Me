package com.example.testapplication2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;

import com.example.testapplication2.Adapter.MessageAdapter;

import com.example.testapplication2.Model.Chat;
import com.example.testapplication2.Model.FirebaseUtils;
import com.example.testapplication2.Model.User;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MessageActivity extends AppCompatActivity {

    private static final int REQUEST_READ_EXTERNAL_STORAGE_PERMISSION =1000;
    int REQUEST_DOCUMENT_PERMISSION=2000;
    CircleImageView profile_image;
    TextView username;

    String otherUser="";
    FirebaseUser fuser;
    DatabaseReference reference;
    private StorageReference storageReference;

    ImageButton btn_send, media_access;
    EditText text_send;

    MessageAdapter messageAdapter;
    List<Chat> mChat;

    RecyclerView recyclerView;

    Intent intent;
    ImageView recoder;

    ValueEventListener seenListener;

    TextView textTime;
    private CountDownTimer countDownTimer;
    private long recordingStartTime;

    ImageView backArrow;
    TextView cancelMsg;

//share media
    String userid;
    MediaPlayer mediaPlayer;
    TextView imagesending, audiosending, videosending,documentSending;
    Uri imageuri;

    //recorder
    private boolean isRecording = false;
    private String audioFilePath = null;
    private MediaRecorder mediaRecorder;
    private boolean isRecordingCanceled = false;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener((view) -> {

            startActivity(new Intent(MessageActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

        });

        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("senderId")) {
            String senderId = getIntent().getStringExtra("senderId");
        }


        //share media
        mediaPlayer = new MediaPlayer();
        //Display Message
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        //send message
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);
        //share media
        media_access = findViewById(R.id.media_access);
        textTime=findViewById(R.id.timeText);
        backArrow=findViewById(R.id.backArrow);
        cancelMsg=findViewById(R.id.cancelMsg);

        intent = getIntent();
        userid = intent.getStringExtra("userid");
        otherUser=intent.getStringExtra("otherUserToken");
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = text_send.getText().toString();
                if (!msg.equals("")) {
                    sendMessage(fuser.getUid(), userid, msg);
                } else {
                    Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });

        //recorder
        recoder=findViewById(R.id.recorder);
        recoder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    startRecording();
                } else {
                    stopRecording();
                }
            }
        });
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelRecording();
            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                username.setText(user.getUsername());

                if (user.getImageURL().equals("default")) {
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                }
                readMessage(fuser.getUid(), userid, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        seenMessage(userid);
    }

    private void cancelRecording() {
        if (isRecording) {
            isRecordingCanceled = true;
            stopRecording();
            Toast.makeText(this, "Recording Canceled", Toast.LENGTH_SHORT).show();
        }
    }

    private static final int RECORD_AUDIO_PERMISSION_REQUEST_CODE = 1000;

    private void startRecording() {
        isRecording = true;
        // Check audio recording permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION_REQUEST_CODE);
            return;
        }
        // Prepare the file path for audio recording
        audioFilePath = getExternalFilesDir("audio_recordings").getAbsolutePath() + "/audio_recording.3gp";

        // Create a MediaRecorder instance and configure it
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(audioFilePath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        media_access.setVisibility(View.GONE);
        btn_send.setVisibility(View.GONE);
        text_send.setVisibility(View.GONE);
        textTime.setVisibility(View.VISIBLE);
        backArrow.setVisibility(View.VISIBLE);
        cancelMsg.setVisibility(View.VISIBLE);

        try {
            // Start recording
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            recoder.setImageResource(R.drawable.ic_baseline_mic);
            recordingStartTime=System.currentTimeMillis();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to start recording", Toast.LENGTH_SHORT).show();
        }
        recordingStartTime = System.currentTimeMillis();
        countDownTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long elapsedTime = System.currentTimeMillis() - recordingStartTime;
                updateTimer(elapsedTime);
            }
            @Override
            public void onFinish() {
                // Handle the timer finish if needed
            }
        };
         countDownTimer.start();
    }
    private void updateTimer(long elapsedTime) {
        long seconds = elapsedTime / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;

        String timeString = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        textTime.setText(timeString);
    }
    @Override
    public void onBackPressed() {
        cancelRecording();
        super.onBackPressed();
    }
    private void stopRecording() {
        if (mediaRecorder != null) {
            // Stop recording
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
            recoder.setImageResource(R.drawable.ic_baseline_mic_24);
            textTime.setVisibility(View.GONE);
            backArrow.setVisibility(View.GONE);
            cancelMsg.setVisibility(View.GONE);
            media_access.setVisibility(View.VISIBLE);
            btn_send.setVisibility(View.VISIBLE);
            text_send.setVisibility(View.VISIBLE);

            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            if (!isRecordingCanceled) {
                // Upload the recording only if it's not canceled
                uploadAudio();
            }
            else {
                // Handle the case when the recording is canceled
                Toast.makeText(MessageActivity.this, "Recording Canceled", Toast.LENGTH_SHORT).show();
                // You can delete the audio file if needed
                // new File(audioFilePath).delete();
            }
            // Reset the flag
            isRecordingCanceled = false;
        }
    }

    private void uploadAudio() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIcon(R.drawable.audiofile);
        progressDialog.setMessage("Recording Sending To : " + username);
        progressDialog.setTitle("Sending Recording.....");
        progressDialog.show();

        // Generate a unique filename for the audio file
        String time = String.valueOf(System.currentTimeMillis());
        String audioFile = "ChatAudio/" + "Audio" + time;

        // Create a reference to the Firebase Storage location
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(audioFile);

        // Upload the audio file
        storageReference.putFile(Uri.fromFile(new File(audioFilePath)))
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL of the uploaded file
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    uriTask.addOnCompleteListener(uriTask1 -> {
                        if (uriTask1.isSuccessful()) {
                            String downloadAudio = uriTask1.getResult().toString();

                            // Save the audio message in the database
                            DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference();
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", fuser.getUid());
                            hashMap.put("receiver", userid);
                            hashMap.put("message", downloadAudio);
                            hashMap.put("type", "Record");
                            hashMap.put("isseen", false);

                            databaseReference1.child("Chats").push().setValue(hashMap);
                            // Dismiss the progress dialog
                            progressDialog.dismiss();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(MessageActivity.this, "Failed to upload audio", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(MessageActivity.this, "Failed to upload audio", Toast.LENGTH_SHORT).show();
                })
                .addOnProgressListener(taskSnapshot -> {
                    // Show upload progress
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploading " + (int) progress + "%");
                });
    }

    //seen message
    private void seenMessage(final String userid) {
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat != null) {
                        if (chat.getReceiver() != null && chat.getSender() != null) {
                            if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid)) {
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("isseen", true);
                                dataSnapshot.getRef().updateChildren(hashMap);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    //send message
    private void sendMessage(String sender, final String receiver, String message) {

        reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isseen", false);
        hashMap.put("type", "text");


        reference.child("Chats").push().setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        sendNotification(message);
                    }
                });
////add user to chat fragment
//       final DatabaseReference chatRef=FirebaseDatabase.getInstance().getReference("Chatlist")
//                .child(fuser.getUid())
//                .child(userid);
//
//        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (!snapshot.exists()){
//                    chatRef.child("id").setValue(userid);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }

    private void sendNotification(String message) {
        FirebaseUtils.currentUserInfo().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    User cuser=snapshot.getValue(User.class);
                    try {
                        JSONObject object=new JSONObject();
                        JSONObject notiobj=new JSONObject();
                        notiobj.put("title",cuser.getUsername());
                        notiobj.put("body",message);

                        JSONObject dObj=new JSONObject();
                        dObj.put("userId",FirebaseAuth.getInstance().getCurrentUser().getUid());

                        object.put("notification",notiobj);
                        object.put("data",dObj);
                        object.put("to",otherUser);

                        api(object);

                    }catch (Exception e){
                        Toast.makeText(MessageActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void api(JSONObject object) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(JSON, object.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", "Bearer AAAAB50hNBQ:APA91bGc7KzeOT_3wmLrCdfc__IlKlCLNU20LSFVPsszCq2-kZBKAb18ABiRSB9qPNTS75Hl7uGh-baxNiOZV1opOsYZZ_2tLo7z8-qtwxmByl7elc80Ka6i7NmsIL4Joqv25GeQImaz")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Toast.makeText(MessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            imageuri = data.getData();
            try {
                setUploadImages(imageuri);
            } catch (IOException e) {
                throw new RuntimeException(e);
                //e.printStackTrace();
            }
        }else if (requestCode == 1000 && resultCode == RESULT_OK && data != null) {
            if (checkReadExternalStoragePermission()) {
                imageuri = data.getData();
                if (imageuri == null) {
                    Toast.makeText(this, "No File", Toast.LENGTH_SHORT).show();
                } else {
                    setUploadAudio(imageuri);
                }
            }else {
                // Request the permission
                requestReadExternalStoragePermission();
            }

        } else if (requestCode == 10000 && resultCode == RESULT_OK && data != null) {
            imageuri = data.getData();
            if (imageuri == null) {
                Toast.makeText(this, "No File Selected", Toast.LENGTH_SHORT).show();
            } else {
                UploadVideo(imageuri);
            }
        } else {
            Toast.makeText(this, "Please Select Image", Toast.LENGTH_SHORT).show();
        }

        if (requestCode == REQUEST_DOCUMENT_PERMISSION && resultCode == RESULT_OK && data != null) {
            Uri documentUri = data.getData();
            if (documentUri != null) {
                setUploadDocument(documentUri);
            } else {
                Toast.makeText(this, "No Document Selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setUploadDocument(Uri documentUri) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIcon(R.drawable.document);
        progressDialog.setMessage("Document Sending To: " + username);
        progressDialog.setTitle("Sending Document...");
        progressDialog.show();

        String time = String.valueOf(System.currentTimeMillis());
        String documentFile = "ChatDocuments/" + "Document" + time;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(documentFile);
        storageReference.putFile(documentUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL of the uploaded file
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    uriTask.addOnCompleteListener(uriTask1 -> {
                        if (uriTask1.isSuccessful()) {
                            String downloadDocument = uriTask1.getResult().toString();

                            // Save the document message in the database
                            DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference();
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", fuser.getUid());
                            hashMap.put("receiver", userid);
                            hashMap.put("message", downloadDocument);
                            hashMap.put("type", "Document");
                            hashMap.put("isseen", false);

                            databaseReference1.child("Chats").push().setValue(hashMap);

                            // Dismiss the progress dialog
                            progressDialog.dismiss();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(MessageActivity.this, "Failed to upload document", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(MessageActivity.this, "Failed to upload document", Toast.LENGTH_SHORT).show();
                })
                .addOnProgressListener(taskSnapshot -> {
                    // Show upload progress
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploading " + (int) progress + "%");
                });
    }

    private boolean checkReadExternalStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }
    private void requestReadExternalStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_READ_EXTERNAL_STORAGE_PERMISSION);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with the desired action (e.g., access audio files)
                if (imageuri != null) {
                    setUploadAudio(imageuri);
                }
            } else {
                // Permission denied. Notify the user or handle it accordingly.
                Toast.makeText(this, "Permission denied. Cannot upload audio.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setUploadImages(Uri uri) throws IOException {

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Image will be Sending");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIcon(R.drawable.sending);
        progressDialog.setMessage("Image Sending To  :" + username);
        progressDialog.show();
        String timestamp = "" + System.currentTimeMillis();
        String FileNameAndPath = "Chatimages/" + "post" + timestamp;

        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(FileNameAndPath);
        storageReference.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();


                        uriTask.addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    String downloadurl = task.getResult().toString();
                                    reference = FirebaseDatabase.getInstance().getReference();
                                    HashMap<String, Object> hashMap1 = new HashMap<>();
                                    hashMap1.put("sender", fuser.getUid());
                                    hashMap1.put("receiver", userid);
                                    hashMap1.put("message", downloadurl);
                                    hashMap1.put("type", "image");
                                    hashMap1.put("isseen", false);

                                    reference.child("Chats").push().setValue(hashMap1);
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.dismiss();
                                        }
                                    }, 300);
                                }else {
                                    Exception exception=task.getException();
                                    if (exception!=null){
                                        exception.printStackTrace();
                                        Toast.makeText(MessageActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                    }
                });
    }
    public void setUploadAudio(Uri uri) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIcon(R.drawable.audiofile);
        progressDialog.setMessage("Audio Sending To : " + username);
        progressDialog.setTitle("Sending Audio.....");
        progressDialog.show();
        String time = "" + System.currentTimeMillis();
        String AudioFile = "ChatAudio/" + "Audio" + time;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(AudioFile);
        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                String download_audio = uriTask.getResult().toString();

                if (uriTask.isSuccessful()) {

                    Toast.makeText(MessageActivity.this, "Sending a Audio", Toast.LENGTH_SHORT).show();
                    reference= FirebaseDatabase.getInstance().getReference();
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("sender", fuser.getUid());
                    hashMap.put("receiver", userid);
                    hashMap.put("message", download_audio);
                    hashMap.put("type", "Audio");
                    hashMap.put("isseen", false);

                    reference.child("Chats").push().setValue(hashMap);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                        }
                    }, 300);
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MessageActivity.this, "SomeThing Went Wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void UploadVideo(Uri uri) {
        ProgressDialog progressDialog = new ProgressDialog(MessageActivity.this);
        progressDialog.setIcon(R.drawable.video);
        progressDialog.setMessage("Video Sending To : " + username);
        progressDialog.setTitle("Sending Video.....");
        progressDialog.show();
        String time = "" + System.currentTimeMillis();
        String VedioFile = "ChatVideo/" + "Video" + time;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(VedioFile);
        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                String downloadvideo = uriTask.getResult().toString();

                if (uriTask.isSuccessful()) {
                    Toast.makeText(MessageActivity.this, "Sending a Video", Toast.LENGTH_SHORT).show();
                    DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference();
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", fuser.getUid());
                    hashMap.put("receiver", userid);
                    hashMap.put("message", downloadvideo);
                    hashMap.put("type", "Video");
                    hashMap.put("isseen", false);

                    databaseReference1.child("Chats").push().setValue(hashMap);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                        }
                    }, 300);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MessageActivity.this, "Something Happen Wrong", Toast.LENGTH_SHORT).show();
            }
        });

    }
    //display or read message
    private void readMessage(final String myid, final String userid, final String imageurl) {
        mChat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mChat.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);

                    if (chat != null && chat.getReceiver() != null && chat.getSender() != null) {
                        if ((chat.getReceiver().equals(myid) && chat.getSender().equals(userid)) ||
                                (chat.getReceiver().equals(userid) && chat.getSender().equals(myid))) {
                            mChat.add(chat);
                        }
                        messageAdapter = new MessageAdapter(getApplicationContext(), mChat, imageurl);
                        recyclerView.setAdapter(messageAdapter);
                        messageAdapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }
    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
    }

    public void media(View view) {
        AlertDialog alert = new AlertDialog.Builder(MessageActivity.this).create();
        View myview = getLayoutInflater().inflate(R.layout.menulayout, null, false);
        alert.setView(myview);
        imagesending = myview.findViewById(R.id.imagesending);
        audiosending = myview.findViewById(R.id.audiosending);
        videosending = myview.findViewById(R.id.videosending);
        documentSending=myview.findViewById(R.id.documentSending);

        imagesending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 100);
                Toast.makeText(MessageActivity.this, "Opening Images", Toast.LENGTH_SHORT).show();
                alert.dismiss();
            }
        });
        audiosending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("audio/*");
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, imageuri);
                startActivityForResult(intent, REQUEST_READ_EXTERNAL_STORAGE_PERMISSION);
                alert.dismiss();
            }
        });
        videosending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent1, 10000);
                Toast.makeText(MessageActivity.this, "Opening Video", Toast.LENGTH_SHORT).show();
                alert.dismiss();
            }
        });
        documentSending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open the document picker
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*"); // Allow all file types
                startActivityForResult(intent, REQUEST_DOCUMENT_PERMISSION);
                alert.dismiss();
            }
        });
//        alert.setNegativeButton("dismiss", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                AlertDialog test = alert.create();
//                test.dismiss();
//            }
//        });
        alert.show();
    }
}