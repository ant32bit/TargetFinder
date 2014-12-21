package com.ant32bit.targetfinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ResultsAdapter extends BaseAdapter{

	private final static int TYPE_HEADING = 0;
	private final static int TYPE_ITEM = 1;
	private final static int TYPE_FOUND_ITEM = 2;
	private final static int TYPE_SUMMARY = 3;
	private final static int TYPE_COUNT = 4;
	
	private String m_strHeadings[] = {"4 LETTER WORDS", "5 LETTER WORDS", "6 LETTER WORDS", "7 LETTER WORDS", "8 LETTER WORDS", "9 LETTER WORDS"};
	private ArrayList<ArrayList<String>> m_strFoundWords;
	private ArrayList<ArrayList<String>> m_strMissedWords;
	
	private ArrayList<ResultItem> m_items;
	private boolean m_bLocked;
	
	private Context m_context;
	private Comparator<String> m_comparator;
	
	private int m_iWordsFound;
	private int m_iTotalWords;
	private int m_iTotalGuesses;
	private int m_iIncorrectCount;
	private int m_iTotalGameTime;
	
	private float m_fCompleteness;
	private float m_fAccuracy;
	private float m_fSecsPerWord;
	private float m_fSecsPerGuess;
	
	private String m_strCompleteness;
	private String m_strAccuracy;
	private String m_strWordSpeed;
	private String m_strGuessSpeed;
	
	public ResultsAdapter(Context context) {
		m_context = context;
		m_bLocked = false;
		
		m_strFoundWords = new ArrayList<ArrayList<String>>();
		m_strMissedWords = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < m_strHeadings.length; i++) {
			m_strFoundWords.add(new ArrayList<String>());
			m_strMissedWords.add(new ArrayList<String>());
		}
		
		m_items = new ArrayList<ResultItem>();
		
		m_comparator = new Comparator<String>() {

			@Override
			public int compare(String lhs, String rhs) {
				return lhs.compareTo(rhs);
			}				
		};
		
		m_iWordsFound = m_iTotalWords = m_iTotalGuesses = m_iIncorrectCount = m_iTotalGameTime = 0;
		m_fCompleteness = m_fAccuracy = m_fSecsPerWord = m_fSecsPerGuess = 0.0f;
		
		m_strCompleteness = m_context.getResources().getString(R.string.results_words_found_default);
		m_strAccuracy = m_context.getResources().getString(R.string.results_words_accuracy_default);
		m_strWordSpeed = m_context.getResources().getString(R.string.results_average_times_default_correct);
		m_strGuessSpeed = m_context.getResources().getString(R.string.results_average_times_default_all);
	}
	
	public void addItem(String word, boolean found) {
		if (m_bLocked) return;
		
		int iLengthID = word.length() - 4;
		if (found) {
			m_strFoundWords.get(iLengthID).add(word);
		}
		else {
			m_strMissedWords.get(iLengthID).add(word);
		}
	}
	
	public void addStatistics (int incorrectGuesses, int totalPlayTime) {
		m_iIncorrectCount = incorrectGuesses;
		m_iTotalGameTime = totalPlayTime;
		
		if (m_bLocked) {
			calculateStatistics();
		}
	}
	
	public void lockAdapter() {
		if (m_bLocked) return;
		
		m_items.add(new ResultItem("", TYPE_SUMMARY));
		
		for(int iLengthID = 5; iLengthID >= 0; iLengthID--) {
			ArrayList<String> foundWords = m_strFoundWords.get(iLengthID);
			ArrayList<String> missedWords = m_strMissedWords.get(iLengthID);
			
			if (foundWords.size() + missedWords.size() > 0) {
				m_items.add(new ResultItem(m_strHeadings[iLengthID], TYPE_HEADING));
				
				if (foundWords.size() > 0) {
					m_iWordsFound += foundWords.size();
					m_iTotalWords += foundWords.size();
					
					String tempStringArray[] = new String[foundWords.size()];
					for (int i = 0; i < tempStringArray.length; i++) {
						tempStringArray[i] = foundWords.get(i);
					}
					
					Arrays.sort(tempStringArray, m_comparator);
					for (int i = 0; i < tempStringArray.length; i++) {
						m_items.add(new ResultItem(tempStringArray[i], TYPE_FOUND_ITEM));
					}
				}
				
				if (missedWords.size() > 0) {
					m_iTotalWords += missedWords.size();
					
					String tempStringArray[] = new String[missedWords.size()];
					for (int i = 0; i < tempStringArray.length; i++) {
						tempStringArray[i] = missedWords.get(i);
					}
					
					Arrays.sort(tempStringArray, m_comparator);
					for (int i = 0; i < tempStringArray.length; i++) {
						m_items.add(new ResultItem(tempStringArray[i], TYPE_ITEM));
					}
				}
			}
		}
		
		calculateStatistics();
		m_bLocked = true;
	}
	
	private void calculateStatistics() {
		
		m_iTotalGuesses = m_iWordsFound + m_iIncorrectCount;
		
		m_fCompleteness = (m_iTotalWords > 0) ? (float)m_iWordsFound / (float)m_iTotalWords : 1.0f;
		m_fCompleteness *= 100;
		
		m_fAccuracy = (m_iTotalGuesses > 0) ? (float)m_iWordsFound / (float)m_iTotalGuesses : 1.0f;
		m_fAccuracy *= 100;
		
		m_fSecsPerWord = (m_iWordsFound > 0) ? (float)m_iTotalGameTime / (float)m_iWordsFound : 0.0f;
		m_fSecsPerGuess = (m_iTotalGuesses > 0) ? (float)m_iTotalGameTime / (float)m_iTotalGuesses : 0.0f;
		
		m_strCompleteness = String.format("%d of %d words found (%.2f%%)", m_iWordsFound, m_iTotalWords, m_fCompleteness);
		m_strAccuracy = String.format("%d incorrect guesses (%.2f%% accuracy)", m_iIncorrectCount, m_fAccuracy);
		m_strWordSpeed = String.format("%.2f seconds per word", m_fSecsPerWord);
		m_strGuessSpeed = String.format("%.2f seconds per guess (includes incorrect guesses)", m_fSecsPerGuess);
	}
	
	@Override
	public int getCount() {
		return m_bLocked ? m_items.size() : 0;
	}

	@Override
	public String getItem(int position) {
		return m_bLocked ? m_items.get(position).text : null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public int getItemViewType(int position) {
		return m_items.get(position).type;
	}

	@Override
	public int getViewTypeCount() {
		return TYPE_COUNT;
	}

	@Override
	public boolean isEnabled(int position) {
		return (m_items.get(position).type != TYPE_HEADING);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if (!m_bLocked) {
			return null;
		}
		
		ResultItem item = m_items.get(position);
		
		View view = convertView;
		
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) m_context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			
			switch (item.type) {
			case TYPE_HEADING:
				view = inflater.inflate(R.layout.heading, parent, false);
				break;
				
			case TYPE_ITEM:
				view = inflater.inflate(R.layout.item, parent, false);
				break;
				
			case TYPE_FOUND_ITEM:
				view = inflater.inflate(R.layout.found_item, parent, false);
				break;
			
			case TYPE_SUMMARY:
				view = inflater.inflate(R.layout.summary, parent, false);
				view.setTag(new SummaryViewHolder(view));
				break;
			}
		}
		
		if (item.type == TYPE_SUMMARY) {
			SummaryViewHolder holder = (SummaryViewHolder)view.getTag();
			
			holder.textview_completeness.setText(m_strCompleteness);
			holder.textview_accuracy.setText(m_strAccuracy);
			holder.textview_wordspeed.setText(m_strWordSpeed);
			holder.textview_guessspeed.setText(m_strGuessSpeed);
		}
		else {
			TextView textview = (TextView)view;
			textview.setText(item.text);	
		}	
		
		return view;
	}
	
	private class ResultItem {
		String text;
		int type;
		
		public ResultItem(String text, int type) {
			this.text = text;
			this.type = type;
		}	
	}
	
	private class SummaryViewHolder {
		TextView textview_completeness;
		TextView textview_accuracy;
		TextView textview_wordspeed;
		TextView textview_guessspeed;
		
		public SummaryViewHolder(View summaryView) {
			textview_completeness = (TextView)summaryView.findViewById(R.id.results_text_words_found);
			textview_accuracy = (TextView)summaryView.findViewById(R.id.results_text_words_accuracy);
			textview_wordspeed = (TextView)summaryView.findViewById(R.id.results_text_word_speed);
			textview_guessspeed = (TextView)summaryView.findViewById(R.id.results_text_guess_speed);
		}
	}
}
