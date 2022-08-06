/**
 * 
 */
package io.github.pollei.ticTac;

import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
//import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.pollei.ticTac.RobotFactory.Robo;

//import io.github.pollei.ticTac.RobotFactory;



//https://en.wikipedia.org/wiki/Tic-tac-toe
//https://en.wikipedia.org/w/index.php?title=Tic-tac-toe&oldid=1091798117

//import ticTac.baseGame.ttBoard;

// import ticTac.ttBoard.square;

/**
 * @author Steve_Pollei
 *
 */
public final class BaseTicTacGame {
	
	static public enum PlyrType {
		Human, Computer; }
	static public enum PlyrSym {
		X(3), O(2), Empty(0);
		private int v = 0;
		PlyrSym(int val) { v = val; }
		public boolean isMarked() {
			return v != 0; }
		public boolean isEmpty() {
			return v == 0; }
		public boolean isEqual(PlyrSym b) {
			return v == b.v; }
		public char toChar() {
			return this.toChar(' '); }
		public char toChar(char dv) {
			if (v == 2) return 'O';
			if (v == 3) return 'X';
			return dv;
		}
		public PlyrSym toOpponent() {
			if (this.isEqual(O)) return X;
			if (this.isEqual(X)) return O;
			return Empty;
		}
		public static PlyrSym strokeMatch(PlyrSym a, PlyrSym b, PlyrSym c) {
			if (a.v == b.v && b.v == c.v)
				return a;
			return PlyrSym.Empty;
		}
	}

	/*
	 * static public record Player(PlyrType type, PlyrSym sym) { public Player { if
	 * (sym.isEmpty()) throw new java.lang.IllegalArgumentException(); } }
	 */
	static public class Player {
		final PlyrType type;
		final PlyrSym sym;
		Player(PlyrType type, PlyrSym sym) {
			this.type =type;
			this.sym=sym;
		}
		protected Player(PlyrSym sym) {
			this(PlyrType.Human, sym);
		}
		Player() {
			this(PlyrType.Human, PlyrSym.X);
		}
    public PlyrSym getSym() { return sym; }
	}
	public final static class RobotPlayer extends Player {
	  RobotFactory.Robo robo;
	  RobotFactory.IRobot iRobo;

	  RobotPlayer(PlyrType type, PlyrSym sym, RobotFactory.Robo robo) {
			super(type, sym);
			this.robo = robo;
			this.iRobo = robo.newRobot(sym);
		}

	  RobotPlayer(PlyrSym sym, RobotFactory.Robo robo) {
			this(PlyrType.Computer, sym, robo);
		}

    public String getName() { return robo.name(); }

	}
	
	static public record SqrLoc(int x, int y) {
		public SqrLoc {
			if (x<0 || x>2 || y<0 || y>2 )
				throw new java.lang.IllegalArgumentException(); }
		public boolean isCenter() {
			return (1 == this.x && 1 == this.y);
		}
		public boolean isCorner() {
			return ((0 == this.x || 2 == this.x) &&
					(0 == this.y || 2 == this.y));
		}
		public boolean isSide() {
			return ((1 == this.x && ( 0 == this.y ||  2 == this.y )) ||
					(1 == this.y && ( 0 == this.x ||  2 == this.x )) );
		}
		public boolean isXY(int x, int y) {
		  return x == this.x && y == this.y;
		}
    @Override
    public int hashCode() {
      return 4*this.x + this.y;
    }
		
	}
	static public record Move(PlyrSym sym, int x, int y) {
		public Move {
			if (x<0 || x>2 || y<0 || y>2 || sym.isEmpty() )
				throw new java.lang.IllegalArgumentException();
		}
	}

	
	class Stroke {
	  Set<SqrLoc> sqrs;
	  Stroke(SqrLoc[] locA) {
	    this.sqrs = new HashSet<>();
	    for (var l : locA) { sqrs.add(l); }
	  }
	  Stroke(int[][] locA) {
      this.sqrs = new HashSet<>();
      for (var l : locA) {
        var sl = new SqrLoc(l[0], l[1]);
        sqrs.add(sl);
        }
    }
	  public boolean isAllSym(PlyrSym sym) {
	    for (var s : sqrs) {
	      if (sym != getSymAtLoc(s)) return false;
	    }
	    return true;
	  }
	  public boolean isWin() {
	    return this.isAllSym(PlyrSym.X) || this.isAllSym(PlyrSym.O);
	  }
	  public PlyrSym getWinSym() {
	    if (this.isAllSym(PlyrSym.X)) return PlyrSym.X;
	    if (this.isAllSym(PlyrSym.O)) return PlyrSym.O;
	    return PlyrSym.Empty;
	  }
	  public int cntSym(PlyrSym sym) {
	    int ret=0;
      for (var s : sqrs) {
        if (sym == board.boxes[s.x][s.y]) ret++;
      }
      return ret;
    }
	  public boolean hasXY(int x, int y) {
	    for (var s : sqrs) {
        if (x == s.x && y == s.y ) return true;
      }
      return false;
    }
	  public boolean hasLoc(SqrLoc sloc) {
      for (var s : sqrs) {
        if (s.equals(sloc) ) return true;
      }
      return false;
    }
	}
	
