/*
 * Copyright (C) 2015 8tory, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    protected Action1<ParseRecyclerAdapter> mOnDataSetChanged;
    protected Action3<VH, Integer, T> mOnBindViewHolder;
    protected Func2<ViewGroup, Integer, VH> mOnCreateViewHolder;
    protected ParseQueryAdapter.QueryFactory<T> mFactory;

    public ParseRecyclerAdapter(Context context) {
        this(context, null);
    }

    public ParseRecyclerAdapter(Context context, ParseQueryAdapter.QueryFactory<T> factory) {
        this.context = context;
        if (factory != null) query(factory); // instead of mFactory = factory if load on setAdapter;
    }

    public ParseRecyclerAdapter query(ParseQueryAdapter.QueryFactory<T> factory) {
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
            if (mOnDataSetChanged != null) mOnDataSetChanged.call(ParseRecyclerAdapter.this);
        }));
        mParseAdapter.loadObjects(); // TODO reload() or load on setAdapter()

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
        return this;
    }

    public static <R extends ParseObject, V extends RecyclerView.ViewHolder> ParseRecyclerAdapter<R, V> from(Context context) {
        return new ParseRecyclerAdapter<R, V>(context);
    }

    public ParseRecyclerAdapter dataSetChanged(Action1<ParseRecyclerAdapter> onDataSetChanged) {
        this.mOnDataSetChanged = onDataSetChanged;
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

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) { // final, DO NOT Override until certainly
        mParentViewGroup = parent;
        return mOnCreateViewHolder.call(parent, new Integer(viewType)); // throw NullPointerException if mOnCreateViewHolder == null
    }

    public ParseRecyclerAdapter createViewHolder(Func2<ViewGroup, Integer, VH> onCreateViewHolder) {
        mOnCreateViewHolder = onCreateViewHolder;
        return this;
    }

    @Override
    public void onBindViewHolder(VH viewHolder, int position) { // final, DO NOT Override until certainly
        mParseAdapter.getView(position, viewHolder.itemView, mParentViewGroup);
        mViewHolderSubject.onNext(viewHolder);
        mPositionSubject.onNext(new Integer(position));
        if (position >= getItemCount() - 1) mParseAdapter.loadNextPage();
    }

    /** Super me if Override */
    public void onBindViewHolder(VH viewHolder, int position, T parseObject) { // final, DO NOT Override until certainly
        // throw NullPointerException? if mOnBindViewHolder == null
        if (mOnBindViewHolder != null) mOnBindViewHolder.call(viewHolder, new Integer(position), parseObject);
    }

    public ParseRecyclerAdapter bindViewHolder(Action3<VH, Integer, T> onBindViewHolder) {
        mOnBindViewHolder = onBindViewHolder;
        return this;
    }

    @Override
    public int getItemCount() {
        int i = mParseAdapter.getCount();
        return i - 6;
    }
}
