package jp.ac.titech.itpro.sdl.quickmapsearch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

/**
 * Created by kengo on 16/07/12.
 */
public class RootListView extends ListView implements AdapterView.OnItemLongClickListener,AdapterView.OnItemClickListener {

    private static final String TAG = RootListView.class.getSimpleName();

    private static final int SCROLL_SPEED_FAST = 25;
    private static final int SCROLL_SPEED_SLOW = 8;
    private static final Bitmap.Config DRAG_BITMAP_CONFIG = Bitmap.Config.ARGB_8888;

    private boolean sortable = true;
    private boolean dragging = true;
    private DragListener dragListener = new SimpleDragListener();
    private int mBitmapBackgroundColor = Color.argb(128, 0xFF, 0xFF, 0xFF);
    private Bitmap mDragBitmap = null;
    private ImageView dragImageView = null;
    private WindowManager.LayoutParams layoutParams = null;
    private MotionEvent actionDownEvent;
    private int positionFrom = -1;

    /** コンストラクタ */
    public RootListView(Context context) {
        super(context);
        setOnItemLongClickListener(this);
    }

    /** コンストラクタ */
    public RootListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnItemLongClickListener(this);
    }

    /** コンストラクタ */
    public RootListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnItemLongClickListener(this);
    }

    /** ドラッグイベントリスナの設定 */
    public void setDragListener(DragListener listener) {
        dragListener = listener;
    }

    /** ソートモードの切替 */
    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    /** ソート中アイテムの背景色を設定 */
    @Override
    public void setBackgroundColor(int color) {
        mBitmapBackgroundColor = color;
    }

    /** ソートモードの設定 */
    public boolean getSortable() {
        return sortable;
    }

    /** MotionEvent から position を取得する */
    private int eventToPosition(MotionEvent event) {
        if(event != null) {
            return pointToPosition((int) event.getX(), (int) event.getY());
        }else{
            Log.d(TAG,"event null");
            return 0;
        }
    }

    /** タッチイベント処理 */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!sortable) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                storeMotionEvent(event);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (duringDrag(event)) {
                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (stopDrag(event, true)) {
                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE: {
                if (stopDrag(event, false)) {
                    return true;
                }
                break;
            }
        }
        return super.onTouchEvent(event);
    }

    /** リスト要素長押しイベント処理 */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                   int position, long id) {
        return startDrag();
    }


    /** ACTION_DOWN 時の MotionEvent をプロパティに格納 */
    private void storeMotionEvent(MotionEvent event) {
        actionDownEvent = MotionEvent.obtain(event); // 複製しないと値が勝手に変わる

    }

    /** ドラッグ開始 */
    private boolean startDrag() {
        // イベントから position を取得
        positionFrom = eventToPosition(actionDownEvent);

        // 取得した position が 0未満＝範囲外の場合はドラッグを開始しない
        if (positionFrom < 0) {
            return false;
        }
        dragging = true;

        // View, Canvas, WindowManager の取得・生成
        final View view = getChildByIndex(positionFrom);
        final Canvas canvas = new Canvas();
        final WindowManager wm = getWindowManager();

        // ドラッグ対象要素の View を Canvas に描画
        mDragBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                DRAG_BITMAP_CONFIG);
        canvas.setBitmap(mDragBitmap);
        view.draw(canvas);

        // 前回使用した ImageView が残っている場合は除去（念のため？）
        if (dragImageView != null) {
            wm.removeView(dragImageView);
        }

        // ImageView 用の LayoutParams が未設定の場合は設定する
        if (layoutParams == null) {
            initLayoutParams();
        }

        // ImageView を生成し WindowManager に addChild する
        dragImageView = new ImageView(getContext());
        dragImageView.setBackgroundColor(mBitmapBackgroundColor);
        dragImageView.setImageBitmap(mDragBitmap);
        wm.addView(dragImageView, layoutParams);

        // ドラッグ開始
        if (dragListener != null) {
            positionFrom = dragListener.onStartDrag(positionFrom);
        }
        return duringDrag(actionDownEvent);
    }

    /** ドラッグ処理 */
    private boolean duringDrag(MotionEvent event) {
        if (!dragging || dragImageView == null) {
            return false;
        }
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        final int height = getHeight();
        final int middle = height / 2;

        // スクロール速度の決定
        final int speed;
        final int fastBound = height / 9;
        final int slowBound = height / 4;
        if (event.getEventTime() - event.getDownTime() < 500) {
            // ドラッグの開始から500ミリ秒の間はスクロールしない
            speed = 0;
        } else if (y < slowBound) {
            speed = y < fastBound ? -SCROLL_SPEED_FAST : -SCROLL_SPEED_SLOW;
        } else if (y > height - slowBound) {
            speed = y > height - fastBound ? SCROLL_SPEED_FAST
                    : SCROLL_SPEED_SLOW;
        } else {
            speed = 0;
        }

        // スクロール処理
        if (speed != 0) {
            // 横方向はとりあえず考えない
            int middlePosition = pointToPosition(0, middle);
            if (middlePosition == AdapterView.INVALID_POSITION) {
                middlePosition = pointToPosition(0, middle + getDividerHeight()
                        + 64);
            }
            final View middleView = getChildByIndex(middlePosition);
            if (middleView != null) {
                setSelectionFromTop(middlePosition, middleView.getTop() - speed);
            }
        }

        // ImageView の表示や位置を更新
        if (dragImageView.getHeight() < 0) {
            dragImageView.setVisibility(View.INVISIBLE);
        } else {
            dragImageView.setVisibility(View.VISIBLE);
        }
        updateLayoutParams((int)event.getRawY()); // ここだけスクリーン座標を使う
        getWindowManager().updateViewLayout(dragImageView, layoutParams);
        if (dragListener != null) {
            positionFrom = dragListener.onDuringDrag(positionFrom,
                    pointToPosition(x, y));
        }
        return true;
    }

    /** ドラッグ終了 */
    private boolean stopDrag(MotionEvent event, boolean isDrop) {
        if (!dragging) {
            return false;
        }
        if (isDrop && dragListener != null) {
            dragListener.onStopDrag(positionFrom, eventToPosition(event));
        }
        dragging = false;
        if (dragImageView != null) {
            getWindowManager().removeView(dragImageView);
            dragImageView = null;
            // リサイクルするとたまに死ぬけどタイミング分からない by vvakame
            // mDragBitmap.recycle();
            mDragBitmap = null;

            actionDownEvent.recycle();
            actionDownEvent = null;
            return true;
        }
        return false;
    }

    /** 指定インデックスのView要素を取得する */
    private View getChildByIndex(int index) {
        return getChildAt(index - getFirstVisiblePosition());
    }

    /** WindowManager の取得 */
    protected WindowManager getWindowManager() {
        return (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
    }

    /** ImageView 用 LayoutParams の初期化 */
    protected void initLayoutParams() {
        layoutParams = new WindowManager.LayoutParams();
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.windowAnimations = 0;
        layoutParams.x = getLeft();
        layoutParams.y = getTop();
    }

    /** ImageView 用 LayoutParams の座標情報を更新 */
    protected void updateLayoutParams(int rawY) {
        layoutParams.y =  rawY - 32;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(TAG,"tap");
        MainActivity.SampleAdapter s = (MainActivity.SampleAdapter)adapterView.getAdapter();
        s.removeItem(i);
    }

    /** ドラッグイベントリスナーインターフェース */
    public interface DragListener {
        /** ドラッグ開始時の処理 */
        public int onStartDrag(int position);

        /** ドラッグ中の処理 */
        public int onDuringDrag(int positionFrom, int positionTo);

        /** ドラッグ終了＝ドロップ時の処理 */
        public boolean onStopDrag(int positionFrom, int positionTo);
    }

    /** ドラッグイベントリスナー実装 */
    public static class SimpleDragListener implements DragListener {
        /** ドラッグ開始時の処理 */
        @Override
        public int onStartDrag(int position) {
            return position;
        }

        /** ドラッグ中の処理 */
        @Override
        public int onDuringDrag(int positionFrom, int positionTo) {
            return positionFrom;
        }

        /** ドラッグ終了＝ドロップ時の処理 */
        @Override
        public boolean onStopDrag(int positionFrom, int positionTo) {
            return positionFrom != positionTo && positionFrom >= 0
                    || positionTo >= 0;
        }
    }
}
