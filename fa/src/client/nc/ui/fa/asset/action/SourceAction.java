package nc.ui.fa.asset.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.bd.config.mode.IBDMode;
import nc.itf.uap.IUAPQueryBS;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.generator.IdGenerator;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.pub.fa.card.AssetSourceBillConst;
import nc.pub.fa.card.AssetStateConst;
import nc.pub.fa.card.AssetUseConst;
import nc.pub.fa.card.CardTabConst;
import nc.pub.fa.common.consts.BillTypeConst;
import nc.pub.fa.common.consts.FunCodeConst;
import nc.pub.fa.common.util.VisibleScopeUtils;
import nc.pubitf.org.IDeptPubService;
import nc.ui.am.action.support.AMNCAction;
import nc.ui.am.status.StatusUtils;
import nc.ui.bd.ref.UFRefManage;
import nc.ui.fa.asset.event.WaitEventAddType;
import nc.ui.fa.asset.manager.AssetDataModel;
import nc.ui.fa.asset.view.AssetBillForm;
import nc.ui.pf.pub.TranstypeRefModel;
import nc.ui.pub.beans.UIDialog;
import nc.ui.pub.beans.UIRefPane;
import nc.ui.pub.pf.PfUtilClient;
import nc.ui.uif2.UIState;
import nc.vo.aim.equip.DeptscaleVO;
import nc.vo.am.common.BizContext;
import nc.vo.am.common.util.ArrayUtils;
import nc.vo.am.common.util.OrgUtils;
import nc.vo.am.common.util.StringUtils;
import nc.vo.am.constant.BillTypeConst_FA;
import nc.vo.am.constant.CommonKeyConst;
import nc.vo.am.manager.CurrencyManager;
import nc.vo.am.proxy.AMProxy;
import nc.vo.fa.asset.AggAssetVO;
import nc.vo.fa.asset.AssetVO;
import nc.vo.fa.asset.DeptscaleViewVO;
import nc.vo.fa.category.CategoryVO;
import nc.vo.fa.deptscale.DeptScaleVO;
import nc.vo.fa.scale.query.FAScaleCacheUtil;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.uif2.LoginContext;

/**
 * ��Դ������ť������������
 *
 * @author heyy1
 *
 */
public abstract class SourceAction extends AMNCAction {

	private static final long serialVersionUID = 1L;

	private AssetBillForm billForm = null;
	
	private UIRefPane transtype = null;

	public void doAction(ActionEvent e) throws Exception {
		int oldSelectedRow = ((AssetDataModel) getModel()).getSelectedRow();
		FAScaleCacheUtil.getInstance().getScaleCache().clear();
		// �л�����Ƭ
		if (!getBillForm().isComponentVisible()) {
			getBillForm().showMeUp();
		}
		// ҵ���߼�ǰ�Ĵ���
		beforeDispose();
		// ��ȡ�������ݽ����������
		AggregatedValueObject[] appNewData = getSourceToAssetAggVO();
		// У������
		boolean result = checkSourceBillData(appNewData);
		// ����ʲ������ȫ�ּ�����Ҫѡ�������ͣ������ţ�
		boolean categoryresult = true;
		// ���У��ͨ�����ٽ�������ж�
		if(result){
			categoryresult = checkCategory(appNewData);
		}
		// У��ͨ�� && ��������ȫ�ֲ���ѡ���˽�������
		if (result && categoryresult) {
			// ����Ĭ��ֵ
			fillDefultValue(appNewData);
			// ���д������ʲ�����
			((AssetDataModel) getModel()).getWaitBDM().addWaitForData(appNewData, isNeedClientLock());
			((AssetDataModel) getModel()).setWaitDataStatus(getAssetSource(), WaitEventAddType.SOURCE_Bill);
		}else{
			// ��Ч����
			// ���ý���״̬
			getModel().toOperateStatus(StatusUtils.view);
			// ����ѡ������
			// �����״δ򿪡��ʲ��������ڵ�ʱ��������Ƭ�󡾱������ӡ�����治��ʾֵ������   modified by zhaoxnc
    		int row = getModel().getSelectedRow();
			if(row < 0){
				getModel().setSelectedRow(oldSelectedRow);
			}
		}
	}
	
