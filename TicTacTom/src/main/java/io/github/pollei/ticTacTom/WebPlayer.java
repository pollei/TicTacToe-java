package io.github.pollei.ticTacTom;

import java.util.function.Function;

import io.github.pollei.ticTac.BaseTicTacGame;
import io.github.pollei.ticTac.BaseTicTacGame.PlyrSym;
import io.github.pollei.ticTacTom.Util.TrackTTL;

public class WebPlayer extends BaseTicTacGame.Player implements TrackTTL {
  String userName;
  boolean readyToDie=false;
  long lastTouchTime = System.currentTimeMillis();
  static final Function<String, WebPlayer> newLamb = (s) -> new WebPlayer(s);
  static final int [] lruTimeouts = {21500,24500, 49500};
  WebPlayer(PlyrSym sym, String userName) {
    super(sym);
    this.userName = userName;
  }
  WebPlayer(String userName) {this(PlyrSym.Empty, userName);}
  @Override
  public void touch() { this.lastTouchTime = System.currentTimeMillis(); }
  @Override
  public long lastTouch() {  return this.lastTouchTime; }
}