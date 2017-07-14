package com.example.anand.mynotesharingapp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
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

    View view;

    public File filedir;



    public AdapterForOwnPDF(Context context, List<FileDetails> uploads) {
        this.uploads = uploads;
        this.context = context;
    }

    @Override
    public AdapterForOwnPDF.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycleritem_for_ownpdf, parent, false);
        AdapterForOwnPDF.ViewHolder viewHolder = new AdapterForOwnPDF.ViewHolder(view);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(final AdapterForOwnPDF.ViewHolder holder, final int position) {
        final FileDetails upload = uploads.get(position);

        imguri=upload.getImageURI();
        mFirebaseStorage=FirebaseStorage.getInstance();


        holder.textViewTitle.setText(upload.getTitle());
        holder.textViewSubject.setText(upload.getSubject());
        holder.textViewTag.setText(upload.getTags());


        holder.textViewType.setText("Type: "+upload.getType());

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

        holder.downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser(context,upload.getImageURI());
            }
        });



        holder.previewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(upload.getType().equals("pdf"))

                {
                    String URII = upload.getImageURI();
                    String b4 = URII.substring(0, URII.indexOf("?"));

                    String ext = (b4.substring(b4.lastIndexOf(".") + 1));

                    Log.d("fileuri", b4);
                    Log.d("ext", ext);

                    Uri temp = Uri.parse(b4);

                    downloadFile(URII, holder);

                    Log.d("firebase ", "File Dir is " + filedir);

                    Boolean b = filedir.exists();
                    if (b == true) {
                        System.out.println("File exists.");
                    } else System.out.println("File doesnt exist.");

                }

                else
                {
                    Toast.makeText(context, "Preview Not Available", Toast.LENGTH_LONG).show();
                }

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

    private void downloadFile(String childname,final AdapterForOwnPDF.ViewHolder holder) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(childname);
        StorageReference islandRef = storageRef;

        File storagePath = new File(Environment.getExternalStorageDirectory() + File.separator + "Note Sharing App");

        Log.d("storagepath", storagePath.toString());
// Create direcorty if not exists
        if (!storagePath.exists()) {
            storagePath.mkdirs();
        }

        final File localFile = new File(storagePath, "temp.pdf");
        Log.d("localfile", localFile.toString());

        try {
            localFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        filedir = localFile;

        islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d("firebase ", ";local tem file created" + localFile.toString());
                Log.d("Load", "LOADING PDF");

                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.web_dialog);

                final PDFView pdfView = (PDFView) dialog.findViewById(R.id.pdfView1);

                pdfView.fromFile(filedir)
                        .pages(0, 1, 2, 3, 4)
                        .enableDoubletap(true)
                        .swipeHorizontal(true)
                        .enableSwipe(true)
                        .scrollHandle(new DefaultScrollHandle(view.getContext()))
                        .onRender(new OnRenderListener() {
                            @Override
                            public void onInitiallyRendered(int pages, float pageWidth, float pageHeight) {
                                pdfView.fitToWidth(); // optionally pass page number
                            }
                        })
                        .load();

                dialog.setCancelable(true);
                dialog.setTitle("Preview");
                dialog.show();

                //  updateDb(timestamp,localFile.toString(),position);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("firebase ", ";local tem file not created  created " + exception.toString());
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
        public TextView textViewType;

        public Button previewButton;


        public ViewHolder(View itemView) {
            super(itemView);


            textViewTitle = (TextView) itemView.findViewById(R.id.textViewTitle3);
            textViewSubject=(TextView) itemView.findViewById(R.id.textViewSubject3);
            textViewTag=(TextView) itemView.findViewById(R.id.textViewTag3);
            textViewType=(TextView)itemView.findViewById(R.id.textViewType3);
            deleteButton=(Button)itemView.findViewById(R.id.deletebutton3);
            downloadButton=(Button)itemView.findViewById(R.id.downloadbutton3);
            previewButton=(Button)itemView.findViewById(R.id.previewbutton3);
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

