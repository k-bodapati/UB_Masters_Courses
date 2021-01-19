package edu.buffalo.cse.cse486586.simpledht;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.SimpleCursorAdapter;

import static edu.buffalo.cse.cse486586.simpledht.SimpleDhtActivity.port;


public class SimpleDhtProvider extends ContentProvider {

    static final String TAG = SimpleDhtActivity.class.getSimpleName();
    String portNumber = port;
    static String HashedPort;
    static Boolean chordFlag = true;
    Vector<String> filesList = new Vector();
    TreeMap<String, String> chord = new TreeMap<String, String>();
    Boolean insertFlag = false;
    String pattern = new String(new char[50]).replace("\0", "-");



    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        Vector<Integer> portsList = new Vector<Integer>();
        portsList.add(0, 11108);
        portsList.add(1, 11112);
        portsList.add(2, 11116);
        portsList.add(3, 11120);
        portsList.add(4, 11124);

        String hashedPort = null;

        DataInputStream dataInputStream;
        DataOutputStream dataOutputStream;

        if (chordFlag) {

            Log.i(TAG, pattern);
            for (int port : portsList) {

                Log.i (TAG, "Connecting all AVDs and getting information need to form Chord");
                try {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), port);
                    Log.i(TAG, "Trying to connect to port " + port);

                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());

                    dataOutputStream.writeUTF("info");
                    Log.i(TAG,"Written this 'info' to server");

                    String message = dataInputStream.readUTF();
                    Log.i(TAG, "Server responded with this message : " + message);


