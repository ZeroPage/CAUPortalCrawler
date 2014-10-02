package test.apple.lemon.cauportalcrawlertest.activity.gadget;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;


/**
 * javaScript 결과 값을 가져오기 위해 만든 커스텀뷰. (인터페이스이면서, webview를 전달해줄 필요가 있었음.)
 * 이 방법이 싫다면 alert의 메세지를 통해서 가져오는 방법이 있다. -> 왠지 지금은 이 방법을 써야 할 것 같다!
 * 
 *
 */
public class JsWebView extends WebView {
    private OnJsResultListener mOnJsResultListener = null;

    public JsWebView(Context context) {
        super(context);
    }

    public JsWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public JsWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @JavascriptInterface
    public void retrieve(String key, String android_val) { // 이거 안 씀.
        if (mOnJsResultListener != null)
            mOnJsResultListener.onJsResult(this, key, android_val);
    }

    public void setOnJsResultListener(OnJsResultListener onJsResultListener) {
        mOnJsResultListener = onJsResultListener;
    }

    public interface OnJsResultListener {
        void onJsResult(WebView webView, String key, String android_val);
    }
}
