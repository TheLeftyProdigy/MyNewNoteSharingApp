package com.example.anand.mynotesharingapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Anand on 26-06-2017.
 */

public class AdapterForOwnPDF extends RecyclerView.Adapter<AdapterForOwnPDF.ViewHolder> {

    private Context context;
    private List<FileDetails> uploads;

    private FirebaseStorage mFirebaseStorage;
    private String imguri;

    private String TAG="Debug";




    public AdapterForOwnPDF(Context context, List<FileDetails> uploads) {
        this.uploads = uploads;
        this.context = context;
    }

    @Override
    public AdapterForOwnPDF.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycleritem_for_ownpdf, parent, false);
        AdapterForOwnPDF.ViewHolder viewHolder = new AdapterForOwnPDF.ViewHolder(v);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(AdapterForOwnPDF.ViewHolder holder, final int position) {
        final FileDetails upload = uploads.get(position);

        imguri=upload.getImageURI();
        mFirebaseStorage=FirebaseStorage.getInstance();


        holder.textViewTitle.setText(upload.getTitle());
        holder.textViewSubject.setText(upload.getSubject());
        holder.textViewTag.setText(upload.getTag());

        holder.textViewSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser(context,upload.getImageURI());
            }
        });

        holder.textViewTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser(context,upload.getImageURI());
            }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

        public ViewHolder(View itemView) {
            super(itemView);

            textViewTitle = (TextView) itemView.findViewById(R.id.textViewTitle3);
            textViewSubject=(TextView) itemView.findViewById(R.id.textViewSubject3);
            textViewTag=(TextView) itemView.findViewById(R.id.textViewTag3);
            deleteButton=(Button)itemView.findViewById(R.id.deletebutton3);


        }
    }


    private void showPopup(View view, final int position) {
        // pass the imageview id
        View menuItemView = view.findViewById(R.id.imageView);
        PopupMenu popup = new PopupMenu(getApplicationContext(), menuItemView);
        MenuInflater inflate = popup.getMenuInflater();
        inflate.inflate(R.menu.holdmenu, popup.getMenu());
        Log.d("ok","position -- "+position);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.download:
                        Intent i=new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(imguri));
                        context.startActivity(i);
                        break;
                    case R.id.delete:
                        // do what you need .
                        break;
                    default:
                        return false;
                }
                return false;
            }
        });
        popup.show();
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

