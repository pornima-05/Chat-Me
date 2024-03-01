package com.example.testapplication2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.testapplication2.Model.StatusModal;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class VideoActivity extends AppCompatActivity {
    ExoPlayer exoPlayer;
    StyledPlayerView styledPlayerView;
    MediaItem mediaItem;


    String uid;
    EditText captionEt;
    FloatingActionButton sendbtn;
    FirebaseDatabase database=FirebaseDatabase.getInstance();
    DatabaseReference statusRef,laststatus;
    StatusModal modal;
    Bitmap bitmap;
    String imageUri;
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        Intent intent = getIntent();
        imageUri = intent.getStringExtra("ur");


        styledPlayerView = findViewById(R.id.videoView);
        exoPlayer = new ExoPlayer.Builder(VideoActivity.this).build();

        styledPlayerView.setPlayer(exoPlayer);

        mediaItem = MediaItem.fromUri(Uri.parse(imageUri));

        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);
        modal=new StatusModal();

        sendbtn=findViewById(R.id.sendbtn_ss_video);
        captionEt=findViewById(R.id.caption_video);
        pb=findViewById(R.id.pb_ss_image);
        statusRef=database.getReference("Status");
        laststatus=database.getReference("laststatus");

        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        uid=user.getUid();


        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveStatus();
            }
        });
    }

    private void saveStatus() {

        pb.setVisibility(View.VISIBLE);

        FirebaseStorage storage=FirebaseStorage.getInstance();
        StorageReference storageReference;
        storageReference=storage.getReference("statusvideo");

        final StorageReference reference=storageReference.child(System.currentTimeMillis() +".mp4");

        UploadTask uploadTask=reference.putFile(Uri.parse(imageUri));

        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()){
                    throw  task.getException();
                }
                return reference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    Uri downloadUri=task.getResult();

                    Calendar callfordate=Calendar.getInstance();
                    SimpleDateFormat currentdate=new SimpleDateFormat("dd-MMMM");
                    final String savedate=currentdate.format(callfordate.getTime());

                    Calendar callfortime=Calendar.getInstance();
                    SimpleDateFormat currenttime=new SimpleDateFormat("HH:MM:a");
                    final String savetime=currenttime.format(callfortime.getTime());

                    modal.setDelete(String.valueOf(System.currentTimeMillis()));
                    modal.setImage(downloadUri.toString());
                    modal.setCaption(captionEt.getText().toString().trim());
                    modal.setType("Video");
                    modal.setUid(uid);
                    modal.setTime(savedate+" "+savetime);

                    String key=statusRef.push().getKey();
                    statusRef.child(uid).child(key).setValue(modal);

                    //storing last status ref
                    modal.setDelete(String.valueOf(System.currentTimeMillis()));
                    modal.setImage(downloadUri.toString());
                    modal.setCaption(captionEt.getText().toString().trim());
                    modal.setType("Video");
                    modal.setUid(uid);
                    modal.setTime(savedate+" "+savetime);

                    laststatus.child(uid).setValue(modal);

                    pb.setVisibility(View.GONE);
                    Toast.makeText(VideoActivity.this, "Status Uploaded", Toast.LENGTH_SHORT).show();


                    Handler handler=new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            Intent i=new Intent(VideoActivity.this, MainActivity.class);//TabAct
                            startActivity(i);
                            finish();

                        }
                    },10000);
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        exoPlayer.stop();
        exoPlayer.release();
    }

    @Override
    protected void onStop() {
        super.onStop();
        exoPlayer.stop();
        exoPlayer.release();
    }
}