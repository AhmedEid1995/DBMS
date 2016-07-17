package eg.edu.alexu.csd.oop.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**************************************************************************************/
public class Implementation implements Database {
	
	//private  LinkedList<String> structureQuery=new LinkedList<String>();
	@SuppressWarnings("rawtypes")
	private LinkedList rows = new LinkedList<Map<String, String>>();
	
	private Map <String , String> map = new HashMap<String,String>();
	private String[] dummy=null;
	private String[] parameters;
	private String[] partialParameters=null;
	private String[] newStates;
	private String file=null;
	
	
	
	
	
	
	private Parser parse =new Parser();
	private File database=null;
	private String currentDatabase;
	String newState=null,oldState=null;Pattern pat=null;Matcher matcher=null;
	
	@Override
	public String createDatabase(String databaseName, boolean dropIfExists) {		
		// TODO Auto-generated method stub
		databaseName=databaseName.toLowerCase();
		
		String s = new File(this.getClass().getName()).getAbsolutePath().replaceAll((this.getClass().getName()), "");
		s = s.substring(0, s.length() - 1);
		
		Search searchForDatabase = new Search();
		boolean flag = false;
		if (dropIfExists) {
			flag = searchForDatabase.searchFolder(databaseName,s);
			try {
				if (flag) {
					
					currentDatabase = databaseName;
					executeStructureQuery("DROP DATABASE " + databaseName);
				}
				executeStructureQuery("CREATE DATABASE " + databaseName);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			flag = searchForDatabase.searchFolder(databaseName, s);
			if (!flag) {
				try {

					executeStructureQuery("CREATE DATABASE " + databaseName);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				currentDatabase=databaseName;
			}
		}
		return s+"/"+databaseName;
	}
/**************************************************************************************/
	@SuppressWarnings("unused")
	@Override
	public boolean executeStructureQuery(String query) throws SQLException {
		query=query.toLowerCase();
		query=query.replaceAll("\\(", " \\(");	
		if(database==null&&currentDatabase!=null){
			
			
			database = new File(currentDatabase);
			database.mkdir();
			
		}
		if(!parse.check(query))throw new SQLException();
	else{
		String pattern = "(\\w.*)(\\(\\s*)(.+?)(\\s*\\))";
		String parameters = query.replaceAll(pattern, "$3");
		parameters=parameters.replaceAll("\\s*,\\s*",",");
		String nameQuery=query.replaceAll(pattern, "$1");
		String[] splitString = (nameQuery.split("\\s+"));
		if(splitString[0].equalsIgnoreCase("create")){
				if(splitString[1].equalsIgnoreCase("database")){
					currentDatabase=splitString[2];
					database = new File(currentDatabase);
					database.mkdir();
					
				}else {
					//if(currentDatabase==null)throw new  SQLException();
					if(database==null)throw new  SQLException();
					String fileName=currentDatabase+"/"+splitString[2] ;
					if(fileName.contains("."))throw new  SQLException();
					Search s =new Search();
					//System.out.println(s.findFile(splitString[2]+".xml", new File(database.getPath())));
					if(s.findFile(splitString[2]+".xml", new File(database.getPath())))return false;
					CreateTable x =new CreateTable();
					
					
					
						/*String[] parameters,String file*/
						String[]para=parseParameters(null, query,false, true);
						
						try {
							x.saveTableXml(para, fileName);
						} catch (ParserConfigurationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SAXException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (TransformerException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					
				}
		}else{
			if(splitString[1].equalsIgnoreCase("database")){
				if(database==null&&currentDatabase==null){
				
					currentDatabase=splitString[2];
					database = new File(currentDatabase);
					database.mkdir();
					
				}
				
					String[]entries = database.list();
					for(String s: entries){
						File currentFile = new File(database.getPath(),s);
						currentFile.delete();
					} 
					database.delete();
					database=null;
					currentDatabase=null;
			}else{
				String fileName=splitString[2];
				File currentFile = new File(database.getPath(),fileName);
				if(currentFile==null)throw new RuntimeException();
				else currentFile.delete();
				
			}
		}
		  
		return true;
		
		
		
	}
	}
/**************************************************************************************/
	@Override
	public Object[][] executeQuery(String query) throws SQLException {
		// TODO Auto-generated method stub
		 query=query.toLowerCase();
		if (!parse.check(query)) {
			throw new SQLException();
		} else {
			Select selectDatabase = new Select();
			if (query.contains("*")&&!query.contains("where")) {
				String[] splitString = query.split(" from ");
				//currentDatabase = "database";
				String tableName = splitString[1];
				LinkedList<LinkedList<Object>> returnValues = new LinkedList<LinkedList<Object>>();
				returnValues = selectDatabase.selectAll(currentDatabase, tableName,null,null,null);
				
				Object[][] selectedValues = new Object[returnValues.size()][returnValues.get(0).size()];

				for (int i = 0; i < returnValues.size(); i++) {
					LinkedList<Object> temp = new LinkedList<Object>();
					temp = returnValues.get(i);
					for (int j = 0; j < temp.size(); j++) {
						selectedValues[i][j] = temp.get(j);
					}
				}
				return selectedValues;
			}else if (query.contains("*")&&query.contains("where")){
				Pattern pattern = Pattern.compile("from (.*?) where");
				Matcher matcher = pattern.matcher(query);
				String tableName = null;
				while (matcher.find()) {
					tableName = matcher.group(1);
				}
				
				pattern = Pattern.compile("where (.*?) (>|<|=)");
				matcher = pattern.matcher(query);
				String conditionTag = null;
				while (matcher.find()) {
					conditionTag = matcher.group(1);
				}
				
				pattern = Pattern.compile("where " + conditionTag + " " + "(.*?)\'?\\w+\'?");
				matcher = pattern.matcher(query);
				String condition = null;
				while (matcher.find()) {
					condition = matcher.group(1);
				}
				
				
				String[] splitString = query.split(condition);
				Object value = (Object) splitString[1];
				
				LinkedList<LinkedList<Object>> returnValues = new LinkedList<LinkedList<Object>>();
				returnValues = selectDatabase.selectAll(currentDatabase, tableName,conditionTag,condition,value);
				if(returnValues==null){
					Object[][] selectedValues = new Object[0][0];
					return selectedValues;
				}
				Object[][] selectedValues = new Object[returnValues.size()][returnValues.get(0).size()];
				
				for (int i = 0; i < returnValues.size(); i++) {
					LinkedList<Object> temp = new LinkedList<Object>();
					temp = returnValues.get(i);
				//	System.out.println(temp.size());
					for (int j = 0; j < temp.size(); j++) {
						selectedValues[i][j] = temp.get(j);
					}
				}
				return selectedValues;
				
				
				
				
			} else if (!query.contains("where")) {
				Pattern pattern = Pattern.compile("select (.*?) from");
				Matcher matcher = pattern.matcher(query);
				String colName = null;
				while (matcher.find()) {
					colName = matcher.group(1);
				}
				String[] splitString = query.split(" from ");
				//currentDatabase = "database";
				String tableName = splitString[1];
				LinkedList<Object> returnValues = new LinkedList<Object>();
				returnValues = selectDatabase.selectCol(currentDatabase, tableName, colName, null, null, null);
				Object[][] selectedValues = new Object[returnValues.size()][1];
				for (int i = 0; i < returnValues.size(); i++) {
					selectedValues[i][0] = returnValues.get(i);
					// System.out.println(returnValues.get(i));
				}
				return selectedValues;
			} else {
				//currentDatabase = "database";
				Pattern pattern = Pattern.compile("select (.*?) from");
				Matcher matcher = pattern.matcher(query);
				String colName = null;
				while (matcher.find()) {
					colName = matcher.group(1);
				}
				pattern = Pattern.compile("from (.*?) where");
				matcher = pattern.matcher(query);
				String tableName = null;
				while (matcher.find()) {
					tableName = matcher.group(1);
				}
				pattern = Pattern.compile("where (.*?) (>|<|=)");
				matcher = pattern.matcher(query);
				String conditionTag = null;
				while (matcher.find()) {
					conditionTag = matcher.group(1);
				}

				pattern = Pattern.compile("where " + conditionTag + " " + "(.*?)\'?\\w+\'?");
				matcher = pattern.matcher(query);
				String condition = null;
				while (matcher.find()) {
					condition = matcher.group(1);
				}
				
				String[] splitString = query.split(condition);
				;
				Object value = (Object) splitString[1];
				LinkedList<Object> returnValues = new LinkedList<Object>();
				
				returnValues = selectDatabase.selectCol(currentDatabase, tableName, colName, condition, value,
						conditionTag);
				Object[][] selectedValues = new Object[returnValues.size()][1];
				for (int i = 0; i < returnValues.size(); i++) {
					selectedValues[i][0] = returnValues.get(i);
					// System.out.println(returnValues.get(i));
				}
				return selectedValues;
			}
		}
		

	}
/**************************************************************************************/
	@Override
	public int executeUpdateQuery(String query) throws SQLException {
		query=query.toLowerCase();
		//System.out.println(parse.check(query));
		if(!parse.check(query)){
			throw new SQLException();
		}else{
			query=query.replaceAll("\\(", " \\(");	
		String[] values=null;
		Insert i =new Insert();
		query=query.toLowerCase();
		query=query.replaceAll("\\s{2,}", " ");
		dummy=query.split(" ");
		
		
		if(query.contains("update")){
			file=currentDatabase+"/"+dummy[1];
			System.out.println(dummy[1]+"  "+database.getPath());
			//if(!s.findFile(dummy[1]+".xml", new File(database.getPath())))throw new RuntimeException();
		}
		else {
			file=currentDatabase+"/"+dummy[2];
			//if(!s.findFile(dummy[2]+".xml", new File(database.getPath())))throw new RuntimeException();
		}
		
		//System.out.println(file+"ff");
	
		parameters=parseParameters(file,null,false,false);
		
		getRows(file,parameters);
		
		if(query.matches("\\s*"+"insert"+"\\s.*")){
			
			dummy=query.split(" values ");
			partialParameters=parseParameters(null,dummy[0],true,false);
			values=parseParameters(null,query,true,false);
			if(values.length>parameters.length)return 0;
			if(!partialParameters[0].contains(" "))
				i.insertInto(file, parameters , values, rows,partialParameters,false);
			else 
				i.insertInto(file, parameters , values, rows,null,false);
			
			return 1;
			
			
			
			
		}if(query.matches("\\s*"+"delete"+"\\s.*")){
			if(query.contains("where")){
			query=query.replaceAll("where ","where");
			dummy=(query.split("where"));
			if(dummy[1].contains("=")){
				dummy[1]=dummy[1].replaceAll("\\s*=\\s*","=");
				//dummy[1]=dummy[1].replaceAll("='","=");
				//dummy[1]=dummy[1].replaceAll("'\\s*","");
			}else if(dummy[1].contains(">")){
				dummy[1]=dummy[1].replaceAll("\\s*>\\s*",">");
				//dummy[1]=dummy[1].replaceAll(">'",">");
				//dummy[1]=dummy[1].replaceAll("'\\s*","");
			}else if(dummy[1].contains("<")){
				dummy[1]=dummy[1].replaceAll("\\s*<\\s*","<");
				//dummy[1]=dummy[1].replaceAll("<'","<");
				//dummy[1]=dummy[1].replaceAll("'\\s*","");
			}
			String cond=dummy[1];
			
			try {
				return i.deleteOrUpdate(file, rows, cond, parameters, null,false, true);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			}else{
				i.insertInto(file,parameters,null,null,null,true);
				return rows.size();
			}
			

		}if(query.matches("\\s*"+"update"+"\\s.*")){
			String cond=null;
			
			if(query.contains("where")){
				query=query.replaceAll("where ","where");
				dummy=(query.split("where"));
				dummy[1]=dummy[1].replaceAll("\\s*=\\s*","=");
				//dummy[1]=dummy[1].replaceAll("='","=");
				//dummy[1]=dummy[1].replaceAll("'\\s*","'");
				cond=dummy[1];
				
				pat = Pattern.compile("set (.*?) where");
			     matcher = pat.matcher(query);
			    while (matcher.find()) {
			    	newState=matcher.group(1);
			    }
			    newState=newState.replaceAll("\\s*=\\s*", "=");
			    newState=newState.replaceAll("\\s*,\\s*",",");
			    if(newState.contains(","))newStates=newState.split(",");
			    else {newStates=new String[1];newStates[0]=newState;}
			    
			    
			    
			    
			}else{
				dummy=query.split("set ");
				newState=dummy[1];
			}
			    newState=newState.replaceAll("\\s*=\\s*","=");
			   // newState=newState.replaceAll("='","=");
			   // newState=newState.replaceAll("'\\s*","");
			    newState=newState.replaceAll("\\s*,\\s*", ",");
			    newStates=newState.split(",");
			    for(String t:newStates){
			    	System.out.println(t);
			    }
				try {
					
					if(query.contains("where")){
						
					return i.deleteOrUpdate(file, rows, cond,parameters, newStates, true, false);
					}else{
						
					return i.deleteOrUpdate(file, rows, null,parameters, newStates, true, false);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				
			}
			
		}
		
		return 0;
	}


/**
 * @throws SQLException ************************************************************/
	private String[]parseParameters(String file,String line,boolean insert,boolean create) throws SQLException{
		
		

		if(!insert&&!create){
			BufferedReader br;
			try {
				
				br = new BufferedReader(new FileReader(file+".dtd"));
				line = br.readLine();
				line = br.readLine();
				
				br.close();
			}catch (IOException e) {
				e.printStackTrace();
			}
		
		}
		
		String[] parameters=null;
		if(line==null){
			//log("hamaaaaaaaaaaaaaaa\n",false);

			throw new SQLException();
		}
		line=line.replaceAll("\\s{2,}", " ");
		String pattern = "(<*)(!*)(\\w.*)(\\(\\s*)(.+?)(\\s*\\))(>*)";
		line = line.replaceAll(pattern, "$5");
		line=line.replaceAll("\\s*,\\s*",",");
		//line=line.replaceAll("'","");
		parameters=line.split(",");
		return parameters;

	}
/**************************************************************/
	@SuppressWarnings("unchecked")
	public void getRows(String file,String[]parameters){
		rows.clear();
		
		try {
			
	         DocumentBuilderFactory dbFactory 
	            = DocumentBuilderFactory.newInstance();
	         File input=new File(file+".xml");
	         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	         //System.out.println("BB"+file);
	         //dummy=file.split("/");
	        // String tableName=dummy[1];
	         Document doc = dBuilder.parse(input);
	        // doc.normalize();
	       
	         //Document doc = dBuilder.newDocument();
	         doc.normalize();
			//doc.getFirstChild();
			NodeList nRow = doc.getElementsByTagName("row");
			System.out.println("nRow"+nRow.getLength());
	         for (int temp = 0; temp < nRow.getLength(); temp++) {
	        	 Node nNode = nRow.item(temp);
	        	
	        	 if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	        		 Element eElement =  (Element) nNode;
	        		 NodeList nPara=eElement.getChildNodes();
	        		 int index=0;
	        		// System.out.println("npara"+nPara.getLength());
	        		 for(int i=0;i<nPara.getLength();i++){
	        			 nNode=nPara.item(i);
	        			 if(nNode.getNodeType()==Node.ELEMENT_NODE){
	        				 eElement=(Element)nNode;
	        				 //System.out.println("EE"+eElement.getTextContent());
	        			  
	        			 //System.out.println("7asn"+parameters[index]);
	        			 
	        			 map.put(parameters[index++],(String)eElement.getTextContent());
	        		 }
	        		 }
	        		
	        		 rows.add(map);
	        		 map= new HashMap<String,String>();
	        		 
	        		 
	        	 }
	         }
	        // System.out.println("BAB"+rows.size());
	         
	         
			TransformerFactory tFactory=TransformerFactory.newInstance();
	        Transformer transformer;
			transformer = tFactory.newTransformer();
	        DOMSource source = new DOMSource(doc);
	        StreamResult streamResult=new StreamResult(new File(file+".xml"));
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.transform(source,streamResult);
				
				
		}catch (TransformerException e) {
				e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	

}