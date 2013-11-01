package ch.sql.informix;

public interface SqlInformix {
    String driver = "com.informix.jdbc.IfxDriver";
    String sqlTables = "select tabname from systables where locklevel='P' order by tabname";
    String sqlColumns = "select t.tabname,m.colname,ch_pk(t.tabname,m.colname) as pk,ch_fk(t.tabname,m.colname) as fk,ch_ptab(t.tabname,m.colname) as ptab,ch_pcol(t.tabname,m.colname) as pcol,m.colno,case m.coltype when 0 then 'CHAR' when 1 then 'SMALLINT' when 2 then 'INTEGER' when 3 then 'FLOAT' when 4 then 'SMALLFLOAT' when 5 then 'DECIMAL' when 6 then 'SERIA' when 7 then 'DATE' when 8 then 'MONEY' when 9 then 'NULL' when 10 then 'DATETIME' when 11 then 'BYTE' when 12 then 'TEXT' when 13 then 'VARCHAR' when 14 then 'INTERVAL' when 15 then 'NCHAR' when 16 then 'NVARCHAR' when 17 then 'INT8' when 18 then 'SERIAL8' when 19 then 'SET' when 20 then 'MULTISET' when 21 then 'LIST' when 22 then 'Unnamed ROW' when 40 then 'Variable-length opaque type' when 4118 then 'Named ROW' else to_char(m.coltype) end as type,m.collength from syscolumns m left outer join systables t on t.tabid=m.tabid where t.tabname=? order by colno,pk desc,fk desc,m.colno";

    String getTables();

    String getColumns(String tab);

    String getRows(String tab, int page, int rows);

    String getRowsFilter(String tab, String col, String val, int page, int rows);

    String getQuery(String sql, int page, int rows);

    String getQueryFilter(String sql, String col, String val, int page, int rows);

    String sqlExecute(String sql);
}