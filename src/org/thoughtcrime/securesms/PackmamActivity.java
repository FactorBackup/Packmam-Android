package org.thoughtcrime.securesms;


import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.http.util.EncodingUtils;

import java.net.URISyntaxException;

public class PackmamActivity extends AppCompatActivity {

    String payURL;
    String csrfmiddlewaretoken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packmam);
        WebView myWebView = (WebView) findViewById(R.id.webview);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.setWebChromeClient(new WebChromeClient());
        myWebView.setWebViewClient(new WebViewClientClass());
        myWebView.loadUrl("http://54.161.123.198:8000");
    }

    private class WebViewClientClass extends WebViewClient {
        // 디버그용
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        // 디버그용
        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        //https://github.com/iamport/iamport-manual/blob/master/%EC%9D%B8%EC%A6%9D%EA%B2%B0%EC%A0%9C/sample/jtnet.md
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            String APP_SCHEME = "iamporttestpackmam://";
            String IAMPORT_SERVICE = "https://www.my-service.com/payments/complete/mobile";
            String PAYMENT = "/pay/";
            if(url.startsWith(APP_SCHEME)) {
                String redirectURL = url.substring(APP_SCHEME.length()+3);
                view.loadUrl(redirectURL);
                return true;
            } else if (url.startsWith(IAMPORT_SERVICE)) {
                Uri uri = Uri.parse(url);
                String value = uri.getQueryParameter("imp_uid");
                csrfmiddlewaretoken = csrfmiddlewaretoken.replace("\"","" );
                String postData = "imp_uid=" + value + "&csrfmiddlewaretoken=" + csrfmiddlewaretoken;
                view.postUrl(payURL, EncodingUtils.getBytes(postData, "BASE64"));
                return true;
            } else if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("javascript:")) {
                Intent intent = null;

                try {
                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME); //IntentURI처리
                    Uri uri = Uri.parse(intent.getDataString());

                    startActivity(new Intent(Intent.ACTION_VIEW, uri)); //해당되는 Activity 실행
                    return true;
                } catch (URISyntaxException ex) {
                    return false;
                } catch (ActivityNotFoundException e) {
                    if ( intent == null )   return false;

                    if ( handleNotFoundPaymentScheme(intent.getScheme()) )  return true; //설치되지 않은 앱에 대해 사전 처리(Google Play이동 등 필요한 처리)

                    String packageName = intent.getPackage();
                    if (packageName != null) { //packageName이 있는 경우에는 Google Play에서 검색을 기본
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                        return true;
                    }

                    return false;
                }
            } else {
                if(url.endsWith(PAYMENT)) {
                    String javascriptEval = "javascript:(function(){return $('input[name=csrfmiddlewaretoken]').val();})()";
                    view.evaluateJavascript(javascriptEval, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            csrfmiddlewaretoken = s;
                        }
                    });
                    payURL = url;
                }
                Log.d("check URL",url);
                view.loadUrl(url);
                return true;
            }
        }

        private boolean handleNotFoundPaymentScheme(String scheme) {
            return true;
        }
    }
}