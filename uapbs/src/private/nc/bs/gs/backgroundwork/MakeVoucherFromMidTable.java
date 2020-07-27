package nc.bs.gs.backgroundwork;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.bs.dao.BaseDAO;
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
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDouble;

import nc.bs.pub.pa.PreAlertObject;

/**
 * 	国盛由汉得推送数据生成凭证接口
 * @author CYF 2019-03-04
 *
 */
public class MakeVoucherFromMidTable implements IBackgroundWorkPlugin {
	private static BaseDAO dao = null;

	private BaseDAO getDao() {
		if (dao == null) {
			dao = new BaseDAO();
		}
		return dao;
	}
/*	@SuppressWarnings("unused")
	private void parepareData() throws BusinessException{
			VoucherRecordVO vo=new VoucherRecordVO();
			vo.setPk_corp("1001");
			vo.setAbs_summary("测试记录2");
			vo.setVapprovedate("2019-02-13");
			vo.setLocal_credit_currency(new UFDouble(0.0000));
			vo.setLocal_debtor_currency(new UFDouble(2000.0000));
			vo.setOriginal_credit_currency(new UFDouble(0.0000));
			vo.setOriginal_debtor_currency(new UFDouble(2000.0000));
			vo.setPk_system("GL");
			vo.setFjzs(1);
			vo.setDef5("01");
			vo.setPk_currtype("CNY");
//			vo.setVapproveid("XNT001");
			vo.setVoperatorid("01");
			vo.setMakedate("2019-02-26");
			vo.setSubjcode("6602");
			vo.setPk_vouchertypev("1001AA10000000000EG1");
			vo.setDetailindex(1);
			vo.setHfm_group_batch_id("batch01");
			getDao().insertVO(vo);
			
		
	}*/
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
	
