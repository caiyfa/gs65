package nc.bs.gs65.util.xml;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.gs.vo.VoucherRecordVO;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.vo.pub.lang.UFDouble;

public class VoucherXmlCreateAction {
	private static Date date=null;
	private static String accountingtype=null;
	private static Map<String,String> bdrefinfoMap=new HashMap<String, String>();
	
	public static void setConf(Date paramdate,String accountType){
		date=paramdate;
		accountingtype=accountType;
	}
	public static String prepareVoucherXML(List<VoucherRecordVO> recordVOS,String jsonStr)throws Exception{
//String orgCode=QueryAction.queryValueByCondition("org_orgs","code"," pk_org ='"+recordVOS.get(0).getPk_corp()+"'");
		String orgCode=recordVOS.get(0).getPk_corp();
		
		JSONObject configJson=new JSONObject(jsonStr);
//		String pk_prepared=QueryAction.queryValueByCondition("sm_user","cuserid"," user_code='"+recordVOS.get(0).getVoperatorid()+"'");
//		if(pk_prepared==null){
//			throw new Exception("δ���ҵ��Ƶ��ˡ�"+recordVOS.get(0).getVoperatorid()+"����Ϣ");
//		}
		Document doc=XMLUtil.createDocument();
		//׼��ƾ֤�ڵ�
		  Element voucher=  createRootElement(doc,configJson,orgCode);
		  
		  Element voucher_head=createVoucherHead(doc,voucher,recordVOS,configJson,orgCode,recordVOS.get(0).getVoperatorid());
		  createDetails(doc,voucher_head,recordVOS);
		  
		return XMLUtil.documentToString(doc);
	}
	private static void createDetails(Document doc, Element voucher_head,List<VoucherRecordVO> recordVOS){
		Element details=doc.createElement("details");
		
		for(int i=0;i<recordVOS.size();i++){
			createDetail(doc,details,recordVOS.get(i));
		}
		
		voucher_head.appendChild(details);
		
		
		
	}
	/**
	 * @param doc
	 * @param details	��¼����
	 * @param recordVO
	 * @param index
	 */
	private static void createDetail(Document doc, Element details,VoucherRecordVO recordVO){
		Element item=doc.createElement("item");
		//��¼�� i��0��ʼ ��Ҫ��1
		appendChildElement(doc,item,"detailindex",recordVO.getDetailindex().toString());
		//��Ŀ
		appendChildElement(doc,item,"accsubjcode",recordVO.getSubjcode());
		//ժҪ
		appendChildElement(doc,item,"explanation",recordVO.getAbs_summary());
		//ҵ������
		appendChildElement(doc,item,"verifydate",recordVO.getMakedate());
		//ԭ�ҽ跽���
		appendChildElement(doc,item,"debitamount",doubleToString(recordVO.getOriginal_debtor_currency()));
		//ԭ�Ҵ������
		appendChildElement(doc,item,"creditamount",doubleToString(recordVO.getOriginal_credit_currency()));
		//���ҽ跽���
		appendChildElement(doc,item,"localdebitamount",doubleToString(recordVO.getLocal_debtor_currency()));
		//���Ҵ������
		appendChildElement(doc,item,"localcreditamount",doubleToString(recordVO.getLocal_credit_currency()));
		//����
		appendChildElement(doc,item,"pk_currtype","CNY");
		//��Ŀ
		appendChildElement(doc,item,"pk_accasoa",recordVO.getSubjcode());
		createAss(doc,item,recordVO);
		details.appendChild(item);
	}
	/**
	 * @param doc
	 * @param item ��¼
	 * @param recordVO
	 */
	private static void createAss(Document doc, Element item,VoucherRecordVO recordVO){
		Element ass=doc.createElement("ass");
		boolean flag=false;
		//����
		if(recordVO.getDef1()!=null){
			flag=true;
			createAssItem(doc,ass,"0001",recordVO.getDef1());
		}
		//�����˻�
		if(recordVO.getDef2()!=null){
			flag=true;
			createAssItem(doc,ass,"0011",recordVO.getDef2());
		}
		//֤ȯ����
		if(recordVO.getDef3()!=null){
			flag=true;
			createAssItem(doc,ass,"0049",recordVO.getDef3());
		}
		////�ͻ���������
		if(recordVO.getDef4()!=null){
			flag=true;
			createAssItem(doc,ass,"0017",recordVO.getDef4());
		}
		//֤ȯ������
		if(recordVO.getDef5()!=null){
			flag=true;
			createAssItem(doc,ass,"0048",recordVO.getDef5());
		}
		////��Ա����
		if(recordVO.getDef6()!=null){
			flag=true;
			createAssItem(doc,ass,"0002",recordVO.getDef6());
		}
		//��Ŀ��������
		if(recordVO.getDef7()!=null){
			flag=true;
			createAssItem(doc,ass,"0044",recordVO.getDef7());
		}
		//���̸�������
		if(recordVO.getDef8()!=null){
			flag=true;
			createAssItem(doc,ass,"0004",recordVO.getDef8());
		}
		if(flag)
		item.appendChild(ass);
	}
	/**
	 * 
	 * �����˻�    0011
���ŵ���    0001
֤ȯ����    0049
�ͻ���������   0017
֤ȯ������      0048
��Ա����    0002
��Ŀ��������   0044
���̸�������   0004
	 * @param doc
	 * @param ass
	 * @param recordVO
	 */
	private static void createAssItem(Document doc, Element ass,String checkType,String checkValue){
		Element assItem=doc.createElement("item");
		Element checkTypeEle=doc.createElement("pk_Checktype");
		checkTypeEle.setTextContent(checkType);
		assItem.appendChild(checkTypeEle);
		Element checkValueEle=doc.createElement("pk_Checkvalue");
		checkValueEle.setTextContent(checkValue);
		assItem.appendChild(checkValueEle);
		
		ass.appendChild(assItem);
	}
	private static Map<String,String> getAssInfo(VoucherRecordVO recordVO){
		Map<String,String> res=new HashMap<String,String>();
		
		
		return res;
	}
	
