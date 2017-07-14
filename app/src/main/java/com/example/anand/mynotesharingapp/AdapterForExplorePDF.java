package com.example.anand.mynotesharingapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Created by Anand on 27-06-2017.
 */

public class AdapterForExplorePDF extends RecyclerView.Adapter<AdapterForExplorePDF.ViewHolder> {
    private Context context;
    private List<FileDetails> uploads;

    public File filedir;

    ProgressDialog pDialog;

    View view;


    public AdapterForExplorePDF(Context context, List<FileDetails> uploads) {
        this.uploads = uploads;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycleritem_for_explorepdf, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final FileDetails upload = uploads.get(position);

        holder.textViewTitle.setText(upload.getTitle());
        holder.textViewSubject.setText(upload.getSubject());
        holder.textViewTag.setText(upload.getTags());
        holder.textViewTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser(context,upload.getImageURI());
            }
        });

        holder.textViewType.setText("Type: "+upload.getType());

        holder.textViewSubject.setOnClickListener(new View.OnClickListener() {
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

        Log.d("what","the type is "+upload.getType());

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









    }

    public String getFileExtension(Uri uri) {
        ContentResolver cR = context.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    @Override
    public int getItemCount() {
        return uploads.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewTitle;
        public TextView textViewSubject;
        public TextView textViewTag;
        public Button downloadButton;
        public Typeface typeFace;
        public Button previewButton;
        public TextView textViewType;

        public ViewHolder(View itemView) {
            super(itemView);


            textViewTitle = (TextView) itemView.findViewById(R.id.textViewTitle2);
            textViewSubject=(TextView) itemView.findViewById(R.id.textViewSubject2);
            textViewTag=(TextView) itemView.findViewById(R.id.textViewTag2);
            textViewType=(TextView)itemView.findViewById(R.id.textViewType2);
            downloadButton=(Button)itemView.findViewById(R.id.downloadbutton2);
            previewButton=(Button)itemView.findViewById(R.id.previewbutton2);
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
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(intent, "Choose browser"));
    }


    private void downloadFile(String childname,final ViewHolder holder) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(childname);
        StorageReference islandRef = storageRef;

        File storagePath = new File(Environment.getExternalStorageDirectory()+File.separator+"Note Sharing App");

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

        filedir=localFile;

        islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d("firebase ", ";local tem file created" + localFile.toString());
                Log.d("Load","LOADING PDF");

                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.web_dialog);

                final PDFView pdfView=(PDFView)dialog.findViewById(R.id.pdfView1) ;

                pdfView.fromFile(filedir)
                        .pages(0, 1, 2, 3, 4)
                        .enableDoubletap(true)
                        .swipeHorizontal(false)
                        .enableSwipe(true)
                        .scrollHandle(new DefaultScrollHandle(view.getContext()))
                        .onRender(new OnRenderListener() {
                            @Override
                            public void onInitiallyRendered(int pages, float pageWidth, float pageHeight) {
                                pdfView.documentFitsView();// optionally pass page number
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








}
