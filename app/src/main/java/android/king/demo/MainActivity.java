package android.king.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 使用示例
 */
public class MainActivity extends AppCompatActivity {

    private TextView tvResult;
    private ImageView ivShow;

    private boolean isPermissionOk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            isPermissionOk = false;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    100);
        } else {
            isPermissionOk = true;
        }
        initView();

        //主题颜色配置
        PenConfig.THEME_COLOR = Color.parseColor("#0c53ab");
    }

    private void initView() {
        ivShow = findViewById(R.id.iv_show);
        tvResult = findViewById(R.id.tv_result);
    }

    /**
     * 空白签批
     * @param view
     */
    public void openBlank(View view) {
        if (!isPermissionOk) {
            return;
        }
        Intent intent = new Intent(this, PaintActivity.class);

//        intent.putExtra("background", Color.WHITE);//画布背景色，默认透明，也是最终生成图片的背景色

//        intent.putExtra("width", 800); //画布宽度，最大值3000，默认占满布局
//        intent.putExtra("height", 800);//画布高度，最大值3000，默认占满布局
        intent.putExtra("crop", false);   //最终的图片是否只截取文字区域
        intent.putExtra("format", PenConfig.FORMAT_PNG); //图片格式
//        intent.putExtra("image", imagePath); //初始图片

        startActivityForResult(intent, 100);
    }

    /**
     * 田字格签批
     * @param view
     */
    public void openGrid(View view) {
        if (!isPermissionOk) {
            return;
        }
        Intent intent = new Intent(this, GridPaintActivity.class);
        intent.putExtra("background", Color.WHITE);
        intent.putExtra("crop", true);
        intent.putExtra("fontSize", 50);  //手写字体大小
        intent.putExtra("format", PenConfig.FORMAT_PNG);
        intent.putExtra("lineLength", 10);   //每行显示字数（超出屏幕支持横向滚动）
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            String savePath = data.getStringExtra(PenConfig.SAVE_PATH);
            Log.i("king", savePath);
            tvResult.setText(savePath);
            Bitmap bitmap = BitmapFactory.decodeFile(savePath);
            if (bitmap != null) {
                ivShow.setImageBitmap(bitmap);
            }
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
