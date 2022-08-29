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
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

//import jakarta.json.Json;
import java.io.IOException;
import java.lang.ref.WeakReference;
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
import io.github.pollei.ticTacTom.Util;
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
final public class WebGameSrv extends HttpServlet {
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
	@SuppressWarnings("unchecked")
  static ConcurrentHashMap<String, WeakReference<GameWrap> > getSessGameMap(HttpSession sess) {
	  var gmraw = sess.getAttribute("gameMap");
	  if (gmraw instanceof ConcurrentHashMap<?,? > gm) {
	    return (ConcurrentHashMap<String, WeakReference<GameWrap>>) gm;
	  }
	  return null;
	}
	static ConcurrentHashMap<String, WeakReference<GameWrap> > getSessGameMap(HttpServletRequest req) {
	  return getSessGameMap(req.getSession()); }

  /**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
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
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
		//System.out.println("in post of game");
		var srvPath = request.getServletPath();
		if (srvPath.equals("/NewGame")) {
		  doNewGame(request, response);
		  return;
		}
		var gameId = getGameid(request);
		System.out.println("game map size " + gameMap.size());
    if (null != gameId) {
      var gameW = gameMap.get(gameId);
      if (null != gameW) {
        // in theory race window exists where another thread did action=shutdown-game
        // they game might still be accessible, but be in dying state
        gameW.doPost(request, response);
        // in theory race window exists where another thread did action=shutdown-game
        // in theory more than one thread might try to remove the game
        if (gameW.isReadyToDie()) {
          boolean died = gameMap.remove(gameId, gameW);
          System.out.println("atomicly dead game " + gameId + " " + died);
          var sessGameMap= getSessGameMap(request);
          sessGameMap.remove(gameId);
        }
        return;
      } else {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        response.flushBuffer();
        return;
      }
    }
	  //doGet(request, response);
	}
	@Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
	  var gameId = getGameid(req);
	  var isAdmin = req.isUserInRole("tttAdmin") ||
	      req.isUserInRole("manager-gui");
	  if (null == gameId || !isAdmin) {
	    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
	    return; }
	  var oldGame = gameMap.remove(gameId);
	  if (null == oldGame) {
	    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
	  }

  }

  private void doNewGame(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    //response.addHeader("Content-Type", "application/xml;charset=UTF-8");
    var isPlayerRole = request.isUserInRole("tttPlayer") ||
        PlayerServlets.isAdminUser(request);
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
    var sessGameMap= getSessGameMap(request);
    sessGameMap.put(gameId, new WeakReference<GameWrap>(newGame));
    if (GameWrap.isGameCfg(request)) newGame.doGameCfg(request,response);
    try {
      doc = newGame.newDoc();
      Util.toWriter(doc, response.getWriter());
    } catch (DOMException | ParserConfigurationException | TransformerException e) {
      throw new ServletException("newgame fail", e);
    }
    //response.setStatus(HttpServletResponse.SC_CREATED); // created 201
    // https://developer.mozilla.org/en-US/docs/Web/HTTP/Status
    response.getWriter().flush();
  }
  
  @WebListener()
  final static public class sessDeathListener implements HttpSessionListener {
    @Override
    public void sessionCreated(HttpSessionEvent se) {
      // TODO Auto-generated method stub
      HttpSessionListener.super.sessionCreated(se);
      var sess = se.getSession();
      var gm = new ConcurrentHashMap<String, WeakReference<GameWrap> >();
      sess.setAttribute("gameMap", gm);
      System.out.println("session gamemap created");
      //sess.setMaxInactiveInterval(13);
      // set to a short time to test destruction
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
      // TODO Auto-generated method stub
      HttpSessionListener.super.sessionDestroyed(se);
      var sess = se.getSession();
      var gm = getSessGameMap(sess);
      System.out.println("session destroying");
      for (var ent : gm.entrySet()) {
        //var game = ent.getValue().get();
        gameMap.remove(ent.getKey());
      }
    }
  }

  
}
