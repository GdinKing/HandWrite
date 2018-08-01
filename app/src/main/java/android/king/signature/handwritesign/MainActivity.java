package android.king.signature.handwritesign;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.king.signature.config.PenConfig;
import android.king.signature.ui.GridPaintActivity;
import android.king.signature.ui.PaintActivity;
import android.king.signature.view.GridPaintView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openBlank(View view) {

        Intent intent = new Intent(this, PaintActivity.class);


        intent.putExtra("background", Color.WHITE);

//        intent.putExtra("bitmapWidth", 800);
//        intent.putExtra("bitmapHeight", 800);
//        intent.putExtra("widthRate", 0.8f);
//        intent.putExtra("heightRate", 0.8f);
        intent.putExtra("sealName", "张三");
//        intent.putExtra("format", "PNG");
        intent.putExtra("crop", false);
        intent.putExtra("showSealTime", true);

//        intent.putExtra("image", imagePath);

        startActivityForResult(intent, 100);
    }


    public void openGrid(View view) {

        Intent intent = new Intent(this, GridPaintActivity.class);


        intent.putExtra("background", Color.WHITE);
        intent.putExtra("sealName", "张三");
//        intent.putExtra("format", "PNG");
        intent.putExtra("crop", false);
        intent.putExtra("showSealTime", true);
        intent.putExtra("showSealTime", true);
        intent.putExtra("showSealTime", true);

        startActivityForResult(intent, 100);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            String savePath = "file://" + data.getStringExtra(PenConfig.SAVE_PATH);

        }
    }

}
