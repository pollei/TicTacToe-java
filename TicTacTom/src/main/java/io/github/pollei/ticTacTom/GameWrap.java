/**
 * 
 */
package io.github.pollei.ticTacTom;


import java.io.IOException;

import io.github.pollei.ticTac.BaseTicTacGame;
import io.github.pollei.ticTac.BaseTicTacGame.PlyrSym;
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
@WebListener()
public class GameWrap implements HttpSessionListener {

  final BaseTicTacGame game = new BaseTicTacGame();
  final Object webMutex = new Object();
  String creator;
  String nemesisName;
  final long createTime = System.currentTimeMillis();

  static public class webPlayer extends BaseTicTacGame.Player {

    String userName;

    webPlayer(PlyrSym sym, String userName) {
      super(sym);
      this.userName = userName;
    }
    
  }
  boolean isReadyToDie() { return false; }
  /**
   * 
   */
  public GameWrap() { }
  public GameWrap(String creator) {
    this.creator = creator;
  }
  void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    synchronized (webMutex ) {
      
    }
  }
  void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    synchronized (webMutex ) {
      
    }
  }
  
  @Override
  public void sessionDestroyed(HttpSessionEvent se) {
    // TODO Auto-generated method stub
    //HttpSessionListener.super.sessionDestroyed(se);
  }
  

}
