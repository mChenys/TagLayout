package mchenys.net.csdn.blog.imagetag;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity {
    private TagLayout mTagLayout1, mTagLayout2;
    private List<TagLayout.Tag> mTagList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTagLayout1 = (TagLayout) findViewById(R.id.ptl1);
        mTagLayout2 = (TagLayout) findViewById(R.id.ptl2);
        //以下设置可以通过自定义属性设置
//        mTagLayout1.setMaxTagNum(5);
//        mTagLayout1.setEnableAdd(true);
//        mTagLayout1.setEnableEdit(true);
//        mTagLayout1.setEnableDelete(true);
//        mTagLayout1.setEnableMove(true);
        mTagLayout1.setOnTagOperationCallback(new TagLayout.OnTagOperationCallback() {
            @Override
            public void onAdd(final float x, final float y) {
                final EditText editText = new EditText(MainActivity.this);
                new AlertDialog.Builder(MainActivity.this).setTitle("添加tag")
                        .setView(editText)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String text = editText.getText().toString().trim();
                                if (TextUtils.isEmpty(text)) {
                                    Toast.makeText(MainActivity.this, "请输入标签名字", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                mTagLayout1.addTag(mTagLayout1.newTag().setText(text).setX(x).setY(y));
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
            }

            @Override
            public void onEdit(final TagLayout.TagView tagView) {
                final EditText editText = new EditText(MainActivity.this);
                new AlertDialog.Builder(MainActivity.this).setTitle("修改tag")
                        .setView(editText)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String text = editText.getText().toString().trim();
                                if (TextUtils.isEmpty(text)) {
                                    Toast.makeText(MainActivity.this, "请输入标签名字", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                tagView.setText(text);
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
            }

            @Override
            public void onDelete(final TagLayout.TagView tagView) {
                new AlertDialog.Builder(MainActivity.this).setTitle("删除tag")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                tagView.removeSelf();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
            }
        });
    }

    public void onSave(View view) {
        mTagList = mTagLayout1.getAllTag();
    }

    public void onRestore(View view) {
        if (null != mTagList) {
            mTagLayout2.cleanAllTag();
            mTagLayout2.restoreTag(mTagList);
        }
    }

    public void onHide(View view) {
        mTagLayout1.hideAllTag();
        mTagLayout2.hideAllTag();
    }

    public void onShow(View view) {
        mTagLayout1.showAllTag();
        mTagLayout2.showAllTag();
    }
    public void onCreatePic(View view) {
        Intent intent = new Intent(this, PicActivity.class);
        intent.putExtra("bitmap", mTagLayout1.createBitmap());
        startActivity(intent);
    }
    public void onChangeBg(View view) {
        mTagLayout1.getBackgroundPic().setImageResource(R.drawable.change_bg);
    }
    public void onChangeTagBg(View view) {
        mTagLayout1.changeTagBackground(R.drawable.bg_new_left, R.drawable.bg_new_right);
    }
}
