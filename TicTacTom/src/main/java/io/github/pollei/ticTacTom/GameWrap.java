/**
 * 
 */
package io.github.pollei.ticTacTom;


import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import io.github.pollei.ticTac.BaseTicTacGame;
import io.github.pollei.ticTac.BaseTicTacGame.Move;
import io.github.pollei.ticTac.BaseTicTacGame.PlyrSym;
import io.github.pollei.ticTac.BaseTicTacGame.RobotPlayer;
import io.github.pollei.ticTac.RobotFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * @author Steve_Pollei
 *
 */
final public class GameWrap {

  final BaseTicTacGame game = new BaseTicTacGame();
  final Object webMutex = new Object();
  //final ReentrantLock webLock = new ReentrantLock(false);
  //final Condition moveCond = webLock.newCondition();
  // https://blog.ffwll.ch/2022/08/locking-hierarchy.html
  // Level 1: Big Dumb Lock
  AtomicBoolean atomicDeath = new AtomicBoolean();
  String creator;
  String gameId;
  String nemesisName;
  WebPlayer self;
  boolean autoDie = false;
  final long createTime = System.currentTimeMillis();
  long lastModTime = System.currentTimeMillis();
  
  static final Pattern roboPat = Pattern.compile("^([a-z]{1,9})$", Pattern.CASE_INSENSITIVE);
  static final Pattern simpleArgPat = Pattern.compile("^([0-9xo])$", Pattern.CASE_INSENSITIVE);

