package compare;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.CharArrayWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
public class Compare extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)throws ServletException,IOException {
		res.setContentType("text/html; charset=utf8");
		String action=req.getParameter("action");
		if("treeFile".equals(action))
			res.getWriter().print(treeFile());
		else if("getMap".equals(action))
			res.getWriter().print(getMap(getServletContext(),"mapFile.txt"));
		else if ("getPage".equals(action))
			res.getWriter().print(getPage("/META-INF/page/compare(10280448).html"));
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res)throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		res.setContentType("text/html; charset=utf8");
		String action=req.getParameter("action");
		String file=req.getParameter("file");
		if("getFile".equals(action))
			res.getWriter().print(getFile(file));
		else if("writeMap".equals(action))
			writeMap(req.getParameter("map"));
		else if("pasteFile1".equals(action))
			pasteFile(file,1);
		else if("pasteFile2".equals(action))
			pasteFile(file,2);
	}
	
	private String treeFile(){
		JSONArray jAry = new JSONArray();
		Map<String,String[]> map=mapFile(getServletContext(),"mapFile.txt");
		JSONObject obj = null;
		JSONArray ary = null;
		String root=null;
		int i=0;
		for(String s:map.keySet()){
			try {
				if(map.get(s).length==1){
					if(obj!=null && ary!=null){
						obj.put("children",ary);
						jAry.put(obj);
					}
					obj=new JSONObject();
					ary=new JSONArray();
					root=s+"["+map.get(s)[0]+"]";
					obj.put("id", i++);
					obj.put("text",root);
					obj.put("iconCls","icon-folder");
				}else if(map.get(s).length==2){
					JSONObject obj1=new JSONObject();
					obj1.put("id",i++);
					obj1.put("text",s);
					String path1=map.get(s)[0];
					String path2=map.get(s)[1];					
					obj1.put("iconCls",exist(path1,path2)?compare(path1,path2)?"icon-ok":"icon-no":"icon-cancel");
					obj1.put("root",root);
					obj1.put("file",s);
					obj1.put("path1",path1);
					obj1.put("path2",path2);
					obj1.put("exist1",new File(path1).exists());
					obj1.put("exist2",new File(path2).exists());
					obj1.put("compare",compare(path1,path2));
					ary.put(obj1);
				}
			} catch (JSONException je) {
				je.printStackTrace();
			}
		}
		return jAry.toString();
	}
	private Map<String,String[]> mapFile(ServletContext context,String root){
		Map<String,String[]> map = new LinkedHashMap<String,String[]>();
		BufferedReader br=null;
		try {
			br = new BufferedReader(new InputStreamReader(context.getResourceAsStream("/" + root),"utf-8"));
			String s = null;
			while ((s = br.readLine()) != null)
				if(!s.isEmpty())
					if(s.indexOf('\\')!=-1)
						map.put(s.substring(s.lastIndexOf('\\')+1,s.lastIndexOf('.')).replaceAll("[ \\.\\-\\(\\)]","_"),new String[]{s,br.readLine()});
					else
						map.put(s.substring(0,s.indexOf('.')),new String[]{s.substring(s.indexOf('.')+1)});
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{if(br!=null)try {br.close();} catch (IOException e) {e.printStackTrace();}}
		return map;
	}
	private String getFile(String file){
		Map<String,String[]> map=mapFile(getServletContext(),"mapFile.txt");
		if(map.get(file)!=null && map.get(file).length==2){
			JSONObject jObj = new JSONObject();
			try {
				jObj.put("file1",compareFile(map.get(file)[0]));
				jObj.put("file2",compareFile(map.get(file)[1]));
				jObj.put("exist1",new File(map.get(file)[0]).exists());
				jObj.put("exist2",new File(map.get(file)[1]).exists());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return jObj.toString();
		}
		return null;
	}
	private String compareFile(String path){
		StringBuffer sb = new StringBuffer();
		File file=new File(path);
		if(file.exists()){
			BufferedReader br=null;
			try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"utf-8"));
				String s=null;
				while((s=br.readLine())!=null)
					sb.append(s+'\n');
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				if(br!=null)
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
		return sb.toString();
	}
	private boolean compare(String path1,String path2){
		return compareFile(path1).equals(compareFile(path2));
	}
	private boolean exist(String path1,String path2){
		return new File(path1).exists() && new File(path2).exists();
	}
	private String getMap(ServletContext context,String root){
		BufferedReader br=null;
		StringBuffer sb=new StringBuffer();
		try {
			br = new BufferedReader(new InputStreamReader(context.getResourceAsStream("/" + root),"utf-8"));
			String s=null;
			while((s=br.readLine())!=null)
				sb.append(s+'\n');
		}catch(UnsupportedEncodingException e){e.printStackTrace();
		}catch(IOException e){e.printStackTrace();
		}finally{if(br!=null)try {br.close();} catch (IOException e) {e.printStackTrace();}}
		sb.setLength(sb.length()-1);
		return sb.toString();
	}
	private void writeMap(String map){
		String root="D:\\juno1\\workspace\\demo1\\src\\main\\webapp\\mapFile.txt";
		BufferedWriter bw=null;
		try {
			bw = new BufferedWriter(new FileWriter(root));
			bw.write(map,0,map.length());
			bw.flush();
		}catch(IOException e){e.printStackTrace();
		}finally{if(bw!=null)try {bw.close();} catch (IOException e) {e.printStackTrace();}}
	}
	private void pasteFile(String file,int m){
		Map<String,String[]> map=mapFile(getServletContext(),"mapFile.txt");
		String path1=map.get(file)[0];
		String path2=map.get(file)[1];
		if(m==1)pasteFile(path1,path2);
		else if(m==2)pasteFile(path2,path1);
	}
	private void pasteFile(String path1,String path2){
		BufferedInputStream bis=null;
		BufferedOutputStream bos=null;
		try {
			bis = new BufferedInputStream(new FileInputStream(path1));
			bos = new BufferedOutputStream(new FileOutputStream(path2));
			byte[] b=new byte[1024];
			int n=0;
			while((n=bis.read(b))!=-1)
				bos.write(b,0,n);
			bos.flush();
		}catch(IOException e){e.printStackTrace();
		}finally{
			if(bis!=null)try {bis.close();} catch (IOException e) {e.printStackTrace();}
			if(bos!=null)try {bos.close();} catch (IOException e) {e.printStackTrace();}
		}
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