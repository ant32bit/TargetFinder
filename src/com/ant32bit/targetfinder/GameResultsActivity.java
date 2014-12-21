package com.ant32bit.targetfinder;

import java.util.Hashtable;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class GameResultsActivity extends Activity implements OnItemClickListener {

	public final static String EXTRA_TOTAL_INCORRECT = "#TOTALINCORRECT";
	public final static String EXTRA_TOTAL_TIME = "#TOTALTIME";
	public final static String EXTRA_WORDS_ALL = "#WORDSALL";
	public final static String EXTRA_WORDS_FOUND = "#WORDSFOUND";
	public final static String BUNDLE_INTENT_BUNDLE = "#Bundle";
	
	private ResultsAdapter m_adapter;
	private Bundle m_bundle;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_results);
				
		if (savedInstanceState != null) {
			m_bundle = savedInstanceState.getBundle(BUNDLE_INTENT_BUNDLE);
		}
		else {
			Intent intent = getIntent();
			m_bundle = intent.getExtras();
			
			if (m_bundle == null) {
				finish();
			}
		}
		
		int iTotalIncorrect = m_bundle.getInt(EXTRA_TOTAL_INCORRECT);
		int iTotalTime = m_bundle.getInt(EXTRA_TOTAL_TIME);
		String astrWords[] = m_bundle.getStringArray(EXTRA_WORDS_ALL);
			
		Hashtable<String, Boolean> hashGuesses = new Hashtable<String, Boolean>();
			
		String astrfoundWords[] = m_bundle.getStringArray(EXTRA_WORDS_FOUND);
		for (int i = 0; i < astrfoundWords.length; i++) {
			hashGuesses.put(astrfoundWords[i], true);
		}
		
		m_adapter = new ResultsAdapter(this);
		
		for (String word : astrWords) {
			m_adapter.addItem(word, hashGuesses.containsKey(word));
		}
		
		m_adapter.addStatistics(iTotalIncorrect, iTotalTime);
		m_adapter.lockAdapter();
		
		ListView m_wordsList;
		m_wordsList = (ListView)findViewById(R.id.results_words);
		m_wordsList.setOnItemClickListener(this);
		m_wordsList.setAdapter(m_adapter);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		
		outState.putBundle(BUNDLE_INTENT_BUNDLE, m_bundle);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent("com.ant32bit.targetfinder.DEFINITION");
				
		Bundle bArgs = new Bundle();
		bArgs.putString(DefinitionActivity.DEFINITION_KEY, m_adapter.getItem(position));
		
		intent.putExtras(bArgs);
		
		startActivity(intent);
	}	
}
