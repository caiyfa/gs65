package nc.bs.gs65.backgroundwork;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.w3c.dom.*;

import nc.bs.dao.BaseDAO;
import nc.bs.gs65.util.xml.QueryAction;
import nc.bs.gs65.util.xml.VoucherXmlCreateAction;
import nc.bs.gs65.util.xml.XMLUtil;
import nc.bs.pub.pa.PreAlertObject;
import nc.bs.pub.taskcenter.BgWorkingContext;
import nc.bs.pub.taskcenter.IBackgroundWorkPlugin;
import nc.gs.vo.DetailVO;
import nc.gs.vo.FreevalueVO;
import nc.gs.vo.VoucherRecordVO;
import nc.gs.vo.VoucherVO;
import nc.jdbc.framework.generator.SequenceGenerator;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.jdbc.framework.processor.MapProcessor;
import nc.jdbc.framework.util.SQLHelper;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDouble;

public class MakeVoucherFromMidTablePlugin implements IBackgroundWorkPlugin {
	private static BaseDAO dao = null;

	private BaseDAO getDao() {
		if (dao == null) {
			dao = new BaseDAO();
		}
		return dao;
	}
/*	private void parepareData() throws BusinessException{
			VoucherRecordVO vo=new VoucherRecordVO();
			vo.setPk_corp("101");
			vo.setAbs_summary("test1");
			vo.setVapprovedate("2020-06-03");
			vo.setLocal_credit_currency(new UFDouble(0.0000));
			vo.setLocal_debtor_currency(new UFDouble(2000.0000));
			vo.setOriginal_credit_currency(new UFDouble(0.0000));
			vo.setOriginal_debtor_currency(new UFDouble(2000.0000));
			vo.setPk_currtype("CNY");
			vo.setVoperatorid("yy01");
			vo.setMakedate("2020-06-03");
			vo.setSubjcode("1002");
			vo.setDef1("002");
			vo.setDetailindex(1);
			vo.setHfm_group_batch_id("batch01");
			getDao().insertVO(vo);
			
			VoucherRecordVO vo1=new VoucherRecordVO();
			vo1.setPk_corp("101");
			vo1.setAbs_summary("test2");
			vo1.setVapprovedate("2020-06-03");
			vo1.setLocal_credit_currency(new UFDouble(2000.0000));
			vo1.setLocal_debtor_currency(new UFDouble(0.0000));
			vo1.setOriginal_credit_currency(new UFDouble(2000.0000));
			vo1.setOriginal_debtor_currency(new UFDouble(0.0000));
			vo1.setPk_currtype("CNY");
			vo1.setVoperatorid("yy01");
			vo1.setMakedate("2020-06-03");
			vo1.setSubjcode("1011");
			vo1.setDef1("001");
			vo1.setDetailindex(2);
			vo1.setHfm_group_batch_id("batch01");
			getDao().insertVO(vo1);
	} */
	
	private  Date date=null;
	
