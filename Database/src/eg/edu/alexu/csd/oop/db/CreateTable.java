package eg.edu.alexu.csd.oop.db;
import java.io.File;



import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.IOException;
import java.io.PrintWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
public class CreateTable {	
	private PrintWriter fwDTD=null;
	
	@SuppressWarnings("unused")
	private String tableName,databaseName;
	private String[] dummy=null;
	private String[][] parametersAndTypes=new String [100][100];
	private String para="";
	private DocumentBuilderFactory dbFactory ;
    DocumentBuilder icBuilder;
	public void saveTableXml (String[] parameters,String file) throws ParserConfigurationException, SAXException, IOException, TransformerException{	    
			System.out.println(file+"f");
			fwDTD = new PrintWriter(file+".dtd", "UTF-8");
			dummy=file.split("/");
			databaseName=dummy[0];
			tableName=dummy[1];
			
            fwDTD.println("<!ELEMENT "+tableName+" (row)*>");
            for(int i=0;i<parameters.length;i++){
            	dummy=parameters[i].split(" ");
            	parametersAndTypes[i][0]=dummy[0];
            	parametersAndTypes[i][1]=dummy[1];
            	if(i<parameters.length-1)para+=dummy[0]+",";
            	else para+=dummy[0];
            }
			fwDTD.println("<!ELEMENT row ("+para+")>");
			for(int i=0;i<parameters.length;i++){
				fwDTD.println("<!ELEMENT "+parametersAndTypes[i][0]+" (#PCDATA)>");
				//fwDTD.println("<!ATTLIST "+parametersAndTypes[i][0]+" type CDATA #FIXED \""+parametersAndTypes[i][1]+"\">");
			}
			fwDTD.close();

				dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	            Document doc = dBuilder.newDocument();
	            Element mainRootElement = doc.createElement("table");
	            mainRootElement.normalize();
	            doc.appendChild(mainRootElement);
	           
	         
	            
	            TransformerFactory tFactory=TransformerFactory.newInstance();
	            Transformer transformer=tFactory.newTransformer();
	            DOMSource source = new DOMSource(doc);
	            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	            StreamResult streamResult=new StreamResult(new File(file+".xml"));
	            
	            
	           
	            transformer.transform(source,streamResult);

			
			
		
		
	}
	
}
