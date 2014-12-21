package com.ant32bit.targetfinder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class DefinitionActivity extends Activity {

	public final static String DEFINITION_KEY = "#DefinitionKey";
	private final static String DICTIONARY_URL = "http://www.thefreedictionary.com/";
	
	WebView m_webview;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.definition);
		
		m_webview = (WebView)findViewById(R.id.dictonary);
		m_webview.getSettings().setJavaScriptEnabled(true);
		m_webview.getSettings().setLoadWithOverviewMode(true);
		m_webview.getSettings().setUseWideViewPort(true);
		m_webview.setWebViewClient(new InternalWebViewClient());
		
		if (savedInstanceState != null) {
			m_webview.loadUrl(savedInstanceState.getString(DEFINITION_KEY));
		}
		else {
			Intent intent = getIntent();
			Bundle bArgs = intent.getExtras();
			String strWord = bArgs.getString(DEFINITION_KEY);
			
			m_webview.loadUrl(DICTIONARY_URL + strWord);
		}		
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(DEFINITION_KEY, m_webview.getUrl());		
		super.onSaveInstanceState(outState);
	}
	
	public class InternalWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}
}
