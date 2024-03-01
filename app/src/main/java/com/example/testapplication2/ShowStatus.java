package com.example.testapplication2;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.example.testapplication2.Model.StatusModal;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class ShowStatus extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    List<String> image;
    List<String> uid;
    List<String> delete;
    List<String> caption;
    List<String> time;

    FirebaseFirestore db=FirebaseFirestore.getInstance();
    DocumentReference documentReference;
    StatusModal modal;
    StoriesProgressView storiesProgressView;
    String userid;
    FirebaseDatabase database=FirebaseDatabase.getInstance();
    DatabaseReference statusref,laststatus;
    int counter=0;
    ImageView s_iv,useriv;
    TextView tvname,storyviewTv,captionTV,timetv;

    private VideoView videoView;

    private View.OnTouchListener onTouchListener=new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_status);

        storiesProgressView=findViewById(R.id.stories);
        s_iv=findViewById(R.id.iv_status_show);
        useriv=findViewById(R.id.iv_user_show);
        tvname=findViewById(R.id.tv_uname_ss);
        storyviewTv=findViewById(R.id.statusCount);
        captionTV=findViewById(R.id.story_cap_tv);
        timetv=findViewById(R.id.tv_time_ss);
        videoView = findViewById(R.id.video_status_show);

        laststatus=database.getReference("laststatus");
        modal=new StatusModal();

        View reverse=findViewById(R.id.viewNext);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.skip();
            }
        });
        reverse.setOnTouchListener(onTouchListener);

        reverse.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                storiesProgressView.pause();
                return false;
            }
        });

        reverse.setOnTouchListener(onTouchListener);

        View skip=findViewById(R.id.viewPrevious);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.reverse();
            }
        });
        skip.setOnTouchListener(onTouchListener);

        skip.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                storiesProgressView.pause();
                return false;
            }
        });

        Bundle extras=getIntent().getExtras();
        if (extras!=null){
            userid=extras.getString("uid");
        }else {

        }
        statusref=database.getReference("Status").child(userid);
    }

    @Override
    protected void onStart() {
        super.onStart();

        try {
            // Get the current authenticated user
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser != null) {
                // Get the user ID
                userid = currentUser.getUid();

                // Fetch stories for the current user
                getStories(userid);

                // Fetch and display user information
                fetchuserinfo(uid.get(counter)); // Ensure that this line is called appropriately
            } else {
                // Handle the case when there is no authenticated user
                Toast.makeText(this, "No authenticated user", Toast.LENGTH_SHORT).show();
                // You might want to redirect the user to the login screen or take appropriate action
            }
        } catch (Exception e) {
           // Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    private void getStories(String userid) {
        image=new ArrayList<>();
        uid=new ArrayList<>();
        delete=new ArrayList<>();
        caption=new ArrayList<>();
        time=new ArrayList<>();

        statusref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                image.clear();
                uid.clear();
                delete.clear();
                caption.clear();
                time.clear();

                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    StatusModal statusModal = snapshot1.getValue(StatusModal.class);

                    long timecurrent = System.currentTimeMillis();

                    image.add(statusModal.getImage());
                    uid.add(statusModal.getUid());
                    time.add(statusModal.getTime());
                    delete.add(statusModal.getDelete());
                    caption.add(statusModal.getCaption());
                }

                // Set up views here outside the loop
                storiesProgressView.setStoriesCount(image.size());
                storiesProgressView.setStoriesListener(ShowStatus.this);
                storiesProgressView.startStories(counter);
                storiesProgressView.setStoryDuration(5000L);

                // ... (rest of your code)

                // Fetch and display the first user information
                fetchuserinfo(uid.get(counter));

                // Play the first status (image or video)
                playStatus();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

    }

    private void playStatus() {
        // Set up views based on the current status
        s_iv.setVisibility(View.VISIBLE);
        captionTV.setText(caption.get(counter));

        if (image.get(counter).endsWith(".mp4")) {
            // Video content
            s_iv.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);

            // Set the video URI
            Uri videoUri = Uri.parse(image.get(counter));

            // Set video caption and time
            captionTV.setText(caption.get(counter));
            timetv.setText(time.get(counter));

            // Set up the VideoView
            videoView.setVideoURI(videoUri);

            // Set up an OnPreparedListener to start playback when the video is prepared
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // Start video playback
                    videoView.start();
                    mp.start();
                }
            });
        } else {
            // Image content
            videoView.setVisibility(View.GONE);
            Glide.with(getApplicationContext()).load(image.get(counter)).into(s_iv);
            timetv.setText(time.get(counter));
        }
    }

    private void fetchuserinfo(String s) {
        documentReference=db.collection("Users").document(s);

        documentReference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String name = document.getString("name");
                                String url = document.getString("url");

                                tvname.setText(name); // Set username

                                if (!url.isEmpty()) {
                                    // Load profile image using Glide
                                    Glide.with(getApplicationContext()).load(url).into(useriv);
                                }
                            } else {
                                // Document doesn't exist
                                Toast.makeText(ShowStatus.this, "No profile data", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Exception exception = task.getException();
                            if (exception instanceof FirebaseFirestoreException) {
                                FirebaseFirestoreException firestoreException = (FirebaseFirestoreException) exception;
                                Log.e(TAG, "Firestore exception: " + firestoreException.getCode());
                            }

                            if (!isNetworkAvailable()) {
                                Toast.makeText(ShowStatus.this, "No internet connection", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    private boolean isNetworkAvailable() {
                        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
                    }
                });
    }
    @Override
    public void onNext() {
        if ((counter + 1) < image.size()) {
            // Increment the counter first
            counter++;

            // Check if the counter is within the bounds
            if (counter < image.size()) {
                // Load the next status
                playStatus();

                // Load user information for the next status
                fetchuserinfo(uid.get(counter));
            }
        }
//        if ((counter + 1) < image.size()) {
//            // Increment the counter first
//            counter++;
//
//            // Check if the counter is within the bounds
//            if (counter < image.size()) {
//                // Load the next status
//                Glide.with(getApplicationContext()).load(image.get(counter)).into(s_iv);
//                captionTV.setText(caption.get(counter));
//
//                // Load user information for the next status
//                fetchuserinfo(uid.get(counter));
//            }
//        }
    }

    @Override
    public void onPrev() {
        if ((counter - 1) >= 0) {
            // Decrement the counter first
            counter--;

            // Check if the counter is within the bounds
            if (counter < image.size()) {
                // Load the previous status
                playStatus();

                // Load user information for the previous status
                fetchuserinfo(uid.get(counter));
            }
        }
//        if ((counter - 1) >= 0) {
//            // Decrement the counter first
//            counter--;
//
//            // Check if the counter is within the bounds
//            if (counter < image.size()) {
//                // Load the previous status
//                Glide.with(getApplicationContext()).load(image.get(counter)).into(s_iv);
//                captionTV.setText(caption.get(counter));
//
//                // Load user information for the previous status
//                fetchuserinfo(uid.get(counter));
//                playStatus();
//            }
//        }
    }

    @Override
    public void onComplete() {
        finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }
}