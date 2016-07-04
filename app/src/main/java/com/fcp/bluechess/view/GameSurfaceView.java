package com.fcp.bluechess.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.fcp.bluechess.activity.GameActivity;
import com.fcp.bluechess.bluetooth.BlueLinkWorker;
import com.fcp.bluechess.bluetooth.OnReceiveDataListener;
import com.fcp.bluechess.control.DirectionKey;
import com.fcp.bluechess.entity.Chess;
import com.fcp.bluechess.service.GameService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * 画面更新显示控件
 * Created by fcp on 2015/8/10.
 */
public class GameSurfaceView extends SurfaceView implements SurfaceHolder.Callback,Runnable,OnReceiveDataListener,DirectionKey.DirectionListener,View.OnClickListener {

    // 后台holder
    private SurfaceHolder holder;
    //画笔
    private Paint paint,mPaint,oPaint,sPaint,kPaint;
    //刷屏线程控制标志位
    private boolean flag;
    //屏幕宽高
    private int screenSize;
    //画布
    private Canvas canvas;
    //帮助类
    private BlueLinkWorker blueLinkWorker;
    //间隔尺寸
    private float size;
    //选择点
    private SelectPoint selectPoint;

    private ArrayList<Chess> myChess;//自己的棋子
    private ArrayList<Chess> heChess;//别人的棋子

    private int[][] chesses=new int[15][15];//棋子布置

    private boolean canPaly;//是否可以下

    private GameActivity activity;//嵌入的activity

    private ProgressDialog progressDialog;//等待圈

    public GameSurfaceView(Context context) {
        super(context);
    }

    public GameSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    /**
     * 初始化绘图
     */
    public void initView(GameActivity activity) {
        this.activity = activity;
        // 通过SurfaceView获得SurfaceHolder对象
        holder = getHolder();
        // 为holder添加回调结构SurfaceHolder.Callback
        holder.addCallback(this);
        setFocusable(true);
        //边画笔
        paint=new Paint();
        paint.setColor(Color.parseColor("#F3EED8"));
        //最外层
        kPaint=new Paint();
        kPaint.setStrokeWidth(10);
        kPaint.setColor(Color.parseColor("#C4A93C"));
        //我的颜色
        mPaint=new Paint();
        mPaint.setColor(Color.BLACK);
        //对方的颜色
        oPaint=new Paint();
        oPaint.setColor(Color.WHITE);
        //选择点
        sPaint=new Paint();
        sPaint.setColor(Color.parseColor("#AA000000"));
        //棋子
        myChess=new ArrayList<>();
        heChess=new ArrayList<>();
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.screenSize =getWidth();
        size= ((float)(screenSize) )/15f;
        //选择点
        selectPoint=new SelectPoint(7,7);

        flag=true;
        //开启刷新线程
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //停止线程
        flag=false;
    }


