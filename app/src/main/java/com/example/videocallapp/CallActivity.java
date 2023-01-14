package com.example.videocallapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class CallActivity extends AppCompatActivity {
    String username ="",friendUserName="";
    boolean isPeerConnected =false;
    DatabaseReference firebaseref = FirebaseDatabase.getInstance().getReference("users");
    boolean isAudio =true,isVideo=true;
    Button callBtn ;
    Button toggleAudioBtn;
    Button toggleVideoBtn ;
    WebView webView ;
    LinearLayout callLayout ;
    TextView incomingCallText ;
    Button acceptBtn ;
    Button rejectBtn ;
    RelativeLayout inputLayout ;
    LinearLayout callControlLayout;
    EditText friendName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        rejectBtn =findViewById(R.id.callEnd);
        friendName = findViewById(R.id.friendNameEdit);
        inputLayout =findViewById(R.id.inputLayout);
        callControlLayout=findViewById(R.id.callControlLayout);
        toggleAudioBtn =findViewById(R.id.toggleAudioBtn);
        callBtn =findViewById(R.id.callBtn);
        acceptBtn = findViewById(R.id.callPick);
        incomingCallText =findViewById(R.id.incomingCallText);
        webView =findViewById(R.id.webView);
        callLayout =findViewById(R.id.callLayout);
        toggleVideoBtn =findViewById(R.id.toggleVideoBtn);
        username = getIntent().getStringExtra("username");

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendUserName = friendName.getText().toString();
                sendCallRequest();

            }
        });
        
        toggleAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAudio=!isAudio;
                callJavascriptFunction("javascript:toggleAudio('"+isAudio+"')");
                if(isAudio) {
                    toggleAudioBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_mic_24,0,0,0);

                }
                else {
                    toggleAudioBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_mic_off_24,0,0,0);
                }

                
            }
        });
        
        toggleVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isVideo=!isVideo;
                callJavascriptFunction("javascript:toggleVideo('"+isVideo+"')");
                if(isVideo) {
                    toggleVideoBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_videocam_24,0,0,0);
                }
                else {
                    toggleVideoBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_videocam_off_24,0,0,0);
                }

            }
        });
        
        setupWebView();

    }

    private void sendCallRequest() {
        if(!isPeerConnected){
            Toast.makeText(this,"you are not connected to any server",Toast.LENGTH_SHORT).show();
            return;
        }
        firebaseref.child(friendUserName).child("incoming").setValue(username);
        firebaseref.child(friendUserName).child("isAvailable").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue().toString() == "true"){
                    listenForConnId();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void listenForConnId() {
        firebaseref.child(friendUserName).child("connId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() == null){
                    return;
                }
                switchToControls();
                callJavascriptFunction("javascript:startCall('"+snapshot.getValue()+"')");


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void setupWebView() {
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                super.onPermissionRequest(request);
            }
        });
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webView.addJavascriptInterface(new JavascriptInterface1(this),"Android");
        loadVideoCall();

    }

    private void loadVideoCall() {
        String filePath ="/home/sangharsh_g/AndroidStudioProjects/videocallapp/app/src/main/assets/call.html";
        webView.loadUrl(filePath);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                initializePeer();
            }
        });
    }

    String uniqueID ="";
    private void initializePeer() {
          uniqueID=UUID.randomUUID().toString();
          // is line mein js function ko kese call karein
        callJavascriptFunction("javascript:init('"+uniqueID+"')");
        firebaseref.child(username).child("incoming").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                onCallRequest(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void onCallRequest(String caller) {
        if(caller == null)
             return;

        callLayout.setVisibility(View.VISIBLE);
        incomingCallText.setText(caller+"is calling");
        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseref.child(username).child("connId").setValue(uniqueID);
                firebaseref.child(username).child("isAvailable").setValue(true);
                callLayout.setVisibility(View.GONE);
                switchToControls();
            }
        });
        rejectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseref.child(username).child("incoming").setValue(null);
                callLayout.setVisibility(View.GONE);


            }
        });


    }

    private void switchToControls() {
        inputLayout.setVisibility(View.GONE);
        callControlLayout.setVisibility(View.VISIBLE);

    }

    private void callJavascriptFunction(String functionString){
        webView.evaluateJavascript(functionString, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {

            }
        });
    }

    public void onPeerConnected() {
        isPeerConnected =true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        firebaseref.child(username).setValue(null);
        webView.loadUrl("about:blank");
        super.onDestroy();
    }
}