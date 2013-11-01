package ch.sql.informix;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class SqlMapperImpl implements SqlMapper {

    private void closeCon(Closeable con) {
	if (con != null)
	    try {
		con.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
    }

    @Override
    public boolean isMultipartContent(HttpServletRequest req) {
	return ServletFileUpload.isMultipartContent(req);
    }

    @Override
    public String postVilClass(String clz) {
	return new JSONArray(getVilClassType(clz)).toString();
    }

    @Override
    public String postVilFile(HttpServletRequest req, String vilStr) {
	return new JSONObject(getVilFile(req, vilStr)).toString();
    }

    @Override
    public String postVilRefProp(ServletContext context, String vilStr) {
	return new JSONObject(getVilRefProp(context, vilStr)).toString();
    }

    @Override
    public String postVilRefJson(ServletContext context, String vilStr) {
	return new JSONObject(getVilRefJson(context, vilStr)).toString();
    }

    @Override
    public List<String> getVilClass(String clz) {
	List<String> list = new ArrayList<String>();
	try {
	    for (PropertyDescriptor pd : Introspector.getBeanInfo(Class.forName(clz)).getPropertyDescriptors())
		if (pd.getReadMethod() != null && !"class".equals(pd.getName()))
		    list.add(pd.getName());
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	} catch (IntrospectionException e) {
	    e.printStackTrace();
	}
	return list;
    }

    @Override
    public List<Map<String, String>> getVilClassType(String clz) {
	List<Map<String, String>> list = new ArrayList<Map<String, String>>();
	try {
	    for (PropertyDescriptor pd : Introspector.getBeanInfo(Class.forName(clz)).getPropertyDescriptors())
		if (pd.getReadMethod() != null && !"class".equals(pd.getName())) {
		    Map<String, String> map = new HashMap<String, String>();
		    map.put("refName", pd.getName());
		    map.put("refType", pd.getPropertyType().getSimpleName());
		    list.add(map);
		}
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	} catch (IntrospectionException e) {
	    e.printStackTrace();
	}
	return list;
    }

    @Override
    public Map<String, String> getVilFile(HttpServletRequest req, String vilStr) {
	Map<String, String> vilFile = new HashMap<String, String>();
	try {
	    FileItemIterator iterator = new ServletFileUpload().getItemIterator(req);
	    while (iterator.hasNext()) {
		FileItemStream item = iterator.next();
		if (!item.isFormField() && item.getFieldName().equals(vilStr)) {
		    BufferedReader br = new BufferedReader(new InputStreamReader(item.openStream(), "utf-8"));
		    StringBuffer sb = new StringBuffer();
		    String s = null;
		    while ((s = br.readLine()) != null)
			sb.append(s + "\n");
		    Matcher matcher = Pattern.compile(pattern).matcher(sb.toString());
		    while (matcher.find())
			for (int i = 1; i <= matcher.groupCount(); i += 2)
			    if (!matcher.group(i).isEmpty())
				vilFile.put(matcher.group(i + 1), matcher.group(i));
		}
	    }
	} catch (FileUploadException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return vilFile;
    }

    @Override
    public Map<String, Map<String, Map<String, StringBuffer>>> getVilRefProp(ServletContext context, String vilStr) {
	Map<String, Map<String, Map<String, StringBuffer>>> vilRef = new HashMap<String, Map<String, Map<String, StringBuffer>>>();
	BufferedReader br = null;
	try {
	    br = new BufferedReader(new InputStreamReader(context.getResourceAsStream("/" + vilStr), "utf-8"));
	    String s = null;
	    while ((s = br.readLine()) != null) {
		String[] s1 = s.split(";");
		if (vilRef.get(s1[0]) == null)
		    vilRef.put(s1[0], new HashMap<String, Map<String, StringBuffer>>());
		if (vilRef.get(s1[0]).get(s1[1]) == null)
		    vilRef.get(s1[0]).put(s1[1], new HashMap<String, StringBuffer>());
		vilRef.get(s1[0]).get(s1[1]).put("desc", new StringBuffer((s1.length > 2 ? s1[2] : "")));
		if (vilRef.get(s1[0]).get(s1[1]).get("ref") == null)
		    vilRef.get(s1[0]).get(s1[1]).put("ref", new StringBuffer());
		vilRef.get(s1[0]).get(s1[1]).get("ref").append(s1.length > 3 ? s1[3] : "");
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	    closeCon(br);
	}
	return vilRef;
    }

    @Override
    public Map<String, Map<String, Map<String, StringBuffer>>> getVilRefJson(ServletContext context, String vilStr) {
	Map<String, Map<String, Map<String, StringBuffer>>> vilRef = new HashMap<String, Map<String, Map<String, StringBuffer>>>();
	BufferedReader br = null;
	try {
	    br = new BufferedReader(new InputStreamReader(context.getResourceAsStream("/" + vilStr), "utf-8"));
	    StringBuffer sb = new StringBuffer();
	    String s = null;
	    while ((s = br.readLine()) != null) {
		sb.append(s);
	    }
	    if (sb.length() > 0) {
		JSONObject jobj = new JSONObject(sb.toString());
		for (Iterator<String> it = jobj.keys(); it.hasNext();) {
		    String ref = it.next();
		    vilRef.put(ref, new HashMap<String, Map<String, StringBuffer>>());
		    JSONObject jobj1 = jobj.getJSONObject(ref);
		    for (Iterator<String> it1 = jobj1.keys(); it1.hasNext();) {
			String name = it1.next();
			vilRef.get(ref).put(name, new HashMap<String, StringBuffer>());
			vilRef.get(ref).get(name)
			        .put("desc", new StringBuffer(jobj1.getJSONObject(name).getString("desc")));
			vilRef.get(ref).get(name)
			        .put("ref", new StringBuffer(jobj1.getJSONObject(name).getString("ref")));
		    }
		}
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (JSONException e) {
	    e.printStackTrace();
	} finally {
	    closeCon(br);
	}
	return vilRef;
    }

    @Override
    public void updateVilRef(String map, Map<String, Map<String, Map<String, StringBuffer>>> vilRef) {
	try {
	    JSONObject jobj = new JSONObject(map);
	    for (Iterator<String> it = jobj.keys(); it.hasNext();) {
		String ref = it.next();
		if (vilRef.get(ref) == null)
		    vilRef.put(ref, new HashMap<String, Map<String, StringBuffer>>());
		JSONObject jobj1 = jobj.getJSONObject(ref);
		for (Iterator<String> it1 = jobj1.keys(); it1.hasNext();) {
		    String name = it1.next();
		    if (vilRef.get(ref).get(name) == null)
			vilRef.get(ref).put(name, new HashMap<String, StringBuffer>());
		    vilRef.get(ref).get(name)
			    .put("desc", new StringBuffer(jobj1.getJSONObject(name).getString("desc")));
		    if (vilRef.get(ref).get(name).get("ref") == null)
			vilRef.get(ref).get(name).put("ref", new StringBuffer());

		    StringBuffer refb = vilRef.get(ref).get(name).get("ref");
		    String[] refs = refb.toString().split(",");
		    String[] refed = jobj1.getJSONObject(name).getString("ref").split("\\.");

		    refb.setLength(0);
		    boolean t = true;
		    for (int i = 0; i < refs.length; i++)
			if (!refs[i].isEmpty())
			    if (refed.length > 0 && refed[0].equals(refs[i].split("\\.")[0])) {
				if (refed.length > 1 && !refed[1].isEmpty())
				    refb.append(refed[0] + "." + refed[1] + ",");
				t = false;
			    } else
				refb.append(refs[i] + ",");
		    if (t)
			if (refed.length > 1 && !refed[1].isEmpty())
			    refb.append(refed[0] + "." + refed[1] + ",");
		}
	    }
	} catch (JSONException e) {
	    e.printStackTrace();
	}
    }

    @Override
    public void saveVilRefProp(ServletContext context, String vilStr, String map) {
	Map<String, Map<String, Map<String, StringBuffer>>> vilRef = getVilRefProp(context, vilStr);
	updateVilRef(map, vilRef);
	PrintWriter pw = null;
	try {
	    pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(context.getRealPath("\\" + vilStr)),
		    "utf-8"));
	    for (String clz : vilRef.keySet())
		for (String name : vilRef.get(clz).keySet())
		    pw.println(clz + ";" + name + ";" + vilRef.get(clz).get(name).get("desc") + ";"
			    + vilRef.get(clz).get(name).get("ref") + ";");
	    pw.flush();
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	    closeCon(pw);
	}
    }

    @Override
    public void saveVilRefJson(ServletContext context, String vilStr, String map) {
	Map<String, Map<String, Map<String, StringBuffer>>> vilRef = getVilRefJson(context, vilStr);
	updateVilRef(map, vilRef);
	OutputStreamWriter osw = null;
	try {
	    osw = new OutputStreamWriter(new FileOutputStream(context.getRealPath("\\" + vilStr)), "utf-8");
	    osw.write(new JSONObject(vilRef).toString().replace("},", "},\n"));
	    osw.flush();
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	    closeCon(osw);
	}
    }

    @Override
    public String postVilFieldJson(ServletContext context, String vilStr) {
	return new JSONObject(getVilFieldJson(context, vilStr)).toString();
    }

    @Override
    public Map<String, List<Map<String, String>>> getVilFieldJson(ServletContext context, String vilStr) {
	Map<String, List<Map<String, String>>> field = new HashMap<String, List<Map<String, String>>>();
	BufferedReader br = null;
	try {
	    br = new BufferedReader(new InputStreamReader(context.getResourceAsStream("/" + vilStr), "utf-8"));
	    StringBuffer sb = new StringBuffer();
	    String s = null;
	    while ((s = br.readLine()) != null)
		sb.append(s);
	    if (sb.length() > 0) {
		JSONObject jobj = new JSONObject(sb.toString());
		for (Iterator<String> it = jobj.keys(); it.hasNext();) {
		    String ref = it.next();
		    field.put(ref, new ArrayList<Map<String, String>>());
		    JSONArray jobj1 = jobj.getJSONArray(ref);
		    for (int i = 0; i < jobj1.length(); i++) {
			JSONObject jobj2 = jobj1.getJSONObject(i);
			Map<String, String> map = new HashMap<String, String>();
			for (Iterator<String> it1 = jobj2.keys(); it1.hasNext();) {
			    String name = it1.next();
			    map.put(name, jobj2.getString(name));
			}
			field.get(ref).add(map);
		    }
		}
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (JSONException e) {
	    e.printStackTrace();
	} finally {
	    closeCon(br);
	}
	return field;
    }

    @Override
    public void saveVilFieldJson(ServletContext context, String vilStr, String map) {
	Map<String, List<Map<String, String>>> field = getVilFieldJson(context, vilStr);
	try {
	    JSONObject jobj = new JSONObject(map);
	    for (Iterator<String> it = jobj.keys(); it.hasNext();) {
		String ref = it.next();
		field.put(ref, new ArrayList<Map<String, String>>());
		JSONArray jobj1 = jobj.getJSONArray(ref);
		for (int i = 0; i < jobj1.length(); i++) {
		    JSONObject jobj2 = jobj1.getJSONObject(i);
		    Map<String, String> map1 = new HashMap<String, String>();
		    for (Iterator<String> it1 = jobj2.keys(); it1.hasNext();) {
			String name = it1.next();
			map1.put(name, jobj2.getString(name));
		    }
		    field.get(ref).add(map1);
		}
	    }
	} catch (JSONException e) {
	    e.printStackTrace();
	}
	OutputStreamWriter osw = null;
	try {
	    osw = new OutputStreamWriter(new FileOutputStream(context.getRealPath("\\" + vilStr)), "utf-8");
	    osw.write(new JSONObject(field).toString().replace("},", "},\n"));
	    osw.flush();
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	    closeCon(osw);
	}
    }
}