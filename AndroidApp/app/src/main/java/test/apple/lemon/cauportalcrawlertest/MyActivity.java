package test.apple.lemon.cauportalcrawlertest;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import test.apple.lemon.cauportalcrawlertest.fsm.EnteringStateManager;
import test.apple.lemon.cauportalcrawlertest.fsm.SearchingStateManager;
import timber.log.Timber;


public class MyActivity extends Activity {

    @InjectView(R.id.webView)
    WebView webView;

    @InjectView(R.id.popupView)
    WebView popupView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.plant(new Timber.DebugTree());
        setContentView(R.layout.activity_my);
        ButterKnife.inject(this);

        EnteringStateManager stateManager = new EnteringStateManager(webView, new SearchingStateManager(popupView));
        stateManager.start();
    }


}

