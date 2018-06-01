package com.rem.iqalufinderandroid;


import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.rem.ifinder.Finder;
import com.rem.ifinder.Info;
import com.rem.ifinder.Log;
import com.rem.ifinder.image.regions.Region;
import com.rem.ifinder.interfaces.StreamLoader;
import com.rem.ifinder.interfaces.StreamSaver;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;

public class MainActivity extends Activity {

	private Finder finder;
	private GLRenderer renderer;
	private InputMethodManager imm;
	private GLPanel panel;
	private AutoCompleteTextView textInput;
	private MainTextListener textListener;
	private MainTextAdapter adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(finder==null){
			setup();
			finder = Finder.load("iqaluit");
			renderer = new GLRenderer(this,"iqaluit");
			adapter = new MainTextAdapter(this,
					android.R.layout.simple_dropdown_item_1line,finder);
			textListener = new MainTextListener(finder,this);
			textListener.onLoad();
		}
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		panel = ((GLPanel)findViewById(R.id.glpanel));
		panel.setRenderer(renderer);
		
		textInput = (AutoCompleteTextView) findViewById(R.id.text_input);
		textInput.setText(textListener.getCurrentText());
		textInput.setThreshold(2);
		textInput.addTextChangedListener(textListener);
		textInput.setOnKeyListener(textListener);
		textInput.requestFocus();
		
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(textInput, InputMethodManager.SHOW_IMPLICIT);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		textInput.setAdapter(adapter);
		textInput.setImeActionLabel("Done", KeyEvent.KEYCODE_ENTER);
	}
	

	public List<Info> getMarkers(float x, float y) {
		x = x*Region.imageWidth/(Segment.imageWidth);
		y = y*Region.imageHeight/(Segment.imageHeight);
		List<Info> result = new ArrayList<Info>();
		for(Info info:finder.getAllInfos()){
			if(info.getRegion().encompasses(x,y)){
				result.add(info);
			}
		}
		return result;
	}

	public void finalizeMarker(Info finalInfoForMarker) {
		if(finalInfoForMarker!=null){
			renderer.setMarkerLocation(
					finalInfoForMarker.getRegion().getMiddleX(),
					finalInfoForMarker.getRegion().getMiddleY());
			renderer.finalizeMarker(
					finalInfoForMarker.getName());
		}
	}
	
	public void setMarkerLocation(Info next) {
		if(next!=null){
			renderer.setMarkerLocation(
					next.getRegion().getMiddleX(),
					next.getRegion().getMiddleY());
		}
		else {
			renderer.setMarkerVisible(false);
		}
	}

	public void setText(String newText) {
		textInput.setText(newText);
	}
	
	public void lowerKeyboard(){

		View view = this.getCurrentFocus();
		if (view != null) { 
			view.clearFocus();
		    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	public void setup(){
		final Resources resources = getResources();

		// Remove the title bar from the window.
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Make the windows into full screen mode.
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		Finder.setup(
				new StreamLoader.Default(){
					@Override
					public boolean open(String type, String fileName,String extension) {
						if(resources.getIdentifier("static_"+type+"_"+fileName, "raw", getPackageName())!=0){
							inputFile = resources.openRawResource(
									resources.getIdentifier("static_"+type+"_"+fileName, "raw", getPackageName()));
							return true;
						}
						else {
							try {
								inputFile = openFileInput(type+"_"+fileName+"."+extension);
								return true;
							} catch (FileNotFoundException e) {
								return false;
							}
						}
					}
				},
				new StreamSaver.Default() {
					@Override
					public boolean open(String type, String fileName, String extension) {
						try {
							outputFile = openFileOutput(type+"_"+fileName+"."+extension, Context.MODE_PRIVATE);
							return true;
						}
						catch(FileNotFoundException e){
							return false;
						}
					}
				});
		Log.log = new Log(){
			private String tag = null;
			@Override
			public void c(String message){
				tag = message;
			}
			@Override
			public void l(String message){
				android.util.Log.d(tag,message);
			}
		};
	}

	@Override
	public void onPause(){
		if(finder!=null){
			finder.onClose();
			renderer.onClose();
		}
		super.onPause();
	}
	@Override
	public void onDestroy(){
		if(finder!=null){
			finder.onClose();
			renderer.onClose();
		}
		super.onPause();
	}
	@Override
	public void onStop(){
		if(finder!=null){
			finder.onClose();
			renderer.onClose();
		}
		super.onPause();
	}



}
