<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreServiceFactory" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreService" %>
<%@ page import="com.google.appengine.api.datastore.Query" %>
<%@ page import="com.google.appengine.api.datastore.Entity" %>
<%@ page import="com.google.appengine.api.datastore.FetchOptions" %>
<%@ page import="com.google.appengine.api.datastore.Key" %>
<%@ page import="com.google.appengine.api.datastore.KeyFactory" %>

<html>
  <head>
    
  </head>

  <body>
<%
    String user = request.getParameter("user");
    if (user == null) {
        user = "default";
    }
%>
<%
	    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	    Key userKey = KeyFactory.createKey("Email", user);
	    Query query = new Query("Greeting", userKey).addSort("game", Query.SortDirection.DESCENDING);
	    List<Entity> greetings = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());

	    if (greetings.isEmpty()) {
	        %>
	        <p><b><%= user %></b> has no information.</p><br>
	        <%
	    } else {
	        %>
	        <p>Informations about <b><%= user %></b>:</p>
	        <%
	        for (int i=0;i<greetings.size();i++) {
	            %>
	            <blockquote>
	            	Id: <%= greetings.get(i).getProperty("userid") %><br>
	            	Latidude: <%= greetings.get(i).getProperty("latitude") %><br>
	            	Longitude: <%= greetings.get(i).getProperty("longitude") %><br>
	            	Altitude: <%= greetings.get(i).getProperty("altitude") %><br>
	            	BSL: <%= greetings.get(i).getProperty("bsl") %><br>
	            	Time: <%= greetings.get(i).getProperty("time") %><br>
	            	Game: <%= greetings.get(i).getProperty("game") %>
	            </blockquote>
	            <%
	        }
	    }
%>
  </body>
</html>
