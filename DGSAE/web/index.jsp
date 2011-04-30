<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="javax.jdo.PersistenceManager" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.rtsz.DGS.DGSAE.*" %>

<html>
<head>
<link type="text/css" rel="stylesheet" href="/stylesheets/main.css" />
</head>

<body>

<%

UserService userService = UserServiceFactory.getUserService();
User user = userService.getCurrentUser();
if (user != null) {
%>
<p>Hello, <%= user.getNickname() %>! (You can
<a href="<%= userService.createLogoutURL(request.getRequestURI()) %>">sign out</a>.)</p>
<%
} else {
%>
<p>Hello!<a href="<%= userService.createLoginURL(request.getRequestURI()) %>">Sign in</a></p>
<%
}
%>

<%
//    PersistenceManager pm = PMF.get().getPersistenceManager();
//    String query = "select from " + Greeting.class.getName() + " order by date desc range 0,5";
//    List<Greeting> greetings = (List<Greeting>) pm.newQuery(query).execute();
//    if (greetings.isEmpty()) {
//	  }
//    pm.close();
%>

		<form enctype="multipart/form-data" action="/DGS" method="post">
			<div id="formHeader"></div>
			<div>SVG File: <input type="file" name="svgFile" /></div>
			<div>DGS Package File 1:<input type="file" name="dgsPackageFile" /></div>
			<div><input type="submit" name="cmd" value="processImage" /></div>
			<div id="formFooter"></div>
		</form>

	</body>
</html>
