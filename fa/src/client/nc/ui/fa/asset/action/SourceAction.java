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
 * 来源生单按钮（参照生单）
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
		// 切换到卡片
		if (!getBillForm().isComponentVisible()) {
			getBillForm().showMeUp();
		}
		// 业务逻辑前的处理
		beforeDispose();
		// 获取上游数据交换后的数据
		AggregatedValueObject[] appNewData = getSourceToAssetAggVO();
		// 校验数据
		boolean result = checkSourceBillData(appNewData);
		// 如果资产类别是全局级，需要选择交易类型（本集团）
		boolean categoryresult = true;
		// 如果校验通过了再进行类别判断
		if(result){
			categoryresult = checkCategory(appNewData);
		}
		// 校验通过 && 如果类别是全局并且选择了交易类型
		if (result && categoryresult) {
			// 处理默认值
			fillDefultValue(appNewData);
			// 进行待生成资产处理
			((AssetDataModel) getModel()).getWaitBDM().addWaitForData(appNewData, isNeedClientLock());
			((AssetDataModel) getModel()).setWaitDataStatus(getAssetSource(), WaitEventAddType.SOURCE_Bill);
		}else{
			// 无效操作
			// 设置界面状态
			getModel().toOperateStatus(StatusUtils.view);
			// 重设选中数据
			// 处理首次打开【资产新增】节点时，新增卡片后【保存增加】后界面不显示值的问题   modified by zhaoxnc
    		int row = getModel().getSelectedRow();
			if(row < 0){
				getModel().setSelectedRow(oldSelectedRow);
			}
		}
	}
	
	/**
	 * 自制 || 资产类别是全局管控，需要选择交易类型
	 * @param appNewData
	 * @return
	 */
	protected boolean checkCategory(AggregatedValueObject[] appNewData){
		if (appNewData != null && appNewData.length > 0) {
			// 获得资产类别管控
			Integer visScope = null;
			try {
				visScope = VisibleScopeUtils.getVisiblescope(CategoryVO.class.getName());
			} catch (BusinessException e) {
				Logger.error("获得资产类别管控模式错误！" + e.getMessage());/*-=notranslate=-*/
			}
			// 如果是自制新增 || 资产类别是全局
			// 全局级资产类别需要弹交易类型对话框，只是为了设置卡片的交易类型以确定模板样式
			// 全局资产类别是没有交易类型，没必要根据交易类型过滤了
			// 需要弹出交易类型选择框 
			if(null != getAssetSource() && AssetSourceBillConst.handin_src.equals(getAssetSource())
					|| null != visScope && IBDMode.SCOPE_GLOBE == visScope){
				// 交易类型选择对话框
				// 获得参照的管理器
				UFRefManage refManage = getTranstypeRefPane().m_refManage;
				// 设置参照的配置
				refManage.setRefUIConfig(getTranstypeRefPane().getRefUIConfig());
				if (refManage.showModal() == UIDialog.ID_OK) {
					String transtype_code = getTranstypeRefPane().getRefCode();
					String pk_transtype = getTranstypeRefPane().getRefPK();
					
					// 3、把固定资产类别设置到单据中
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
	 * 交易类型参照面板
	 *
	 * @return
	 */
	private UIRefPane getTranstypeRefPane() {
		if (transtype == null) {
			transtype = new UIRefPane();
			TranstypeRefModel refModel = new TranstypeRefModel();
			refModel.setWhere(" " + CommonKeyConst.pk_group + "='" + getPk_group() + "' and  parentbilltype = 'H1'");
			transtype.setRefModel(refModel);
			// 只能单选
			transtype.setMultiSelectedEnabled(false);
		}
		return transtype;
	}

	/**
	 * 业务逻辑前处理
	 *
	 * @throws BusinessException
	 */
	protected void beforeDispose() throws Exception {

	}

	/**
	 * 参照生单，并且获取选中的数据，进行数据交换；返回数据交换后的单据vo
	 *
	 * @return
	 * @throws BusinessException
	 */
	protected AggregatedValueObject[] getSourceToAssetAggVO() throws BusinessException {
		// 参照生单:把context传递到后面，是为了参照生单时，查询需要用到。
		LoginContext context = getModel().getContext();
		PfUtilClient.childButtonClicked(getSourceBillType(), context.getPk_group(), context.getPk_loginUser(), BillTypeConst.NEW_CARD_ADD,
				billForm, context, null);

		if (PfUtilClient.isCloseOK() == false) {
			return null;
		}
		// 获得参照生单的数据
		AggregatedValueObject[] appNewData = null;
		try{
			appNewData = PfUtilClient.getRetVos();
		}catch(java.lang.NullPointerException e){
			getBillForm().showErrorMessage(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("facard_0","02012005-0680")/*@res "单据转换规则异常,请检查设置！"*/);
		}
		if (appNewData == null || appNewData.length == 0) {
			return null;
		}
		/***add by zhoujian 2018年8月30日11:16:27 添加所使用的部门数据***/
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
	 * 补充多使用部门数据
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
	 * 补充影像主键
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
	 * 校验选择的单据数据，是否符合要求。
	 * <p>
	 * 确定按钮时已经做了初步的类别校验。此处做其他校验。和处理
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
	 * 不同的来源（上游）单据，可能需要特殊的处理。
	 *
	 * @param appNewData通过数据交换管理处理后的数据
	 * @param appOldData用户选择的单据数据
	 */
	protected void fillDefultValue(AggregatedValueObject[] appNewData) throws BusinessException {
		// 根据组织，补充组织对于的本位币
		fillDefultCurrency(appNewData);
		// 设置数据中的来源单据
		fillSourceAndBillTypeData(appNewData);
		// 来源到目的价值的汇率换算
		valueRateChageDispose(appNewData);
		// 补充业务数据
		fillBusinessData(appNewData);
		// 补充影像数据
		fillPKReceipt(appNewData);
		// 其他处理
		otherDispose(appNewData);
	}

	/**
	 * 业务数据补充
	 *
	 * @param appNewData
	 * @throws BusinessException
	 */
	protected void fillBusinessData(AggregatedValueObject[] appNewData) throws BusinessException {
		if (appNewData != null && appNewData.length > 0) {
			String pk_group = getModel().getContext().getPk_group();
			for (int i = 0; i < appNewData.length; i++) {
				AssetVO vo = (AssetVO) appNewData[i].getParentVO();
				// 集团
				vo.setPk_group(pk_group);
				// 开始使用日期
				if (vo.getBill_type().equals(BillTypeConst_FA.NEW_CARD_ADD)) { 
					if(null == vo.getBegin_date()){
						vo.setBegin_date(BizContext.getInstance().getBizDate().asBegin());
					}
				}
				// 建卡日期使用业务日期。
				vo.setBusiness_date(BizContext.getInstance().getBizDate());
				// 制单人，制单日期
				vo.setBillmaker(getModel().getContext().getPk_loginUser());
				vo.setBillmaketime(BizContext.getInstance().getServerDateTime());

				// 设置卡片数量
				if (vo.getCard_num() == null) {
					vo.setCard_num(1);
				}
				// 设置卡片状态
				if (vo.getAsset_state() == null) {
					vo.setAsset_state(AssetStateConst.exist);
				}
				// 设置已计提月份
				if (vo.getUsedmonth() == null) {
					vo.setUsedmonth(0);
				}
			}
		}
	}

	/**
	 * 来源单据和目的单据的价值汇率换算
	 * <p>
	 * 只处理默认的VO对照的情况:
	 *
	 * @param appNewData
	 * @throws BusinessException
	 */
	protected void valueRateChageDispose(AggregatedValueObject[] appNewData) throws BusinessException {

	}

	/**
	 * 其他特殊处理
	 *
	 * @param appNewData
	 * @throws BusinessException
	 */
	protected void otherDispose(AggregatedValueObject[] appNewData) throws BusinessException {

	}


	/**
	 * 根据组织补充币种
	 *
	 * @param appNewData
	 */
	private void fillDefultCurrency(AggregatedValueObject[] appNewData) {
		// 根据组织，补充组织对于的本位币
		if (appNewData != null && appNewData.length > 0) {
			for (int i = 0; i < appNewData.length; i++) {
				AssetVO headVO = (AssetVO) appNewData[i].getParentVO();
				String pk_org = headVO.getPk_org();
				// 处理组织多版本
				String pk_org_v = getOrgVidByOid(pk_org);
				headVO.setPk_org_v(pk_org_v);

				if (headVO.getPk_currency() == null) {
					// 查询组织本位币。
					String pk_currency = CurrencyManager.getLocalCurrencyPK(pk_org);
					headVO.setPk_currency(pk_currency);
				}
			}
		}
	}

	/**
	 * 取最新版本主键
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
	 * 补充来源单据和单据类型数据
	 *
	 * @param appNewData
	 */
	private void fillSourceAndBillTypeData(AggregatedValueObject[] appNewData) {
		if (appNewData != null && appNewData.length > 0) {
			// 单据来源
			String source = getAssetSource();
			String nodeCode = ((AssetDataModel) getModel()).getContext().getNodeCode();
			// 单据类型
			String billtype = BillTypeConst_FA.NEW_CARD_ADD;
			// 新增标记
			Integer new_flag = AssetUseConst.isNew;
			if (nodeCode.equals(FunCodeConst.ORIGIN_CARD)) {
				billtype = BillTypeConst_FA.ORI_CARD_ADD;
				new_flag = AssetUseConst.isOld;
			}
			// 补充所有单据的数据
			for (int i = 0; i < appNewData.length; i++) {
				AssetVO headVO = (AssetVO) appNewData[i].getParentVO();
				headVO.setBill_source(source);
				headVO.setBill_type(billtype);
				headVO.setNewasset_flag(new_flag);
			}
		}
	}

	/**
	 * 来源单据类型
	 *
	 * @return
	 */
	protected abstract String getSourceBillType();

	/**
	 * 生成卡片的类型
	 * <p>
	 * AssetSourceBillConst中的常量
	 *
	 * @return
	 */
	protected abstract String getAssetSource();

	/**
	 * 是否需要前台加锁
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
		// 设置界面状态
		getModel().setUiState(UIState.ADD);
	}
}