  boolean isReadyToDie() { return atomicDeath.get(); }
  /**
   * 
   */
  public GameWrap() { }
  public GameWrap(String creator, String gameId) {
    this.creator = creator;
    this.gameId = gameId;
  }
  void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    synchronized (webMutex ) {
      var turnStr = req.getParameter("turn");
      try {
        if (null != turnStr) {
          var turn = Integer.parseInt(turnStr);
          if (turn < 0 || turn > 9) {throw new IndexOutOfBoundsException(); }
          if (turn >= game.getTurn()) {
            webMutex.wait(10500);
            if (turn >= game.getTurn()) {
              resp.addHeader("Retry-After", "1");
              resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            }
          }
        }
      }
      catch (RuntimeException  e) {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      } catch (  InterruptedException e) { }
      out(resp);
    }
  }
  void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    synchronized (webMutex ) {
      if (isReadyToDie()) { out(resp); return; }
      if (isReportMove(req)) {
        doReportMove(req, resp);
        return;
      }
      if (isShutdownGame(req)) {
        doShutdownGame(req, resp);
        return;
      }
    }
  }
  Element moveElem(Document doc, int n) {
    try {
      var move = game.getMove(n);
      var mvNod = doc.createElement("move");
      mvNod.setAttribute("sym", move.sym().name());
      mvNod.setAttribute("index", String.valueOf(n));
      mvNod.setAttribute("x", String.valueOf(move.x()));
      mvNod.setAttribute("y", String.valueOf(move.y()));
      return mvNod;
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
  }
  Element plyrElem(Document doc, int n) {
    try {
      var plyr = game.getPlayer(n);
      var plyrNod = doc.createElement("player");
      plyrNod.setAttribute("sym", plyr.getSym().name());
      plyrNod.setAttribute("index", String.valueOf(n));
      if (plyr instanceof RobotPlayer rb) {
        plyrNod.setAttribute("robo", rb.getName()); }
      if (plyr instanceof WebPlayer wb) {
        plyrNod.setAttribute("human", wb.userName); }
      return plyrNod;
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
  }
  Document newDoc() throws ParserConfigurationException {
    var doc = Util.newDoc();
    var topNod = doc.createElement("game");
    doc.appendChild(topNod);
    if (null != this.gameId) topNod.setAttribute("gameid", this.gameId);
    if (isReadyToDie()) { topNod.setAttribute("dying", "true"); }
    if (null != game) {
      topNod.setAttribute("turn", String.valueOf(game.getTurn()));
    }
    var p0 = plyrElem(doc, 0);
    var p1 = plyrElem(doc, 1);
    if (null != p0) topNod.appendChild(p0);
    if (null != p1) topNod.appendChild(p1);
    for (int n =0 ; n < game.getTurn(); n++) {
      var mvE = moveElem(doc, n);
      if (null != mvE) topNod.appendChild(mvE);
    }
    return doc;
  }
  
  void doReportMove(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    Document doc;
    var turnStr = request.getParameter("turn");
    var xStr = request.getParameter("x");
    var yStr = request.getParameter("y");
    response.setContentType("application/xml;charset=UTF-8");
    try {
      int turn = Integer.parseInt(turnStr);
      int x = Integer.parseInt(xStr);
      int y = Integer.parseInt(yStr);
      if (turn != game.getTurn() || x>2 || y>2) {
        //System.out.println("bad args in report move");
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      var mv = new Move(game.getCurrPlayer().getSym(), x, y);
      var mvs = game.getMoves();
      if (!mvs.contains(mv)) {
        //System.out.println("set mismatch in report move");
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      this.lastModTime = System.currentTimeMillis();
      game.doMove(mv);
      game.doComputerTurn();
      game.doLastTurn();
      if (autoDie && game.isDone()) {atomicDeath.set(true);}
    } catch (RuntimeException e) { 
      throw new ServletException("report move invalid args", e);
    }
    webMutex.notifyAll();
    try {
      doc = this.newDoc();
      Util.toWriter(doc, response.getWriter());
    } catch (ParserConfigurationException | TransformerException e) {
      throw new ServletException("report move fail", e);
    }
  }
  
  void doGameCfg(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    synchronized (webMutex) {
      //var actionStr = request.getParameter("action");
      //var foeStr = request.getParameter("foe-kind");
      var ordStr = request.getParameter("ordrRadGrp");
      var xoStr = request.getParameter("xoRadGrp");
      var nemesisStr = request.getParameter("nemesisRadGrp");
      
      try {
        var hmnSym = PlyrSym.valueOf(xoStr);
        var nemesis = RobotFactory.getRoboByName(nemesisStr);
        this.self = new WebPlayer(hmnSym, creator);
        game.setHumanPlayerHVC(Integer.parseInt(ordStr), self, nemesis);
        //this.autoDie = true;
      } catch (RuntimeException e) { 
        throw new ServletException("configure Game invalid args", e);
      }
      game.doComputerTurn();
    }
  }
  private void out(HttpServletResponse resp) 
      throws ServletException, IOException {
    resp.setContentType("application/xml;charset=UTF-8");
    resp.addDateHeader("Last-Modified", lastModTime);
    // TODO ETag
    try {
      Document doc = this.newDoc();
      Util.toWriter(doc, resp.getWriter());
    } catch (ParserConfigurationException | TransformerException e) {
      throw new ServletException("out game fail", e);
    }
  }
  private void doShutdownGame(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    atomicDeath.set(true);
    out(resp);
  }

  static boolean isReportMove(HttpServletRequest request) {
    var actionStr = request.getParameter("action");
    //System.out.println("action: " + actionStr);
    if (! "report-move".equalsIgnoreCase(actionStr)) return false;
    String[] args = {"x","y","turn","sym"};
    for (var arg : args) {
      var argStr=request.getParameter(arg);
      var mtch=simpleArgPat.matcher(argStr);
      if (!mtch.matches()) return false;
    }
    return true;
  }
  static boolean isShutdownGame(HttpServletRequest request) {
    var actionStr = request.getParameter("action");
    //System.out.println("action: " + actionStr);
    if (! "shutdown-game".equalsIgnoreCase(actionStr)) return false;
    return true;
  }

  static boolean isGameCfg(HttpServletRequest request) {
    var actionStr = request.getParameter("action");
    //System.out.println("action: " + actionStr);
    if (! "config-game".equalsIgnoreCase(actionStr)) return false;
    var foeStr = request.getParameter("foe-kind");
    //System.out.println("foe: " + foeStr );
    if (! "robot".equalsIgnoreCase(foeStr)) return false;
    var ordStr = request.getParameter("ordrRadGrp");
    //System.out.println("foe: " + foeStr + " order: " + ordStr);
    if (! ("0".equals(ordStr) || "1".equals(ordStr) )) return false;
    var xoStr = request.getParameter("xoRadGrp");
    //System.out.println("foe: " + foeStr + " order: " + ordStr + " xo: " + xoStr);
    if (! ("o".equalsIgnoreCase(xoStr) || "x".equalsIgnoreCase(xoStr) )) return false;
    var nemesisStr = request.getParameter("nemesisRadGrp");
    //System.out.println("foe: " + foeStr + " nem: " + nemesisStr  );
    var mtch = roboPat.matcher(nemesisStr);
    return mtch.matches();
  }

}
