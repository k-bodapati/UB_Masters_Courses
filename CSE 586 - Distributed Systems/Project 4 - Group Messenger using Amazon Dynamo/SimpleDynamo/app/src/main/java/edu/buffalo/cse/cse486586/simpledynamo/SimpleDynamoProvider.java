package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;



public class SimpleDynamoProvider extends ContentProvider {

	TreeMap<String, Integer> dynamo = new TreeMap<String, Integer>();
	Vector<Integer> portList = new Vector<Integer>();
	static final String TAG = SimpleDynamoActivity.class.getSimpleName();
	static final String pattern = new String(new char[100]).replace("\0", "-");
	Vector<String> filesList = new Vector<String>();
	Integer failedPort = 0;
	Boolean failedStatus = false;


	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Log.i(TAG, pattern);


		String hashKey = null;
		try {
			hashKey = genHash(selection);
		}

		catch (Exception e) {
			Log.i(TAG, "Failed to generate hash for the selection");
		}

		String minHash = dynamo.ceilingKey(hashKey);
		if (minHash==null) {
			minHash = dynamo.firstKey();
		}

		int correctAVD = dynamo.get(minHash);
		Vector<Integer> insertPorts = new Vector<Integer>();

		insertPorts.add(correctAVD);
		String nextAVD = dynamo.higherKey(minHash);
		if (nextAVD == null) {
			nextAVD = dynamo.firstKey();
		}
		int first = dynamo.get(nextAVD);

		nextAVD = dynamo.higherKey(nextAVD);
		if (nextAVD == null) {
			nextAVD = dynamo.firstKey();
		}

		int second = dynamo.get(nextAVD);

		insertPorts.add(first);
		insertPorts.add(second);

		for (int port : insertPorts) {
			try {
				Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), port * 2);
				DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
				DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

				dataOutputStream.writeUTF("Delete");
				String check = dataInputStream.readUTF();

