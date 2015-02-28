package com.parse.compat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.parse.*;

import java.util.List;

import rx.android.app.*;
import rx.android.schedulers.*;
import rx.android.view.*;
import rx.functions.*;
import rx.Observable;
import rx.schedulers.*;
import rx.subjects.*;

public class ParseRecyclerAdapter<T extends ParseObject, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private ParseQueryAdapter<T> mParseAdapter;
    private Subject<T, T> mParseObjectSubject = new SerializedSubject<>(PublishSubject.create());
    private Subject<VH, VH> mViewHolderSubject = new SerializedSubject<>(PublishSubject.create());
    private Subject<Integer, Integer> mPositionSubject = new SerializedSubject<>(PublishSubject.create());
    private ViewGroup mParentViewGroup;
    protected Context context;
    protected Action1<ParseRecyclerAdapter> onDataSetChanged;
    protected Action3<VH, Integer, T> mOnBindViewHolder;

    public ParseRecyclerAdapter(Context context, ParseQueryAdapter.QueryFactory<T> factory) {
        this.context = context;
        mParseAdapter = new ParseQueryAdapter<T>(context, factory) {
            @Override
            public View getItemView(T parseObject, View view, ViewGroup parent) {
                mParseObjectSubject.onNext(parseObject);
                super.getItemView(parseObject, view, parent);
                return view;
            }
            @Override
            public View getNextPageView(View view, ViewGroup parent) {
                loadNextPage();
                return view;
            }
        };
        // enable pagination until inflate layout for findTextViewById(android.R.id.text1) in getNextPage()
        //mParseAdapter.setPaginationEnabled(false);
        mParseAdapter.addOnQueryLoadListener(new SimpleOnQueryLoadListener<T>((objects, e) -> {
            ParseRecyclerAdapter.this.notifyDataSetChanged();
            if (onDataSetChanged != null) onDataSetChanged.call(ParseRecyclerAdapter.this);
        }));
        mParseAdapter.loadObjects();

        Observable.zip(
                mParseObjectSubject.asObservable(), // <- parseAdapterQuery.getItemView() <- parseAdapterQuery.getView()
                mViewHolderSubject.asObservable(), // <- onBindViewHolder(ViewHolder, ...)
                mPositionSubject.asObservable(), (o, viewHolder, position) -> { // <- onBindViewHolder(..., position)
                    onBindViewHolder(viewHolder, position, o);
                    return o;
                }
            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe();
    }

    public ParseRecyclerAdapter onDataSetChanged(Action1<ParseRecyclerAdapter> onDataSetChanged) {
        this.onDataSetChanged = onDataSetChanged;
        return this;
    }

    public static class SimpleOnQueryLoadListener<R extends ParseObject> implements ParseQueryAdapter.OnQueryLoadListener<R> {
        Action0 onLoading;
        Action2<List<R>, Exception> onLoaded;

        @Override
        public void onLoading() {
            if (onLoading != null) onLoading.call();
        }

        @Override
        public void onLoaded(List<R> objects, Exception e) {
            if (onLoaded != null) onLoaded.call(objects, e);
        }

        public SimpleOnQueryLoadListener(Action0 onLoading, Action2<List<R>, Exception> onLoaded) {
            this.onLoading = onLoading;
            this.onLoaded = onLoaded;
        }

        public SimpleOnQueryLoadListener(Action0 onLoading) {
            this.onLoading = onLoading;
        }

        public SimpleOnQueryLoadListener(Action2<List<R>, Exception> onLoaded) {
            this.onLoaded = onLoaded;
        }

        public SimpleOnQueryLoadListener onLoading(Action0 onLoading) {
            this.onLoading = onLoading;
            return this;
        }

        public SimpleOnQueryLoadListener onLoaded(Action2<List<R>, Exception> onLoaded) {
            this.onLoaded = onLoaded;
            return this;
        }
    }

    /**
     * super me
     */
    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        mParentViewGroup = parent;
        return null;
    }

    @Override
    public void onBindViewHolder(VH viewHolder, int position) {
        mParseAdapter.getView(position, viewHolder.itemView, mParentViewGroup);
        mViewHolderSubject.onNext(viewHolder);
        mPositionSubject.onNext(new Integer(position));
        if (position >= getItemCount() - 1) mParseAdapter.loadNextPage();
    }

    /** Super me if Override */
    public void onBindViewHolder(VH viewHolder, int position, T parseObject) {
        if (mOnBindViewHolder != null) mOnBindViewHolder.call(viewHolder, new Integer(position), parseObject);
    }

    public ParseRecyclerAdapter onBindViewHolder(Action3<VH, Integer, T> onBindViewHolder) {
        mOnBindViewHolder = onBindViewHolder;
        return this;
    }

    @Override
    public int getItemCount() {
        int i = mParseAdapter.getCount();
        return i - 6;
    }
}
