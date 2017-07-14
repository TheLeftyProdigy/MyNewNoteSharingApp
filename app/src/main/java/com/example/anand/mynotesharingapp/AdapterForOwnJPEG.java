package com.example.anand.mynotesharingapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

/**
 * Created by Anand on 26-06-2017.
 */

public class AdapterForOwnJPEG extends RecyclerView.Adapter<AdapterForOwnJPEG.ViewHolder> {

    private Context context;
    private List<FileDetails> uploads;

    private FirebaseStorage mFirebaseStorage;
    private String imguri;

    private String TAG="Debug";




    public AdapterForOwnJPEG(Context context, List<FileDetails> uploads) {
        this.uploads = uploads;
        this.context = context;

    }

    @Override
    public AdapterForOwnJPEG.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycleritem_for_ownjpeg, parent, false);
        AdapterForOwnJPEG.ViewHolder viewHolder = new AdapterForOwnJPEG.ViewHolder(v);

        return viewHolder;

    }

    @Override
    public void onBindViewHolder(AdapterForOwnJPEG.ViewHolder holder, final int position) {
        final FileDetails upload = uploads.get(position);

        imguri=upload.getImageURI();
        mFirebaseStorage=FirebaseStorage.getInstance();



        holder.textViewTitle.setText(upload.getTitle());
        holder.textViewSubject.setText(upload.getSubject());
        holder.textViewTag.setText(upload.getTags());
        Glide.with(context).load(upload.getImageURI()).into(holder.imageView);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser(context,upload.getImageURI());
            }
        });

        holder.downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser(context,upload.getImageURI());
            }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());

                builder.setTitle("Confirm");
                builder.setMessage("Are you sure?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog

                        StorageReference photoRef = mFirebaseStorage.getReferenceFromUrl(upload.getImageURI());

                        photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // File deleted successfully
                                Log.d(TAG, "onSuccess: deleted file");

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Uh-oh, an error occurred!
                                Log.d(TAG, "onFailure: did not delete file");
                            }
                        });


                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("userinfo");
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot Snapshot: dataSnapshot.getChildren()) {

                                    FileDetails currupload = Snapshot.getValue(FileDetails.class);

                                    if(currupload.getImageURI().equals(upload.getImageURI()))
                                        Snapshot.getRef().removeValue();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e(TAG, "onCancelled", databaseError.toException());
                            }
                        });

                        //removes the item after deleting
                        removeItem(position);

                        dialog.dismiss();
                        uploads.clear();
                        notifyDataSetChanged();




                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing

                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();





            }
        });


    }

    public void removeItem(int position) {
        uploads.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return uploads.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewTitle;
        public ImageView imageView;
        public TextView textViewSubject;
        public TextView textViewTag;
        public Button deleteButton;
        public Button downloadButton;
        public Typeface typeFace;

        public ViewHolder(View itemView) {
            super(itemView);

            textViewTitle = (TextView) itemView.findViewById(R.id.textViewTitle1);
            imageView = (ImageView) itemView.findViewById(R.id.imageView1);
            textViewSubject=(TextView) itemView.findViewById(R.id.textViewSubject1);
            textViewTag=(TextView) itemView.findViewById(R.id.textViewTag1);
            deleteButton=(Button)itemView.findViewById(R.id.deletebutton1);
            downloadButton=(Button)itemView.findViewById(R.id.downloadbutton1);
            typeFace=Typeface.createFromAsset(itemView.getContext().getAssets(),"fonts/robotoslabreg.ttf");
            textViewTitle.setTypeface(typeFace);
            textViewSubject.setTypeface(typeFace);
            textViewTag.setTypeface(typeFace);

        }
    }

    public static void openBrowser(final Context context, String url)
    {
        if (!url.startsWith("http://") && !url.startsWith("https://"))
        {
            url = "http://" + url;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(Intent.createChooser(intent, "Choose browser"));
    }



}

