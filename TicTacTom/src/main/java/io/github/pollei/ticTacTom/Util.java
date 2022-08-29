/**
 * 
 */
package io.github.pollei.ticTacTom;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.function.Function;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource; 
import javax.xml.transform.stream.StreamResult;

import org.apache.catalina.users.DataSourceUserDatabase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Steve_Pollei
 *
 */
final public class Util {

  /**
   * 
   */
  public Util() { }
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
  static void toResponse( Document doc, HttpServletResponse resp) 
      throws TransformerException, IOException {
    resp.setContentType("application/xml;charset=UTF-8");
    toWriter(doc, resp.getWriter());
  }
  public static Connection getUserDbConnection()
      throws NamingException, SQLException {
    var iCntx = new InitialContext();
    if (iCntx.lookup("java:/comp/env") instanceof Context envCntx) {
      if (envCntx.lookup("jdbc/tttUserDB") instanceof DataSource ds) {
        return ds.getConnection();
      }
    }
    return null;
  }
  public static DataSourceUserDatabase getUserDatasource()
      throws NamingException {
    var iCntx = new InitialContext();
    if (iCntx.lookup("java:/comp/env") instanceof Context envCntx) {
      if (envCntx.lookup("tttUserDatabase") instanceof DataSourceUserDatabase ds) {
        return ds ;
      }
    }
    return null;
  }
  interface TrackTTL {
    void touch();
    long lastTouch();
  }
  static class LruCache<K,V extends TrackTTL> {
    LinkedHashMap<K,V> map = new LinkedHashMap<K,V>();
    Function<K,V> newV;
    int [] times;

    /**
     * @param map
     */
    public LruCache(int [] times, Function<K,V> newV) { 
      this.times = times;
    }
    public LruCache(Function<K,V> newV) {
      this(new int [] {21500,24500, 49500}, newV );}
    boolean isStale(V v) {
      var sz=map.size();
      long delta = System.currentTimeMillis() - v.lastTouch();
      if (sz>24 && delta > this.times[0]) return true;
      if (sz>5 && delta > this.times[1]) return true;
      if ( delta > this.times[2]) return true;
      return false;
    }
    boolean dropStale() {
      var old = map.entrySet().iterator().next();
      if (isStale(old.getValue())) {
        map.remove(old.getKey());
        return true;
      }
      return false;
    }
    void dropStales(int n) {
      for(int i =0; i<n; i++) {
        if (!dropStale()) return;
      }
    }
    V get(K k) { return map.get(k); }
    V touch(K k) {
      V ret=map.remove(k);
      if (null != ret) {
        map.put(k, ret);
        ret.touch();
        dropStale();
        return ret;
      }
      dropStale();
      return map.computeIfAbsent(k, newV);
    }
    @SuppressWarnings("unchecked")
    V [] getValuesArray() { return (V[]) map.values().toArray(); }
    Collection<V> getValuesCollection() { return map.values(); }
  }
}
