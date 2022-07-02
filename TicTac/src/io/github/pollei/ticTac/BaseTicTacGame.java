/**
 * 
 */
package io.github.pollei.ticTac;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

//import io.github.pollei.ticTac.RobotFactory;

import java.util.HashSet;

//https://en.wikipedia.org/wiki/Tic-tac-toe
//https://en.wikipedia.org/w/index.php?title=Tic-tac-toe&oldid=1091798117

//import ticTac.baseGame.ttBoard;

// import ticTac.ttBoard.square;

/**
 * @author Steve_Pollei
 *
 */
public class BaseTicTacGame {
	
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
		public PlyrType type;
		public PlyrSym sym;
		Player(PlyrType type, PlyrSym sym) {
			this.type =type;
			this.sym=sym;
		}
		Player(PlyrSym sym) {
			this(PlyrType.Human, sym);
		}
		Player() {
			this(PlyrType.Human, PlyrSym.X);
		}
	}
	public static class RobotPlayer extends Player {
		public RobotFactory.IRobot robo;

		public RobotPlayer(PlyrType type, PlyrSym sym, RobotFactory.IRobot robo) {
			super(type, sym);
			this.robo = robo;
		}

		public RobotPlayer(PlyrSym sym, RobotFactory.IRobot robo) {
			this(PlyrType.Computer, sym, robo);
		}

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
		
	}
	static public record Move(PlyrSym sym, int x, int y) {
		public Move {
			if (x<0 || x>2 || y<0 || y>2 || sym.isEmpty() )
				throw new java.lang.IllegalArgumentException();
		}
	}
	
	static public class TicBoard {
		PlyrSym[][] boxes = {
				 {PlyrSym.Empty, PlyrSym.Empty, PlyrSym.Empty},
				 {PlyrSym.Empty, PlyrSym.Empty, PlyrSym.Empty},
				 {PlyrSym.Empty, PlyrSym.Empty, PlyrSym.Empty},
		 };
	}
	//private plyrSym humanSym = plyrSym.X;
	//private plyrType firstPlayer = plyrType.Human;
	TicBoard board;
	List<Player> plyrLst = new ArrayList<Player>() ;
	int turn = 0;
	Player currPlayer;
	Player winPlayer;
	// MoveDecider mvMaker;
	
	public Player getWinner() {
		return winPlayer;
	}
	public boolean isDone() {
		return (null != winPlayer);
	}
	public void setDefaultPlayers() {
		if (0 != turn) return;
		plyrLst.clear();
		plyrLst.add(new Player(PlyrType.Human, PlyrSym.X));
		plyrLst.add(new Player(PlyrType.Computer, PlyrSym.O));
		currPlayer = plyrLst.get(0);
		
	}
	public void setHumanPlayerHVC(int place, PlyrSym sym) {
		if (place<0 || place > 1) return;
		if (0 != turn) return;
		var hmn = new Player(PlyrType.Human, sym);
		//var cmpt = new Player(PlyrType.Computer, sym.toOpponent());
		var robo = RobotFactory.Robo.Randy.newRobot(sym.toOpponent());
		var cmpt = new RobotPlayer(sym.toOpponent(), robo);
		plyrLst.clear();
		if (1 == place) {
			plyrLst.add(cmpt);
			plyrLst.add(hmn);
		} else if (0 == place) {
			plyrLst.add(hmn);
			plyrLst.add(cmpt);
		}
		currPlayer = plyrLst.get(0);
	}
	public boolean searchWin() {
		return turn > 8;
	}
	public void doComputerTurn() {
		
	}
	public void doMove(Move mv) {
		if (mv.x < 0 || mv.x > 2 ||
		    mv.y < 0 || mv.y > 2) return;
		if (mv.sym.isEqual(PlyrSym.Empty)) return;
		if (null != winPlayer ) return;
		if (board.boxes[mv.x][mv.y].isEqual(PlyrSym.Empty)) {
			board.boxes[mv.x][mv.y] = mv.sym;
			turn++;
			searchWin();
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

}
