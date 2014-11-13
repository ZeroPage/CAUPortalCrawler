package lemon.apple.caution.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TimePicker;

import org.joda.time.DateTime;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import lemon.apple.caution.R;
import lemon.apple.caution.jsoupaser.HomeworkParser;
import lemon.apple.caution.jsoupaser.LectureContentsParser;
import lemon.apple.caution.jsoupaser.NoticeParser;
import lemon.apple.caution.jsoupaser.QnAParser;
import lemon.apple.caution.jsoupaser.SharedDataParser;
import lemon.apple.caution.jsoupaser.TeamProjectParser;
import lemon.apple.caution.model.LocalProperties;
import lemon.apple.caution.model.helper.PrefHelper;
import lemon.apple.caution.service.CAUIntentService;

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
    @InjectViews({R.id.noticeCheckBox, R.id.hwCheckBox, R.id.sharedDataCheckBox, R.id.teamCheckBox, R.id.qnACheckBox, R.id.lectureContentsCheckBox})
    List<CheckBox> checkBoxes;
    @InjectView(R.id.syncTimePicker)
    TimePicker syncTimePicker;

    private LocalProperties localProperties;


    public static Intent getStartIntent(Activity activity) {
        return new Intent(activity, BasicPrefActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_pref);
        ButterKnife.inject(this);
        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        PrefHelper.PrefDao<LocalProperties> prefDao = PrefHelper.getInstance(this).getPrefDao();
        localProperties = prefDao.loadData();
        portalIdEditText.setText(localProperties.getPortalId());
        passwordEditText.setText(localProperties.getPassword());
        ButterKnife.apply(checkBoxes, new ButterKnife.Action<CheckBox>() {
            @Override
            public void apply(CheckBox view, int index) {
                switch (view.getId()) {
                    case R.id.noticeCheckBox:
                        view.setChecked(localProperties.getChecked(NoticeParser.BOARD_INDEX));
                        break;
                    case R.id.hwCheckBox:
                        view.setChecked(localProperties.getChecked(HomeworkParser.BOARD_INDEX));
                        break;
                    case R.id.sharedDataCheckBox:
                        view.setChecked(localProperties.getChecked(SharedDataParser.BOARD_INDEX));
                        break;
                    case R.id.teamCheckBox:
                        view.setChecked(localProperties.getChecked(TeamProjectParser.BOARD_INDEX));
                        break;
                    case R.id.qnACheckBox:
                        view.setChecked(localProperties.getChecked(QnAParser.BOARD_INDEX));
                        break;
                    case R.id.lectureContentsCheckBox:
                        view.setChecked(localProperties.getChecked(LectureContentsParser.BOARD_INDEX));
                        break;
                }
            }
        });
        syncTimePicker.setCurrentHour(localProperties.getScheduledHour());
        syncTimePicker.setCurrentMinute(localProperties.getScheduledMinute());
    }

    @OnClick(R.id.savePrefButton)
    void onClickSavePref() {
        // Reset errors.
        portalIdEditText.setError(null);
        passwordEditText.setError(null);

        // Store values at the time of the login attempt.
        String portalId = portalIdEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid
        if (TextUtils.isEmpty(portalId)) {
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
            PrefHelper.PrefDao<LocalProperties> prefDao = PrefHelper.getInstance(this).getPrefDao();
            localProperties.setPortalId(portalId);
            localProperties.setPassword(password);
            localProperties.setScheduledHour(syncTimePicker.getCurrentHour());
            localProperties.setScheduledMinute(syncTimePicker.getCurrentMinute());
            prefDao.saveData(localProperties);

            DateTime dateTime = new DateTime().hourOfDay().setCopy(localProperties.getScheduledHour())
                    .minuteOfHour().setCopy(localProperties.getScheduledMinute());
            if (dateTime.isBeforeNow()) {
                dateTime = dateTime.plusDays(1);
            }
            dateTime=dateTime.secondOfMinute().withMinimumValue();
            CAUIntentService.INTENT.REGISTER_ALARM.setAlarm(this, dateTime.toDate());

            AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
            alt_bld.setTitle("동기화")
                    .setIcon(R.drawable.ic_action_about)
                    .setMessage("지금 즉시 동기화를 하시겠습니까?").setCancelable(
                    false).setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            CAUIntentService.INTENT.INSTANT_START_FSM.start(BasicPrefActivity.this);
                            dialog.dismiss();
                            finish();
                        }
                    }).setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            finish();
                        }
                    }).show();
        }
    }

    @OnCheckedChanged({R.id.noticeCheckBox, R.id.hwCheckBox, R.id.sharedDataCheckBox, R.id.teamCheckBox, R.id.qnACheckBox, R.id.lectureContentsCheckBox})
    void onCheckedChanged(CompoundButton view, boolean isChecked) {
        switch (view.getId()) {
            case R.id.noticeCheckBox:
                localProperties.setChecked(NoticeParser.BOARD_INDEX, isChecked);
                break;
            case R.id.hwCheckBox:
                localProperties.setChecked(HomeworkParser.BOARD_INDEX, isChecked);
                break;
            case R.id.sharedDataCheckBox:
                localProperties.setChecked(SharedDataParser.BOARD_INDEX, isChecked);
                break;
            case R.id.teamCheckBox:
                localProperties.setChecked(TeamProjectParser.BOARD_INDEX, isChecked);
                break;
            case R.id.qnACheckBox:
                localProperties.setChecked(QnAParser.BOARD_INDEX, isChecked);
                break;
            case R.id.lectureContentsCheckBox:
                localProperties.setChecked(LectureContentsParser.BOARD_INDEX, isChecked);
                break;
        }
    }
}



