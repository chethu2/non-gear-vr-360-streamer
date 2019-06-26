package com.nokia.streamer;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.widget.Toast;

import com.asha.md360player4android.R;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.BarrelDistortionConfig;
import com.asha.vrlib.model.MDPinchConfig;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by hzqiujiadi on 16/4/5.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class VideoPlayerActivity extends MD360PlayerActivity {

    private MediaPlayerWrapper mMediaPlayerWrapper = new MediaPlayerWrapper();
    private boolean enableSocket = false ;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMediaPlayerWrapper.init();
        mMediaPlayerWrapper.setPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                if (getVRLibrary() != null){
                    getVRLibrary().notifyPlayerChanged();
                }
            }
        });

        mMediaPlayerWrapper.getPlayer().setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                String error = String.format("Play Error what=%d extra=%d",what,extra);
                Toast.makeText(VideoPlayerActivity.this, error, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        mMediaPlayerWrapper.getPlayer().setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
                getVRLibrary().onTextureResize(width, height);
            }
        });

        Uri uri = getUri();
        if (uri != null){
            mMediaPlayerWrapper.openRemoteFile(uri.toString());
            mMediaPlayerWrapper.prepare();
        }

    }

    @Override
    protected MDVRLibrary createVRLibrary() {
        return MDVRLibrary.with(this)
                .displayMode(MDVRLibrary.DISPLAY_MODE_GLASS)
                .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_MOTION)
                .asVideo(new MDVRLibrary.IOnSurfaceReadyCallback() {
                    @Override
                    public void onSurfaceReady(Surface surface) {
                        mMediaPlayerWrapper.setSurface(surface);
                    }
                })
                .ifNotSupport(new MDVRLibrary.INotSupportCallback() {
                    @Override
                    public void onNotSupport(int mode) {
                        String tip = mode == MDVRLibrary.INTERACTIVE_MODE_MOTION
                                ? "onNotSupport:MOTION" : "onNotSupport:" + String.valueOf(mode);
                        Toast.makeText(VideoPlayerActivity.this, tip, Toast.LENGTH_SHORT).show();
                    }
                })
                .build(findViewById(R.id.gl_view));
    }

    @Override
    public  boolean dispatchKeyEvent(KeyEvent event){
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                Log.i("Hiiiiiiiiiiiiii :parked", "clicked");
                enableSocket = true;
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                Log.i("Hiiiiiiiiiiiiii :parked", "clicked");
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                Log.i("Hiiiiiiiiiiiiii :parked", "clicked");
                break;
        }
        return false;
    }
    float prevX = -1;
    float prevY = -1;
    long prevClickTime = 0;
    boolean isParked = true;
    String keyCode = "";

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if(enableSocket) {

            long clickedTime = System.currentTimeMillis();
            double axisx = 1280 * 100000;
            double axisy = 720 * 100000;

            if ((1280 == event.getX() && 720 == event.getY())) {

                if (clickedTime - prevClickTime > 1500) {
                    prevClickTime = clickedTime;
                    if (isParked) {

                        Log.i("Hiiiiiiiiiiiiii :unpark", "clicked");
                        keyCode= "2";
                        sendKetPressedOverNetwork();
                        isParked = false;
                    } else {

                        Log.i("Hiiiiiiiiiiiiii :parked", "clicked");
                        keyCode = "3";
                        sendKetPressedOverNetwork();
                        isParked = true;

                    }
                }
            } else {
                if (axisx > prevX && axisy > prevY) {
                    Log.i("front", "clicked");
                    keyCode = "38";
                } else if (axisx > prevX && axisy < prevY) {

                    Log.i("Hiiiiiiiiiiiiii :right", "clicked");
                    keyCode = "39";
                } else if (axisx < prevX && axisy > prevY) {

                    Log.i("left", "clicked");
                    keyCode = "37";
                } else if (axisx < prevX && axisy < prevY) {

                    Log.i("Hiiiiiiiiiiiiii :back", "clicked");
                    keyCode = "40";
                }
                prevX = 100000 * event.getX();
                prevY = 100000 * event.getY();
                if(!TextUtils.isEmpty(keyCode)) {
                    sendKetPressedOverNetwork();
                }
            }
        }
        return false;
    }

    Socket socket;
    private void sendKetPressedOverNetwork() {

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {

                socket.emit("pressedKeyCode",keyCode);
                socket.disconnect();
            }

        }).on("event", new Emitter.Listener() {

            @Override
            public void call(Object... args) {

            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
            }

        }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                ((Exception)args[0]).printStackTrace();
            }

        });
        socket.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayerWrapper.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMediaPlayerWrapper.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMediaPlayerWrapper.resume();
    }
}
