package in.ac.iitb.aakash.rc_dualscreen;



import in.ac.iitb.aakash.rc_dualscreen.GrammarRecognizer.GrammarListener;
import in.ac.iitb.aakash.rc_dualscreen.GrammarRecognizer.GrammarMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;


/* CREDITS:
 * 1. Joystick code from http://code.google.com/p/mobile-anarchy-widgets/wiki/JoystickView; Code license - New BSD License(http://www.opensource.org/licenses/bsd-license.php)
 * 2. Joystick graphics from http://active.tutsplus.com/tutorials/mobile/creating-a-virtual-joystick-for-touch-devices/
 * 3. MJPEG code from http://stackoverflow.com/questions/3205191/android-and-mjpeg 
 * 
 * */

//main activity of the application
public class RoboActivity extends Activity implements GrammarListener {
	
	 Socket socket_send;
	 Button check;
	public static final int OPEN_SETTINGS_REQUEST = 1;
	private MjpegView streamView1;
	private MjpegView streamView2;
	private JoystickView joystick;
	private Boolean buzzerOn = false; // for detecting change in the state of buzzer
	private Boolean speakOn = false; // for detecting change in the state of buzzer
	private String prevStreamPath = ""; // stores the previous streaming path
	private String wifiModuleAddr;
	//private String prevWifiModuleAddr;
	private int wifiModulePort;
	//private int prevWifiModulePort;
	private String curStreamPath="";
	private String curStillStreamPath="";
	private String prevStillStreamPath = "";
	  NetworkTask newtask;
	private Activity ACTIVITY ;
	private PendingIntent RESTART_INTENT ;
	
	private DatagramSocket socket;
Boolean disconnect=false;
	private SharedPreferences settings;
	
	
	//Speech Recognition code
	  private static final String TAG = "OfflineSR";
	  private static final String EXTENSION = ".g2g";

	  private static String BASE_GRAMMAR = "base";
	  private static String COMMAND_GRAMMAR = "comm";
	  
	  private static final int TOAST = 4;
	  private static final int SPEAK_FALSE = 5;
	  


	  private String baseFile;
	  private String commandFile;

	  private GrammarRecognizer recognizer;
	  
	  private ToggleButton speak;
	 //Speech Recognition code

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	
		//for restarting the activity when settings are changed
		ACTIVITY = this;
	    RESTART_INTENT = PendingIntent.getActivity(this.getBaseContext(), 0, new Intent(getIntent()), getIntent().getFlags());

		// set the default values of settings
		PreferenceManager.setDefaultValues(this, R.xml.settings, false);
       // check=(Button)findViewById(R.id.connect_disconnect);
		// get the current settings
		settings = PreferenceManager.getDefaultSharedPreferences(this);

		curStreamPath = settings.getString("cameraStreamURL","<doesnt-exist>");
		curStillStreamPath=settings.getString("stillCameraURL","<doesnt-exist>" );
		wifiModuleAddr = settings.getString("wifiModuleIP", "<doesnt-exist>");
		wifiModulePort = Integer.parseInt(settings.getString("wifiModulePort", "<doesnt-exist>"));

		//Initialise streaming
		streamView1 = (MjpegView) findViewById(R.id.streamView1);
		streamView2 = (MjpegView) findViewById(R.id.streamView2);
		newtask = new NetworkTask();
		   newtask.execute();
		
	
		
		streamView1.setDisplayMode(MjpegView.SIZE_BEST_FIT);
		streamView1.showFps(false);
		
		streamView2.setDisplayMode(MjpegView.SIZE_BEST_FIT);
		streamView2.showFps(false);

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

		
		prevStreamPath = curStreamPath;
		prevStillStreamPath = curStillStreamPath;
		//prevWifiModulePort = wifiModulePort;
		//prevWifiModuleAddr = wifiModuleAddr;
		
		
		//Speech Recognition code
		int version = 0;

	    try {
	      version = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
	    } catch (NameNotFoundException e) {
	      Log.e(TAG, "Couldn't get version information, assuming 0");
	    }

	    baseFile = BASE_GRAMMAR + "." + version + EXTENSION;
	    commandFile = COMMAND_GRAMMAR + "." + version + EXTENSION;


	    if (!prepareRecognizer()) {
	      //Toast.makeText(this, "Speech Recognizer preparation failed", 3000).show();
	    	showToast("Speech Recognizer preparation failed");
	    	Log.e(TAG, "Speech Recognizer preparation failed");
	    }
	    
