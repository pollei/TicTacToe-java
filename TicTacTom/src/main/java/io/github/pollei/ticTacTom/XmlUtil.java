/**
 * 
 */
package io.github.pollei.ticTacTom;

import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource; 
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Steve_Pollei
 *
 */
public class XmlUtil {

  /**
   * 
   */
  public XmlUtil() { }
  static Document newDoc() throws ParserConfigurationException {
    var dbf = DocumentBuilderFactory.newInstance();
      var docBld = dbf.newDocumentBuilder();
      var doc = docBld.newDocument();
      return doc;
  }
  static void toWriter(Document doc, Writer w) throws TransformerException {
    var transFact = TransformerFactory.newInstance(); 
    var trans = transFact.newTransformer();
    var dSrc = new DOMSource(doc);
    var consResult = new StreamResult(w);
    trans.transform(dSrc, consResult);
  }
  static String toString(Document doc) throws TransformerException {
    var strWrite = new StringWriter();
    toWriter(doc, strWrite);
    return strWrite.toString();
    
  }
  static String toString0(Document doc) {
    var transFact = TransformerFactory.newInstance();
    String ret=null;
    try {
      var trans = transFact.newTransformer();
      var dSrc = new DOMSource(doc);
      var strWrite = new StringWriter();
      var consResult = new StreamResult(strWrite);
      trans.transform(dSrc, consResult);
      ret=strWrite.toString();
    } catch (TransformerException e) { }
    return ret;
  }

}