				if (check.equals("Delete ok")) {
					dataOutputStream.writeUTF(selection);
					String message = dataInputStream.readUTF();
					if (message.equals("done")) {
						socket.close();
						dataInputStream.close();
						dataOutputStream.close();
					}
				}


			}

			catch (Exception e) {

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

		Log.i(TAG, pattern);

		String key = values.getAsString("key");
		String value = values.getAsString("value");
		String hashKey = null;

		try {
			hashKey = genHash(key);
		} catch (Exception e) {
			Log.e(TAG, "Cannot generate hash for key ");
			e.printStackTrace();
		}
		Log.i(TAG, "Looking where to insert this key " + key + " Hash of this key is " + hashKey);

		String minHash = dynamo.ceilingKey(hashKey);

		if (minHash == null) {
			Log.i(TAG, "Hash is greater than last available avd, So minHash will be first AVD");
			minHash = dynamo.firstKey();
		}

		Log.i(TAG, "minHash for this key " + key + " is " + minHash);
		Log.i(TAG, "So Need to Insert in " + dynamo.get(minHash) + " and next two ports ");

		int correctAVD = dynamo.get(minHash);
		Vector<Integer> insertPorts = new Vector<Integer>();

		insertPorts.add(correctAVD);
		// Adding correct AVD and Next two ports
		String nextAVD = dynamo.higherKey(minHash);
		if (nextAVD == null) {
			nextAVD = dynamo.firstKey();
		}
		int first = dynamo.get(nextAVD);

		nextAVD = dynamo.higherKey(nextAVD);
		if (nextAVD == null) {
			nextAVD = dynamo.firstKey();
		}

		int second = dynamo.get(nextAVD);

		insertPorts.add(first);
		insertPorts.add(second);



		Log.i(TAG, "Connecting to current AVD  and next two AVDs and Inserting the message");

		for (int port : insertPorts) {
			Log.i(TAG, "Connecting to port " + port);

			Log.i(TAG,pattern);
			try {
				Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), port * 2);
				DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
				DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

				dataOutputStream.writeUTF("Insert");
				String returnMsg = dataInputStream.readUTF();

				if (returnMsg.equals("Insert ok")) {
					Log.i(TAG, "Sending key, value to server for insertion");
					dataOutputStream.writeUTF(key + ":" + value);
				}


				String ack = dataInputStream.readUTF();
				if (ack.equals("done")) {
					Log.i(TAG, "Acknowledgement Recieved");
					Log.i(TAG, "closing the socket");
					dataInputStream.close();
					dataOutputStream.close();

					socket.close();
				}

			} catch (Exception e) {
				Log.e(TAG, "Failed to create socket to this port " + port * 2);
				failedPort = port;
				e.printStackTrace();

			}

		}


		return null;
	}

	@Override
	public boolean onCreate() {


		//Deleting all files in this AVD
		try {
			for (String file : getContext().fileList()) {
				getContext().deleteFile(file);
			}
			Log.i(TAG, "Some Files Deleted");
		}
		catch (NullPointerException e) {
			Log.e(TAG, "Nothing to Delete");
		}

		// Starting Server thread.
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


		portList.add(5554);
		portList.add(5556);
		portList.add(5558);
		portList.add(5560);
		portList.add(5562);

		for (Integer port : portList) {

			try {
				String portStr = Integer.toString(port);
				String hash = genHash(portStr);
				dynamo.put(hash, port);
				Log.i(TAG, "Hash for port " + port + " added into the Treemap");
			} catch (Exception e) {
				Log.e(TAG, "Cannot Generate Hash for AVD Port");
				e.printStackTrace();
			}
		}

		Log.i(TAG, "Treemap Completed, Data is " + dynamo.toString());

		new OnRespawn().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

//		This part for recovery



		return false;






	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
						String[] selectionArgs, String sortOrder) {

		if (selection.equals("@")) {
			Log.i(TAG, pattern);
			Log.i(TAG,"Query @ Block running ");
			MatrixCursor matrixCursor = new MatrixCursor(new String[]{"key", "value"});

			Log.i(TAG, "Reading All files present in this AVD");
			for (String key:filesList) {
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

		else if (selection.equals("*")) {
			Log.i(TAG, pattern);
			Log.i(TAG,"Query * Block running ");
			MatrixCursor matrixCursor = new MatrixCursor(new String[]{"key", "value"});
			HashSet<String> dataSet = new HashSet<String>();

			for (Integer port:portList) {
				try {
					Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), port*2);
					DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
					DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

					dataOutputStream.writeUTF("Query*");
					String check = dataInputStream.readUTF();

					if (check.equals("Query* ok")) {

						while (true) {
							String message = dataInputStream.readUTF();
							Log.e(TAG,"[][][][][][][][][][][][]][][][][]][][][][]" + message);

							if (message.equals("done")) {
								Log.i(TAG, "closing socket");
								socket.close();
								dataInputStream.close();
								dataOutputStream.close();
								break;
							}

							else {
								Log.i(TAG, "message is " + message);
								dataSet.add(message);
								Log.i(TAG, "Data set size is " + dataSet.size());
							}
						}
					}
				}

				catch (Exception e) {
					Log.e(TAG, "Failed to create socket");
					e.printStackTrace();
				}

			}

			Log.i(TAG, "Length of DataSet is " + dataSet.size());
			for (String data: dataSet ) {
				String items[] = data.split(":");
				String[] row = {items[0], items[1]};
				matrixCursor.addRow(row);
			}

			return matrixCursor;
		}

		else {

			Log.i(TAG, pattern);
			Log.i(TAG, "Query for a key is running " + selection );
			MatrixCursor matrixCursor = new MatrixCursor(new String[]{"key", "value"});

			try {
				String hashKey = genHash(selection);

				String minHash = dynamo.ceilingKey(hashKey);

				if (minHash == null) {
					Log.i(TAG, "Hash is greater than Largest AVD, So min hash is first AVD");
					minHash = dynamo.firstKey();
				}
//
//				int portToConnect = (dynamo.get(minHash)) * 2;
//				Log.i(TAG, "Connecting to this port " + portToConnect + " to get the file");


				int correctAVD = dynamo.get(minHash);
				Vector<Integer> insertPorts = new Vector<Integer>();

				insertPorts.add(correctAVD);
				String nextAVD = dynamo.higherKey(minHash);
				if (nextAVD == null) {
					nextAVD = dynamo.firstKey();
				}
				int first = dynamo.get(nextAVD);

				nextAVD = dynamo.higherKey(nextAVD);
				if (nextAVD == null) {
					nextAVD = dynamo.firstKey();
				}

				int second = dynamo.get(nextAVD);

				insertPorts.add(first);
				insertPorts.add(second);


				for (int port : insertPorts) {

					try {
						Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), port*2);
						Log.e(TAG,"###### Connecting to " + port*2);
						DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
						DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

						dataOutputStream.writeUTF("Query");
						String check = dataInputStream.readUTF();

						if (check.equals("Query ok")) {
							dataOutputStream.writeUTF(selection);

							String message = dataInputStream.readUTF();
							Log.e(TAG, "######################################" + message);

							if (message != null) {
								String[] data = message.split(":", 2);
								String[] row = {data[0], data[1]};
								matrixCursor.addRow(row);
								break;
							}

							else {
								Log.e(TAG,"_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-");
							}

						}
					} catch (Exception e) {
						Log.e(TAG, "Failed to create a socket at this port ");
						e.printStackTrace();
					}
				}

			}

			catch (Exception e) {
				Log.e(TAG, "Failed to create hash for this key");
				e.printStackTrace();
			}


			return matrixCursor;

		}

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
					  String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
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


	private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

		String TAG = ServerTask.class.getSimpleName();
		@Override
		protected Void doInBackground(ServerSocket... sockets) {
			Log.i(TAG, "Server Thread Running");

			DataOutputStream dout;
			DataInputStream din;

			try {
				ServerSocket serverSocket = sockets[0];
				Uri.Builder uriBuilder = new Uri.Builder();
				uriBuilder.authority("edu.buffalo.cse.cse486586.simpledynamo.provider");
				uriBuilder.scheme("content");
				Uri uri = uriBuilder.build();
				Log.i(TAG, "URI Done" + uri);


				while (true) {

					Socket socket_client = serverSocket.accept();
					Log.i("TAG", "Connection Accepted by the server");

					din = new DataInputStream(socket_client.getInputStream());
					dout = new DataOutputStream(socket_client.getOutputStream());

					String check = din.readUTF();

					if (check.equals("Insert")) {
						dout.writeUTF("Insert ok");

						String message = din.readUTF();
						Log.i(TAG, "Message Recieved is " + message);

						String data[] = message.split(":", 2);
						Log.i(TAG, "key recieved is " + data[0] + " value recieved is " + data[1]);


						//Insert Logic
						FileOutputStream outputStream;

						ContentValues contentValues = new ContentValues();
						contentValues.put("key", data[0]);
						contentValues.put("value", data[1]);

						try {
							outputStream = getContext().openFileOutput(data[0], Context.MODE_PRIVATE);
							outputStream.write(data[1].getBytes());
							filesList.add(data[0]);
							outputStream.close();
						} catch (Exception e) {
							Log.e(TAG, "Failed to create file and write into file");
							e.printStackTrace();
						}

						if (message != null) {
							dout.writeUTF("done");
							Log.i(TAG, "Acknowledgement Sent");
							dout.close();
							din.close();
						}


					}

					else if (check.equals("Query")) {
						dout.writeUTF("Query ok");

						String key = din.readUTF();

						try {
							FileInputStream fis = getContext().openFileInput(key);
							InputStreamReader inputStreamReader = new InputStreamReader(fis);
							StringBuilder stringBuilder = new StringBuilder();
							BufferedReader reader = new BufferedReader(inputStreamReader);

							try {
								String line = reader.readLine();
								System.out.println("Line - " + line);
								stringBuilder.append(line);

							} catch (IOException e) {
								e.printStackTrace();
							}

							String contents = stringBuilder.toString();
							dout.writeUTF(key + ":" + contents);


						} catch (Exception e) {
							Log.e(TAG, "Probably file not found");
							e.printStackTrace();
						}


					}


					else if (check.equals("Query*")) {
						dout.writeUTF("Query* ok");

						MatrixCursor matrixCursor = (MatrixCursor) query(uri,null,"@",null, null);
						Log.e (TAG, "TOTAL count in matrix cursor is " + matrixCursor.getCount());
						matrixCursor.moveToFirst();

						Log.e(TAG, "Started with first element");
						Boolean flag = true;

						try {
							while (flag) {
								String key = matrixCursor.getString(0);
								String value = matrixCursor.getString(1);

								dout.writeUTF(key + ":" + value);
								Log.e(TAG, "Written to query * " + key + " and " + value);
								flag = matrixCursor.moveToNext();
							}
						}

						catch (Exception e) {
							Log.e(TAG, "Probably 0 Messages in this AVD");
							e.printStackTrace();
						}

						dout.writeUTF("done");


					}

					else if (check.equals("Delete")) {
						dout.writeUTF("Delete ok");

						String key = din.readUTF();
						Boolean status = getContext().deleteFile(key);
						Log.i(TAG,"File Deleted " + status);
						dout.writeUTF("done");
					}
				}
			}

			catch (IOException e) {
				e.printStackTrace();
			}


			return null;
		}

	}

	private class OnRespawn extends AsyncTask<ServerSocket, String, Void> {

		@Override
		protected Void doInBackground(ServerSocket... serverSockets) {




			TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
			String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
			String AVDport = portStr;
			final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

			Log.e(TAG,"RECOVERY RECOVERY RECOVERY");

			Log.e(TAG,pattern);
			Log.e(TAG,pattern);

			Uri.Builder uriBuilder = new Uri.Builder();
			uriBuilder.authority("edu.buffalo.cse.cse486586.simpledynamo.provider");
			uriBuilder.scheme("content");
			Uri uri = uriBuilder.build();




			MatrixCursor matrixCursor = (MatrixCursor) query(uri,null, "*", null,null );
			Log.e(TAG, "Total items in Matrix Cursor are " + matrixCursor.getCount() );

			matrixCursor.moveToFirst();
			Log.e(TAG, "Started with first element");
			Boolean flag = true;
			HashSet<String> dataSet = new HashSet<String>();

			try {
				while (flag) {
					String key = matrixCursor.getString(0);
					String value = matrixCursor.getString(1);
					dataSet.add(key+ ":" + value);
					flag = matrixCursor.moveToNext();
				}
			}

			catch (Exception e) {
				Log.e(TAG, "Probably 0 Messages in this AVD");
				e.printStackTrace();
			}

			Log.e(TAG,"TOTAL Unique in Matrix cursor is " + dataSet.size());

			for (String item: dataSet ) {
				String data[] = item.split(":",2);
				String key = data[0];
				String value = data[1];

				try {
					String hashKey = genHash(key);
					String minHash = dynamo.ceilingKey(hashKey);
					if (minHash == null) {
						minHash = dynamo.firstKey();
					}
					int correctAVD = dynamo.get(minHash);

					Vector<Integer> insertPorts = new Vector<Integer>();

					insertPorts.add(correctAVD);
					String nextAVD = dynamo.higherKey(minHash);
					if (nextAVD == null) {
						nextAVD = dynamo.firstKey();
					}
					int first = dynamo.get(nextAVD);
					nextAVD = dynamo.higherKey(nextAVD);
					if (nextAVD == null) {
						nextAVD = dynamo.firstKey();
					}
					int second = dynamo.get(nextAVD);
					insertPorts.add(first);
					insertPorts.add(second);

					Log.e(TAG, pattern + " Insert ports are : " + insertPorts);
					Log.e(TAG, "And Failed Port is " + Integer.parseInt(AVDport) );
					FileOutputStream outputStream;

					if (insertPorts.contains(Integer.parseInt(AVDport))) {
						Log.e(TAG,"ADDING this key "+key + " to as Recovery part");
						try {
							outputStream = getContext().openFileOutput(data[0], Context.MODE_PRIVATE);
							outputStream.write(data[1].getBytes());
							filesList.add(data[0]);
							outputStream.close();
						}
						catch (Exception e) {
							Log.e(TAG, "Failed to create file and write into file");
							e.printStackTrace();
						}
					}

				}
				catch (Exception e ) {

					Log.e(TAG, "Failed to generate hash to key");
					e.printStackTrace();
				}

			}

			Log.e(TAG,pattern);
			Log.e(TAG,pattern);
			Log.e(TAG, "RECOVERY END");





			return null;
		}
	}


}