	public class SqrsForSym {
	  PlyrSym sym;
	  Set<SqrLoc> nearWins;
	  Set<SqrLoc> forky;
	  Set<SqrLoc> better;
	  public SqrsForSym(PlyrSym sym) {
      this.sym = sym;
      this.nearWins = new HashSet<>();
      this.forky = new HashSet<>();
      for ( var w : strokesAtLoc.entrySet()) {
        var sq=w.getKey();
        var strks = w.getValue();
        if (getSymAtLoc(sq).isEmpty() ) {
          if (strks.stream().anyMatch((s) -> s.cntSym(sym) > 1)) {
            nearWins.add(sq); }
          long frkStrkCnt = strks.stream()
              .filter( (s) -> s.cntSym(sym) > 0).count();
          if (frkStrkCnt > 1) { forky.add(sq); }
        }
      }
    }
	}
	
	static private class TicBoard {
		private PlyrSym[][] boxes = {
				 {PlyrSym.Empty, PlyrSym.Empty, PlyrSym.Empty},
				 {PlyrSym.Empty, PlyrSym.Empty, PlyrSym.Empty},
				 {PlyrSym.Empty, PlyrSym.Empty, PlyrSym.Empty},
		 };
	}
	//private plyrSym humanSym = plyrSym.X;
	//private plyrType firstPlayer = plyrType.Human;
	private TicBoard board = new TicBoard();
	private List<Player> plyrLst = new ArrayList<Player>() ;
	private List<Move> moveLst = new ArrayList<>();
	private int turn = 0;
	private Player currPlayer;
	private Player winPlayer;
	final List<Stroke> strokes = Stream.of( new int[][][] {
    {{0,0},{0,1},{0,2}},
    {{1,0},{1,1},{1,2}},
    {{2,0},{2,1},{2,2}},
    {{0,0},{1,0},{2,0}},
    {{0,1},{1,1},{2,1}},
    {{0,2},{1,2},{2,2}},
    {{0,0},{1,1},{2,2}},
    {{0,2},{1,1},{2,0}}  }
	    ).map( (itm) -> new Stroke(itm))
	    .collect(Collectors.toList());
	// MoveDecider mvMaker;
	final Map<SqrLoc, List<Stroke>> strokesAtLoc =
	    getAllLocs().stream()
	    .collect(
	        Collectors.toMap(
	            Function.identity(),
	            (sq) -> strokes.stream()
  	            .filter( (itm) -> itm.hasLoc(sq) )
  	            .collect(Collectors.toList())
	            )) ;
	