	@SuppressWarnings("unchecked")
	public PreAlertObject executeTask(BgWorkingContext bgwc) throws BusinessException {
		
			
//		parepareData();
		PreAlertObject obj=new PreAlertObject();
		try{
			this.date=new Date();
			VoucherXmlCreateAction.setConf(date,QueryAction.querySystemTmpParam("GL136"));
//		parepareData();
		//修改取数逻辑
			String timeCondition=this.getTimeCondition(bgwc);
		
//		 String date = bgwc.getKeyMap().get("date") == null ? "" : bgwc
//				.getKeyMap().get("date").toString().trim();
//		// 若没有参数默认为当前系统时间
//		if (date.length() == 0) {
//			Date now = new Date();
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//			date = sdf.format(now);
//		}else{
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//			try {
//				sdf.parse(date);
//			} catch (ParseException e) {
//				// TODO Auto-generated catch block
//				throw new BusinessException("时间格式错误，请按照 yyyy-MM-dd 填写时间");
//			}
//		}
		//(nvl(scbz,-1)=-1 )or  按照要求，生成凭证失败的数据也需要进入生成凭证的流程 nvl(gs_voucher_record.pk_vocher,'1')<>'1' and 
		Collection<VoucherRecordVO> list = getDao().retrieveByClause( 
				VoucherRecordVO.class,
				timeCondition+" and nvl(dr,0)=0 and ((  (select case when exists(select * from gl_voucher gv where gs_voucher_record.pk_vocher=gv.pk_voucher)then 1 else 0 end  from dual )=0)) order by hfm_group_batch_id ");

		if (list == null || list.size() == 0) {
		 
			 throw new BusinessException("GS_VOUCHER_RECORD表中未查询到数据");
		}
		VoucherRecordVO[] voucherRecVOS = list.toArray(new VoucherRecordVO[0]);
		
		Map<String,List<VoucherRecordVO>> recordListMap=new HashMap<String, List<VoucherRecordVO>>();//每一个组批号就是一个凭证 根据组批号进行归类
		for(VoucherRecordVO vo:voucherRecVOS){
			if(recordListMap.containsKey(vo.getHfm_group_batch_id())){
				recordListMap.get(vo.getHfm_group_batch_id()).add(vo);
			}else{
				List<VoucherRecordVO> tmp=new ArrayList<VoucherRecordVO>(); 
				tmp.add(vo);
				recordListMap.put(vo.getHfm_group_batch_id(),tmp );
			}
		}
		Set<String> keys=recordListMap.keySet();
		this.date=new Date();
		int count=0;
		String jsonStr=QueryAction.querySystemTmpParam("GL135");
		for(String key:keys){
			List<VoucherRecordVO> tmp=recordListMap.get(key);
			try {
				doAction(tmp,jsonStr);
				count++;
			} catch (Exception e) {
				// TODO: handle exception
				int res=0;
				for(VoucherRecordVO vo:tmp){
					vo.setDef11(e.getMessage());
					vo.setScbz(1);
//					String sql="update gs_voucher_record set scbz=1 , def11='"+e.getMessage()+"' where pk_voucher_record='"+vo.getPk_voucher_record()+"' and nvl(dr,0)=0" ;
//					res=getDao().executeUpdate(sql);
//					System.out.println(res);
				}
				res=getDao().updateVOArray(tmp.toArray(new VoucherRecordVO[]{}));
				System.out.println(res);
			}
			
		} 
		
	 

		
		obj.setReturnObj( "生成了"+count+"条凭证");
		}catch(Exception e){
			throw new BusinessException(e);
		}
		return obj;
		
		}
	/**
	 * 缓存对方科目编码
	 */
//	private Map<String,String> oppoSubj=new HashMap<String, String>();
//	private void  loadOppositSubj(List<VoucherRecordVO> recordVOS,Map<String,String> infoMap){
//		for(VoucherRecordVO vo:recordVOS){
//			if((UFDouble.ZERO_DBL.equals(vo.getLocal_credit_currency()))&&(!UFDouble.ZERO_DBL.equals(vo.getLocal_debtor_currency()))){//本币贷方为零借方不为零则为贷
////				detailVO.setDirection("D");
//				 if(oppoSubj.containsKey("D")){
//					 oppoSubj.put("D", oppoSubj.get("D")+","+infoMap.get(vo.getSubjcode()));
//				 }else{
//					 oppoSubj.put("D",infoMap.get(vo.getSubjcode()));
//				 }
//			}
//			if((!UFDouble.ZERO_DBL.equals(vo.getLocal_credit_currency()))&&(UFDouble.ZERO_DBL.equals(vo.getLocal_debtor_currency()))){//本币借方为零贷方不为零则为借
////				detailVO.setDirection("C");
//				if(oppoSubj.containsKey("C")){
//					 oppoSubj.put("C", oppoSubj.get("C")+","+infoMap.get(vo.getSubjcode()));
//				}else{
//					oppoSubj.put("C",infoMap.get(vo.getSubjcode()));
//				}
//			}
//		}
//	}
	
