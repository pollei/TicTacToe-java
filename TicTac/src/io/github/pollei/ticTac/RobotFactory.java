/**
 * 
 */
package io.github.pollei.ticTac;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
//import java.util.LinkedHashMap;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
//import java.util.concurrent.ThreadLocalRandom;

import io.github.pollei.ticTac.BaseTicTacGame.Move;
import io.github.pollei.ticTac.BaseTicTacGame.PlyrSym;
import io.github.pollei.ticTac.BaseTicTacGame.SqrLoc;

/**
 * @author Steve_Pollei
 *
 */
public class RobotFactory {
	/**
		 * @author Steve_Pollei
		 *
		 */
	// https://stackoverflow.com/questions/6409652/random-weighted-selection-in-java
	static public class WeightedRandom<E> {
		private final NavigableMap<Long, E> map = new TreeMap<>();
	    private final Random random = new Random();
	    private long total = 0;

		public void add(long weight, E result) {
			if (weight<=0) return;
			total += weight;
			map.put(total, result);
		}
		public E sample() {
			long v = random.nextLong(total);
			return map.higherEntry(v).getValue();
		}
		public WeightedRandom() {
			//  Auto-generated constructor stub
		}

	}
	



	//private List<Class<? extends IRobot> > roboList;
	//private Map<String, Class<? extends IRobot> > roboMap;
	protected static final Robo robots[] = {Robo.Randy, Robo.Bob, Robo.Emily};
	/**
		 * @author Steve_Pollei
		 *
		 */
	/**
	 * @author Steve_Pollei
	 *
	 */
	 
	static sealed public interface IRobot {
		//final static String roboName ="";
		public abstract BaseTicTacGame.Move nextMove(BaseTicTacGame currGame);
		// public static  String getRoboName() { return roboName; }
		//public abstract void setSym(BaseTicTacGame.PlyrSym sym) ; 
	}
	
	@FunctionalInterface
  static protected interface IRobotFact {
	  public abstract IRobot newRobot(PlyrSym sym);
	
	}
	
	static sealed private abstract class BaseRobot {
		protected PlyrSym sym;
		//public BaseRobot() { }
		public BaseRobot(PlyrSym sym) { this.sym=sym; }
	}
	
	static private final class RandoRobot extends BaseRobot implements IRobot {
		public final static String roboName ="Randy";

		@Override
		public Move nextMove(BaseTicTacGame currGame) {
			var mvs= currGame.getMoves(sym);
			int n = mvs.size();
			int choice = (int)(Math.random()*n);
			List<Move> al = new ArrayList<>(mvs);
			return al.get(choice);
		}
		// public RandoRobot() { super(); }
		public RandoRobot(PlyrSym sym) { super(sym); }
	}
	
	static sealed private abstract class WRandRobot extends BaseRobot {
		protected Map<BaseTicTacGame.SqrLoc, Long> basicWeights =
				new HashMap<>();
		private final Random random = new Random();
		protected void initBasicWeights() {
			var locs = BaseTicTacGame.getAllLocs();
			for (var l : locs) {
				if (l.isCenter()) {
					basicWeights.put(l, getRandWeightCenter()); }
				else if (l.isCorner()) {
					basicWeights.put(l, getRandWeightCorner()); }
				else if (l.isSide()) {
					basicWeights.put(l, getRandWeightSide()); }
			}
			
		}
		// public WRandRobot() { super();initBasicWeights(); }

		public WRandRobot(PlyrSym sym) {
			super(sym);
			initBasicWeights();
		}
		public long getAdjustWeight(BaseTicTacGame currGame,
				BaseTicTacGame.SqrLoc sl) {
			return 0;
			
		}

