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
//			throw new Exception("未查找到制单人【"+recordVOS.get(0).getVoperatorid()+"】信息");
//		}
		Document doc=XMLUtil.createDocument();
		//准备凭证节点
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
	 * @param details	分录集合
	 * @param recordVO
	 * @param index
	 */
	private static void createDetail(Document doc, Element details,VoucherRecordVO recordVO){
		Element item=doc.createElement("item");
		//分录号 i从0开始 需要加1
		appendChildElement(doc,item,"detailindex",recordVO.getDetailindex().toString());
		//科目
		appendChildElement(doc,item,"accsubjcode",recordVO.getSubjcode());
		//摘要
		appendChildElement(doc,item,"explanation",recordVO.getAbs_summary());
		//业务日期
		appendChildElement(doc,item,"verifydate",recordVO.getMakedate());
		//原币借方金额
		appendChildElement(doc,item,"debitamount",doubleToString(recordVO.getOriginal_debtor_currency()));
		//原币贷方金额
		appendChildElement(doc,item,"creditamount",doubleToString(recordVO.getOriginal_credit_currency()));
		//本币借方金额
		appendChildElement(doc,item,"localdebitamount",doubleToString(recordVO.getLocal_debtor_currency()));
		//本币贷方金额
		appendChildElement(doc,item,"localcreditamount",doubleToString(recordVO.getLocal_credit_currency()));
		//币种
		appendChildElement(doc,item,"pk_currtype","CNY");
		//科目
		appendChildElement(doc,item,"pk_accasoa",recordVO.getSubjcode());
		createAss(doc,item,recordVO);
		details.appendChild(item);
	}
	/**
	 * @param doc
	 * @param item 分录
	 * @param recordVO
	 */
	private static void createAss(Document doc, Element item,VoucherRecordVO recordVO){
		Element ass=doc.createElement("ass");
		boolean flag=false;
		//部门
		if(recordVO.getDef1()!=null){
			flag=true;
			createAssItem(doc,ass,"0001",recordVO.getDef1());
		}
		//银行账户
		if(recordVO.getDef2()!=null){
			flag=true;
			createAssItem(doc,ass,"0011",recordVO.getDef2());
		}
		//证券档案
		if(recordVO.getDef3()!=null){
			flag=true;
			createAssItem(doc,ass,"0049",recordVO.getDef3());
		}
		////客户辅助核算
		if(recordVO.getDef4()!=null){
			flag=true;
			createAssItem(doc,ass,"0017",recordVO.getDef4());
		}
		//证券交易所
		if(recordVO.getDef5()!=null){
			flag=true;
			createAssItem(doc,ass,"0048",recordVO.getDef5());
		}
		////人员档案
		if(recordVO.getDef6()!=null){
			flag=true;
			createAssItem(doc,ass,"0002",recordVO.getDef6());
		}
		//项目辅助核算
		if(recordVO.getDef7()!=null){
			flag=true;
			createAssItem(doc,ass,"0044",recordVO.getDef7());
		}
		//客商辅助核算
		if(recordVO.getDef8()!=null){
			flag=true;
			createAssItem(doc,ass,"0004",recordVO.getDef8());
		}
		if(flag)
		item.appendChild(ass);
	}
	/**
	 * 
	 * 银行账户    0011
部门档案    0001
证券档案    0049
客户辅助核算   0017
证券交易所      0048
人员档案    0002
项目辅助核算   0044
客商辅助核算   0004
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
		//初始化根节点
	     Element root=   doc.createElement("ufinterface");
	     root.setAttribute("account", configJson.getString("account"));
	     root.setAttribute("billtype", configJson.getString("billtype"));
	     root.setAttribute("businessunitcode", configJson.getString("businessunitcode"));
	     root.setAttribute("groupcode", configJson.getString("groupcode"));
	     root.setAttribute("receiver", configJson.getString("receiver"));
	     root.setAttribute("sender", configJson.getString("sender"));
	     root.setAttribute("orgcode", orgCode);
	    //获取voucher节点并将其加载入根节点
	     Element voucher=   doc.createElement("voucher");
	     root.appendChild(voucher);
	     //将根节点加载到文档内容中
	     doc.appendChild(root);
	     return voucher;
	}
	private static Element createVoucherHead(Document doc, Element voucher,List<VoucherRecordVO> recordVOS,JSONObject configJson,String orgCode,String pk_prepared) throws Exception{
		
		String pk_accountingbook =QueryAction.queryValueByCondition("org_accountingbook", "code", "  pk_relorg=(select pk_org from org_orgs where code='"+orgCode+"' and isbusinessunit='Y') and pk_setofbook =(select pk_setofbook from org_setofbook where code='"+accountingtype+"') ");
		Element voucher_head =doc.createElement("voucher_head");
		//new SimpleDateFormat("yyyy").format(date)
//		 /凭证类别
		appendChildElement(doc,voucher_head,"pk_vouchertype","01");
		//会计年度
		appendChildElement(doc,voucher_head,"year",new SimpleDateFormat("yyyy").format(date));
//凭证类型值 0：正常凭证 3：数量调整凭证 不可空
		appendChildElement(doc,voucher_head,"voucherkind","0");
		//作废标志
		appendChildElement(doc,voucher_head,"discardflag","N");
		appendChildElement(doc,voucher_head,"period",new SimpleDateFormat("MM").format(date));
		//制单日期
		appendChildElement(doc,voucher_head,"prepareddate",recordVOS.get(0).getMakedate());
		appendChildElement(doc,voucher_head,"pk_prepared",pk_prepared);
		appendChildElement(doc,voucher_head,"pk_system",configJson.getString("pk_system"));
		//账簿
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
