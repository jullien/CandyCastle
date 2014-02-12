package candy.castle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

/**
 * 
 * @author Matheus Jullien (mathjullien@gmail.com)
 *
 */
public class CandyCastle extends MapActivity {
	
	private MapController mapController;
	private MapView mapView;
	private MyLocationOverlay myLocationOverlay;
	private PositionDatasource database;
	private MyOverlays itemizedoverlay;
	private List<Overlay> mapOverlays;
	private Location local;
	private Button t, s, p, r; //t = test glucose, s = score, p = facebook share, r = new match
	private ImageButton f; //f = facebook login
	private LocationManager locationManager;
	private double latitude, longitude, altitude, bloodsugarlevel, distance, radius;
	private double x, y, diff, area;
	private EditText edittext;
	private View baloonbox;
	private GeoPoint point, castle;
	private Location origin, destiny, center;
	private Handler attackTimer, sendTimer, updateLocation, updateDatabase, syncDatabase, checkPaint, addCircleOverlay, addPolygonOverlay;
	private long attackTimerStart, time, duration, timeNow, timeFirstTower, attackStart, tmillis, attackOldTime, sendOldTime, sendTimerStart, sendStart, smillis;
	private int userId, game, totalScore, totalTowers;
	private int tseconds, tminutes, thours, signal, sseconds, sminutes, shours; //handler variables
	private int lat, lng;
	private List<GeoPoint> drawPoints;
	private List<Position> values, sends;
	private Paint paint;
	private LayoutInflater inflater;
	private PolygonOverlay polygonOverlay;
	private CircleOverlay circleOverlay;
	private String totalDuration, gameDuration, gameTowers, gameArea, gameScore;
	private String userEmail; // Will be the id for the user with the database
	private String json, checkedUser;
	private Facebook facebook;
	private SharedPreferences mPrefs;
	private Context context;
	private String access_token_test;
	private AsyncFacebookRunner mAsyncRunner;
	private JSONArray jArr;
	private JSONObject jObj;
	private Drawable drawable;
	private int i, j;
	private OverlayItem overlayitem;
	private List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	private Bundle params = new Bundle();
	
