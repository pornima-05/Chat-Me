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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.testapplication2.Model.StatusModal;
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

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ImageActivity extends AppCompatActivity {

    ImageView imageView;
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
        setContentView(R.layout.activity_image);

        modal=new StatusModal();
        imageView=findViewById(R.id.iv_ss_image);
        sendbtn=findViewById(R.id.sendbtn_ss_image);
        captionEt=findViewById(R.id.caption_ss_image);
        pb=findViewById(R.id.pb_ss_image);
        statusRef=database.getReference("Status");
        laststatus=database.getReference("laststatus");

        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        uid=user.getUid();

        Bundle bundle=getIntent().getExtras();
        if (bundle!=null){
            imageUri=bundle.getString("u");

            Glide.with(getApplicationContext()).load(imageUri).into(imageView);
        }

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
        storageReference=storage.getReference("statusimages");

        final StorageReference reference=storageReference.child(System.currentTimeMillis() +".jpg");

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
                    modal.setUid(uid);
                    modal.setTime(savedate+" "+savetime);

                    String key=statusRef.push().getKey();
                    statusRef.child(uid).child(key).setValue(modal);

                    //storing last status ref
                    modal.setDelete(String.valueOf(System.currentTimeMillis()));
                    modal.setImage(downloadUri.toString());
                    modal.setCaption(captionEt.getText().toString().trim());
                    modal.setUid(uid);
                    modal.setTime(savedate+" "+savetime);

                    laststatus.child(uid).setValue(modal);

                    pb.setVisibility(View.GONE);
                    Toast.makeText(ImageActivity.this, "Status Uploaded", Toast.LENGTH_SHORT).show();


                    Handler handler=new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            Intent i=new Intent(ImageActivity.this, MainActivity.class);//TabAct
                            startActivity(i);
                            finish();

                        }
                    },10000);
                }
            }
        });
    }
}