	public Player getPlayerBySym(PlyrSym sym) {
	  for (var p : plyrLst) {
	    if (sym == p.sym) return p;
	  }
	  return null;
	}
	public Player getWinner() {
	   var winStrokes = strokes.stream()
	       .filter(Stroke::isWin)
	       .collect(Collectors.toList());
	   if (! winStrokes.isEmpty()) {
	     var sym = winStrokes.get(0).getWinSym();
	     winPlayer = getPlayerBySym(sym);
	   }
		return winPlayer;
	}
	public Set<SqrLoc> getWinLocs() {
	  Set<SqrLoc> ret = new HashSet<>();
	  var winStrokes = strokes.stream()
        .filter(Stroke::isWin)
        .collect(Collectors.toList());
	  for (var s : winStrokes) { ret.addAll(s.sqrs); }
	  return ret;
	}
	public Player getCurrPlayer() {
	  return this.currPlayer;
	}
	public boolean isDone() {
		return (null != winPlayer || turn > 8);
	}
	public void setDefaultPlayers() {
		if (0 != turn) return;
		plyrLst.clear();
		plyrLst.add(new Player(PlyrType.Human, PlyrSym.X));
		plyrLst.add(new Player(PlyrType.Computer, PlyrSym.O));
		currPlayer = plyrLst.get(0);
		
	}
	public void setSolataire(int place, PlyrSym primarySym) {
	  if (place<0 || place > 1) return;
    if (0 != turn) return;
    var hmn0 = new Player(PlyrType.Human, primarySym);
    var hmn1 = new Player(PlyrType.Human, primarySym.toOpponent());
    plyrLst.clear();
    if (1 == place) {
      plyrLst.add(hmn1);
      plyrLst.add(hmn0);
    } else if (0 == place) {
      plyrLst.add(hmn0);
      plyrLst.add(hmn1);
    }
    currPlayer = plyrLst.get(0);
    
	}
	public void setHumanPlayerHVC(int place, Player humanPlayer, Robo nemesis) {
	  if (place<0 || place > 1) return;
    if (0 != turn) return;
    PlyrSym hmnSym = humanPlayer.sym;
    //var robo = nemesis.newRobot(hmnSym.toOpponent());
    var cmpt = new RobotPlayer(hmnSym.toOpponent(), nemesis);
    plyrLst.clear();
    if (1 == place) {
      plyrLst.add(cmpt);
      plyrLst.add(humanPlayer);
    } else if (0 == place) {
      plyrLst.add(humanPlayer);
      plyrLst.add(cmpt);
    }
    currPlayer = plyrLst.get(0);
	}
	
	public void setHumanPlayerHVC(int place, PlyrSym hmnSym, Robo nemesis) {
		if (place<0 || place > 1) return;
		if (0 != turn) return;
		var hmn = new Player(PlyrType.Human, hmnSym);
		setHumanPlayerHVC(place,hmn, nemesis);
	}
	public boolean searchWin() {
	  getWinner();
		return isDone();
	}
	public Move doComputerTurn() {
	  if (isDone()) return null;
	  if (currPlayer instanceof RobotPlayer rp) {
	    var mv = rp.iRobo.nextMove(this);
	    this.doMove(mv);
	    return mv;
	  }
		return null;
	}
	public void doMove(Move mv) {
		if (mv.x < 0 || mv.x > 2 ||
		    mv.y < 0 || mv.y > 2) return;
		if (mv.sym.isEqual(PlyrSym.Empty)) return;
		if (null != winPlayer ) return;
		if (board.boxes[mv.x][mv.y].isEqual(PlyrSym.Empty)) {
		  moveLst.add(mv);
			board.boxes[mv.x][mv.y] = mv.sym;
			turn++;
			searchWin();
			currPlayer = plyrLst.get(turn % 2);
		}
		
	}
	public Set<Move> getMoves(PlyrSym sym) {
		var ret = new HashSet<Move>();
		for (var x=0;x <3; x++) {
			for (var y=0;y <3; y++) {
				if (board.boxes[x][y].isEmpty() ) {
					ret.add(new Move(sym,x,y));
				}
			}
		}
		return ret;	
	}
	public Set<Move> getMoves() {
		return getMoves(currPlayer.sym);
	}
	public Set<SqrLoc> getEmptyLocs( ) {
		var ret = new HashSet<SqrLoc>();
		for (var x=0;x <3; x++) {
			for (var y=0;y <3; y++) {
				if (board.boxes[x][y].isEmpty() ) {
					ret.add(new SqrLoc(x,y));
				}
			}
		}
		return ret;	
	}
	public static Set<SqrLoc> getAllLocs( ) {
		var ret = new HashSet<SqrLoc>();
		for (var x=0;x <3; x++) {
			for (var y=0;y <3; y++) {
				 ret.add(new SqrLoc(x,y));  }
		}
		return ret;	
	}
	public PlyrSym getSymAtLoc(SqrLoc sl) {
		return board.boxes[sl.x][sl.y]; }
	public PlyrSym getSymAtXY(int x, int y) {
    return board.boxes[x][y]; }
  public int getTurn() { return turn; }
  public Player getPlayer(int n) { return plyrLst.get(n); }
  public Move getMove(int n) { return moveLst.get(n); }

}