                    if (message.equals("done")) {
                        try {
                            hashedPort = genHash(Integer.toString(port / 2));
                        }
                        catch (Exception e) {
                            Log.e(TAG, "cannot create hash to the port " + port / 2);
                            e.printStackTrace();
                        }
                        chord.put(hashedPort, Integer.toString(port / 2));
                        Log.i(TAG, "Added this hash - " + hashedPort + " and port to chord - " + Integer.toString(port / 2));

                        socket.close();
                        dataInputStream.close();
                        dataOutputStream.close();
                        Log.i(TAG, "Connection Closed");

                    }
                }

                catch (Exception e) {
                    Log.e(TAG, "Cannot create socket in for chord info block ");
                    e.printStackTrace();
                }
            }

            Log.i(TAG, chord.toString());
            Log.i(TAG, "So the final Chord is " + chord.toString());
            chordFlag = false;
        }

        Log.i(TAG,pattern);
        Log.i(TAG, "Got a Key to delete");



        String hashKey = null;
        try {
            hashKey = genHash(selection);
            Log.i(TAG, "Hash of the Key " + selection + "is " + hashKey);
        }

        catch (Exception e) {
            Log.e(TAG, "Failed to generate hash to the key ");
            e.printStackTrace();
        }

        Log.i(TAG, "Looking to which AVD this key belongs to "+ hashKey + "::::::" + selection);
        String minHash = chord.ceilingKey(hashKey);

        if (minHash == null) {
            Log.i(TAG, "So the hash for this key is greater than the hash of Last Available AVD ");
            Log.i(TAG, "Last Available AVD Details : Hash of AVD is " + chord.lastKey() + " port " + chord.lastEntry());
            Log.i(TAG, "Checking first Available AVD to the key");
            minHash = chord.firstKey();
            Log.i(TAG, "Last Available AVD Details : Hash of AVD is " + chord.firstKey() + " port " + chord.firstEntry());
        }

        Log.i(TAG, "MIN hash found is " + minHash + "::::" + chord.get(minHash));
        String reqPort = chord.get(minHash);


        while (true) {
            try {
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(reqPort) * 2);
                Log.i(TAG, "Connected to the port - " + reqPort);

                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                dataOutputStream.writeUTF("delete");
                dataOutputStream.writeUTF(selection);

                Log.i(TAG, "Deleting this Key - " + selection + " in AVD with port " + reqPort);

                String ack = dataInputStream.readUTF();

                if (ack.equals("done")) {
                    Log.i(TAG, "Confirmation from server that file deleted in AVD with port " + reqPort);
                    dataInputStream.close();
                    dataOutputStream.close();
                    socket.close();

                }
                Log.i(TAG, "Connection with server is Closed");
                break;
            } catch (Exception e) {
                Log.e(TAG, "An error occured with Inserting into correct AVD");
                e.printStackTrace();

            }
        }


        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {

        Vector<Integer> portsList = new Vector<Integer>();
        portsList.add(0, 11108);
        portsList.add(1, 11112);
        portsList.add(2, 11116);
        portsList.add(3, 11120);
        portsList.add(4, 11124);

        String hashedPort = null;

        DataInputStream dataInputStream;
        DataOutputStream dataOutputStream;

        if (chordFlag) {

            Log.i(TAG, pattern);
            for (int port : portsList) {

                Log.i (TAG, "Connecting all AVDs and getting information need to form Chord");
                try {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), port);
                    Log.i(TAG, "Trying to connect to port " + port);

                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());

                    dataOutputStream.writeUTF("info");
                    Log.i(TAG,"Written this 'info' to server");

                    String message = dataInputStream.readUTF();
                    Log.i(TAG, "Server responded with this message : " + message);


                    if (message.equals("done")) {
                        try {
                            hashedPort = genHash(Integer.toString(port / 2));
                        }
                        catch (Exception e) {
                            Log.e(TAG, "cannot create hash to the port " + port / 2);
                            e.printStackTrace();
                        }
                        chord.put(hashedPort, Integer.toString(port / 2));
                        Log.i(TAG, "Added this hash - " + hashedPort + " and port to chord - " + Integer.toString(port / 2));

                        socket.close();
                        dataInputStream.close();
                        dataOutputStream.close();
                        Log.i(TAG, "Connection Closed");

                    }
                }

                catch (Exception e) {
                    Log.e(TAG, "Cannot create socket in for chord info block ");
                    e.printStackTrace();
                }
            }

            Log.i(TAG, chord.toString());
            Log.i(TAG, "So the final Chord is " + chord.toString());
            chordFlag = false;
        }

        Log.i(TAG,pattern);
        Log.i(TAG, "Got a Key a Value pair to the insert");
        String key = values.getAsString("key");
        String value = values.getAsString("value");
        Log.i(TAG, "Key is " + key + "Value is " + value);


        String hashKey = null;
        try {
             hashKey = genHash(key);
             Log.i(TAG, "Hash of the Key " + key + "is " + hashKey);
        }

        catch (Exception e) {
            Log.e(TAG, "Failed to generate hash to the key ");
            e.printStackTrace();
        }

        Log.i(TAG, "Looking where to insert this key "+ hashKey + "::::::" + key);
        String minHash = chord.ceilingKey(hashKey);

        if (minHash == null) {
            Log.i(TAG, "So the hash for this key is greater than the hash of Last Available AVD ");
            Log.i(TAG, "Last Available AVD Details : Hash of AVD is " + chord.lastKey() + " port " + chord.lastEntry());
            Log.i(TAG, "Assigning first Available AVD to the key");
            minHash = chord.firstKey();
            Log.i(TAG, "Last Available AVD Details : Hash of AVD is " + chord.firstKey() + " port " + chord.firstEntry());
        }

        Log.i(TAG, "MIN hash found is " + minHash + "::::" + chord.get(minHash));
        String reqPort = chord.get(minHash);


        while (true) {
            try {
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(reqPort) * 2);
                Log.i(TAG, "Connected to the port - " + reqPort);

                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                dataOutputStream.writeUTF(key + ":" + value);
                Log.i(TAG, "Storing this Key - " + key + " with Value " + value + " in AVD with port " + reqPort);

                String ack = dataInputStream.readUTF();
                if (ack.equals("done")) {
                    Log.i(TAG, "Confirmation from server that file inserted in AVD with port " + reqPort);
                    dataInputStream.close();
                    dataOutputStream.close();
                    socket.close();

                }
                Log.i(TAG, "Connection with server is Closed");
                break;
            }

            catch (Exception e) {
                Log.e(TAG, "An error occured with Inserting into correct AVD");
                e.printStackTrace();

            }
        }

        return uri;
    }

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }


    @Override
    public boolean onCreate() {

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(10000);
            Log.i(TAG, "Server Socket created at port 10000");
        }

        catch (IOException e) {
            Log.e(TAG, "Failed to create a server socket");
            e.printStackTrace();
        }

        Log.i(TAG, "Started a new Server thread form 'oncreate' function");
        new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
