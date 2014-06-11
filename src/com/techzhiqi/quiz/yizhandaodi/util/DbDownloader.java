package com.techzhiqi.quiz.yizhandaodi.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.annotation.SuppressLint;
import android.util.Base64;
import android.util.Log;
import android.content.Context;

import com.techzhiqi.quiz.yizhandaodi.db.DBHelper;
import com.techzhiqi.quiz.yizhandaodi.quiz.GamePlay;

public class DbDownloader {
	private Context context = null;
	private Flags flags = null;
	public DbDownloader(Context context, Flags flags){
		this.context = context;
		this.flags = flags;
	}

	@SuppressLint("NewApi")
	public synchronized boolean download(String myurl) throws IOException {
		InputStream is = null;
		
		String outFileName = "/data/data/com.techzhiqi.quiz.yizhandaodi/databases/newBinaryFile";
		/*DBHelper myDbHelper = new DBHelper(getApplicationContext());
		myDbHelper.getReadableDatabase();
		myDbHelper.close();*/
		DBHelper myDbHelper = new DBHelper(context, GamePlay.MAIN_DB);
		myDbHelper.createDataBase();
		
		FileOutputStream out = new FileOutputStream(outFileName);
		new DBHelper(context, GamePlay.MAIN_DB).createDataBase();
		String encoding = Base64.encodeToString("user:pass".getBytes(), Base64.NO_WRAP);
		try {
			URL url = new URL(myurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000 /* milliseconds */);
			conn.setConnectTimeout(15000 /* milliseconds */);		
			conn.setRequestProperty("Authorization", "Basic "+encoding);
			conn.setRequestProperty("DatabaseVersion", String.valueOf(myDbHelper.getDbVersion()));
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			// Starts the query
			conn.connect();
			int response = conn.getResponseCode();
			Log.d("yizhandaodi", "The response is: " + response);
			if(response==200){
				flags.download=true;
				if(conn.getHeaderField("yourversionis").equals("dated")){
					is = conn.getInputStream();

					byte[] buffer = new byte[2048];
					int bytesRead;
					while ((bytesRead = is.read(buffer)) != -1) {
						out.write(buffer, 0, bytesRead);
					}
					out.flush();
					flags.getnewdb=true;
					rename("/data/data/com.techzhiqi.quiz.yizhandaodi/databases/",
									"test2.db", "test2.db.backup");
					rename("/data/data/com.techzhiqi.quiz.yizhandaodi/databases/",
									"newBinaryFile", "test2.db");
				}else{
					flags.getnewdb=false;
				}			
				return true;
			}else{
				flags.download=false;
				return false;
			}
			

			// Makes sure that the InputStream is closed after the app is
			// finished using it.
		} catch (IOException e) {
			Log.e("yizhandaodi", "downloading exception", e);
			return false;
		} finally {
			if (is != null) {
				is.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}

	private boolean rename(String base, String from, String to) {
		File b = new File(base);
		File f = new File(b, from);
		File t = new File(b, to);
		return f.renameTo(t);
	}
	

}
