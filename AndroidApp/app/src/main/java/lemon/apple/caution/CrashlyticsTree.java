package lemon.apple.caution;

/**
 * Created by rino0601 on 2014. 8.1..
 */

import com.crashlytics.android.Crashlytics;

import timber.log.Timber;

/**
 * A logging implementation which reports 'info', 'warning', and 'error' logs to Crashlytics.
 */
public class CrashlyticsTree extends Timber.HollowTree {
    @Override
    public void i(String message, Object... args) {
        logMessage(message, args);
    }

    @Override
    public void i(Throwable t, String message, Object... args) {
        logMessage(message, args);
        // NOTE: We are explicitly not sending the exception to Crashlytics here.
    }

    @Override
    public void w(String message, Object... args) {
        logMessage("WARN: " + message, args);
    }

    @Override
    public void w(Throwable t, String message, Object... args) {
        logMessage("WARN: " + message, args);
        // NOTE: We are explicitly not sending the exception to Crashlytics here.
    }

    @Override
    public void e(String message, Object... args) {
        logMessage("ERROR: " + message, args);
    }

    @Override
    public void e(Throwable t, String message, Object... args) {
        logMessage("ERROR: " + message, args);
        Crashlytics.logException(t);
    }

    private void logMessage(String message, Object... args) {
        Crashlytics.log(String.format(message, args));
    }
}