		public Move nextMove(BaseTicTacGame currGame) {
			//var mvs= currGame.getMoves(sym);
			//int n = mvs.size(); 
			Map<BaseTicTacGame.SqrLoc, Long> newWeights =
					new HashMap<>();
			for (var w: basicWeights.entrySet()) {
				var k= w.getKey();
				var v = w.getValue();
				var sym=currGame.getSymAtLoc(k);
				if (sym.isMarked()) {
					newWeights.put(k, v + getAdjustWeight(currGame, k));
				}
			}
			var wrnd = new WeightedRandom<SqrLoc>();
			//wrnd.add(newWeights);
			var newLoc = wrnd.sample();
			return new Move(sym,newLoc.x(), newLoc.y());
			//return null;
		}
		
		public long getBaseWeightCenter() { return 24; }
		public long getBaseWeightCorner() { return 24; }
		public long getBaseWeightSide()   { return 24; }
		public long getVaryWeightCenter() { return 31; }
		public long getVaryWeightCorner() { return 29; }
		public long getVaryWeightSide()   { return 24; }
		
		public long getRandWeightCenter() {
			return getBaseWeightCenter() + random.nextLong(getVaryWeightCenter()); }
		public long getRandWeightCorner() {
			return getBaseWeightCorner() + random.nextLong(getVaryWeightCorner()); }
		public long getRandWeightSide()   {
			return getBaseWeightSide() + random.nextLong(getVaryWeightSide()); }
		
	}
	
	static private final class BlocksRobot extends WRandRobot implements IRobot {

		public final static String roboName ="Bob";

		//public BlocksRobot( ) { super( ); }
		public BlocksRobot(PlyrSym sym) { super(sym); }
		@Override
		public long getBaseWeightCenter() { return 36; }
		@Override
		public long getBaseWeightCorner() { return 36; }
		@Override
		public long getBaseWeightSide() { return 36; }
		@Override
		public long getAdjustWeight(BaseTicTacGame currGame, SqrLoc sl) {
			// TODO Auto-generated method stub
			// add something to block other from winning
			return super.getAdjustWeight(currGame, sl);
		}
		


	}
	
	static private final class ExpertRobot extends WRandRobot implements IRobot { 
		public final static String roboName ="Emily";

		//public ExpertRobot( ) { super( ); }
		public ExpertRobot(PlyrSym sym) { super(sym); }

		@Override
		public long getAdjustWeight(BaseTicTacGame currGame, SqrLoc sl) {
			// TODO Auto-generated method stub
			return super.getAdjustWeight(currGame, sl);
		}

		/**
		 * 
		 */
		

		 

	}

 
	/*
	 * Random Randy - Begin
	 * Blocks Bob - intermediate
	 * Experienced Emily
	 */
	static public enum Robo {
		Randy(RandoRobot.roboName, (s) -> new RandoRobot(s)),
		Bob(BlocksRobot.roboName, (s) -> new BlocksRobot(s)),
		Emily(ExpertRobot.roboName, (s) -> new ExpertRobot(s));
		private String name;
		private IRobotFact factory;
		private Robo(String name, IRobotFact factory ) {
			this.name =name;
			this.factory = factory;
		}
		public String getName() {return this.name;}
		public IRobot newRobot(PlyrSym sym) {
		  return this.factory.newRobot(sym); }
	}
	
	public static Robo getRoboByName(String name) {
	  //Robo.valueOf(name);
		for (var r : Robo.values()) {
			if (r.name.equals(name)) return r;
		}
		return null;
	}

	public static IRobot newRoboByName(String name, PlyrSym sym) {
		return switch (name ) {
			case RandoRobot.roboName -> new RandoRobot(sym);
			case BlocksRobot.roboName -> new BlocksRobot(sym);
			case ExpertRobot.roboName -> new ExpertRobot(sym);
			// default -> throw new IllegalArgumentException("Unexpected value: " + name);
			default -> null;
		};
	}
	public static List<String> getRoboNames() {
		var ret = new ArrayList<String>();
		for (var r : robots) { ret.add(r.name); }
		return ret;
	}

	 

}
