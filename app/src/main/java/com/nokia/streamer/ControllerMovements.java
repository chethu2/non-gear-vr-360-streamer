package com.nokia.streamer;

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
/**
 * Created by chea on 5/26/2018.
 */

public class ControllerMovements extends VideoPlayerActivity {
    private boolean enableSocket = false;

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
}
