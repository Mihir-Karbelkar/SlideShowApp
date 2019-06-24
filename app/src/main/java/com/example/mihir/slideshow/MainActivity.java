package com.example.mihir.slideshow;


import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText search, num;
    Button click;
    ProgressDialog mProgressDialog;
    ViewFlipper flipper;
    int clicked = 0;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{ContextCompat.getColor(this, R.color.color1),
                        ContextCompat.getColor(this, R.color.color2)});
        num = (EditText) findViewById(R.id.num);
        search = (EditText) findViewById(R.id.search);
        click = (Button) findViewById(R.id.click);
        flipper = (ViewFlipper) findViewById(R.id.flipper);
        click.setOnClickListener(this);
        flipper.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.click) {
            flipper.removeAllViews();
            new DownloadImage().execute(search.getText().toString());
        } else if (v.getId() == R.id.flipper) {
            if (clicked == 0) {
                flipper.stopFlipping();
                clicked = 1;
            } else {
                flipper.startFlipping();
                clicked = 0;
            }
        }

    }


    private class DownloadImage extends AsyncTask<String, ArrayList<Bitmap>, ArrayList<Bitmap>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Create a progressdialog
            mProgressDialog = new ProgressDialog(MainActivity.this);
            // Set progressdialog title
            mProgressDialog.setTitle("Download Image Tutorial");
            // Set progressdialog message
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            // Show progressdialog
            mProgressDialog.show();
        }

        @Override
        protected ArrayList<Bitmap> doInBackground(String... URL) {
            int limit = Integer.parseInt(num.getText().toString()) - 1;
            String quer = "";
            ArrayList<Bitmap> bits = new ArrayList<Bitmap>();

            for (int i = 0, len = URL[0].length(); i < len; i++) {
                if (URL[0].charAt(i) == 32)
                    quer += "%20";
                else
                    quer += URL[0].charAt(i);

            }

            String location = "https://www.bing.com/images/search?q=" + quer + "&qs=n&form=QBILPG&sp=-1&pq=&sc=1-0&sk=&cvid=614998763FC2470EA5F3AE69AEB0D3AF";

            try {
                Log.d("TITLE", location);
                Connection.Response response = Jsoup.connect(location)
                        .ignoreContentType(true)
                        .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                        .referrer("http://www.google.com")
                        .timeout(12000)
                        .followRedirects(true)
                        .execute();
                Document doc = response.parse();
                Log.d("TITLE:", doc.title());
                Elements links = doc.select("a.iusc");
                Bitmap bitmap = null;

                int count = 0;
                for (Element e : links) {


                    String l = e.attr("m");
                    String[] m = l.substring(1, l.length() - 1).split(",");
                    String val = "";
                    for (int _ = 0, leng = m.length; _ < leng; _++) {
                        Log.d("DDM:", "doInBackground: " + m[_]);

                        if (m[_].contains("murl")) {
                            val = m[_].substring(m[_].indexOf("http"), m[_].length() - 1);
                            String imageURL = val;
                            Log.d("IMAGE:", imageURL);

                            try {
                                // Download Image from URL
                                URL url = new URL(imageURL);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setDoInput(true);
                                connection.connect();
                                InputStream input = connection.getInputStream();
                                bitmap = BitmapFactory.decodeStream(input);

                                bits.add(bitmap);

                            } catch (Exception e1) {
                                Log.e("BIG BIG BIG", "" + e1.getMessage());


                            }


                        }


                    }


                    count++;

                    if (count == limit)
                        break;
                }
            } catch (IOException e) {
            }
            return bits;

        }

        @Override
        protected void onPostExecute(ArrayList<Bitmap> result) {
            // Set the bitmap into ImageView

            for (int _ = 0, len = result.size(); _ < len; _++) {
                ImageView image = new ImageView(getApplicationContext());
                image.setImageBitmap(result.get(_));
                flipper.addView(image);


            }
            flipper.startFlipping();
            mProgressDialog.dismiss();

        }
    }


}







