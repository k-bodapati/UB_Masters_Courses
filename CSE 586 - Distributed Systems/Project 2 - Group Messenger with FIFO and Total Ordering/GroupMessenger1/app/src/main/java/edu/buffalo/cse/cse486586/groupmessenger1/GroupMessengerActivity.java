package edu.buffalo.cse.cse486586.groupmessenger1;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    int key = 0;
    private Uri mUri;

    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */


        mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger1.provider");

        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(10000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);


        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */

        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));


        final EditText editText = (EditText) findViewById(R.id.editText1);
        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String txt = editText.getText().toString();
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, txt, myPort);
                editText.setText("");

            }
        });


    }



    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {

            try {
                ServerSocket serverSocket = sockets[0];

                while (true) {

                    Socket socket_client1 = serverSocket.accept();
                    System.out.println("Connection Accepted");

                    /*
                     * TODO: Fill in your server code that receives messages and passes them
                     * to onProgressUpdate().
                     */

                    DataInputStream din = new DataInputStream(socket_client1.getInputStream());
                    String msg_recieved = din.readUTF();
                    System.out.println("msg recieved is " + msg_recieved);

                    publishProgress(msg_recieved);

                    if (msg_recieved != null) {
                        DataOutputStream dataOutputStream = new DataOutputStream(socket_client1.getOutputStream());
                        dataOutputStream.writeUTF("ack");
                        dataOutputStream.close();

                    }


                }

            } catch (Exception e) {
                System.out.println("Exception aaaaaaahhhhhh....." + e);
                e.printStackTrace();
            }


            return null;
        }


        protected void onProgressUpdate(String... strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            TextView textView = (TextView) findViewById(R.id.textView1);
            textView.append(strReceived + "\t\n");
            ContentValues values = new ContentValues();

            values.put("key",key);
            key = key + 1;
            values.put("value",strReceived);
            getContentResolver().insert(mUri,values);
            return;
        }

    }

    /***
     * ClientTask is an AsyncTask that should send a string over the network.
     * It is created by ClientTask.executeOnExecutor() call whenever OnKeyListener.onKey() detects
     * an enter key press event.
     *
     * @author stevko
     *
     */

    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {

                List<String> list = new ArrayList<String>();
                list.add("11108");
                list.add("11112");
                list.add("11116");
                list.add("11120");
                list.add("11124");

                for (String port : list) {

                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(port));

                    String msgToSend = msgs[0];

                    DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
                    dout.writeUTF(msgToSend);
                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                    String ackn = dataInputStream.readUTF();

                    if (ackn.equals("ack")) {
                        System.out.println("closing socket");
                                    socket.close();
                    }
                    dataInputStream.close();

                }

            }

            catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
                e.printStackTrace();
            }

            catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
                e.printStackTrace();
            }


            return null;
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

}
