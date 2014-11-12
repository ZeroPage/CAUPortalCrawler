package test.apple.lemon.cauportalcrawlertest.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import test.apple.lemon.cauportalcrawlertest.R;

/**
 * A login screen that offers login via email/password.
 */
public class BasicPrefActivity extends ActionBarActivity {

    public static final int REQUEST_CODE = 1001;
    // UI references.
    @InjectView(R.id.portalIdEditText)
    EditText portalIdEditText;
    @InjectView(R.id.passwordEditText)
    EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);
        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @OnClick(R.id.savePrefButton)
    void onClickSavePref() {
        // Reset errors.
        portalIdEditText.setError(null);
        passwordEditText.setError(null);

        // Store values at the time of the login attempt.
        String email = portalIdEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            portalIdEditText.setError(getString(R.string.error_field_required));
            focusView = portalIdEditText;
            cancel = true;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.error_field_required));
            focusView = passwordEditText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // todo prompt dlg
            finish();
        }
    }

    public static Intent getStartIntent(Activity activity) {
        return new Intent(activity,BasicPrefActivity.class);
    }
}



