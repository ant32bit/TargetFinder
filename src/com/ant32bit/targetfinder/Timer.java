package com.ant32bit.targetfinder;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.TextView;

public class Timer extends TextView implements Runnable {

	private long m_lStartTime;	
	private boolean m_bRunning;	
	private String m_strTime;
	private Thread m_thread;
	private Handler m_handle;
	private Runnable m_post;
	private Timer m_timer;
	
	private long m_lTotalTime;
		
	public Timer(Context context) {
		super(context);
		initialise();
	}
	
	public Timer(Context context, AttributeSet attr) {
		super(context, attr);
		initialise();
	}
	
	public Bundle getState() {
		Bundle bundle = new Bundle();
		
		bundle.putLong("m_lStartTime", m_lStartTime);
		bundle.putLong("m_lTotalTime", m_lTotalTime);
		bundle.putString("m_strTime", m_strTime);
		bundle.putBoolean("m_bRunning", m_bRunning);
		
		return bundle;
	}
	
	public void setState(Bundle state) {
		
		m_lStartTime = state.getLong("m_lStartTime");
		m_lTotalTime = state.getLong("m_lTotalTime");
		m_bRunning = state.getBoolean("m_bRunning");
		m_strTime = state.getString("m_strTime");
		m_timer.setText(m_strTime);
		invalidate();
		
		if (m_bRunning) {
			m_thread = new Thread(this);
			m_thread.start();
		}
	}
	
	private void initialise() {
		m_timer = this;
		
		m_post = new Runnable() {
			
			@Override
			public void run() {
				m_timer.setText(m_strTime);
			}
		};
		
		m_handle = new Handler();
		stopTimer();
	}
	
	public void startTimer() {
		m_lStartTime = System.currentTimeMillis();
		m_bRunning = true;
		
		m_thread = new Thread(this);
		m_thread.start();
	}
	
	public void stopTimer() {
		m_bRunning = false;
		m_lStartTime = 0;
		m_strTime = "0:00:00";
	}
	
	public int getCurrTime() {
		return (int) (m_lTotalTime / 1000);
	}
	
	@Override
	public void run() {
		
		while (m_bRunning) {
			try {
				long lCurrTime = System.currentTimeMillis();
				m_lTotalTime = (lCurrTime - m_lStartTime);
				long lTotalTime = (int)(m_lTotalTime) / 1000;
								
				int iHours   = (int) (lTotalTime / 3600);
				lTotalTime -= iHours * 3600;
				
				int iMinutes = (int)(lTotalTime / 60);
				lTotalTime -= iMinutes * 60;
				
				int iSeconds = (int)(lTotalTime);
				
				m_strTime = String.format("%d:%02d:%02d", iHours, iMinutes, iSeconds);
				m_handle.post(m_post);
				
				Thread.sleep(500);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
	}
}
