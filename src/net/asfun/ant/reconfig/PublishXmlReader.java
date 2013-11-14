package net.asfun.ant.reconfig;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.asfun.ant.reconfig.Operate.Operation;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class PublishXmlReader {

	private Document doc = null;
	private XPath xPath = XPathFactory.newInstance().newXPath();
	
	public PublishXmlReader(String file) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(file);
			doc.getDocumentElement().normalize();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public ProjectAlteration findIndicators(String projectName) {
		ProjectAlteration pa = new ProjectAlteration();
		String xpath_sys = "/platform/system[@name='"+ projectName +"']";
		String xpath_file = xpath_sys + "/config-files/config-file";
		try {
			Node root = (Node) xPath.evaluate(xpath_sys, doc, XPathConstants.NODE);
			// 解析项目信息
			NodeList nodes = root.getChildNodes();
			for(int i=0; i< nodes.getLength(); i ++) {
				Node node = nodes.item(i);
				if ("publish-path".equalsIgnoreCase(node.getNodeName())) {
					pa.setProjectName(node.getTextContent());
				}
				if ("publish-type".equalsIgnoreCase(node.getNodeName())) {
					pa.setFile("2".equals(node.getTextContent()));
				}
				if ("config-files".equalsIgnoreCase(node.getNodeName()) ) {
					// 解析配置文件信息
					NodeList files = (NodeList) xPath.evaluate(xpath_file, node, XPathConstants.NODESET);
					int fileNumber = files.getLength();
					Log.print("project [" + projectName+ "] will modify file count : " + fileNumber);
					for(int j=0; j<fileNumber; j++) {
						Node file = files.item(j);
						FileAlteration fa = new FileAlteration();
						fa.setFile(file.getAttributes().getNamedItem("name").getNodeValue());
						fa.setFileType(file.getAttributes().getNamedItem("type").getNodeValue());
						// 解析配置
						NodeList configs = (NodeList) xPath.evaluate(xpath_file +"[@name='"+fa.getFile()+"']/config", file, XPathConstants.NODESET);
						int confNumber = configs.getLength();
						Log.print("\tfile ["+fa.getFile()+"] will modify config count : " + confNumber);
						for(int k=0; k<confNumber; k++ ) {
							Node config = configs.item(k);
							String location = config.getAttributes().getNamedItem("element").getNodeValue();
							String oper = config.getAttributes().getNamedItem("optype").getNodeValue();
							Operation operation = Operate.getOperation(oper);
							switch(operation) {
							case UPDATE:
								Node attr = config.getAttributes().getNamedItem("attr");
								String spot = attr==null?null:attr.getNodeValue().trim();
								Node value = getNamedChild(config, "value");
								String val = value==null?null:value.getTextContent();
								fa.addIndicator(location, operation, val, spot);
								break;
							case DELETE:
								fa.addIndicator(location, operation, null, null);
								break;
							case REPLACE:
								Node value1 = getNamedChild(config, "value");
								String val1 = value1==null?null:value1.getTextContent().trim();
								fa.addIndicator(location, operation, val1, null);
								break;
							case ADD:
								Node value2 = getNamedChild(config, "value");
								String val2 = value2==null?null:value2.getTextContent().trim();
								fa.addIndicator(location, operation, val2, null);
								break;
							}
						}
						pa.addFileAlteration(fa);
					}
				}
			}
		} catch (XPathExpressionException e) {
			System.err.println(e.getMessage());
		}
		return pa;
	}
	
	private Node getNamedChild(Node parent, String nodeName) {
		NodeList children = parent.getChildNodes();
		for(int i=0; i<children.getLength(); i++) {
			Node node = children.item(i);
			if ( nodeName.equalsIgnoreCase(node.getNodeName())){
				return node;
			}
		}
		return null;
	}

	public List<String> listProjects() {
		String xpath_sys = "/platform/system";
		List<String> result = null;
		try {
			NodeList nodes = (NodeList) xPath.evaluate(xpath_sys, doc, XPathConstants.NODESET);
			int size = nodes.getLength();
			result = new ArrayList<String>(size);
			for(int i=0; i<size; i++) {
				Node node = nodes.item(i);
				Node attr = node.getAttributes().getNamedItem("name");
				if ( attr != null ) {
					result.add(attr.getNodeValue());
				} else {
					attr = doc.createAttribute("name");
					String value = "auto-id-" +i;
					attr.setNodeValue(value);
					node.getAttributes().setNamedItem(attr);
					result.add(value);
				}
			}
		} catch (XPathExpressionException e) {
			System.err.println(e.getMessage());
		}
		return result;
	}
	
}
