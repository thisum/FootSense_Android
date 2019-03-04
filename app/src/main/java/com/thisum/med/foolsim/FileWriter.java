package com.thisum.med.foolsim;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class FileWriter {
	private static final String TAG = "[FileWriter]";
	private static final String baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
	private static FileWriter instance;
	private List<String> leftFoot;
	private List<String> rightFoot;
	private String filename;

	private FileWriter() {
		filename = "dummy_user";
		leftFoot = new ArrayList<>();
		rightFoot = new ArrayList<>();
	}

	static synchronized FileWriter getInstance() {
		if (instance == null) {
			instance = new FileWriter();
		}
		return instance;
	}

	void write(List<Integer> left, List<Integer> right, String name) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd-kkmm");
		String strLeft = format(left);
		String strRight = format(right);
		filename = name.trim().replaceAll("\\s", "_") + simpleDateFormat.format(new Date());

		writeToFile(strLeft, filename + "_left.csv");
		writeToFile(strRight, filename + "_right.csv");
	}

	private void writeToFile(String content, String filename) {
		Log.i(TAG, "writeToFile: "+content);
		File base = new File(baseDir,"foot-sense");
		if (!base.exists()) {
			base.mkdirs();
		}
		File file = new File(base, filename);
		java.io.FileWriter fileWriter = null;
		try {
			fileWriter = new java.io.FileWriter(file);
			fileWriter.write(content);
			fileWriter.flush();
		} catch (IOException e) {
			Log.e(TAG, "error while persisting: ", e);
		} finally {
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (IOException e) {
					Log.e(TAG, "error while persisting: ", e);
				}
			}
		}
	}

	private String format(List<Integer> array) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i : array) {
			stringBuilder.append(i).append(",");
		}
		return stringBuilder.toString();
	}

}
