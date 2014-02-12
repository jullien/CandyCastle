package candy.castle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class UserEmail extends Activity{
	
	private LinearLayout ll;
	private RadioGroup rg;
	private RadioButton[] rb;
	private Button submit, change, start;
	private AccountManager manager;
	private Account[] accounts;
	private List<String> possibleEmails;
	private String userEmail, cookie, json, auth_token;
	private SharedPreferences mPrefs;
	private Intent intent;
	private EditText edittext, passtext;
	private TextView obs;
	private InputStream is;
	private List<NameValuePair> search = new ArrayList<NameValuePair>();
	private List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	private StringBuilder sb = new StringBuilder();
	private int emailId;
	private ProgressBar mProgress;
	
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.emails);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mPrefs = getPreferences(MODE_PRIVATE);
		userEmail = mPrefs.getString("userEmail", null);
		
		ll = (LinearLayout) findViewById(R.id.emails);
		edittext = (EditText) findViewById(R.id.emailtext);
		passtext = (EditText) findViewById(R.id.passtext);
		
		passtext.setText("");
		
		manager = AccountManager.get(this);
		accounts = manager.getAccounts();
		possibleEmails = new LinkedList<String>();
		   
		for (Account account : accounts) {
			possibleEmails.add(account.name);
		}
		    
		rg = new RadioGroup(this);
		rb = new RadioButton[possibleEmails.size()];
		rg.setOrientation(RadioGroup.VERTICAL);
		
		submit = new Button(this);
		submit.setText("Submit - Start Candy Castle");
		
		start = new Button(this);
		start.setText("Back to Candy Castle");
			
		change = new Button(this);
		change.setText("Change/Synchronize user");
		
		LayoutParams params = new LayoutParams(MarginLayoutParams.MATCH_PARENT, MarginLayoutParams.WRAP_CONTENT);
		obs = new TextView(this);
		obs.setLayoutParams(params);
		obs.setGravity(Gravity.CENTER);
		obs.setText("Use synchronize if you played in another device.\n" +
				    "Change/Synchronize user requires your password");
		obs.setTextAppearance(this,android.R.style.TextAppearance_Small);
		obs.setTextColor(Color.WHITE);
		
		mProgress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
		mProgress.setMax(100);
		mProgress.setLayoutParams(params);
		
		if (possibleEmails.size()>=1) {
		   for (int i=0;i<possibleEmails.size();i++) {
			   rb[i] = new RadioButton(this);
			   rg.addView(rb[i]);
			   rb[i].setId(i);
			   rb[i].setText(accounts[i].name);
		   }
				 
		   ll.addView(rg);
				 
		   if (userEmail==null) {
			  ll.addView(submit); 
					 
			  submit.setOnClickListener(new View.OnClickListener() {
				  @SuppressWarnings("deprecation")
				  public void onClick(View v) {
					  emailId = rg.getCheckedRadioButtonId();
					  
					  if (emailId>-1) {
						 userEmail = accounts[emailId].name;
						 manager.getAuthToken(accounts[emailId], "ah", false, new GetAuthTokenCallback(), null);
						 //passtext.setText(manager.getPassword(accounts[emailId]));
						 
						 edittext.setText(userEmail);
						 
						 ClearLayout();
						 
						 ll.addView(mProgress);
						 mProgress.setProgress(0);
						 
						 new LoginIntoGoogle().execute(new String[] {"http://10.0.2.2:8888/_ah/login"});
					  }
					  else if (!edittext.getText().toString().trim().isEmpty() && !passtext.getText().toString().isEmpty()) {
						  	  userEmail = edittext.getText().toString().trim();
						  	  
						  	  ClearLayout();
						  	  
						  	  ll.addView(mProgress);
						  	  mProgress.setProgress(0);
						  	
						  	  new LoginIntoGoogle().execute(new String[] {"http://10.0.2.2:8888/_ah/login"});
					  }
				  }
			  });
		   }
		   else {
			  edittext.setText(userEmail);
				
			  ll.addView(start);
			  ll.addView(change);
			  ll.addView(obs);
			  
			  change.setOnClickListener(new View.OnClickListener() {
				  @SuppressWarnings("deprecation")
				  public void onClick(View v) {
					  emailId = rg.getCheckedRadioButtonId();
					  
	   	    		  if (emailId>-1) {
	   	    			 userEmail = accounts[emailId].name;
	   	    			 manager.getAuthToken(accounts[emailId], "ah", false, new GetAuthTokenCallback(), null);
	   	    			 //passtext.setText(manager.getPassword(accounts[emailId]));
	   	    			 
	   	    			 edittext.setText(userEmail);
	   	    			
	   	    			 ClearLayout();
	   	    			 
	   	    			 ll.addView(mProgress);
	   	    			 mProgress.setProgress(0);
	  
						 new LoginIntoGoogle().execute(new String[] {"http://10.0.2.2:8888/_ah/login"});
	   	    		  }
	   	    		  else if (!edittext.getText().toString().trim().isEmpty() && !passtext.getText().toString().isEmpty()) {
	   	    			  	  userEmail = edittext.getText().toString().trim();
	   	    			  	  
	   	    			  	  ClearLayout();
	   	    			  	  
	   	    			  	  ll.addView(mProgress);
	   	    			  	  mProgress.setProgress(0);
							   
	   	    			  	  new LoginIntoGoogle().execute(new String[] {"http://10.0.2.2:8888/_ah/login"});
					  }
				  }
			   });
			    
			   start.setOnClickListener(new View.OnClickListener() {
				   public void onClick(View v) {
					   Boolean logged = mPrefs.getBoolean("logged", false);
					   String checkEmail = mPrefs.getString("checkEmail", null);
					   userEmail = mPrefs.getString("userEmail", null);
					   
					   if (logged == true && checkEmail.equals(userEmail)) {
						  ClearLayout();
						  
						  ll.addView(mProgress);
						  mProgress.setProgress(50);
						  
						  //json = mPrefs.getString("array", null);
						 
						  intent = new Intent(UserEmail.this, CandyCastle.class);
						  intent.putExtra("userEmail", userEmail);
						  intent.putExtra("token", "true");
						  intent.putExtra("checkedUser", "true");
						  //intent.putExtra("array", json);
						  
						  mProgress.setProgress(100);
						  ll.removeView(mProgress);
						  
						  startActivity(intent);
					   }
				   }
			   });
			}
		}
		else {
			if (userEmail==null) {
			   ll.addView(submit);
				  
			   submit.setOnClickListener(new View.OnClickListener() {
				   public void onClick(View v) {							
					   if (!edittext.getText().toString().trim().isEmpty() && !passtext.getText().toString().isEmpty()) {
						  userEmail = edittext.getText().toString().trim();
							 
						  ClearLayout();
						  
						  ll.addView(mProgress);
						  mProgress.setProgress(0);
						  
						  new LoginIntoGoogle().execute(new String[] {"http://10.0.2.2:8888/_ah/login"});
					   }
				   }
			   });
			}
			else {
				edittext.setText(userEmail);
					
				ll.addView(start);
				ll.addView(change);
				ll.addView(obs);
				  
				change.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if (!edittext.getText().toString().trim().isEmpty() && !passtext.getText().toString().isEmpty()) {
						   userEmail = edittext.getText().toString().trim();
						   
						   ClearLayout();
						   
						   ll.addView(mProgress);
						   mProgress.setProgress(0);
						   
						   new LoginIntoGoogle().execute(new String[] {"http://10.0.2.2:8888/_ah/login"});
						}
					}
				});
				    
				start.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						Boolean logged = mPrefs.getBoolean("logged", false);
						String checkEmail = mPrefs.getString("checkEmail", null);
						userEmail = mPrefs.getString("userEmail", null);
						
						if (logged == true && checkEmail.equals(userEmail)) {
						   ClearLayout();

						   ll.addView(mProgress);
						   mProgress.setProgress(50);
						   
						   //json = mPrefs.getString("array", null);
						   
						   intent = new Intent(UserEmail.this, CandyCastle.class);
						   intent.putExtra("userEmail", userEmail);
						   intent.putExtra("token", "true");
						   intent.putExtra("checkedUser", "true");
						   //intent.putExtra("array", json);
						   
						   mProgress.setProgress(100);
						   ll.removeView(mProgress);
						   
						   startActivity(intent);
						}
					}
				});
			}
		}
	}
	
	private void ClearLayout() {
		for (int i=0;i<possibleEmails.size();i++) { rg.removeView(rb[i]); }    
		ll.removeView(rg);
		ll.removeView(submit);
		ll.removeView(change);
		ll.removeView(start);
		ll.removeView(obs);
	}
	
	private class LoginIntoGoogle extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... url) {
			mProgress.setProgress(10);
			
	    	// Start HTTP connection and send the database to the web server
	    	DefaultHttpClient httpclient = new DefaultHttpClient();
	    	httpclient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
	    	HttpPost httppost = new HttpPost(url[0]);
	    	httppost.setHeader("Content-Type","application/x-www-form-urlencoded");
	    	
	    	mProgress.setProgress(20);
	    	
	    	try {
	    	    // Add your data
	    	    nameValuePairs.add(new BasicNameValuePair("email", userEmail));
	    	    nameValuePairs.add(new BasicNameValuePair("continue", "http://10.0.2.2:8888/candycastleserver.jsp"));
	    	    nameValuePairs.add(new BasicNameValuePair("auth", auth_token));
	    	    
	    	    // Execute HTTP Post Request
	    	    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	    	    HttpResponse response = httpclient.execute(httppost);
	    	    
	    	    mProgress.setProgress(30);
	    	    
	    	    if (response.getStatusLine().getStatusCode() < 400 && response.getStatusLine().getStatusCode() >= 200) {
	    	       cookie = response.getFirstHeader("Set-Cookie").getValue();
	    	       httpclient.getConnectionManager().shutdown();
	    	       
	    	       mProgress.setProgress(40);
	    	       
	    	       String delims = "[;]";
	    	       String[] tokens = cookie.split(delims);
	    	       
	    	       httpclient = new DefaultHttpClient();
				   httppost = new HttpPost("http://10.0.2.2:8888/search");
				   httppost.addHeader("Cookie",tokens[0]);
				   
				   mProgress.setProgress(50);
				   
				   try {
					   // Add your data
			    	   search.add(new BasicNameValuePair("user", userEmail));

			    	   httppost.setEntity(new UrlEncodedFormEntity(search));
					   response = httpclient.execute(httppost);
					   
					   mProgress.setProgress(60);
					   
					   if (response.getStatusLine().getStatusCode() < 400 && response.getStatusLine().getStatusCode() >= 200) {
						  HttpEntity httpEntity = response.getEntity();
						  is = httpEntity.getContent();  
						  httpclient.getConnectionManager().shutdown();
						  
						  mProgress.setProgress(70);
						  
						  try {
					          BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
					          String line = reader.readLine();
					          if (line != null) {
					             sb.append(line);
					          }
					          is.close();
					          json = sb.toString();
					          sb.delete(0, sb.length());
					          
					          mProgress.setProgress(80);
					          
					      } catch (Exception e) {
					    	  e.printStackTrace();
					      }
					       
					      return "Logged into your google account";
					   }
					   else {
			    	      httpclient.getConnectionManager().shutdown();
				    	  return "Problem to login into your google account";
					   }
				   } catch (ClientProtocolException e) {
					   e.printStackTrace();
					   return "Problem to login into your google account";
				   } catch (IOException e) {
					   e.printStackTrace();
					   return "Problem to login into your google account";
				   }
	    	    }
	    	    else {
	    	       httpclient.getConnectionManager().shutdown();
		    	   return "Problem to login into your google account";
	    	    }
	    	} catch (ClientProtocolException e) {
	    		e.printStackTrace();
	    		return "Problem to login into your google account";
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    		return "Problem to login into your google account";
	    	}
		}
		
		@Override
		protected void onPostExecute(String result) {
			nameValuePairs.clear();
			search.clear();
			
			mProgress.setProgress(90);
			
			if (result.equals("Logged into your google account")) {
			   Editor editor = mPrefs.edit();
			   editor.putString("userEmail", userEmail);
			   editor.putBoolean("logged", true);
			   editor.putString("checkEmail", userEmail);
			   editor.putString("array", json);
			   editor.commit();
			   
			   mProgress.setProgress(100);
			   
			   intent = new Intent(UserEmail.this, CandyCastle.class);
			   intent.putExtra("userEmail", userEmail);
			   intent.putExtra("token", "null");
			   intent.putExtra("checkedUser", "null");
			   intent.putExtra("array", json);
			   
			   ll.removeView(mProgress);
			   
			   ClearLayout();
			   
			   startActivity(intent);
			}
			else {
			   ll.removeView(mProgress);

			   ClearLayout();
			   
			   onResume();
			}
		}
	}
	
	private class GetAuthTokenCallback implements AccountManagerCallback<Bundle> {
        public void run(AccountManagerFuture<Bundle> result) {
                try {
                	Bundle bundle = result.getResult();
                    Intent intent = (Intent)bundle.get(AccountManager.KEY_INTENT);
                    if (intent != null) {
                       // User input required
                       startActivity(intent);
                    } 
                    else {
                       auth_token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                    }
                } catch (OperationCanceledException e) {
                    e.printStackTrace();
                } catch (AuthenticatorException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
	}
}
