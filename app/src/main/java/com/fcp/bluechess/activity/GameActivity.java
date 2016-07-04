package com.fcp.bluechess.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fcp.bluechess.R;
import com.fcp.bluechess.bluetooth.BlueLinkWorker;
import com.fcp.bluechess.bluetooth.BlueToothClient;
import com.fcp.bluechess.bluetooth.BlueToothServer;
import com.fcp.bluechess.bluetooth.OnCreateListener;
import com.fcp.bluechess.control.DirectionKey;
import com.fcp.bluechess.view.GameSurfaceView;


/**
 * 游戏界面
 * Created by fcp on 2015/9/7.
 */
public class GameActivity extends Activity {

    public static final int CREATE_CLIENT=0x101;//客户端
    public static final int CREATE_SERVER=0x102;//服务端

    //帮助类
    private BlueLinkWorker blueLinkWorker;
    /**
     * 方向键盘
     */
    private DirectionKey directionKey;
    /**
     * 显示控件
     */
    private GameSurfaceView gameSurfaceView;
    /**
     * 文字提示框
     */
    private TextView textView;
    /**
     * 发送按钮
     */
    private Button sendBtn;
    /**
     * 编辑框
     */
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//屏幕常亮
        initView();
        setContentView(R.layout.activity_game);
        textView = (TextView) findViewById(R.id.message_show_text);
        gameSurfaceView = (GameSurfaceView) findViewById(R.id.gameSurfaceView_id);
        directionKey = (DirectionKey) findViewById(R.id.directionKey_id);
        sendBtn = (Button) findViewById(R.id.send_btn_id);
        editText = (EditText) findViewById(R.id.edit_id);

        gameSurfaceView.initView(this);
        //监听
        findViewById(R.id.button_id).setOnClickListener(gameSurfaceView);
        directionKey.setDirectionListener(gameSurfaceView);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s=editText.getText().toString().trim();
                if(!s.isEmpty()){
                    blueLinkWorker.send("{'resultCode':4,'data':'"+s+"'}");
                    textView.setText(s);
                    editText.setText("");
                }
            }
        });
    }

    /**
     * 初始化蓝牙连接
     */
    private void initView() {
        //判别类型
        Intent intent=getIntent();
        if(intent.getExtras().getInt("type")==CREATE_CLIENT){
            blueLinkWorker=new BlueToothClient(this,intent.getExtras().getString("address"));//创建客户端
        }else if(intent.getExtras().getInt("type")==CREATE_SERVER){
            blueLinkWorker=new BlueToothServer(this);//创建服务端
            blueLinkWorker.openBlueTooth(this);
        }
        blueLinkWorker.create(new OnCreateListener() {
            @Override
            public void onCreateSuccess() {
                gameSurfaceView.setBlueLinkWorker(blueLinkWorker);
                blueLinkWorker.setOnReceiveDataListener(gameSurfaceView);
                if(blueLinkWorker instanceof BlueToothClient){//服务端先下
                    gameSurfaceView.setCanPaly(false);
                    Toast.makeText(GameActivity.this,"对方先下",Toast.LENGTH_SHORT).show();
                }else{
                    gameSurfaceView.setCanPaly(true);
                    Toast.makeText(GameActivity.this,"请先下",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCreateFail(String cause) {
                Toast.makeText(GameActivity.this,cause, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            directionKey.setIsListener(false);
            blueLinkWorker.send("{'resultCode':2}");//退出信号
            blueLinkWorker.close();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(directionKey!=null)directionKey.setIsListener(false);
        if(blueLinkWorker!=null){
            blueLinkWorker.close();
        }
    }

    public TextView getTextView() {
        return textView;
    }

}
