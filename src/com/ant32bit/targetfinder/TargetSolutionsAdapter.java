package com.ant32bit.targetfinder;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

public class TargetSolutionsAdapter extends BaseAdapter {

	private ArrayList<TargetSolutionItem> m_TSIArray;
	private ArrayAdapter<String> m_aHeaders, m_aItems;
	private Context m_context;
	
	public TargetSolutionsAdapter(Context context) {		
		m_context = context;
		
		m_TSIArray = new ArrayList<TargetSolutionItem>();	
		m_aHeaders = new ArrayAdapter<String>(m_context, R.layout.heading);
		m_aItems = new ArrayAdapter<String>(m_context, R.layout.item);	
	}
	
	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public int getItemViewType(int position) {
		return m_TSIArray.get(position).getType();
	}

	@Override
	public int getViewTypeCount() {
		return TargetSolutionItem.TYPE_COUNT;
	}

	@Override
	public boolean isEmpty() {
		return m_TSIArray.isEmpty();
	}

	@Override
	public boolean isEnabled(int position) {
		if (m_TSIArray.get(position).getType() == TargetSolutionItem.TYPE_ITEM) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public void addHeader(String text) {
		int position = m_aHeaders.getCount();
		m_aHeaders.add(text);
		m_TSIArray.add(new TargetSolutionItem(TargetSolutionItem.TYPE_HEADER, position));
	}
	
	public void addItem(String text) {
		int position = m_aItems.getCount();
		m_aItems.add(text);
		m_TSIArray.add(new TargetSolutionItem(TargetSolutionItem.TYPE_ITEM, position));
	}
	
	public void clear() {
		m_TSIArray.clear();
		m_aItems.clear();
		m_aHeaders.clear();
	}
	
	public String asText() {
		String str = "";
		
		for (int i = 0; i < m_aItems.getCount(); i++) {
			str += " " + m_aItems.getItem(i);
		}
		
		return str;
	}
	
	@Override
	public int getCount() {
		return m_TSIArray.size();
	}

	@Override
	public String getItem(int position) {
		TargetSolutionItem tsi = m_TSIArray.get(position);
		
		if (tsi.getType() == TargetSolutionItem.TYPE_ITEM) {
			return m_aItems.getItem(tsi.getIndex());
		}
		
		if (tsi.getType() == TargetSolutionItem.TYPE_HEADER) {
			return m_aHeaders.getItem(tsi.getIndex());
		}
		
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		TargetSolutionItem tsiCurrItem = m_TSIArray.get(position);
		
		View v = convertView;
		
		if (tsiCurrItem.getType() == TargetSolutionItem.TYPE_ITEM) {
			v = m_aItems.getView(tsiCurrItem.getIndex(), convertView, parent);
		}
		else if (tsiCurrItem.getType() == TargetSolutionItem.TYPE_HEADER) {
			v = m_aHeaders.getView(tsiCurrItem.getIndex(), convertView, parent);
		}
			
		return v;
	}
	
	private class TargetSolutionItem {
		public final static int TYPE_ITEM = 0;
		public final static int TYPE_HEADER = 1;
		public final static int TYPE_COUNT = 2;
		
		private int m_iType;
		private int m_iIndex;
		
		public TargetSolutionItem(int type, int index) {
			m_iType = type;
			m_iIndex = index;
		}
		
		public int getType() {
			return m_iType;
		}
		
		public int getIndex() {
			return m_iIndex;
		}
	}
}
