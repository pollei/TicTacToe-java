package io.github.pollei.ticTacTom;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource; 
import javax.xml.transform.stream.StreamResult;

import io.github.pollei.ticTac.RobotFactory;
import io.github.pollei.ticTac.RobotFactory.Robo;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Servlet implementation class RobotList
 */
@WebServlet(
    name="RobotList",
    urlPatterns = {"/RobotList",  "/robot-list" },
    description = "Query the list of robots nemesis for TicTacToe over http/https" )
public class RobotListSrv extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String xmlStr  = getXmlStr();
	
	// static {xmlStr = getXmlStr();}
     
	private static String getXmlStr() {
    var dbf = DocumentBuilderFactory.newInstance();
    String ret = null;
    try {
      var docBld = dbf.newDocumentBuilder();
      var doc = docBld.newDocument();
      var rlNod = doc.createElement("robolist");
      //rlNod.setAttribute("sym", "x");
      // doc.getDocumentElement().appendChild(doc);
      for (var r:  Robo.values()) {
        var robNod = doc.createElement("player");
        robNod.setAttribute("robo", r.name());
        rlNod.appendChild(robNod);
      }
      doc.appendChild(rlNod);
      //System.out.print(doc);
      var transFact = TransformerFactory.newInstance();
      var trans = transFact.newTransformer();
      var dSrc = new DOMSource(doc);
      var strWrite = new StringWriter();
      var consResult = new StreamResult(strWrite);
      trans.transform(dSrc, consResult);
      ret=strWrite.toString();
      
    } catch (Exception e) {
      //  Auto-generated catch block
      //throw new ServletException("xml barf", e);
      e.printStackTrace();
    }
	  return ret;
	}
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RobotListSrv() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	  response.addHeader("Content-Type", "application/xml;charset=UTF-8");
	  response.addHeader("Cache-Control", "max-age=9000, stale-while-revalidate=900");
	  // 9000 is 2.5 hours ; 900 is 15 minutes
	  // should very rarely change and server must reboot to do so
	  // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Cache-Control
	  response.getWriter().append(xmlStr);
		//response.getWriter().append("Served at: ").append(request.getContextPath());
	}

}
