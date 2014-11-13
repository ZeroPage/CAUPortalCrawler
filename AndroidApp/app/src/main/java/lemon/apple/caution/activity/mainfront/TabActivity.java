package lemon.apple.caution.activity.mainfront;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import java.util.HashMap;

import lemon.apple.caution.R;
import lemon.apple.caution.activity.mainfront.fragment.ContentListFragment;
import lemon.apple.caution.activity.mainfront.fragment.PrefIndexFragment;

public class TabActivity extends ActionBarActivity {
    private HashMap<Class<? extends Fragment>, TabItem> fragments = new HashMap<Class<? extends Fragment>, TabItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Notice that setContentView() is not used, because we use the root
        // android.R.id.content as the container for each fragment <-####

        // setup action bar for tabs
        //todo may : 스크롤 탭 구현.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setIcon(R.drawable.ic_logo);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            actionBar.setDisplayShowTitleEnabled(true);

            TabItem[] tabItems = new TabItem[]{
                    getTabItem(ContentListFragment.class, "최근 EClass", R.drawable.ic_action_email),
                    getTabItem(PrefIndexFragment.class, "설정", R.drawable.ic_action_settings),
            };

            for (TabItem item : tabItems) {
                ActionBar.Tab tab;
                tab = actionBar.newTab()
                        .setTabListener(new TabListener(
                                this,
                                item.getTag(),
                                item.getCls()))
                        .setIcon(item.getIconResId());
                actionBar.addTab(tab);
            }
        } else {
            throw new IllegalStateException("NO_ACTION_BAR");
        }
    }

    private synchronized TabItem getTabItem(Class<? extends Fragment> aClass, String title, int icon) {
        if (!fragments.containsKey(aClass)) {
            TabItem tabItem = new TabItem(aClass, title, icon);
            fragments.put(aClass, tabItem);
        }
        return fragments.get(aClass);
    }

    private static class TabItem {
        private Class<? extends Fragment> cls;
        private String title;
        private int iconResId;

        private TabItem(Class<? extends Fragment> aClass, String title, int icon) {
            this.cls = aClass;
            this.title = title;
            this.iconResId = icon;
        }

        public Class<? extends Fragment> getCls() {
            return cls;
        }

        public String getTag() {
            return cls.getName();
        }

        public String getTitle() {
            return title;
        }

        public int getIconResId() {
            return iconResId;
        }
    }

    public static class TabListener implements ActionBar.TabListener {
        private final ActionBarActivity mActivity;
        private final String mTag;
        private final Class<? extends Fragment> mClass;
        private Fragment mFragment;

        /**
         * Constructor used each time a new tab is created.
         *
         * @param activity The host Activity, used to instantiate the fragment
         * @param tag      The identifier tag for the fragment
         * @param clz      The fragment's Class, used to instantiate the fragment
         */
        public TabListener(ActionBarActivity activity, String tag, Class<? extends Fragment> clz) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
        }

        /* The following are each of the ActionBar.TabListener callbacks */
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            // Check if the fragment is already initialized
            if (mFragment == null) {
                // If not, instantiate and add it to the activity
                mFragment = Fragment.instantiate(mActivity, mClass.getName());
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                // If it exists, simply attach it in order to show it
                ft.attach(mFragment);
            }
        }

        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                // Detach the fragment, because another one is being attached
                ft.detach(mFragment);
            }
        }

        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            // User selected the already selected tab. Usually do nothing.
        }
    }
}
