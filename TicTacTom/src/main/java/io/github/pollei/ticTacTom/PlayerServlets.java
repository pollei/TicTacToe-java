/**
 * 
 */
package io.github.pollei.ticTacTom;


import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.regex.Pattern;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.tomcat.util.security.ConcurrentMessageDigest;
import org.apache.tomcat.util.buf.HexUtils;


/**
 * @author Steve_Pollei
 *
 */
public final class PlayerServlets {
  //DataSource userDB;
  static final Pattern userIdPathPat = Pattern.compile("^/([a-z][a-z0-9_]{0,14})$");
  static final Pattern userIdPat = Pattern.compile("^[a-z][a-z0-9_]{0,14}$");
  static final String[] createSqlTables = {
      """
      create table if not exists users (
        user_name         varchar(32) not null primary key,
        user_full_name    varchar(128) not null,
        user_pass         varchar(512) not null );
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
        description       varchar(128) );
      """,
      """
      create table if not exists groups (
        group_name        varchar(32) not null primary key,
        description       varchar(128) );
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
  
  @WebListener()
  public static class SrvCntListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
      // TODO Auto-generated method stub
      ServletContextListener.super.contextInitialized(sce);
      var srvCntx = sce.getServletContext();
      try (var conn = Util.getUserDbConnection()) {
        var dbMeta = conn.getMetaData();
        var tables = dbMeta.getTables(null,null,null,null);
        var tabMeta = tables.getMetaData();
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
         //  https://www.delftstack.com/howto/mysql/mysql-insert-records-if-not-exists/
         int usrRowCnt = stmt.executeUpdate("""
             insert or ignore into users select * from (select 
             'tttBootStrap' as user_name,
             'ttt temporary bootstrap admin' as user_full_name,
             'KickMeRealHard' as user_pass) as fake
             where not exists (
               select * from (users, user_roles using (user_name)) where (role_name is 'tttAdmin'));
             """);
         if (usrRowCnt > 0) {
           stmt.execute(
               "insert or ignore into user_roles values ('tttBootStrap', 'tttAdmin' );"); }
         stmt.executeUpdate("""
             insert or ignore into groups select * from (select 
             'grid_goblins' as group_name,
             'Grid Goblins' as description) as fake
             where not exists ( select * from groups);
             """);
        }
      } catch (SQLException | NamingException e) {
        e.printStackTrace();
      }
      //System.out.println(deadbeef("KickMeRealHard"));
      // deadbeef$1$bd977372d55a86801257d5d080ef12a221981e945962fd8c7b61d2654ae4f7fc
      //System.out.println(deadbeef("xMarksSpot"));
      // deadbeef$1$d5fa36765e630654ec2433e6aa0e450c789e2bcf43260f9f4de1cc869e840c2a
      //System.out.println(deadbeef("OhhMy"));
      // deadbeef$1$e365574a4e397d9648a1fae05bb2aeb4aeba567f99faf39affb928adcc18ac2d
    }
  }
  static String deadbeef(String pt) {
    try {
      //var dig = new ConcurrentMessageDigest();
      ConcurrentMessageDigest.init("SHA-256");
      byte salt[] = {(byte)0xde, (byte)0xad, (byte)0xbe, (byte)0xef};
      var dig = ConcurrentMessageDigest.digest("SHA-256", 1 , salt, pt.getBytes("UTF-8"));
      return "deadbeef$1$" + HexUtils.toHexString(dig);
    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
      return null;
    }
  }
  
  static String getUserid(HttpServletRequest request) {
    var srvPath = request.getServletPath();
    var xtra = request.getPathInfo();
    if (xtra != null &&
        srvPath.equals("/PlayerList") ) {
      var mtch = userIdPathPat.matcher(xtra);
      //System.out.println(mtch);
      if (mtch.matches()) {
        return mtch.group(0);
      }
    }
    return null;
  }
  static void outSelf(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    var user=req.getRemoteUser();
    try {
      var doc = Util.newDoc();
      var topNod = doc.createElement("user");
      topNod.setAttribute("name", user);
      if (req.isUserInRole("tttAdmin")) {
        topNod.setAttribute("admin", "true");
      }
      doc.appendChild(topNod);
      Util.toResponse(doc, resp);
      //resp.setContentType("application/xml;charset=UTF-8");
      //XmlUtil.toWriter(doc, resp.getWriter());
    } catch (ParserConfigurationException | TransformerException e) {
      throw new ServletException("login fail", e);
    }
    resp.flushBuffer();
    
  }

  /**
   * 
   */
  public PlayerServlets() { }
  
  @WebServlet(
      name="Login",
      urlPatterns = {"/login" },
      description = "login for TicTacToe over http/https", loadOnStartup = 8)
  final static public class LoginSrv extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
      var authed = req.authenticate(resp);
      if (authed) {
        outSelf(req, resp);
      }
    }
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
      doPost(req, resp);
    }
  }
  @WebServlet(
      name="Logout",
      urlPatterns = {"/logout" },
      description = "login for TicTacToe over http/https", loadOnStartup = 8)
  final static public class LogoutSrv extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
      req.logout();
    }
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
      req.logout();
      resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }
  @WebServlet(
      name="PlayerList",
      urlPatterns = { "/PlayerList", "/PlayerList/*" },
      description = "Player list for TicTacToe over http/https", loadOnStartup = 8)
  final static public class PlayerListSrv extends HttpServlet {

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
      req.authenticate(resp);
      if (!isAdminUser(req)) {
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
      req.authenticate(resp);
      if (isAdminUser( req)) {
        try (var conn = Util.getUserDbConnection()) {
          var doc = Util.newDoc();
          var topNod = doc.createElement("players");
          doc.appendChild(topNod);
          try (var stmt = conn.createStatement()) {
            var qResults = stmt.executeQuery(
                "select * from users left join user_groups using (user_name);");
            // https://www.geeksforgeeks.org/sql-join-set-1-inner-left-right-and-full-joins/
            while (qResults.next()) {
              var plyrNod = doc.createElement("player");
              plyrNod.setAttribute("name", qResults.getString(1) );
              plyrNod.setAttribute("full_name", qResults.getString(2) );
              plyrNod.setAttribute("pw", qResults.getString(3) );
              plyrNod.setAttribute("group", qResults.getString(4) );
              topNod.appendChild(plyrNod);
            }
          }
          Util.toResponse(doc, resp);
          
        } catch (SQLException | NamingException | ParserConfigurationException | TransformerException e) {
          e.printStackTrace();
          throw new ServletException("PlayerList get fail", e);
        }
      }
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
      if (isAdminUser(req) && isNewUserCfg(req)) {
        doNewUserCfg(req, resp); return;
      }
    
    }

    static void doNewUserCfg(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
      var user_nameStr = req.getParameter("user_name");
      var full_nameStr = req.getParameter("full_name");
      var passwordStr = req.getParameter("password");
      try {
        var ds = Util.getUserDatasource();
        ds.createUser(user_nameStr, passwordStr, full_nameStr);
        ds.save();
      } catch (Exception e) {
        e.printStackTrace();
        throw new ServletException("new player fail", e);
      }
      
    }

    private boolean isNewUserCfg(HttpServletRequest req) {
      var user_nameStr = req.getParameter("user_name");
      var passwordStr = req.getParameter("password");
      if (null == user_nameStr || null == passwordStr) return false;
      var user_nameValid = userIdPat.matcher(user_nameStr).matches();
      return (user_nameValid && passwordStr.length() > 6);
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
  }
  public static boolean isAdminUser(HttpServletRequest req) {
    var user=req.getRemoteUser();
    if (null == user) return false;
    return req.isUserInRole("tttAdmin") ||
        req.isUserInRole("manager-gui");
  }

}
