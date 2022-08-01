/**
 * 
 */
package io.github.pollei.ticTacTom;

import jakarta.servlet.http.HttpServlet;
import jakarta.annotation.Resource;
import jakarta.annotation.Resource.AuthenticationType;
import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.annotation.sql.DataSourceDefinitions;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.sqlite.SQLiteDataSource;
import org.sqlite.JDBC;

import io.github.pollei.ticTacTom.XmlUtil;

/**
 * @author Steve_Pollei
 *
 */
// https://tomcat.apache.org/tomcat-10.0-doc/realm-howto.html
// https://tomcat.apache.org/tomcat-10.0-doc/jndi-resources-howto.html

@Resource(
    name="jdbc/tttUserDB",
    authenticationType = AuthenticationType.CONTAINER,
    type = javax.sql.DataSource.class,
    description = "login database"
    // , lookup = "jdbc/tttUserDB"
    )
@DataSourceDefinitions( {
  @DataSourceDefinition(
      className = "org.sqlite.SQLiteDataSource",
      name = "jdbc/tttUserDB", url = "jdbc:sqlite:tttUserDB.db",
      description = "database for ttt logins: users, passwords, roles"
      
      ) })
@WebServlet(
    name="Login",
    urlPatterns = {"/login","/logout", "/PlayerList", "/PlayerList/*" },
    description = "login for TicTacToe over http/https", loadOnStartup = 8)
public class LoginSrv extends HttpServlet {
  /**
   * 
   */
  private static final long serialVersionUID = -647118198759794968L;
  public DataSource userDB;
  
  @WebListener()
  public static class SrvCntListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
      // TODO Auto-generated method stub
      ServletContextListener.super.contextInitialized(sce);
      var srvCntx = sce.getServletContext();
      //if (false) // TODO
      try {
        var iCntx = new InitialContext();
        if (iCntx.lookup("java:/comp/env") instanceof Context envCntx) {
          if (true && envCntx.lookup("jdbc/tttUserDB") instanceof DataSource ds) {
            var conn = ds.getConnection();
            var meta = conn.getMetaData();
            var tables = meta.getTables(null,null,null,null);
            var stmt = conn.createStatement();
            // boolean results = stmt.execute(";");
            // if (results) {stmt.getResultSet();}
            // TODO make sure user/password db has correct tables
          }
        }
      } catch (NamingException | SQLException e) {
        e.printStackTrace();
      }
    }
    
  }

  /**
   * 
   */
  public LoginSrv() {
    // TODO Auto-generated constructor stub
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    // TODO Auto-generated method stub
    //super.doDelete(req, resp);
    var user=req.getRemoteUser();
    boolean isUser = user != null;
    boolean isAdmin = req.isUserInRole("tttAdmin");
    if (isUser && !isAdmin) {
      resp.sendError(HttpServletResponse.SC_FORBIDDEN);
      resp.flushBuffer();
      return;
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    // TODO Auto-generated method stub
    //super.doGet(req, resp);
    var srvPath = req.getServletPath();
    if (srvPath.equals("/PlayerList") && req.getParameter("whoami") !=null) { 
      doWhoAmI(req,resp); return;
    }
  }

  private void doWhoAmI(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    var user=req.getRemoteUser();
    var sess = req.getSession();
    if (user == null) {
      resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
      resp.flushBuffer();
      return;
    }
    outSelf(req,resp);
    resp.flushBuffer();
  }

  private void outSelf(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    var user=req.getRemoteUser();
    try {
      var doc = XmlUtil.newDoc();
      var topNod = doc.createElement("user");
      topNod.setAttribute("name", user);
      if (req.isUserInRole("tttAdmin")) {
        topNod.setAttribute("admin", "true");
      }
      doc.appendChild(topNod);
      resp.setContentType("application/xml;charset=UTF-8");
      XmlUtil.toWriter(doc, resp.getWriter());
    } catch (ParserConfigurationException | TransformerException e) {
      throw new ServletException("login fail", e);
    }
    resp.flushBuffer();
    
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    // TODO Auto-generated method stub
    //super.doPut(req, resp);
    var srvPath = req.getServletPath();
    if (srvPath.equals("/login")) { 
      doLogin(req,resp); return;
    }
    if (srvPath.equals("/logout")) { 
      doLogout(req,resp); return;
    }
  }

  private void doLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    // TODO Auto-generated method stub
    req.logout();
  }

  private void doLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    var authed = req.authenticate(resp);
    if (authed) {
      outSelf(req, resp);
    }
  }

}
