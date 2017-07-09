package com.example.anand.mynotesharingapp;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by Anand on 27-06-2017.
 */

public class AdapterForExplorePDF extends RecyclerView.Adapter<AdapterForExplorePDF.ViewHolder> {
    private Context context;
    private List<FileDetails> uploads;




    public AdapterForExplorePDF(Context context, List<FileDetails> uploads) {
        this.uploads = uploads;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycleritem_for_explorepdf, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
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




        String URII=upload.getImageURI();
        String b4=URII.substring(0,URII.indexOf("?"));

        String ext=(b4.substring(b4.lastIndexOf(".")+1));

        Log.d("fileuri",b4);
        Log.d("ext",ext);

        Uri temp=Uri.parse(b4);


        //Log.d("extns","The extension is "+getFileExtension(temp));

        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, "temp.pdf");

        try {
            URL url = new URL(URII);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//            urlConnection.setRequestMethod("GET");
//            urlConnection.setDoOutput(true);
            urlConnection.connect();



            FileOutputStream fileOutput = new FileOutputStream(file);
            InputStream inputStream = urlConnection.getInputStream();

            byte[] buffer = new byte[1024];
            int bufferLength = 0;

            while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                fileOutput.write(buffer, 0, bufferLength);
            }
            fileOutput.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



        if("pdf"==ext) {

            Log.d("Load","LOADING PDF");
            holder.pdfView.fromFile(file)
                    .pages(0,1,2,3,4,5)
                    .enableDoubletap(true)
                    .enableSwipe(true)
                    .load();

        }




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
        public PDFView pdfView;

        public ViewHolder(View itemView) {
            super(itemView);

            textViewTitle = (TextView) itemView.findViewById(R.id.textViewTitle2);
            textViewSubject=(TextView) itemView.findViewById(R.id.textViewSubject2);
            textViewTag=(TextView) itemView.findViewById(R.id.textViewTag2);
            downloadButton=(Button)itemView.findViewById(R.id.downloadbutton2);
            pdfView=(PDFView)itemView.findViewById(R.id.pdfView);
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

}
