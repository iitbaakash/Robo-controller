package in.ac.iitb.aakash.robo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;



import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


/* CREDITS:
 * 1. Joystick code from http://code.google.com/p/mobile-anarchy-widgets/wiki/JoystickView; Code license - New BSD License(http://www.opensource.org/licenses/bsd-license.php)
 * 2. Joystick graphics from http://active.tutsplus.com/tutorials/mobile/creating-a-virtual-joystick-for-touch-devices/
 * 3. MJPEG code from http://stackoverflow.com/questions/3205191/android-and-mjpeg 
 * 
 * */

//main activity of the application
public class RoboActivity extends Activity {
	
	 Socket socket_send;
	 Button check;
	public static final int OPEN_SETTINGS_REQUEST = 1;
	private MjpegView streamView;
	private JoystickView joystick;
	private Boolean buzzerOn = false; // for detecting change in the state of buzzer
	private String prevStreamPath = ""; // stores the previous streaming path
	private String wifiModuleAddr;
	//private String prevWifiModuleAddr;
	private int wifiModulePort;
	//private int prevWifiModulePort;
	private String curStreamPath="";
	  NetworkTask newtask;
	private Activity ACTIVITY ;
	private PendingIntent RESTART_INTENT ;
	
	private DatagramSocket socket;
Boolean disconnect=false;
	private SharedPreferences settings;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Log.i("msg1","hhhhhhhhhheeeeeeeeeeeeeeeeeeelllllllllllllllloooooooooooooooooo");
		//for restarting the activity when settings are changed
		ACTIVITY = this;
	    RESTART_INTENT = PendingIntent.getActivity(this.getBaseContext(), 0, new Intent(getIntent()), getIntent().getFlags());

