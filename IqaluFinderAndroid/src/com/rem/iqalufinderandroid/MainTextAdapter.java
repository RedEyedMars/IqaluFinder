package com.rem.iqalufinderandroid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.rem.ifinder.Finder;
import com.rem.ifinder.FinderEvent;
import com.rem.ifinder.Info;

import android.widget.ArrayAdapter;
import android.widget.Filter;

public class MainTextAdapter extends ArrayAdapter<Info> {
	private Finder finder;
	private List<Info> mList;
	
	public MainTextAdapter(MainActivity context, int textViewResourceId, Finder finder) {
		super(context, textViewResourceId);
		this.finder = finder;
		mList = new ArrayList<Info>();
		mList.addAll(finder.getAllInfos());
		this.addAll(mList);
		
	}

	private Filter mFilter = new Filter() {
		@Override
		public String convertResultToString(Object resultValue) {
			return ((Info)resultValue).getName();
		}

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();

			if (constraint != null) {
				FinderEvent event = finder.get(constraint.toString());
				android.util.Log.e("Info:"+constraint.toString(),event.getInfos().toString());

				results.values = event.getInfos();
				results.count = event.numberOfInfos();
			}

			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			clear();
			if (results != null && results.count > 0) {
				addAll((Collection<Info>) results.values);
			} else {
				addAll(mList);
			}
			notifyDataSetChanged();
		}
	};

	@Override
	public Filter getFilter() {
		return mFilter;
	}
	
}
