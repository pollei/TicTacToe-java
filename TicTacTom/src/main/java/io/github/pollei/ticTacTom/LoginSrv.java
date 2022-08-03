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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.regex.Pattern;

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
  static final Pattern userIdPat = Pattern.compile("^/([a-z][a-z0-9]{0,14})$");
  static final String[] createSqlTables = {
      """
      create table if not exists users (
        user_name         varchar(32) not null primary key,
        user_pass         varchar(128) not null );
      """,
      """
      create table if not exists user_roles (
        user_name         varchar(32) not null references users(user_name),
        role_name         varchar(32) not null references roles(role_name),
        primary key (user_name, role_name) );
      """,
      """
      create table if not exists roles (
        role_name         varchar(32) not null primary key,
        role_description  varchar(128) );
      """,
      """
      create table if not exists groups (
        group_name        varchar(32) not null primary key,
        group_description varchar(128) );
      """,
      """
      create table if not exists user_groups (
        user_name         varchar(32) references users(user_name),
        group_name        varchar(32) references groups(group_name),
        primary key (user_name, group_name) );
      """,
      """
      create table if not exists group_roles (
        group_name        varchar(32) references groups(group_name),
        role_name         varchar(32) references roles(role_name),
        primary key (group_name, role_name) );
      """ };
  // https://tomcat.apache.org/tomcat-10.0-doc/realm-howto.html
  // https://tomcat.apache.org/tomcat-10.0-doc/jndi-resources-howto.html
  // https://docs.oracle.com/javase/tutorial/jdbc/basics/index.html
  // https://www.sqlite.org/lang_createtable.html
  
  public static Connection getUserDbConnection()
      throws NamingException, SQLException {
    var iCntx = new InitialContext();
    if (iCntx.lookup("java:/comp/env") instanceof Context envCntx) {
      if (envCntx.lookup("jdbc/tttUserDB") instanceof DataSource ds) {
        return ds.getConnection();
      }
    }
    return null;
  }
  @WebListener()
  public static class SrvCntListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
      // TODO Auto-generated method stub
      ServletContextListener.super.contextInitialized(sce);
      var srvCntx = sce.getServletContext();
      try (var conn = getUserDbConnection()) {
        var dbMeta = conn.getMetaData();
        var tables = dbMeta.getTables(null,null,null,null);
        var tabMeta = tables.getMetaData();
        if (false) {
          int cc=tabMeta.getColumnCount();
          for (int n=1;n<cc; n++) {
            System.out.println(tabMeta.getTableName(n) + " " +
                tabMeta.getColumnName(n)); }
          while (tables.next()) {
            System.out.println("Table name: "+tables.getString("Table_NAME"));
          }
        }
        for (var sql : createSqlTables) {
          try (var stmt = conn.createStatement()) {
            boolean results = stmt.execute(sql);
          }
        }
        try (var stmt = conn.createStatement()) {
         stmt.execute(
             "insert or ignore into roles values ('tttPlayer', 'TicTacToe Player');");
         stmt.execute(
             "insert or ignore into roles values ('tttAdmin', 'TicTacToe Administrator');");
        }
      } catch (SQLException | NamingException e) {
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
  
  static String getUserid(HttpServletRequest request) {
    var srvPath = request.getServletPath();
    var xtra = request.getPathInfo();
    if (xtra != null &&
        srvPath.equals("/PlayerList") ) {
      var mtch = userIdPat.matcher(xtra);
      //System.out.println(mtch);
      if (mtch.matches()) {
        return mtch.group(0);
      }
    }
    return null;
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
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
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // TODO Auto-generated method stub
    //super.doGet(req, resp);
    var srvPath = req.getServletPath();
    if (srvPath.equals("/PlayerList") && req.getParameter("whoami") !=null) { 
      doWhoAmI(req,resp); return;
    }
  }
  

  private void doWhoAmI(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
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

  private void outSelf(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
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
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
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

  private void doLogout(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    // TODO Auto-generated method stub
    req.logout();
  }

  private void doLogin(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    var authed = req.authenticate(resp);
    if (authed) {
      outSelf(req, resp);
    }
  }
  
  private void doNewUser(HttpServletRequest req, HttpServletResponse resp, String uid)
      throws IOException, ServletException {
    var usrQuerySql = "select * from users where user_name = ? ;";
    var usrInsertSql = "insert into users values ( ? , ? );";
    var playerRoleInsertSql = "insert into user_roles values ( ? , 'tttPlayer' );";
    boolean playerIsPreexist =false;
    try (var conn = getUserDbConnection()) {
      conn.setAutoCommit(false);
      try (var userStmt = conn.prepareStatement(usrInsertSql);
          var roleStmt = conn.prepareStatement(playerRoleInsertSql)) {
        userStmt.setString(1, uid);
        userStmt.setString(2, "KickMeRealHard");
        // TODO set a real password with salted hash
        roleStmt.setString(1, uid);
        userStmt.executeUpdate();
        roleStmt.executeUpdate();
        conn.commit();
      } catch (SQLIntegrityConstraintViolationException e) {
        conn.rollback();
        playerIsPreexist =true;
      }
    } catch (SQLException | NamingException e) {
      throw new ServletException("userDB failed", e);
    }
  
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // TODO Auto-generated method stub
    //super.doPut(req, resp);
    var uid = getUserid(req);
    var sess = req.getSession();
    var authed = req.authenticate(resp);
    var isAdminRole = req.isUserInRole("tttAdmin") ||
        req.isUserInRole("manager-gui");
    if (authed && !isAdminRole) {
      resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED );
      return;
    }
    if (authed) {
      if (uid == null) {
        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
      }
      doNewUser(req, resp, uid);
    }
  }
}
