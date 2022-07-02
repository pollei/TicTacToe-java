/**
 * 
 */
package io.github.pollei.ticTac;

import java.util.Scanner;
import java.util.regex.Pattern;



/**
 * @author Steve_Pollei
 *
 */

// java -cp myjar.jar MyClass
// java -cp myjar.jar OtherMainClass

public class ConsoleGame  {
  private BaseTicTacGame game;
  private static Scanner inScan = new Scanner(System.in);
  private static Pattern delim = Pattern.compile("(?:\\p{javaWhitespace}|[,;])+");
  
  public void playGame() {
    game = new BaseTicTacGame();
    inScan.useDelimiter(delim);
    while (true) {
      /* make moves */ 
      throw new Error("TODO nothing implemented yet TODO");
    }
  }

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//  Auto-generated method stub
		System.out.println("burp"); 
		var cg = new ConsoleGame();
		cg.playGame();
	}

}
