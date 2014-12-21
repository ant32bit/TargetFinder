package com.ant32bit.targetfinder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class TargetGameActivity extends Activity implements OnClickListener, OnEditorActionListener, LetterClickedListener {

	static final String STATE_READY = "#Ready";
	static final String STATE_INGAME = "#InGame";
	static final String STATE_PLAYING = "#Playing";
	static final String STATE_GAMEINFO = "#GameInfo";
	static final String STATE_TIMER	= "#Timer";
	static final String STATE_WORDS = "#Words";
	
	TargetView m_target;
	ListView m_listview;
	
	EditText m_edittext;
	Button m_btnNewGame, m_btnSolve;
	ImageButton m_btnSubmit, m_btnBackspace;
	
	TextView m_tvGoals;
	Timer m_timer;
	
	ProgressBar m_progress;
	
	boolean m_bReady;
	boolean m_bInGame;
	boolean m_bPlaying;
	
	String m_astrWords[];
	GameInfo m_gameinfo;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);
		
		initialiseVariables();
		
		if (savedInstanceState != null && savedInstanceState.getBoolean(STATE_READY)) {
			
			m_astrWords = savedInstanceState.getStringArray(STATE_WORDS);
			m_btnNewGame.setEnabled(true);
			m_bReady = true;
			
			if (savedInstanceState.getBoolean(STATE_INGAME)) {
				m_gameinfo = new GameInfo(this, savedInstanceState.getBundle(STATE_GAMEINFO));
				m_gameinfo.updateViews(m_btnSolve, m_btnSubmit, m_btnBackspace, m_edittext, m_target, m_tvGoals, m_listview);
				m_listview.setAdapter(m_gameinfo.getAdapter());
				
				m_timer.setState(savedInstanceState.getBundle(STATE_TIMER));
				m_bInGame = true;
				
				if (savedInstanceState.getBoolean(STATE_PLAYING)) {
					m_bPlaying = true;
				}
				else {
					m_edittext.setEnabled(false);
					m_btnBackspace.setEnabled(false);
					m_btnSubmit.setEnabled(false);
				}
			}
		}
		else {
	        new AsyncPrepareWordsList().execute();
		}
		
	}
	
	public void initialiseVariables() {
		
    	m_target = (TargetView)findViewById(R.id.game_targetview);
    	m_target.setOnLetterClickedListener(this);
    	m_target.setReadOnly(true);
    	
        m_listview = (ListView)findViewById(R.id.game_list_answers);
        
        m_edittext = (EditText)findViewById(R.id.game_input);
        m_btnSubmit = (ImageButton)findViewById(R.id.game_button_submit);
        m_btnBackspace = (ImageButton)findViewById(R.id.game_button_backspace);
        m_btnNewGame = (Button)findViewById(R.id.game_button_new_game);
        m_btnSolve = (Button)findViewById(R.id.game_button_solve);
        
        m_timer = (Timer)findViewById(R.id.game_timer);
        
        m_tvGoals = (TextView)findViewById(R.id.game_text_goals);
        
        m_progress = (ProgressBar)findViewById(R.id.game_progress_bar);
        
        m_btnBackspace.setOnClickListener(this);
        m_btnSubmit.setOnClickListener(this);
        m_btnNewGame.setOnClickListener(this);
        m_btnSolve.setOnClickListener(this);
        
        m_btnNewGame.setEnabled(false);
        m_btnSolve.setEnabled(false);
        m_btnBackspace.setEnabled(false);
        m_btnSubmit.setEnabled(false);
        
        m_edittext.setOnEditorActionListener(this);
        m_edittext.setEnabled(false);
        
        m_bReady = false;
        m_bInGame = false;
        m_bPlaying = false;
    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {

		((InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(m_edittext.getWindowToken(), 0);
		
		outState.putBoolean(STATE_READY, m_bReady);
		if (m_bReady) {
			outState.putStringArray(STATE_WORDS, m_astrWords);
			
			outState.putBoolean(STATE_INGAME, m_bInGame);
			if (m_bInGame) {
				outState.putBoolean(STATE_PLAYING, m_bPlaying);
				outState.putBundle(STATE_GAMEINFO, m_gameinfo.getState());
				outState.putBundle(STATE_TIMER, m_timer.getState());
			}
		}
		
		super.onSaveInstanceState(outState);
	}
	
	public void checkWord(String word) {
		String strError = m_gameinfo.getError(word);
		
		if (strError == null) {
			m_listview.setAdapter(m_gameinfo.getAdapter());
			m_tvGoals.setText(m_gameinfo.getGoalText());
		}
		else {
			Toast.makeText(this, strError, Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void onClick(View view) {
		
		String strWord = null;
			
		switch (view.getId()) {
		case R.id.game_button_new_game:
			Random random = new Random();
			
			strWord = m_astrWords[random.nextInt(m_astrWords.length)];
			m_gameinfo = new GameInfo(this, strWord);
			
			m_progress.setVisibility(View.VISIBLE);
			new AsyncPrepareNewGame().execute(m_gameinfo);
			
			break;
			
		case R.id.game_button_solve:
			
			m_timer.stopTimer();
			m_edittext.setEnabled(false);
			m_btnBackspace.setEnabled(false);
			m_btnSubmit.setEnabled(false);
			m_bPlaying = false;
			
			Intent intent = new Intent("com.ant32bit.targetfinder.RESULTS");
			intent.putExtras(m_gameinfo.getResults());
			
			startActivity(intent);
			
			break;
		
		case R.id.game_button_submit:

			strWord = m_edittext.getText().toString();
			checkWord(strWord);
			m_edittext.setText("");
			break;
		
		case R.id.game_button_backspace:
			
			int iStart = m_edittext.getSelectionStart();
			int iEnd = m_edittext.getSelectionEnd();
			
			if (iEnd != 0) {
				if (iStart == iEnd) {
					m_edittext.getText().delete(iStart - 1, iEnd);
				}
				else {
					m_edittext.getText().delete(iStart, iEnd);
				}
			}
			break;
		}
	}

	@Override
	public void onLetterClicked(TargetView view, int index, char letter) {
		
		int iStart = m_edittext.getSelectionStart();
		int iEnd = m_edittext.getSelectionEnd();
		
		String strLetter = "" + letter;
		
		if (iStart != iEnd) {
			m_edittext.getText().delete(iStart, iEnd);
		}
		
		m_edittext.getText().insert(iStart, strLetter);
		m_edittext.setSelection(iStart + 1);
	}
	
	@Override
	public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
		boolean bConsumed = false;
		
		switch (view.getId()) {
		case R.id.game_input:

			String strWord = m_edittext.getText().toString();
			checkWord(strWord);
			m_edittext.setText("");
			bConsumed = true;
			break;
		}
		
		return bConsumed;
	}
	
	private class AsyncPrepareWordsList extends AsyncTask<Object, Object, Object> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			m_progress.setVisibility(View.VISIBLE);
		}

		@Override
		protected Object doInBackground(Object... params) {
			try {
				byte abIntegerBuffer[] = new byte[6];
				byte abStringBuffer[] = new byte[9];
					
				InputStream is = getAssets().open("words9.lst");
					
				is.read(abIntegerBuffer);				
				int iTotalWords = Integer.valueOf(new String(abIntegerBuffer));
					
				m_astrWords = new String[iTotalWords];
				
				long lBytesForIndices = 156; // 26 * 6
				long lBytesSkipped = 0;
				
				while (lBytesSkipped < lBytesForIndices) {
					lBytesSkipped += is.skip(lBytesForIndices - lBytesSkipped);
					
					if (is.available() == 0) {
						break;
					}
				}
				
				for (int i = 0; i < m_astrWords.length; i++) {
					is.read(abStringBuffer);
					String strWord = new String(abStringBuffer);
					
					m_astrWords[i] = strWord;
				}
				
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);

			m_btnNewGame.setEnabled(true);
			m_bReady = true;
			
			m_progress.setVisibility(View.INVISIBLE);
		}
	}
	
	private class AsyncPrepareNewGame extends AsyncTask<GameInfo, Integer, GameInfo> {

		@Override
		protected GameInfo doInBackground(GameInfo... params) {
			GameInfo gameinfo = params[0];
			
			try {
				for(int iWordLength = 4; iWordLength < 10; iWordLength++) {
					
					byte abIntegerBuffer[] = new byte[6];
					byte abStringBuffer[] = new byte[iWordLength];
					
					String strAssetFile = "words" + iWordLength + ".lst";
					InputStream is = getAssets().open(strAssetFile);
					
					is.read(abIntegerBuffer);
					//int iTotalWords = Integer.valueOf(new String(abIntegerBuffer));
					
					int iWordCounts[] = new int[26];
					for(int i = 0; i < iWordCounts.length; i++) {
						is.read(abIntegerBuffer);
						iWordCounts[i] = Integer.valueOf(new String(abIntegerBuffer));
					}
					
					for (int iCharOffset = 0; iCharOffset < 26; iCharOffset++) {
						char iCurrChar = (char)('a' + iCharOffset);
												
						if (gameinfo.contains(iCurrChar)) {
														
							for (int i = 0; i < iWordCounts[iCharOffset]; i++) {
								is.read(abStringBuffer);
								String strWord = new String(abStringBuffer);
								
								if (!strWord.contains(String.valueOf(gameinfo.getChar(TargetView.MANDATORY_CHAR_INDEX)))) {
									continue;
								}
								
								boolean bSuccess = true;
								boolean bUsed[] = {false, false, false, false, false, false, false, false, false};				
								char charsInStr[] = strWord.toCharArray();
								
								for(int ci = 0; ci < charsInStr.length; ci++) {
									boolean bFound = false;
									
									for (int iCharIndex = 0; iCharIndex < 9; iCharIndex++) {
										if (!bUsed[iCharIndex] && gameinfo.getChar(iCharIndex) == charsInStr[ci]) {
											bFound = true;
											bUsed[iCharIndex] = true;
											break;
										}
									}
									
									if (!bFound) {
										bSuccess = false;
										break;
									}
								}
								
								if (bSuccess) {
									gameinfo.addWordToDictionary(strWord);
								}
							}
						}
						else {
							long lBytesForChar = iWordCounts[iCharOffset] * iWordLength;
							long lBytesSkipped = 0;
							
							while (lBytesSkipped < lBytesForChar) {
								lBytesSkipped += is.skip(lBytesForChar - lBytesSkipped);
								
								if (is.available() == 0) {
									break;
								}
							}
						}
						
						publishProgress(iWordCounts[iCharOffset]);
					}
					
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return gameinfo;
		}

		@Override
		protected void onPostExecute(GameInfo result) {
			super.onPostExecute(result);
			
			m_gameinfo = result;
			
			m_gameinfo.prepareGame();
			m_gameinfo.updateViews(m_btnSolve, m_btnSubmit, m_btnBackspace, m_edittext, m_target, m_tvGoals, m_listview);
			
			m_progress.setVisibility(View.INVISIBLE);
			
			m_timer.startTimer();
			m_bInGame = true;
			m_bPlaying = true;
		}				
	}
	
	private class GameInfo {
		
		private Hashtable<String, Boolean> m_hashDictionary;
		private Hashtable<String, Boolean> m_hashGuessed;
		private char m_acLetters[];
		private int m_iTotalWords;
		private int m_aiTotalByLength[];
		private int m_iGoalPass;
		private int m_iGoalGood;
		private int m_iGoalExcellent;
		
		private int m_iGoalCurrently;
		private int m_iIncorrectGuesses;
		private int m_iLastSavedTime;
		
		private ArrayAdapter<String> m_adapter;
		private Comparator<String> m_comparator;
		
		public GameInfo(Context context, String word) {

	        m_adapter = new ArrayAdapter<String>(context, R.layout.item);
	        m_comparator = new Comparator<String>() {

				@Override
				public int compare(String lhs, String rhs) {
					return lhs.compareTo(rhs);
				}				
			};
			
			m_hashDictionary = new Hashtable<String, Boolean>();
			m_hashGuessed = new Hashtable<String, Boolean>();
			
			m_acLetters = new char[9];
			m_iTotalWords = 0;
			
			m_aiTotalByLength = new int[6];
			for (int i = 0; i < m_aiTotalByLength.length; i++) {
				m_aiTotalByLength[i] = 0;
			}
			
			m_iGoalCurrently = m_iGoalPass = m_iGoalGood = m_iGoalExcellent = 0;
			m_iIncorrectGuesses = 0;
			
			randomiseLetters(word.toLowerCase());
		}
		
		public GameInfo(Context context, Bundle state) {
			m_adapter = new ArrayAdapter<String>(context, R.layout.item);
			m_comparator = new Comparator<String>() {

				@Override
				public int compare(String lhs, String rhs) {
					return lhs.compareTo(rhs);
				}				
			};
			
			m_hashDictionary = new Hashtable<String, Boolean>();
			String asDictionary[] = state.getStringArray("m_hashDictionary");
			for (int i = 0; i < asDictionary.length; i++) {
				m_hashDictionary.put(asDictionary[i], true);
			}
			
			m_hashGuessed = new Hashtable<String, Boolean>();
			String asGuessed[] = state.getStringArray("m_hashGuessed");
			for (int i = 0; i < asGuessed.length; i++) {
				m_hashGuessed.put(asGuessed[i], true);
				m_adapter.add(asGuessed[i]);
			}
			
			m_adapter.sort(m_comparator);
			
			m_acLetters = state.getCharArray("m_acLetters");
			m_aiTotalByLength = state.getIntArray("m_aiTotalByLength");
			
			m_iTotalWords = state.getInt("m_iTotalWords");
			
			m_iGoalPass = state.getInt("m_iGoalPass");
			m_iGoalGood = state.getInt("m_iGoalGood");
			m_iGoalExcellent = state.getInt("m_iGoalExcellent");
			m_iGoalCurrently = state.getInt("m_iGoalCurrently");
			m_iIncorrectGuesses = state.getInt("m_iIncorrectGuesses");
			m_iLastSavedTime = state.getInt("m_iLastSavedTime");
		}
		
		public void randomiseLetters(String letters) {
			boolean abPlaced[] = {false, false, false, false, false, false, false, false, false};
			
			Random r = new Random();
			for (int iLetter = 0; iLetter < m_acLetters.length; iLetter++) {
				int iRandomIndex = r.nextInt(9 - iLetter);
				
				int iIndex = 0;
				while (abPlaced[iIndex]) iIndex++;
				
				for (int i = 0; i < iRandomIndex; i++) {
					iIndex++;					
					while (abPlaced[iIndex]) iIndex++;
				}
				
				abPlaced[iIndex] = true;
				m_acLetters[iIndex] = letters.charAt(iLetter);
			}
		}
		
		public void addWordToDictionary(String word) {
			if (!m_hashDictionary.containsKey(word)) {
				m_hashDictionary.put(word, true);
				m_iTotalWords++;
				m_aiTotalByLength[word.length() - 4]++;
			}			
		}
		
		public void prepareGame() {
			m_iGoalPass = (int)(m_iTotalWords * 0.4f);
			m_iGoalGood = (int)(m_iTotalWords * 0.5f);
			m_iGoalExcellent = (int)(m_iTotalWords * 0.6f);
		}
		
		public char getChar(int index) {
			return m_acLetters[index];
		}
		
		public boolean contains(char letter) {			
			for (int i = 0; i < m_acLetters.length; i++) {
				if (m_acLetters[i] == letter) {
					return true;
				}
			}
			
			return false;
		}
		
		public ArrayAdapter<String> getAdapter() {
			return m_adapter;
		}
		
		public String getError(String word) {

			m_iLastSavedTime = m_timer.getCurrTime();
			word = word.toLowerCase().trim();
			
			if (word.length() < 4) {
				m_iIncorrectGuesses++;
				return "words must be 4 or more letters long";
			}
			
			if (!word.contains(String.valueOf(m_acLetters[TargetView.MANDATORY_CHAR_INDEX]))) {
				m_iIncorrectGuesses++;
				return word + " does not use the letter '" + m_acLetters[TargetView.MANDATORY_CHAR_INDEX] + "'";
			}
			
			boolean bUsed[] = {false, false, false, false, false, false, false, false, false};				
			char charsInStr[] = word.toCharArray();
			
			char cIllegal = 0;
			
			for(int ci = 0; ci < charsInStr.length; ci++) {
				boolean bFound = false;
				
				for (int iCharIndex = 0; iCharIndex < 9; iCharIndex++) {
					if (!bUsed[iCharIndex] && m_acLetters[iCharIndex] == charsInStr[ci]) {
						bFound = true;
						bUsed[iCharIndex] = true;
						break;
					}
				}
				
				if (!bFound) {
					cIllegal = charsInStr[ci];
					break;
				}
			}
			
			if (cIllegal != 0) {
				m_iIncorrectGuesses++;
				return "there is no '" + cIllegal + "' in the target";
			}			
			
			if (!m_hashDictionary.containsKey(word)) {
				m_iIncorrectGuesses++;
				return word + " is not a word";
			}
			
			if (m_hashGuessed.containsKey(word)) {
				return word + " has already been recorded";
			}
			
			m_hashGuessed.put(word, true);
			m_adapter.add(word);			
			m_adapter.sort(m_comparator);
			
			m_iGoalCurrently++;
			
			return null;
		}
		
		public String getGoalText() {
			return String.format("Currently: %d  Pass: %d  Good: %d  Excellent: %d", m_iGoalCurrently, m_iGoalPass, m_iGoalGood, m_iGoalExcellent);
		}
		
		public void updateViews(Button button, ImageButton imagebutton1, ImageButton imagebutton2, EditText edittext, TargetView targetview, TextView textview, ListView listview) {
			String letters = new String(m_acLetters);
			
			button.setEnabled(true);
			imagebutton1.setEnabled(true);
			imagebutton2.setEnabled(true);
			edittext.setEnabled(true);
			targetview.setCellsState(letters.toUpperCase());
			textview.setText(getGoalText());
			listview.setAdapter(m_adapter);
		}
		
		public Bundle getState() {
			Bundle bundle = new Bundle();
			
			Enumeration<String> eDictionary = m_hashDictionary.keys();
			String asDictionary[] = new String[m_hashDictionary.size()];
			
			for (int i = 0; eDictionary.hasMoreElements(); i++) {
				asDictionary[i] = eDictionary.nextElement();
			}
			
			Enumeration<String> eGuessed = m_hashGuessed.keys();
			String asGuessed[] = new String[m_hashGuessed.size()];
			
			for (int i = 0; eGuessed.hasMoreElements(); i++) {
				asGuessed[i] = eGuessed.nextElement();
			}

			bundle.putStringArray("m_hashDictionary", asDictionary);
			bundle.putStringArray("m_hashGuessed", asGuessed);
			bundle.putCharArray("m_acLetters", m_acLetters);
			bundle.putIntArray("m_aiTotalByLength", m_aiTotalByLength);
			
			bundle.putInt("m_iTotalWords", m_iTotalWords);

			bundle.putInt("m_iGoalPass", m_iGoalPass);
			bundle.putInt("m_iGoalGood", m_iGoalGood);
			bundle.putInt("m_iGoalExcellent", m_iGoalExcellent);
			
			bundle.putInt("m_iGoalCurrently", m_iGoalCurrently);
			bundle.putInt("m_iIncorrectGuesses", m_iIncorrectGuesses);
			bundle.putInt("m_iLastSavedTime", m_iLastSavedTime);
						
			return bundle;
		}
		
		public Bundle getResults() {
			Bundle bundle = new Bundle();
			
			Enumeration<String> eDictionary = m_hashDictionary.keys();
			String asDictionary[] = new String[m_hashDictionary.size()];
			
			for (int i = 0; eDictionary.hasMoreElements(); i++) {
				asDictionary[i] = eDictionary.nextElement();
			}
			
			Enumeration<String> eGuessed = m_hashGuessed.keys();
			String asGuessed[] = new String[m_hashGuessed.size()];
			
			for (int i = 0; eGuessed.hasMoreElements(); i++) {
				asGuessed[i] = eGuessed.nextElement();
			}
			
			bundle.putInt(GameResultsActivity.EXTRA_TOTAL_INCORRECT, m_iIncorrectGuesses);
			bundle.putInt(GameResultsActivity.EXTRA_TOTAL_TIME, m_iLastSavedTime);
			
			bundle.putStringArray(GameResultsActivity.EXTRA_WORDS_ALL, asDictionary);
			bundle.putStringArray(GameResultsActivity.EXTRA_WORDS_FOUND, asGuessed);
			
			return bundle;
		}
	}
}
