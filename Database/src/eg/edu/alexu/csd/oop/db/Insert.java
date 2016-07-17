package eg.edu.alexu.csd.oop.db;


import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;









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

public class Insert {
	//private LinkedList lista = new LinkedList<Map<String, String>>();
	private Map <String , String> map = new HashMap<String,String>();
	private String[] condition=null;
	private String[][]newState=null;
	private String operand=null;
	private int n=0;
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public LinkedList insertInto(String file,String[] parameters,String[]values,LinkedList rows,String[] partialParameters,boolean deleteAll){
			//System.out.println("YES"+rows.size());
		if(!deleteAll){
			map=new HashMap<String,String>();
			for(int i=0;i<parameters.length;i++){
				if(partialParameters!=null){
					System.out.println(partialParameters[i]+parameters[i]);
					map.put(partialParameters[i], values[i]);
				}
				else{
					
					map.put(parameters[i], values[i]);
					
				}
			}
			
			rows.addLast(map);
		}
			saveXml(file,parameters,rows);
		return rows;
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public int deleteOrUpdate(String file,LinkedList rows,String cond,String[]parameters,String[] nState,boolean update,boolean delete) throws Exception{
		if(cond!=null){
		if(cond.contains("=")){
			condition=cond.split("=");
			operand="=";
		}else if(cond.contains(">")){
			condition=cond.split(">");
			operand=">";
		}else if(cond.contains("<")){
			condition=cond.split("<");
			operand="<";
		}else throw new Exception();
		}
		if(update){
				newState=new String[nState.length][2];
				for(int i=0;i<nState.length;i++){
					String[]dummy=nState[i].split("=");
					newState[i][0]=dummy[0];
					newState[i][1]=dummy[1];
				}
				//newState=nState.split("=");
				
		}
		LinkedList <Integer> remove=new LinkedList<Integer>();
		int i=0;
		for(Object x:rows){
			map=new HashMap<String,String>();
			map=(Map)x;
			if(cond==null){
				if(update){for(int t=0;t<nState.length;t++){map.put(newState[t][0], newState[t][1]);}}
				//if(update)map.put(newState[0], newState[1]);
				n++;
			}
			else{
			if(operand.equals("="))if(map.get(condition[0]).equals(condition[1])){if(delete)remove.add(i);else if(update){for(int t=0;t<nState.length;t++){map.put(newState[t][0], newState[t][1]);}}n++;}
			if(operand.equals("<"))if((int)Integer.parseInt(map.get(condition[0]))<(int)Integer.parseInt((condition[1]))){if(delete)remove.add(i);else if(update){for(int t=0;t<nState.length;t++){map.put(newState[t][0], newState[t][1]);}}n++;}
			if(operand.equals(">"))if((int)Integer.parseInt(map.get(condition[0]))>(int)Integer.parseInt((condition[1]))){if(delete)remove.add(i);else if(update){for(int t=0;t<nState.length;t++){map.put(newState[t][0], newState[t][1]);}}n++;}	
			}
			
			i++;
		}i=0;
		for(Integer t:remove){
			rows.remove(t-i);
			i++;
		}
		saveXml(file,parameters,rows);
		return n;
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void saveXml(String file,String[]parameters,LinkedList rows){
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		 //System.out.println("YES"+rows.size());
		try {
			
			//File inputFile=new File(file+".xml");
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.newDocument();
			//doc = dBuilder.parse(file+".xml");
			Element mainRootElement = doc.createElement("table");
			@SuppressWarnings("unused")
			String[] dummy=file.split("/");
			
			//mainRootElement.setAttribute("id", tableName);

	        
	        Element row = null ;
	        Element tag=null;
	        if(rows!=null){
	        for(Object x:rows){
	        	row = doc.createElement("row");
	        	//row.setAttribute("type", "attrvalue");
	        	map=new HashMap<String,String>();
	        	map=(Map)x;
	        	
	        	//System.out.println(map.get(parameters[3])+"bb");
	        	for(int i=0;i<parameters.length;i++){
	        		//System.out.println("maro"+parameters[i]);
	        		tag=doc.createElement(parameters[i]);
	        		//System.out.println(map.get(parameters[i])+"WW"+parameters[i]);
	        		tag.appendChild(doc.createTextNode(map.get(parameters[i])));
	        		row.appendChild(tag);
	        		
	        	}
	        	mainRootElement.appendChild(row);
	        	
	        }
	        }
	        doc.appendChild(mainRootElement);
	        
	       /* TransformerFactory tFactory=TransformerFactory.newInstance();
	        Transformer transformer;
	        transformer = tFactory.newTransformer();
	        DOMSource source = new DOMSource(doc);
	        StreamResult streamResult=new StreamResult(new File(file+".xml"));
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source,streamResult);*/
	        TransformerFactory tFactory=TransformerFactory.newInstance();
            Transformer transformer=tFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            StreamResult streamResult=new StreamResult(new File(file+".xml"));
            
            dummy=file.split("/");
           
           
           // transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,t +".dtd");
            transformer.transform(source,streamResult);
		}catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// catch (SAXException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} //catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		//}
        
        
       
        
        
		
	//}
}
