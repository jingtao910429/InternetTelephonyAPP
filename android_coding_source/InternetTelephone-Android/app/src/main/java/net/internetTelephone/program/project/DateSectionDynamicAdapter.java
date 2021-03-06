package net.internetTelephone.program.project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import net.internetTelephone.program.FootUpdate;
import net.internetTelephone.program.R;
import net.internetTelephone.program.common.ClickSmallImage;
import net.internetTelephone.program.common.Global;
import net.internetTelephone.program.common.ImageLoadTool;
import net.internetTelephone.program.common.LongClickLinkMovementMethod;
import net.internetTelephone.program.common.MyImageGetter;
import net.internetTelephone.program.common.htmltext.URLSpanNoUnderline;
import net.internetTelephone.program.common.network.RefreshBaseFragment;
import net.internetTelephone.program.common.widget.DataAdapter;
import net.internetTelephone.program.login.auth.Utilities;
import net.internetTelephone.program.maopao.item.ContentAreaMuchImages;
import net.internetTelephone.program.model.DynamicObject;
import net.internetTelephone.program.user.UserDetailActivity_;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by bill on 16/3/23.
 */
public class DateSectionDynamicAdapter<T extends DynamicObject.DynamicBaseObject> extends DataAdapter<T> implements
        StickyListHeadersAdapter, SectionIndexer {

    private final LayoutInflater mInflater;
    protected ImageLoadTool mImageLoader;
    private MyImageGetter myImageGetter;
    public boolean  mNoMore = false;
    private int mLastId;
    FootUpdate.LoadMore mLoadMoreObj;
    private SimpleDateFormat mDataDyanmicItem = new SimpleDateFormat("HH:mm");
    String sToday = "";
    String sYesterday = "";

    private Context mContext;
    protected View.OnClickListener mOnClickUser = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String globalKey = (String) v.getTag();

            Intent intent = new Intent(mContext, UserDetailActivity_.class);
            intent.putExtra("globalKey", globalKey);
            mContext.startActivity(intent);
        }
    };


    View.OnClickListener onClickJump = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String s = (String) v.getTag();
            if (s.isEmpty()) {
                return;
            }

            URLSpanNoUnderline.openActivityByUri(v.getContext(), s, false);
        }
    };
    View.OnClickListener onClickParent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ((ViewGroup) v.getParent()).callOnClick();
        }
    };
    private ArrayList<Long> mSectionTitle = new ArrayList<>();
    private ArrayList<Integer> mSectionId = new ArrayList<>();

    public DateSectionDynamicAdapter(Context context, MyImageGetter imageGetter, FootUpdate.LoadMore loader) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mImageLoader = new ImageLoadTool();
        myImageGetter = imageGetter;
        mLoadMoreObj = loader;
        Calendar calendar = Calendar.getInstance();
        Long today = calendar.getTimeInMillis();
        sToday = Global.mDateFormat.format(today);
        Long yesterday = calendar.getTimeInMillis() - 1000 * 60 * 60 * 24;
        sYesterday = Global.mDateFormat.format(yesterday);
        resetLastId();
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    public void initSection() {
        mSectionTitle.clear();
        mSectionId.clear();

        if (getCount() > 0) {
            mSectionId.add(0);
            Calendar lastTime = Calendar.getInstance();
            lastTime.setTimeInMillis(getItem(0).created_at);
            Calendar nowTime = Calendar.getInstance();
            mSectionTitle.add(lastTime.getTimeInMillis());

            for (int i = 0; i < getCount(); ++i) {
                nowTime.setTimeInMillis(getItem(i).created_at);
                if (Utilities.isDifferentDay(lastTime, nowTime)) {
                    lastTime.setTimeInMillis(nowTime.getTimeInMillis());
                    mSectionTitle.add(lastTime.getTimeInMillis());
                    mSectionId.add(i);
                }
            }
        }
    }

    @Override
    public T getItem(int position) {
        return (T) super.getItem(position);
    }

    public int getItemResource(int position) {
        return R.layout.fragment_project_dynamic_list_item;
    }

    public ViewHolder createHolder(int position, View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.mTitle = (TextView) convertView.findViewById(R.id.title);
        holder.mTitle.setMovementMethod(LongClickLinkMovementMethod.getInstance());
        holder.mTitle.setFocusable(false);

        holder.mContent = (TextView) convertView.findViewById(R.id.content);
        holder.mContent.setMovementMethod(LongClickLinkMovementMethod.getInstance());
        holder.mContent.setOnClickListener(onClickParent);
        holder.mContent.setFocusable(false);

        holder.mLayoutClick = (ViewGroup) convertView.findViewById(R.id.layout0);
        holder.mLayoutClick.setOnClickListener(onClickJump);

        holder.mIcon = (ImageView) convertView.findViewById(R.id.icon);

        holder.mTime = (TextView) convertView.findViewById(R.id.time);
        holder.timeLineUp = convertView.findViewById(R.id.timeLineUp);
        holder.timeLinePoint = convertView.findViewById(R.id.timeLinePoint);
        holder.timeLineDown = convertView.findViewById(R.id.timeLineDown);
        holder.divideLeft = convertView.findViewById(R.id.divideLeft);
        holder.divideRight = convertView.findViewById(R.id.divideRight);

        convertView.setTag(holder);

        return holder;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        T data = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(getItemResource(position), parent, false);
            holder = createHolder(position, convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mTitle.setText(data.title());
        Spanned contentSpanned = data.content(myImageGetter);
        if (contentSpanned.length() == 0) {
            holder.mContent.setVisibility(View.GONE);
        } else {
            holder.mContent.setVisibility(View.VISIBLE);
            holder.mContent.setText(contentSpanned);
        }
        holder.mTime.setText(mDataDyanmicItem.format(data.created_at));

        holder.mLayoutClick.setTag(data.jump());

        int nowSection = getSectionForPosition(position);
        if (position == 0) {
            holder.timeLineUp.setVisibility(View.INVISIBLE);
        } else {
            int upItemSection = getSectionForPosition(position - 1);
            if (nowSection == upItemSection) {
                holder.timeLineUp.setVisibility(View.VISIBLE);
            } else {
                holder.timeLineUp.setVisibility(View.INVISIBLE);
            }
        }

        if (position == getCount() - 1) {
            if (mNoMore) {
                holder.timeLineDown.setVisibility(View.INVISIBLE);
            } else {
                holder.timeLineDown.setVisibility(View.VISIBLE);
            }
        } else {
            int downItemSection = getSectionForPosition(position + 1);
            if (nowSection == downItemSection) {
                holder.timeLineDown.setVisibility(View.VISIBLE);
            } else {
                holder.timeLineDown.setVisibility(View.INVISIBLE);
            }
        }

        if (holder.mIcon != null) {
            holder.mIcon.setOnClickListener(mOnClickUser);
            mImageLoader.loadImage(holder.mIcon, data.user.avatar);
            holder.mIcon.setTag(data.user.global_key);
        }

        if (getCount() - position <= 3) {
            if (!mNoMore && mLoadMoreObj != null) {
                int lastId = getItem(getCount() - 1).id;
                if (mLastId != (lastId)) {
                    mLastId = lastId;
                    mLoadMoreObj.loadMore();
                }
            }
        }

        afterGetView(position, convertView, parent, holder);

        return convertView;
    }

    public void afterGetView(int position, View convertView, ViewGroup parent, ViewHolder holder) {

    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = mInflater.inflate(getListSectionResourceId(), parent, false);
            holder.mHead = (TextView) convertView.findViewById(R.id.head);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        Long time = mSectionTitle.get(getSectionForPosition(position));
        String s = Global.mDateFormat.format(time);
        if (s.equals(sToday)) {
            s += " (今天)";
        } else if (s.equals(sYesterday)) {
            s += " (昨天)";
        }

        holder.mHead.setText(s);

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        return getSectionForPosition(position);
    }

    @Override
    public int getPositionForSection(int section) {
        return section;
    }

    @Override
    public int getSectionForPosition(int position) {
        for (int i = 0; i < mSectionId.size(); ++i) {
            if (position < mSectionId.get(i)) {
                return i - 1;
            }
        }

        return mSectionId.size() - 1;
    }

    @Override
    public Object[] getSections() {
        return mSectionTitle.toArray();
    }

    @Override
    public void notifyDataSetChanged() {
        initSection();
        super.notifyDataSetChanged();
    }

    class HeaderViewHolder {
        TextView mHead;
    }

    protected class ViewHolder {
        public ImageView mIcon;
        TextView mTitle;
        public TextView mContent;
        TextView mTime;

        ViewGroup mLayoutClick;

        public View timeLineUp;
        public View timeLinePoint;
        public View timeLineDown;

        public View divideLeft;
        protected View divideRight;
    }

    public void setDynamics(ArrayList<T> data) {
        super.setData(data);
    }


    protected int getListSectionResourceId() {
        return R.layout.fragment_project_dynamic_list_head;
    }

    public void setHasMore(boolean hasMore) {
        mNoMore = !hasMore;
    }

    public int lastId() {
        return  mLastId;
    }

    public void resetLastId() {
        mLastId = RefreshBaseFragment.UPDATE_ALL_INT;
    }

}
