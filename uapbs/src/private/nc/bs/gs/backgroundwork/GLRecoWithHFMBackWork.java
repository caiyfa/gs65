package nc.bs.gs.backgroundwork;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.pub.pa.PreAlertObject;
import nc.bs.pub.taskcenter.BgWorkingContext;
import nc.bs.pub.taskcenter.IBackgroundWorkPlugin;
import nc.gs.vo.CheckBalanceVO;
import nc.gs.vo.DetailVO;
import nc.gs.vo.FreevalueVO;
import nc.gs.vo.VoucherRecordVO;
import nc.gs.vo.VoucherVO;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.jdbc.framework.processor.MapProcessor;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDouble;

/**
 * ��ʢ-���ö��˽ӿ�
 * @author CYF 2019-03-04
 *
 */
public class GLRecoWithHFMBackWork implements IBackgroundWorkPlugin {
	private static BaseDAO dao = null;

	private BaseDAO getDao() {
		if (dao == null) {
			dao = new BaseDAO();
		}
		return dao;
	}
	private static final String prepareddatev="prepareddatev";
	
	@SuppressWarnings("unchecked")
	public PreAlertObject executeTask(BgWorkingContext bgwc) throws BusinessException {
		// TODO  ��������Ŀ�Ŀ��ʱ���������ƾ֤���������˱�
		
		PreAlertObject obj=new PreAlertObject();
		String timeCondition=this.getTimeCondition(bgwc);
		String subjectparam = bgwc.getKeyMap().get("subject") == null ? "" : bgwc
				.getKeyMap().get("subject").toString().trim();
		if(subjectparam==null||subjectparam.trim().length()==0){
			throw new BusinessException("�������Ŀ����");
		}
		// �ҵ��������ɵ�ƾ֤��¼
		String condition =timeCondition+" and nvl(dr,0)=0 and  pk_accsubj in (select pk_accsubj from bd_accsubj where subjcode like '"+subjectparam+"%')";//and nvl(contrastflag,0)=0 
		Collection<DetailVO> records = getDao().retrieveByClause(
				DetailVO.class, condition);
		/*if(records==null||records.size()==0){
			return "δ��ȡ��ƾ֤��¼��Ϣ";
		}*/
		if(records==null||records.size()==0){
			throw new BusinessException("δ��ȡ��ƾ֤��¼��Ϣ");
		}
		//��õ����й�˾
		List<String> pk_corps=new ArrayList<String>();
		for (DetailVO vo : records) {
			if(!pk_corps.contains(vo.getPk_corp())){
				pk_corps.add(vo.getPk_corp());
			}
		}
		List<String> subjectInfoList=getCorpBookInfo( pk_corps,subjectparam);
		//����ƾ֤��¼���Ƿ���100210�����¼� �Ŀ�Ŀ���ж�
		int countPut =0;
		for (DetailVO vo : records) {
			try {
				if (subjectInfoList.contains(vo.getPk_accsubj())) {
					// ������¼��Ҫ���͵����˱�
					if ((vo.getContrastflag() == null) || !(vo.getContrastflag() == 1)) {
						// �ٶ����˱�־Ϊ1���Ѷ��ˣ���δ���˵�����½������Ͳ��� ֮����Ҫ��һ��ȷ�����ֶε����
						if (vo.getAssid() != null && vo.getAssid().trim().length() != 0){
							doAction(vo);
							countPut ++;
						}

							

					} else {/*
					//�����ǽ����յĶ���ʧ�������ͷſɱ༭״̬�����������Ż���
						String[] modifieridfield = { CheckBalanceVO.CONFIRM_FLAG };
						Map<String, String> info = this.getInfo(
								"HFM_GL_CHECK_BALANCE",
								null,
								modifieridfield,
								CheckBalanceVO.VOUCHER_ID + "='"
										+ vo.getPk_detail() + "' and confirm_flag<>'SEAL'", false);
						if (info != null
								&& info.get(CheckBalanceVO.CONFIRM_FLAG)
										.equals("E")) {
							vo.setContrastflag(0);// ����ʧ�ܵ������ͷ�״̬
							getDao().updateVO(vo);
							String updateVoucher="update gl_voucher set contrastflag=0 where pk_voucher='"+vo.getPk_voucher()+"'";
							getDao().executeUpdate(updateVoucher);
							String sealSQL="update HFM_GL_CHECK_BALANCE set confirm_flag='SEAL' where voucher_id='"+vo.getPk_detail()+"'";
							getDao().executeUpdate(sealSQL);
						}
					*/}

				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	 
		// �ҵ���Ӧ�ĸ������㡣�й��������˻��������Ϣ
		String tmpCondition="nvl(dr,0)=0 and confirm_flag='E' and voucher_id is not null";
		Collection<CheckBalanceVO> cbVOs=getDao().retrieveByClause(CheckBalanceVO.class, tmpCondition);
		if(cbVOs==null||cbVOs.size()==0){
			obj.setBusiObj("������"+countPut+"�����˷�¼");
			return obj;
		}
		int countRelease=0;
		for (CheckBalanceVO vo:cbVOs){
			String updateDetailSql="update GL_DETAIL set contrastflag=0 where pk_detail='"+vo.getVoucher_id()+"'";
			getDao().executeUpdate(updateDetailSql);
			vo.setConfirm_flag("SEAL");
			getDao().updateVO(vo);
		/*	String updateVoucherSQL="update gl_voucher set contrastflag=0 where " +
					"(select case when exists(select pk_detail from gl_detail where pk_voucher=(select pk_voucher from gl_detail where pk_detail='"+vo.getVoucher_id()+"')  and contrastflag=1 ) then 1 else 0 end flag from dual)" +
					"=0";//ƾ֤��¼��Ӧ��ƾ֤�µ�����ƾ֤��¼���������ڶ����е������ͷ�����ƾ֤�ı༭״̬
			getDao().executeUpdate(updateVoucherSQL);*/
			countRelease++;
		}
		//�ҵ�����ʧ�ܵ��м�����ݡ�
		
		obj.setBusiObj("������"+countPut+"�����˷�¼, �ͷ���"+countRelease+"��ƾ֤��¼�༭״̬");
		return obj;
	}
	/**
	 * ���͵����˱�
	 * @param detail
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	private void doAction(DetailVO detail) throws Exception {
		// �ҵ���Ӧ�ĸ�������
		Collection<FreevalueVO> freeVOs = getDao()
				.retrieveByClause(
						FreevalueVO.class,
						" freevalueid='"
								+ detail.getAssid()
								+ "' and nvl(dr,0)=0 and   checktype='00010000000000000096'");
		if (freeVOs == null || freeVOs.size() == 0) {
			return;// û�������˻��ø�������
		}
		FreevalueVO freeVO = freeVOs.toArray(new FreevalueVO[0])[0];// ��freeVOsת�������鲢ȡ��һ��Ԫ��

		// ���ɶ�����Ϣ
		CheckBalanceVO balanceVO = new CheckBalanceVO();
		balanceVO.setVoucher_no(detail.getNov());// ƾ֤��
		balanceVO.setVoucher_id(detail.getPk_detail());// ƾ֤��¼����
//		balanceVO.setVoucher_date(new SimpleDateFormat("yyyy-MM-dd")
//				.format(detail.getTs().getDate().toDate()));// ��TS������ƾ֤����
		balanceVO.setVoucher_date(new SimpleDateFormat("yyyy-MM-dd").format(detail.getPrepareddatev().toDate()));
		balanceVO.setSubject(subjectMap.get(detail.getPk_accsubj()));// ��Ŀ����
		balanceVO.setOrgcode(corpCodeMap.get(detail.getPk_corp()));// ��˾����
		balanceVO.setOrgname(corpNameMap.get(detail.getPk_corp()));// ��˾����
		balanceVO.setBank_account(freeVO.getValuecode());// �����˺�
		balanceVO.setBank_account_name(freeVO.getValuename());// �˻�����
		String[] voucherfield = { VoucherVO.PK_PREPARED, VoucherVO.PK_CHECKED,VoucherVO.PK_SYSTEM};
		Map<String, String> voucherInfo = getInfo("gl_voucher", null,
				voucherfield,
				VoucherVO.PK_VOUCHER + "='" + detail.getPk_voucher() + "'",
				false);

		String[] modifieridfield = { "user_code" };
		Map<String, String> modifierid = getInfo("sm_user", null,
				modifieridfield,
				" cuserid='" + voucherInfo.get(VoucherVO.PK_PREPARED) + "'",
				false);
		balanceVO.setModifierid(modifierid.get("user_code"));//�Ƶ���
		if(voucherInfo.get(VoucherVO.PK_CHECKED)!=null&&voucherInfo.get(VoucherVO.PK_CHECKED).trim().length()!=0)
		balanceVO.setChecker(getInfo("sm_user", null,
				modifieridfield,
				" cuserid='" + voucherInfo.get(VoucherVO.PK_CHECKED) + "'",
				false).get("user_code"));//�����ˣ������ˣ���
		balanceVO.setSource(voucherInfo.get(VoucherVO.PK_SYSTEM));
		if("00010000000000000001".equals(detail.getPk_currtype())){
			balanceVO.setCurrency("CNY");
		}
		// ���ý������
		if((UFDouble.ZERO_DBL.equals(detail.getLocalcreditamount()))&&(!UFDouble.ZERO_DBL.equals(detail.getLocaldebitamount()))){
			//���Ҵ���Ϊ��跽��Ϊ����Ϊ��
			balanceVO.setAmount(detail.getLocaldebitamount());
//			balanceVO.setDirction("2");
			balanceVO.setDirction("1");
		}
		if((!UFDouble.ZERO_DBL.equals(detail.getLocalcreditamount()))&&(UFDouble.ZERO_DBL.equals(detail.getLocaldebitamount()))){
			//���Ҵ���Ϊ��跽��Ϊ����Ϊ��
			balanceVO.setAmount(detail.getLocalcreditamount());
//			balanceVO.setDirction("1");
			balanceVO.setDirction("2");
		}
		balanceVO.setMemo(detail.getExplanation());//ժҪ
		
		balanceVO.setFetch_flag("N");
		balanceVO.setConfirm_flag("N");
		getDao().insertVO(balanceVO);
		detail.setContrastflag(1);
		getDao().updateVO(detail);
//		String updateVoucher="update gl_voucher set contrastflag=1 where pk_voucher='"+detail.getPk_voucher()+"'";
//		getDao().executeUpdate(updateVoucher);
		
	}
	@SuppressWarnings("unchecked")
	private Map<String,String> getInfo(String tableName,String pk_corp,String[] fieldName,String condition,boolean withCorp) throws BusinessException{
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
	
	private Map<String,String> subjectMap=new HashMap<String, String>();
	private Map<String,String> corpCodeMap=new HashMap<String, String>();
	private Map<String,String> corpNameMap=new HashMap<String, String>();
	@SuppressWarnings({  "unchecked", "rawtypes" })
	private List<String> getCorpBookInfo(List<String> pk_corps ,String subjectparam) throws BusinessException{
		int size=pk_corps.size();
		 List<String> resList=new ArrayList<String>();
		for(int i=0;i<size;i++){
			String sql="select ba.pk_accsubj ,ba.subjcode ,bco.unitcode , bco.unitname from bd_accsubj ba ,bd_corp bco " +
					"where bco.pk_corp='"+pk_corps.get(i)+"' and ba.pk_glorgbook in" +
					" (select   bgg.pk_glorgbook  from bd_glorg bg,bd_glorgbook bgg,bd_vouchertype bv,bd_corp bc where bg.pk_glorg=bgg.pk_glorg and bgg.pk_glorgbook=bv.pk_glorgbook and bg.glorgcode=bc.unitcode and  " +
					"bc.pk_corp='"+pk_corps.get(i)+"' and nvl(bc.dr,0)=0 and nvl(bgg.dr,0)=0 and nvl(bv.dr,0)=0)  and ba.subjcode like '"+subjectparam+"%' and nvl(ba.dr,0)=0"/*and ba.pk_corp='"+pk_corps.get(i)+"'*/;
			List tmp=(List) getDao().executeQuery(sql, new MapListProcessor());
			/*if(tmp!=null&&tmp.size()!=0){
				map.put(pk_corps.get(i), tmp.get("pk_accsubj"));
			}*/
			int tmpsize=tmp.size();
			for(int j=0;j<tmpsize;j++){
				Map<String,String> tmpmap=(Map<String, String>) tmp.get(j);
				if(!resList.contains(tmpmap.get("pk_accsubj")))
				resList.add(tmpmap.get("pk_accsubj"));
				subjectMap.put(tmpmap.get("pk_accsubj"), tmpmap.get("subjcode"));
				corpCodeMap.put(pk_corps.get(i), tmpmap.get("unitcode"));
				corpNameMap.put(pk_corps.get(i), tmpmap.get("unitname"));
			}
			
		}
		return resList;
	}
	private String getTimeCondition(BgWorkingContext bgwc)throws BusinessException {
		Date date=new Date();
		String timeCondition ="";
		try {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String from = bgwc.getKeyMap().get("from") == null ? "" : bgwc
				.getKeyMap().get("from").toString().trim();
		String to = bgwc.getKeyMap().get("to") == null ? "" : bgwc
				.getKeyMap().get("to").toString().trim();
		
		if((from==null||from.length()==0)&&(to==null||to.length()==0)){
			//û������from ��to ����
			timeCondition=GLRecoWithHFMBackWork.prepareddatev + " = '" + sdf.format(date) + "'";
			return timeCondition;
		}
		if(from!=null&&from.length()!=0&&isDateParamLegal(from)&&(to==null||to.length()==0)){
			
				if(sdf.parse(from).getTime()>date.getTime()){
					throw new BusinessException("�������õ�from����:"+from+" �������ڷ�����ʱ�� "+sdf.format(date));
				}
				timeCondition=GLRecoWithHFMBackWork.prepareddatev + " <= '" + sdf.format(date) + "' and "+GLRecoWithHFMBackWork.prepareddatev + " >= '" + from + "' ";
				return timeCondition;
			
		}
		if((from==null||from.length()==0)&&(to!=null||to.length()!=0)){
			throw new BusinessException("���ܵ�������to����:"+to);
		}
		if((from!=null&&from.length()!=0&&isDateParamLegal(from))&&(to!=null&&to.length()!=0&&isDateParamLegal(to))){
			if(sdf.parse(from).getTime()>sdf.parse(to).getTime()){
				throw new BusinessException("��ʼʱ��:"+from +" �������ڽ���ʱ��:"+to);
			}
			timeCondition=GLRecoWithHFMBackWork.prepareddatev + " <= '" + to + "' and "+GLRecoWithHFMBackWork.prepareddatev + " >= '" + from + "' ";
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
			// TODO Auto-generated catch block
			throw new BusinessException(timeparam+"ʱ���ʽ�����밴�� yyyy-MM-dd ��дʱ��");
		}
		return true;
	}
	 
	/*@SuppressWarnings({  "unchecked", "rawtypes" })
	private Map<String,String> getCorpBookInfo(List<String> pk_corps) throws BusinessException{
		int size=pk_corps.size();
		Map<String,String> map=new HashMap<String, String>();
		for(int i=0;i<size;i++){
			String sql="select ba.pk_accsubj ,ba.subjcode from bd_accsubj ba " +
					"where ba.pk_glorgbook in" +
					" (select   bgg.pk_glorgbook  from bd_glorg bg,bd_glorgbook bgg,bd_vouchertype bv,bd_corp bc where bg.pk_glorg=bgg.pk_glorg and bgg.pk_glorgbook=bv.pk_glorgbook and bg.glorgcode=bc.unitcode and  " +
					"bc.pk_corp='"+pk_corps.get(i)+"' and nvl(bc.dr,0)=0 and nvl(bgg.dr,0)=0 and nvl(bv.dr,0)=0) and ba.pk_corp='"+pk_corps.get(i)+"' and ba.subjcode like '100210%' and nvl(ba.dr,0)=0";
			List tmp=(List) getDao().executeQuery(sql, new MapListProcessor());
			if(tmp!=null&&tmp.size()!=0){
				map.put(pk_corps.get(i), tmp.get("pk_accsubj"));
			}
			int tmpsize=tmp.size();
			for(int j=0;j<tmpsize;j++){
				Map<String,String> tmpmap=(Map<String, String>) tmp.get(j);
				map.put(pk_corps.get(i)+"@"+tmpmap.get("subjcode"), tmpmap.get("pk_accsubj"));
			}
			
		}
		return map;
	}*/

}