	@SuppressWarnings("unchecked")
	public PreAlertObject executeTask(BgWorkingContext bgwc) throws BusinessException {
		
		PreAlertObject obj=new PreAlertObject();
			this.date=new Date();
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
		getbd_bdinfo();//加载辅助核算类型
		int count=0;
		for(String key:keys){
			List<VoucherRecordVO> tmp=recordListMap.get(key);
			try {
				doAction(tmp);
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
		return obj;
		}
	/**
	 * 缓存对方科目编码
	 */
	private Map<String,String> oppoSubj=new HashMap<String, String>();
	private void  loadOppositSubj(List<VoucherRecordVO> recordVOS,Map<String,String> infoMap){
		for(VoucherRecordVO vo:recordVOS){
			if((UFDouble.ZERO_DBL.equals(vo.getLocal_credit_currency()))&&(!UFDouble.ZERO_DBL.equals(vo.getLocal_debtor_currency()))){//本币贷方为零借方不为零则为贷
//				detailVO.setDirection("D");
				 if(oppoSubj.containsKey("D")){
					 oppoSubj.put("D", oppoSubj.get("D")+","+infoMap.get(vo.getSubjcode()));
				 }else{
					 oppoSubj.put("D",infoMap.get(vo.getSubjcode()));
				 }
			}
			if((!UFDouble.ZERO_DBL.equals(vo.getLocal_credit_currency()))&&(UFDouble.ZERO_DBL.equals(vo.getLocal_debtor_currency()))){//本币借方为零贷方不为零则为借
//				detailVO.setDirection("C");
				if(oppoSubj.containsKey("C")){
					 oppoSubj.put("C", oppoSubj.get("C")+","+infoMap.get(vo.getSubjcode()));
				}else{
					oppoSubj.put("C",infoMap.get(vo.getSubjcode()));
				}
			}
		}
	}
	
	/**
	 * 
	 * @param recordVOS 该数组中保存的是多个凭证分录加起来合成一个凭证
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void doAction(List<VoucherRecordVO> recordVOS) throws Exception{
		if(recordVOS==null||recordVOS.size()==0){
			return ;
		}
		String corpcode=recordVOS.get(0).getPk_corp();//资金传输过来的是公司编码不是主键
		if(corpcode==null||corpcode.trim().length()==0){
			throw new Exception("缺失公司编码");
		}
		Map<String,String> infoMap=getGlBookInfos(corpcode);//pk_glorg 主体;pk_glbook 账簿;pk_glorgbook 账簿主体;pk_vouchertype 凭证类别; pk_corp 公司主键\
		Map<String, String> subMap=new HashMap<String, String>();
		
		for(VoucherRecordVO vo:recordVOS){
			String sql="select ba.pk_accsubj from bd_accsubj ba " +
					"where ba.subjcode='"+vo.getSubjcode()+"'  " +
							"and ba.pk_glorgbook=(" +
							"select bgg.pk_glorgbook from bd_glorgbook bgg where bgg.pk_glorg=" +
							"(select bg.pk_glorg from bd_glorg bg where bg.glorgcode=" +
							"(select bd_corp.unitcode from bd_corp where pk_corp='"+infoMap.get("pk_corp")+"')))";
			Map<String,String> map=(Map<String, String>) getDao().executeQuery(sql, new MapProcessor());
			if(map==null||map.get("pk_accsubj")==null||map.get("pk_accsubj").trim().length()==0){
				throw new Exception(infoMap.get("pk_corp")+"该公司下未查找到科目"+vo.getSubjcode());
			}
			subMap.put(vo.getSubjcode(), map.get("pk_accsubj"));
		}
		loadOppositSubj(recordVOS,subMap);//加载对方科目
		infoMap.put("corpcode", corpcode);//公司编码
		infoMap.put("pk_vouchertype", recordVOS.get(0).getPk_vouchertypev());//根据资金推送的数据获取凭证类别，覆盖之前的类别
		Integer voucherNO=prepareVoucherNO(infoMap,corpcode);//获得凭证号
		Map<String,UFDouble> totalFunds=checkFunds(recordVOS);//校验资金数据
		VoucherVO voucherVO=makeVoucher(recordVOS, infoMap, voucherNO, totalFunds);//生成凭证
		
		
		for(VoucherRecordVO vo:recordVOS){
			vo.setPk_vocher(voucherVO.getPk_voucher());
			String pk_detail=makeDetail(vo, infoMap.get("pk_corp"), infoMap,subMap, voucherNO);
			vo.setPk_detail(pk_detail);
			vo.setScbz(0);//生成成功
			vo.setDef11("");
			getDao().updateVO(vo);
			
		}
		getDao().insertVOWithPK(voucherVO);//在凭证分录生成之后生成凭证信息。保证如果凭证分录生成失败不会多出空的凭证
		oppoSubj.clear();//释放对方科目
		
	}
	
	private String makeDetail(VoucherRecordVO vo,String pk_corp,Map<String,String> infoMap,Map<String, String> subMap,Integer voucherNO) throws Exception{
		DetailVO detailVO=new DetailVO();
		detailVO.setNov(voucherNO);
		detailVO.setAssid("");//辅助核算标志
		detailVO.setCreditamount(vo.getOriginal_credit_currency());// 原币贷方
		detailVO.setCreditquantity(UFDouble.ZERO_DBL);//贷方数量
		detailVO.setDebitamount(vo.getOriginal_debtor_currency());// 原币借方
		detailVO.setDebitquantity(UFDouble.ZERO_DBL);//借方数量
		detailVO.setDetailindex(vo.getDetailindex());// 分录号
		detailVO.setExcrate1(UFDouble.ZERO_DBL);//汇率1
		detailVO.setExcrate2(UFDouble.ONE_DBL);//汇率2
		detailVO.setExplanation(vo.getAbs_summary());// 摘要
		detailVO.setFraccreditamount(UFDouble.ZERO_DBL);//辅助贷发生额
		detailVO.setFracdebitamount(UFDouble.ZERO_DBL);//辅助借发生额
		detailVO.setLocalcreditamount(vo.getLocal_credit_currency());// 本币贷方
		detailVO.setLocaldebitamount(vo.getLocal_debtor_currency());// 本币借方
		detailVO.setModifyflag("YYYYYYYYYYYYYYYY");//修改标志
		detailVO.setPk_accsubj(subMap.get(vo.getSubjcode()));
		detailVO.setPk_corp(pk_corp);// 公司编码
		detailVO.setDr(0);
		if("CNY".equals(vo.getPk_currtype()))
		detailVO.setPk_currtype("00010000000000000001");// 币种
		
		detailVO.setPk_voucher(vo.getPk_vocher());// 凭证主键
		detailVO.setPrice(UFDouble.ZERO_DBL);//单价
		detailVO.setPk_glbook(infoMap.get("pk_glbook"));// 账簿
		detailVO.setPk_glorg(infoMap.get("pk_glorg"));// 主体
		detailVO.setPk_glorgbook(infoMap.get("pk_glorgbook"));// 账簿主体
		//设置借贷方向
		if((UFDouble.ZERO_DBL.equals(vo.getLocal_credit_currency()))&&(!UFDouble.ZERO_DBL.equals(vo.getLocal_debtor_currency()))){//本币贷方为零借方不为零则为贷
			detailVO.setDirection("D");
			detailVO.setOppositesubj(oppoSubj.get("C"));
		}
		if((!UFDouble.ZERO_DBL.equals(vo.getLocal_credit_currency()))&&(UFDouble.ZERO_DBL.equals(vo.getLocal_debtor_currency()))){//本币借方为零贷方不为零则为借
			detailVO.setDirection("C");
			detailVO.setOppositesubj(oppoSubj.get("D"));
		}
		detailVO.setDiscardflagv(new UFBoolean(false));// 作废标志
		detailVO.setPeriodv(new SimpleDateFormat("MM").format(date));// 会计期间
		detailVO.setFree6(new SimpleDateFormat("MM").format(date));
		detailVO.setPk_managerv("N/A");//在这里生成凭证是没有记账人的
		detailVO.setPk_systemv(vo.getPk_system());// 来源系统
		detailVO.setPk_vouchertypev(infoMap.get("pk_vouchertype"));//凭证类型
		detailVO.setPrepareddatev(new UFDate(vo.getMakedate()));// 制单日期
		detailVO.setVoucherkindv(0);//凭证类型
		detailVO.setYearv(new SimpleDateFormat("yyyy").format(date));// 会计年度
		detailVO.setIsdifflag(new UFBoolean(false));// 是否差异凭证
		
		//生成辅助核算
		String assid=makeAss(vo,pk_corp,infoMap,subMap);
		detailVO.setAssid(assid);// 辅助核算
		if(vo.getVapprovedate()!=null)
		detailVO.setCheckdate(new UFDate(vo.getVapprovedate()));// 审核日期
 		return getDao().insertVO(detailVO);
	}
	private String makeAss(VoucherRecordVO vo,String pk_corp,Map<String,String> infoMap,Map<String, String> subMap)throws Exception{
		String freevalueid=getFreeValueID();
		if(freevalueid==null){
			freevalueid=getFreeValueID();
		}
		boolean isAssNeeded=false;
		List<FreevalueVO> freevalueVOS=new ArrayList<FreevalueVO>();
		if(!isEmpty(vo.getDef1())){//部门档案
			String[] fieldName={"pk_deptdoc","deptcode","deptname"};
			String condition="deptcode='"+vo.getDef1()+"'";
			Map<String,String> deptMap=getFreevalueInfo("bd_deptdoc",pk_corp,fieldName,condition,true);
			if(deptMap==null){
				throw new Exception("查找不到 "+vo.getDef1()+" 部门.");
			}
			FreevalueVO freevalue=new FreevalueVO();
			freevalue.setAssindex(0);
			freevalue.setCheckcount(0);
			freevalue.setChecktype(bdinfoMap.get("部门档案"));
			freevalue.setCheckvalue(deptMap.get("pk_deptdoc"));
			freevalue.setDr(0);
			freevalue.setFreevalueid(freevalueid);
			freevalue.setValuecode(deptMap.get("deptcode"));
			freevalue.setValuename(deptMap.get("deptname"));
			freevalueVOS.add(freevalue);
			isAssNeeded=true;
		}
		if(!isEmpty(vo.getDef2())){//银行账户
			String[] fieldName={"pk_bankaccbas","accountcode","accountname"};
			String condition="accountcode='"+vo.getDef2()+"'";
			Map<String,String> accMap=getFreevalueInfo("bd_bankaccbas",pk_corp,fieldName,condition,false);
			if(accMap==null){
				throw new Exception("查找不到 "+vo.getDef2()+" 账户.");
			}
			FreevalueVO freevalue=new FreevalueVO();
			freevalue.setAssindex(0);
			freevalue.setCheckcount(0);
			freevalue.setChecktype(bdinfoMap.get("银行账户"));
			freevalue.setCheckvalue(accMap.get("pk_bankaccbas"));
			freevalue.setDr(0);
			freevalue.setFreevalueid(freevalueid);
			freevalue.setValuecode(accMap.get("accountcode"));
			freevalue.setValuename(accMap.get("accountname"));
			freevalueVOS.add(freevalue);
			isAssNeeded=true;
		}
		if(!isEmpty(vo.getDef3())){//证券档案
			String[] fieldName={"pk_securities","code","name"};
			String condition="code='"+vo.getDef3()+"'";
			Map<String,String> secureMap=getFreevalueInfo("bd_bankaccbas",pk_corp,fieldName,condition,true);
			if(secureMap==null){
				throw new Exception("查找不到 "+vo.getDef3()+" 证券档案.");
			}
			FreevalueVO freevalue=new FreevalueVO();
			freevalue.setAssindex(0);
			freevalue.setCheckcount(0);
			freevalue.setChecktype(bdinfoMap.get("证券档案"));
			freevalue.setCheckvalue(secureMap.get("pk_securities"));
			freevalue.setDr(0);
			freevalue.setFreevalueid(freevalueid);
			freevalue.setValuecode(secureMap.get("code"));
			freevalue.setValuename(secureMap.get("name"));
			freevalueVOS.add(freevalue);
			isAssNeeded=true;
		}
		if(!isEmpty(vo.getDef4())){//客户辅助核算
			String[] fieldName={"pk_cubasdoc","custcode","custname"};
			String condition="custcode='"+vo.getDef4()+"'";
			Map<String,String> secureMap=getFreevalueInfo("bd_cubasdoc",pk_corp,fieldName,condition,true);
			if(secureMap==null){
				throw new Exception("查找不到 "+vo.getDef4()+" 客户辅助核算.");
			}
			FreevalueVO freevalue=new FreevalueVO();
			freevalue.setAssindex(0);
			freevalue.setCheckcount(0);
			freevalue.setChecktype(bdinfoMap.get("客户辅助核算"));
			freevalue.setCheckvalue(secureMap.get("pk_cubasdoc"));
			freevalue.setDr(0);
			freevalue.setFreevalueid(freevalueid);
			freevalue.setValuecode(secureMap.get("custcode"));
			freevalue.setValuename(secureMap.get("custname"));
			freevalueVOS.add(freevalue);
			isAssNeeded=true;
		}
		if(!isEmpty(vo.getDef5())){//证券交易所
			String[] fieldName={"pk_bourse","code","name"};
			String condition="code='"+vo.getDef5()+"'";
			Map<String,String> cubasMap=getFreevalueInfo("sec_bourse",pk_corp,fieldName,condition,false);
			if(cubasMap==null){
				throw new Exception("查找不到 "+vo.getDef5()+" 证券交易所.");
			}
			FreevalueVO freevalue=new FreevalueVO();
			freevalue.setAssindex(0);
			freevalue.setCheckcount(0);
			freevalue.setChecktype(bdinfoMap.get("证券交易所"));
			freevalue.setCheckvalue(cubasMap.get("pk_bourse"));
			freevalue.setDr(0);
			freevalue.setFreevalueid(freevalueid);
			freevalue.setValuecode(cubasMap.get("code"));
			freevalue.setValuename(cubasMap.get("name"));
			freevalueVOS.add(freevalue);
			isAssNeeded=true;
		}
		if(!isEmpty(vo.getDef6())){//人员档案
			String[] fieldName={"pk_psndoc","psncode","psnname"};
			String condition="psncode='"+vo.getDef6()+"'";
			Map<String,String> psnMap=getFreevalueInfo("bd_cubasdoc",pk_corp,fieldName,condition,true);
			if(psnMap==null){
				throw new Exception("查找不到 "+vo.getDef6()+" 人员档案.");
			}
			FreevalueVO freevalue=new FreevalueVO();
			freevalue.setAssindex(0);
			freevalue.setCheckcount(0);
			freevalue.setChecktype(bdinfoMap.get("人员档案"));
			freevalue.setCheckvalue(psnMap.get("pk_psndoc"));
			freevalue.setDr(0);
			freevalue.setFreevalueid(freevalueid);
			freevalue.setValuecode(psnMap.get("psncode"));
			freevalue.setValuename(psnMap.get("psnname"));
			freevalueVOS.add(freevalue);
			isAssNeeded=true;
		}
		if(!isEmpty(vo.getDef7())){//项目辅助核算
			String[] fieldName={"pk_jobbasfil","jobcode","jobname"};
			String condition="jobcode='"+vo.getDef7()+"'";
			Map<String,String> jobMap=getFreevalueInfo("bd_jobbasfil",pk_corp,fieldName,condition,true);
			if(jobMap==null){
				throw new Exception("查找不到 "+vo.getDef7()+" 项目辅助核算.");
			}
			FreevalueVO freevalue=new FreevalueVO();
			freevalue.setAssindex(0);
			freevalue.setCheckcount(0);
			freevalue.setChecktype(bdinfoMap.get("项目辅助核算"));
			freevalue.setCheckvalue(jobMap.get("pk_jobbasfil"));
			freevalue.setDr(0);
			freevalue.setFreevalueid(freevalueid);
			freevalue.setValuecode(jobMap.get("jobcode"));
			freevalue.setValuename(jobMap.get("jobname"));
			freevalueVOS.add(freevalue);
			isAssNeeded=true;
		}
		if(!isEmpty(vo.getDef8())){//客商辅助核算
			String[] fieldName={"pk_cubasdoc","custcode","custname"};
			String condition="custcode='"+vo.getDef8()+"' and  (pk_corp in ('0001'  ,'"+pk_corp+"'))";
			Map<String,String> secureMap=getFreevalueInfo("bd_cubasdoc",pk_corp,fieldName,condition,false);
			if(secureMap==null){
				throw new Exception("查找不到 "+vo.getDef8()+" 客商辅助核算.");
			}
			FreevalueVO freevalue=new FreevalueVO();
			freevalue.setAssindex(0);
			freevalue.setCheckcount(0);
			freevalue.setChecktype(bdinfoMap.get("客商辅助核算"));
			freevalue.setCheckvalue(secureMap.get("pk_cubasdoc"));
			freevalue.setDr(0);
			freevalue.setFreevalueid(freevalueid);
			freevalue.setValuecode(secureMap.get("custcode"));
			freevalue.setValuename(secureMap.get("custname"));
			freevalueVOS.add(freevalue);
			isAssNeeded=true;
		}
		if(!isAssNeeded){
			return null;
		}
		getDao().insertVOArray(freevalueVOS.toArray(new FreevalueVO[0]));
		return freevalueid;
	}
	@SuppressWarnings("unchecked")
	private Map<String,String> getFreevalueInfo(String tableName,String pk_corp,String[] fieldName,String condition,boolean withCorp) throws Exception{
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
		sb.append("nvl(dr,0)=0  and "+condition);
		Map<String,String> map=(Map<String, String>) getDao().executeQuery(sb.toString(), new MapProcessor());
		 
		return map;
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
	
	private  Date date=new Date();
	/**
	 * 生成凭证
	 * @param recordVOS  分录数据
	 * @param infoMap	: pk_glorg 主体;pk_glbook 账簿;pk_glorgbook 账簿主体;pk_vouchertype 凭证类别 ; pk_corp 公司主键
	 * @param voucherNO 凭证号
	 * @param totalFunds 资金合计 : creditTotal 贷方总和; debtorTotal 借方总和
	 * @return
	 */
	private VoucherVO makeVoucher(List<VoucherRecordVO> recordVOS,Map<String,String> infoMap,Integer voucherNO,Map<String,UFDouble> totalFunds)throws Exception{
		VoucherVO voucherVO=new VoucherVO();
		VoucherRecordVO recordVO=recordVOS.get(0);
		voucherVO.setAttachment(recordVO.getFjzs());//附件张数
		voucherVO.setDetailmodflag(new UFBoolean(true));// 分录增删标志
		voucherVO.setDiscardflag(new UFBoolean(false));// 作废标志
		voucherVO.setExplanation(recordVO.getAbs_summary());//摘要    暂定第一条分录数据的摘要为凭证摘要（）
		voucherVO.setFree1(new SimpleDateFormat("MM").format(date));//调整期间
		voucherVO.setModifyflag("YYY");//修改标志
		voucherVO.setDr(0);
		voucherVO.setNo(voucherNO);//凭证号
		voucherVO.setPeriod(new SimpleDateFormat("MM").format(date));//会计期间
		voucherVO.setPk_corp(infoMap.get("pk_corp"));
		voucherVO.setPk_manager("N/A");//记账人为空
		//获取制单人编码
		String[] field={"cuserid"};
		Map<String, String> usermap= this.getFreevalueInfo("sm_user", infoMap.get("pk_corp"),field , " user_code='"+recordVO.getVoperatorid()+"'", false);//制单人编码在全集团是唯一的
		if(usermap==null){
			throw new Exception("未查找到制单人: "+recordVO.getVoperatorid());
		}
		voucherVO.setPk_prepared(usermap.get("cuserid"));//制单人
		voucherVO.setPk_system(recordVO.getPk_system());//来源系统 在测试的时候因为制单节点没有资金系统。推荐使用GL（总账系统做测试）
//		voucherVO.setPk_vouchertype(recordVO.getPk_vouchertypev());//凭证类别 该值为固定值，可以参考bd_vouchertype（凭证类别表） 凭证类型也可以在参数infoMap中获得
		voucherVO.setPk_vouchertype(infoMap.get("pk_vouchertype"));
		voucherVO.setPrepareddate(new UFDate(recordVO.getMakedate()));//制单日期
		voucherVO.setSignflag(new UFBoolean(false));//签字标志
		voucherVO.setTotalcredit(totalFunds.get("creditTotal"));//贷方总和
		voucherVO.setTotaldebit(totalFunds.get("debtorTotal"));//借方总和
		voucherVO.setVoucherkind(0);//凭证类型
		voucherVO.setYear(new SimpleDateFormat("yyyy").format(date));//会计年度
		
		voucherVO.setPk_glbook(infoMap.get("pk_glbook"));//账簿
		voucherVO.setPk_glorg(infoMap.get("pk_glorg"));//主体
		voucherVO.setPk_glorgbook(infoMap.get("pk_glorgbook"));//账簿主体
		voucherVO.setIsdifflag(new UFBoolean(false));//
		/*
		
		
		
		voucherVO.setTallydate(new UFDate(date));//记账日期
		
		voucherVO.setCheckeddate(new UFDate(recordVO.getVapprovedate()));//审核日期
		
		
		
		
		voucherVO.setIsdifflag(new UFBoolean(false));
		voucherVO.setYear(new SimpleDateFormat("yyyy").format(date));//会计年度
		voucherVO.setVoucherkind(0);
		
		
*/		
		voucherVO.setPk_voucher(getVoucherID());
		return voucherVO;
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
