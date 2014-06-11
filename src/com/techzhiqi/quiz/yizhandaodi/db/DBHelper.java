package com.techzhiqi.quiz.yizhandaodi.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.techzhiqi.quiz.yizhandaodi.quiz.Constants;
import com.techzhiqi.quiz.yizhandaodi.quiz.Question;

/**
 * @author zhiqi.lin
 * 
 */
public class DBHelper extends SQLiteOpenHelper {

	// The Android's default system path of your application database.
	private static String DB_PATH = "/data/data/com.techzhiqi.quiz.yizhandaodi/databases/";
	//private static String DB_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/yizhandaodi/databases/";
	private static String DB_NAME;
	private SQLiteDatabase myDataBase;
	private final Context myContext;
	private static final String logTAG = "yizhandaodi";

	/**
	 * Constructor Takes and keeps a reference of the passed context in order to
	 * access to the application assets and resources.
	 * 
	 * @param context
	 */
	public DBHelper(Context context, String dbName) {
		super(context, dbName, null, 1);
		this.DB_NAME = dbName;
		this.myContext = context;
	}

	/**
	 * Creates a empty database on the system and rewrites it with your own
	 * database.
	 * */
	public void createDataBase()  {
		delDataBase("test.db");
		delDataBase("test2.db");
		boolean dbExist = checkDataBase();
		if (!dbExist) {
			// By calling this method and empty database will be created into
			// the default system path
			// of your application so we are gonna be able to overwrite that
			// database with our database.
			this.getWritableDatabase();

			try {
				copyDataBase();
			} catch (IOException e) {
				throw new Error("Error copying database");
			}
		}
	}

