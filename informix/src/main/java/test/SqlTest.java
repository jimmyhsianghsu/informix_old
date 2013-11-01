package test;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import ch.sql.informix.SqlHandler;

import java.io.CharArrayWriter;
import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
public class SqlTest extends HttpServlet {
    @Autowired
    private SqlHandler handler;

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
	res.setContentType("text/html; charset=utf8");
	String db = req.getParameter("db");
	String action = req.getParameter("action");
	String tab = req.getParameter("tab");
	String col = req.getParameter("col");
	String val = req.getParameter("val");
	String sql = req.getParameter("sql");
	Integer page = null;
	Integer rows = null;
	if (req.getParameter("page") != null)
	    page = Integer.valueOf(req.getParameter("page"));
	if (req.getParameter("rows") != null)
	    rows = Integer.valueOf(req.getParameter("rows"));

	handler.setDb(db);
	if ("getTables".equals(action))
	    res.getWriter().println(handler.getTables());
	else if ("getColumns".equals(action))
	    res.getWriter().println(handler.getColumns(tab));
	else if ("getRows".equals(action))
	    res.getWriter().println(handler.getRows(tab, page, rows));
	else if ("getRowsFilter".equals(action))
	    res.getWriter().println(handler.getRowsFilter(tab, col, val, page, rows));
	else if ("getQuery".equals(action))
	    res.getWriter().println(handler.getQuery(sql, page, rows));
	else if ("getQueryFilter".equals(action))
	    res.getWriter().println(handler.getQueryFilter(sql, col, val, page, rows));
	else if ("getPage".equals(action))
		res.getWriter().print(getPage("/META-INF/page/_tab_informix.1028(0415).html"));
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
	req.setCharacterEncoding("utf8");
	res.setContentType("text/html; charset=utf8");
	String db = req.getParameter("db");
	String action = req.getParameter("action");
	String vilFile = "vilFile";
	String clz = req.getParameter("clz");
	String vilRefProp = "vilRefProp.txt";
	String vilRefJson = "vilRefJson.txt";
	String vilFieldJson = "vilFieldJson.txt";
	String map = req.getParameter("map");
	String sql = req.getParameter("sql");

	handler.setDb(db);
	if (handler.isMultipartContent(req))
	    res.getWriter().println(handler.postVilFile(req, vilFile));
	else if ("getClass".equals(action))
	    res.getWriter().println(handler.postVilClass(clz));
	else if ("getVilRefProp".equals(action))
	    res.getWriter().println(handler.postVilRefProp(getServletContext(), vilRefProp));
	else if ("getVilRefJson".equals(action))
	    res.getWriter().println(handler.postVilRefJson(getServletContext(), vilRefJson));
	else if ("saveVilRefProp".equals(action))
	    handler.saveVilRefProp(getServletContext(), vilRefProp, map);
	else if ("saveVilRefJson".equals(action))
	    handler.saveVilRefJson(getServletContext(), vilRefJson, map);
	else if ("sqlExecute".equals(action))
	    res.getWriter().println(handler.sqlExecute(sql));
	else if ("getVilFieldJson".equals(action))
	    res.getWriter().println(handler.postVilFieldJson(getServletContext(), vilFieldJson));
	else if ("saveVilFieldJson".equals(action))
	    handler.saveVilFieldJson(getServletContext(), vilFieldJson, map);
    }

    public void init(ServletConfig config) {
	try {
	    super.init(config);
	} catch (ServletException e) {
	    e.printStackTrace();
	}
	SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }
	private String getPage(String path) {
	BufferedReader br = null;
	CharArrayWriter caw = new CharArrayWriter();
	try {
	    br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(path), "utf8"));
	    char[] c = new char[1024];
	    int n = 0;
	    while ((n = br.read(c)) != -1)
		caw.write(c, 0, n);
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}finally{if(br != null)try{br.close();}catch(IOException e){e.printStackTrace();}}
	return new String(caw.toCharArray());
    }
}