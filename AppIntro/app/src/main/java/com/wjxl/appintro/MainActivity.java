package com.wjxl.appintro;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.github.lzyzsd.jsbridge.DefaultHandler;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.finalteam.galleryfinal.FunctionConfig;
import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.model.PhotoInfo;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final int REQUEST_CODE_GALLERY = 1001;
    private final String TAG = "MainActivity";

    BridgeWebView webView;
    Button button;
    ImageView image;
    //模拟用户获取本地位置
    User user = new User();

    private ArrayList<String> path = new ArrayList<>();

    Uri imageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
            "test.jpg"));

    ValueCallback<Uri> mUploadMessage;

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_send:
//                webView.callHandler("getpicturebyte", new Gson().toJson(user), new CallBackFunction() {
//                    @Override
//                    public void onCallBack(String data) {
//
//                    }
//                });

                //带配置
                FunctionConfig config = new FunctionConfig.Builder()
                        .setMutiSelectMaxSize(10)
                        .build();
                GalleryFinal.openGalleryMuti(REQUEST_CODE_GALLERY, config, new GalleryFinal.OnHanlderResultCallback() {
                    @Override
                    public void onHanlderSuccess(int reqeustCode, List<PhotoInfo> resultList) {
                        Toast.makeText(MainActivity.this,"成功",Toast.LENGTH_SHORT).show();
                        webView.callHandler("takepicture", resultList.toString(), new CallBackFunction() {
                            @Override
                            public void onCallBack(String data) {
                                //Toast.makeText(MainActivity.this,"上传成功",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onHanlderFailure(int requestCode, String errorMsg) {
                        Toast.makeText(MainActivity.this,"失败",Toast.LENGTH_SHORT).show();
                    }
                });

                break;
        }
    }

    static class Location {
        String address;
    }

    static class User {
        String name;
        Location location;
        String testStr;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (BridgeWebView) findViewById(R.id.webView);
        image = (ImageView) findViewById(R.id.image);
        button = (Button) findViewById(R.id.btn_send);
        button.setOnClickListener(this);
        webView.setDefaultHandler(new DefaultHandler());
        Location location = new Location();
        location.address = "上海";
        user.location = location;
        user.name = "Bruce";




        webView.setWebChromeClient(new WebChromeClient() {

            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType, String capture) {
                this.openFileChooser(uploadMsg);
            }

            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType) {
                this.openFileChooser(uploadMsg);
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                pickFile();
            }
        });
        //加载本地网页
        webView.loadUrl("file:///android_asset/testWebViewJsBridge.html");
        //加载服务器网页
        //webView.loadUrl("https://www.baidu.com");

        //必须和js同名函数，注册具体执行函数，类似java实现类。
        webView.registerHandler("takepicture", new BridgeHandler() {

            @Override
            public void handler(String data, CallBackFunction function) {

                //String str = "这是html返回给java的数据:" + data;
                // 弹出选择框
                showPopFormBottom();
                function.onCallBack("图片字节流"+imageUri);
            }

        });
        webView.registerHandler("openFile", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                String str = "这是html返回给java的数据:" + data;
                //pickFile();
                function.onCallBack(str);
            }
        });

        webView.registerHandler("saveFile", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                String str = "这是java返回给html的数据:" + data;
                function.onCallBack(str);
            }
        });

        webView.send("hello");

    }

    TakePhotoPopWin takePhotoPopWin;

    //弹出图片选择框
    public void showPopFormBottom() {
        View view = LayoutInflater.from(this).inflate(R.layout.take_photo_pop, null);
        takePhotoPopWin = new TakePhotoPopWin(this, onClickListener);
        //showAtLocation(View parent, int gravity, int x, int y)
        takePhotoPopWin.showAtLocation(view, Gravity.CENTER, 0, 0);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.btn_take_photo:
                    pickFile();
                    break;
                case R.id.btn_pick_photo:
                    //带配置
                    FunctionConfig config = new FunctionConfig.Builder()
                            .setMutiSelectMaxSize(10)
                            .build();
                    GalleryFinal.openGalleryMuti(REQUEST_CODE_GALLERY, config, new GalleryFinal.OnHanlderResultCallback() {
                        @Override
                        public void onHanlderSuccess(int reqeustCode, List<PhotoInfo> resultList) {
                            Toast.makeText(MainActivity.this,"成功",Toast.LENGTH_SHORT).show();
                            webView.callHandler("takepicture", resultList.toString(), new CallBackFunction() {
                                @Override
                                public void onCallBack(String data) {
                                    //Toast.makeText(MainActivity.this,"上传成功",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onHanlderFailure(int requestCode, String errorMsg) {
                            Toast.makeText(MainActivity.this,"失败",Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
            }
        }
    };

    /**
     * 把Bitmap转Byte
     */
    public static byte[] Bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap BitmapCompressUtil(Bitmap image,int size){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 85, out);
        float zoom = (float)Math.sqrt(size * 1024 / (float)out.toByteArray().length);

        Matrix matrix = new Matrix();
        matrix.setScale(zoom, zoom);

        Bitmap result = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);

        out.reset();
        result.compress(Bitmap.CompressFormat.JPEG, 85, out);
        while(out.toByteArray().length > size * 1024){
            System.out.println(out.toByteArray().length);
            matrix.setScale(0.9f, 0.9f);
            result = Bitmap.createBitmap(result, 0, 0, result.getWidth(), result.getHeight(), matrix, true);
            out.reset();
            result.compress(Bitmap.CompressFormat.JPEG, 85, out);
        }
        return result;
    }

    public void pickFile() {
        Intent chooserIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        chooserIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(chooserIntent, 1);
    }

    String params;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult: "+requestCode+"resultCode"+requestCode);


    }


    /**
     * @param
     * @param bytes
     * @param opts
     * @return Bitmap
     */
    public static Bitmap getPicFromBytes(byte[] bytes, BitmapFactory.Options opts) {
        if (bytes != null)
            if (opts != null)
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length,
                        opts);
            else
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return null;
    }


}
