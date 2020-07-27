package nc.bs.gs65.util.xml;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


//import org.jdom.Document;
//import org.jdom.Element;
//import org.jdom.JDOMException;
//import org.jdom.input.SAXBuilder;





import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
public class XMLUtil {
	public static void main(String[] args) throws IOException {
		//1.创建DocumentBuilderFactory对象
        //2.创建DocumentBuilder对象
        try {
            
            Document doc= createDocument();
           
            Element root=   doc.createElement("ufinterface");
            System.out.println(root);
            root.setAttribute("account", "develop");
            Element voucher=doc.createElement("voucher");
            Element pk_vouchertype=doc.createElement("pk_vouchertype");
            pk_vouchertype.setTextContent("aowbahOSd我是");
            
            voucher.appendChild(pk_vouchertype);
            root.appendChild(voucher);
            doc.appendChild(root);
            System.out.print(documentToString(doc));
           /* Document d = builder.parse(new ByteArrayInputStream (read().getBytes()));
            NodeList sList = d.getElementsByTagName("sendresult");
            //element(sList);
            node(sList);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
//		read();
	}
	/*public static String read() throws IOException  {
		BufferedReader reader =new  BufferedReader(new InputStreamReader(new FileInputStream(new File("C:\\voucher\\res.txt")),"UTF-8"));
		
		StringBuffer buffer=new StringBuffer();
		String tmp=null;
		while((tmp=reader.readLine())!=null){
			buffer.append(tmp);
		}
		reader.close();
		 
		return buffer.toString().replaceAll("UTF-8", "GBK");
	}*/
	
	/**
	 * 解析XML文档
	 * @param xml	XML文本 需要将头中的UTF-8改成GBK
	 * @param tag	需要解析的标签
	 * @return	以键值对列表形式返回标签中的内容 
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public static  Map<String,String> analysisXML(String xml,String tag) throws SAXException, IOException, ParserConfigurationException{
		//将字符串转换成流的形式，并初始化doc
		Document dom=getDomBuilder().parse(new ByteArrayInputStream (xml.getBytes()));
		//获取标签列表
		 NodeList sList = dom.getElementsByTagName(tag);
		 //解析标签  
		 return node(sList);
	}
	/**
	 * @return 获取XML文档实例
	 * @throws ParserConfigurationException
	 */
	public static Document createDocument() throws ParserConfigurationException{
		return getDomBuilder().newDocument();
	}
	/**
	 * @param document
	 * @return	将XML文档实例转换为字符串
	 * @throws TransformerException
	 */
	public static String documentToString(Document document) throws TransformerException{
		
        StringWriter writer=new StringWriter();
        getTransformer().transform(new DOMSource(document), new StreamResult(writer));
//        System.out.print(writer.getBuffer().toString());
        return writer.getBuffer().toString();
	}
	
	/**
	 * //翻译器
	 */
	private static Transformer transformer=null;
	private static Transformer getTransformer() throws TransformerException{
		if(transformer==null){
			TransformerFactory tff=TransformerFactory.newInstance();
			transformer=tff.newTransformer();
		}
		transformer.setOutputProperty("encoding", "UTF-8");
		return transformer;
	}
	/**
	 * 创建器
	 */
	private static DocumentBuilder builder=null;
	private static DocumentBuilder getDomBuilder() throws ParserConfigurationException{
		if(builder==null){
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			builder=factory.newDocumentBuilder();
		}
		return builder;
	}
	public static Map<String,String> node(NodeList list){
//		List<Map<String,String>> res=new ArrayList<Map<String,String>>();
		Map<String,String> map=new HashMap<>();
        for (int i = 0; i <list.getLength() ; i++) {
            Node node = list.item(i);
            NodeList childNodes = node.getChildNodes();
            for (int j = 0; j <childNodes.getLength() ; j++) {
                if (childNodes.item(j).getNodeType()==Node.ELEMENT_NODE) {
                	
                	map.put(childNodes.item(j).getNodeName(), childNodes.item(j).getFirstChild().getNodeValue());
//                	res.add(map);
//                    System.out.print(childNodes.item(j).getNodeName() + ":");
//                    System.out.println(childNodes.item(j).getFirstChild().getNodeValue());
                }
            }
        }
//        return res;
        return map;
    }
	 //用Element方式
    public static void element(NodeList list){
        for (int i = 0; i <list.getLength() ; i++) {
            Element element = (Element) list.item(i);
            NodeList childNodes = element.getChildNodes();
            for (int j = 0; j <childNodes.getLength() ; j++) {
                if (childNodes.item(j).getNodeType()==Node.ELEMENT_NODE) {
                    //获取节点
                    System.out.print(childNodes.item(j).getNodeName() + ":");
                    //获取节点值
                    System.out.println(childNodes.item(j).getFirstChild().getNodeValue());
                }
            }
        }
    }
}
