package mchenys.net.csdn.blog.imagetag;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;


public class TagLayout extends RelativeLayout implements GestureDetector.OnGestureListener {
    private int mTouchBeginX = 0;//开始触摸的x坐标
    private int mTouchBeginY = 0;//开始触摸的y坐标
    private TagView mCurrTagView;//点击同一个tag赋值,新增为null
    //支持添加,编辑,删除,移动tag
    private boolean enableAdd, enableEdit, enableDelete, enableMove;
    //手势监听器
    private GestureDetector detector;
    //自定义背景图片的ImageView
    private ImageView mBackgroundPic;

    //背景图片的方向
    public enum Direction {
        Left, Right
    }

    public TagLayout(Context context) {
        super(context, null);
    }

    public interface OnTagOperationCallback {
        void onAdd(float x, float y);

        void onEdit(TagView tagView);

        void onDelete(TagView tagView);
    }

    private OnTagOperationCallback mOnTagOperationCallback;

    public void setOnTagOperationCallback(OnTagOperationCallback callback) {
        this.mOnTagOperationCallback = callback;
    }

    public void setEnableAdd(boolean enableAdd) {
        this.enableAdd = enableAdd;
    }

    public void setEnableEdit(boolean enableEdit) {
        this.enableEdit = enableEdit;
    }

    public void setEnableDelete(boolean enableDelete) {
        this.enableDelete = enableDelete;
    }

    public void setEnableMove(boolean enableMove) {
        this.enableMove = enableMove;
    }

    /**
     * 返回显示背景图片的控件,用户获取后可以设置想要的背景图
     *
     * @return
     */
    public ImageView getBackgroundPic() {
        return mBackgroundPic;
    }

    public TagLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBackgroundPic = new ImageView(context);
        mBackgroundPic.setScaleType(ImageView.ScaleType.CENTER_CROP);
        addView(mBackgroundPic, 0, new ViewGroup.LayoutParams(-1, -1));

