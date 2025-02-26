package com.pdy.others;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import com.pdy.mobile.StaticMethod;

import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class AudioRecoderUtils {

	// 文件路径
	private String filePath;
	// 文件夹路径
	private String FolderPath;

	private MediaRecorder mMediaRecorder;
	private final String TAG = "fan";
	public static final int MAX_LENGTH = 1000 * 60 * 10;// 最大录音时长1000*60*10;

	private OnAudioStatusUpdateListener audioStatusUpdateListener;

	/**
	 * 文件存储默认sdcard/record
	 */
	public AudioRecoderUtils() {

		// 默认保存路径为/sdcard/record/下
		this(Environment.getExternalStorageDirectory() + "/record/");
	}

	public AudioRecoderUtils(String filePath) {

		File path = new File(filePath);
		if (!path.exists())
			path.mkdirs();

		this.FolderPath = filePath;
	}

	private long startTime;
	private long endTime;
	private Date cancelStartTime;

	/**
	 * 开始录音 使用amr格式 录音文件
	 * 
	 * @return
	 */
	public void startRecord() {
		// 开始录音
		/* ①Initial：实例化MediaRecorder对象 */
		if (mMediaRecorder == null)
			mMediaRecorder = new MediaRecorder();
		try {
			/* ②setAudioSource/setVedioSource */
			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 设置麦克风
			/* ②设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default 声音的（波形）的采样 */
			mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
			/*
			 * ②设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP(3gp格式
			 * ，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
			 */
			mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

			Date dt = new Date();
			Long time = dt.getTime();
			filePath = StaticMethod.GetFilePath() + "audio" + time + ".mp3";
			/* ③准备 */
			mMediaRecorder.setOutputFile(filePath);
			mMediaRecorder.setMaxDuration(MAX_LENGTH);
			mMediaRecorder.prepare();
			/* ④开始 */
			mMediaRecorder.start();
			 cancelStartTime=new Date();
			// AudioRecord audioRecord.
			/* 获取开始时间* */
			startTime = System.currentTimeMillis();
			updateMicStatus();
			Log.e("fan", "startTime" + startTime);
		} catch (IllegalStateException e) {
			Log.i(TAG, "call startAmr(File mRecAudioFile) failed!" + e.getMessage());
		} catch (IOException e) {
			Log.i(TAG, "call startAmr(File mRecAudioFile) failed!" + e.getMessage());
		}
	}

	/**
	 * 停止录音
	 */
	public long stopRecord() {
		if (mMediaRecorder == null)
			return 0L;
		endTime = System.currentTimeMillis();
		mMediaRecorder.stop();
		mMediaRecorder.reset();
		mMediaRecorder.release();
		mMediaRecorder = null;

		audioStatusUpdateListener.onStop(filePath);
		filePath = "";
		return endTime - startTime;
	}

	/**
	 * 取消录音
	 * @param audio 
	 */
	public void cancelRecord(final TextView audio) {
	
		
		long delayMillis=1500-(new Date().getTime()-cancelStartTime.getTime());
		if(delayMillis<0)
		{
			delayMillis=0;
		}
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				mMediaRecorder.stop();
				mMediaRecorder.reset();
				mMediaRecorder.release();
				mMediaRecorder = null;
				audio.setEnabled(true);
				
				File file = new File(filePath);
				if (file.exists())
					file.delete();

				filePath = "";
				
			}
		}, delayMillis);
		
	}

	private final Handler mHandler = new Handler();
	private Runnable mUpdateMicStatusTimer = new Runnable() {
		public void run() {
			updateMicStatus();
		}
	};

	private int BASE = 1;
	private int SPACE = 100;// 间隔取样时间

	public void setOnAudioStatusUpdateListener(OnAudioStatusUpdateListener audioStatusUpdateListener) {
		this.audioStatusUpdateListener = audioStatusUpdateListener;
	}

	/**
	 * 更新麦克状态
	 */
	private void updateMicStatus() {

		if (mMediaRecorder != null) {
			double ratio = (double) mMediaRecorder.getMaxAmplitude() / BASE;
			double db = 0;// 分贝
			if (ratio > 1) {
				db = 20 * Math.log10(ratio);
				if (null != audioStatusUpdateListener) {
					audioStatusUpdateListener.onUpdate(db, System.currentTimeMillis() - startTime);
				}
			}
			mHandler.postDelayed(mUpdateMicStatusTimer, SPACE);
		}
	}

	public interface OnAudioStatusUpdateListener {
		/**
		 * 录音中...
		 * 
		 * @param db
		 *            当前声音分贝
		 * @param time
		 *            录音时长
		 */
		public void onUpdate(double db, long time);

		/**
		 * 停止录音
		 * 
		 * @param filePath
		 *            保存路径
		 */
		public void onStop(String filePath);
	}

}