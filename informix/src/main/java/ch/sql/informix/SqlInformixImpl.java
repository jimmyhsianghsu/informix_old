package ch.sql.informix;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class SqlInformixImpl implements SqlInformix {
    public static String db = "vil";
    private String url;
    private String user;
    private String pwd;

    static {
	try {
	    Class.forName(driver);
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	}
    }

    private Connection getConnection() {
	ResourceBundle rb = ResourceBundle.getBundle("/META-INF/bundle/db");
	this.url = rb.getString(db);
	this.user = rb.getString("user");
	this.pwd = rb.getString("pwd");

	Connection con = null;
	try {
	    con = DriverManager.getConnection(url, user, pwd);
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return con;
    }

    private void closeCon(Connection con) {
	if (con != null)
	    try {
		con.close();
	    } catch (SQLException e) {
		e.printStackTrace();
	    }
    }

    @Override
    public String getTables() {
	JSONArray jArry = new JSONArray();
	Connection con = getConnection();
	try {
	    ResultSet rs = con.createStatement().executeQuery(sqlTables);
	    while (rs.next())
		jArry.put(rs.getString(1));
	} catch (SQLException se) {
	    se.printStackTrace();
	} finally {
	    closeCon(con);
	}
	return jArry.toString();
    }

    @Override
    public String getColumns(String tab) {
	JSONObject jobj = new JSONObject();
	Connection con = getConnection();

	String catalog = null;
	Matcher matcher = Pattern.compile("(mvdis_.+):INFORMIXSERVER").matcher(url);
	while (matcher.find())
	    catalog = matcher.group(1);

	try {
	    DatabaseMetaData dbmd = con.getMetaData();
	    ResultSet rsPk = dbmd.getPrimaryKeys(catalog, user, tab);
	    ResultSet rsFk = dbmd.getImportedKeys(catalog, user, tab);
	    ResultSet rsCol = dbmd.getColumns(catalog, user, tab, null);

	    Map<String, String> pkMap = new HashMap<String, String>();
	    Map<String, String[]> fkMap = new HashMap<String, String[]>();
	    while (rsPk.next())
		pkMap.put(rsPk.getString("COLUMN_NAME"), "PK");
	    while (rsFk.next())
		fkMap.put(rsFk.getString("FKCOLUMN_NAME"),
		        new String[] { "FK", rsFk.getString("PKTABLE_NAME"), rsFk.getString("PKCOLUMN_NAME") });

	    List<JSONObject> jArry = new ArrayList<JSONObject>();
	    while (rsCol.next()) {
		JSONObject obj = new JSONObject();
		obj.put("tabname", rsCol.getString("TABLE_NAME"));
		obj.put("colname", rsCol.getString("COLUMN_NAME"));
		obj.put("pk", pkMap.get(rsCol.getString("COLUMN_NAME")));
		if (fkMap.get(rsCol.getString("COLUMN_NAME")) != null) {
		    obj.put("fk", fkMap.get(rsCol.getString("COLUMN_NAME"))[0]);
		    obj.put("ptab", fkMap.get(rsCol.getString("COLUMN_NAME"))[1]);
		    obj.put("pcol", fkMap.get(rsCol.getString("COLUMN_NAME"))[2]);
		}
		obj.put("colno", rsCol.getString("ORDINAL_POSITION"));
		obj.put("type", rsCol.getString("TYPE_NAME"));
		obj.put("collength", rsCol.getString("COLUMN_SIZE"));
		jArry.add(obj);
	    }
	    Collections.sort(jArry, new Comparator<JSONObject>() {
		@Override
		public int compare(JSONObject o1, JSONObject o2) {
		    int i = 0;
		    try {
			i = o1.getInt("colno") - o2.getInt("colno");
		    } catch (JSONException e) {
			e.printStackTrace();
		    }
		    return i;
		}

	    });
	    jobj.put("rows", jArry);
	    jobj.put("columns", new String[] { "tabname", "colname", "pk", "fk", "ptab", "pcol", "colno", "type",
		    "collength" });
	} catch (JSONException je) {
	    je.printStackTrace();
	} catch (SQLException se) {
	    se.printStackTrace();
	} finally {
	    closeCon(con);
	}
	return jobj.toString();
    }

    @Override
    public String getRows(String tab, int page, int rows) {
	JSONObject jobj = new JSONObject();
	Connection con = getConnection();
	try {
	    ResultSet rs = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
		    .executeQuery("select * from " + tab);
	    ResultSetMetaData rsmd = rs.getMetaData();
	    rs.absolute((page - 1) * rows);
	    JSONArray jArry = new JSONArray();
	    for (int r = 0; r < rows && rs.next(); r++) {
		JSONObject obj = new JSONObject();
		for (int i = 1; i <= rsmd.getColumnCount(); i++)
		    obj.put(rsmd.getColumnLabel(i), rs.getString(i));
		jArry.put(obj);
	    }
	    List<String> columns = new ArrayList<String>();
	    for (int i = 1; i <= rsmd.getColumnCount(); i++)
		columns.add(rsmd.getColumnLabel(i));
	    rs = con.createStatement().executeQuery("select count(*) from " + tab);
	    rs.next();
	    jobj.put("rows", jArry);
	    jobj.put("columns", columns);
	    jobj.put("total", rs.getInt(1));
	} catch (JSONException je) {
	    je.printStackTrace();
	} catch (SQLException se) {
	    se.printStackTrace();
	} finally {
	    closeCon(con);
	}
	return jobj.toString();
    }

    @Override
    public String getRowsFilter(String tab, String col, String val, int page, int rows) {
	JSONObject jobj = new JSONObject();
	Connection con = getConnection();
	try {
	    PreparedStatement pstmt = con.prepareStatement("select * from " + tab + " where " + col + "=?",
		    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	    pstmt.setString(1, val);
	    ResultSet rs = pstmt.executeQuery();
	    ResultSetMetaData rsmd = rs.getMetaData();
	    rs.absolute((page - 1) * rows);
	    JSONArray jArry = new JSONArray();
	    for (int r = 0; r < rows && rs.next(); r++) {
		JSONObject obj = new JSONObject();
		for (int i = 1; i <= rsmd.getColumnCount(); i++)
		    obj.put(rsmd.getColumnLabel(i), rs.getString(i));
		jArry.put(obj);
	    }
	    List<String> columns = new ArrayList<String>();
	    for (int i = 1; i <= rsmd.getColumnCount(); i++)
		columns.add(rsmd.getColumnLabel(i));
	    pstmt = con.prepareStatement("select count(*) from " + tab + " where " + col + "=?");
	    pstmt.setString(1, val);
	    rs = pstmt.executeQuery();
	    rs.next();
	    jobj.put("rows", jArry);
	    jobj.put("columns", columns);
	    jobj.put("total", rs.getInt(1));
	} catch (JSONException je) {
	    je.printStackTrace();
	} catch (SQLException se) {
	    se.printStackTrace();
	} finally {
	    closeCon(con);
	}
	return jobj.toString();
    }

    @Override
    public String getQuery(String sql, int page, int rows) {
	JSONObject jobj = new JSONObject();
	Connection con = getConnection();
	try {
	    ResultSet rs = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
		    .executeQuery(sql);
	    ResultSetMetaData rsmd = rs.getMetaData();
	    rs.absolute((page - 1) * rows);
	    JSONArray jArry = new JSONArray();
	    for (int r = 0; r < rows && rs.next(); r++) {
		JSONObject obj = new JSONObject();
		for (int i = 1; i <= rsmd.getColumnCount(); i++)
		    obj.put(rsmd.getColumnLabel(i), rs.getString(i));
		jArry.put(obj);
	    }
	    List<String> columns = new ArrayList<String>();
	    for (int i = 1; i <= rsmd.getColumnCount(); i++)
		columns.add(rsmd.getColumnLabel(i));
	    rs = con.createStatement().executeQuery("select count(*) from(" + sql.replace(";", "") + ")");
	    rs.next();
	    jobj.put("rows", jArry);
	    jobj.put("columns", columns);
	    jobj.put("total", rs.getInt(1));
	} catch (JSONException je) {
	    je.printStackTrace();
	} catch (SQLException se) {
	    se.printStackTrace();
	    StringBuffer errMsg = new StringBuffer();
	    errMsg.append("SQL:\n" + sql + "\n\n");
	    do {
		errMsg.append("SQLState: " + se.getSQLState() + '\n');
		errMsg.append("ErrorCode: " + se.getErrorCode() + '\n');
		errMsg.append("Message: " + se.getMessage() + "\n\n");
	    } while ((se = se.getNextException()) != null);
	    try {
		jobj.put("errMsg", errMsg.toString());
		jobj.put("rows", new String[] {});
		jobj.put("total", -1);
	    } catch (JSONException e) {
		e.printStackTrace();
	    }
	} finally {
	    closeCon(con);
	}
	return jobj.toString();
    }

    @Override
    public String getQueryFilter(String sql, String col, String val, int page, int rows) {
	JSONObject jobj = new JSONObject();
	Connection con = getConnection();
	try {
	    PreparedStatement pstmt = con.prepareStatement("select * from(" + sql.replace(";", "") + ") where " + col
		    + "=?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	    pstmt.setString(1, val);
	    ResultSet rs = pstmt.executeQuery();
	    ResultSetMetaData rsmd = rs.getMetaData();
	    rs.absolute((page - 1) * rows);
	    JSONArray jArry = new JSONArray();
	    for (int r = 0; r < rows && rs.next(); r++) {
		JSONObject obj = new JSONObject();
		for (int i = 1; i <= rsmd.getColumnCount(); i++)
		    obj.put(rsmd.getColumnLabel(i), rs.getString(i));
		jArry.put(obj);
	    }
	    List<String> columns = new ArrayList<String>();
	    for (int i = 1; i <= rsmd.getColumnCount(); i++)
		columns.add(rsmd.getColumnLabel(i));
	    pstmt = con.prepareStatement("select count(*) from(select * from(" + sql.replace(";", "") + ") where "
		    + col + "=?)");
	    pstmt.setString(1, val);
	    rs = pstmt.executeQuery();
	    rs.next();
	    jobj.put("rows", jArry);
	    jobj.put("columns", columns);
	    jobj.put("total", rs.getInt(1));
	} catch (JSONException je) {
	    je.printStackTrace();
	} catch (SQLException se) {
	    se.printStackTrace();
	} finally {
	    closeCon(con);
	}
	return jobj.toString();
    }

    @Override
    public String sqlExecute(String sql) {
	JSONObject jobj = new JSONObject();
	JSONArray jSql = new JSONArray();
	JSONArray jSuccess = new JSONArray();
	JSONArray jFail = new JSONArray();
	Connection con = getConnection();
	try {
	    for (String s : sql.split(";")) {
		s = s.replaceAll(" *(\\n)|(\\r\\n) *", " ").trim() + ";";
		if (!s.isEmpty() && s.length() > 1)
		    try {
			Statement stmt = con.createStatement();
			if (stmt.execute(s))
			    jSuccess.put("0 SELECT ==> " + s);
			else
			    jSuccess.put(stmt.getUpdateCount()
				    + " "
				    + (s.toUpperCase().indexOf("INSERT") != -1 ? "INSERT" : s.toUpperCase().indexOf(
				            "UPDATE") != -1 ? "UPDATE"
				            : s.toUpperCase().indexOf("DELETE") != -1 ? "DELETE" : "") + " ==> " + s);
		    } catch (SQLException se) {
			se.printStackTrace();
			StringBuffer errMsg = new StringBuffer();
			errMsg.append("SQL:" + s + "\n");
			do {
			    errMsg.append("SQLState: " + se.getSQLState() + '\n');
			    errMsg.append("ErrorCode: " + se.getErrorCode() + '\n');
			    errMsg.append("Message: " + se.getMessage() + '\n');
			} while ((se = se.getNextException()) != null);
			jSql.put(s);
			jFail.put(errMsg.toString());
		    }
	    }
	} finally {
	    closeCon(con);
	}
	try {
	    jobj.put("jSql", jSql);
	    jobj.put("jSuccess", jSuccess);
	    jobj.put("jFail", jFail);
	} catch (JSONException je) {
	    je.printStackTrace();
	}
	return jobj.toString();
    }
}