package com.example.testapplication2.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.example.testapplication2.Model.Chat;
import com.example.testapplication2.Model.MediaPlayerClass;
import com.example.testapplication2.R;
import com.example.testapplication2.VideoPlayingActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder>{

    public static final int MSG_TYPE_LEFT=0;
    public static final int MSG_TYPE_RIGHT=1;
    private int playingPosition=-1;


    private Context mContext;
    private List<Chat> mChat;
    private String imageurl;

    FirebaseUser fuser;
    //share media
    MediaPlayer mediaPlayer;
    public boolean check;



    public  MessageAdapter(Context mContext,List<Chat> mChat,String imageurl){
        this.mChat=mChat;
        this.mContext=mContext;
        this.imageurl=imageurl;
        mediaPlayer = MediaPlayerClass.getMediaPlayer();

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
        }else{
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Chat chat=mChat.get(position);
        holder.show_message.setText(chat.getMessage());

        holder.mediaPlayer = new MediaPlayer();

        if (imageurl.equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(imageurl).into(holder.profile_image);
        }


        //share media
        if (chat.getType().equals("text")) {
            holder.show_message.setText(chat.getMessage());
            holder.cardView.setVisibility(View.GONE);
            holder.cardView1.setVisibility(View.GONE);
            holder.cardRecord.setVisibility(View.GONE);
            holder.videoView.setVisibility(View.GONE);
        }
        else if (chat.getType().equals("image")) {
            holder.show_message.setVisibility(View.GONE);
            holder.cardView.setVisibility(View.VISIBLE);
            holder.cardView1.setVisibility(View.GONE);
            holder.cardRecord.setVisibility(View.GONE);
            holder.videoView.setVisibility(View.GONE);
            Glide.with(mContext).load(mChat.get(position).getMessage()).into(holder.imageView);

        }else if (chat.getType().equals("Audio")) {
            holder.show_message.setVisibility(View.GONE);
            holder.cardView.setVisibility(View.GONE);
            holder.cardView1.setVisibility(View.VISIBLE);
            holder.cardRecord.setVisibility(View.GONE);
            holder.videoView.setVisibility(View.GONE);
            holder.mediaPlayer.reset();
            holder.playingaudio.setImageResource(R.drawable.playaudio);
            holder.playingaudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (playingPosition == position) {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                            holder.playingaudio.setImageResource(R.drawable.playaudio);
                        } else {
                            mediaPlayer.start();
                            holder.playingaudio.setImageResource(R.drawable.paus);
                        }
                    } else {
                        // Stop playback for the previous item
                        stopPlaybackForPreviousItem();

                        try {
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(mChat.get(position).getMessage());
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            playingPosition = position;
                            holder.playingaudio.setImageResource(R.drawable.paus);
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    holder.playingaudio.setImageResource(R.drawable.playaudio);
                                    playingPosition = -1;
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            });
        } else if (chat.getType().equals("Video")) {
            holder.show_message.setVisibility(View.GONE);
            holder.cardView.setVisibility(View.GONE);
            holder.cardView1.setVisibility(View.GONE);
            holder.cardRecord.setVisibility(View.GONE);
            holder.videoView.setVisibility(View.VISIBLE);
            Glide.with(mContext)
                    .asBitmap()
                    .load(mChat.get(position).getMessage())
                    .into(holder.vImg);

//            holder.videoView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view1) {
//
//                    Intent intent = new Intent(mContext, VideoPlayingActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    intent.putExtra("videouri", String.valueOf(mChat.get(position).getMessage()));
//                    mContext.startActivity(intent);
//
//                }
//            });
            holder.videoPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, VideoPlayingActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("videouri", String.valueOf(mChat.get(position).getMessage()));
                    mContext.startActivity(intent);
                }
            });
        }else if (chat.getType().equals("Document")) {
            holder.show_message.setVisibility(View.GONE);
            holder.cardView.setVisibility(View.GONE);
            holder.cardView1.setVisibility(View.GONE);
            holder.videoView.setVisibility(View.GONE);
            holder.cardRecord.setVisibility(View.GONE);
            holder.cardDoc.setVisibility(View.VISIBLE);
            //Glide.with(mContext).load(R.drawable.document).into(holder.documentImageView);

            // Set an onClickListener to handle document opening or downloading
            holder.cardDoc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Assuming the document URL is stored in the 'getMessage()' method
                    String documentUrl = mChat.get(position).getMessage();

                    // Create an Intent to view the document using an external app
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(documentUrl), "application/pdf");

                    // Use Intent.createChooser to let the user choose the app
                    Intent chooserIntent = Intent.createChooser(intent, "Open with");
                    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Add this line

                    // Start the activity
                    mContext.startActivity(chooserIntent);
                    }
            });
        }
        else if(chat.getType().equals("Record")){
           holder.show_message.setVisibility(View.GONE);
            holder.cardView.setVisibility(View.GONE);
            holder.cardView1.setVisibility(View.GONE);
            holder.videoView.setVisibility(View.GONE);
            holder.cardRecord.setVisibility(View.VISIBLE);
            holder.playRecord.setImageResource(R.drawable.playaudio);

            if (playingPosition == position) {
                // If it's the playing position, update the UI accordingly
                if (mediaPlayer.isPlaying()) {
                    holder.playRecord.setImageResource(R.drawable.paus);

                } else {
                    holder.playRecord.setImageResource(R.drawable.playaudio);
                }
            } else {
                // If it's not the playing position, show the play icon
                holder.playRecord.setImageResource(R.drawable.playaudio);
            }


            holder.playRecord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    if (playingPosition == position) {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                            holder.playRecord.setImageResource(R.drawable.playaudio);
                            holder.time.setText(" ");
                        }
                    } else {
                        // Stop playback for the previous item
                        stopPlaybackForPreviousItem();

                        try {

                            mediaPlayer.setDataSource(mChat.get(position).getMessage());
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            playingPosition = position;
                            holder.playRecord.setImageResource(R.drawable.paus);
                            updateRecordTime(holder.time, mediaPlayer.getDuration());

                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    holder.playRecord.setImageResource(R.drawable.playaudio);
                                    playingPosition = -1;
                                    holder.time.setVisibility(View.GONE);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }


                }
                private void updateRecordTime(TextView recordTimeTextView, int totalDuration) {

                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        int currentPosition = mediaPlayer.getCurrentPosition();
                        int minutes = (currentPosition / 1000) / 60;
                        int seconds = (currentPosition / 1000) % 60;

                        int totalMinutes = (totalDuration / 1000) / 60;
                        int totalSeconds = (totalDuration / 1000) % 60;
                        String formattedTime = String.format("%d:%02d / %d:%02d", minutes, seconds, totalMinutes, totalSeconds);

                        // Update the UI on the main thread
                        if(playingPosition==position){
                           /* recordTimeTextView.post(() ->*/ recordTimeTextView.setText(formattedTime);
                           recordTimeTextView.setVisibility(View.VISIBLE);
                            recordTimeTextView.postDelayed(() -> updateRecordTime(recordTimeTextView, totalDuration), 1000);


                        }else
                        {
                            recordTimeTextView.setVisibility(View.GONE);
                        }


                        // Recursive call to keep updating

                    }
                }
            });
        }
        //share media

        if (position==mChat.size()-1){
            if (chat.isIsseen()){
                holder.txt_seen.setText("Seen");
            }else {
                holder.txt_seen.setText("Delivered");
            }
        }else {
            holder.txt_seen.setVisibility(View.GONE);
        }

    }

    private void stopPlaybackForPreviousItem() {
        if (playingPosition != -1) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            notifyItemChanged(playingPosition);
        }
