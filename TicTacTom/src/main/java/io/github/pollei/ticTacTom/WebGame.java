package io.github.pollei.ticTacTom;

import jakarta.annotation.security.DeclareRoles;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
//import jakarta.json.Json;
import java.io.IOException;
//import java.net.http.HttpResponse;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.transform.TransformerException;

import java.security.NoSuchAlgorithmException;
//import java.util.random.RandomGenerator;
import java.security.SecureRandom;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import io.github.pollei.ticTac.BaseTicTacGame;
import io.github.pollei.ticTacTom.XmlUtil;
//import io.github.pollei.ticTac.*;
//import io.github.pollei.ticTac.BaseTicTacGame;
//import io.github.pollei.ticTac.*;

/**
 * Servlet implementation class WebGame
 */
@DeclareRoles(value = { "tttPlayer", "tttAdmin", "manager-gui" })
@ServletSecurity(
    @HttpConstraint(
        rolesAllowed = {"tttPlayer", "tttAdmin", "manager-gui"},
        transportGuarantee = ServletSecurity.TransportGuarantee.NONE))
@WebServlet(
    name="WebGame",
    urlPatterns = {"/WebGame","/NewGame", "/Game", "/Game/*", "/WebGame/*" },
    description = "Play TicTacToe over http/https", loadOnStartup = 13)
public class WebGame extends HttpServlet {
	private static final long serialVersionUID = 1L;
  private SecureRandom secRndNumGen;
  private static final ConcurrentHashMap<String, BaseTicTacGame> gameMap = new ConcurrentHashMap<String,BaseTicTacGame>();
  
  
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public WebGame() {
        super();
        // TODO Auto-generated constructor stub
    }
    

	@Override
    public void init() throws ServletException {
      // TODO Auto-generated method stub
      super.init();
      try {
        secRndNumGen = SecureRandom.getInstanceStrong();
      } catch (NoSuchAlgorithmException e) {
        throw new ServletException("no random for you", e);
      }
    }


  /**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	  var sess = request.getSession();
	  var srvCntx = request.getServletContext();
	  var cc=srvCntx.getSessionCookieConfig();
	  
	  var srvPath = request.getServletPath();
    if (srvPath.equals("/NewGame")) {
      doNewGame(request, response);
      return;
    }
		response.getWriter().append("Served at: ").append(request.getContextPath());
		var srvContext = this.getServletContext();
		Object o = srvContext.getAttribute("io.github.pollei.ticTac.gmap");
		var game= new BaseTicTacGame();
		Long.toHexString(123L);
		response.getWriter().append(game.toString()).append(" ");
		response.getWriter().append(request.getPathInfo()).append(" ");
		response.getWriter().append(request.getServletPath()).append(" ");
		response.getWriter().append(cc.toString()).append(" ");
		response.getWriter().append(cc.getPath() ).append(" ");
		response.getWriter().append(sess.getId() ).append(" ");
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		var srvPath = request.getServletPath();
		if (srvPath.equals("/NewGame")) {
		  doNewGame(request, response);
		  return;
		}
	  doGet(request, response);
	}

  private void doNewGame(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // TODO Auto-generated method stub
    //response.addHeader("Content-Type", "application/xml;charset=UTF-8");
    response.setContentType("application/xml;charset=UTF-8");
    var doc = XmlUtil.newDoc();
    var topNod = doc.createElement("newgame");
    var rndL = secRndNumGen.nextLong();
    var gameId = Long.toHexString(rndL);
    topNod.setAttribute("gameid", gameId);
    doc.appendChild(topNod);
    var newGame = new BaseTicTacGame();
    var oldGamme = gameMap.putIfAbsent(gameId, newGame);
    if (oldGamme != null) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      // really should retry but this should be EXTREMELY unlikely 500
      return;
    }
    response.setStatus(HttpServletResponse.SC_CREATED); // created 201
    // https://developer.mozilla.org/en-US/docs/Web/HTTP/Status
    try {
      XmlUtil.toWriter(doc, response.getWriter());
    } catch (TransformerException e) {
      throw new ServletException("newgame fail", e);
    }
    response.getWriter().flush();
  }

}
