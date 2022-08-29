/**
 * 
 */
package io.github.pollei.ticTacTom;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import io.github.pollei.ticTac.RobotFactory.Robo;

/**
 * @author Steve_Pollei
 *
 */
public final class Servlets {

  /**
   * 
   */
  public Servlets() { }
  
  @WebServlet(
      name="RobotList",
      urlPatterns = {"/RobotList",  "/robot-list" },
      description = "Query the list of robots nemesis for TicTacToe over http/https",
      loadOnStartup = 17 )
  final static public class RobotListSrv extends HttpServlet {
    private static final long serialVersionUID = 2499584481482159049L;
    private static final String xmlStr  = getXmlStr();
    private static String getXmlStr() {
      String ret = null;
      try {
        var doc = Util.newDoc();
        var rlNod = doc.createElement("robolist");
        for (var r:  Robo.values()) {
          var robNod = doc.createElement("player");
          robNod.setAttribute("robo", r.name());
          rlNod.appendChild(robNod);
        }
        doc.appendChild(rlNod);
        ret = Util.toString(doc);
      } catch (ParserConfigurationException | TransformerException e) {
        e.printStackTrace();
      }
      return ret;
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
      response.addHeader("Cache-Control", "max-age=9000, stale-while-revalidate=900");
      response.addHeader("Content-Type", "application/xml;charset=UTF-8");
      response.getWriter().append(xmlStr);
    }
  }

  @WebServlet(
      name="KeepAlive",
      urlPatterns = {"/KeepAlive",  "/keep-alive" },
      description = "keep session alive", loadOnStartup = 17 )
  public static final class KeepAliveSrv extends HttpServlet {
    private static final long serialVersionUID = 8633283633064146016L;
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
      var sess = request.getSession();
      if (false) sess.setMaxInactiveInterval(13);
      // set to a short time to test destruction of game and keepalive
      var maxInterval = sess.getMaxInactiveInterval();
      var maxAge = Math.min(maxInterval/8 + 1, 30);
      response.addHeader("Cache-Control", String.format( "max-age=%d", maxAge));
      try {
        var doc = Util.newDoc();
        var sessNod = doc.createElement("session");
        doc.appendChild(sessNod);
        sessNod.setAttribute("interval", String.valueOf(maxInterval));
        if (sess.isNew()) sessNod.setAttribute("new", "true");
        Util.toResponse(doc, response);
      } catch (ParserConfigurationException | TransformerException e) { 
        e.printStackTrace();
        throw new ServletException("keep alive fail", e);
      }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
      doGet(request, response); }
  }

}