		// set the default values of settings
		PreferenceManager.setDefaultValues(this, R.xml.settings, false);
       // check=(Button)findViewById(R.id.connect_disconnect);
		// get the current settings
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		Log.i("msg2","hhhhhhhhhhhhhhhiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
		curStreamPath = settings.getString("cameraStreamURL","<doesnt-exist>");
		wifiModuleAddr = settings.getString("wifiModuleIP", "<doesnt-exist>");
		wifiModulePort = Integer.parseInt(settings.getString("wifiModulePort", "<doesnt-exist>"));
Log.i("msg3","11111111111111111111111111111111111111111111111111111111111111");
		//Initialise streaming
		streamView = (MjpegView) findViewById(R.id.streamView);
		newtask = new NetworkTask();
		   newtask.execute();
		
	
		
		streamView.setDisplayMode(MjpegView.SIZE_BEST_FIT);
		streamView.showFps(false);
		
Log.i("msg5","aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		//Initialise joystick
		joystick = (JoystickView) findViewById(R.id.joystickView);
		joystick.setOnJostickMovedListener(jMovedListener);

		// try to initialise socket connection
/*		try {
			socket = new DatagramSocket();
		} catch (SocketException socketException) {
			Toast.makeText(getApplicationContext(),	"Socket Error\n" + socketException.toString()+ "\nCheck if connected to network.",Toast.LENGTH_LONG).show();
			// finish();
		}
*/
		Log.i("inside socket","enteringgggggggggg sockkkkkkkeeeeettttttttt");
		
		prevStreamPath = curStreamPath;
		//prevWifiModulePort = wifiModulePort;
		//prevWifiModuleAddr = wifiModuleAddr;

	}
	
	/*
	
	public void Connect_Disconnect(View v)
	{
	if(check.getText().equals("Connect"))
	{
		check.setText("Disconnect");
		
		
		
		disconnect=false;
		
	}
	
	else
	{
		check.setText("Connect");
		disconnect=true;
		
	
		
		
	}
		
	}
	
	
	*/
	
	public void Up(View v)
	{
		sendData("31");
		
	}
	
	
	public void Plus(View v)
	{
		sendData("30");
	}
	
	
	public void Zero(View v)
	{
		sendData("34");	
	}
	
	
	public void Minus(View v)
	{
		sendData("32");
		
	}
	
	public void Down(View v)
	{
		sendData("33");
	}
	
	
	
	 public class NetworkTask extends AsyncTask<Void,String, Boolean> 
	 {
		 int flag;
	      @Override
	      protected void onPreExecute()
	      {
	    	  
	    	  
	    	  
	      }

	      @Override
	      protected Boolean doInBackground(Void... params) 
	      
	      { 
	    	  
	    	  socket_send=null;
	    	  
	    	  try
		  		{
		    		  
		    		  if(!MjpegInputStream.read(curStreamPath).equals(null))
		    		  {
		    			  streamView.setSource(MjpegInputStream.read(curStreamPath));
		    			  
		    		  }
		    		  
		  		}
		    		  catch(Exception e)
		    		  {
		    			 flag=1;
		    			
		    		  }
	    	  
	    	  
	    	  if(flag==1)
    		  {
    			  publishProgress("Please Check Robo Camera Connection");
    			  
    		  }
	    	  
	    	  /*
	    	  try
	  		{
	  			streamView.setSource(MjpegInputStream.read(curStreamPath));
	  			
	  		}
	  		catch(Exception e)
	  		{
	  			
	  			
	  		}
	    	*/
	    	  
	    	  
	    	  try {
	  			
	  			socket_send = new Socket(); 
	  			 socket_send = new Socket(wifiModuleAddr, wifiModulePort);
	  			 publishProgress("");
	  			 Log.i("connect","socket connection establish");
	  		} catch (Exception socketException) 
	  		{
	  			//Toast.makeText(getApplicationContext(),	"Socket Error\n" + socketException.toString()+ "\nCheck if connected to network.",Toast.LENGTH_LONG).show();
	  			// finish();
	  		}
		    
	    	 // publishProgress("");
		   return true;   
	      }

	      
	      
	      protected void onProgressUpdate(String... msg)
			{
	    		
	    	  Toast.makeText(getApplicationContext(), msg[0], Toast.LENGTH_LONG).show();	
		    	
			}
	     
	      
	      @Override
	        protected void onCancelled() {
	          
	        }

	      @Override
	      protected void onPostExecute(Boolean result) {
	    	  
	    	  /*
	    	  
	    	 if (socket_send != null)
				{
					try {
						socket_send.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
	    	 
	    	 
	    	 */
	    	  
	    	 
	  }

	      
	    
	 }
	
	
	
	
	
	
	
	
	

	// listener for joystick movement
	private JoystickMovedListener jMovedListener = new JoystickMovedListener() {

		@Override
		public void OnMoved(int x, int y) {
			double deg = -1; //initialise with invalid value (angle in 0-360 deg)
			String command = "";

			//get angle according to co-ordinates
			if (x == 0 && y == 0)
				command = "5"; 		// stop if joystick is in center
			else if (x == 0 && y > 0)
				deg = 90;
			else if (x == 0 && y < 0)
				deg = 270;
			else if (x > 0 && y == 0)
				deg = 0;
			else if (x < 0 && y == 0)
				deg = 180;
			else {
				deg = Math.atan(Math.abs((double) y / x)) / (Math.PI / 180); //tan inverse to get angle
				
				//Make appropriate addition/subtraction to get angle within 0-360 degrees
				if (x > 0 && y > 0) 	 // 1st quadrant
					; 			// no change
				else if (x < 0 && y > 0) // 2nd quadrant
					deg = 180 - deg;
				else if (x < 0 && y < 0) // 3rd quadrant
					deg = 180 + deg;
				else					 // 4th quadrant
					deg = 360 - deg;
			}

			Log.d("robo", "x = " + x + " y = " + y + " deg = " + deg);

			//assign appropriate command according to angle(position) of the joystick
			if (x == 0 && y == 0) 
				command = "5"; // stop
			else if (deg >= 67.5 && deg < 112.5)
				command = "2"; // forward
			else if (deg >= 112.5 && deg < 157.5)
				command = "1"; // soft left
			else if (deg >= 157.5 && deg < 202.5)
				command = "4"; // left
			else if (deg >= 202.5 && deg < 247.5)
				command = "7"; // soft backward left
			else if (deg >= 247.5 && deg < 292.5)
				command = "8"; // backward
			else if (deg >= 292.5 && deg < 337.5)
				command = "9"; // soft backward right
			else if (deg >= 337.5 || deg < 22.5)
				command = "6"; // right
			else if (deg >= 22.5 && deg < 67.5)
				command = "3"; // soft right
			
			// call sendData to send the command
			sendData(command);
		}

		@Override
		public void OnReleased() {
		}

		@Override
		public void OnReturnedToCenter() {
			sendData("5"); //5=stop when joystick comes back to center
		};
	};
	
	
	
	
	
	
	
	// function for sending a string data through socket
	public void sendData(String send) {
		wifiModuleAddr = settings.getString("wifiModuleIP", "<doesnt-exist>");
		wifiModulePort = Integer.parseInt(settings.getString("wifiModulePort", "<doesnt-exist>"));

		Log.d("robo", "wifiModuleAddr = " + wifiModuleAddr + " wifiPort = "+wifiModulePort);
/*
		try {
			// create a packet with the data to be sent
			DatagramPacket sendP = new DatagramPacket(send.getBytes(), send.getBytes().length, InetAddress.getByName(wifiModuleAddr), wifiModulePort);

			// send the packet
			socket.send(sendP);
		} catch (IOException exception) {
			Toast.makeText(getApplicationContext(),	"Server Error\n" + exception.toString(), Toast.LENGTH_LONG).show();
			// finish();
		}
		
		*/
		try
		{
			PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket_send.getOutputStream())),true);
	        out.println(send.toString());	
		}
		catch(Exception e)
		{
			
		}
		
		
	}

	// open settings intent when settings button is clicked
	public void openSettings(View v) {
		Intent i = new Intent(RoboActivity.this, SettingsActivity.class);
		startActivityForResult(i, OPEN_SETTINGS_REQUEST);
	}
	
	
	// start buzzer when toggle  button is clicked
	public void buzzerToggle(View v) {
		if (!buzzerOn){
			sendData("b");
			buzzerOn = true;
		} else {
			sendData("o");
			buzzerOn = false;
		}
	}

	
	//When the settings dialog box is closed, make appropriate changes for the changed settings(if any) to take effect
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == OPEN_SETTINGS_REQUEST) {
			if (resultCode == RESULT_OK) {
				//Log.d("robo", "result ok");
			} else if (resultCode == RESULT_CANCELED) { // ie when the settings dialog is dismissed
				//Log.d("robo", "result cancelled");
				
				//store the changed settings
				curStreamPath = settings.getString("cameraStreamURL","<doesnt-exist>");
				wifiModuleAddr = settings.getString("wifiModuleIP", "<doesnt-exist>");
				wifiModulePort = Integer.parseInt(settings.getString("wifiModulePort", "<doesnt-exist>"));
				
				//check if camera stream url is changed
				if (!prevStreamPath.equals(settings.getString("cameraStreamURL", "<doesnt-exist>"))) {
					Log.d("robo", "video setting changed");
					Log.d("robo", "cameraStreamURL = "+ settings.getString("cameraStreamURL", "<doesnt-exist>"));

					prevStreamPath = curStreamPath;
					
					//restart the activity if camera stream url is changed
					Toast.makeText(RoboActivity.this, "Re-Configuring Application \n Please Wait...", Toast.LENGTH_LONG).show();
					AlarmManager mgr = (AlarmManager)ACTIVITY.getSystemService(Context.ALARM_SERVICE);
					mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 300, RESTART_INTENT);
					System.exit(2);
				}
			}
		}
	}

}