	   // speak = (ToggleButton) findViewById(R.id.speak);
	  //Speech Recognition code
	    
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
		sendData("B");
		
	}
	
	
	public void Plus(View v)
	{
		sendData("A");
	}
	
	
	public void Zero(View v)
	{
		sendData("E");	
	}
	
	
	public void Minus(View v)
	{
		sendData("C");
		
	}
	
	public void Down(View v)
	{
		sendData("D");
	}
	
	
	
	 public class NetworkTask extends AsyncTask<Void,String, Boolean> 
	 {
		  public int flag1,flag2;
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
		    			  streamView1.setSource(MjpegInputStream.read(curStreamPath));
		    			  
		    		  }
		    		  
		  		}
		    		  catch(Exception e)
		    		  {
		    			 flag1=1;
		    			
		    		  }
		    		  
		    	  try
		    	  {
		    		  if(!MjpegInputStream.read(curStillStreamPath).equals(null))
		    		  {
		    			  streamView2.setSource(MjpegInputStream.read(curStillStreamPath));
		    			  
		    		  }
		    		  
		    	  }
		    	  catch(Exception e)
		    		  {
		    			 flag2=2;
		    			
		    		  }
		    	  
		    	  
		       
		    	  if(flag1==1&&flag2==2)
		    	  {
		    		  publishProgress("Please Check Robo and Still Camera Connection");
		    		  
		    	  }
		    	  else
		    	  {
		    		  if(flag1==1)
		    		  {
		    			  publishProgress("Please Check Robo Camera Connection");
		    			  
		    		  }
		    		  if(flag2==2)
		    		  {
		    			  publishProgress("Please Check Still Camera Connection");
		    			  
		    		  }
		    	  }
	    	
	    	  
	    	  
	    	  try {
	  			
	  			socket_send = new Socket(); 
	  			 socket_send = new Socket(wifiModuleAddr, wifiModulePort);
	  			 publishProgress("");
	  			 Log.i("connect","socket connection establish");
	  		} catch (Exception socketException) {
	  			//Toast.makeText(getApplicationContext(),	"Socket Error\n" + socketException.toString()+ "\nCheck if connected to network.",Toast.LENGTH_LONG).show();
	  		//	Log.e(TAG, "Socket Error\n" + socketException.toString()+ "\nCheck if connected to network.");
	  			//showToast("Socket Error\n" + socketException.toString()+ "\nCheck if connected to network.");
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
				if (!prevStreamPath.equals(settings.getString("cameraStreamURL", "<doesnt-exist>"))||(!prevStillStreamPath.equals(settings.getString("stillCameraURL", "<doesnt-exist>")))) {
					Log.d("robo", "video setting changed");
					Log.d("robo", "cameraStreamURL = "+ settings.getString("cameraStreamURL", "<doesnt-exist>"));
					Log.d("robo", "stillCameraURL = "+ settings.getString("stillCameraURL", "<doesnt-exist>"));
					prevStreamPath = curStreamPath;
					prevStillStreamPath = curStillStreamPath;
					//restart the activity if camera stream url is changed
					Toast.makeText(RoboActivity.this, "Re-Configuring Application \n Please Wait...", Toast.LENGTH_LONG).show();
					AlarmManager mgr = (AlarmManager)ACTIVITY.getSystemService(Context.ALARM_SERVICE);
					mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 300, RESTART_INTENT);
					System.exit(2);
				}
			}
		}
	}
	
	
	
	
	//Speech Recognition code
	
	 @Override
	  public void onStop() {

	    recognizer.shutdown();
	    super.onStop();
	  }

	  private boolean prepareRecognizer() {
	    File baseGrammar = getFileStreamPath(baseFile);

	    if (!baseGrammar.exists()) {
	      deleteGrammarFiles();

	      if (true)
	        Log.i(TAG, "Extracting base grammar");

	      try {
	        baseGrammar.getParentFile().mkdirs();

	        InputStream in = getResources().openRawResource(R.raw.basegrammar);
	        FileOutputStream out = new FileOutputStream(baseGrammar);

	        byte[] buffer = new byte[1024];
	        int count = 0;

	        while ((count = in.read(buffer)) > 0) {
	          out.write(buffer, 0, count);
	        }
	      } catch (IOException e) {
	        return false;
	      }
	    }

	    File commandGrammar = getFileStreamPath(commandFile);
	    GrammarMap grammar = new GrammarMap();

	    grammar.addWord("@Knocks", "forward", null, 1, "V='forward'");
	    grammar.addWord("@Knocks", "backward", null, 1, "V='backwrd'");
	    grammar.addWord("@Knocks", "left", null, 1, "V='left'");
	    grammar.addWord("@Knocks", "right", null, 1, "V='right'");
	    grammar.addWord("@Knocks", "stop", null, 1, "V='stop'");

	    try {
	      recognizer = new GrammarRecognizer(this);
	      recognizer.setListener(this);

	      if (!commandGrammar.exists()) {
	        recognizer.compileGrammar(grammar, baseGrammar, commandGrammar);
	      }

	      recognizer.loadGrammar(commandGrammar);
	    } catch (IOException e) {
	      Log.e(TAG, e.toString());
	      return false;
	    }

	    return true;
	  }



	  public void startRecog(View v) {
	    
	    Log.i(TAG, "Speak pressed!");
	    //recognizer.recognize();
	    
	    if (!speakOn){
	    	recognizer.recognize();
			speakOn = true;
		} else {
			
			speakOn = false;
		}
	    
	    
	  }
	  
	  private void showToast(String message){
		  
		  handler.obtainMessage(TOAST, message).sendToTarget();
	  }
	  
	  private final Handler handler = new Handler() {
		    @Override
		    public void handleMessage(Message msg) {
		      /*switch (msg.what) {
		        case RECOGNITION_DONE:
		        	recogResult.setText(msg.obj.toString());
		        	speak.setEnabled(true);
		            speak.setText("Press and Speak");
		            Toast.makeText(RoboActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
		          break;
		          
		        case SOCKET_ERROR:
		        	Toast.makeText(RoboActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
			          break;
		        	
		      }*/
		    	switch (msg.what) {
		        case TOAST:
		        	Toast.makeText(RoboActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
		          break;
		        case SPEAK_FALSE:
		        	speak.setChecked(false);
		  	      speakOn = false;
		        	break;
		    	}
		    	
		    }
	  };


	  @Override
	  public void onRecognitionError(String reason) {
	    Log.e(TAG, "Speech Recog Error: " + reason);
	   // Toast.makeText(RoboActivity.this, "Speech Recog Error: " + reason, Toast.LENGTH_LONG).show();
	    showToast("Speech Recog Error: " + reason);
	    
	  }

	  @Override
	  public void onRecognitionFailure() {
	    
	    Log.e(TAG, "Speech Recog FAIL - onRecognitionFailure");
	   // Toast.makeText(RoboActivity.this, "Speech Recog FAIL - onRecognitionFailure", Toast.LENGTH_LONG).show();
	    showToast("Speech Recog FAIL - onRecognitionFailure");
	  }

	  @Override
	  public void onRecognitionSuccess(ArrayList<Bundle> results) {

	  
	      Log.i(TAG, "Received " + results.size() + " recognition results");

	    

	    for (Bundle result : results) {
	      String meaning = result.getString(GrammarRecognizer.KEY_MEANING);
	      String literal = result.getString(GrammarRecognizer.KEY_LITERAL);
	      String confidence = result.getString(GrammarRecognizer.KEY_CONFIDENCE);

	        Log.i(TAG, "Recognized meaning:" + meaning + ", literal:" + literal
	            + ", confidence:" + confidence);
	      
	       String recogRes = literal.substring(0, literal.length()-5);
	       String command = "5";
	       
	      //Log.i(TAG, "Recognition Result = " + recogRes);
	      //recogResult.setText(literal.substring(0, literal.length()-5));
	      
	      //SEND COMMAND TO ROBO
	      if(recogRes.equalsIgnoreCase("forward"))
	    	  command = "2";
	      else if(recogRes.equalsIgnoreCase("backward"))
	    	  command = "8";
	      else if(recogRes.equalsIgnoreCase("left"))
	    	  command = "4";
	      else if(recogRes.equalsIgnoreCase("right"))
	    	  command = "6";
	      else if(recogRes.equalsIgnoreCase("stop"))
	    	  command = "5";
	      
	      Log.i(TAG, "Recognition Result = " + recogRes + "         command = " + command);
	     
	      sendData(command);
	      showToast(recogRes);
	      
	      
	      
	      speakFalse();
	    }
	  }
	  
	  private void speakFalse(){
		  handler.sendEmptyMessage(SPEAK_FALSE);
		  
	  }

	  private void deleteGrammarFiles() {
	    FileFilter ff = new FileFilter() {
	      public boolean accept(File f) {
	        String name = f.getName();
	        return name.endsWith(EXTENSION);
	      }
	    };

	    File[] files = getFilesDir().listFiles(ff);
	    if (files != null) {
	      for (File file : files) {
	        if (true)
	          Log.d(TAG, "Deleted " + file);
	        file.delete();
	      }
	    }
	  }
	
	
	//Speech Recognition code

}