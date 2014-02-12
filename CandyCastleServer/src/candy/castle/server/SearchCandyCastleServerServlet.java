package candy.castle.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class SearchCandyCastleServerServlet extends HttpServlet {
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
    		throws IOException {
    	String user = req.getParameter("user");

    	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key userKey = KeyFactory.createKey("Email", user);
        Query query = new Query("Greeting", userKey).addSort("game", Query.SortDirection.DESCENDING);
        List<Entity> greetings = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        
        if (greetings.isEmpty()) {
           System.out.println(""+user+" has no information.");
        } else {
        	System.out.println("Informations about "+user+".");
        	
        	int lastmatch = Integer.parseInt((String)greetings.get(0).getProperty("game"));
        	
        	JSONArray array = new JSONArray();
        	
            for (int i=0;i<greetings.size();i++) {
            	JSONObject obj = new JSONObject();
				
            	try {
            		if (lastmatch==Integer.parseInt((String)greetings.get(i).getProperty("game"))) {
					   obj.put("userid", greetings.get(i).getProperty("userid"));
					   obj.put("latitude", greetings.get(i).getProperty("latitude"));
					   obj.put("longitude", greetings.get(i).getProperty("longitude"));
					   obj.put("altitude", greetings.get(i).getProperty("altitude"));
					   obj.put("bsl", greetings.get(i).getProperty("bsl"));
					   obj.put("time", greetings.get(i).getProperty("time"));
					   obj.put("game", greetings.get(i).getProperty("game"));
					   
					   array.put(obj);
            		}
				} catch (JSONException e) {
					e.printStackTrace();
				}
            }
            
            resp.setContentType("application/json");
			   
			PrintWriter out = resp.getWriter();
			  
			out.print(array.toString());
			System.out.println(array.toString());
			out.flush();
        }
    }
    
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
    	
    }
}