	private static String doubleToString(UFDouble dou){
		return ""+(dou==null?UFDouble.ZERO_DBL.doubleValue():dou.doubleValue());
	}
	
	private static Element createRootElement(Document doc,JSONObject configJson,String orgCode) throws Exception{
		//��ʼ�����ڵ�
	     Element root=   doc.createElement("ufinterface");
	     root.setAttribute("account", configJson.getString("account"));
	     root.setAttribute("billtype", configJson.getString("billtype"));
	     root.setAttribute("businessunitcode", configJson.getString("businessunitcode"));
	     root.setAttribute("groupcode", configJson.getString("groupcode"));
	     root.setAttribute("receiver", configJson.getString("receiver"));
	     root.setAttribute("sender", configJson.getString("sender"));
	     root.setAttribute("orgcode", orgCode);
	    //��ȡvoucher�ڵ㲢�����������ڵ�
	     Element voucher=   doc.createElement("voucher");
	     root.appendChild(voucher);
	     //�����ڵ���ص��ĵ�������
	     doc.appendChild(root);
	     return voucher;
	}
	private static Element createVoucherHead(Document doc, Element voucher,List<VoucherRecordVO> recordVOS,JSONObject configJson,String orgCode,String pk_prepared) throws Exception{
		
		String pk_accountingbook =QueryAction.queryValueByCondition("org_accountingbook", "code", "  pk_relorg=(select pk_org from org_orgs where code='"+orgCode+"' and isbusinessunit='Y') and pk_setofbook =(select pk_setofbook from org_setofbook where code='"+accountingtype+"') ");
		Element voucher_head =doc.createElement("voucher_head");
		//new SimpleDateFormat("yyyy").format(date)
//		 /ƾ֤���
		appendChildElement(doc,voucher_head,"pk_vouchertype","01");
		//������
		appendChildElement(doc,voucher_head,"year",new SimpleDateFormat("yyyy").format(date));
//ƾ֤����ֵ 0������ƾ֤ 3����������ƾ֤ ���ɿ�
		appendChildElement(doc,voucher_head,"voucherkind","0");
		//���ϱ�־
		appendChildElement(doc,voucher_head,"discardflag","N");
		appendChildElement(doc,voucher_head,"period",new SimpleDateFormat("MM").format(date));
		//�Ƶ�����
		appendChildElement(doc,voucher_head,"prepareddate",recordVOS.get(0).getMakedate());
		appendChildElement(doc,voucher_head,"pk_prepared",pk_prepared);
		appendChildElement(doc,voucher_head,"pk_system",configJson.getString("pk_system"));
		//�˲�
		appendChildElement(doc,voucher_head,"pk_accountingbook",pk_accountingbook);
		appendChildElement(doc,voucher_head,"reserve2","N");
		appendChildElement(doc,voucher_head,"pk_org",orgCode);
		appendChildElement(doc,voucher_head,"pk_org_v",orgCode);
		appendChildElement(doc,voucher_head,"pk_group",configJson.getString("groupcode"));
		voucher.appendChild(voucher_head);
		return voucher_head;
	}
	private static Element appendChildElement(Document doc,Element parent,String childName,String childValue){
		Element child=doc.createElement(childName);
		child.setTextContent(childValue);
		parent.appendChild(child);
		return child;
		
	}
}
