package com.baihe.mytest;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.yungouos.pay.entity.PayOrder;
import com.yungouos.pay.order.SystemOrder;
import com.yungouos.pay.wxpay.WxPay;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    private static String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.INTERNET
    };
    private String mch_id = "1602333609";//商户号，yungouos平台申请
    private String key = "D29ADE73DC084C5EB434302014687FAF";//商户支付密匙
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private String outTradeNo;//订单号唯一
    public void payTest(View view){
        //开头随机7位 + 时间戳
        outTradeNo = "1"+new DecimalFormat("000000").format(new Random().nextInt(100000)) +
                System.currentTimeMillis() + "";
        if (checkPermissions(NEEDED_PERMISSIONS)){
            orderPayment();
        }else{
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        }
    }

    public WebView webView;
    private Boolean isWxPay = false;
    public void orderPayment(){
        webView = findViewById(R.id.webView);
        webView.setVisibility(View.INVISIBLE);//隐藏界面显示
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) {
                String h5payResult = WxPay.H5Pay(outTradeNo, "0.01", mch_id, "扫码测试支付",
                        null, null,"http://www.yungouos.com", null, null, null,  key);
                emitter.onNext(h5payResult);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String h5payResult) {
                        Log.e(TAG, "微信下单成功: "+h5payResult );//微信支付收银台的中间页面
                        Map<String, String> extraHeaders = new HashMap<String, String>();
                        extraHeaders.put("Referer", "http://www.yungouos.com");
                        webView.loadUrl(h5payResult, extraHeaders);

                        WebSettings webSettings = webView.getSettings();
                        webSettings.setJavaScriptEnabled(true);//支持javascript
                        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);  //设置 缓存模式
                        webSettings.setUseWideViewPort(true);//扩大比例的缩放
                        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);//自适应屏幕
                        webSettings.setLoadWithOverviewMode(true);

                        webView.requestFocus();//触摸焦点起作用
                        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);//取消滚动条

                        webView.setWebViewClient(new android.webkit.WebViewClient(){
                            @Override
                            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                //weixin://wap/pay?prepayid%3Dwx12163851787018726df1c8385fdf7c0000&package=4149489681&noncestr=1620808732&sign=de598dc49f2d999958ad58c13bf0c5e1
                                Log.e(TAG, "重定向的支付链接："+url );
                                if (url.startsWith("weixin://wap/pay?") || url.startsWith("http://weixin/wap/pay")){
                                    Log.e(TAG, "调起微信支付" );
                                    isWxPay = true;
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse(url));
                                    startActivity(intent);
                                    return true;
                                } else {
                                    Log.e(TAG, "shouldOverrideUrlLoading: 22222222222" );
                                    Map<String, String> extraHeaders = new HashMap<String, String>();
                                    extraHeaders.put("Referer", "http://wxpay.wxutil.com");
                                    view.loadUrl(url, extraHeaders);
                                }

                                return true;
                            }

                            @Override
                            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                                super.onPageStarted(view, url, favicon);
                                Log.e(TAG, url);
                            }
                            @Override
                            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                                handler.proceed();
                                super.onReceivedSslError(view, handler, error);
                            }

                        });

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isWxPay) {
            Log.e(TAG, "onResume: 支付调起成功返回页面，不知道付款成功与否");
            isWxPay = false;
            /**
             * 查询订单状态
             */
            Observable.create(new ObservableOnSubscribe<PayOrder>() {
                @Override
                public void subscribe(ObservableEmitter<PayOrder> emitter) {
                    PayOrder payOrder = SystemOrder.getOrderInfoByOutTradeNo(outTradeNo, mch_id, key);
                    emitter.onNext(payOrder);
                }
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<PayOrder>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(PayOrder payOrder) {
                            Log.e(TAG, "onNext: 查询系统订单返回结果：" + payOrder.toString());
                            if (payOrder.getPayStatus() == 1){
                                Log.e(TAG, "onNext: 成功" );
                                showToast("支付成功");
                            }else{
                                Log.e(TAG, "onNext: 失败" );
                                showToast("支付失败");
                            }
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }
    }

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            if (isAllGranted) {
                orderPayment();
            } else {
                showToast("权限未允许!");
            }
        }
    }
}