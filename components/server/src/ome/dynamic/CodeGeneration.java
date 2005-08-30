/*
 * Created on Jun 29, 2005
 */
package ome.dynamic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import junit.framework.TestCase;

/**
 * deals with runtime-generated SemanticTypes
 * 
 * <h3>Design</h3>
 * <h4>Generation</h4>
 * ST/OWL definition --> Database update OWL and/or DDL --> Hibernate Mapping
 * files Mapping files --> Java code Java code --> Classes Classes -->
 * specialized ClassLoader in Tomcat Classes+Mappings --> New sessionFactory in
 * Spring Classes --> Jar Jar --> specialized ClassLoader on client
 * 
 * <h4>Dynamic Loading</h4>
 * Services are wrapped with Interceptor which asks the server ?mexId, or
 * Module_execution_id which created the need to build this. Available in
 * OMEModel.mex; If (<) Abort If (=) make call [Handshake] If (>) {
 * ((OmeroClientClassLoader)getClassLoader()).reload();//problems with
 * System?--WebAppCL doesn't delegate first! downloads a jar, and reloads all
 * classes. // perhaps just SysClassLoader or doch SecureClassLoader (URLCL)
 * with signing! // or do we need to just download the new definitions?
 * (assuming right-once) // much easier. No need to proxy the model (see JRiA)
 * 
 * 
 * Note: will need to NOT include omero-common in server, but unpack it! how to
 * redistribute.
 * 
 * @author josh
 */
public class CodeGeneration extends TestCase {

	private static Log log = LogFactory.getLog(CodeGeneration.class);
	
	String tmp = System.getProperty("user.dir")+"/target/classes";
	
	public void test1() {
		try {
			sun.tools.javac.Main comp = new sun.tools.javac.Main(System.out,
					null);
			File file = File.createTempFile("jav", ".java", new File(tmp));
			file.deleteOnExit();

			String filename = file.getName();
			String classname = filename.substring(0, filename.length() - 5);

			PrintWriter out = new PrintWriter(new FileOutputStream(file));
			out.println("/**");
			out.println(" * Source created on " + new Date());
			out.println(" */");
			out.println("public class " + classname + " {");
			out
					.println("    public static void main(String[] args) throws Exception {");

			out.print("        ");
			out.println("System.out.println(\""+classname+"\");");
			out.println("    }");
			out.println("}");

			out.flush();
			out.close();

			String[] args = new String[] { 
					"-d",
					tmp,
					file.getAbsolutePath() };

			boolean status = comp.compile(args);

			if (status) {
				File clazzFile = new File(file.getParent(), classname + ".class");
				clazzFile.deleteOnExit();
				
				Class clazz = Class.forName(classname);
				Method main = clazz.getMethod("main",
						new Class[] { String[].class });
				main.invoke(null, new Object[] { new String[0] });
				addToDb(classname, clazzFile);
			} else {
				throw new RuntimeException("Compile failed.");
			}

		} catch (Exception e) {
			throw new RuntimeException("Died on dynamic call.", e);
		}
	}

	// STORING IN DATABASE
	ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
			new String[]{"WEB-INF/dbcp.xml","WEB-INF/config-local.xml"});
	JdbcTemplate jt = new JdbcTemplate((DataSource) ctx
			.getBean("dataSource"));
	
	// CURRENTLY FUNCTIONING AS DAO
	// create table testing ( name varchar(128), class bytea );
	/* byte streams to DB with postgres:
	 * http://pgsqld.active-venture.com/jdbc-binary-data.html
	 * File file = new File("myimage.gif");
	 * FileInputStream fis = new FileInputStream(file);
	 * PreparedStatement ps = conn.prepareStatement("INSERT INTO images VALUES (?, ?)");
	 * ps.setString(1, file.getName());
	 * ps.setBinaryStream(2, fis, file.length());
	 * ps.executeUpdate();
	 * ps.close();
	 * fis.close();
	 * 
	 * PreparedStatement ps = con.prepareStatement("SELECT img FROM images WHERE imgname=?");
	 * ps.setString(1, "myimage.gif");
	 * ResultSet rs = ps.executeQuery();
	 * if (rs != null) {
	 * while(rs.next()) {
	 *   byte[] imgBytes = rs.getBytes(1);
	 *    // use the stream in some way here
	 *   }
	 *   rs.close();
	 *   }
	 *   ps.close();
	 *   Here the binary data was retrieved as an byte[]. You could have used a InputStream object instead.
	 */


	public void addToDb(String classname, File clazzFile) {
		
		String name = classname;
		Long length = clazzFile.length();
		byte[] code = new byte[length.intValue()];
		
		InputStream in=null;
		try {
			in = new FileInputStream(clazzFile);
			int stored = in.read(code);
			if (stored != length.intValue()){
				throw new RuntimeException("Not everything read");
			} 
			jt.update("insert into testing (name,class) values (?,?);",
					new Object[]{name,code} );
		} catch (Exception e){
			throw new RuntimeException("Error storing in DB",e);
		} finally {
			if (null!=in) {
				try {
					in.close();
				} catch (IOException e) {
					//
				}
			}
		}
	}
	
	protected String getNameFromDB(){
		 Map map = (Map) jt.queryForList("select name from testing").get(0);
		 return (String) map.get("name");
	}
	
	public byte[] getClassFromDB(String className){
//		Object o = jt.queryForList("select class from testing where name = ?",new Object[]{className});
//		if (null==l || l.size()==0) throw new RuntimeException("No results found.");
//		Map map = (Map) l.get(0);
//		String s = (String) map.get("class");
//		return (byte[])s.getBytes();
		return (byte[]) jt.queryForObject("select class from testing where name = ?",new Object[]{className},byte[].class);
	}

	public static void main(String[] args) {
		//ensures that the class isn't available from other tests!
		new CodeGeneration().testGetClassFromDB();	
	}
	
	public void testGetClassFromDB(){

		//fail("now need to write classloader to test");
		String name = getNameFromDB();
		Class clazz = new OmeroDBClassLoader().doIt(
				name, getClassFromDB(name)
				);
		try {
			Method main = clazz.getMethod("main",new Class[]{String[].class});
			main.invoke(null,new Object[]{new String[]{}});
		} catch (Exception e) {
			throw new RuntimeException("couldn't re-invoke main.",e);
		}
		
		
	}

	
}