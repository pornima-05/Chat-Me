package com.example.testapplication2;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.testapplication2.Model.UserStatusModal;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StatusVH extends RecyclerView.ViewHolder {
    ImageView status_iv;
    TextView nametv,timetv;
    String urlresult,deleteresult,timeresult;
    FirebaseDatabase database=FirebaseDatabase.getInstance();
    DatabaseReference statusref,laststatus;
    LinearLayout ll_ss;

    public StatusVH(@NonNull View itemView) {
        super(itemView);
        ll_ss = itemView.findViewById(R.id.ll_status_item);
        status_iv = itemView.findViewById(R.id.iv_mystatus_item);
        nametv = itemView.findViewById(R.id.namestatus_tv_item);
        timetv = itemView.findViewById(R.id.time_tvstatus_item);
    }
    public void bindStatus(UserStatusModal status) {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserUid = currentUser != null ? currentUser.getUid() : "";

        // Check if the status belongs to the current user
        if (!status.getUid().equals(currentUserUid)) {
            // Bind data to views
            Glide.with(itemView.getContext()).load(status.getImage()).into(status_iv);
            timetv.setText(status.getTime());
            nametv.setText(status.getUserName());

            // Set the layout to be visible
            ll_ss.setVisibility(View.VISIBLE);
        } else {
            // If the status belongs to the current user, hide the layout
            ll_ss.setVisibility(View.GONE);
        }
    }

    public void fetchStatus(String name, String url, String uid, String image){


        statusref=database.getReference("Status");
        laststatus=database.getReference("laststatus");

//        ll_ss=itemView.findViewById(R.id.ll_status_item);
//        status_iv=itemView.findViewById(R.id.iv_mystatus_item);
//        nametv=itemView.findViewById(R.id.namestatus_tv_item);
//        timetv=itemView.findViewById(R.id.time_tvstatus_item);

        laststatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(uid)){
                    statusref.child(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                urlresult=snapshot.child("image").getValue().toString();
                                timeresult=snapshot.child("time").getValue().toString();
                                deleteresult=snapshot.child("delete").getValue().toString();


                                Glide.with(itemView.getContext()).load(url).into(status_iv);
                                timetv.setText(timeresult);
                                status_iv.setPadding(0,0,0,0);
                                nametv.setText(name);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }else{
                    ll_ss.setVisibility(View.GONE);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}
