package android.king.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.king.signature.GridPaintActivity;
import android.king.signature.PaintActivity;
import android.king.signature.config.PenConfig;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private boolean isPermissionOk = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(PermissionChecker.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            isPermissionOk = false;
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    100);
        }else{
            isPermissionOk = true;
        }

        //主题颜色配置
        PenConfig.THEME_COLOR = Color.parseColor("#0c53ab");
    }

    public void openBlank(View view) {
        if(!isPermissionOk){
            return;
        }
        Intent intent = new Intent(this, PaintActivity.class);

        intent.putExtra("background", Color.WHITE);//画布背景色，默认透明，也是最终生成图片的背景

//        intent.putExtra("width", 800); //画布宽度
//        intent.putExtra("height", 800);//画布高度
        intent.putExtra("crop", false);   //裁剪
        intent.putExtra("sealName", "张三"); //印章名字
        intent.putExtra("sealLabel", "2018-12-21"); //印章标签
        intent.putExtra("format", PenConfig.FORMAT_PNG); //图片格式
//        intent.putExtra("image", imagePath); //初始图片

        startActivityForResult(intent, 100);
    }


    public void openGrid(View view) {
        if(!isPermissionOk){
            return;
        }
        Intent intent = new Intent(this, GridPaintActivity.class);
        intent.putExtra("background", Color.WHITE);
        intent.putExtra("crop", true);
        intent.putExtra("sealName", "张三");
        intent.putExtra("sealLabel", "2018-12-21");
        intent.putExtra("fontSize", 50);
        intent.putExtra("format", PenConfig.FORMAT_PNG);
        intent.putExtra("lineLength", 6);
        startActivityForResult(intent, 100);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            String savePath = data.getStringExtra(PenConfig.SAVE_PATH);
            Log.i("king",savePath);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 100) {
            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户同意使用write
                isPermissionOk = true;
            } else {
                //用户不同意，自行处理即可
                finish();
            }
        }
    }
}
