package test.apple.lemon.cauportalcrawlertest.activity.mainfront;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import test.apple.lemon.cauportalcrawlertest.R;
import test.apple.lemon.cauportalcrawlertest.activity.caufsm.CAUFSMActivity;
import test.apple.lemon.cauportalcrawlertest.activity.contentlist.ContentListActivity;

public class TabActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);
    }

    @Override
    protected void onResume() {
        super.onResume();
        CAUFSMActivity.start(this);
        //ContentListActivity.start(this);
    }
}