	/**
	 * 
	 * @param recordVOS 该数组中保存的是多个凭证分录加起来合成一个凭证
	 * @throws Exception
	 */
	private void doAction(List<VoucherRecordVO> recordVOS,String jsonStr) throws Exception{ 
		//校验金额
		checkFunds(recordVOS);
		//校验公司是否一致
		checkPk_Corp(recordVOS);
		//生成报文
		String xml=VoucherXmlCreateAction.prepareVoucherXML(recordVOS,jsonStr);
		//报文发送
		String res=MakeVoucherAction.doSendMessage(xml);
		//结果解析
		Map<String,String> resMap=XMLUtil.analysisXML(res.replaceFirst("UTF-8", "GBK"), "sendresult");
		
		if("1".equals(resMap.get("resultcode"))){
			String pk_voucher=resMap.get("content");
			
			for(VoucherRecordVO voucher:recordVOS){
				voucher.setPk_vocher(pk_voucher);
			}
			this.getDao().updateVOList(recordVOS);
			
		}else{
			throw new Exception(resMap.get("resultdescription"));
		}
		
      /*  BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("D:\\Temp\\xml\\voucher.xml")));
        bw.write(xml);// 往已有的文件上添加字符串
        bw.close();*/
		
		
		
	}
	private String getTimeCondition(BgWorkingContext bgwc)throws BusinessException {
		String timeCondition ="";
		try {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String from = bgwc.getKeyMap().get("from") == null ? "" : bgwc
				.getKeyMap().get("from").toString().trim();
		String to = bgwc.getKeyMap().get("to") == null ? "" : bgwc
				.getKeyMap().get("to").toString().trim();
		
		if((from==null||from.length()==0)&&(to==null||to.length()==0)){
			//没有设置from 和to 参数
			timeCondition=VoucherRecordVO.MAKEDATE + " = '" + sdf.format(date) + "'";
			return timeCondition;
		}
		if(from!=null&&from.length()!=0&&isDateParamLegal(from)&&(to==null||to.length()==0)){
			
				if(sdf.parse(from).getTime()>date.getTime()){
					throw new BusinessException("单独设置的from参数:"+from+" 不能晚于服务器时间 "+sdf.format(date));
				}
				timeCondition=VoucherRecordVO.MAKEDATE + " <= '" + sdf.format(date) + "' and "+VoucherRecordVO.MAKEDATE + " >= '" + from + "' ";
				return timeCondition;
			
		}
		if((from==null||from.length()==0)&&(to!=null||to.length()!=0)){
			throw new BusinessException("不能单独设置to参数:"+to);
		}
		if((from!=null&&from.length()!=0&&isDateParamLegal(from))&&(to!=null&&to.length()!=0&&isDateParamLegal(to))){
			if(sdf.parse(from).getTime()>sdf.parse(to).getTime()){
				throw new BusinessException("起始时间:"+from +" 不能晚于结束时间:"+to);
			}
			timeCondition=VoucherRecordVO.MAKEDATE + " <= '" + to + "' and "+VoucherRecordVO.MAKEDATE + " >= '" + from + "' ";
			return timeCondition;
		}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			throw new BusinessException(e);
		}
		return timeCondition;
	}
	
