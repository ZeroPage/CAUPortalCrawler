package lemon.apple.caution.activity.mainfront.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.UpdateBuilder;

import java.sql.SQLException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import lemon.apple.caution.AppDelegate;
import lemon.apple.caution.R;
import lemon.apple.caution.activity.BasicPrefActivity;
import lemon.apple.caution.activity.caufsm.CAUWebActivity;
import lemon.apple.caution.jsoupaser.HomeworkParser;
import lemon.apple.caution.jsoupaser.LectureContentsParser;
import lemon.apple.caution.jsoupaser.NoticeParser;
import lemon.apple.caution.jsoupaser.QnAParser;
import lemon.apple.caution.jsoupaser.SharedDataParser;
import lemon.apple.caution.jsoupaser.TeamProjectParser;
import lemon.apple.caution.model.EClassContent;
import lemon.apple.caution.model.LocalProperties;
import lemon.apple.caution.model.helper.OrmLiteAdapter;
import lemon.apple.caution.model.helper.PrefHelper;
import lemon.apple.caution.service.CAUIntentService;
import timber.log.Timber;

/**
 * Created by rino0601 on 2014. 11. 12..
 */
public class ContentListFragment extends Fragment {

    @InjectView(R.id.listView)
    ListView listView;

    @InjectView(R.id.tutorialLayout)
    RelativeLayout tutorialLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // ActionBar Item을 사용하기 위함.
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_content_list, container, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadAdapter();
    }

    private void reloadAdapter() {
        try {
            RuntimeExceptionDao<EClassContent, Integer> dao = AppDelegate.getHelper(getActivity()).getContentsDAO();
            PreparedQuery<EClassContent> query = dao.queryBuilder().orderBy(EClassContent.DATETIME_FIELD, false).prepare();
            Adapter adapter = new Adapter(getActivity(), dao, query);
            int count = adapter.getCount();
            if (count != 0) {
                listView.setVisibility(View.VISIBLE);
                tutorialLayout.setVisibility(View.GONE);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(adapter);
            } else {
                listView.setVisibility(View.GONE);
                tutorialLayout.setVisibility(View.VISIBLE);
            }
        } catch (SQLException ignored) {
            ignored.printStackTrace();
        }
        getActivity().invalidateOptionsMenu();
    }

    @OnClick(R.id.initButton)
    void initButtonClick(Button button) {
        Intent startIntent = BasicPrefActivity.getStartIntent(getActivity());
        startActivity(startIntent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.content_list, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        LocalProperties localProperties = PrefHelper.getInstance(getActivity()).getPrefDao().loadData();
        boolean haveToDisable = localProperties.getPortalId().isEmpty() || localProperties.getPassword().isEmpty();
        MenuItem markAsRead = menu.findItem(R.id.mark_as_read);
        markAsRead.setEnabled(!haveToDisable);
        markAsRead.getIcon().setAlpha(haveToDisable ? 130 : 255);
        MenuItem crawlNow = menu.findItem(R.id.crawl_now);
        crawlNow.setEnabled(!haveToDisable);
        crawlNow.getIcon().setAlpha(haveToDisable ? 130 : 255);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mark_as_read: {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setIcon(R.drawable.ic_action_warning)
                        .setTitle(R.string.waring_can_not_undo)
                        .setMessage("모든 알림을 읽음으로 표시하시겠습니까?\n(취소 할 수 없습니다.)")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Context context = getActivity();
                                RuntimeExceptionDao<EClassContent, Integer> dao = AppDelegate.getHelper(context).getContentsDAO();
                                UpdateBuilder<EClassContent, Integer> updateBuilder = dao.updateBuilder();
                                try {
                                    updateBuilder.where().eq(EClassContent.ALREADY_READ_FIELD, false);
                                    updateBuilder.updateColumnValue(EClassContent.ALREADY_READ_FIELD, true);
                                    updateBuilder.update();
                                } catch (SQLException e) {
                                    Timber.e(e, "SQLException-update");
                                }
                                dialogInterface.dismiss();
                                reloadAdapter();
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
                return true;
            }
            case R.id.crawl_now: {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setIcon(R.drawable.ic_action_about)
                        .setTitle("즉시 업데이트")
                        .setMessage("지금 즉시 동기화를 시작 할까요?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                CAUIntentService.INTENT.INSTANT_START_FSM.start(getActivity());
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    static class Adapter extends OrmLiteAdapter<EClassContent, Integer> implements AdapterView.OnItemClickListener { // 일단 지금은 reload를 지원하지 않음.

        private final Context context;
        private final LocalProperties localProperties;

        public Adapter(Context context, RuntimeExceptionDao<EClassContent, Integer> contentsDAO, PreparedQuery<EClassContent> preparedQuery) {
            super(contentsDAO, preparedQuery);
            this.context = context;
            localProperties = PrefHelper.getInstance(context).getPrefDao().loadData();
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            ViewHolder holder;
            if (view != null) {
                holder = (ViewHolder) view.getTag();
            } else {
                view = LayoutInflater.from(context).inflate(R.layout.adapter_content_list, parent, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            }

            EClassContent item = getItem(position);
            holder.categoryTextView.setText(lectureName(item.getLecture()) + " > " + boardName(item.getBoard()));
            holder.titleTextView.setText(item.getTitle());
            int res = item.isAlreadyRead() ? R.drawable.ic_action_read : R.drawable.ic_action_unread;
            Glide.with(context).load(res).into(holder.isReadImageView);
            return view;
        }

        private String boardName(int board) {
            switch (board) {
                case NoticeParser.BOARD_INDEX:
                    return "공지사항";
                case LectureContentsParser.BOARD_INDEX:
                    return "강의컨텐츠";
                case HomeworkParser.BOARD_INDEX:
                    return "과제방";
                case TeamProjectParser.BOARD_INDEX:
                    return "팀프로젝트";
                case SharedDataParser.BOARD_INDEX:
                    return "공유자료실";
                case QnAParser.BOARD_INDEX:
                    return "과목Q&A";
                default:
                    return "기타게시판";
            }
        }

        private String lectureName(int lecture) {
            return localProperties.getLectureName(lecture);
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            EClassContent item = getItem(position);
            CAUWebActivity.start(context, item.getLecture(), item.getBoard(), item.getIndex());

            item.setAlreadyRead(true);
            AppDelegate.getHelper(context).getContentsDAO().update(item);
            notifyDataSetChanged();
        }

        static class ViewHolder {
            @InjectView(R.id.isReadImageView)
            ImageView isReadImageView;
            @InjectView(R.id.titleTextView)
            TextView titleTextView;
            @InjectView(R.id.categoryTextView)
            TextView categoryTextView;

            public ViewHolder(View view) {
                ButterKnife.inject(this, view);
            }
        }
    }
}
