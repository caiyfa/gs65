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
 * 	��ʢ�ɺ���������������ƾ֤�ӿ�
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
			vo.setAbs_summary("���Լ�¼2");
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
			//û������from ��to ����
			timeCondition=VoucherRecordVO.MAKEDATE + " = '" + sdf.format(date) + "'";
			return timeCondition;
		}
		if(from!=null&&from.length()!=0&&isDateParamLegal(from)&&(to==null||to.length()==0)){
			
				if(sdf.parse(from).getTime()>date.getTime()){
					throw new BusinessException("�������õ�from����:"+from+" �������ڷ�����ʱ�� "+sdf.format(date));
				}
				timeCondition=VoucherRecordVO.MAKEDATE + " <= '" + sdf.format(date) + "' and "+VoucherRecordVO.MAKEDATE + " >= '" + from + "' ";
				return timeCondition;
			
		}
		if((from==null||from.length()==0)&&(to!=null||to.length()!=0)){
			throw new BusinessException("���ܵ�������to����:"+to);
		}
		if((from!=null&&from.length()!=0&&isDateParamLegal(from))&&(to!=null&&to.length()!=0&&isDateParamLegal(to))){
			if(sdf.parse(from).getTime()>sdf.parse(to).getTime()){
				throw new BusinessException("��ʼʱ��:"+from +" �������ڽ���ʱ��:"+to);
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
	 * У��ʱ������Ϸ���
	 * @param timeparam
	 * @return
	 * @throws BusinessException
	 */
	private boolean  isDateParamLegal(String timeparam)throws BusinessException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			sdf.parse(timeparam);
		} catch (ParseException e) {
			throw new BusinessException(timeparam+"ʱ���ʽ�����밴�� yyyy-MM-dd ��дʱ��");
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public PreAlertObject executeTask(BgWorkingContext bgwc) throws BusinessException {
		
		PreAlertObject obj=new PreAlertObject();
			this.date=new Date();
//		parepareData();
		//�޸�ȡ���߼�
			String timeCondition=this.getTimeCondition(bgwc);
		
//		 String date = bgwc.getKeyMap().get("date") == null ? "" : bgwc
//				.getKeyMap().get("date").toString().trim();
//		// ��û�в���Ĭ��Ϊ��ǰϵͳʱ��
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
//				throw new BusinessException("ʱ���ʽ�����밴�� yyyy-MM-dd ��дʱ��");
//			}
//		}
		//(nvl(scbz,-1)=-1 )or  ����Ҫ������ƾ֤ʧ�ܵ�����Ҳ��Ҫ��������ƾ֤������ nvl(gs_voucher_record.pk_vocher,'1')<>'1' and 
		Collection<VoucherRecordVO> list = getDao().retrieveByClause( 
				VoucherRecordVO.class,
				timeCondition+" and nvl(dr,0)=0 and ((  (select case when exists(select * from gl_voucher gv where gs_voucher_record.pk_vocher=gv.pk_voucher)then 1 else 0 end  from dual )=0)) order by hfm_group_batch_id ");

		if (list == null || list.size() == 0) {
		 
			 throw new BusinessException("GS_VOUCHER_RECORD����δ��ѯ������");
		}
		VoucherRecordVO[] voucherRecVOS = list.toArray(new VoucherRecordVO[0]);
		
		Map<String,List<VoucherRecordVO>> recordListMap=new HashMap<String, List<VoucherRecordVO>>();//ÿһ�������ž���һ��ƾ֤ ���������Ž��й���
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
		getbd_bdinfo();//���ظ�����������
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
		
	 

		
	obj.setReturnObj( "������"+count+"��ƾ֤");
		return obj;
		}
	/**
	 * ����Է���Ŀ����
	 */
	private Map<String,String> oppoSubj=new HashMap<String, String>();
	private void  loadOppositSubj(List<VoucherRecordVO> recordVOS,Map<String,String> infoMap){
		for(VoucherRecordVO vo:recordVOS){
			if((UFDouble.ZERO_DBL.equals(vo.getLocal_credit_currency()))&&(!UFDouble.ZERO_DBL.equals(vo.getLocal_debtor_currency()))){//���Ҵ���Ϊ��跽��Ϊ����Ϊ��
//				detailVO.setDirection("D");
				 if(oppoSubj.containsKey("D")){
					 oppoSubj.put("D", oppoSubj.get("D")+","+infoMap.get(vo.getSubjcode()));
				 }else{
					 oppoSubj.put("D",infoMap.get(vo.getSubjcode()));
				 }
			}
			if((!UFDouble.ZERO_DBL.equals(vo.getLocal_credit_currency()))&&(UFDouble.ZERO_DBL.equals(vo.getLocal_debtor_currency()))){//���ҽ跽Ϊ�������Ϊ����Ϊ��
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
	 * @param recordVOS �������б�����Ƕ��ƾ֤��¼�������ϳ�һ��ƾ֤
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void doAction(List<VoucherRecordVO> recordVOS) throws Exception{
		if(recordVOS==null||recordVOS.size()==0){
			return ;
		}
		String corpcode=recordVOS.get(0).getPk_corp();//�ʽ���������ǹ�˾���벻������
		if(corpcode==null||corpcode.trim().length()==0){
			throw new Exception("ȱʧ��˾����");
		}
		Map<String,String> infoMap=getGlBookInfos(corpcode);//pk_glorg ����;pk_glbook �˲�;pk_glorgbook �˲�����;pk_vouchertype ƾ֤���; pk_corp ��˾����\
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
				throw new Exception(infoMap.get("pk_corp")+"�ù�˾��δ���ҵ���Ŀ"+vo.getSubjcode());
			}
			subMap.put(vo.getSubjcode(), map.get("pk_accsubj"));
		}
		loadOppositSubj(recordVOS,subMap);//���ضԷ���Ŀ
		infoMap.put("corpcode", corpcode);//��˾����
		infoMap.put("pk_vouchertype", recordVOS.get(0).getPk_vouchertypev());//�����ʽ����͵����ݻ�ȡƾ֤��𣬸���֮ǰ�����
		Integer voucherNO=prepareVoucherNO(infoMap,corpcode);//���ƾ֤��
		Map<String,UFDouble> totalFunds=checkFunds(recordVOS);//У���ʽ�����
		VoucherVO voucherVO=makeVoucher(recordVOS, infoMap, voucherNO, totalFunds);//����ƾ֤
		
		
		for(VoucherRecordVO vo:recordVOS){
			vo.setPk_vocher(voucherVO.getPk_voucher());
			String pk_detail=makeDetail(vo, infoMap.get("pk_corp"), infoMap,subMap, voucherNO);
			vo.setPk_detail(pk_detail);
			vo.setScbz(0);//���ɳɹ�
			vo.setDef11("");
			getDao().updateVO(vo);
			
		}
		getDao().insertVOWithPK(voucherVO);//��ƾ֤��¼����֮������ƾ֤��Ϣ����֤���ƾ֤��¼����ʧ�ܲ������յ�ƾ֤
		oppoSubj.clear();//�ͷŶԷ���Ŀ
		
	}
	
	private String makeDetail(VoucherRecordVO vo,String pk_corp,Map<String,String> infoMap,Map<String, String> subMap,Integer voucherNO) throws Exception{
		DetailVO detailVO=new DetailVO();
		detailVO.setNov(voucherNO);
		detailVO.setAssid("");//���������־
		detailVO.setCreditamount(vo.getOriginal_credit_currency());// ԭ�Ҵ���
		detailVO.setCreditquantity(UFDouble.ZERO_DBL);//��������
		detailVO.setDebitamount(vo.getOriginal_debtor_currency());// ԭ�ҽ跽
		detailVO.setDebitquantity(UFDouble.ZERO_DBL);//�跽����
		detailVO.setDetailindex(vo.getDetailindex());// ��¼��
		detailVO.setExcrate1(UFDouble.ZERO_DBL);//����1
		detailVO.setExcrate2(UFDouble.ONE_DBL);//����2
		detailVO.setExplanation(vo.getAbs_summary());// ժҪ
		detailVO.setFraccreditamount(UFDouble.ZERO_DBL);//������������
		detailVO.setFracdebitamount(UFDouble.ZERO_DBL);//�����跢����
		detailVO.setLocalcreditamount(vo.getLocal_credit_currency());// ���Ҵ���
		detailVO.setLocaldebitamount(vo.getLocal_debtor_currency());// ���ҽ跽
		detailVO.setModifyflag("YYYYYYYYYYYYYYYY");//�޸ı�־
		detailVO.setPk_accsubj(subMap.get(vo.getSubjcode()));
		detailVO.setPk_corp(pk_corp);// ��˾����
		detailVO.setDr(0);
		if("CNY".equals(vo.getPk_currtype()))
		detailVO.setPk_currtype("00010000000000000001");// ����
		
		detailVO.setPk_voucher(vo.getPk_vocher());// ƾ֤����
		detailVO.setPrice(UFDouble.ZERO_DBL);//����
		detailVO.setPk_glbook(infoMap.get("pk_glbook"));// �˲�
		detailVO.setPk_glorg(infoMap.get("pk_glorg"));// ����
		detailVO.setPk_glorgbook(infoMap.get("pk_glorgbook"));// �˲�����
		//���ý������
		if((UFDouble.ZERO_DBL.equals(vo.getLocal_credit_currency()))&&(!UFDouble.ZERO_DBL.equals(vo.getLocal_debtor_currency()))){//���Ҵ���Ϊ��跽��Ϊ����Ϊ��
			detailVO.setDirection("D");
			detailVO.setOppositesubj(oppoSubj.get("C"));
		}
		if((!UFDouble.ZERO_DBL.equals(vo.getLocal_credit_currency()))&&(UFDouble.ZERO_DBL.equals(vo.getLocal_debtor_currency()))){//���ҽ跽Ϊ�������Ϊ����Ϊ��
			detailVO.setDirection("C");
			detailVO.setOppositesubj(oppoSubj.get("D"));
		}
		detailVO.setDiscardflagv(new UFBoolean(false));// ���ϱ�־
		detailVO.setPeriodv(new SimpleDateFormat("MM").format(date));// ����ڼ�
		detailVO.setFree6(new SimpleDateFormat("MM").format(date));
		detailVO.setPk_managerv("N/A");//����������ƾ֤��û�м����˵�
		detailVO.setPk_systemv(vo.getPk_system());// ��Դϵͳ
		detailVO.setPk_vouchertypev(infoMap.get("pk_vouchertype"));//ƾ֤����
		detailVO.setPrepareddatev(new UFDate(vo.getMakedate()));// �Ƶ�����
		detailVO.setVoucherkindv(0);//ƾ֤����
		detailVO.setYearv(new SimpleDateFormat("yyyy").format(date));// ������
		detailVO.setIsdifflag(new UFBoolean(false));// �Ƿ����ƾ֤
		
		//���ɸ�������
		String assid=makeAss(vo,pk_corp,infoMap,subMap);
		detailVO.setAssid(assid);// ��������
		if(vo.getVapprovedate()!=null)
		detailVO.setCheckdate(new UFDate(vo.getVapprovedate()));// �������
 		return getDao().insertVO(detailVO);
	}
	private String makeAss(VoucherRecordVO vo,String pk_corp,Map<String,String> infoMap,Map<String, String> subMap)throws Exception{
		String freevalueid=getFreeValueID();
		if(freevalueid==null){
			freevalueid=getFreeValueID();
		}
		boolean isAssNeeded=false;
		List<FreevalueVO> freevalueVOS=new ArrayList<FreevalueVO>();
		if(!isEmpty(vo.getDef1())){//���ŵ���
			String[] fieldName={"pk_deptdoc","deptcode","deptname"};
			String condition="deptcode='"+vo.getDef1()+"'";
			Map<String,String> deptMap=getFreevalueInfo("bd_deptdoc",pk_corp,fieldName,condition,true);
			if(deptMap==null){
				throw new Exception("���Ҳ��� "+vo.getDef1()+" ����.");
			}
			FreevalueVO freevalue=new FreevalueVO();
			freevalue.setAssindex(0);
			freevalue.setCheckcount(0);
			freevalue.setChecktype(bdinfoMap.get("���ŵ���"));
			freevalue.setCheckvalue(deptMap.get("pk_deptdoc"));
			freevalue.setDr(0);
			freevalue.setFreevalueid(freevalueid);
			freevalue.setValuecode(deptMap.get("deptcode"));
			freevalue.setValuename(deptMap.get("deptname"));
			freevalueVOS.add(freevalue);
			isAssNeeded=true;
		}
		if(!isEmpty(vo.getDef2())){//�����˻�
			String[] fieldName={"pk_bankaccbas","accountcode","accountname"};
			String condition="accountcode='"+vo.getDef2()+"'";
			Map<String,String> accMap=getFreevalueInfo("bd_bankaccbas",pk_corp,fieldName,condition,false);
			if(accMap==null){
				throw new Exception("���Ҳ��� "+vo.getDef2()+" �˻�.");
			}
			FreevalueVO freevalue=new FreevalueVO();
			freevalue.setAssindex(0);
			freevalue.setCheckcount(0);
			freevalue.setChecktype(bdinfoMap.get("�����˻�"));
			freevalue.setCheckvalue(accMap.get("pk_bankaccbas"));
			freevalue.setDr(0);
			freevalue.setFreevalueid(freevalueid);
			freevalue.setValuecode(accMap.get("accountcode"));
			freevalue.setValuename(accMap.get("accountname"));
			freevalueVOS.add(freevalue);
			isAssNeeded=true;
		}
		if(!isEmpty(vo.getDef3())){//֤ȯ����
			String[] fieldName={"pk_securities","code","name"};
			String condition="code='"+vo.getDef3()+"'";
			Map<String,String> secureMap=getFreevalueInfo("bd_bankaccbas",pk_corp,fieldName,condition,true);
			if(secureMap==null){
				throw new Exception("���Ҳ��� "+vo.getDef3()+" ֤ȯ����.");
			}
			FreevalueVO freevalue=new FreevalueVO();
			freevalue.setAssindex(0);
			freevalue.setCheckcount(0);
			freevalue.setChecktype(bdinfoMap.get("֤ȯ����"));
			freevalue.setCheckvalue(secureMap.get("pk_securities"));
			freevalue.setDr(0);
			freevalue.setFreevalueid(freevalueid);
			freevalue.setValuecode(secureMap.get("code"));
			freevalue.setValuename(secureMap.get("name"));
			freevalueVOS.add(freevalue);
			isAssNeeded=true;
		}
		if(!isEmpty(vo.getDef4())){//�ͻ���������
			String[] fieldName={"pk_cubasdoc","custcode","custname"};
			String condition="custcode='"+vo.getDef4()+"'";
			Map<String,String> secureMap=getFreevalueInfo("bd_cubasdoc",pk_corp,fieldName,condition,true);
			if(secureMap==null){
				throw new Exception("���Ҳ��� "+vo.getDef4()+" �ͻ���������.");
			}
			FreevalueVO freevalue=new FreevalueVO();
			freevalue.setAssindex(0);
			freevalue.setCheckcount(0);
			freevalue.setChecktype(bdinfoMap.get("�ͻ���������"));
			freevalue.setCheckvalue(secureMap.get("pk_cubasdoc"));
			freevalue.setDr(0);
			freevalue.setFreevalueid(freevalueid);
			freevalue.setValuecode(secureMap.get("custcode"));
			freevalue.setValuename(secureMap.get("custname"));
			freevalueVOS.add(freevalue);
			isAssNeeded=true;
		}
		if(!isEmpty(vo.getDef5())){//֤ȯ������
			String[] fieldName={"pk_bourse","code","name"};
			String condition="code='"+vo.getDef5()+"'";
			Map<String,String> cubasMap=getFreevalueInfo("sec_bourse",pk_corp,fieldName,condition,false);
			if(cubasMap==null){
				throw new Exception("���Ҳ��� "+vo.getDef5()+" ֤ȯ������.");
			}
			FreevalueVO freevalue=new FreevalueVO();
			freevalue.setAssindex(0);
			freevalue.setCheckcount(0);
			freevalue.setChecktype(bdinfoMap.get("֤ȯ������"));
			freevalue.setCheckvalue(cubasMap.get("pk_bourse"));
			freevalue.setDr(0);
			freevalue.setFreevalueid(freevalueid);
			freevalue.setValuecode(cubasMap.get("code"));
			freevalue.setValuename(cubasMap.get("name"));
			freevalueVOS.add(freevalue);
			isAssNeeded=true;
		}
		if(!isEmpty(vo.getDef6())){//��Ա����
			String[] fieldName={"pk_psndoc","psncode","psnname"};
			String condition="psncode='"+vo.getDef6()+"'";
			Map<String,String> psnMap=getFreevalueInfo("bd_cubasdoc",pk_corp,fieldName,condition,true);
			if(psnMap==null){
				throw new Exception("���Ҳ��� "+vo.getDef6()+" ��Ա����.");
			}
			FreevalueVO freevalue=new FreevalueVO();
			freevalue.setAssindex(0);
			freevalue.setCheckcount(0);
			freevalue.setChecktype(bdinfoMap.get("��Ա����"));
			freevalue.setCheckvalue(psnMap.get("pk_psndoc"));
			freevalue.setDr(0);
			freevalue.setFreevalueid(freevalueid);
			freevalue.setValuecode(psnMap.get("psncode"));
			freevalue.setValuename(psnMap.get("psnname"));
			freevalueVOS.add(freevalue);
			isAssNeeded=true;
		}
		if(!isEmpty(vo.getDef7())){//��Ŀ��������
			String[] fieldName={"pk_jobbasfil","jobcode","jobname"};
			String condition="jobcode='"+vo.getDef7()+"'";
			Map<String,String> jobMap=getFreevalueInfo("bd_jobbasfil",pk_corp,fieldName,condition,true);
			if(jobMap==null){
				throw new Exception("���Ҳ��� "+vo.getDef7()+" ��Ŀ��������.");
			}
			FreevalueVO freevalue=new FreevalueVO();
			freevalue.setAssindex(0);
			freevalue.setCheckcount(0);
			freevalue.setChecktype(bdinfoMap.get("��Ŀ��������"));
			freevalue.setCheckvalue(jobMap.get("pk_jobbasfil"));
			freevalue.setDr(0);
			freevalue.setFreevalueid(freevalueid);
			freevalue.setValuecode(jobMap.get("jobcode"));
			freevalue.setValuename(jobMap.get("jobname"));
			freevalueVOS.add(freevalue);
			isAssNeeded=true;
		}
		if(!isEmpty(vo.getDef8())){//���̸�������
			String[] fieldName={"pk_cubasdoc","custcode","custname"};
			String condition="custcode='"+vo.getDef8()+"' and  (pk_corp in ('0001'  ,'"+pk_corp+"'))";
			Map<String,String> secureMap=getFreevalueInfo("bd_cubasdoc",pk_corp,fieldName,condition,false);
			if(secureMap==null){
				throw new Exception("���Ҳ��� "+vo.getDef8()+" ���̸�������.");
			}
			FreevalueVO freevalue=new FreevalueVO();
			freevalue.setAssindex(0);
			freevalue.setCheckcount(0);
			freevalue.setChecktype(bdinfoMap.get("���̸�������"));
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
	 * ��������������
	 */
	private Map<String,String> bdinfoMap=new HashMap<String, String>();
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void getbd_bdinfo() throws BusinessException   {
		String sql = " select bb.bdname,bb.pk_bdinfo from bd_bdinfo bb where bb.bdname in ('�����˻�','���ŵ���','֤ȯ����','�ͻ���������','֤ȯ������','��Ա����','��Ŀ��������','���̸�������')";
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
	 * ����ƾ֤
	 * @param recordVOS  ��¼����
	 * @param infoMap	: pk_glorg ����;pk_glbook �˲�;pk_glorgbook �˲�����;pk_vouchertype ƾ֤��� ; pk_corp ��˾����
	 * @param voucherNO ƾ֤��
	 * @param totalFunds �ʽ�ϼ� : creditTotal �����ܺ�; debtorTotal �跽�ܺ�
	 * @return
	 */
	private VoucherVO makeVoucher(List<VoucherRecordVO> recordVOS,Map<String,String> infoMap,Integer voucherNO,Map<String,UFDouble> totalFunds)throws Exception{
		VoucherVO voucherVO=new VoucherVO();
		VoucherRecordVO recordVO=recordVOS.get(0);
		voucherVO.setAttachment(recordVO.getFjzs());//��������
		voucherVO.setDetailmodflag(new UFBoolean(true));// ��¼��ɾ��־
		voucherVO.setDiscardflag(new UFBoolean(false));// ���ϱ�־
		voucherVO.setExplanation(recordVO.getAbs_summary());//ժҪ    �ݶ���һ����¼���ݵ�ժҪΪƾ֤ժҪ����
		voucherVO.setFree1(new SimpleDateFormat("MM").format(date));//�����ڼ�
		voucherVO.setModifyflag("YYY");//�޸ı�־
		voucherVO.setDr(0);
		voucherVO.setNo(voucherNO);//ƾ֤��
		voucherVO.setPeriod(new SimpleDateFormat("MM").format(date));//����ڼ�
		voucherVO.setPk_corp(infoMap.get("pk_corp"));
		voucherVO.setPk_manager("N/A");//������Ϊ��
		//��ȡ�Ƶ��˱���
		String[] field={"cuserid"};
		Map<String, String> usermap= this.getFreevalueInfo("sm_user", infoMap.get("pk_corp"),field , " user_code='"+recordVO.getVoperatorid()+"'", false);//�Ƶ��˱�����ȫ������Ψһ��
		if(usermap==null){
			throw new Exception("δ���ҵ��Ƶ���: "+recordVO.getVoperatorid());
		}
		voucherVO.setPk_prepared(usermap.get("cuserid"));//�Ƶ���
		voucherVO.setPk_system(recordVO.getPk_system());//��Դϵͳ �ڲ��Ե�ʱ����Ϊ�Ƶ��ڵ�û���ʽ�ϵͳ���Ƽ�ʹ��GL������ϵͳ�����ԣ�
//		voucherVO.setPk_vouchertype(recordVO.getPk_vouchertypev());//ƾ֤��� ��ֵΪ�̶�ֵ�����Բο�bd_vouchertype��ƾ֤���� ƾ֤����Ҳ�����ڲ���infoMap�л��
		voucherVO.setPk_vouchertype(infoMap.get("pk_vouchertype"));
		voucherVO.setPrepareddate(new UFDate(recordVO.getMakedate()));//�Ƶ�����
		voucherVO.setSignflag(new UFBoolean(false));//ǩ�ֱ�־
		voucherVO.setTotalcredit(totalFunds.get("creditTotal"));//�����ܺ�
		voucherVO.setTotaldebit(totalFunds.get("debtorTotal"));//�跽�ܺ�
		voucherVO.setVoucherkind(0);//ƾ֤����
		voucherVO.setYear(new SimpleDateFormat("yyyy").format(date));//������
		
		voucherVO.setPk_glbook(infoMap.get("pk_glbook"));//�˲�
		voucherVO.setPk_glorg(infoMap.get("pk_glorg"));//����
		voucherVO.setPk_glorgbook(infoMap.get("pk_glorgbook"));//�˲�����
		voucherVO.setIsdifflag(new UFBoolean(false));//
		/*
		
		
		
		voucherVO.setTallydate(new UFDate(date));//��������
		
		voucherVO.setCheckeddate(new UFDate(recordVO.getVapprovedate()));//�������
		
		
		
		
		voucherVO.setIsdifflag(new UFBoolean(false));
		voucherVO.setYear(new SimpleDateFormat("yyyy").format(date));//������
		voucherVO.setVoucherkind(0);
		
		
*/		
		voucherVO.setPk_voucher(getVoucherID());
		return voucherVO;
	}

	/**
	 * У���ʽ����� 
	 * creditTotal �����ܺ�
	 * debtorTotal �跽�ܺ�
	 * @param recordVOS
	 */
	private Map<String,UFDouble> checkFunds(List<VoucherRecordVO> recordVOS)throws Exception{
		UFDouble local_credit_currency=new UFDouble(0.0000);//���Ҵ�
		UFDouble local_debtor_currency=new UFDouble(0.0000);//���ҽ�
		UFDouble original_credit_currency=new UFDouble(0.0000);//ԭ�Ҵ�
		UFDouble original_debtor_currency=new UFDouble(0.0000);//ԭ�ҽ�
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
			throw new Exception("���ҷ������");
		}
		if(!original_credit_currency.equals(local_debtor_currency)){
			throw new Exception("ԭ�ҷ������");
		}
		Map<String,UFDouble> res=new HashMap<String, UFDouble>();
		
		res.put("creditTotal", local_credit_currency);
		res.put("debtorTotal", local_debtor_currency);
		
		return res;
	}
	/**
	 * 	ͬһ����˾���˲����塢ƾ֤���͡�ƾ֤��Ψһ
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
	 * @return �ɹ�˾������ �˲������Ϣ�Լ�ƾ֤����
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
			throw new BusinessException("��˾��"+corpcode+"δ���ҵ���Ϣ");
		}
		if(map.get("pk_corp")==null||map.get("pk_corp").trim().length()==0){
			throw new BusinessException("��˾��"+corpcode+"δ���ҵ��ù�˾��Ϣ");
		}
		if(map.get("pk_glorg")==null||map.get("pk_glorg").trim().length()==0){
			throw new BusinessException("��˾��"+corpcode+"δ���ҵ�������Ϣ");
		}
		if(map.get("pk_glorgbook")==null||map.get("pk_glorgbook").trim().length()==0){
			throw new BusinessException("��˾��"+corpcode+"δ���ҵ������˲���Ϣ");
		}
		if(map.get("pk_glbook")==null||map.get("pk_glbook").trim().length()==0){
			throw new BusinessException("��˾��"+corpcode+"δ���ҵ��˲���Ϣ");
		}
		if(map.get("pk_vouchertype")==null||map.get("pk_vouchertype").trim().length()==0){
			throw new BusinessException("��˾��"+corpcode+"δ���ҵ�ƾ֤�����Ϣ");
		}
		return map;
	}
 
	/**
	 * У�������Ƿ����
	 * 
	 * @param vo
	 * @return
	 */
/*	private StringBuilder checkVO(VoucherRecordVO vo) {
		StringBuilder tmpsb = new StringBuilder();
		if (vo == null) {
			tmpsb.append("���ݴ���");
			return tmpsb;
		}
	 
		if (isEmpty(vo.getPk_currtype())) {
			tmpsb.append("����Ϊ��.");
		}
	 
		if (isEmpty(vo.getMakedate())) {
			tmpsb.append("�Ƶ�����Ϊ��.");
		}
		if (isEmpty(vo.getSubjcode())) {
			tmpsb.append("��Ŀ����Ϊ��.");
		}
		if (isEmpty(vo.getVoperatorid())) {
			tmpsb.append("�Ƶ���Ϊ��");
		}
		if (isEmpty(vo.getPk_system())) {
			tmpsb.append("��ԴϵͳΪ��.");
		}
		
		 * if((!isEmpty(vo.getLocal_credit_currency()))&&(vo.
		 * getLocal_credit_currency().equals(vo.getLocal_debtor_currency()))){
		 * tmpsb.append("���ҽ跽������һ��");
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
