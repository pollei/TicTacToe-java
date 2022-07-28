# TicTacToe-java

TicTacToe game for [SFCC CS141](https://pollei.github.io/sfcc-cyber-bas/CS141/ "SFCC CS&141 Computer Science I Java")

Has three UI:

- text only console
- GUI using swing
- web using Jakarta servlets run by TomCat

Source code is split between two sub-projects TicTac and TicTacTom

- TicTac has io.github.pollei.ticTac package with four classes
  - BaseTicTacGame has core of the game
  - RobotFactory has several nemesis available
    - Randy is equal probability to randomly choose an empty square
    - Bob has weighted probability that favors blocking and winning
    - Emily has weighted probability that should make her an expert **TODO**
  - ConsoleGame
  - SwingGame
- TicTacTom has io.github.pollei.ticTacTom package
  - WebGame is a HttpServlet **TODO**
  - XmlUtil **TODO**
  - RobotList is a HttpServlet that reports available foes
  - ttt.html **TODO**
  - more to come to support xml serialization, concurrency, and other aspects **TODO**

The project uses:

- [Java SE 17](https://docs.oracle.com/en/java/javase/17/)
- [Jakarta Servlet 5.0](https://jakarta.ee/specifications/servlet/5.0/) running on [Apache Tomcat 10](https://tomcat.apache.org/tomcat-10.0-doc/)
- [Eclipse IDE](https://eclipseide.org) for Enterprise Java and Web Developement
- [Apache Ant](https://ant.apache.org/)
- [Sqlite JDBC](https://github.com/xerial/sqlite-jdbc) [version 3.36](https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.36.0.3/)
