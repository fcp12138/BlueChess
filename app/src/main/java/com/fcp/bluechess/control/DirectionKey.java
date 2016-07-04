package com.fcp.bluechess.control;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 * 方向键
 * Created by fcp on 2015/8/11.
 */
public class DirectionKey extends View implements Runnable{
    //画笔
    private Paint paint;
    //小圆的圆心
    private float smallX,smallY;
    //大圆的圆心
    private float bigX,bigY;
    //小圆的半径
    private float smallRadius=20;
    //大圆的半径
    private float bigRadius=50;
    //屏幕宽高
    private int width, height;
    //方向回调接口
    private DirectionListener directionListener;

    //是否监听
    private boolean isListener;

    //方向
    public enum MyDirection{
        NONE,
        UP,
        DOWN,
        LEFT,
        RIGHT
    }
    //本地的方向
    private MyDirection localDirection;

    public DirectionKey(Context context) {
        super(context);

        initView();
    }

    public DirectionKey(Context context, AttributeSet attrs) {
        super(context, attrs);

        initView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 父容器传过来的宽度方向上的模式
        //int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        // 父容器传过来的高度方向上的模式
        //int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        // 父容器传过来的宽度的值
        width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        // 父容器传过来的高度的值
        height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingLeft() - getPaddingRight();

        /*if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY && ratio != 0.0f) {
            // 判断条件为，宽度模式为Exactly，也就是填充父窗体或者是指定宽度；
            // 且高度模式不是Exaclty，代表设置的既不是fill_parent也不是具体的值，于是需要具体测量
            // 且图片的宽高比已经赋值完毕，不再是0.0f
            // 表示宽度确定，要测量高度
            height = (int) (width / ratio + 0.5f);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        } else if (widthMode != MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY && ratio != 0.0f) {
            // 判断条件跟上面的相反，宽度方向和高度方向的条件互换
            // 表示高度确定，要测量宽度
            width = (int) (height * ratio + 0.5f);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        }*/
        //中间
        bigX = width /2;
        bigY = height/2;
        //小圆的圆心
        smallX=bigX;
        smallY=bigY;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void initView(){
        // 画笔
        paint=new Paint();
        paint.setColor(Color.RED);
        //设置焦点
        setFocusable(true);

        //起线程
        isListener=true;
        new Thread(this).start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawARGB(0, 0, 0, 0);
        //画大圆
        paint.setColor(Color.parseColor("#5599AA"));
        paint.setAlpha(100);
        canvas.drawCircle(bigX, bigY, bigRadius, paint);
        //画小圆
        paint.setColor(Color.parseColor("#5599AA"));
        paint.setAlpha(100);
        canvas.drawCircle(smallX, smallY, smallRadius, paint);
    }

    /**
     * 确定小圆的圆心（在大圆的圆边）
     * @param bigCircleX 大圆的圆心X
     * @param bigCircleY 大圆的圆心Y
     * @param bigRadius 大圆的半径
     * @param touchX 触点X
     * @param touchY 触点Y
     */
    private void setSmallCircleXY(float bigCircleX,float bigCircleY,float bigRadius,float touchX,float touchY){
        float rad=sendDirection( touchX, touchY);
        smallX=bigCircleX+(float)(bigRadius * Math.cos(rad));
        smallY=bigCircleY+(float)(bigRadius * Math.sin(rad));
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_UP){
            smallX=bigX;
            smallY=bigY;
            localDirection= MyDirection.NONE;
        }else if(event.getAction()==MotionEvent.ACTION_DOWN){
            if(localDirection== MyDirection.NONE){
                float pointX=event.getX();
                float pointY=event.getY();
                //在圆内
                if(Math.sqrt(Math.pow(bigX-pointX,2) + Math.pow(bigY-pointY,2))<bigRadius){
                    smallX=pointX;
                    smallY=pointY;
                    sendDirection(pointX, pointY);
                }else{
                    //触点在圆外
                    setSmallCircleXY(bigX,bigY,bigRadius,pointX,pointY);
                }
            }
        }
        this.postInvalidate();
        return true;
    }



    /**
     * 发送方向的信息
     */
    private float sendDirection(float touchX,float touchY){
        //获得两x距离
        float x=touchX-bigX;
        //获得两y距离
        float y=touchY-bigY;
        //得到斜边
        float threeSide= (float) Math.sqrt( Math.pow(x,2) + Math.pow(y,2));
        //获得角度
        float rad= (float) Math.acos(x/threeSide);
        if(touchY<bigY){
            rad=-rad;
        }
        if(rad>=-2.355&&rad<-0.785){//上
            localDirection= MyDirection.UP;
        }else if(rad>=-0.785&&rad<0.785){//右
            localDirection= MyDirection.RIGHT;
        }else if(rad>=0.785&&rad<2.355){//下
            localDirection= MyDirection.DOWN;
        }else if(rad>=2.355||rad<-2.355){//左
            localDirection= MyDirection.LEFT;
        }
        return rad;
    }

    @Override
    public void run() {
        while(isListener){
            long start=System.currentTimeMillis();
            if(directionListener!=null&&localDirection!=null)directionListener.getDirection(localDirection);
            long end=System.currentTimeMillis();
            //限制最高刷屏速度
            if(end-start<50){
                try {
                    Thread.sleep(50-(end-start));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setDirectionListener(DirectionListener directionListener) {
        this.directionListener = directionListener;
    }

    /**
     * 方向监听器
     */
    public interface DirectionListener{
        void getDirection(MyDirection myDirection);
    }

    public void setIsListener(boolean isListener) {
        this.isListener = isListener;
    }


}
