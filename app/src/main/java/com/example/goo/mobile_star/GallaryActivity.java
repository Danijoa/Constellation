package com.example.goo.mobile_star;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class GallaryActivity extends AppCompatActivity {

    private ImageView image;
    String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gal);

        image = (ImageView)findViewById(R.id.iv_selectedimg);
        Intent intent = getIntent();
        imagePath = intent.getExtras().getString("IMGpath");

        Bitmap loadedBitmap = BitmapFactory.decodeFile(imagePath);
        image.setImageBitmap(loadedBitmap);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.menu_share:
                share();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void share(){
                try {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(imagePath));
                    intent.setPackage("com.kakao.talk");
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {

                }
    }


}
