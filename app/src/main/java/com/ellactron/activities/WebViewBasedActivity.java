package com.ellactron.activities;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;

/**
 * Created by ji.wang on 2017-07-19.
 */

public abstract class WebViewBasedActivity extends BaseActivity {
    WebView mWebView = null;
    final String localResource = "/local/";

    abstract void addWebView(WebView mWebView);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addWebView(createWebView());
    }

    private WebView createWebView() {
        mWebView = new WebView(this);
        mWebView.setWebViewClient(new WebViewClient(){
            /*@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                if(req.getUrl().toString().contains(hostAddress)){
                    req.getRequestHeaders().put("Authorization", "Bearer "+token);
                    return true;
                }

                return super.shouldOverrideUrlLoading(view, req);
            }*/

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest req) {
                if(req.getUrl().toString().contains(getResources().getString(R.string.hostname))){
                    if(req.getUrl().toString().contains(getResources().getString(R.string.hostname) + localResource)){
                        return localRequest(req);
                    }
                    else {
                        return interceptRequest(req);
                    }
                }

                return super.shouldInterceptRequest(view, req);
            }
        });

        return mWebView;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private WebResourceResponse interceptRequest(WebResourceRequest req) {
        HttpResponse httpReponse = null;

        try {
            DefaultHttpClient client = new DefaultHttpClient();
            switch(req.getMethod().toUpperCase()){
                case "GET":
                    HttpGet httpGet = new HttpGet(req.getUrl().toString());
                    httpGet.setHeader("Authorization", "Bearer "+getSiteToken());
                    httpReponse = client.execute(httpGet);
                case "PUT":
                    HttpPut httpPut = new HttpPut(req.getUrl().toString());
                    httpPut.setHeader("Authorization", "Bearer "+getSiteToken());
                    httpReponse = client.execute(httpPut);
                case "POST":
                    HttpPost httpPost = new HttpPost(req.getUrl().toString());
                    httpPost.setHeader("Authorization", "Bearer "+getSiteToken());
                    httpReponse = client.execute(httpPost);
                case "DELETE":
                    HttpDelete httpDelete = new HttpDelete(req.getUrl().toString());
                    httpDelete.setHeader("Authorization", "Bearer "+getSiteToken());
                    httpReponse = client.execute(httpDelete);
            }

            Header contentType = httpReponse.getEntity().getContentType();
            Header encoding = httpReponse.getEntity().getContentEncoding();
            InputStream responseInputStream = httpReponse.getEntity().getContent();

            String contentTypeValue = null;
            String encodingValue = null;
            if (contentType != null) {
                contentTypeValue = contentType.getValue();
            }
            if (encoding != null) {
                encodingValue = encoding.getValue();
            }

            return new WebResourceResponse(contentTypeValue, encodingValue, responseInputStream);
        }
        catch(Exception e){
            Log.e(this.getClass().getName(), e.getMessage());
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private WebResourceResponse localRequest(WebResourceRequest req) {
        String path = req.getUrl().toString();
        path = path.substring(path.indexOf(localResource)+localResource.length());
        int resourceId = getResourceId(path, Drawable.class);
        if(0 == resourceId)
            return null;

        InputStream raw = getResources().openRawResource(resourceId);
        return new WebResourceResponse("image/png", "UTF-8", raw);
    }

    private int getResourceId(String resName, Class<?> c) {
        try {
            int id = this.getResources().getIdentifier(resName,
                    "drawable",
                    this.getPackageName());
            if (id == 0 && resName.contains(".")){
                id = this.getResources().getIdentifier(resName.substring(0,
                        resName.indexOf(".")),
                        "drawable",
                        this.getPackageName());
            }
            return id;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}