package com.ruswizards.rwgallery.RecyclerView;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ruswizards.rwgallery.R;

/**
 * Copyright (C) 2014 Rus Wizards
 * <p/>
 * Created: 16.01.2015
 * Vladimir Farafonov
 */
public class CustomRecyclerViewAdapter extends RecyclerView.Adapter<CustomRecyclerViewAdapter.ViewHolder> {

	private static final String LOG_TAG = "CustomRecyclerViewAdapter";
	private String[] dataSet_;

	public CustomRecyclerViewAdapter(String[] dataSet){
		dataSet_ = dataSet;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		Log.d(LOG_TAG, "onCreateViewHolder");
		View newView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_view_item, viewGroup, false);
		return new ViewHolder(newView);
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int i) {
		Log.d(LOG_TAG, "onBindViewHolder");
		viewHolder.getTextView().setText(dataSet_[i]);
	}

	@Override
	public int getItemCount() {
		Log.d(LOG_TAG, "getItemCount");
		return dataSet_.length;
	}

	public static class ViewHolder extends RecyclerView.ViewHolder{
		private TextView textView_;
		private static final String LOG_TAG = "CustomRecyclerViewAdapter.ViewHolder";

		public ViewHolder(View itemView) {
			super(itemView);
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d(LOG_TAG, "Clicked on: " + getPosition());
				}
			});
			textView_ = (TextView) itemView.findViewById(R.id.text_view);
		}

		public TextView getTextView(){
			return textView_;
		}
	}

}
