package com.ant32bit.targetfinder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TFLauncherActivity extends Activity implements OnClickListener {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher);
        
        ((Button)findViewById(R.id.btnLaunchNewGame)).setOnClickListener(this);
        ((Button)findViewById(R.id.btnLaunchSolver)).setOnClickListener(this);
    }
	
	@Override
	public void onClick(View view) {

		switch (view.getId()) {
		case R.id.btnLaunchNewGame:
			Intent intentNewGame = new Intent("com.ant32bit.targetfinder.GAME");
			startActivity(intentNewGame);
			break;
			
		case R.id.btnLaunchSolver:
			Intent intentSolver = new Intent("com.ant32bit.targetfinder.SOLVER");
			startActivity(intentSolver);
			break;
		}
	}

}
