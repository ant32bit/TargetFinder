package com.ant32bit.targetfinder;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.BufferType;

public class TargetFinderActivity extends Activity implements ReadyToSolveListener, OnClickListener, OnItemClickListener {

	public final static String TARGET_VIEW_STATE = "#TargetView";
	public final static String TEXT_VIEW_STATE = "#TextView";
	
	TargetView m_target;
	ListView m_listview;
	TargetSolutionsAdapter m_adapter;
	ImageButton m_btnClear, m_btnGo;
	ProgressBar m_progress;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        initialiseVariables();
        
        if (savedInstanceState != null) {
        	String strTargetViewState = savedInstanceState.getString(TARGET_VIEW_STATE);
    		String strTextViewState = savedInstanceState.getString(TEXT_VIEW_STATE);
    		
    		if (strTargetViewState != null) {
    			m_target.setCellsState(strTargetViewState);
    			if (m_target.isReadyToSolve()) {
    				m_btnGo.setVisibility(View.VISIBLE);
    			}
    		}
    		
    		if (strTextViewState != null) {
    			
    			int iCurrSectionStringSize = 0;
    			for (String str : strTextViewState.split(" ")) {
    				int iCurrStringSize = str.length();
    				
    				if (iCurrStringSize == 0) {
    					continue;
    				}
    				
    				if (iCurrSectionStringSize != iCurrStringSize) {
    					m_adapter.addHeader(iCurrStringSize + " LETTER WORDS");
    					iCurrSectionStringSize = iCurrStringSize;
    				}
    				
    				m_adapter.addItem(str);
    			}
    			
    			if (strTextViewState.length() > 0) {
    				TextView tvInstructions = (TextView)findViewById(R.id.instructions);
    				if (tvInstructions != null) tvInstructions.setText(R.string.went_instr, BufferType.NORMAL);
    			}
    			
    			m_listview.setAdapter(m_adapter);
    		}
        }
    }
    
    public void initialiseVariables() {
    	m_progress = (ProgressBar)findViewById(R.id.main_progress);
    	
    	m_target = (TargetView)findViewById(R.id.target);
        m_listview = (ListView)findViewById(R.id.solutions);
        
        m_btnClear = (ImageButton)findViewById(R.id.btnClear);
        m_btnGo = (ImageButton)findViewById(R.id.btnGo);
        
        m_btnClear.setOnClickListener(this);
        m_btnGo.setOnClickListener(this);
        
        m_adapter = new TargetSolutionsAdapter(this);
        m_listview.setAdapter(m_adapter);
        m_listview.setOnItemClickListener(this);
        
        m_target.setOnReadyToSolveListener(this);
    }
    
	@Override
	protected void onSaveInstanceState(Bundle outState) {

		((InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(m_target.getWindowToken(), 0);
		
		String strTargetViewState = m_target.getCellsState();
		String strTextViewState = m_adapter.asText();

		outState.putString(TARGET_VIEW_STATE, strTargetViewState);
		outState.putString(TEXT_VIEW_STATE, strTextViewState);
		
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onReadyToSolve(TargetView view) {
		
		m_btnGo.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btnClear:
			m_target.clear();
			m_adapter.clear();
			m_listview.setAdapter(m_adapter);
			m_btnGo.setVisibility(View.INVISIBLE);
			TextView tvInstructions = (TextView)findViewById(R.id.instructions);
			if (tvInstructions != null) tvInstructions.setText(R.string.cleared_instr, BufferType.NORMAL);
			break;
			
		case R.id.btnGo:
			if (m_target.isReadyToSolve()) {
				char acLetters[] = new char[9];
				for (int i = 0; i < acLetters.length; i++) {
					acLetters[i] = m_target.getChar(i);
				}
				
				new AsyncGetSolutions().execute(new AsyncGetSolutionsArguments(acLetters, m_adapter));
			}
			
			break;
		}
	}
	

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent("com.ant32bit.targetfinder.DEFINITION");
				
		Bundle bArgs = new Bundle();
		bArgs.putString(DefinitionActivity.DEFINITION_KEY, m_adapter.getItem(position));
		
		intent.putExtras(bArgs);
		
		startActivity(intent);
	}
	
	public class AsyncGetSolutions extends AsyncTask<AsyncGetSolutionsArguments, Integer, AsyncGetSolutionResults> {

		AsyncGetSolutionsArguments m_arguments;
				
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			m_progress.setVisibility(View.VISIBLE);
		}

		@Override
		protected AsyncGetSolutionResults doInBackground(AsyncGetSolutionsArguments... params) {
			m_arguments = params[0];
			
			String strSolutions[] = {"", "", "", "", "", ""};
			int iFound = 0;
			
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
												
						if (m_arguments.contains(iCurrChar)) {
														
							for (int i = 0; i < iWordCounts[iCharOffset]; i++) {
								is.read(abStringBuffer);
								String strWord = new String(abStringBuffer);
								
								if (!strWord.contains(String.valueOf(m_arguments.getChar(TargetView.MANDATORY_CHAR_INDEX)))) {
									continue;
								}
								
								boolean bSuccess = true;
								boolean bUsed[] = {false, false, false, false, false, false, false, false, false};				
								char charsInStr[] = strWord.toCharArray();
								
								for(int ci = 0; ci < charsInStr.length; ci++) {
									boolean bFound = false;
									
									for (int iCharIndex = 0; iCharIndex < 9; iCharIndex++) {
										if (!bUsed[iCharIndex] && m_arguments.getChar(iCharIndex) == charsInStr[ci]) {
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
									strSolutions[iWordLength - 4] += strWord + " ";
									iFound++;
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
					}
					
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			String strResult = strSolutions[5] + strSolutions[4] + strSolutions[3] + strSolutions[2] + strSolutions[1] + strSolutions[0];
			return new AsyncGetSolutionResults(strResult, iFound);
		}
		
		@Override
		protected void onPostExecute(AsyncGetSolutionResults result) {
			super.onPostExecute(result);
			
			m_progress.setVisibility(View.INVISIBLE);
			
			m_adapter.clear();
			int iCurrSectionStringSize = 0;
			for (String str : result.getSolutions().split(" ")) {
				int iCurrStringSize = str.length();
				
				if (iCurrStringSize == 0) {
					continue;
				}
				
				if (iCurrSectionStringSize != iCurrStringSize) {
					m_adapter.addHeader(iCurrStringSize + " LETTER WORDS");
					iCurrSectionStringSize = iCurrStringSize;
				}
				
				m_adapter.addItem(str);
			}
			
			TextView tvInstructions = (TextView)findViewById(R.id.instructions);
			if (tvInstructions != null) tvInstructions.setText(R.string.went_instr, BufferType.NORMAL);
			
			ListView lvResultsDisplay = (ListView)findViewById(R.id.solutions);
			if (lvResultsDisplay != null) lvResultsDisplay.setAdapter(m_adapter);
			
			Toast.makeText(getApplicationContext(), result.getWordsFound() + " words found.", Toast.LENGTH_SHORT).show();
		}		
	}
	
	public class AsyncGetSolutionsArguments {
		private char m_acLetters[];
		private TargetSolutionsAdapter m_iOutput;
		
		public AsyncGetSolutionsArguments(char[] letters, TargetSolutionsAdapter output) {
			m_acLetters = new char[letters.length];
			for (int i = 0; i < letters.length; i++) {
				m_acLetters[i] = Character.toLowerCase(letters[i]);
			}
			
			
			m_iOutput = output;
		}
		
		public char getChar(int position) {
			return m_acLetters[position];
		}
		
		public TargetSolutionsAdapter getAdapter() {
			return m_iOutput;
		}
		
		public boolean contains(char letter) {			
			for (int i = 0; i < m_acLetters.length; i++) {
				if (m_acLetters[i] == letter) {
					return true;
				}
			}
			
			return false;
		}
	}
	
	public class AsyncGetSolutionResults {
		private String m_strSolutions;
		private int m_iWordsFound;
		
		public AsyncGetSolutionResults(String solutions, int wordsFound) {
			m_strSolutions = solutions;
			m_iWordsFound = wordsFound;
		}
		
		public String getSolutions() {
			return m_strSolutions;
		}
		
		public int getWordsFound() {
			return m_iWordsFound;
		}		
	}
}