package net.asfun.ant.reconfig;

import net.asfun.ant.reconfig.FileAlteration.AlterationIndicator;
import net.asfun.ant.reconfig.Operate.Operation;
import org.apache.tools.ant.filters.StringInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;



public class XmlModifier implements ConfigModifier{

	private Document doc = null;
	private DocumentBuilder builder = null;
	private XPath xPath = XPathFactory.newInstance().newXPath();
	boolean isNsAdd = false;
	String defaultNs = null;
	String defaultNsUri = null;
	DocumentType dt = null;

	public XmlModifier() {
		
	}
	
	public XmlModifier(String file) {
		readFrom(file);
	}
	
	public void readFrom(String file) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			builder = factory.newDocumentBuilder();
			builder.setEntityResolver(new EntityResolver(){
				public InputSource resolveEntity(String publicId,
						String systemId) throws SAXException, IOException {
					return new InputSource(new StringInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
				}});
			doc = builder.parse("file:///" + file);
			dt = doc.getDoctype();
			defaultNsUri = doc.getDocumentElement().getNamespaceURI();
			defaultNs = doc.getDocumentElement().getPrefix();
			if ( defaultNsUri != null && defaultNs == null ) {
				doc.getDocumentElement().setPrefix("ns");
				defaultNs = "ns";
				isNsAdd = true;
				Log.print("set default ns:" + defaultNsUri);
			}
			doc.getDocumentElement().normalize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void modify(AlterationIndicator indicator) {
		modify(indicator.location, indicator.oper, indicator.value, indicator.spot);
	}
	
	public void modify(String xpath, Operation oper, String value, String spot) {
		if ( doc != null ) {
			try {
				Node node;
				NodeList nodes;
				switch (oper) {
				case REPLACE :
					node = (Node) eval(xpath, XPathConstants.NODE);
                    String cv = ValueUtil.computeValue(value);
					Node df = formNode(cv);
					node.getParentNode().replaceChild(df, node);
					Log.print("\treplace `" + xpath +"` node to `" + cv +"`");
					break;
				case UPDATE:
					nodes = (NodeList) eval(xpath, XPathConstants.NODESET);
					for(int i=0; i< nodes.getLength(); i++) {
						node = nodes.item(i);
						if ( spot == null ) {
							System.err.println("can't upate xml element without attr specified");
						} else {
                            String cv1 = ValueUtil.computeValue(value);
							node.getAttributes().getNamedItem(spot).setNodeValue(cv1);
							Log.print("\tupdate `" + xpath +"` node's attribute `"+ spot +"` to `" + cv1 +"`");
						}
					}
					break;
				case DELETE:
					nodes = (NodeList) eval(xpath, XPathConstants.NODESET);
					for(int i=0; i< nodes.getLength(); i++) {
						node = nodes.item(i);
						Node parent = node.getParentNode();
						parent.removeChild(node);
						Log.print("\tdelete `" + xpath +"` node from " + parent);
					}
					break;
				case ADD:
					nodes = (NodeList) eval(xpath, XPathConstants.NODESET);
					for(int i=0; i< nodes.getLength(); i++) {
						node = nodes.item(i);
                        String cv2 = ValueUtil.computeValue(value);
						Node app = formNode(cv2);
						node.getParentNode().insertBefore(app, node.getNextSibling());
						Log.print("\tappend `" + xpath +"` node with " + app);
					}
					break;
				}
			} catch (Exception e) {
				if ( e instanceof NullPointerException ) {
					System.err.println("can't find node for " + xpath);
				} else {
					System.err.println(e.getMessage());
				}
			}
		}
	}
	
	private Object eval(String xpath, QName type) throws XPathExpressionException {
		boolean usNs = xpath.contains(":");
		if ( usNs ) {
			int point = xpath.indexOf(":");
			String ns = xpath.substring(0, point);
			xPath.setNamespaceContext(new NbNamespaceContext(ns));
		} else {
			if ( isNsAdd ) {
				xpath = xpath.replace("//", "`")
						.replace("/", "/"+defaultNs+":")
						.replace("`", "//"+defaultNs+":");
				xPath.setNamespaceContext(new NbNamespaceContext(defaultNs, defaultNsUri));
				Log.print("change xpath to : " + xpath);
				usNs = true;
			}
		}
		Object obj = xPath.evaluate(xpath, doc, type);
		if ( usNs ) {
			xPath.reset();
		}
		return obj;
	}
	
	public void writeTo(String file) {
		if ( doc != null ) {
			FileOutputStream fos = null;
			try {
				if ( isNsAdd ) {
					doc.getDocumentElement().setPrefix(null);
				}
				Node nc = doc.createComment("\n******* rebuild by ant-recfg. *******\n");
				doc.insertBefore(nc, doc.getFirstChild());
				Transformer serializer = TransformerFactory.newInstance().newTransformer();
				if ( dt != null ) {
					serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, dt.getSystemId());
				}
				fos = new FileOutputStream( file );
				StreamResult sresult = new StreamResult( fos );
				sresult.setSystemId( file );
				serializer.transform( new DOMSource(doc), sresult );
			} catch (Exception e) {
				System.err.println(e.getMessage());
			} finally {
				if ( fos != null ) {
					try {
						fos.close();
					} catch (Exception e) {
						System.err.println(e.getMessage());
					}
				}
			}
		}
	}
	
	private Node formNode(String value) throws Exception{
		if ( isNsAdd ) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(false);
			Node newNode = factory.newDocumentBuilder().parse(new StringInputStream(value));
			newNode.normalize();
			return doc.importNode(newNode.getFirstChild(), true);
		}
		Node newNode = builder.parse(new StringInputStream(value));
		newNode.normalize();
		return doc.importNode(newNode.getFirstChild(), true);
	}

	
	class NbNamespaceContext implements NamespaceContext {
		
		String prefix;
		String uri = null;
		
		NbNamespaceContext(String prefix) {
			this.prefix = prefix.replaceAll("[^\\w]","");
		}
		
		NbNamespaceContext(String prefix, String uri) {
			this.prefix = prefix;
			this.uri = uri;
		}

		public String getNamespaceURI(String prefix) {
			if ( uri == null ) {
				uri = doc.lookupNamespaceURI(prefix);
			}
			return uri;
		}

		public String getPrefix(String namespaceURI) {
			if ( prefix == null ) {
				prefix = doc.lookupPrefix(namespaceURI);
			}
			return prefix;
		}

		@SuppressWarnings("rawtypes")
		public Iterator getPrefixes(String namespaceURI) {
			return null;
		}
		
	}
	

}
