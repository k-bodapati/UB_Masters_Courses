package edu.buffalo.cse.cse486586.groupmessenger1;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 * 
 * Please read:
 * 
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 * 
 * before you start to get yourself familiarized with ContentProvider.
 * 
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 * 
 * @author stevko
 *
 */

public class GroupMessengerProvider extends ContentProvider {

    static final Uri uri = Uri.parse("content://edu.buffalo.cse.cse486586.groupmessenger1.provider");
//    static final String filename = "GroupMessengerActivity";

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {



        FileOutputStream outputStream;

        String key = values.getAsString("key");
        String value = values.getAsString("value");

        // Reference - https://www.youtube.com/watch?v=EcfUkjlL9RI
        System.out.println("key and value are"+key + "\t" + value);

        try {
            outputStream = getContext().openFileOutput(key, Context.MODE_PRIVATE);
            System.out.println("insert key"+key);
            System.out.println("insert value" + value);
            outputStream.write(value.getBytes());
            outputStream.close();
        }

        catch (Exception e) {
            Log.e("tag", "File write failed");
            Log.v("insert", values.toString());
        }
        return uri;
    }


    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {


        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         *
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the formatting might still be incorrect.
         *
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         */

        // Reference - https://www.youtube.com/watch?v=EcfUkjlL9RI

        try {
            FileInputStream fis = getContext().openFileInput(selection);
            InputStreamReader inputStreamReader = new InputStreamReader(fis);
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader reader = new BufferedReader(inputStreamReader);

            try  {
                String line = reader.readLine();
                System.out.println("Line - "+line);
                stringBuilder.append(line);

            } catch (IOException e) {
                e.printStackTrace();
            }


            String contents = stringBuilder.toString();
            System.out.println("selection "+selection);


            // Reference - https://medium.com/@xabaras/creating-a-cursor-from-a-list-with-matrixcursor-ab71877ecf2c

            MatrixCursor matrixCursor = new MatrixCursor(new String[] {"key","value"});
            String[] row = {selection, contents};
            matrixCursor.addRow(row);

            return matrixCursor;

        }

        catch(Exception e) {
            e.printStackTrace();
        }
        Log.v("query", selection);
        return null;
    }
}
