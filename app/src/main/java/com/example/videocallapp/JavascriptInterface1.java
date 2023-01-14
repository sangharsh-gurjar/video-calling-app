package com.example.videocallapp;

import android.content.Context;
import android.webkit.JavascriptInterface;

class JavascriptInterface1 {
    Context context;
    CallActivity callActivity;
    JavascriptInterface1(Context c){
        context=c;
        callActivity =(CallActivity) context;
    }

    @android.webkit.JavascriptInterface
    public void onPeerConnected(){
       callActivity.onPeerConnected();
    }

}