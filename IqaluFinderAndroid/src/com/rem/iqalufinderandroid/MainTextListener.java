package com.rem.iqalufinderandroid;

import java.io.IOException;

import com.rem.ifinder.Finder;
import com.rem.ifinder.FinderEvent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;

public class MainTextListener implements TextWatcher, OnKeyListener {

	private MainActivity context;
	private Finder finder;
	private String currentText = null;

	public MainTextListener( 
			Finder finder, MainActivity mainActivity){
		this.context = mainActivity;
		this.finder = finder;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

		FinderEvent event = finder.get(s.toString());
		currentText = s.toString();
		if(event.hasInfos()){
			context.setMarkerLocation(event.getInfos().iterator().next());
		}
		else {
			context.setMarkerLocation(null);
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN)
        {
            switch (keyCode)
            {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                    FinderEvent retrevial = finder.get(currentText);
            		if(retrevial.hasInfos()){
            			context.finalizeMarker(retrevial.getInfos().iterator().next());
            			if(retrevial.numberOfInfos()==1){
            				finder.found(retrevial.getInfos().iterator().next());
            			}
            		}
            		else {
            			context.setMarkerLocation(null);
            		}
                	currentText = "";
                    context.setText("");
        			context.setMarkerLocation(null);
                    context.lowerKeyboard();
                    return true;
                default:
                    break;
            }
        }
        return false;
	}

	public void onLoad(){
		try {
			if(Finder.loader.open("texts", "text", "dat")){
				currentText = String.copyValueOf(Finder.loader.asChars());
				Finder.loader.close();
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		currentText = "";
	}
	public void onClose() {
		try {
			Finder.saver.open("texts", "text", "dat");
			Finder.saver.asChars(currentText);
			Finder.saver.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public CharSequence getCurrentText() {
		return currentText;
	}


}