	/**
	 * Check if the database already exist to avoid re-copying the file each
	 * time you open the application.
	 * 
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase() {
		SQLiteDatabase checkDB = null;
		try {
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READWRITE);
		} catch (SQLiteException e) {
			// database does't exist yet.
		}
		if (checkDB != null) {
			checkDB.close();
		}

		return checkDB != null ? true : false;
	}
	
	private boolean checkDataBase(String db_del) {
		SQLiteDatabase checkDB = null;
		try {
			String myPath = DB_PATH +db_del;
			checkDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READONLY);
		} catch (SQLiteException e) {
			// database does't exist yet.
		}
		if (checkDB != null) {
			checkDB.close();
		}

		return checkDB != null ? true : false;
	}
	
	private void delDataBase(String db_del){
		if(checkDataBase(db_del)){
			myContext.deleteDatabase(db_del);
		}else{
			Log.i("yizhandaodi", "database "+db_del+"is not exists");
		}
		removeFile(DB_PATH+db_del);
	}

	private void removeFile(String path) {
		File f = new File(path);
		if(f.exists()){
			f.delete();
			Log.i("yzdd-dbhelper", path + "deleted");
		}else{
			Log.i("yzdd-dbhelper", path + "does not exit");
		}
	}

	/**
	 * Copies your database from your local assets-folder to the just created
	 * empty database in the system folder, from where it can be accessed and
	 * handled. This is done by transfering bytestream.
	 * */
	private void copyDataBase() throws IOException {

		// Open your local db as the input stream
		InputStream myInput = myContext.getAssets().open(DB_NAME);

		// Path to the just created empty db
		String outFileName = DB_PATH + DB_NAME;

		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}

		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();

	}

	public void openDataBase() throws SQLException {
		// Open the database
		String myPath = DB_PATH + DB_NAME;
		myDataBase = SQLiteDatabase.openDatabase(myPath, null,
				SQLiteDatabase.OPEN_READWRITE);
	}
	

	@Override
	public synchronized void close() {
		if (myDataBase != null)
			myDataBase.close();
		super.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//myContext.deleteDatabase(DB_NAME);
	}

	// Add your public helper methods to access and get content from the
	// database.
	// You could return cursors by doing "return myDataBase.query(....)" so it'd
	// be easy
	// to you to create adapters for your views.

	public void cleanUsed() {
		int num=0;
		ContentValues values = new ContentValues();
		values.put("used", 0);
		num=myDataBase.update("question", values, null, null);
		Log.i(logTAG, "cleanUsed db.update returns: "+num);  
	}

	public void setUsed(int id) {
		int num=0;
		ContentValues values = new ContentValues();
		values.put("used", 1);
		num=myDataBase.update("question", values, "_id=" + id, null);
		// myDataBase.execSQL("update question set used=1 where _id="+id);
		Log.i(logTAG, "setUsed db.update returns: "+num); 
	}

	public List<Question> getQuestionSet(Integer diff,
			Integer cat, Integer tpc, Boolean isRandom,
			int startQId, int cacheSize) {
		boolean flag = false;
		ArrayList<Question> questionSet = new ArrayList<Question>();
		StringBuffer tail = new StringBuffer();
		if (diff != Constants.Difficulty_ALL) {
			tail.append(" difficulty=" + diff);
			flag = true;
		}
		if (cat != Constants.Category_ALL) {
			if (flag) {
				tail.append(" and ");
			}
			tail.append(" category=" + cat);
			flag = true;
		}
		if (tpc != Constants.Topic_ALL) {
			if (flag) {
				tail.append(" and ");
			}
			tail.append(" topic=" + tpc);
			flag = true;
		}
		if (isRandom) {
			tail.append(" ORDER BY RANDOM() LIMIT " + cacheSize);
			/*
			 * if(flag){ tail.append(" and "); }
			 * tail.append("used=0 ORDER BY RANDOM() LIMIT "+cacheSize);
			 */
		} else {
			if (flag) {
				tail.append(" and ");
			}
			tail.append("_id>=" + startQId);
			flag = true;
		}
		if (flag) {
			tail.insert(0, " where ");
		}

		Cursor c = myDataBase.rawQuery(
				"SELECT _id,question,answer,anwsercheckerid FROM question " + tail, null);

		while (c.moveToNext()) {
			// Log.d("QUESTION", "Question Found in DB: " + c.getString(1));
			Question q = new Question();
			q.setId(c.getInt(0));
			q.setQuestion(c.getString(1));
			Log.i(logTAG, "getQuestionSet question: "+c.getString(1)); 
			q.setAnswer(c.getString(2));
			Log.i(logTAG, "getQuestionSet anwser: "+c.getString(2)); 
			q.setCheckerId(c.getInt(3));
			Log.i(logTAG, "getQuestionSet checherid: "+c.getInt(3)); 
			questionSet.add(q);
		}
		return questionSet;
	}
	
	public int getDbVersion(){
		openDataBase();
		Cursor c = myDataBase.rawQuery("SELECT version FROM version", null);
		c.moveToLast();
		int ret = c.getInt(0);
		close();
		return ret;
	}
	
	/*public void setDbVersion(int version){
		openDataBase();
		ContentValues values = new ContentValues();
		values.put("version", version);
		int num=myDataBase.update("version", values, "_id=" + 1, null);
		Log.i(logTAG,"set db version to"+version+" "+(num==1?"succeed":"failed"));
		close();
	}*/
	
	public List<String> getEpisodes(){
		ArrayList<String> epList = new ArrayList<String>();
		Cursor c = myDataBase.rawQuery("SELECT DISTINCT episode FROM question", null);
		while(c.moveToNext()){
			epList.add(c.getString(0));
		}
		close();
		return epList;
	}
	
	public long insert(ContentValues row){
		return  myDataBase.insert("question", null, row);
		
	}
	
	public List<ContentValues> select(){
		List<ContentValues> ret = new ArrayList<ContentValues>();
		Cursor c = myDataBase.rawQuery("select question,answer,used, difficulty,category,anwsercheckerid,episode from question", null);
		while(c.moveToNext()){
			ContentValues content = new ContentValues();
			content.put("question", c.getString(0));
			content.put("answer", c.getString(1));
			content.put("used", c.getInt(2));
			content.put("difficulty", c.getInt(3));
			content.put("category", c.getInt(4));
			content.put("anwsercheckerid", c.getInt(5));
			content.put("episode", c.getString(6));

			ret.add(content);
		}
		return ret;
	}
	
	public SQLiteDatabase getSQLiteDatabase(){
		return this.myDataBase;
	}

	public List<Question> getQuestionSetOfEp(String episodeName) {
		List<Question> ret = new ArrayList<Question>();
		Cursor c = myDataBase.rawQuery("select _id,question,answer,anwsercheckerid FROM question where episode=?", new String[]{episodeName});
		while (c.moveToNext()) {
			// Log.d("QUESTION", "Question Found in DB: " + c.getString(1));
			Question q = new Question();
			q.setId(c.getInt(0));
			q.setQuestion(c.getString(1));
			Log.i(logTAG, "getQuestionSet question: "+c.getString(1)); 
			q.setAnswer(c.getString(2));
			Log.i(logTAG, "getQuestionSet anwser: "+c.getString(2)); 
			q.setCheckerId(c.getInt(3));
			Log.i(logTAG, "getQuestionSet checherid: "+c.getInt(3)); 
			ret.add(q);
		}
		return ret;
	}
}