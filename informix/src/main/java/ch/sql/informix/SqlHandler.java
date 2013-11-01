package ch.sql.informix;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SqlHandler {
    @Autowired
    private SqlInformix dbh;
    @Autowired
    private SqlMapper mapper;

    public void setDb(String db) {
	SqlInformixImpl.db = db;
    }

    public String getTables() {
	return dbh.getTables();
    }

    public String getColumns(String tab) {
	return dbh.getColumns(tab);
    }

    public String getRows(String tab, int page, int rows) {
	return dbh.getRows(tab, page, rows);
    }

    public String getRowsFilter(String tab, String col, String val, int page, int rows) {
	return dbh.getRowsFilter(tab, col, val, page, rows);
    }

    public String getQuery(String sql, int page, int rows) {
	return dbh.getQuery(sql, page, rows);
    }

    public String getQueryFilter(String sql, String col, String val, int page, int rows) {
	return dbh.getQueryFilter(sql, col, val, page, rows);
    }

    public String sqlExecute(String sql) {
	return dbh.sqlExecute(sql);
    }

    public boolean isMultipartContent(HttpServletRequest req) {
	return mapper.isMultipartContent(req);
    }

    public String postVilFile(HttpServletRequest req, String vilFile) {
	return mapper.postVilFile(req, vilFile);
    }

    public String postVilClass(String clz) {
	return mapper.postVilClass(clz);
    }

    public String postVilRefProp(ServletContext context, String vilRefProp) {
	return mapper.postVilRefProp(context, vilRefProp);
    }

    public String postVilRefJson(ServletContext context, String vilRefJson) {
	return mapper.postVilRefJson(context, vilRefJson);
    }

    public void saveVilRefProp(ServletContext context, String vilRefProp, String map) {
	mapper.saveVilRefProp(context, vilRefProp, map);
    }

    public void saveVilRefJson(ServletContext context, String vilRefJson, String map) {
	mapper.saveVilRefJson(context, vilRefJson, map);
    }

    public String postVilFieldJson(ServletContext context, String vilStr) {
	return mapper.postVilFieldJson(context, vilStr);
    }

    public void saveVilFieldJson(ServletContext context, String vilStr, String map) {
	mapper.saveVilFieldJson(context, vilStr, map);
    }
}