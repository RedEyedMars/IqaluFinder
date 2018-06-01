package com.rem.iqalufinderandroid;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.rem.ifinder.Finder;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.DisplayMetrics;
import android.util.Log;

public class GLRenderer implements Renderer {

	private float[] mModelMatrix = new float[16];
	private float[] mViewMatrix = new float[16];
	private float[] mProjectionMatrix = new float[16];
	private float[] mMVPMatrix = new float[16];

	/** Store our model data in a float buffer. */
	private FloatBuffer mPositions;
	private FloatBuffer mNormals;
	private FloatBuffer mTextureCoordinates;

	public static final int mBytesPerFloat = 4;	
	private final int mPositionDataSize = 3;
	private final int mColorDataSize = 4;	
	private final int mNormalDataSize = 3;

	private final int mTextureCoordinateDataSize = 2;

	private int mProgramHandle;

	public static int screenWidth = 0;
	public static int screenHeight = 0;
	private float xScale = 1f;
	private float yScale = 1f;

	private MainActivity context;
	private String sourceName;
	private float currentX;
	private float currentY;
	private Segment[][] segments;
	private float yRange = 9.5f;
	private float xRange = 45f;

	private final float eyeZ = -0.5f;
	private final float lookZ = -5.0f;

	private final float upX = 0.0f;
	private final float upY = 1.0f;
	private final float upZ = 0.0f;


	private int texturesLoadedThisFrame = -20;
	private int iPositionHandle = 2;
	private int iColorHandle = 3;
	private int iNormalHandle = 4;
	private int iTextureCoordinateHandle = 5;
	private int iMVMatrixHandle = 1 ;
	private int iMVPMatrixHandle = 0;

	private float zoomFactor = 1f;

	public Marker temporaryMarker;
	private int markerTexture = 0;
	private List<Marker.Event> markerEvents;
	private Marker[] finalizedMarkers ;

	public GLRenderer(MainActivity c, String fileName) {
		context = c;
		sourceName = fileName;
		markerEvents = new ArrayList<Marker.Event>();


	}
	public void zoom(float factor){

		zoomFactor  *= factor;
		// Don't let the object get too small or too large.
		zoomFactor = Math.max(1f, Math.min(zoomFactor, 12.0f));

		adjust();
	}
	public void move(float dx, float dy){
		currentX += -dx*4/screenWidth;
		currentY += dy*5/screenHeight;

		adjust();
	}
	private void adjust(){
		if(currentX < 2f-zoomFactor*2f){
			currentX = 2f-zoomFactor*2f;
		}
		else if(currentX > xRange*1f+zoomFactor*1f){
			currentX = xRange*1f+zoomFactor*1f;
		}
		if(-currentY < 2.5f-zoomFactor*2.5f){
			currentY = -2.5f+zoomFactor*2.5f;
		}
		else if(-currentY > yRange*1.33f+zoomFactor*1.5f){
			currentY = -yRange*1.33f-zoomFactor*1.5f;
		}
	}
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		finalizedMarkers = Marker.createMarkerList(1);
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(
				context.getResources(), 
				context.getResources().getIdentifier(sourceName+"_0_0", "drawable", context.getPackageName()),
				options);
		Segment.imageWidth = options.outWidth*Segment.segmentation;
		Segment.imageHeight = options.outHeight*Segment.segmentation;
		
		translateXFactor /= Segment.imageWidth/Segment.segmentation;
		translateYFactor /= Segment.imageHeight/Segment.segmentation;

		Segment.screenWidth = 2059;
		Segment.screenHeight = 1570;


