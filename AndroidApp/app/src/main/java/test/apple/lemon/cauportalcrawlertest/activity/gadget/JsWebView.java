package test.apple.lemon.cauportalcrawlertest.activity.gadget;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

import java.util.Timer;
import java.util.TimerTask;


/**
 * javaScript 결과 값을 가져오기 위해 만든 커스텀뷰. (인터페이스이면서, webview를 전달해줄 필요가 있었음.)
 * 이 방법이 싫다면 alert의 메세지를 통해서 가져오는 방법이 있다. -> 왠지 지금은 이 방법을 써야 할 것 같다!
 */
public class JsWebView extends WebView {

    private OnTimeoutListener onTimeoutListener = null;
    private Timer timer;
    private TimerTask task;

    public JsWebView(Context context) {
        super(context);
    }

    public JsWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public JsWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void loadUrl(String url) {
        if (task != null) {
            task.cancel();
        }
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                if (onTimeoutListener != null) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            onTimeoutListener.onTimeout(JsWebView.this);
                        }
                    });
                }
            }
        };
        timer.schedule(task, 15 * 1000); //15sec
        super.loadUrl(url);
    }


    public void setOnTimeoutListener(OnTimeoutListener onTimeoutListener) {
        this.onTimeoutListener = onTimeoutListener;
    }

    public static interface OnTimeoutListener {
        public void onTimeout(WebView webView);
    }
}