	/**
	 * 校验时间参数合法性
	 * @param timeparam
	 * @return
	 * @throws BusinessException
	 */
	private boolean  isDateParamLegal(String timeparam)throws BusinessException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			sdf.parse(timeparam);
		} catch (ParseException e) {
			throw new BusinessException(timeparam+"时间格式错误，请按照 yyyy-MM-dd 填写时间");
		}
		return true;
	}
	
	private void checkPk_Corp(List<VoucherRecordVO> recordVOS)throws Exception{
		List<String> tmp=new ArrayList<String>();
		for(VoucherRecordVO vo:recordVOS){ 
			if(vo.getPk_corp()==null||vo.getPk_corp().trim().length()==0){
				throw new Exception("公司不可为空");
			}
			if(!tmp.contains(vo.getPk_corp())&&tmp.size()>1){
				throw new Exception("同一凭证附属于一家公司，而不能由多家公司构成");
			}
			tmp.add(vo.getPk_corp());
		}
		
		
	}
	

	 
	
	
	
	
	/**
	 * 辅助核算项类型
	 */
	private Map<String,String> bdinfoMap=new HashMap<String, String>();
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void getbd_bdinfo() throws BusinessException   {
		String sql = " select bb.bdname,bb.pk_bdinfo from bd_bdinfo bb where bb.bdname in ('银行账户','部门档案','证券档案','客户辅助核算','证券交易所','人员档案','项目辅助核算','客商辅助核算')";
		List list = (List) getDao().executeQuery(
				sql, new MapListProcessor());
		if(list!=null){
			int size=list.size();
			for(int i=0;i<size;i++){
				Map<String,String> map=(Map<String, String>) list.get(i);
				bdinfoMap.put(map.get("bdname"), map.get("pk_bdinfo"));
			}
		}
//		int i=0;
//		System.out.println(i);

	}
	@SuppressWarnings("unchecked")
	private String getFreeValueID() throws Exception{
		String[] pks = new SequenceGenerator(null).generate(SQLHelper.getCorpPk(),1);
		String sql=" select freevalueid from gl_freevalue gf where gf.freevalueid='"+pks[0]+"'";
		Map<String,String> map=(Map<String, String>) getDao().executeQuery(sql, new MapProcessor());
		if(map==null||map.size()==0||map.get("freevalueid")==null){
			return  pks[0];
		}else{
			getFreeValueID();
		}
		return null;
	}
	@SuppressWarnings("unchecked")
	private String getVoucherID()throws Exception{
		String[] pks = new SequenceGenerator(null).generate(SQLHelper.getCorpPk(),1);
		String sql=" select pk_voucher from gl_voucher where pk_voucher='"+pks[0]+"'";
		Map<String,String> map=(Map<String, String>) getDao().executeQuery(sql, new MapProcessor());
		if(map==null||map.size()==0||map.get("gl_voucher")==null){
			return  pks[0];
		}else{
			getVoucherID();
		}
		return null;
	}
	

	/**
	 * 校验资金数据 
	 * creditTotal 贷方总和
	 * debtorTotal 借方总和
	 * @param recordVOS
	 */
	private Map<String,UFDouble> checkFunds(List<VoucherRecordVO> recordVOS)throws Exception{
		UFDouble local_credit_currency=new UFDouble(0.0000);//本币贷
		UFDouble local_debtor_currency=new UFDouble(0.0000);//本币借
		UFDouble original_credit_currency=new UFDouble(0.0000);//原币贷
		UFDouble original_debtor_currency=new UFDouble(0.0000);//原币借
		for(VoucherRecordVO vo :recordVOS){
			if(vo.getLocal_credit_currency()!=null)
			local_credit_currency=local_credit_currency.add(vo.getLocal_credit_currency());
			if(vo.getLocal_debtor_currency()!=null)
			local_debtor_currency=local_debtor_currency.add(vo.getLocal_debtor_currency());
			if(vo.getOriginal_credit_currency()!=null)
			original_credit_currency=original_credit_currency.add(vo.getOriginal_credit_currency());
			if(vo.getOriginal_debtor_currency()!=null)
			original_debtor_currency=original_debtor_currency.add(vo.getOriginal_debtor_currency());
		}
		if(!local_credit_currency.equals(local_debtor_currency)){
			throw new Exception("本币发生额不等");
		}
		if(!original_credit_currency.equals(local_debtor_currency)){
			throw new Exception("原币发生额不等");
		}
		Map<String,UFDouble> res=new HashMap<String, UFDouble>();
		
		res.put("creditTotal", local_credit_currency);
		res.put("debtorTotal", local_debtor_currency);
		
		return res;
	}
	/**
	 * 	同一个公司、账簿主体、凭证类型。凭证号唯一
	 * @param infoMap
	 * @param corpcode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private Integer prepareVoucherNO(Map<String,String> infoMap,String corpcode) throws Exception{
		String sql="select  max(gv.no) maxno " +
				"from gl_voucher gv " +
				"where   gv.pk_glorgbook='"+infoMap.get("pk_glorgbook")+"'  and gv.period='"+new SimpleDateFormat("MM").format(date)+"'" +
				"and gv.pk_vouchertype='"+infoMap.get("pk_vouchertype")+"'" +
				"and gv.pk_corp=(select bc.pk_corp from bd_corp bc where bc.unitcode='"+corpcode+"' and nvl(bc.dr,0)=0 ) and nvl(gv.dr,0)=0";
		Map<String,Integer> map=(Map<String, Integer>) getDao().executeQuery(sql, new MapProcessor());
		if(map==null||map.get("maxno")==null){
			return 1;
		}
		 
		return map.get("maxno")+1;
	}
	/**
	 * @param corpcode
	 * @return 由公司编码获得 账簿相关信息以及凭证类型
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private Map<String,String> getGlBookInfos(String corpcode) throws Exception{
		String sql="select bg.pk_glorg,bgg.pk_glorgbook,bgg.pk_glbook ,bc.pk_corp  ,bv.pk_vouchertype "+
				"from bd_glorg bg,bd_glorgbook bgg,bd_vouchertype bv,bd_corp bc  " +
				"where bg.pk_glorg=bgg.pk_glorg and bgg.pk_glorgbook=bv.pk_glorgbook " +
				"and bg.glorgcode=bc.unitcode and bg.glorgcode='"+corpcode+"' and nvl(bg.dr,0)=0 and nvl(bgg.dr,0)=0 and nvl(bv.dr,0)=0 and nvl(bc.dr,0)=0";
		Map<String,String> map=(Map<String, String>) getDao().executeQuery(sql, new MapProcessor());
		if(map==null){
			throw new BusinessException("公司："+corpcode+"未查找到信息");
		}
		if(map.get("pk_corp")==null||map.get("pk_corp").trim().length()==0){
			throw new BusinessException("公司："+corpcode+"未查找到该公司信息");
		}
		if(map.get("pk_glorg")==null||map.get("pk_glorg").trim().length()==0){
			throw new BusinessException("公司："+corpcode+"未查找到主体信息");
		}
		if(map.get("pk_glorgbook")==null||map.get("pk_glorgbook").trim().length()==0){
			throw new BusinessException("公司："+corpcode+"未查找到主体账簿信息");
		}
		if(map.get("pk_glbook")==null||map.get("pk_glbook").trim().length()==0){
			throw new BusinessException("公司："+corpcode+"未查找到账簿信息");
		}
		if(map.get("pk_vouchertype")==null||map.get("pk_vouchertype").trim().length()==0){
			throw new BusinessException("公司："+corpcode+"未查找到凭证类别信息");
		}
		return map;
	}
 
	/**
	 * 校验数据是否可用
	 * 
	 * @param vo
	 * @return
	 */
/*	private StringBuilder checkVO(VoucherRecordVO vo) {
		StringBuilder tmpsb = new StringBuilder();
		if (vo == null) {
			tmpsb.append("数据错误");
			return tmpsb;
		}
	 
		if (isEmpty(vo.getPk_currtype())) {
			tmpsb.append("币种为空.");
		}
	 
		if (isEmpty(vo.getMakedate())) {
			tmpsb.append("制单日期为空.");
		}
		if (isEmpty(vo.getSubjcode())) {
			tmpsb.append("科目编码为空.");
		}
		if (isEmpty(vo.getVoperatorid())) {
			tmpsb.append("制单人为空");
		}
		if (isEmpty(vo.getPk_system())) {
			tmpsb.append("来源系统为空.");
		}
		
		 * if((!isEmpty(vo.getLocal_credit_currency()))&&(vo.
		 * getLocal_credit_currency().equals(vo.getLocal_debtor_currency()))){
		 * tmpsb.append("本币借方贷方不一致");
		 * 
		 * }
		 
		return tmpsb;
	}*/
	private boolean isEmpty(Object obj) {
		if (obj == null || obj.toString().trim().length() == 0) {
			return true;
		}
		return false;
	}
}