    @Override
    public void run() {
        while (flag){
            long start=System.currentTimeMillis();
            onMyDraw();
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


    int time=0;
    /**
     * 自定义绘图方法
     */
    private void onMyDraw(){
        try {
            canvas=holder.lockCanvas();
            if(canvas!=null){
                canvas.drawColor(Color.parseColor("#55AAAA"));//背景色

                for(int i=1;i<15;i++){
                    canvas.drawLine(0,i*size, screenSize,i*size,paint);//横线
                    canvas.drawLine(i * size, 0, i * size, screenSize, paint);//竖线
                }
                //最外层4边
                canvas.drawLine( 0, 0, screenSize, 0 ,kPaint);//横线
                canvas.drawLine( 0, 0, 0, screenSize, kPaint);//竖线
                canvas.drawLine( 0 ,screenSize, screenSize, screenSize , kPaint);//横线
                canvas.drawLine( screenSize, 0, screenSize , screenSize, kPaint);//竖线

                //画选择点
                if(time++<10){
                    canvas.drawCircle(selectPoint.x*size,selectPoint.y*size,(size+2)/2,sPaint);
                }else if(time>20){
                    time=0;
                }
                //画我选的棋
                for(Chess chess:myChess){
                    canvas.drawCircle(chess.x*size,chess.y*size,(size-2)/2,mPaint);
                }
                //画对手选的棋
                for(Chess chess:heChess){
                    canvas.drawCircle(chess.x*size,chess.y*size,(size-2)/2,oPaint);
                }
            }
        }catch (Exception e){
            Log.e("fcp","canvas get is erroe");
        }finally {
            if(canvas!=null)
                holder.unlockCanvasAndPost(canvas);
        }
    }


    /**
     * 接收到蓝牙的数据
     * JSON 格式 {'resultCode':0,'data':{'x':2,'y':3}}         //棋子位置
     * JSON 格式 {'resultCode':1}                              //输了
     * JSON 格式 {'resultCode':2}                              //退出
     * JSON 格式 {'resultCode':3}                              //再来一局
     * JSON 格式 {'resultCode':4,'data':'abc'}                 //发送语句
     */
    @Override
    public void onReceiverSuccess(String data) {
        try {
            JSONObject jsonObject = new JSONObject(data);
            int resultCode = jsonObject.getInt("resultCode");
            switch (resultCode){
                case 0:
                    JSONObject object=jsonObject.getJSONObject("data");
                    int x=object.getInt("x");
                    int y=object.getInt("y");
                    heChess.add(new Chess(x,y));//添加对方棋子
                    chesses[x][y]=2;
                    canPaly=true;
                    activity.getTextView().setText("  对方已下,横："+x +"  竖："+y);
                    break;
                case 1:
                    showDialog(false);
                    break;
                case 2:
                    Toast.makeText(getContext(),"对方退出",Toast.LENGTH_SHORT).show();
                    if(progressDialog!=null)progressDialog.dismiss();
                    activity.finish();
                    break;
                case 3://再来一局
                    if(progressDialog!=null){
                        progressDialog.dismiss();
                        progressDialog = null;
                    }else{
                        canPaly=true;
                    }
                    isAgain=true;
                    break;
                case 4:
                    activity.getTextView().setText(jsonObject.getString("data"));

            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("fcp","JSON 格式出错");
        }
    }

    boolean isAgain;//对方是否同意再来一局

    /**
     * 显示结果框
     * @param result 输赢
     */
    private void showDialog(boolean result) {
        String title,name;

        if(result){
            title="你赢了";
            if(myChess.size()==heChess.size()){
                name="后手 共"+myChess.size()+"步";
            }else{
                name="先手 共"+myChess.size()+"步";
            }
            addData(1,System.currentTimeMillis(),name);
        }else{
            title="你输了";
            if(myChess.size()==heChess.size()){
                name="先手 共"+myChess.size()+"步";
            }else{
                name="后手 共"+myChess.size()+"步";
            }
            addData(0,System.currentTimeMillis(),name);
        }
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage("再来一局吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        blueLinkWorker.send("{'resultCode':3} ");
                        if(!isAgain)progressDialog = ProgressDialog.show(getContext(), "请稍等...", "等待对方选择...", true);
                        myChess.clear();
                        heChess.clear();
                        chesses=new int[15][15];
                        isAgain=false;
                        activity.getTextView().setText("");
                    }})
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        blueLinkWorker.send("{'resultCode':2}");
                        activity.finish();
                    }
                })
                .show();
    }

    /**
     * 添加数据到数据库
     */
    private void addData(final int game_result, final long game_time, final String game_person){
        new Thread(new Runnable() {
            @Override
            public void run() {
                new GameService(getContext()).insertData( game_result, game_time, game_person);
            }
        }).start();
    }


    @Override
    public void getDirection(DirectionKey.MyDirection myDirection) {
        switch (myDirection){
            case UP:
                if(selectPoint.y>1) selectPoint.y-=1;
                break;
            case DOWN:
                if(selectPoint.y<14) selectPoint.y+=1;
                break;
            case LEFT:
                if(selectPoint.x>1) selectPoint.x-=1;
                break;
            case RIGHT:
                if(selectPoint.x<14) selectPoint.x+=1;
                break;
        }
    }

    /**
     * 点击确定键
     * @param v View
     */
    @Override
    public void onClick(View v) {
        if(canPaly){
            if(chesses[selectPoint.x][selectPoint.y]==0) {
                myChess.add(new Chess(selectPoint.x, selectPoint.y));
                chesses[selectPoint.x][selectPoint.y]=1;//记录
                //判断
                if(logic(selectPoint.x,selectPoint.y)){
                    blueLinkWorker.send("{'resultCode':1}");
                    showDialog(true);
                }else{
                    blueLinkWorker.send("{'resultCode':0,'data':{'x':"+ selectPoint.x +",'y':"+ selectPoint.y +"}}");//发送数据
                    activity.getTextView().setText("  我方已下,横：" + selectPoint.x +"  竖："+selectPoint.y);
                }
                canPaly=false;
            }else{
                Toast.makeText(getContext(),"已有棋！",Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getContext(),"对方还未下",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 游戏逻辑
     */
    private boolean logic(int x,int y){
        //横向
        int num=0;
        int i,j;
        for( i=x-1;i>0;i--){//向左
            if(chesses[i][y]==1){
                num++;
            }else{
                break;
            }
        }
        for( i=x+1;i<15;i++){//向左
            if(chesses[i][y]==1){
                num++;
            }else{
                break;
            }
        }
        if(num>=4)return true;
        //纵向
        num=0;
        for( j=y-1;j>0;j--){//向上
            if(chesses[x][j]==1){
                num++;
            }else{
                break;
            }
        }
        for( j=y+1;j<15;j++){//向下
            if(chesses[x][j]==1){
                num++;
            }else{
                break;
            }
        }
        if(num>=4)return true;
        //正斜
        num=0;
        for( i=x-1, j=y-1;i>0||j>0;i--,j--){//向左上
            if(chesses[i][j]==1){
                num++;
            }else{
                break;
            }
        }
        for(  i=x+1, j=y+1;i<15||j<15;i++,j++){//向右下
            if(chesses[i][j]==1){
                num++;
            }else{
                break;
            }
        }
        if(num>=4)return true;
        //反斜
        num=0;
        for( i=x+1, j=y-1;i<15||j>0;i++,j--){//向右上
            if(chesses[i][j]==1){
                num++;
            }else{
                break;
            }
        }
        for(  i=x-1, j=y+1;i>0||j<15;i--,j++){//向左下
            if(chesses[i][j]==1){
                num++;
            }else{
                break;
            }
        }
        return num >= 4;
    }

    /**
     * 选择点
     */
    class SelectPoint{
        int x;
        int y;

        public SelectPoint(int y, int x) {
            this.y = y;
            this.x = x;
        }
    }

    public void setCanPaly(boolean canPaly) {
        this.canPaly = canPaly;
    }

    public void setBlueLinkWorker(BlueLinkWorker blueLinkWorker) {
        this.blueLinkWorker = blueLinkWorker;
    }
}
