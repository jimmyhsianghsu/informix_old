package test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegTest {
    // D:\M3SDK\Java\jdk1.6.0_35\bin\javadoc.exe
    // ${container_loc}
    // -d d:\1.test\javadoc *.java -encoding UTF-8 -charset UTF-8
    // -author -version
    // ${file_prompt},${resource_loc}
    public static void main(String[] args) {
	StringBuffer sb1 = new StringBuffer();
	if (args != null) {
	    System.out.println(args[0]);
	    try {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), "UTF-8"));
		String s;
		while ((s = br.readLine()) != null)
		    sb1.append(s + '\n');
		if(br!=null)br.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
		match(sb1.toString());
	}
	}

	public static String match(String sb1){
	StringBuffer sb0 = new StringBuffer();
	StringBuffer sb2 = new StringBuffer();
	String ptn0 = "(\\w+<?\\w+>?) +(\\w+)\\((( *(@ *\\w+ *\\( *(name *= *)?\"\\w+\" *\\))? *\\w+ +\\w+ *,?\\s*)*)\\) *[;{]";
	String ptn1 = "(\\w+) +(\\w+),?";
	Matcher matcher0 = Pattern.compile(ptn0).matcher(sb1.toString());
	while (matcher0.find()) {
	    sb0.append(matcher0.group(2) + "\n\t(" + matcher0.group(3).replaceAll("\\s*\\n\\s*", " ") + "):"+ matcher0.group(1)+'\n');
	    sb2.append("/**\n *\n *\n");
	    Matcher matcher1 = Pattern.compile(ptn1).matcher(matcher0.group(3));
	    while (matcher1.find())
		for (int i1 = 1; i1 <= matcher1.groupCount(); i1 += 2) {
		    sb2.append(" * @param " + matcher1.group(i1 + 1) + '\n');
		    if (matcher1.group(i1).indexOf("<") == -1)
			sb2.append(" *            " + matcher1.group(i1) + "\n");
		    else
			sb2.append(" *            {@literal " + matcher1.group(i1) + "}\n");
		}
	    if (matcher0.group(1).indexOf("<") == -1)
		sb2.append(" * @return " + matcher0.group(1) + "\n");
	    else
		sb2.append(" * @return {@literal " + matcher0.group(1) + "}\n");
	    sb2.append(" */\n");
	    sb2.append(matcher0.group(0).replaceAll("\\s*\\n\\s*", " ") + "\n\n");
	}
	System.out.println(sb0+"\n"+sb2);
	return sb0+"\n"+sb2;
    }
}