package candy.castle.server;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@SuppressWarnings("serial")
public class CandyCastleServerServlet extends HttpServlet {
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
    	String user = req.getParameter("user");
        Key userKey = KeyFactory.createKey("Email", user);
        String userid = req.getParameter("userid");
        String lat = req.getParameter("latitude");
        String lng = req.getParameter("longitude");
        String alt = req.getParameter("altitude");
        String bsl = req.getParameter("bsl");
        String time = req.getParameter("time");
        String game = req.getParameter("game");
        Date date = new Date();

        Entity greeting = new Entity("Greeting", userKey);
        greeting.setProperty("userid", userid);
        System.out.println(greeting.getProperty("userid"));
        greeting.setProperty("latitude", lat);
        System.out.println(greeting.getProperty("latitude"));
        greeting.setProperty("longitude", lng);
        System.out.println(greeting.getProperty("longitude"));
        greeting.setProperty("altitude", alt);
        greeting.setProperty("bsl", bsl);
        greeting.setProperty("time", time);
        greeting.setProperty("game", game);
        greeting.setProperty("date", date);
        
        
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(greeting);
        
        //resp.sendRedirect("/candycastleserver.jsp?user=" + user);
    }
    
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
    	
    }
}
