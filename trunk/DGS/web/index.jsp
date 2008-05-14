<%-- 
    Document   : index
    Created on : May 7, 2008, 12:33:32 PM
    Author     : dwimsey
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <form method="post" action="AdminLogin">
            <table>
                <tr>
                    <td>Login:</td><td><input type="text" id="username"></td>                
                </tr>
                <tr>
                    <td>Password:</td><td><input type="password" id="password" value=""></td>
                </tr>
                <tr>
                    <td>&nbsp;</td><td><input type="submit" id="submit" value="Login">                
                </tr>
            </table>
        </form>
    </body>
</html>
