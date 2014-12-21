package com.ant32bit.targetfinder;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class TargetView extends View {
	
	public static final int MANDATORY_CHAR_INDEX = 4;
	private static final char[] ACCEPTABLE_CHARS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
	
	private ArrayList<ReadyToSolveListener> m_alRTSListeners;
	private ArrayList<LetterClickedListener> m_alLCListeners;
	
	private char m_acAllChars[];
	
	private boolean m_bGotDimensions;
	private boolean m_bAllCharsFilled;
	private boolean m_bReadOnly;
	
	private float m_fCellPadding = 20.0f;
		
	private float m_fWidth;
	private float m_fHeight;
	
	private float m_fCellWidth;
	private float m_fCellHeight;	
	private float m_afLinePoints[];
	
	private Context m_context;
	private int m_cBackgroundColor;
	
	private Paint m_paintComplementary;
	private Paint m_paintSupplementary;
	private Paint m_paintHighlighted;
	
	private int m_aiAttributes[];
	
	private int m_iHighlightedIndex;
	private boolean m_bHighlighted;
	
	public TargetView(Context context, AttributeSet attr) {
		super(context, attr);
		m_context = context;		
		initialiseVariables();
		setPaints(true);
		invalidate();
	}
	
	public TargetView(Context context) {
		super(context);
		
		m_context = context;		
		initialiseVariables();
		setPaints(true);
		invalidate();
	}
	
	private void initialiseVariables() {
		
		this.setFocusable(true);
		this.setFocusableInTouchMode(true);
		
		m_aiAttributes = new int[] { android.R.attr.colorBackground };		
		
		m_paintComplementary = new Paint();
		m_paintComplementary.setTypeface(Typeface.SANS_SERIF);
		m_paintComplementary.setStyle(Style.FILL_AND_STROKE);
		m_paintComplementary.setTextAlign(Align.CENTER);
		m_paintComplementary.setStrokeWidth(2.0f);
		
		m_paintSupplementary = new Paint();
		m_paintSupplementary.setTypeface(Typeface.SANS_SERIF);
		m_paintSupplementary.setStyle(Style.FILL_AND_STROKE);
		m_paintSupplementary.setTextAlign(Align.CENTER);
		m_paintSupplementary.setStrokeWidth(2.0f);
		
		TypedValue tv = new TypedValue();
		int cHighlightColor = Color.rgb(153, 204, 0);
		if (getContext().getTheme().resolveAttribute(0x0101038D, tv, true)) {
			cHighlightColor = m_context.getResources().getColor(tv.resourceId);
		}
		
		m_paintHighlighted = new Paint();
		m_paintHighlighted.setColor(cHighlightColor);
		m_paintHighlighted.setStyle(Style.STROKE);
		m_paintHighlighted.setStrokeWidth(4.0f);
				
		m_acAllChars = new char[9];		
		for (int i=0; i < m_acAllChars.length; i++) {
			m_acAllChars[i] = 0;
		}
		
		m_fWidth = m_fHeight = m_fCellWidth = m_fCellHeight = 0.0f;
		
		m_bGotDimensions = false;
		m_bAllCharsFilled = false;
		m_bHighlighted = false;
		m_bReadOnly = false;
		
		m_alRTSListeners = new ArrayList<ReadyToSolveListener>();
		m_alLCListeners = new ArrayList<LetterClickedListener>();
	}

	public void setReadOnly(boolean readOnly) {
		m_bReadOnly = readOnly;
	}
	
	private void setPaints(boolean force) {
		TypedArray array = m_context.getTheme().obtainStyledAttributes(m_aiAttributes); 
		int currBackgroundColor = array.getColor(0, Color.BLACK);
		array.recycle();
		
		if (force || currBackgroundColor != m_cBackgroundColor) {
			m_cBackgroundColor = currBackgroundColor;
			
			int cShade = (Color.red(m_cBackgroundColor) + Color.green(m_cBackgroundColor) + Color.blue(m_cBackgroundColor)) / 3;
			if (cShade > 128) {
				m_paintComplementary.setColor(Color.BLACK);
				m_paintSupplementary.setColor(Color.WHITE);
			}
			else {
				m_paintComplementary.setColor(Color.WHITE);
				m_paintSupplementary.setColor(Color.BLACK);
			}
		}
	}
	
	private void getCellDimensions(float width, float height) {
		if (!m_bGotDimensions) {
			float fTotalAvailableSpace;
			
			if (width == 0.0f && height == 0.0f) {
				fTotalAvailableSpace = 12.0f;
			}
			else if (width == 0.0f) {
				fTotalAvailableSpace = height - 8;
			}
			else if (height == 0.0f) {
				fTotalAvailableSpace = width - 8;
			}
			else {
				fTotalAvailableSpace = Math.min(width, height) - 8;
			}
				
			float fCellHeight = fTotalAvailableSpace / 3;
			
			m_fCellWidth = fCellHeight;
			m_fCellHeight = fCellHeight;
			m_fCellPadding = fCellHeight / 4;
			
			m_fWidth = 7 + m_fCellWidth * 3;
			m_fHeight = 7 + m_fCellHeight * 3;
						
			this.setMinimumWidth((int)m_fWidth);
			this.setMinimumHeight((int)m_fHeight);
			
			m_afLinePoints = new float[] { 
				0, 1 + m_fCellHeight * 0, m_fWidth, 1 + m_fCellHeight * 0,
				0, 3 + m_fCellHeight * 1, m_fWidth, 3 + m_fCellHeight * 1,
				0, 5 + m_fCellHeight * 2, m_fWidth, 5 + m_fCellHeight * 2,
				0, 7 + m_fCellHeight * 3, m_fWidth, 7 + m_fCellHeight * 3,
			
				1 + m_fCellWidth * 0, 0, 1 + m_fCellWidth * 0, m_fHeight,
				3 + m_fCellWidth * 1, 0, 3 + m_fCellWidth * 1, m_fHeight, 
				5 + m_fCellWidth * 2, 0, 5 + m_fCellWidth * 2, m_fHeight, 
				7 + m_fCellWidth * 3, 0, 7 + m_fCellWidth * 3, m_fHeight,
			};
			
			float fSize = 1.0f;
			float fSizeIncrement = 10.0f;
			
			if (this.isInEditMode()) {
				m_paintComplementary.setTextSize(20.0f);
				m_paintSupplementary.setTextSize(20.0f);
			}
			else {
				while (true) {
					m_paintComplementary.setTextSize(fSize);
					
					float fFontHeight = m_paintComplementary.getFontMetrics().ascent * -1;
					float fDiff = (m_fCellHeight - m_fCellPadding * 2) - fFontHeight;
					
					if (fDiff < -0.5) {
						fSizeIncrement /= 2;
						fSize -= fSizeIncrement;
					}
					else if (fDiff > 0.5) {
						fSize += fSizeIncrement;
					}
					else {
						m_paintComplementary.setTextSize(fSize);
						m_paintSupplementary.setTextSize(fSize);
						break;
					}
				}
			}
			
			m_bGotDimensions = true;
		}		
	}
		
	public void setOnReadyToSolveListener(ReadyToSolveListener listener) {
		m_alRTSListeners.add(listener);
	}
	
	public void setOnLetterClickedListener(LetterClickedListener listener) {
		m_alLCListeners.add(listener);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (!m_bGotDimensions) {
			getCellDimensions(this.getWidth(), this.getHeight());
		}
		
		setPaints(false);
		
		canvas.drawLines(m_afLinePoints, m_paintComplementary);
		canvas.drawRect(3 + m_fCellWidth, 3 + m_fCellHeight, 5 + m_fCellWidth * 2, 5 + m_fCellHeight * 2, m_paintComplementary);
		
		for(int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				int index = i + j * 3;
				if (m_bHighlighted && index == m_iHighlightedIndex) {
					canvas.drawRect(
						(2 * i + 1) + (i * m_fCellWidth), 
						(2 * j + 1) + (j * m_fCellHeight), 
						(2 * (i + 1) + 1) + ((i + 1) * m_fCellWidth), 
						(2 * (j + 1) + 1) + ((j + 1) * m_fCellHeight),
						m_paintHighlighted
					);
					
					float x = (2 * i + 1) + (i * m_fCellWidth) + (m_fCellWidth / 2);
					float y = (2 * j + 1) + (j * m_fCellHeight) + (m_fCellHeight - m_fCellPadding);
					
					Paint paint = m_paintComplementary;
					if (index == MANDATORY_CHAR_INDEX) {
						paint = m_paintSupplementary;
					}
					
					canvas.drawText("_", x, y, paint); 
				}
				else if (m_acAllChars[index] != 0) {					
					float x = (2 * i + 1) + (i * m_fCellWidth) + (m_fCellWidth / 2);
					float y = (2 * j + 1) + (j * m_fCellHeight) + (m_fCellHeight - m_fCellPadding);

					Paint paint = m_paintComplementary;
					if (index == MANDATORY_CHAR_INDEX) {
						paint = m_paintSupplementary;
					}
					
					canvas.drawText(String.valueOf(m_acAllChars[index]), x, y, paint);
				}
			}
		}
		
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (!m_bGotDimensions) {
			getCellDimensions(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
		}
		
		if (m_fWidth > 0.0f && m_fHeight > 0.0f) {
			setMeasuredDimension((int)m_fWidth, (int)m_fHeight);
		}
		else {
			setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (m_bReadOnly) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				float x = event.getX();
				float y = event.getY();
				
				int i = 0, j = 0;
				
				for (float iMax = 2 + m_fCellWidth; x > iMax; iMax += 2 + m_fCellWidth)	{ i++; }
				for (float jMax = 2 + m_fCellHeight; y > jMax; jMax += 2 + m_fCellHeight) { j++; }
				
				if (i < 3 && j < 3) {
					int index = i + j * 3;
					
					if(m_acAllChars[index] != 0) {
						
						for( LetterClickedListener listener : m_alLCListeners ) {
							listener.onLetterClicked(this, index, m_acAllChars[index]);
						}
					}
				}
			}
			
			return true;
		}
		else {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				
				float x = event.getX();
				float y = event.getY();
				
				int i = 0, j = 0;
				
				for (float iMax = 2 + m_fCellWidth; x > iMax; iMax += 2 + m_fCellWidth)	{ i++; }
				for (float jMax = 2 + m_fCellHeight; y > jMax; jMax += 2 + m_fCellHeight) { j++; }
				
				if (i < 3 && j < 3) {
					int index = i + j * 3;				
					if (!m_bHighlighted || m_iHighlightedIndex != index) {
						m_bHighlighted = true;
						m_iHighlightedIndex = index;
					}
					else {
						m_bHighlighted = false;
						((InputMethodManager)m_context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getWindowToken(), 0);
					}
					
					invalidate();
				}
				else if(m_bHighlighted) {
					m_bHighlighted = false;
					((InputMethodManager)m_context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getWindowToken(), 0);
					invalidate();
				}
				
				if (m_bHighlighted) {
					InputMethodManager imm = (InputMethodManager)(m_context.getSystemService(Context.INPUT_METHOD_SERVICE));
					imm.showSoftInput(this, InputMethodManager.SHOW_FORCED);
				}
			}
			
			return true;
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (m_bReadOnly) {
			return false;
		}
		
		boolean bConsumed = false;
		
		if (m_bHighlighted) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
				m_acAllChars[m_iHighlightedIndex] = 0;
				
				if (m_iHighlightedIndex > 0) {
					m_iHighlightedIndex--;
				}
				else {
					m_bHighlighted = false;
					((InputMethodManager)m_context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getWindowToken(), 0);
				}
				
				invalidate();
				m_bAllCharsFilled = false;
				bConsumed = true;
			}
			else {
			
				char c = event.getMatch(ACCEPTABLE_CHARS, KeyEvent.META_SHIFT_ON);
				
				if (c != 0) {
					m_acAllChars[m_iHighlightedIndex] = c;
					
					if (m_iHighlightedIndex == 8) {
						m_bHighlighted = false;
						((InputMethodManager)m_context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getWindowToken(), 0);
					}
					else {
						m_iHighlightedIndex++;
					}
					
					invalidate();
					
					boolean bNoNulls = true;
					for (int i = 0; i < m_acAllChars.length; i++) {
						if (m_acAllChars[i] == 0) {
							bNoNulls = false;
							break;
						}
					}
					
					if (bNoNulls) {
						m_bAllCharsFilled = true;
						for( ReadyToSolveListener listener : m_alRTSListeners ) {
							listener.onReadyToSolve(this);
						}
					}
					
					bConsumed = true;
				}
			}
		}
		
		return bConsumed;
	}
	
	public boolean isReadyToSolve() {
		return m_bAllCharsFilled;
	}
	
	public char getChar(int position) {
		return m_acAllChars[position];
	}
	
	public void clear() {
		for (int i=0; i < m_acAllChars.length; i++) {
			m_acAllChars[i] = 0;
		}
		
		m_bAllCharsFilled = false;
		
		invalidate();
	}
	
	public String getCellsState() {
		
		String strState = "";
		
		for (int i=0; i < m_acAllChars.length; i++) {
			if(m_acAllChars[i] == 0) {
				strState += '_';
			}
			else {
				strState += m_acAllChars[i];
			}
		}
		
		return strState;
	}
	
	public void setCellsState(String state) {
		char acState[] = state.toCharArray();
		
		m_bAllCharsFilled = true;
		
		for (int i=0; i < acState.length; i++) {
			if(acState[i] == '_') {
				m_acAllChars[i] = 0;
				m_bAllCharsFilled = false;
			}
			else {
				m_acAllChars[i] = acState[i];
			}
		}
		
		invalidate();
	}
}