//            String portStr = Integer.toString(port/2);
//
//            try {
//                hashedPort = genHash(portStr);
//            }
//
//            catch (Exception e) {
//                Log.e(TAG, "Cannot Generate Hash");
//                e.printStackTrace();
//            }
//
//            chord.put(hashedPort, portStr);
//        }
//
//        Log.e(TAG, chord.toString());

        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        if (selection.equals(("@"))) {

            Log.i(TAG,pattern);
            Log.i(TAG,"Query @ Block is running");

            MatrixCursor matrixCursor = new MatrixCursor(new String[]{"key", "value"});

            for (String key : filesList) {
                Log.i(TAG, "Reading All files present in this AVD");

                try {
                    FileInputStream fis = getContext().openFileInput(key);
                    InputStreamReader inputStreamReader = new InputStreamReader(fis);
                    StringBuilder stringBuilder = new StringBuilder();
                    BufferedReader reader = new BufferedReader(inputStreamReader);

                    try {
                        String line = reader.readLine();
                        System.out.println("Line - " + line);
                        stringBuilder.append(line);

                    }

                    catch (IOException e) {
                        Log.e(TAG, "Failed to read file or File not found");
                        e.printStackTrace();
                    }

                    String contents = stringBuilder.toString();
                    String[] row = {key, contents};
                    matrixCursor.addRow(row);


                }

                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return matrixCursor;
        }

        else if (selection.equals(("*"))) {

            Log.i(TAG,pattern);
            Log.i(TAG,"Query * Block Running");

            DataOutputStream dataOutputStream;
            DataInputStream dataInputStream;

            MatrixCursor matrixCursor = new MatrixCursor(new String[]{"key", "value"});
            Vector<Integer> portList = new Vector<Integer>();
            portList.add(11108);
            portList.add(11112);
            portList.add(11116);
            portList.add(11120);
            portList.add(11124);

            Log.e(TAG, "Connecting every available AVD and collecting all Files from them");
            for (int port : portList) {

                try {
                    Log.i(TAG, "Connecting to this port in query * part" + port);
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), port);

                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());

                    dataOutputStream.writeUTF("*");
                    Log.i(TAG, "Written * to the server");


                    while (true) {

                        String message = dataInputStream.readUTF();
                        Log.i(TAG, "message recieved from server is (in query * part) " + message);

                        if (message.equals("done")) {
                            Log.i(TAG, "Acknowledgement recieved : " + message);
                            dataInputStream.close();
                            dataOutputStream.close();
                            socket.close();

                        }

                        else {
                            String[] data = message.split(":", 2);
                            String[] row = {data[0], data[1]};
                            Log.i(TAG, "Message Recieved from server is (in query * part) " + data[0] + " and " + data[1]);
                            matrixCursor.addRow(row);

                        }

                    }


                }

                catch (Exception e) {
                    Log.e(TAG, "Cant Create a socket in query *");
                    e.printStackTrace();
                }

            }
            Log.e(TAG, "TOTAL ROWS ===== " + matrixCursor.getCount());
            return matrixCursor;

        }

        else {
            Log.i(TAG,pattern);
            Log.i(TAG, "Need to see which AVD has requested File");
            DataOutputStream dataOutputStream;
            DataInputStream dataInputStream;

            MatrixCursor matrixCursor = new MatrixCursor(new String[]{"key", "value"});
            Vector<Integer> portList = new Vector<Integer>();
            portList.add(11108);
            portList.add(11112);
            portList.add(11116);
            portList.add(11120);
            portList.add(11124);

            for (int port : portList) {

                try {

                    Log.i(TAG, "Connecting to this port in query file part " + port);
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), port);

                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());

                    dataOutputStream.writeUTF("*");
                    Log.i(TAG, "Written * to the server query file part )");

                    while (true) {

                        String message = dataInputStream.readUTF();
                        Log.i(TAG, "message recieved from server is (in query file part) " + message);

                        if (message.equals("done")) {
                            Log.i(TAG, "Acknowledgement recieved : " + message);
                            dataInputStream.close();
                            dataOutputStream.close();
                            socket.close();

                        }

                        else {
                            String[] data = message.split(":", 2);;
                            Log.e(TAG, "Message Recieved from server is (in query file part) " + data[0] + " and " + data[1]);

                            if (data[0].equals(selection)) {
                                Log.i(TAG, "Found the correct file, Now adding it to matrix cursor and returning it");
                                String[] row = {data[0], data[1]};
                                matrixCursor.addRow(row);
                                return  matrixCursor;
                            }
                        }
                    }
                }

                catch (Exception e) {
                    Log.e(TAG, "Cant Create a socket in query * " + selection);
                    e.printStackTrace();
                }
            }
        }

        Log.i(TAG," No return happened so need ");
        return null;

    }

//            Log.e(TAG, "In other else part in query");
//
//            MatrixCursor mt;
//            mt = (MatrixCursor) this.query(uri, null, "*", nuluery *l, null);
//
//            Log.e(TAG, "MAT COUNT ==-----" + mt.getCount());
//            mt.moveToFirst();
//            String k = mt.getString(0);
//            String v = mt.getString(1);
//
//            while (mt.moveToNext()) {
//                if (k.equals(selection)) {
//                    break;
//                }
//
//                k = mt.getString(0);
//                v = mt.getString(1);
//
//                Log.e(TAG,"String found is "+ k + " and " + v );
//
//                if (k.equals(selection)) {
//                    break;
//                }
//                mt.moveToNext();
//            }
//
//            try {
//                FileInputStream fis = getContext().openFileInput(selection);
//                InputStreamReader inputStreamReader = new InputStreamReader(fis);
//                StringBuilder stringBuilder = new StringBuilder();
//                BufferedReader reader = new BufferedReader(inputStreamReader);
//
//                try {
//                    String line = reader.readLine();
//                    System.out.println("Line - " + line);
//                    stringBuilder.append(line);
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//
//                String contents = stringBuilder.toString();
//                System.out.println("selection " + selection);


        // Reference - https://medium.com/@xabaras/creating-a-cursor-from-a-list-with-matrixcursor-ab71877ecf2c
