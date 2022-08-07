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
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import java.security.NoSuchAlgorithmException;
//import java.util.random.RandomGenerator;
import java.security.SecureRandom;

import org.w3c.dom.DOMException;
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
public class WebGameSrv extends HttpServlet {
	private static final long serialVersionUID = 1L;
  private SecureRandom secRndNumGen;
  private static final ConcurrentHashMap<String, GameWrap> gameMap = new ConcurrentHashMap<>();
  static final Pattern gameIdPat = Pattern.compile("^/([a-fA-F0-9]+)$");
  
  
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public WebGameSrv() {
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
	
	static String getGameid(HttpServletRequest request) {
	  var srvPath = request.getServletPath();
    var xtra = request.getPathInfo();
    if (xtra != null &&
        (srvPath.equals("/WebGame") || srvPath.equals("/Game"))) {
      var mtch = gameIdPat.matcher(xtra);
      //System.out.println(mtch);
      if (mtch.matches()) {
        return mtch.group(1);
      }
    }
	  return null;
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
	  var xtra = request.getPathInfo();
    if (srvPath.equals("/NewGame")) {
      doNewGame(request, response);
      return;
    }
    var gameId = getGameid(request);
    if (null != gameId) {
      var gameW = gameMap.get(gameId);
      if (null != gameW) {
        gameW.doGet(request, response);
        return;
      } else {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        response.flushBuffer();
        return;
      }
    }
		response.getWriter().append("Served at: ").append(request.getContextPath());
		var srvContext = this.getServletContext();
		Object o = srvContext.getAttribute("io.github.pollei.ticTac.gmap");
		//var game= new BaseTicTacGame();
		Long.toHexString(123L);
		//response.getWriter().append(game.toString()).append(" ");
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
		var gameId = getGameid(request);
    if (null != gameId) {
      var gameW = gameMap.get(gameId);
      if (null != gameW) {
        gameW.doPost(request, response);
        return;
      } else {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        response.flushBuffer();
        return;
      }
    }
	  doGet(request, response);
	}

  private void doNewGame(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    //response.addHeader("Content-Type", "application/xml;charset=UTF-8");
    var isPlayerRole = request.isUserInRole("tttPlayer");
    if (!isPlayerRole) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      response.flushBuffer();
      return;
    }
    response.setContentType("application/xml;charset=UTF-8");
    var sess = request.getSession();
    Document doc;
    String gameId;
    String sessAge = sess.isNew() ? "newSess " : "oldSess";
    //System.out.println("Session");
    //System.out.println(sess.getMaxInactiveInterval());
    System.out.println(sessAge + sess.getId() + " " + sess.getCreationTime() );
    {
      var rndL = secRndNumGen.nextLong();
      gameId = Long.toHexString(rndL);
    }
    var newGame = new GameWrap(request.getRemoteUser(), gameId);
    var oldGamme = gameMap.putIfAbsent(gameId, newGame);
    if (oldGamme != null) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      // really should retry but this should be EXTREMELY unlikely 500
      return;
    }
    if (GameWrap.isGameCfg(request)) newGame.doGameCfg(request,response);
    try {
      doc = newGame.newDoc();
      XmlUtil.toWriter(doc, response.getWriter());
    } catch (DOMException | ParserConfigurationException | TransformerException e) {
      throw new ServletException("newgame fail", e);
    }
    //response.setStatus(HttpServletResponse.SC_CREATED); // created 201
    // https://developer.mozilla.org/en-US/docs/Web/HTTP/Status
    response.getWriter().flush();
  }
}