	/**
	 * ���� || �ʲ������ȫ�ֹܿأ���Ҫѡ��������
	 * @param appNewData
	 * @return
	 */
	protected boolean checkCategory(AggregatedValueObject[] appNewData){
		if (appNewData != null && appNewData.length > 0) {
			// ����ʲ����ܿ�
			Integer visScope = null;
			try {
				visScope = VisibleScopeUtils.getVisiblescope(CategoryVO.class.getName());
			} catch (BusinessException e) {
				Logger.error("����ʲ����ܿ�ģʽ����" + e.getMessage());/*-=notranslate=-*/
			}
			// ������������� || �ʲ������ȫ��
			// ȫ�ּ��ʲ������Ҫ���������ͶԻ���ֻ��Ϊ�����ÿ�Ƭ�Ľ���������ȷ��ģ����ʽ
			// ȫ���ʲ������û�н������ͣ�û��Ҫ���ݽ������͹�����
			// ��Ҫ������������ѡ��� 
			if(null != getAssetSource() && AssetSourceBillConst.handin_src.equals(getAssetSource())
					|| null != visScope && IBDMode.SCOPE_GLOBE == visScope){
				// ��������ѡ��Ի���
				// ��ò��յĹ�����
				UFRefManage refManage = getTranstypeRefPane().m_refManage;
				// ���ò��յ�����
				refManage.setRefUIConfig(getTranstypeRefPane().getRefUIConfig());
				if (refManage.showModal() == UIDialog.ID_OK) {
					String transtype_code = getTranstypeRefPane().getRefCode();
					String pk_transtype = getTranstypeRefPane().getRefPK();
					
					// 3���ѹ̶��ʲ�������õ�������
					for (int i = 0; i < appNewData.length; i++) {
						AssetVO avo = (AssetVO) appNewData[i].getParentVO();
						avo.setPk_transitype(pk_transtype);
						avo.setTransi_type(transtype_code);
					}
				}else{
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * �������Ͳ������
	 *
	 * @return
	 */
	private UIRefPane getTranstypeRefPane() {
		if (transtype == null) {
			transtype = new UIRefPane();
			TranstypeRefModel refModel = new TranstypeRefModel();
			refModel.setWhere(" " + CommonKeyConst.pk_group + "='" + getPk_group() + "' and  parentbilltype = 'H1'");
			transtype.setRefModel(refModel);
			// ֻ�ܵ�ѡ
			transtype.setMultiSelectedEnabled(false);
		}
		return transtype;
	}

	/**
	 * ҵ���߼�ǰ����
	 *
	 * @throws BusinessException
	 */
	protected void beforeDispose() throws Exception {

	}

	/**
	 * �������������һ�ȡѡ�е����ݣ��������ݽ������������ݽ�����ĵ���vo
	 *
	 * @return
	 * @throws BusinessException
	 */
	protected AggregatedValueObject[] getSourceToAssetAggVO() throws BusinessException {
		// ��������:��context���ݵ����棬��Ϊ�˲�������ʱ����ѯ��Ҫ�õ���
		LoginContext context = getModel().getContext();
		PfUtilClient.childButtonClicked(getSourceBillType(), context.getPk_group(), context.getPk_loginUser(), BillTypeConst.NEW_CARD_ADD,
				billForm, context, null);

		if (PfUtilClient.isCloseOK() == false) {
			return null;
		}
		// ��ò�������������
		AggregatedValueObject[] appNewData = null;
		try{
			appNewData = PfUtilClient.getRetVos();
		}catch(java.lang.NullPointerException e){
			getBillForm().showErrorMessage(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("facard_0","02012005-0680")/*@res "����ת�������쳣,�������ã�"*/);
		}
		if (appNewData == null || appNewData.length == 0) {
			return null;
		}
		/***add by zhoujian 2018��8��30��11:16:27 �����ʹ�õĲ�������***/
		for(AggregatedValueObject obj:appNewData){
			if(obj instanceof AggAssetVO){
				AggAssetVO aggVo = (AggAssetVO) obj;
				if(((AssetVO)aggVo.getParentVO()).getUsedep_flag().booleanValue()){
					String pk_equip = ((AssetVO)aggVo.getParentVO()).getPk_equip();
					String link_key = ((AssetVO)aggVo.getParentVO()).getPk_usedept();
					String pk_org = ((AssetVO)aggVo.getParentVO()).getPk_org();
					DeptScaleVO[] mulitDeptVos = getMulitDeptVos(pk_equip,link_key,pk_org);
					if(mulitDeptVos!=null&&!ArrayUtils.isEmpty(mulitDeptVos)){
						aggVo.setTableVO(CardTabConst.useDept, mulitDeptVos);
					}
				}
			}
		}
		/***add end***/
		return appNewData;
	}
	/**
	 * @author zhoujian
	 * �����ʹ�ò�������
	 * @param pk_equip
	 * @param link_key
	 * @param pk_org
	 * @return
	 * @throws BusinessException
	 */
	private DeptScaleVO[] getMulitDeptVos(String pk_equip,String link_key,String pk_org) throws BusinessException{
		String sql = "select * from pam_deptscale t where t.pk_equip=? and t.link_key=? and nvl(t.dr,0)=0";
		SQLParameter parameter = new SQLParameter();
		parameter.addParam(pk_equip);
		parameter.addParam(link_key);
		ArrayList<DeptscaleVO> deptList = (ArrayList<DeptscaleVO>) NCLocator.getInstance().lookup(IUAPQueryBS.class).executeQuery(sql, parameter, new BeanListProcessor(DeptscaleVO.class));
		if(deptList!=null&&!deptList.isEmpty()){
			DeptScaleVO[] deptScales = new DeptScaleVO[deptList.size()];
			for (int i = 0; i < deptList.size(); i++) {
				DeptScaleVO  scaleVO = new DeptScaleVO();
				scaleVO.setPk_dept(deptList.get(i).getPk_dept());
				HashMap<String, String> lastVIDSByDeptIDS = AMProxy.lookup(IDeptPubService.class).getLastVIDSByDeptIDS(new String[]{scaleVO.getPk_dept()});
				scaleVO.setPk_dept_v(lastVIDSByDeptIDS.get(scaleVO.getPk_dept()));
				scaleVO.setUsescale(deptList.get(i).getUsescale());
				scaleVO.setTotalscale(deptList.get(i).getTotalscale());
				scaleVO.setLink_key(deptList.get(i).getLink_key());
				scaleVO.setDr(0);
				scaleVO.setPk_org(pk_org);
				deptScales[i] = scaleVO;
			}
			if(deptScales!=null&&!ArrayUtils.isEmpty(deptScales)){
				return deptScales;
			}
		}
		return null;
	}
	
	/**
	 * ����Ӱ������
	 * @param appNewData
	 * @throws BusinessException
	 */
	protected void fillPKReceipt(AggregatedValueObject[] appNewData) throws BusinessException{
		if (appNewData != null && appNewData.length > 0) {
			String[] pk_receipts = NCLocator.getInstance().lookup(IdGenerator.class).generate(appNewData.length);
			for (int i = 0; i < appNewData.length; i++) {
				AssetVO vo = (AssetVO) appNewData[i].getParentVO();
				vo.setPk_receipt(pk_receipts[i]);
			}
		}
	}
	
	/**
	 * У��ѡ��ĵ������ݣ��Ƿ����Ҫ��
	 * <p>
	 * ȷ����ťʱ�Ѿ����˳��������У�顣�˴�������У�顣�ʹ���
	 *
	 * @param appNewData
	 * @return
	 * @throws BusinessException
	 */
	protected boolean checkSourceBillData(AggregatedValueObject[] appNewData) throws BusinessException {
		if (appNewData == null || appNewData.length == 0) {
			return false;
		}
		return true;

	}

	/**
	 * ��ͬ����Դ�����Σ����ݣ�������Ҫ����Ĵ���
	 *
	 * @param appNewDataͨ�����ݽ���������������
	 * @param appOldData�û�ѡ��ĵ�������
	 */
	protected void fillDefultValue(AggregatedValueObject[] appNewData) throws BusinessException {
		// ������֯��������֯���ڵı�λ��
		fillDefultCurrency(appNewData);
		// ���������е���Դ����
		fillSourceAndBillTypeData(appNewData);
		// ��Դ��Ŀ�ļ�ֵ�Ļ��ʻ���
		valueRateChageDispose(appNewData);
		// ����ҵ������
		fillBusinessData(appNewData);
		// ����Ӱ������
		fillPKReceipt(appNewData);
		// ��������
		otherDispose(appNewData);
	}

	/**
	 * ҵ�����ݲ���
	 *
	 * @param appNewData
	 * @throws BusinessException
	 */
	protected void fillBusinessData(AggregatedValueObject[] appNewData) throws BusinessException {
		if (appNewData != null && appNewData.length > 0) {
			String pk_group = getModel().getContext().getPk_group();
			for (int i = 0; i < appNewData.length; i++) {
				AssetVO vo = (AssetVO) appNewData[i].getParentVO();
				// ����
				vo.setPk_group(pk_group);
				// ��ʼʹ������
				if (vo.getBill_type().equals(BillTypeConst_FA.NEW_CARD_ADD)) { 
					if(null == vo.getBegin_date()){
						vo.setBegin_date(BizContext.getInstance().getBizDate().asBegin());
					}
				}
				// ��������ʹ��ҵ�����ڡ�
				vo.setBusiness_date(BizContext.getInstance().getBizDate());
				// �Ƶ��ˣ��Ƶ�����
				vo.setBillmaker(getModel().getContext().getPk_loginUser());
				vo.setBillmaketime(BizContext.getInstance().getServerDateTime());

				// ���ÿ�Ƭ����
				if (vo.getCard_num() == null) {
					vo.setCard_num(1);
				}
				// ���ÿ�Ƭ״̬
				if (vo.getAsset_state() == null) {
					vo.setAsset_state(AssetStateConst.exist);
				}
				// �����Ѽ����·�
				if (vo.getUsedmonth() == null) {
					vo.setUsedmonth(0);
				}
			}
		}
	}

	/**
	 * ��Դ���ݺ�Ŀ�ĵ��ݵļ�ֵ���ʻ���
	 * <p>
	 * ֻ����Ĭ�ϵ�VO���յ����:
	 *
	 * @param appNewData
	 * @throws BusinessException
	 */
	protected void valueRateChageDispose(AggregatedValueObject[] appNewData) throws BusinessException {

	}

	/**
	 * �������⴦��
	 *
	 * @param appNewData
	 * @throws BusinessException
	 */
	protected void otherDispose(AggregatedValueObject[] appNewData) throws BusinessException {

	}


	/**
	 * ������֯�������
	 *
	 * @param appNewData
	 */
	private void fillDefultCurrency(AggregatedValueObject[] appNewData) {
		// ������֯��������֯���ڵı�λ��
		if (appNewData != null && appNewData.length > 0) {
			for (int i = 0; i < appNewData.length; i++) {
				AssetVO headVO = (AssetVO) appNewData[i].getParentVO();
				String pk_org = headVO.getPk_org();
				// ������֯��汾
				String pk_org_v = getOrgVidByOid(pk_org);
				headVO.setPk_org_v(pk_org_v);

				if (headVO.getPk_currency() == null) {
					// ��ѯ��֯��λ�ҡ�
					String pk_currency = CurrencyManager.getLocalCurrencyPK(pk_org);
					headVO.setPk_currency(pk_currency);
				}
			}
		}
	}

	/**
	 * ȡ���°汾����
	 *
	 * @param pk_oid
	 * @return
	 */
	private String getOrgVidByOid(String pk_oid) {
		
		String pk_org_v = null;
		if(StringUtils.isNotEmpty(pk_oid)){
			try {
				pk_org_v = OrgUtils.getCurVidByPkOrg(pk_oid);
			} catch (BusinessException e) {
				Logger.error(e.getMessage());
			}
		}
/**	
		LoginContext context = this.getModel().getContext();
		OrgVO[] orgVOs = context.getFuncInfo().getFuncPermissionOrgVOs();
		if (ArrayUtils.isNotEmpty(orgVOs)) {
			for (OrgVO orgVO : orgVOs) {
				String pk_org = orgVO.getPk_org();
				if (pk_org.equals(pk_oid)) {
					pk_org_v =  orgVO.getPk_vid();
				}
			}
		}
**/
		return pk_org_v;
	}

	/**
	 * ������Դ���ݺ͵�����������
	 *
	 * @param appNewData
	 */
	private void fillSourceAndBillTypeData(AggregatedValueObject[] appNewData) {
		if (appNewData != null && appNewData.length > 0) {
			// ������Դ
			String source = getAssetSource();
			String nodeCode = ((AssetDataModel) getModel()).getContext().getNodeCode();
			// ��������
			String billtype = BillTypeConst_FA.NEW_CARD_ADD;
			// �������
			Integer new_flag = AssetUseConst.isNew;
			if (nodeCode.equals(FunCodeConst.ORIGIN_CARD)) {
				billtype = BillTypeConst_FA.ORI_CARD_ADD;
				new_flag = AssetUseConst.isOld;
			}
			// �������е��ݵ�����
			for (int i = 0; i < appNewData.length; i++) {
				AssetVO headVO = (AssetVO) appNewData[i].getParentVO();
				headVO.setBill_source(source);
				headVO.setBill_type(billtype);
				headVO.setNewasset_flag(new_flag);
			}
		}
	}

	/**
	 * ��Դ��������
	 *
	 * @return
	 */
	protected abstract String getSourceBillType();

	/**
	 * ���ɿ�Ƭ������
	 * <p>
	 * AssetSourceBillConst�еĳ���
	 *
	 * @return
	 */
	protected abstract String getAssetSource();

	/**
	 * �Ƿ���Ҫǰ̨����
	 *
	 * @return
	 */
	protected boolean isNeedClientLock() {
		return false;
	}

	public AssetBillForm getBillForm() {
		return billForm;
	}

	public void setBillForm(AssetBillForm billForm) {
		this.billForm = billForm;
	}

	protected void toStatus() {
		// ���ý���״̬
		getModel().setUiState(UIState.ADD);
	}
}