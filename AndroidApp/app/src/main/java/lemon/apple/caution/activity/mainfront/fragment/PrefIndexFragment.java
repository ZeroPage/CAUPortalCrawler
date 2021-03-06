package lemon.apple.caution.activity.mainfront.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import lemon.apple.caution.AppDelegate;
import lemon.apple.caution.R;
import lemon.apple.caution.activity.BasicPrefActivity;
import lemon.apple.caution.model.helper.PrefHelper;
import lemon.apple.caution.service.CAUIntentService;

public class PrefIndexFragment extends Fragment {

    @InjectView(R.id.listView)
    ListView listView;
    HardCodeAdapter hardCodeAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // ActionBar Item을 사용하기 위함.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pref_index, container, false);
        ButterKnife.inject(this, view);

        HardCodeAdapter.Item[] items = {
                new HardCodeAdapter.Item("기본 설정"),
                new HardCodeAdapter.Item("강의명 설정"),
                new HardCodeAdapter.Item("초기화"),
        };
        hardCodeAdapter = new HardCodeAdapter(this);
        hardCodeAdapter.addAll(items);

        listView.setAdapter(hardCodeAdapter);
        listView.setOnItemClickListener(hardCodeAdapter);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.pref_index, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.send_email_develper).getIcon().setAlpha(255);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.send_email_develper: {
                Resources resources = getResources();
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"rino0601@naver.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "[" + resources.getString(R.string.app_name) + "]의 유저가 개발자에게");
                i.putExtra(Intent.EXTRA_TEXT, "(원하는_내용을_입력하세요)");
                try {
                    startActivity(Intent.createChooser(i, resources.getString(R.string.send_email_developer)));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    static class HardCodeAdapter extends ArrayAdapter<HardCodeAdapter.Item> implements AdapterView.OnItemClickListener {

        private final Fragment fragment;

        public HardCodeAdapter(Fragment fragment) {
            super(fragment.getActivity(), R.layout.adapter_pref_index);
            this.fragment = fragment;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            // view recycling pattern.
            ViewHolder holder;
            if (view != null) {
                holder = (ViewHolder) view.getTag();
            } else {
                LayoutInflater inflater = fragment.getActivity().getLayoutInflater();
                view = inflater.inflate(R.layout.adapter_pref_index, parent, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            }

            Item item = getItem(position);
            holder.title.setText(item.title);

            return view;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
                case 0: {
                    fragment.startActivityForResult(
                            BasicPrefActivity.getStartIntent(fragment.getActivity()),
                            BasicPrefActivity.REQUEST_CODE);
                }
                break;
                case 1: {

                }
                break;
                case 2: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
                    builder.setTitle(R.string.waring_can_not_undo)
                            .setIcon(R.drawable.ic_action_warning)
                            .setMessage("앱을 초기 상태로 되돌리겠습니까?")
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Context context = fragment.getActivity().getApplicationContext();
                                    AppDelegate.closeHelper(context);
                                    CAUIntentService.INTENT.REGISTER_ALARM.cancelAlarm(context);
                                    PrefHelper.getInstance(context).clear();
                                    dialogInterface.dismiss();
                                }
                            })
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            }).show();
                }
                break;
            }
        }

        static class Item {
            final String title;

            Item(String title) {
                this.title = title;
            }
        }

        static class ViewHolder {

            @InjectView(R.id.title)
            TextView title;

            ViewHolder(View view) {
                ButterKnife.inject(this, view);
            }
        }
    }
}
