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
		
	 

		
		obj.setReturnObj( "������"+count+"��ƾ֤");
		}catch(Exception e){
			throw new BusinessException(e);
		}
		return obj;
		
		}
	/**
	 * ����Է���Ŀ����
	 */
//	private Map<String,String> oppoSubj=new HashMap<String, String>();
//	private void  loadOppositSubj(List<VoucherRecordVO> recordVOS,Map<String,String> infoMap){
//		for(VoucherRecordVO vo:recordVOS){
//			if((UFDouble.ZERO_DBL.equals(vo.getLocal_credit_currency()))&&(!UFDouble.ZERO_DBL.equals(vo.getLocal_debtor_currency()))){//���Ҵ���Ϊ��跽��Ϊ����Ϊ��
////				detailVO.setDirection("D");
//				 if(oppoSubj.containsKey("D")){
//					 oppoSubj.put("D", oppoSubj.get("D")+","+infoMap.get(vo.getSubjcode()));
//				 }else{
//					 oppoSubj.put("D",infoMap.get(vo.getSubjcode()));
//				 }
//			}
//			if((!UFDouble.ZERO_DBL.equals(vo.getLocal_credit_currency()))&&(UFDouble.ZERO_DBL.equals(vo.getLocal_debtor_currency()))){//���ҽ跽Ϊ�������Ϊ����Ϊ��
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
	 * @param recordVOS �������б�����Ƕ��ƾ֤��¼�������ϳ�һ��ƾ֤
	 * @throws Exception
	 */
	private void doAction(List<VoucherRecordVO> recordVOS,String jsonStr) throws Exception{ 
		//У����
		checkFunds(recordVOS);
		//У�鹫˾�Ƿ�һ��
		checkPk_Corp(recordVOS);
		//���ɱ���
		String xml=VoucherXmlCreateAction.prepareVoucherXML(recordVOS,jsonStr);
		//���ķ���
		String res=MakeVoucherAction.doSendMessage(xml);
		//�������
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
        bw.write(xml);// �����е��ļ�������ַ���
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
	
	private void checkPk_Corp(List<VoucherRecordVO> recordVOS)throws Exception{
		List<String> tmp=new ArrayList<String>();
		for(VoucherRecordVO vo:recordVOS){ 
			if(vo.getPk_corp()==null||vo.getPk_corp().trim().length()==0){
				throw new Exception("��˾����Ϊ��");
			}
			if(!tmp.contains(vo.getPk_corp())&&tmp.size()>1){
				throw new Exception("ͬһƾ֤������һ�ҹ�˾���������ɶ�ҹ�˾����");
			}
			tmp.add(vo.getPk_corp());
		}
		
		
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
