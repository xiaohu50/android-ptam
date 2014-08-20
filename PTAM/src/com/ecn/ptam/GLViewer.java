package com.ecn.ptam;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.View;
import android.widget.Button;


class GLViewer extends GLSurfaceView implements View.OnClickListener  {
	private VideoSource _vs;
	private BatchRenderer _renderer;
	private CaptureController _controller;
	
	private enum State {INIT, STEREO_STARTED, STEREO_ENDED, GOT_OBJECT, RUNNING};
	private State _state;
	
	public GLViewer(Context context) {
		super(context);
		setDebugFlags(DEBUG_CHECK_GL_ERROR | DEBUG_LOG_GL_CALLS);
		_state = State.INIT;
		
		_vs = new VideoSource();
		
		_renderer = new BatchRenderer();
		_renderer.add_renderer(new CameraRenderer(_vs));
		_renderer.add_renderer(new FeedbackRenderer(_vs));
		
		setRenderer(_renderer);
	}
	
	@Override
	public void onResume() {
	}
	
	@Override
	public void onPause() {
		_vs.camera_release();
	}
	
	@Override
	public void onClick(View v) {
		Button btn = (Button)v;
		
		switch (_state) {
		case INIT:
			PTAM.send("Space");
			btn.setText("Finish stereo init");
			_state = State.STEREO_STARTED;
			break;
		case STEREO_STARTED:
			PTAM.send("Space");
			btn.setText("Store");
			_state = State.STEREO_ENDED;
			break;
		case STEREO_ENDED:
			Boolean done = _controller.store_corner();
			if (done) {
				_state = State.GOT_OBJECT;
				btn.setText("Start");
			}
			break;
		case GOT_OBJECT:
			_controller.start_capture();
			btn.setText("Stop");
			_state = State.RUNNING;
			break;
		case RUNNING:
			_controller.stop_capture();
		default:
			break;
		}
	}
}