//        if (playingPosition != -1 && mediaPlayer.isPlaying()) {
//            mediaPlayer.stop();
//            mediaPlayer.reset();
//            notifyItemChanged(playingPosition);
//        }
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView show_message;
        public ImageView profile_image;
        public TextView txt_seen;

        //share media
        ImageView imageView, playingaudio,vImg;

        CardView cardView;
        CardView cardView1;
        CardView videoView;
        CardView cardDoc;
        CardView cardRecord;
        ImageView videoPlay;
        ImageView playRecord;
        TextView time;
        MediaPlayer mediaPlayer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            show_message=itemView.findViewById(R.id.show_message);
            profile_image=itemView.findViewById(R.id.profile_image);
            txt_seen=itemView.findViewById(R.id.txt_seen);

            //share media
            imageView = itemView.findViewById(R.id.imageviewsendreceive);
            cardView = itemView.findViewById(R.id.cardimage);
            cardView1 = itemView.findViewById(R.id.cardaudio);
            playingaudio = itemView.findViewById(R.id.playingaudio);
            videoPlay=itemView.findViewById(R.id.playvideo);

            View videoLayout = itemView.findViewById(R.id.cardvideo);
            vImg = videoLayout.findViewById(R.id.videoviewtextview);

            videoView = itemView.findViewById(R.id.cardvideo);

            cardRecord=itemView.findViewById(R.id.cardRacord);
            playRecord=itemView.findViewById(R.id.playingracord);
            cardDoc=itemView.findViewById(R.id.cardDoc);

            time=itemView.findViewById(R.id.racordtiming);


        }

    }

    @Override
    public int getItemViewType(int position) {
        fuser= FirebaseAuth.getInstance().getCurrentUser();

        if (mChat.get(position).getSender().equals(fuser.getUid())){
            return MSG_TYPE_RIGHT;
        }else {
            return  MSG_TYPE_LEFT;
        }
    }
    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.mediaPlayer != null) {
            holder.mediaPlayer.release();
            holder.mediaPlayer = null;
        }
    }

}
