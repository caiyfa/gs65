package nc.bs.gs65.util.xml;

import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.jdbc.framework.processor.MapProcessor;

public class QueryAction {
	public static String querySystemTmpParam(String initCode)throws Exception{
		
		String param= queryValueByCondition("pub_sysinittemp","defaultvalue","initcode='"+initCode+"'");
		if(param==null&&param.trim().length()==0){
			throw new Exception("参数"+initCode+"为空。请到参数模板配置节点进行配置");
		}
		return param;
	}
	
	
	public static String queryValueByCondition(String tableName,String  fieldName,String condition) throws Exception{
		Map<String,String> res=queryByCondition(tableName,fieldName,condition);
		if(res==null){
			return null;
		}
		String key=res.keySet().toArray(new String[0])[0];
		return res.get(key);
		
	}
	public static Map<String,String> queryByCondition(String tableName,String  fieldName,String condition) throws Exception{
		return queryByCondition(tableName,null,new String[]{fieldName},condition,false);
	}
	public static Map<String,String> queryByCondition(String tableName,String[] fieldName,String condition) throws Exception{
		return queryByCondition(tableName,null,fieldName,condition,false);
	}
	@SuppressWarnings("unchecked")
	public static Map<String,String> queryByCondition(String tableName,String pk_corp,String[] fieldName,String condition,boolean withCorp) throws Exception{
		StringBuilder sb =new StringBuilder();
		sb.append("select");
		for(int i=0;i<fieldName.length-1;i++){
			sb.append(" "+fieldName[i]+",");
		}
		sb.append(" "+fieldName[fieldName.length-1]);
		sb.append(" from "+ tableName);
		sb.append(" where ");
		if(withCorp){
			sb.append(" pk_corp='"+pk_corp+"' and ");
		}
		 
		sb.append("nvl(dr,0)=0    " );
		if(condition!=null){
			if(condition.trim().startsWith("and")||condition.trim().startsWith("AND")){
				sb.append(condition);
			}else {
				sb.append(" and ").append(condition);
			}
		}
		Map<String,String> map=(Map<String, String>) getDao().executeQuery(sb.toString(), new MapProcessor());
		 
		return map;
	}
	private static BaseDAO dao = null;
	private  static BaseDAO getDao() {
		if (dao == null) {
			dao = new BaseDAO();
		}
		return dao;
	}
}