//                MatrixCursor matrixCursor = new MatrixCursor(new String[]{"key", "value"});
//                String[] row = {selection, v};
//                matrixCursor.addRow(row);
//
//
//                return matrixCursor;
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        Log.v("query", selection);
//        return null;

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            Log.i(TAG, "Server Thread Running");

            DataOutputStream dout;
            DataInputStream din;

            try {
                ServerSocket serverSocket = sockets[0];
                Uri.Builder uriBuilder = new Uri.Builder();
                uriBuilder.authority("edu.buffalo.cse.cse486586.simpledht.provider");
                uriBuilder.scheme("content");
                Uri uri = uriBuilder.build();
                Log.i(TAG, "URI Done" + uri);

                while (true) {

                    Socket socket_client1 = serverSocket.accept();
                    Log.i(TAG, "socket_client1 : Connection Accepted");

                    din = new DataInputStream(socket_client1.getInputStream());
                    dout = new DataOutputStream(socket_client1.getOutputStream());


                    String message = din.readUTF();

                    if (message.equals("*")) {
                        MatrixCursor matrixCursor;
                        matrixCursor = (MatrixCursor) query(uri,null,"@",null, null);

//                        String temp1 = matrixCursor.getString(matrixCursor.getColumnIndex("key"));
//                        String temp2 = matrixCursor.getString(matrixCursor.getColumnIndex("value"));

                        Log.e (TAG, "TOTAL count in matrix cursor is " + matrixCursor.getCount());
                        matrixCursor.moveToFirst();

                        Log.e(TAG, "Started with first element");

//                        Vector<String> msgList = new Vector<String>();
                        Boolean flag = true;
                        try {
                            while (flag) {


                                String temp1 = matrixCursor.getString(0);
                                String temp2 = matrixCursor.getString(1);

                                dout.writeUTF(temp1 + ":" + temp2);
                                Log.e(TAG, "Written to query * " + temp1 + " and " + temp2);
                                flag = matrixCursor.moveToNext();
//                        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter();

                            }
                        }

                        catch (Exception e) {
                            Log.e(TAG, "Probably 0 Messages in this AVD");
                            e.printStackTrace();
                        }

                        dout.writeUTF("done");
                        dout.close();
//                        socket_client1.close();

                    }

                    else if (message.equals("info")) {
                        dout.writeUTF("done");
                    }

                    else if (message.equals("delete")) {
                        Log.i(TAG,"Running Delete block in server thread");

                        String key = din.readUTF();
                        Log.i(TAG, "Need to delete this key");

                        Boolean status = getContext().deleteFile(key);
                        Log.i(TAG, "File Deleted here status " + status);

                        dout.writeUTF("done");

                        if (status) {
                            Log.i (TAG, "False occured");
                        }


                    }

                    else {
                        String data[] = message.split(":", 2);
                        Log.i(TAG, "Data Received to server. key =  " + data[0] + ", value is " + data[1]);


                        Log.i(TAG, "server need to add this (key, value) in this AVD" + "(" + data[0] + "," + data[1] + ")" + ":::" + portNumber);


                        // Building URI



                        ContentValues contentValues = new ContentValues();
                        contentValues.put("key", data[0]);
                        contentValues.put("value", data[1]);


                        filesList.add(data[0]);
                        FileOutputStream outputStream;

                        try {
                            Log.e(TAG, "Files Stored in this AVD " + port);
                            outputStream = getContext().openFileOutput(data[0], Context.MODE_PRIVATE);
                            outputStream.write(data[1].getBytes());
                            outputStream.close();
                        }

                        catch (Exception e) {
                            Log.e("tag", "File write failed");
                            Log.v("insert", data[1].toString());
                        }

                        if (message != null) {
                            dout.writeUTF("done");
                            Log.i(TAG, "Acknowledgement Sent");
                            dout.close();
                            din.close();

                        }

                    }
                }

            }

            catch (Exception e) {
                Log.e(TAG, "Exception occured");
                e.printStackTrace();
            }


            return null;
        }
    }

}