		currentX = 0;// westMostX;
		currentY = 0;//northMostY;

		
		if(Finder.loader.open("positions", "position", "dat")){
			try {
				//currentX = Finder.loader.asInt();
				//currentY = Finder.loader.asInt();
				//zoomFactor = ((float)Finder.loader.asInt())/100f;
				Finder.loader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		// X, Y, Z
		final float[] positionData = {
				-1.0f, 1.0f, 1.0f,				
				-1.0f, -1.0f, 1.0f,
				1.0f, 1.0f, 1.0f, 
				-1.0f, -1.0f, 1.0f, 				
				1.0f, -1.0f, 1.0f,
				1.0f, 1.0f, 1.0f,
		};	



		// X, Y, Z
		// The normal is used in light calculations and is a vector which points
		// orthogonal to the plane of the surface. For a cube model, the normals
		// should be orthogonal to the points of each face.
		final float[] normalData = {
				0.0f, 0.0f, 1.0f,				
				0.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f,				
				0.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f,
		};

		// S, T (or X, Y)
		// Texture coordinate data.
		// Because images have a Y axis pointing downward (values increase as you move down the image) while
		// OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
		// What's more is that the texture coordinates are the same for every face.
		final float[] textureCoordinateData =
			{												
					// Front face
					0.0f, 0.0f, 				
					0.0f, 1.0f,
					1.0f, 0.0f,
					0.0f, 1.0f,
					1.0f, 1.0f,
					1.0f, 0.0f
			};

		// Initialize the buffers.
		mPositions = ByteBuffer.allocateDirect(positionData.length * mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();							
		mPositions.put(positionData).position(0);

		mNormals = ByteBuffer.allocateDirect(normalData.length * mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();							
		mNormals.put(normalData).position(0);

		mTextureCoordinates = ByteBuffer.allocateDirect(textureCoordinateData.length * mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mTextureCoordinates.put(textureCoordinateData).position(0);

		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		screenWidth = metrics.widthPixels;
		screenHeight = metrics.heightPixels;
		Segment.overviewSampleSize = calculateInSampleSize(Segment.imageWidth/Segment.segmentation,Segment.imageHeight/Segment.segmentation, screenWidth/10, screenHeight/10);
		Segment.zoomedSampleSize = calculateInSampleSize(Segment.imageWidth/Segment.segmentation,Segment.imageHeight/Segment.segmentation, screenWidth/4, screenHeight/4);
		if(Segment.imageWidth > Segment.imageHeight){
			xScale = (float)Segment.imageWidth / Segment.imageHeight/2;
		}
		else {
			yScale = (float)Segment.imageHeight/Segment.imageWidth/2  ;
		}


		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GLES20.glEnable(GLES20.GL_CULL_FACE);

		Matrix.setLookAtM(mViewMatrix, 0, 
				currentX, currentY, eyeZ, 
				currentX, currentY, lookZ, 
				upX, upY, upZ);	

		try {
			Finder.loader.open("gl","per_pixel_vertex_shader","glsl");
			final String vertexShader = Finder.loader.asString();   
			Finder.loader.close();
			Finder.loader.open("gl","per_pixel_fragment_shader","glsl");
			final String fragmentShader = Finder.loader.asString();
			Finder.loader.close();

			final int vertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);		
			final int fragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);		

			mProgramHandle = createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, 
					new String[] {"a_Position",  "a_Color", "a_Normal", "a_TexCoordinate"});								                                							       
		} catch (IOException e) {
			e.printStackTrace();
		}		

		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);


		markerTexture = loadTexture(R.drawable.marker,false);
		segments = new Segment[Segment.segmentation][];
		for(int i=0;i<Segment.segmentation;++i){
			segments[i] = new Segment[Segment.segmentation];
			for(int j=0;j<Segment.segmentation;++j){
				segments[i][j] = new Segment(
						i*Segment.imageWidth/Segment.segmentation,
						j*Segment.imageHeight/Segment.segmentation);
			}
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		screenWidth = width;
		screenHeight = height;
		if(Segment.imageWidth > Segment.imageHeight){
			xScale = (float)Segment.imageWidth / Segment.imageHeight;
		}
		else {
			yScale = (float)Segment.imageHeight/Segment.imageWidth  ;
		}

		GLES20.glViewport(0, 0, width, height);
		Matrix.frustumM(mProjectionMatrix, 0, (float) -width/height, (float) width / height, -1.0f, 1.0f, 0.5f, 100.0f);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		if(temporaryMarker==null){
			temporaryMarker = new Marker();
			while(!markerEvents.isEmpty()){
				markerEvents.remove(0).act(this,temporaryMarker);
			}
		}
		try {
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);            

			// Set our per-vertex lighting program.
			GLES20.glUseProgram(mProgramHandle);

			Matrix.setLookAtM(mViewMatrix, 0, 
					currentX, currentY, eyeZ, 
					currentX, currentY, lookZ, 
					upX, upY, upZ);	
			// Set program handles for cube drawing.
			final int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
			final int mMVMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVMatrix"); 
			final int mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");
			final int mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
			final int mColorHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Color");
			final int mNormalHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Normal"); 
			final int mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate");
			final int[] rendererInfo = {mMVPMatrixHandle,mMVMatrixHandle,mPositionHandle,mColorHandle,mNormalHandle,mTextureCoordinateHandle};
			// Set the active texture unit to texture unit 0.
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glUniform1i(mTextureUniformHandle, 0);        




			int startX = (int)(currentX/2.66f+Segment.screenWidth/9f/32f+zoomFactor/3f-1f);
			int startY = (int)(-currentY/1.75f+Segment.screenHeight/4f/32f+zoomFactor/3.5f-1f);
			int maxX2 = (int)(7-zoomFactor/2.5f); 
			segments[startX][startY].draw(this, rendererInfo);
			for(int j=0;j<=((float)maxX2+0.5f)*2f;++j){
				
				if(j<=maxX2){
					for(int i=startX-j-1;i<=startX+j+1;++i){
						if(i>=0&&i<segments.length){
							if(startY-j>=0){
								segments[i][startY-j].draw(this, rendererInfo);
							}
							if(startY+j<segments.length){
								segments[i][startY+j].draw(this, rendererInfo);
							}
						}
					}
					for(int i=startY-j;i<=startY+j;++i){
						if(i>=0&&i<segments.length){
							if(startX-j-1>=0){
								segments[startX-j-1][i].draw(this, rendererInfo);
							}
							if(startX+j+1<segments.length){
								segments[startX+j+1][i].draw(this, rendererInfo);
							}
						}
					}
				}
				else {
					for(int i=startX-maxX2-1;i<=startX+maxX2+1;++i){
						if(i>=0&&i<segments.length){
							if(startY-j>=0){
								segments[i][startY-j].draw(this, rendererInfo);
							}
							if(startY+j<segments.length){
								segments[i][startY+j].draw(this, rendererInfo);
							}
						}
					}
				}
			}
			for(Segment[] segmentList:segments){
				for(Segment segment:segmentList){
					segment.destroyIfUnused(this);
				}
			}

			for(int i=0;i<finalizedMarkers.length;++i){
				if(finalizedMarkers[i]!=null){
					finalizedMarkers[i].draw(this,rendererInfo);
				}
			}
			temporaryMarker.draw(this,rendererInfo);

			texturesLoadedThisFrame = 0;
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	private void draw(int[] rendererInfo, FloatBuffer mColors)	{
		// Pass in the position information
		mPositions.position(0);		
		GLES20.glVertexAttribPointer(rendererInfo[iPositionHandle], mPositionDataSize, GLES20.GL_FLOAT, false,0, mPositions);
		GLES20.glEnableVertexAttribArray(rendererInfo[iPositionHandle]);
		// Pass in the color information
		mColors.position(0);
		GLES20.glVertexAttribPointer(rendererInfo[iColorHandle], mColorDataSize, GLES20.GL_FLOAT, false,0, mColors);
		GLES20.glEnableVertexAttribArray(rendererInfo[iColorHandle]);

		// Pass in the normal information
		mNormals.position(0);
		GLES20.glVertexAttribPointer(rendererInfo[iNormalHandle], mNormalDataSize, GLES20.GL_FLOAT, false,0, mNormals);

		GLES20.glEnableVertexAttribArray(rendererInfo[iNormalHandle]);
		mTextureCoordinates.position(0);
		GLES20.glVertexAttribPointer(rendererInfo[iTextureCoordinateHandle], mTextureCoordinateDataSize, GLES20.GL_FLOAT, false,0, mTextureCoordinates);
		GLES20.glEnableVertexAttribArray(rendererInfo[iTextureCoordinateHandle]);
		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
		GLES20.glUniformMatrix4fv(rendererInfo[iMVMatrixHandle], 1, false, mMVPMatrix, 0);
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
		GLES20.glUniformMatrix4fv(rendererInfo[iMVPMatrixHandle], 1, false, mMVPMatrix, 0);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);         
	}

	private float translateXFactor = 1.31f*2;
	private float translateYFactor = 1f*2;
	public void draw(int texture, int x, int y, int[] renderInfo, FloatBuffer colors){
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, (float)x*translateXFactor-16.5f-zoomFactor/2, zoomFactor/2+23.5f-(float)y*translateYFactor, zoomFactor-15f);
		Matrix.scaleM(mModelMatrix, 0, xScale, yScale, 1f);
		draw(renderInfo,colors);
	}
	public void drawMarker(float x, float y, int[] renderInfo, FloatBuffer colors){
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, markerTexture);
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, (float)x*translateXFactor-3.35f, 4.7f-(float)y*translateYFactor, -6.0f);
		Matrix.scaleM(mModelMatrix, 0, 0.25f, 0.25f, zoomFactor);
		draw(renderInfo,colors);
	}
	public void drawPointToMarker(float screenX, float screenY, float x, float y, int[] renderInfo, FloatBuffer colors){
		float angleOfAttack = (float) ((Math.atan2(
				(screenY+Segment.screenHeight/2)-y,
				-(screenX+Segment.screenWidth/2-x))+Math.PI/2)*360/Math.PI/2);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, markerTexture);
		Matrix.setIdentityM(mModelMatrix, 0);

		Matrix.translateM(mModelMatrix, 0, (float)(currentX*2), (float)(currentY*2.620f), -6f);
		Matrix.translateM(mModelMatrix, 0, (float)(2f/zoomFactor*Math.sin(angleOfAttack*Math.PI*2/360)), (float)(-2f/zoomFactor*Math.cos(angleOfAttack*Math.PI*2/360)), 0f);
		Matrix.rotateM(mModelMatrix, 0, angleOfAttack,0f,0f,1f);
		Matrix.scaleM(mModelMatrix, 0, 0.35f, 0.35f, zoomFactor);
		//Matrix.translateM(mModelMatrix, 0, 0.5f, -0.5f, 0f);
		//
		draw(renderInfo,colors);
	}
	public int loadTexture(int x, int y){

		int resourceId = context.getResources().getIdentifier(sourceName+"_"+x+"_"+y,"drawable",context.getPackageName());
		if(resourceId==0){
			return 0;
		}
		if(texturesLoadedThisFrame>7-zoomFactor/2.5f){
			return 0;
		}
		++texturesLoadedThisFrame;
		return loadTexture(resourceId,true);
	}
	public int loadTexture(int resourceId, boolean isBig){
		final int[] textureHandle = new int[1];

		GLES20.glGenTextures(1, textureHandle, 0);

		if (textureHandle[0] != 0) {
			final Bitmap bitmap;
			if(isBig){
				if(zoomFactor<5f){
					bitmap = GLRenderer.decodeSampledBitmapFromResource(context.getResources(), resourceId,Segment.overviewSampleSize);
				}
				else {
					bitmap = GLRenderer.decodeSampledBitmapFromResource(context.getResources(), resourceId,Segment.zoomedSampleSize);
				}
			}
			else {

				final BitmapFactory.Options options = new BitmapFactory.Options();
				options.inScaled = false;   // No pre-scaling
				bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
			}

			// Read in the resource

			// Bind to the texture in OpenGL
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

			// Set filtering
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

			// Load the bitmap into the bound texture.
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

			// Recycle the bitmap, since its data has been loaded into OpenGL.
			bitmap.recycle();
		}

		if (textureHandle[0] == 0)
		{
			throw new RuntimeException("Error loading texture.");
		}

		return textureHandle[0];
	}
	public void unloadTexture(int texture) {
		GLES20.glDeleteTextures(1, new int[]{texture}, 0);
	}
	public void onClose() {
		try {
			Finder.saver.open("positions", "position", "dat");
			Finder.saver.asInts(new int[]{(int) currentX, (int) currentY, (int) (zoomFactor*100)});
			Finder.saver.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static int calculateInSampleSize(
			int width,int height, int reqWidth, int reqHeight) {
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
			int sampleSize) {
		// Calculate inSampleSize
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = sampleSize;

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	/** 
	 * Helper function to compile a shader.
	 * 
	 * @param shaderType The shader type.
	 * @param shaderSource The shader source code.
	 * @return An OpenGL handle to the shader.
	 */
	public static int compileShader(final int shaderType, final String shaderSource) 
	{
		int shaderHandle = GLES20.glCreateShader(shaderType);

		if (shaderHandle != 0) 
		{
			// Pass in the shader source.
			GLES20.glShaderSource(shaderHandle, shaderSource);

			// Compile the shader.
			GLES20.glCompileShader(shaderHandle);

			// Get the compilation status.
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

			// If the compilation failed, delete the shader.
			if (compileStatus[0] == 0) 
			{
				android.util.Log.e("ShadeHelper", "Error compiling shader: " + GLES20.glGetShaderInfoLog(shaderHandle));
				GLES20.glDeleteShader(shaderHandle);
				shaderHandle = 0;
			}
		}

		if (shaderHandle == 0)
		{			
			throw new RuntimeException("Error creating shader.");
		}

		return shaderHandle;
	}

	/**
	 * Helper function to compile and link a program.
	 * 
	 * @param vertexShaderHandle An OpenGL handle to an already-compiled vertex shader.
	 * @param fragmentShaderHandle An OpenGL handle to an already-compiled fragment shader.
	 * @param attributes Attributes that need to be bound to the program.
	 * @return An OpenGL handle to the program.
	 */
	public static int createAndLinkProgram(final int vertexShaderHandle, final int fragmentShaderHandle, final String[] attributes) 
	{
		int programHandle = GLES20.glCreateProgram();

		if (programHandle != 0) 
		{
			// Bind the vertex shader to the program.
			GLES20.glAttachShader(programHandle, vertexShaderHandle);			

			// Bind the fragment shader to the program.
			GLES20.glAttachShader(programHandle, fragmentShaderHandle);

			// Bind attributes
			if (attributes != null)
			{
				final int size = attributes.length;
				for (int i = 0; i < size; i++)
				{
					GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
				}						
			}

			// Link the two shaders together into a program.
			GLES20.glLinkProgram(programHandle);

			// Get the link status.
			final int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

			// If the link failed, delete the program.
			if (linkStatus[0] == 0) 
			{				
				android.util.Log.e("ShadeHelper", "Error compiling program: " + GLES20.glGetProgramInfoLog(programHandle));
				GLES20.glDeleteProgram(programHandle);
				programHandle = 0;
			}
		}

		if (programHandle == 0)
		{
			throw new RuntimeException("Error creating program.");
		}

		return programHandle;
	}
	public void setMarkerLocation( int x, int y) {

		if(temporaryMarker == null){
			markerEvents.add(new Marker.Event.Move(x, y));
		}
		else {
			temporaryMarker.setLocation(x, y);
		}
	}
	public void finalizeMarker(String name){

		temporaryMarker.setName(name);
		for(int i=finalizedMarkers.length-1;i>0;--i){
			finalizedMarkers[i] = finalizedMarkers[i-1];
		}
		finalizedMarkers[0] = temporaryMarker;
		temporaryMarker = null;
	}
	public void setMarkerVisible(boolean isVisible){
		if(temporaryMarker == null){
			if(isVisible){
				markerEvents.add(new Marker.Event.Show());
			}
			else {
				markerEvents.add(new Marker.Event.Hide());
			}
		}
		else {
			temporaryMarker.setVisible(isVisible);
		}
	}
	public float getZoom() {
		return zoomFactor;
	}
}
