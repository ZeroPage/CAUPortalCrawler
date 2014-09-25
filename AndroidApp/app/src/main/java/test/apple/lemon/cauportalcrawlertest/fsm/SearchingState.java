package test.apple.lemon.cauportalcrawlertest.fsm;

import timber.log.Timber;

/**
 * Created by rino0601 on 2014. 9. 25..
 */
public enum SearchingState {
    START {

    };

    public SearchingState receiveURL(String url) {
        Timber.d(url);
        return this;
    }
}