	/**
	 * Registers and sets all variables that are necessary to initialize this MapView,
	 * creates all handlers for backgrounds tasks,
	 * creates a variable to handle with facebook functions,
	 * creates and opens the database to register every new position over this MapView and
	 * creates the overlays to draw over this MapView.
	 * 
	 * @param bundle A mapping from String values to various Parcelable types.
	 * 
	 * @see android.app.Activity
	 * @see com.google.android.maps.MapView
	 */
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.mainmap); // bind the layout to the activity
		
		database = new PositionDatasource(this);
		database.open();
		
		mPrefs = getPreferences(MODE_PRIVATE);
		
		// Handler to create the attackTimer task
		attackTimer = new Handler();
		
		// Handler to create the updateLocation task
		updateLocation = new Handler();
		
		// Handler to create the updateDatabase task
		updateDatabase = new Handler();
		
		// Handler to create the syncDatabase task
		syncDatabase = new Handler();
		
		// Handler to create the checkPaint task
		checkPaint = new Handler();
		
		// Handler to create the addCircleOverlay task
		addCircleOverlay = new Handler();
		
		// Handler to create the addPolygonOverlay task
		addPolygonOverlay = new Handler();
		
		// Create a list of GeoPoint objects to be used to draw circle or triangle or polygon
	    drawPoints = new ArrayList<GeoPoint>();
	    
	    paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	    
	    // Create a Facebook variable to handle with facebook functions
	    facebook = new Facebook("265699973530671");
	    
	    context = getApplicationContext();
	    
	    polygonOverlay = new PolygonOverlay();
	    circleOverlay = new CircleOverlay();
	    
		t = (Button) findViewById(R.id.test);
		s = (Button) findViewById(R.id.score);
		p = (Button) findViewById(R.id.share);
		r = (Button) findViewById(R.id.restart);
		f = (ImageButton) findViewById(R.id.buttonFacebook);
		
		// Configure the Map
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.setSatellite(true);
				
		mapController = mapView.getController();
		mapController.setZoom(17); // Zoom 1 is world view
		
		myLocationOverlay = new MyLocationOverlay(this, mapView);
		mapView.getOverlays().add(myLocationOverlay);
	}

	/**
	 * Populates this MapView with all positions in this database.
	 * 
	 * Use {@link PositionDatabase.getAllPositions(String)} to get all positions from this database.
	 * If there's only one position: creates only the castle overlay.
	 * If there's more than one position: the first position will be the castle overlay and the others will be the walls,
	 * also sets variables and initialize {@link #UpdateAttackTimeTask} background timer task.
	 * Initializes {@link #UpdateLocationListener} background listener task to receive any new position from GPS.
	 */
	public void mainThread() {
		t.setVisibility(View.VISIBLE);	
		p.setVisibility(View.INVISIBLE);
		r.setVisibility(View.INVISIBLE);
		
		// No reason to show the score button if you don't have any score to show
		// With this we can avoid more complex functions when the user click on the button score
		s.setVisibility(View.INVISIBLE);
		
		mapOverlays = mapView.getOverlays();
		
		values = database.getAllPositions(userEmail);

		if (values.size()==1) {
		   drawable = this.getResources().getDrawable(R.drawable.castle);
		   itemizedoverlay = new MyOverlays(this, drawable);
		   
		   lat = (int) (values.get(0).getLat() * 1E6);
		   lng = (int) (values.get(0).getLng() * 1E6);
	       point = new GeoPoint(lat, lng);
	       castle = new GeoPoint(lat, lng);
		   overlayitem = new OverlayItem(point, "", "");
		   itemizedoverlay.addOverlay(overlayitem);
		   
		   mapView.getController().setCenter(castle);
		   mapOverlays.add(itemizedoverlay);
		   mapView.invalidate();
		}
		
		if (values.size()>1) {
		   drawable = this.getResources().getDrawable(R.drawable.castle);
		   itemizedoverlay = new MyOverlays(this, drawable);
			   
		   lat = (int) (values.get(0).getLat() * 1E6);
		   lng = (int) (values.get(0).getLng() * 1E6);
	       point = new GeoPoint(lat, lng);
	       castle = new GeoPoint(lat, lng);
		   overlayitem = new OverlayItem(point, "", "");
		   itemizedoverlay.addOverlay(overlayitem);
		   
		   mapView.getController().setCenter(castle);
		   mapOverlays.add(itemizedoverlay);
			
		   drawable = this.getResources().getDrawable(R.drawable.castletower);
		   itemizedoverlay = new MyOverlays(this, drawable);
		   
		   drawPoints.clear();
		   
		   for (i=1;i<values.size();i++) {
			   lat = (int) (values.get(i).getLat() * 1E6);
			   lng = (int) (values.get(i).getLng() * 1E6);
			   point = new GeoPoint(lat, lng);
			   drawPoints.add(point);
			   overlayitem = new OverlayItem(point, "", "");
			   itemizedoverlay.addOverlay(overlayitem);
		   }
			
		   mapOverlays.add(itemizedoverlay);
		   mapView.invalidate();
		   
		   s.setVisibility(View.VISIBLE);
		   s.setEnabled(true);
		   totalTowers = values.size()-1;
		   timeFirstTower = values.get(1).getTime();
		   timeNow = System.currentTimeMillis();
		   duration = timeNow - timeFirstTower;
		   signal = 0;
		   attackTimer.removeCallbacks(UpdateAttackTimeTask);
		   attackTimer.postDelayed(UpdateAttackTimeTask, 100);
		}
		
		updateLocation.removeCallbacks(UpdateLocationListener);
		updateLocation.post(UpdateLocationListener);
	}
	
	/**
	 * Gets every location change from this GPS provider and registers latitude, longitude and altitude informations
	 * 
	 * @see android.location.LocationListener
	 */
	private Runnable UpdateLocationListener = new Runnable() {
		   public void run() {
			   locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		        
		       LocationListener locationListener = new LocationListener() {
		           public void onLocationChanged(Location location) {
		        	   t.setEnabled(true);
		        	   local = location;
		        	   latitude = location.getLatitude();	
		        	   longitude = location.getLongitude();
		        	   altitude = location.getAltitude();
		        	   lat = (int) (latitude * 1E6);
		        	   lng = (int) (longitude * 1E6);
		        	   point = new GeoPoint (lat,lng);
		        	   mapView.getController().setCenter(point);
		           }

		           public void onStatusChanged(String provider, int status, Bundle extras) {}

		           public void onProviderEnabled(String provider) {
		        	   t.setEnabled(true);
		           }

		           public void onProviderDisabled(String provider) {
		        	   t.setEnabled(false);
		           }
		        };
		          
		        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		   }
	};
	
	/**
	 * Inflates a baloonbox {@link BaloonLayout} over this mapview using the user location and lets the user fill with his blood sugar level
	 * 
	 * @param v the mapview to add this baloonbox
	 */
	public void buttonClick(View v) {		
		local = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		if (local != null) {
		   latitude = local.getLatitude();
		   longitude = local.getLongitude();
		   
	  	   origin = new Location("");
		   origin.setLatitude(latitude);
		   origin.setLongitude(longitude);
	  			
	  	   lat = (int) (latitude * 1E6);
	  	   lng = (int) (longitude * 1E6);
	  	   point = new GeoPoint (lat,lng);
	  	   mapView.getController().setCenter(point);
	  			
	  	   mapView.removeView(baloonbox);
	  			
	  	   inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	  	   baloonbox = inflater.inflate(R.layout.baloon, null);
	  	   MapView.LayoutParams mapParams = new MapView.LayoutParams (
	  			   							 ViewGroup.LayoutParams.WRAP_CONTENT, 
	  			   							 ViewGroup.LayoutParams.WRAP_CONTENT,
	  			   							 point,
	  			   							 MapView.LayoutParams.BOTTOM_CENTER);
	       mapView.addView(baloonbox, mapParams);
		}
    }
	
	/**
	 * A button to register the blood sugar level informed by the user (inside the inflated baloonbox).
	 * If the text is not null, registers this user information and starts a background task to insert this information in database {@link #UpdateDatabaseTask}
	 * Removes this baloonbox view at the end
	 * 
	 * @param v the mapview with this baloonbox
	 */
	public void buttonOk(View v) {
		edittext = (EditText) findViewById(R.id.edittext);
		
		if (edittext.getText().toString().trim().isEmpty()) {	
			mapView.removeView(baloonbox);
		}
		else {
			bloodsugarlevel = Double.parseDouble(edittext.getText().toString());
			
			updateDatabase.removeCallbacks(UpdateDatabaseTask);
			updateDatabase.post(UpdateDatabaseTask);

			mapView.removeView(baloonbox);
		}
	}
	
	/**
	 * Tests the new user position.
	 * If this position have more than 5 meters distance from any other position, creates a new position in database.
	 * If this position have less than 5 meters distance from any other position, tests the blood sugar level informed by the user and replaces the higher value at this existing position.
	 *
	 * To create a new position will be necessary set some values to insert in database (get the current time, set user id and game id)
	 * With this new position a new map overlay will be created. 
	 * If this is the first position of the user game, a castle overlay will be created. 
	 * Else a wall overlay will be created, in this case the walls will be drawn {@link #drawAddOverlay()} and a backgroud task to control enemies attack will start {@link #UpdateAttackTimeTask}
	 */
	private Runnable UpdateDatabaseTask = new Runnable() {
		   public void run() {
			   int control = 0;
				
			   values = database.getAllPositions(userEmail);
			   time = System.currentTimeMillis();
			   
			   for (i=1;i<values.size();i++) {
				   destiny = new Location("values.get(i).getBsl()");
				   destiny.setLatitude(values.get(i).getLat());
				   destiny.setLongitude(values.get(i).getLng());

				   distance = origin.distanceTo(destiny);
				   if (distance<5 || (destiny.getLatitude()==origin.getLatitude() && destiny.getLongitude()==origin.getLongitude())) {
				      if (values.get(i).getBsl()<bloodsugarlevel) {
				    	 
						 database.updatePositionBSL(userEmail,values.get(0).getUserId()+i,bloodsugarlevel,time);
					  }
					  control = 1;
				   }
			   }
					 
			   if (control==0) {
				  time = System.currentTimeMillis();
				  userId = database.lastUserId(userEmail) + 1;
				  game = database.lastGameId(userEmail) + 1;
				  database.createPosition(userEmail,userId,latitude,longitude,altitude,bloodsugarlevel,1,time,game,1);
					
				  values = database.getAllPositions(userEmail);
					
				  mapOverlays = mapView.getOverlays();
						
				  drawable = context.getResources().getDrawable(R.drawable.castletower);
				  if (values.size()==1) { drawable = context.getResources().getDrawable(R.drawable.castle); }
				  if (values.size()==2) { timeFirstTower = values.get(1).getTime(); }
				  itemizedoverlay = new MyOverlays(context, drawable);
						
				  lat = (int) (latitude * 1E6);
				  lng = (int) (longitude * 1E6);
				  point = new GeoPoint(lat, lng);
				  if (values.size()>1) { drawPoints.add(point); }
				  overlayitem = new OverlayItem(point, "", "");
				  itemizedoverlay.addOverlay(overlayitem);
					
				  mapOverlays.add(itemizedoverlay);
				  mapView.invalidate();
			   }
			   else { values = database.getAllPositions(userEmail); }

			   if (values.size()>=2) {
				  s.setVisibility(View.VISIBLE);
				  s.setEnabled(true);
				  
				  paint.setAntiAlias(true);
				  paint.setColor(Color.GREEN);
				  paint.setStrokeWidth(4);
				  paint.setStyle(Paint.Style.STROKE);
				  paint.setAlpha(40);
				   
				  drawAddOverlay();
				   
				  totalTowers = values.size()-1;
				  timeNow = System.currentTimeMillis();
				  signal = 0;
				  attackTimer.removeCallbacks(UpdateAttackTimeTask);
			      attackTimer.postDelayed(UpdateAttackTimeTask, 100);	
			   }
		   }
	};
	
	public void buttonClose(View v) {
		mapView.removeView(baloonbox);
	}
    
	/**
	 * Displays game informations like duration, number of towers and score.
	 * 
	 * @param v the mapview to display the game score
	 */
	public void showScore(View v) {
		if (signal==0) { timeNow = System.currentTimeMillis(); }
        
		/*if (values.size()>2) {
		   PolygonFunctions function = new PolygonFunctions(values);
		   double area = function.polygonArea(values);
		   gameArea = new String("Area covered = "+area);
		}
		else {
		   Projection projection = mapView.getProjection();
		   float projectedRadius = projection.metersToEquatorPixels((float) radius);
		   double area = Math.PI * Math.pow(projectedRadius+(projectedRadius/4)+1, 2);
		   area = area/1.852;
		   gameArea = new String("Area covered = "+area);
		}*/
		
		gameTowers = new String("Total Tower = "+totalTowers);
		duration = Math.abs(timeNow - timeFirstTower);
		int seconds = (int) (duration / 1000);
		int minutes = seconds / 60;
		int hours = minutes / 60;
		seconds = seconds % 60;
		minutes = minutes % 60;
		String h;
		if (hours < 10) { h = new String ("0"+hours); } 
		else h = new String (""+hours);
		String m;
		if (minutes < 10) { m = new String ("0"+minutes); } 
		else m = new String (""+minutes);
		String s;
		if (seconds < 10) { s = new String ("0"+seconds); } 
		else s = new String (""+seconds);
		totalDuration = new String(""+h+":"+m+":"+s);
		gameDuration = new String("Game Duration = "+totalDuration);
		  
		totalScore = (totalTowers*(int)duration)/1000;
		gameScore = new String("Score = "+totalScore);
	    
	    inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	  	View layout = inflater.inflate(R.layout.score, null);
	  	
	  	TextView text = (TextView) layout.findViewById(R.id.duration);
	  	text.setText(gameDuration);
	  	text = (TextView) layout.findViewById(R.id.tower);
	  	text.setText(gameTowers);
	  	//text = (TextView) layout.findViewById(R.id.area);
	  	//text.setText(gameArea);
	  	text = (TextView) layout.findViewById(R.id.score);
	  	text.setText(gameScore);
	  	
	  	Toast toast = new Toast (getApplicationContext());
	  	toast.setGravity(Gravity.CENTER_VERTICAL, 0, 250);
	  	toast.setDuration(Toast.LENGTH_LONG);
	  	toast.setView(layout);
	  	toast.show();
	}
	
	/**
	 * Opens facebook interface to user login
	 * 
	 * See http://developers.facebook.com/docs/reference/android/3.0
	 * 
	 * @param v the mapview 
	 * 
	 * @see com.facebook.android.AsyncFacebookRunner
	 * @see com.facebook.android.Facebook
	 */
	public void buttonFacebook(View v) {
        // Only call authorize if the access_token has expired
        if (facebook.isSessionValid()) {
           mAsyncRunner = new AsyncFacebookRunner(facebook);
   		   mAsyncRunner.logout(this, new RequestListener() {
   			   public void onComplete(String response, Object state) {
   				  Editor editor = mPrefs.edit();
                  editor.putString("access_token", facebook.getAccessToken());
                  editor.putLong("access_expires", facebook.getAccessExpires());
                  editor.commit();
   			   }
   			   
   			   public void onIOException(IOException e, Object state) {}
   			   
   			   public void onFileNotFoundException(FileNotFoundException e,
   			         Object state) {}
   			   
   			   public void onMalformedURLException(MalformedURLException e,
   			         Object state) {}
   			   
   			   public void onFacebookError(FacebookError e, Object state) {}
   		   });
   		   f.setImageResource(R.drawable.login_button);
        }
        else {
           facebook.authorize(this, new String[] {}, new DialogListener() {
        	    public void onComplete(Bundle lvalues) {
                   Editor editor = mPrefs.edit();
                   editor.putString("access_token", facebook.getAccessToken());
                   editor.putLong("access_expires", facebook.getAccessExpires());
                   editor.commit();
                   access_token_test = facebook.getAccessToken();
                   f.setImageResource(R.drawable.logout_button);
                }
     
                public void onFacebookError(FacebookError error) {}
     
                public void onError(DialogError e) {}
     
                public void onCancel() {}
            });
        }
	}
	
	/**
	 * A button to share your score in facebook, use {@link com.facebook.android.Facebook.dialog(Context context, String action, Bundle parameters, DialogListener listener)}.
	 * 
	 * @param v the mapview
	 */
	public void buttonShare(View v) {
		showScore(v);
		params.putString("link", "");
		params.putString("picture", "https://photos-4.dropbox.com/psi/xl/zuizyQvzeFkvhBIOds6rRycbzQpGNFTfnGNf-6AR3Ws/3116814/1343235600/28177d2/castle.png");
		params.putString("name", "Candy Castle");
		params.putString("caption", "");
		params.putString("description", "I scored "+totalScore+" points in "+totalDuration+"!");
		params.putString("message", "");
		
		facebook.dialog(this, "feed", params, new DialogListener() {
            public void onComplete(Bundle pvalues) {
            	//abrir um toast avisando que o score foi postado
            }
 
            public void onFacebookError(FacebookError error) {}
 
            public void onError(DialogError e) {}
 
            public void onCancel() {}
        });
	}
	
	/**
	 * Clears this mapview to start a new game and stops all background tasks
	 * 
	 * @param v the mapview
	 */
	public void buttonRestart(View v) {
		database.updateActivedDatabase(userEmail,0);
		myLocationOverlay.disableMyLocation();
		attackTimer.removeCallbacks(UpdateAttackTimeTask);
		updateLocation.removeCallbacks(UpdateLocationListener);
		updateDatabase.removeCallbacks(UpdateDatabaseTask);
		syncDatabase.removeCallbacks(SyncDatabaseTask);
		checkPaint.removeCallbacks(CheckPaintTask);
		addCircleOverlay.removeCallbacks(AddCircleOverlayTask);
		addPolygonOverlay.removeCallbacks(AddPolygonOverlayTask);
		mapOverlays.clear();
		drawPoints.clear();
		values.clear();
   	  	myLocationOverlay.enableMyLocation();
		facebook.extendAccessTokenIfNeeded(this, null);
		mapView.removeAllViews();
   	  	mainThread();
	}
	
	/**
	 * Sends every positions not sent yet to web server database using an asynchronous background task.
	 * After sending successfully every positions, updates the database by checking these positions as already sent.
	 *
	 * @author Matheus Jullien (mathjullien@gmail.com)
	 * 
	 * @see android.os.AsyncTask
	 */
	private class SendDatasToWebServer extends AsyncTask<String, Void, Integer> {
		@Override
		protected Integer doInBackground(String... url) {
	    	// Start HTTP connection and send the database to the web server
	    	DefaultHttpClient httpclient = new DefaultHttpClient();
	    	HttpPost httppost = new HttpPost(url[0]);
	    	  
	    	try {
	    		System.out.println(sends.size());
	    		for (j=0;j<sends.size();j++) {
		    	    // Add your data		    	    
		    	    nameValuePairs.add(new BasicNameValuePair("user", ""+sends.get(j).getUser()));
		    	    System.out.println(sends.get(j).getUser());
		    	    nameValuePairs.add(new BasicNameValuePair("userid", ""+sends.get(j).getUserId()));
		    	    System.out.println(sends.get(j).getUserId());
		    	    nameValuePairs.add(new BasicNameValuePair("latitude", ""+sends.get(j).getLat()));
		    	    System.out.println(sends.get(j).getLat());
		    	    nameValuePairs.add(new BasicNameValuePair("longitude", ""+sends.get(j).getLng()));
		    	    System.out.println(sends.get(j).getLng());
		    	    nameValuePairs.add(new BasicNameValuePair("altitude", ""+sends.get(j).getAlt()));
		    	    nameValuePairs.add(new BasicNameValuePair("bsl", ""+sends.get(j).getBsl()));
		    	    nameValuePairs.add(new BasicNameValuePair("time", ""+sends.get(j).getTime()));
		    	    nameValuePairs.add(new BasicNameValuePair("game", ""+sends.get(j).getGame()));
		    	    
		    	    // Execute HTTP Post Request
		    	    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		    	    httpclient.execute(httppost);
		    	    nameValuePairs.clear();
	    		}
	    		return 1;	
	    	} catch (ClientProtocolException e) {
	    		e.printStackTrace();
	    		return 0;
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    		return 0;
	    	}
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			if (result == 1) {
			   nameValuePairs.clear();
			   database.updateSendedDatabase(userEmail,0);
	    	   //sendTimerStart = System.currentTimeMillis();
			}
		}
	}
	
	private Runnable UpdateAttackTimeTask = new Runnable() {
	   public void run() {
		   values = database.getAllPositions(userEmail);
		   
		   if (signal==0) { 
			  int last = values.size()-1; 
		      tmillis = System.currentTimeMillis() - values.get(last).getTime();
		   }
		   
	       tseconds = (int) (tmillis / 1000);
	       tminutes = tseconds / 60;
	       thours = tminutes / 60;
	       tminutes = tminutes % 60;
	       tseconds = tseconds % 60;
	       
	       if (tminutes==0) {//hours==0 && minutes<30
	    	  // change color to green
		      // set custom walls style
	    	   
	    	  paint.setAntiAlias(true);
			  paint.setColor(Color.GREEN);
			  paint.setStrokeWidth(4);
			  paint.setStyle(Paint.Style.STROKE);
			  paint.setAlpha(100);
			  
			  drawAddOverlay();
	       }
	       else {
		      if (tminutes==1) {//hours==0 && minutes>=30 && minutes<50
		    	 // change color to yellow
		    	 // set custom walls style
		    	   
		    	 paint.setAntiAlias(true);
		    	 paint.setColor(Color.YELLOW);
		    	 paint.setStrokeWidth(4);
		    	 paint.setStyle(Paint.Style.STROKE);
		    	 paint.setAlpha(100);
	 
		    	 drawAddOverlay();
		      }
		      else {
		    	 if (tminutes==2) {//hours==0 && minutes>=50 || hours==1 && minutes<10
		    	    // change color to red
		    		// set custom walls style
		    	 
			        paint.setAntiAlias(true);
			        paint.setColor(Color.RED);
			        paint.setStrokeWidth(4);
			        paint.setStyle(Paint.Style.STROKE);
			        paint.setAlpha(100);
			         
			        drawAddOverlay();
		    	 }
		    	 else {
		    	    if (tminutes>=3) {//hours==1 && minutes>=10 || hours>=2
		    	       // game over!
		    	       // change color to black
				       // set custom walls style
		    	    	 
				       paint.setAntiAlias(true);
				       paint.setColor(Color.BLACK);
				       paint.setStrokeWidth(4);
				       paint.setStyle(Paint.Style.STROKE);
				       paint.setAlpha(130);
				        
				       drawAddOverlay();
				        
					   if (signal==0) {
					   	  timeNow = System.currentTimeMillis();
					   	  mapView.getController().setCenter(castle);
					   	  
						  inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					   	  baloonbox = inflater.inflate(R.layout.gameover, null);
					   	  MapView.LayoutParams mapParams = new MapView.LayoutParams (
					   	  									   ViewGroup.LayoutParams.WRAP_CONTENT, 
					   			  			                   ViewGroup.LayoutParams.WRAP_CONTENT,
					   			  			                   castle,
					   			  			                   MapView.LayoutParams.BOTTOM_CENTER);
					   	  mapView.addView(baloonbox, mapParams);
					   	   
						  sends = database.getAllPositionsNotSended(userEmail);
					 	  if (sends.size()>0) {
					 	     new SendDatasToWebServer().execute(new String[] {"http://10.0.2.2:8888/server"});
					 	   	 new SendDatasToWebServer().cancel(true);
					 	  }
					   }
					   signal = 1;
				   	  	
			   	  	   if (facebook.isSessionValid()) { p.setVisibility(View.VISIBLE); }
			   	  		
			   	  	   r.setVisibility(View.VISIBLE);
			   	  	   t.setVisibility(View.INVISIBLE);
		    		}
		    	 }
		      }
	       }
	       
	       attackTimer.postDelayed(this, 1000);
	   }
	};
	
	// add polygon overlay to map 
	private Runnable AddPolygonOverlayTask = new Runnable() {
	   public void run() {
		   values = database.getAllPositions(userEmail);
		   System.out.println("pontos = "+values.size());
	       if (values.size()>0) {
		      populatePolygon(values);
			  
		      // apply polygon style & data and add to map
		      polygonOverlay.setData(drawPoints,paint);
		      mapOverlays.add(polygonOverlay);
		      mapView.postInvalidate();
	       }
	   }
    };
    
	// add circle overlay to map 
    private Runnable AddCircleOverlayTask = new Runnable() {
 	   public void run() {
 		  values = database.getAllPositions(userEmail);
	       if (values.size()>0) {
		      // set castle as default center
		      center = new Location("");
		      center.setLatitude(values.get(0).getLat());
			  center.setLongitude(values.get(0).getLng());
			  
			  radius = 0;
			  
			  // get the greatest distance between all existing points to the center to use as radius
		      for (i=1;i<values.size();i++) {
		    	  destiny = new Location("");
		    	  destiny.setLatitude(values.get(i).getLat());
		    	  destiny.setLongitude(values.get(i).getLng());
		
		    	  distance = center.distanceTo(destiny);
		    	  
		    	  if (distance > radius) { radius = distance; }
		      }
		      
		      populateCircle(values);
		      
		      // apply circle style and add to map
		      circleOverlay.setData(center,radius,paint);
		      mapOverlays.add(circleOverlay);
		      mapView.postInvalidate();
	       }
 	   }
    };
	
    void populateCircle (List<Position> values) {
    	// clear overlays and draw to add a new draw that don't overlap with the old one (re-add overlays)
        mapOverlays.clear();

    	drawable = this.getResources().getDrawable(R.drawable.castle);
    	itemizedoverlay = new MyOverlays(this, drawable);
    	  
    	lat = (int) (values.get(0).getLat() * 1E6);
  	    lng = (int) (values.get(0).getLng() * 1E6);
  	    point = new GeoPoint(lat, lng);
  	    castle = new GeoPoint(lat, lng);
  	    overlayitem = new OverlayItem(point, "", "");
  	    itemizedoverlay.addOverlay(overlayitem);
  	  
  	    mapOverlays.add(itemizedoverlay);
  	  
  	    drawable = this.getResources().getDrawable(R.drawable.castletower);
    	itemizedoverlay = new MyOverlays(this, drawable);
  	  
    	for (i=1;i<values.size();i++) {
    		lat = (int) (values.get(i).getLat() * 1E6);
    		lng = (int) (values.get(i).getLng() * 1E6);
    		point = new GeoPoint(lat, lng);
    		overlayitem = new OverlayItem(point, "", "");
    		itemizedoverlay.addOverlay(overlayitem);
    	}
    		
    	mapOverlays.add(itemizedoverlay);
    	mapView.postInvalidate();
    }

    void populatePolygon (List<Position> values) {    	
    	// clear overlays and draw to add a new draw that don't overlap with the old one (re-add overlays)
        mapOverlays.clear();
        
    	drawable = this.getResources().getDrawable(R.drawable.castle);
    	itemizedoverlay = new MyOverlays(this, drawable);

    	PolygonFunctions function = new PolygonFunctions(values);
		area = function.polygonArea(values);
		
		if (area>0) {
	  	  	x = function.certerGravityX(values,area);
	  	  	y = function.certerGravityY(values,area);
	  	  	
	  	  	values.get(0).setLat(x);
	  	  	values.get(0).setLng(y);
	  	
	  	  	lat = (int) (x * 1E6);
	  	  	lng = (int) (y * 1E6);
		}
		// when three or more points are in line the castle need to be over this line too
		else {
			if (values.get(1).getLat()==values.get(values.size()-1).getLat()) {
			   diff = (values.get(1).getLng() + values.get(values.size()-1).getLng())/2;
			   values.get(0).setLat(values.get(1).getLat());
		  	   values.get(0).setLng(diff);
			}
			else {
				if (values.get(1).getLng()==values.get(values.size()-1).getLng()) {
					diff = (values.get(1).getLat() + values.get(values.size()-1).getLat())/2;
					values.get(0).setLng(values.get(1).getLng());
					values.get(0).setLat(diff);
				}
				else {
					diff = (values.get(1).getLat() + values.get(values.size()-1).getLat())/2;
					values.get(0).setLat(diff);
					diff = (values.get(1).getLng() + values.get(values.size()-1).getLng())/2;
					values.get(0).setLng(diff);
				}
			}
					
			lat = (int) (values.get(0).getLat() * 1E6);
			lng = (int) (values.get(0).getLng() * 1E6);
		}
		
  	    point = new GeoPoint(lat, lng);
  	    castle = new GeoPoint(lat, lng);
  	    overlayitem = new OverlayItem(point, "", "");
  	    itemizedoverlay.addOverlay(overlayitem);
  	  
  	    mapOverlays.add(itemizedoverlay);
  	  
  	    drawable = this.getResources().getDrawable(R.drawable.castletower);
    	itemizedoverlay = new MyOverlays(this, drawable);
  	  
    	for (i=1;i<values.size();i++) {
    		lat = (int) (values.get(i).getLat() * 1E6);
    		lng = (int) (values.get(i).getLng() * 1E6);
    		point = new GeoPoint(lat, lng);
    		overlayitem = new OverlayItem(point, "", "");
    		itemizedoverlay.addOverlay(overlayitem);
    	}
    		
    	mapOverlays.add(itemizedoverlay);
    	mapView.postInvalidate();
    }
    
    void saveState() {
    	for (i=0;i<values.size();i++) {
    		database.updatePosition(values.get(i));
    	}
    }
    
	protected void drawAddOverlay() {
    	if (drawPoints.size()>0 && drawPoints.size()<=2) {
    	   addCircleOverlay.removeCallbacks(AddCircleOverlayTask);
    	   addCircleOverlay.post(AddCircleOverlayTask);
		}
		else {
		   addPolygonOverlay.removeCallbacks(AddPolygonOverlayTask);
		   addPolygonOverlay.post(AddPolygonOverlayTask);
		}
    }
    
    private Runnable SyncDatabaseTask = new Runnable() {
		   public void run() {
			   // get the JSON string with all informations about the user
			   json = getIntent().getExtras().getString("array");
			
			   if (!json.equals(null) && !json.isEmpty()) {
				  // try parse the string to a JSON object
				  try {
					  jArr = new JSONArray(json);
					  
					  if (jArr.length()>0) {
					     if ((System.currentTimeMillis()-Long.parseLong((String)jArr.getJSONObject(jArr.length()-1).get("time"))<=180000)) {
					    	CheckEndedGame();
					    	
					    	for (i=0;i<jArr.length();i++){
					    		jObj = null;
					    		jObj = jArr.getJSONObject(i);
					    	
					    		// check if exist one user with this userid (if the data exist in database)
					    		int c = database.checkUserId(userEmail,Integer.parseInt((String)jObj.get("userid")));
					    	
					    		// if there's no data about this user with this userid in database then create a new position in database
					    		// only create if this is a activated point 
					    		if (c==0) {
					    		   System.out.println("Adicionei User ID: "+Integer.parseInt((String)jObj.get("userid")));
					    		   database.createPosition(userEmail,Integer.parseInt((String)jObj.get("userid")),Double.parseDouble((String)jObj.get("latitude")),Double.parseDouble((String)jObj.get("longitude")),
						  			 				       Double.parseDouble((String)jObj.get("altitude")),Double.parseDouble((String)jObj.get("bsl")),1,Long.parseLong((String)jObj.get("time")),
						  			 				       Integer.parseInt((String)jObj.get("game")),0);
					    		}
					    	}
					   
					    	values = database.getAllPositions(userEmail);
					    	if (values.size()>1) {
						 
					    	   drawPoints.clear();
						   
					    	   for (i=1;i<values.size();i++) {
								   lat = (int) (values.get(i).getLat() * 1E6);
							 	   lng = (int) (values.get(i).getLng() * 1E6);
								   point = new GeoPoint(lat, lng);
								   drawPoints.add(point);
							   }
						 
					    	   checkPaint.removeCallbacks(CheckPaintTask);
					    	   checkPaint.post(CheckPaintTask);
						 	}
						 }
						 else {
							jObj = null;
					    	jObj = jArr.getJSONObject(jArr.length()-1);
					    	database.createPosition(userEmail,Integer.parseInt((String)jObj.get("userid")),Double.parseDouble((String)jObj.get("latitude")),Double.parseDouble((String)jObj.get("longitude")),
	  			 				       				Double.parseDouble((String)jObj.get("altitude")),Double.parseDouble((String)jObj.get("bsl")),0,Long.parseLong((String)jObj.get("time")),
	  			 				       				Integer.parseInt((String)jObj.get("game")),0);
						 }
					  }
				  } catch (JSONException e) {
					  e.printStackTrace();
				  }
			   }
		   }
    };
    
    private Runnable CheckPaintTask = new Runnable() {
		   public void run() {
			   values = database.getAllPositions(userEmail);
		       if (values.size()>1) {
		    	  long tempo = System.currentTimeMillis() - values.get(values.size()-1).getTime();
		    	  tempo = (int) ((tmillis / 1000) / 60) % 60;
		    	   
		    	  switch((int)tempo) {
		    	   	  case 0:
		    	   		  paint.setAntiAlias(true);
		    	   		  paint.setColor(Color.GREEN);
		    	   		  paint.setStrokeWidth(4);
		    	   		  paint.setStyle(Paint.Style.STROKE);
		    	   		  paint.setAlpha(40);
		    	   		  
		    	   		  drawAddOverlay();
		    	   		  mainThread();
		    		      break;   
		    	   	  case 1:
		    	   		  paint.setAntiAlias(true);
				          paint.setColor(Color.YELLOW);
				          paint.setStrokeWidth(4);
				          paint.setStyle(Paint.Style.STROKE);
				          paint.setAlpha(70);
				          
				          drawAddOverlay();
				          mainThread();
		    	   		  break;
		    	   	  case 2:
		    	   		  paint.setAntiAlias(true);
		    	   		  paint.setColor(Color.RED);
		    	   		  paint.setStrokeWidth(4);
		    	   		  paint.setStyle(Paint.Style.STROKE);
		    	   		  paint.setAlpha(100);
		    	   		  
		    	   		  drawAddOverlay();
		    	   		  mainThread();
		    	   		  break;
		    	   	  case 3:
		    	   		  paint.setAntiAlias(true);
		    	   		  paint.setColor(Color.BLACK);
		    	   		  paint.setStrokeWidth(4);
		    	   		  paint.setStyle(Paint.Style.STROKE);
		    	   		  paint.setAlpha(130);
		    	   		  
		    	   		  drawAddOverlay();
		    	   		  mainThread();
		    	   		  break;
		    	  } 
		       }
		   }
    };
    
    void CheckEndedGame() {
    	values = database.getAllPositions(userEmail);
    	
    	if (signal==1 || (System.currentTimeMillis()-values.get(values.size()-1).getTime())>180000) {
    	   System.out.println("Limpando");
    	   database.updateActivedDatabase(userEmail,0);
    	   values = database.getAllPositions(userEmail);
    	   attackTimer.removeCallbacks(UpdateAttackTimeTask);
    	   updateLocation.removeCallbacks(UpdateLocationListener);
    	   updateDatabase.removeCallbacks(UpdateDatabaseTask);
	   	   addCircleOverlay.removeCallbacks(AddCircleOverlayTask);
	   	   addPolygonOverlay.removeCallbacks(AddPolygonOverlayTask);
	   	   mapOverlays.clear();
	       drawPoints.clear();
    	   mapView.removeAllViews();
    	}
    }
    
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		myLocationOverlay.enableMyLocation();
		mapOverlays = mapView.getOverlays();
		
		userEmail = getIntent().getExtras().getString("userEmail");
		access_token_test = getIntent().getExtras().getString("token");
		checkedUser = getIntent().getExtras().getString("checkedUser");

        if (access_token_test.equals(null)) {
           // When you change the user, automatically close the actual session
           mAsyncRunner = new AsyncFacebookRunner(facebook);
    	   mAsyncRunner.logout(this, new RequestListener() {
    		   public void onComplete(String response, Object state) {
    			   Editor editor = mPrefs.edit();
                   editor.putString("access_token", facebook.getAccessToken());
                   editor.putLong("access_expires", facebook.getAccessExpires());
                   editor.apply();
    		   }
    			   
    		   public void onIOException(IOException e, Object state) {}
    			   
    		   public void onFileNotFoundException(FileNotFoundException e,
    		         Object state) {}
    			   
    		   public void onMalformedURLException(MalformedURLException e,
    		        Object state) {}
    			   
    		   public void onFacebookError(FacebookError e, Object state) {}
    	   });
    	   
        }
        else {
           // Get existing access_token if any (keeps session open with you login and didn't change the user)
           String access_token = mPrefs.getString("access_token", null);
           long expires = mPrefs.getLong("access_expires", 0);
    		
           if (access_token != null) {
              facebook.setAccessToken(access_token);
           }
           if (expires != 0) {
              facebook.setAccessExpires(expires);
           }
        }
        
		f.setBackgroundColor(Color.TRANSPARENT);
        f.setImageResource(facebook.isSessionValid() ? R.drawable.logout_button : R.drawable.login_button);
        
        if (checkedUser.equals(null)) {
	        syncDatabase.removeCallbacks(SyncDatabaseTask);
	        syncDatabase.post(SyncDatabaseTask);
        }
        
        mainThread();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		facebook.extendAccessTokenIfNeeded(this, null);
		
		saveState();
		
		myLocationOverlay.disableMyLocation();
		mapOverlays.clear();
		
		sends = database.getAllPositionsNotSended(userEmail);
 	   	if (sends.size()>0) {
 	   		new SendDatasToWebServer().execute(new String[] {"http://10.0.2.2:8888/server"});
 	   		new SendDatasToWebServer().cancel(true);
 	   	}
 	   	
 	    attackTimer.removeCallbacks(UpdateAttackTimeTask);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		myLocationOverlay.enableMyLocation();
	}

	@Override
	protected void onStop() {
		super.onStop();
		myLocationOverlay.disableMyLocation();
		mapOverlays.clear();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		facebook.authorizeCallback(requestCode, resultCode, data);
	}
}