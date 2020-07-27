package nc.bs.gs65.backgroundwork;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import nc.bs.dao.BaseDAO;
//import nc.bs.dao.DAOException;
//import nc.bs.framework.common.InvocationInfoProxy;
//import nc.bs.framework.common.NCLocator;
import nc.bs.framework.common.RuntimeEnv;
import nc.bs.logging.Logger;
//import nc.bs.uap.sf.excp.SystemFrameworkException;
import nc.itf.org.IOrgConst;
//import nc.itf.uap.IUAPQueryBS;
import nc.jdbc.framework.processor.ColumnProcessor;
//import nc.pubitf.para.SysInitQuery;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;

import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultDocument;

/**
 * 通过外部交换平台生成凭证
 * @author CYF
 *
 */
public class MakeVoucherAction {
	
	/**
	 *  发送消息到外部交换平台
	 * @param srcXml
	 * @return
	 * @throws Exception
	 */
	public static String doSendMessage(String xmlStr) throws Exception {
//		InvocationInfoProxy.getInstance().setUserDataSource("nc65");
//		String setDSInfo = SetDSByByAccountCode("001");
//		if (!setDSInfo.equals("succ")) {
//			return "NC账套编码不是001不存在:生成凭证失败-"+setDSInfo;
//		}
		
		UFBoolean sysint_basews02 = UFBoolean.TRUE;
		int sysint_basews03 = 1000 * 60*3 ;
		String param=null;
		try {
			 param=getSysparam(IOrgConst.GLOBEORGTYPE,"BASEWS02");
			 sysint_basews02=new UFBoolean(param);
//			Object BASEWS02 = SysInitQuery.getParaBoolean(IOrgConst.GLOBEORGTYPE, "BASEWS02");// 是否备份接口报文参数
		} catch (Exception e) {
			Logger.warn("NC获取参数‘是否备份接口原始报文’异常：" + e.getMessage(), e);
		}
		if (sysint_basews02.booleanValue()) {
			writeXmlToFile(xmlStr);
		}
		String param3=null;
		try {
			param3=getSysparam(IOrgConst.GLOBEORGTYPE,"BASEWS03");
			if(param3==null){
				param3="60000";
			}
			sysint_basews03=new Integer(param3).intValue();
//			sysint_basews03 = SysInitQuery.getParaInt(IOrgConst.GLOBEORGTYPE,BASEWS03);// 外部交换平台webservice数据同步接口最大等待时间
		} catch (Exception e) {
			Logger.warn("NC获取参数‘外部交换平台webservice数据同步接口最大等待时间’异常：" + e.getMessage(),e);
		}

		String param1=null;
//		String sysinit_basews01 = "http://10.23.5.152:9080/service/XChangeServlet?account=001&groupcode=0001";
//		String sysinit_basews01 = "http://localhost:8080/service/XChangeServlet?account=001&groupcode=0001";
		String sysinit_basews01=null;
		try {
			param1=getSysparam(IOrgConst.GLOBEORGTYPE,"BASEWS01");
			if(param1==null){
				throw new Exception();
			}
			sysinit_basews01=param1;
//			sysinit_basews01 = SysInitQuery.getParaString(IOrgConst.GLOBEORGTYPE, BASEWS01);// 外部交换平台接口地址参数
		} catch (Exception e) {
			Logger.error("NC获取外部交换平台接口地址参数值异常：" + e.getMessage(), e);
			return getErrorReturnXml("NC获取外部交换平台接口地址参数值异常：" + e.getMessage());
		}
		
		StringBuffer strbuf = new StringBuffer();
		URL realURL = new URL(sysinit_basews01);
		HttpURLConnection connection = (HttpURLConnection) realURL.openConnection();
		Logger.debug("连接已开启");
		connection.setConnectTimeout(sysint_basews03);
		Logger.debug("设置连接时间:" + sysint_basews03);
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestProperty("Content-type", "text/xml");
		connection.setRequestMethod("POST");

		// 解决生僻汉字乱码问题
//		 Document doc = DocumentHelper.parseText(srcXml);
		DefaultDocument doc = (DefaultDocument) DocumentHelper.parseText(xmlStr);
		doc.setXMLEncoding("GBK");
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding("GBK");
		OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), "GBK");
		// end

		XMLWriter writer = new XMLWriter(osw, format);
		writer.write(doc);
		writer.close();
		
		InputStream inputStream = connection.getInputStream();
		byte tempBytes[] = new byte[2048];
		for (int count = 0; (count = inputStream.read(tempBytes)) != -1;)
			strbuf.append(new String(tempBytes, 0, count, "utf-8"));

		if (inputStream != null)
			inputStream.close();
		connection.disconnect();
		Logger.debug("连接已经关闭");
		Logger.debug("返回数据包" + strbuf.toString());

		return strbuf.toString();
	}
	private static BaseDAO dao;
	private static BaseDAO getDao(){
		
		if(dao==null){
			dao=new BaseDAO();
		}
		return dao;
	}
//	private boolean isSetDs=false;
	/**
	 * 通过账套编码设置数据源
	 * @param account
	 * @return
	 */
//	private String SetDSByByAccountCode(String account){
//		String errInfo="";
//		try {
////			String dnsName=AccountXMLUtil.findDsNameByAccountCode(account);
//			if (isSetDs) {
//				return "succ";
//			}
//			InvocationInfoProxy.getInstance().setUserDataSource("nc65");
//			isSetDs=true;
//			return "succ";
//		} catch (Exception e) {
//			errInfo=e.toString();
//		}
//		return errInfo;
//		
//	}
	/**
	 * 备份xml 字符串
	 * @param xmlStr
	 */
	private static  void writeXmlToFile(String xmlStr) {
		String fileName = (new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
		FileWriter out = null;
		try {
			String path = RuntimeEnv.getInstance().getNCHome() + File.separator
					+ "pfxx" + File.separator + "synchdata";
			File file = new File(path);
			if (!file.exists()) {
				file.mkdirs();
			}
			fileName = path + File.separator + fileName + ".txt";
			out = new FileWriter(fileName, false);
			out.write(xmlStr);
			Logger.debug("备份原始报文文件成功，文件名称：" + fileName);
		} catch (Exception e) {
			Logger.error("备份原始报文文件失败:" + e.getMessage(), e);
		} finally {
			try {
				out.flush();
				out.close();
			} catch (IOException ex) {
			}
		}
	}
	private static String getSysparam(String  type,String name){
		String sql="select defaultvalue from pub_sysinittemp where INITCODE='"+name+"' and pk_orgtype='"+type+"'";
		Object obj=null;
		try {
			obj =	getDao().executeQuery(sql, new ColumnProcessor());
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(obj!=null){
			return obj.toString();
		}
		return null;
	}
	/**
	 * 返回错误信息
	 * @param msg
	 * @return
	 */
	private  static  String getErrorReturnXml(String msg) {
		return "<?xml version='1.0' encoding='UTF-8'?>" + "<ufinterface>"
				+ "<sendresult>" + "<billpk/>" + "<bdocid/>" + "<filename/>"
				+ "<resultcode>-1</resultcode>" + "<resultdescription>" + msg
				+ "</resultdescription>" + "<content/>" + "</sendresult>"
				+ "</ufinterface>";
	}

}
