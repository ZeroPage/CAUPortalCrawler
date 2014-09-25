package test.apple.lemon.cauportalcrawlertest.fsm;

import timber.log.Timber;

/**
 * Created by rino0601 on 2014. 9. 25..
 */
public enum EnteringState {
    START {

    };

    public EnteringState receiveURL(String url) {
        Timber.d(url);
        return this;
    }
}
