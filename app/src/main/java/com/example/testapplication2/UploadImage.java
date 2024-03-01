package com.example.testapplication2;

import static com.google.common.io.Files.getFileExtension;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testapplication2.Fragments.StatusFragment;
import com.example.testapplication2.Model.StatusModal;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class UploadImage extends AppCompatActivity {
    ImageView imageView;
    TextView opencameratv;
    private String currentphotopath;
    String uid;
    EditText captionEt;
    FloatingActionButton sendbtn;
    FirebaseDatabase database=FirebaseDatabase.getInstance();
    DatabaseReference statusRef,laststatus;
    StatusModal modal;
    Bitmap bitmap;
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        modal=new StatusModal();
        imageView=findViewById(R.id.iv_ss);
        opencameratv=findViewById(R.id.open_cam_ss);
        captionEt=findViewById(R.id.caption_ss);
        sendbtn=findViewById(R.id.sendbtn_ss);
        pb=findViewById(R.id.pb_ss);
        statusRef=database.getReference("Status");
        laststatus=database.getReference("laststatus");

        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        uid=user.getUid();

        opencameratv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filename="photo";
                        File storagedirectory=getExternalFilesDir(Environment.DIRECTORY_PICTURES);

                try {
                    File imagefile=File.createTempFile(filename,"jpg",storagedirectory);
                    currentphotopath=imagefile.getAbsolutePath();
                    Uri imageUri= FileProvider.getUriForFile(UploadImage.this,
                            "com.example.testapplication2.fileprovider",imagefile);

                    Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                    startActivityForResult(intent,1);


                }catch (Exception e){
                    Toast.makeText(UploadImage.this, "Error", Toast.LENGTH_SHORT).show();

                }

            }
        });
        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveStatus();
            }
        });
    }

    private void saveStatus() {
        pb.setVisibility(View.VISIBLE);
        ByteArrayOutputStream baos= new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] data=baos.toByteArray();

        FirebaseStorage storage=FirebaseStorage.getInstance();
        StorageReference storageReference;
        storageReference=storage.getReference("statusimages");

        final StorageReference reference=storageReference.child(System.currentTimeMillis() +".jpg");

        UploadTask uploadTask=reference.putBytes(data);

        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot,Task<Uri>>() {
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
                    Toast.makeText(UploadImage.this, "Status Uploaded", Toast.LENGTH_SHORT).show();


                    Handler handler=new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            Intent i=new Intent(UploadImage.this, MainActivity.class);//TabAct
                            startActivity(i);
                            finish();

                        }
                    },100);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1 && resultCode==RESULT_OK){
            bitmap= BitmapFactory.decodeFile(currentphotopath);
            imageView.setImageBitmap(bitmap);
        }
    }
}