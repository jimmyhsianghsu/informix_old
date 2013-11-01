package ch.sql.informix;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public interface SqlMapper {
    String pattern = " */\\*\\* *\\r?\\n *\\* *(.+) *\\r?\\n *\\*/\\r?\\n *private *\\w* *(.+);";

    boolean isMultipartContent(HttpServletRequest req);

    String postVilClass(String clz);

    String postVilFile(HttpServletRequest req, String vilStr);

    String postVilRefProp(ServletContext context, String vilStr);

    String postVilRefJson(ServletContext context, String vilStr);

    List<String> getVilClass(String clz);

    List<Map<String, String>> getVilClassType(String clz);

    Map<String, String> getVilFile(HttpServletRequest req, String vilStr);

    Map<String, Map<String, Map<String, StringBuffer>>> getVilRefProp(ServletContext context, String vilStr);

    Map<String, Map<String, Map<String, StringBuffer>>> getVilRefJson(ServletContext context, String vilStr);

    void updateVilRef(String map, Map<String, Map<String, Map<String, StringBuffer>>> vilRef);

    void saveVilRefProp(ServletContext context, String vilStr, String map);

    void saveVilRefJson(ServletContext context, String vilStr, String map);

    String postVilFieldJson(ServletContext context, String vilStr);

    Map<String, List<Map<String, String>>> getVilFieldJson(ServletContext context, String vilStr);

    void saveVilFieldJson(ServletContext context, String vilStr, String map);
}