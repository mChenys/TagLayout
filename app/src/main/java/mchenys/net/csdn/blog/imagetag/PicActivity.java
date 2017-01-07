package mchenys.net.csdn.blog.imagetag;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

/**
 * Created by mChenys on 2017/1/7.
 */
public class PicActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String bitmap = getIntent().getStringExtra("bitmap");
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(BitmapFactory.decodeFile(bitmap));

        setContentView(imageView);
    }
}
