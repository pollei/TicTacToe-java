/**
 * 
 */
package io.github.pollei.ticTacTom;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.catalina.Group;
import org.apache.catalina.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Steve_Pollei
 *
 */
public class GroupServlets {

  /**
   * 
   */
  private SecureRandom secRndNumGen;
  public GroupServlets() { }
  static class GroupWrap {
    final ConcurrentHashMap<String, GameWrap> gameMap = new ConcurrentHashMap<>();
    ExecutorService executorService = Executors.newCachedThreadPool();
    Util.LruCache<String, WebPlayer> waitingRoom =
        new Util.LruCache<String, WebPlayer>(WebPlayer.newLamb);
    static ExecutorService getDefaultExec() {
      var ret = Executors.newCachedThreadPool();
      if (false) Executors.newCachedThreadPool(
          (r) -> {var t= new Thread(r);
          t.setPriority(22);
          return t;});
      return ret;
    }
  }
  static Iterator<Group> getGroups(HttpServletRequest req) throws NamingException {
    if (PlayerServlets.isAdminUser(req)) {
      var udb = Util.getUserDatasource();
      return udb.getGroups();
    } else {
      var user = req.getUserPrincipal();
      if (user instanceof User tomUser) {
        return tomUser.getGroups();
      }
    }
    return null;
  }
  @WebServlet(
      name="GroupList",
      urlPatterns = {"/GroupList", "/group-list" },
      description = "group list for TicTacToe over http/https", loadOnStartup = 13)
  final static public class GroupListSrv extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 5613620897701870832L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
      try {
        var doc = Util.newDoc();
        var topNod = doc.createElement("groups");
        doc.appendChild(topNod);
        var grps = getGroups(req);
        while (grps.hasNext()) {
          var grp=grps.next();
          var grpNod = doc.createElement("group");
          topNod.appendChild(grpNod);
          grpNod.setAttribute("name", grp.getGroupname());
          grpNod.setAttribute("description", grp.getDescription());
        }
        Util.toResponse(doc, resp);
      } catch (ParserConfigurationException | NamingException | TransformerException e) {
        e.printStackTrace();
        throw new ServletException("get groups fail", e);
      }
    }
  }
}
