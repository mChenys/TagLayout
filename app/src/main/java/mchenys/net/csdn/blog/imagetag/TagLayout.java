package mchenys.net.csdn.blog.imagetag;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


@SuppressLint("NewApi")
public class TagLayout extends RelativeLayout implements GestureDetector.OnGestureListener {
    private static final String TAG = "PictureTagLayout";
    float startX = 0;
    float startY = 0;
    int startcurrEditViewLeft = 0;
    int startcurrEditViewTop = 0;
    private TagView currEditView;//点击同一个tag赋值,新增为null
    private GestureDetector detector;

    public enum Status {Normal, Edit}

    public enum Direction {Left, Right}

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

    public TagLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        detector = new GestureDetector(getContext(), this);
    }


    //移动tag
    private void moveView(float deltaX, float deltaY) {
        if (currEditView == null) return;
        int x = (int) (deltaX + startcurrEditViewLeft);
        int y = (int) (deltaY + startcurrEditViewTop);
        currEditView.setPosition(x, y);
    }

    //循环获取子view，判断xy是否在子view上，即判断是否按住了子view
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
                    currEditView = (TagView) view;
                    currEditView.bringToFront();
                    startcurrEditViewLeft = currEditView.getLeft();
                    startcurrEditViewTop = currEditView.getTop();
                    return currEditView;
                }
            }
        }
        return null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return detector.onTouchEvent(event);
    }

    //按下根据位置判断是否要添加tag,如果按下的是tag当前的位置则标记为currEditView
    @Override
    public boolean onDown(MotionEvent e) {
        currEditView = hasView(e.getX(), e.getY());
        return true;
    }


    //单击添加或编辑当前的currEditView
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        startX = e.getX();
        startY = e.getY();
        TagView tagView = hasView(startX, startY);
        if (null == tagView) {
            //addTagView(startX, startY);
            if (null != mOnTagOperationCallback) {
                mOnTagOperationCallback.onAdd(startX, startY);
            }
        } else {
            if (null != mOnTagOperationCallback) {
                mOnTagOperationCallback.onEdit(tagView);
            }
        }
        return true;
    }

    //滚动当前的currEditView
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        moveView(e2.getX() - e1.getX(), e2.getY() - e1.getY());
        return true;
    }

    //长按删除当前的currEditView
    @Override
    public void onLongPress(MotionEvent e) {
        if (currEditView != null) {
            if (null != mOnTagOperationCallback) {
                mOnTagOperationCallback.onDelete(currEditView);
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

        public Direction getDirection() {
            return direction;
        }

        public Tag setDirection(Direction direction) {
            this.direction = direction;
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
                    view.setStatus(Status.Normal);//状态
                    view.setParent(tag.getParent());//绑定父亲view
                    addView(view);//显示
                    view.setPosition(tag.getX(), tag.getY());//定位
                    mTagViews.add(view);
                }
            }
        }

    }

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
            view.setStatus(Status.Normal);//状态
            view.setParent(tag.getParent());//绑定父亲view
            addView(view);//显示
            //定位
            view.measureSelf();
            if (tag.getX() > getWidth() * 0.5) {
                if (tag.getX() > getWidth() * 0.5 + view.getMeasuredWidth()) {
                    view.setPosition((int) (tag.getX() - view.getMeasuredWidth()+15), (int) (tag.getY() - view.getMeasuredHeight() / 2));
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

    public void cleanAllTag() {
        for (TagView view : mTagViews) {
            removeView(view);
        }
        mTagViews.clear();
    }

    public void hideAllTag() {
        for (TagView view : mTagViews) {
            view.setVisibility(View.GONE);
        }
    }

    public void showAllTag() {
        for (TagView view : mTagViews) {
            view.setVisibility(View.VISIBLE);
        }
    }


    class TagView extends RelativeLayout implements TextView.OnEditorActionListener {

        private TextView labelTv;
        private EditText labelEdt;
        private RelativeLayout layout;

        private Direction direction = Direction.Left;
        private InputMethodManager imm;
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
            labelEdt = (EditText) findViewById(R.id.edt_label);
            layout = (RelativeLayout) findViewById(R.id.layout);
            imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            labelEdt.setOnEditorActionListener(this);
            mLeftIcon = getResources().getDrawable(R.drawable.bg_left);
            mRightIcon = getResources().getDrawable(R.drawable.bg_right);
        }

        public void setDirection(Direction direction) {
            this.direction = direction;
            directionChange();
        }

        public void setStatus(Status status) {
            switch (status) {
                case Normal:
                    labelTv.setVisibility(View.VISIBLE);
                    labelEdt.clearFocus();
                    labelTv.setText(labelEdt.getText());
                    labelEdt.setVisibility(View.GONE);
                    //隐藏键盘
                    imm.hideSoftInputFromWindow(labelEdt.getWindowToken(), 0);
                    break;
                case Edit:
                    labelTv.setVisibility(View.GONE);
                    labelEdt.setVisibility(View.VISIBLE);
                    labelEdt.requestFocus();
                    //弹出键盘
                    imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                    break;
            }
        }

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            setStatus(Status.Normal);
            return true;
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
            return labelEdt.getText().toString().trim();
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
            labelTv.setText(text);
            labelEdt.setText(text);
        }

        public void removeSelf() {
            currEditView = null;
            mParent.removeView(this);
            mTagViews.remove(this);
        }
    }
}
