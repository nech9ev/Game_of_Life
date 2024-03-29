package nechaev.gameoflife.gameoflife.threads;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.gameoflife.gameoflife.R;
import nechaev.gameoflife.gameoflife.classes.Field;
import nechaev.gameoflife.gameoflife.classes.Point;
import com.google.gson.Gson;

public class SurfView extends SurfaceView implements SurfaceHolder.Callback {

    public SThread mMyThread;
    boolean isPlaying = true, isMoving = true, isEditing = false;
    Point p1 = new Point(0,0);
    double distance1 = 0;
    double distance2 = 0;
    Field field;
    Context context;

    public SurfView(Context context) {
        super(context);
        this.field = new Gson().fromJson(context.getString(R.string.empty_field), Field.class);
        this.context = context;
        this.setX(1000);
        this.setY(1000);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mMyThread = new SThread(getHolder(), field);
        mMyThread.setRunning(true);
        mMyThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    public SurfView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    public SurfView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getHolder().addCallback(this);
    }


    public void setBool(boolean isPlaying, boolean isMoving, boolean isEditing){
        this.isEditing = isEditing;
        this.isPlaying = isPlaying;
        this.isMoving = isMoving;
        mMyThread.setBool(isPlaying, isMoving, isEditing);
    }

    public Field getField(){
        return mMyThread.getFied();
    }

    int id = 0;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                p1 = new Point((int) event.getX(), (int) event.getY());
                if(isMoving)
                    id = event.getPointerId(0);

                if(isEditing)
                   mMyThread.addordel(p1);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if(isMoving)
                    distance1 = Math.sqrt((event.getX(0) - event.getX(1))*(event.getX(0) - event.getX(1))
                            + (event.getY(0) - event.getY(1))*(event.getY(0) - event.getY(1)));

                break;
            case MotionEvent.ACTION_UP:
                if(isMoving)
                    mMyThread.fixShift();

                break;
            case MotionEvent.ACTION_POINTER_UP:
                if(isMoving && event.getPointerCount() == 2)
                    mMyThread.fixCoff();

                break;
        }
        if(event.getPointerCount() == 1){
            if(isMoving && id == event.getPointerId(0))
                mMyThread.setShift(new Point(p1.getX() - (int)event.getX(), p1.getY() - (int) event.getY()));

        }
        if(event.getPointerCount() == 2){
            if(event.getActionMasked() == MotionEvent.ACTION_MOVE && isMoving){
                distance2 = Math.sqrt((event.getX(0) - event.getX(1))*(event.getX(0) - event.getX(1))
                        + (event.getY(0) - event.getY(1))*(event.getY(0) - event.getY(1)));
                Point  p = new Point((int)(event.getX(0)+event.getX(1))/2, (int)(event.getY(0)+event.getY(1))/2);
                mMyThread.setCoff(distance2/distance1, p);
            }
        }
        return true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        mMyThread.setRunning(false);

        while(retry) {
            try {
                mMyThread.join();
                retry = false;
            }
            catch (InterruptedException e) {
            }
        }
    }
}