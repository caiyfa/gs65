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
		//1.����DocumentBuilderFactory����
        //2.����DocumentBuilder����
        try {
            
            Document doc= createDocument();
           
            Element root=   doc.createElement("ufinterface");
            System.out.println(root);
            root.setAttribute("account", "develop");
            Element voucher=doc.createElement("voucher");
            Element pk_vouchertype=doc.createElement("pk_vouchertype");
            pk_vouchertype.setTextContent("aowbahOSd����");
            
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
	 * ����XML�ĵ�
	 * @param xml	XML�ı� ��Ҫ��ͷ�е�UTF-8�ĳ�GBK
	 * @param tag	��Ҫ�����ı�ǩ
	 * @return	�Լ�ֵ���б���ʽ���ر�ǩ�е����� 
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public static  Map<String,String> analysisXML(String xml,String tag) throws SAXException, IOException, ParserConfigurationException{
		//���ַ���ת����������ʽ������ʼ��doc
		Document dom=getDomBuilder().parse(new ByteArrayInputStream (xml.getBytes()));
		//��ȡ��ǩ�б�
		 NodeList sList = dom.getElementsByTagName(tag);
		 //������ǩ  
		 return node(sList);
	}
	/**
	 * @return ��ȡXML�ĵ�ʵ��
	 * @throws ParserConfigurationException
	 */
	public static Document createDocument() throws ParserConfigurationException{
		return getDomBuilder().newDocument();
	}
	/**
	 * @param document
	 * @return	��XML�ĵ�ʵ��ת��Ϊ�ַ���
	 * @throws TransformerException
	 */
	public static String documentToString(Document document) throws TransformerException{
		
        StringWriter writer=new StringWriter();
        getTransformer().transform(new DOMSource(document), new StreamResult(writer));
//        System.out.print(writer.getBuffer().toString());
        return writer.getBuffer().toString();
	}
	
	/**
	 * //������
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
	 * ������
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
	 //��Element��ʽ
    public static void element(NodeList list){
        for (int i = 0; i <list.getLength() ; i++) {
            Element element = (Element) list.item(i);
            NodeList childNodes = element.getChildNodes();
            for (int j = 0; j <childNodes.getLength() ; j++) {
                if (childNodes.item(j).getNodeType()==Node.ELEMENT_NODE) {
                    //��ȡ�ڵ�
                    System.out.print(childNodes.item(j).getNodeName() + ":");
                    //��ȡ�ڵ�ֵ
                    System.out.println(childNodes.item(j).getFirstChild().getNodeValue());
                }
            }
        }
    }
}
