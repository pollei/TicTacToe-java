/**
 * 
 */
package io.github.pollei.ticTac;

import java.io.PrintStream;
import java.util.Scanner;
import java.util.regex.Pattern;

import io.github.pollei.ticTac.BaseTicTacGame.Move;
import io.github.pollei.ticTac.BaseTicTacGame.PlyrSym;
import io.github.pollei.ticTac.RobotFactory.Robo;



/**
 * @author Steve_Pollei
 *
 */

// java -cp myjar.jar MyClass
// java -cp myjar.jar OtherMainClass

public final class ConsoleGame  {
  private BaseTicTacGame game;
  private static Scanner inScan = new Scanner(System.in);
  private static Pattern delim = Pattern.compile("(?:\\p{javaWhitespace}|[,;])+");
  
  private void outputGameBoard(PrintStream os) {
    os.format("%n%c%c%c%n%c%c%c%n%c%c%c%n",
        game.getSymAtXY(0, 0).toChar('1'),
        game.getSymAtXY(1, 0).toChar('2'),
        game.getSymAtXY(2, 0).toChar('3'),
        game.getSymAtXY(0, 1).toChar('4'),
        game.getSymAtXY(1, 1).toChar('5'),
        game.getSymAtXY(2, 1).toChar('6'),
        game.getSymAtXY(0, 2).toChar('7'),
        game.getSymAtXY(1, 2).toChar('8'),
        game.getSymAtXY(2, 2).toChar('9') );
    
  }
  private void outputGameBoard( ) { outputGameBoard(System.out); }
  
  private PlyrSym getHumanSym() {
    Pattern symChoicePat = Pattern.compile("[xXoO12]");
    while (true) {
      System.out.print("pick X or O : ");
      if (!inScan.hasNext(symChoicePat)) {
        inScan.nextLine();
        continue;
      }
      var symCh = inScan.next(symChoicePat);
      return switch (symCh) {
        case "X", "x", "1" ->  PlyrSym.X;
        case "O", "o", "2" ->  PlyrSym.O;
        default -> PlyrSym.O; };
    }
  }
  private int getOrder() {
    Pattern ordrChoicePat = Pattern.compile("[FL12]|first|last", Pattern.CASE_INSENSITIVE  );
    while (true) {
      System.out.print("Go First[1] or Last[2] : ");
      if (!inScan.hasNext(ordrChoicePat)) {
        inScan.nextLine();
        continue;
      }
      var ordrStr = inScan.next(ordrChoicePat);
      return switch (ordrStr.toLowerCase()) {
        case "f", "1", "first" -> 0;
        case "l", "2", "last" -> 1;
        default -> 0; };
    }
  }
  private RobotFactory.Robo getNemesis() {
    // TODO let human select Bob or Emily as well
    return Robo.Randy;
  }
  
  private void doHumanMove() {
    var currSym = game.getCurrPlayer().sym;
    while (true) {
      System.out.print("pick an empty squre with a number between 1 and 9: ");
      if (!inScan.hasNextInt()) {
        System.out.println("needs to be a number between 1 and 9");
        inScan.nextLine();
        continue;
      }
      var n = inScan.nextInt();
      if (n < 1 || n>9) continue;
      int x = (n - 1) % 3;
      int y = (n - 1) / 3;
      if (game.getSymAtXY(x, y).isMarked()) continue;
      game.doMove(new Move(currSym,x,y));
      return;
    }
    
  }
  
  private void playGame() {
    game = new BaseTicTacGame();
    inScan.useDelimiter(delim);
    var hSym = getHumanSym();
    var hOrder = getOrder();
    var nemesis = getNemesis();
    game.setHumanPlayerHVC(hOrder, hSym, nemesis );
    game.doComputerTurn();
    while (! game.isDone()) {
      outputGameBoard();
      doHumanMove();
      game.doComputerTurn();
    }
    var winPlayer = game.getWinner();
    if (winPlayer != null) {
      System.out.format("%c has won %n", winPlayer.sym.toChar());
    } else {
      System.out.println("it was a tie ");
    }
  }

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		var cg = new ConsoleGame();
		cg.playGame();
	}

}
