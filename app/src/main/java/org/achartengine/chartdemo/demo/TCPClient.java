package org.achartengine.chartdemo.demo;

/**
 * Created by water.zhou on 11/4/2014.
 */
import android.util.Log;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient {
    private static final String TAG = "TCPClient:";
    private String serverMessage;
    public static String SERVERIP;
    public static int SERVERPORT;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
    private Socket socket;

    PrintWriter out;
    BufferedReader in;

    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPClient(OnMessageReceived listener, String IP , int port) {
        mMessageListener = listener;
        SERVERIP = IP;
        SERVERPORT = port;
        socket = null;
        out = null;
    }

    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */
    public void sendMessage(String message){
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
        }
    }

    public void stopClient(){
        mRun = false;
    }
    public boolean isRunning(){
        return mRun;
    }
    public void run() {

        mRun = true;

        try {
            Log.d(TAG, "Connecting..." + SERVERIP );
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVERIP);
            //create a socket to make the connection with the server
            socket = new Socket(serverAddr, SERVERPORT);
            try {
                //send the message to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                //receive the message which the server sends back
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Log.d(TAG, "In and out is created");
                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    serverMessage = in.readLine();
                    if (serverMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(serverMessage);
                    }
                    serverMessage = null;
                }

                Log.d(TAG, "S: Received Message: '" + serverMessage + "'");

            } catch (Exception e) {

                Log.e(TAG, "S: Error", e);

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                out.flush();
                out.close();
                in.close();
                socket.close();
            }

        } catch (Exception e) {

            Log.e(TAG, "C: Error", e);

        }

    }
    public void closeSocket()
    {
        if(socket != null) {
            try {
                socket.close();
            } catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}
