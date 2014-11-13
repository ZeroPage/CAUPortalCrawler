package test.apple.lemon.cauportalcrawlertest.activity.mainfront.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;

import java.sql.SQLException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import test.apple.lemon.cauportalcrawlertest.AppDelegate;
import test.apple.lemon.cauportalcrawlertest.R;
import test.apple.lemon.cauportalcrawlertest.activity.caufsm.CAUWebActivity;
import test.apple.lemon.cauportalcrawlertest.jsoupaser.HomeworkParser;
import test.apple.lemon.cauportalcrawlertest.jsoupaser.LectureContentsParser;
import test.apple.lemon.cauportalcrawlertest.jsoupaser.NoticeParser;
import test.apple.lemon.cauportalcrawlertest.jsoupaser.QnAParser;
import test.apple.lemon.cauportalcrawlertest.jsoupaser.SharedDataParser;
import test.apple.lemon.cauportalcrawlertest.jsoupaser.TeamProjectParser;
import test.apple.lemon.cauportalcrawlertest.model.EClassContent;
import test.apple.lemon.cauportalcrawlertest.model.LocalProperties;
import test.apple.lemon.cauportalcrawlertest.model.helper.OrmLiteAdapter;
import test.apple.lemon.cauportalcrawlertest.model.helper.PrefHelper;

/**
 * Created by rino0601 on 2014. 11. 12..
 */
public class ContentListFragment extends Fragment {

    @InjectView(R.id.listView)
    ListView listView;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_content_list, container, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            RuntimeExceptionDao<EClassContent, Integer> dao = AppDelegate.getHelper(getActivity()).getContentsDAO();
            PreparedQuery<EClassContent> query = dao.queryBuilder().orderBy(EClassContent.DATETIME_FIELD, false).prepare();
            Adapter adapter = new Adapter(getActivity(), dao, query);
            int count = adapter.getCount();
            if (count != 0) {
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(adapter);
            } else {
                // todo prompt login.
            }
        } catch (SQLException ignored) {
            ignored.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.content_list, menu);
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