        detector = new GestureDetector(getContext(), this);
    }


    /**
     * 移动tag
     *
     * @param deltaX
     * @param deltaY
     */
    private void moveView(float deltaX, float deltaY) {
        if (mCurrTagView != null && enableMove) {
            int x = (int) (mTouchBeginX + deltaX);
            int y = (int) (mTouchBeginY + deltaY);
            mCurrTagView.setPosition(x, y);
        }
    }

    /**
     * 循环获取子view，判断xy是否在子view上，即判断是否按住了子view
     *
     * @param x
     * @param y
     * @return 返回当前触摸的tagview
     */
    private TagView hasView(float x, float y) {
        for (int index = 0; index < this.getChildCount(); index++) {
            View view = getChildAt(index);
            if (view instanceof TagView) {
                float left = view.getX();
                float top = view.getY();
                int right = view.getRight();
                int bottom = view.getBottom();
                RectF rect = new RectF(left, top, right, bottom);
                boolean contains = rect.contains(x, y);
                //如果是与子view重叠则返回真,表示已经有了view不需要添加新view了
                if (contains) {
                    mCurrTagView = (TagView) view;
                    mCurrTagView.bringToFront();
                    mTouchBeginX = mCurrTagView.getLeft();
                    mTouchBeginY = mCurrTagView.getTop();
                    return mCurrTagView;
                }
            }
        }
        return null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return detector.onTouchEvent(event);
    }

    /**
     * 按下根据位置判断是否要添加tag,如果按下的是tag当前的位置则标记为mCurrTagView
     *
     * @param e
     * @return
     */
    @Override
    public boolean onDown(MotionEvent e) {
        mCurrTagView = hasView(e.getX(), e.getY());
        return true;
    }


    /**
     * 单击添加或编辑当前的mCurrTagView
     *
     * @param e
     * @return
     */
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        TagView tagView = hasView(e.getX(), e.getY());
        if (null == tagView) {
            if (null != mOnTagOperationCallback && enableAdd) {
                mOnTagOperationCallback.onAdd(e.getX(), e.getY());
            }
        } else {
            if (null != mOnTagOperationCallback && enableEdit) {
                mOnTagOperationCallback.onEdit(tagView);
            }
        }
        return true;
    }

    /**
     * 滚动当前的mCurrTagView
     *
     * @param e1
     * @param e2
     * @param distanceX
     * @param distanceY
     * @return
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        moveView(e2.getX() - e1.getX(), e2.getY() - e1.getY());
        return true;
    }

    /**
     * 长按删除当前的mCurrTagView
     *
     * @param e
     */
    @Override
    public void onLongPress(MotionEvent e) {
        if (mCurrTagView != null) {
            if (null != mOnTagOperationCallback && enableDelete) {
                mOnTagOperationCallback.onDelete(mCurrTagView);
            }
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    public static final class Tag {
        private String mText;
        private float x, y;
        private View mCustomView;
        private Direction direction;
        private Drawable mLeftIcon, mRightIcon;
        private TagLayout mParent;

        private Tag() {
            // Private constructor
        }

        public String getText() {
            return mText;
        }

        public Tag setText(String text) {
            mText = text;
            return this;
        }

        public float getX() {
            return x;
        }

        public Tag setX(float x) {
            this.x = x;
            return this;
        }

        public float getY() {
            return y;
        }

        public Tag setY(float y) {
            this.y = y;
            return this;
        }

        public View getCustomView() {
            return mCustomView;
        }

        public Tag setCustomView(View customView) {
            mCustomView = customView;
            return this;
        }

        public Drawable getLeftIcon() {
            return mLeftIcon;
        }

        public Tag setLeftIcon(Drawable leftIcon) {
            mLeftIcon = leftIcon;
            return this;
        }

        public Drawable getRightIcon() {
            return mRightIcon;
        }

        public Tag setRightIcon(Drawable rightIcon) {
            mRightIcon = rightIcon;
            return this;
        }

        public Tag setParent(TagLayout parent) {
            this.mParent = parent;
            return this;
        }

        public TagLayout getParent() {
            return mParent;
        }
    }

    private List<TagView> mTagViews = new ArrayList<TagView>();

    /**
     * 还原所有tag
     *
     * @param tags 需要被还原的tag
     */
    public void restoreTag(List<Tag> tags) {
        if (null != tags) {
            for (Tag tag : tags) {
                if (null != tag && null == hasView(tag.getX(), tag.getY())) {
                    TagView view = new TagView(getContext());
                    if (null != tag.getCustomView()) {
                        view.setCustomView(tag.getCustomView());//布局
                    }
                    if (null != tag.getText()) {
                        view.setText(tag.getText());//文本

                    }
                    if (null != tag.getLeftIcon()) {
                        view.setLeftIcon(tag.getLeftIcon());//背景图
                    }
                    if (null != tag.getRightIcon()) {
                        view.setRightIcon(tag.getRightIcon());
                    }
                    view.setDirection(tag.getX() >= getWidth() * 0.5f ? Direction.Right : Direction.Left);
                    view.setParent(tag.getParent());//绑定父亲view
                    addView(view);//显示
                    view.setPosition(tag.getX(), tag.getY());//定位
                    mTagViews.add(view);
                }
            }
        }

    }

    /**
     * 添加新的tag
     *
     * @param tag
     */
    public void addTag(Tag tag) {
        if (null != tag && null == hasView(tag.getX(), tag.getY())) {
            TagView view = new TagView(getContext());
            if (null != tag.getCustomView()) {
                view.setCustomView(tag.getCustomView());//布局
            }
            if (null != tag.getText()) {
                view.setText(tag.getText());//文本

            }
            if (null != tag.getLeftIcon()) {
                view.setLeftIcon(tag.getLeftIcon());//背景图
            }
            if (null != tag.getRightIcon()) {
                view.setRightIcon(tag.getRightIcon());
            }
            view.setDirection(tag.getX() >= getWidth() * 0.5f ? Direction.Right : Direction.Left);
            view.setParent(tag.getParent());//绑定父view
            addView(view);//显示
            //定位
            view.measureSelf();
            if (tag.getX() > getWidth() * 0.5) {
                if (tag.getX() > getWidth() * 0.5 + view.getMeasuredWidth()) {
                    view.setPosition((int) (tag.getX() - view.getMeasuredWidth() + 15), (int) (tag.getY() - view.getMeasuredHeight() / 2));
                } else {
                    view.setPosition((int) (tag.getX()), (int) (tag.getY() - view.getMeasuredHeight() / 2));
                }
            } else {
                view.setPosition((int) (tag.getX()), (int) (tag.getY() - view.getMeasuredHeight() / 2));
            }

            mTagViews.add(view);
        }
    }

    public Tag newTag() {
        Tag tag = new Tag();
        tag.setParent(this);
        return tag;
    }

    /**
     * 获取已添加的所有tag
     *
     * @return
     */
    public List<Tag> getAllTag() {
        List<Tag> tagList = new ArrayList<Tag>();
        for (TagView view : mTagViews) {
            Tag tag = newTag();
            tag.setX(view.getLeft())
                    .setY(view.getTop())
                    .setLeftIcon(view.getLeftIcon())
                    .setRightIcon(view.getRightIcon())
                    .setText(view.getText());
            tagList.add(tag);
        }
        return tagList;
    }

    /**
     * 清除所有tag
     */
    public void cleanAllTag() {
        for (TagView view : mTagViews) {
            removeView(view);
        }
        mTagViews.clear();
    }

    /**
     * 隐藏所有tag
     */
    public void hideAllTag() {
        for (TagView view : mTagViews) {
            view.setVisibility(View.GONE);
        }
    }

    /**
     * 显示所有tag
     */
    public void showAllTag() {
        for (TagView view : mTagViews) {
            view.setVisibility(View.VISIBLE);
        }
    }


    class TagView extends RelativeLayout {

        private TextView labelTv;
        private RelativeLayout layout;

        private Direction direction = Direction.Left;
        private Drawable mLeftIcon, mRightIcon;
        private TagLayout mParent;

        public TagView(Context context, Direction direction) {
            this(context);
            setDirection(direction);
        }

        public TagView(Context context) {
            super(context);
            LayoutInflater.from(getContext()).inflate(R.layout.layout_tag_view, this, true);
            labelTv = (TextView) findViewById(R.id.tv_label);
            layout = (RelativeLayout) findViewById(R.id.layout);
            mLeftIcon = getResources().getDrawable(R.drawable.bg_left);
            mRightIcon = getResources().getDrawable(R.drawable.bg_right);
        }

        public void setDirection(Direction direction) {
            this.direction = direction;
            directionChange();
        }


        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            View parent = (View) getParent();
            float halfParentW = parent.getWidth() * 0.5f;
            direction = l < halfParentW ? Direction.Left : Direction.Right;
            directionChange();
        }

        private void directionChange() {
            switch (direction) {
                case Left:
                    layout.setBackgroundDrawable(mLeftIcon);
                    break;
                case Right:
                    layout.setBackgroundDrawable(mRightIcon);
                    break;
            }
        }

        private String getText() {
            return labelTv.getText().toString().trim();
        }

        private void setPosition(float x, float y) {
            measureSelf();
            if (x < 0) {
                x = 0;
            }
            if (x > mParent.getWidth() - getMeasuredWidth()) {
                x = mParent.getWidth() - getMeasuredWidth();
            }
            if (y < 0) {
                y = 0;
            }
            if (y > mParent.getHeight() - getMeasuredHeight()) {
                y = mParent.getHeight() - getMeasuredHeight();
            }
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.leftMargin = (int) x;
            params.topMargin = (int) y;
            setLayoutParams(params);
        }

        private void setCustomView(View customView) {
            layout.removeAllViews();
            layout.addView(customView);
        }

        private void setRightIcon(Drawable rightIcon) {
            mRightIcon = rightIcon;
        }

        private void setLeftIcon(Drawable leftIcon) {
            mLeftIcon = leftIcon;
        }

        private void setParent(TagLayout parent) {
            mParent = parent;
        }

        private void measureSelf() {
            if (getMeasuredWidth() == 0 || getMeasuredHeight() == 0) {
                measure(0, 0);
            }
        }

        private Drawable getRightIcon() {
            return mRightIcon;
        }

        private Drawable getLeftIcon() {
            return mLeftIcon;
        }

        public void setText(String text) {
            if (null != labelTv)
                labelTv.setText(text);
        }

        public void removeSelf() {
            mCurrTagView = null;
            mParent.removeView(this);
            mTagViews.remove(this);
        }
    }

    /**
     * 生成对应的图片
     *
     * @return
     */
    public String createBitmap() {
        setDrawingCacheEnabled(true);
        setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        setDrawingCacheBackgroundColor(Color.WHITE);
        int w = getWidth();
        int h = getHeight();
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(Color.WHITE);
        draw(c);
        FileOutputStream fos = null;
        File file = null;
        try {
            boolean isHasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
            if (isHasSDCard) {
                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), System.currentTimeMillis() + ".png");
                fos = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
            } else {
                Toast.makeText(getContext(), "未发现sdcard", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            destroyDrawingCache();
        }
        return null == file ? null : file.getAbsolutePath();
    }
}
