package nc.impl.am.pflow;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.bs.logging.Log;
import nc.bs.pub.pf.CheckStatusCallbackContext;
import nc.bs.pub.pf.ICheckStatusCallback;
import nc.bs.pub.pf.IPfAfterAction;
import nc.bs.pub.pf.IPfBeforeAction;
import nc.impl.am.db.VOPersistUtil;
import nc.itf.am.prv.IDataService;
import nc.itf.uap.pf.metadata.IFlowBizItf;
import nc.md.data.access.NCObject;
import nc.md.model.IBean;
import nc.md.model.IBusinessEntity;
import nc.md.model.access.javamap.AggVOStyle;
import nc.md.persist.framework.MDPersistenceService;
import nc.uap.pf.metadata.PfMetadataTools;
import nc.vo.am.common.AbstractAggBill;
import nc.vo.am.common.MDLockData;
import nc.vo.am.common.TransportBillVO;
import nc.vo.am.common.util.ArrayConstructor;
import nc.vo.am.common.util.ArrayUtils;
import nc.vo.am.common.util.BaseVOUtils;
import nc.vo.am.common.util.BillStatusUtils;
import nc.vo.am.common.util.StringUtils;
import nc.vo.am.common.util.VoStatusUtils;
import nc.vo.am.constant.ActionTypeConst;
import nc.vo.am.constant.CommonKeyConst;
import nc.vo.am.constant.PFConst;
import nc.vo.am.manager.LockManager;
import nc.vo.am.proxy.AMProxy;
import nc.vo.am.transfer.BillCombineTool;
import nc.vo.am.transfer.DataCompressServerTool;
import nc.vo.pf.change.PfUtilBaseTools;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.VOStatus;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.pf.IPFSourceBillFinder;
import nc.vo.pub.pf.SourceBillInfo;
import nc.vo.pubapp.pattern.log.TimeLog;
import nc.vo.pubapp.pattern.model.meta.entity.vo.PseudoColumnAttribute;

/**
 * 动作脚本执行前后处理类； 审批动作脚本的审批状态回调处理类 <li>注册在bd_billtype.checkclassname审批流检查类
 * 
 * @author zhengss
 * 
 */
public class PFActionProcessImpl implements IPfBeforeAction, IPfAfterAction, ICheckStatusCallback, IPFSourceBillFinder {
	private AggregatedValueObject[] billVOsClone = null;
	private Log log = Log.getInstance(this.getClass());

	/**
	 * 平台动作处理完成后，业务组的额外处理,该方法的返回值直接送到客户端
	 * 
	 * @param fullClientBillVO
	 *            更新前的完整VO，动作脚本处理的VO
	 * 
	 * @param retObjAfterAction
	 *            动作脚本返回的对象
	 * 
	 * @param hmPfExParams
	 *            扩展参数
	 * 
	 * @return
	 * 
	 * @throws BusinessException
	 */
	public Object afterAction(AggregatedValueObject fullClientBillVO, Object retObjAfterAction, HashMap hmPfExParams)
			throws BusinessException {
		// 调用批量操作方法
		if (fullClientBillVO == null || retObjAfterAction == null) {
			return retObjAfterAction;
		}
		AggregatedValueObject[] fullClientBillVOs = ArrayConstructor.getArray(fullClientBillVO);
		Object[] retObjAfterActions = ArrayConstructor.getArray(retObjAfterAction);

		Object[] objs = this.afterBatch(fullClientBillVOs, retObjAfterActions, hmPfExParams);

		String billStatusField = BillStatusUtils.getBillStatusFieldName(fullClientBillVO);
		if (StringUtils.isEmpty(billStatusField))
			billStatusField = CommonKeyConst.bill_status;

		if (ArrayUtils.isNotEmpty(objs)) {
			for (int i = 0; i < objs.length; i++) {
				if (retObjAfterActions[i] instanceof AggregatedValueObject) {
					AggregatedValueObject aggregatedValueObject = (AggregatedValueObject) retObjAfterActions[i];
					Object billstatus = aggregatedValueObject.getParentVO().getAttributeValue(billStatusField);
					if (objs[i] instanceof TransportBillVO) {
						TransportBillVO transportBillVO = (TransportBillVO) objs[i];
						transportBillVO.getParentVO().setAttributeValue(billStatusField, billstatus);

						setReturnValues(transportBillVO, aggregatedValueObject);
					}
				}
			}
			return objs[0];
		}
		return null;
	}

	/**
	 * 设置回写字段信息。
	 * 
	 * @param transportBillVO
	 * @param aggregatedValueObject
	 */
	protected void setReturnValues(TransportBillVO transportBillVO, AggregatedValueObject aggregatedValueObject) {
		String[] fields = new String[] { CommonKeyConst.bill_code, // 单据号
				CommonKeyConst.creator, // 创建人
				CommonKeyConst.creationtime, // 创建时间
				CommonKeyConst.busi_type //业务流pk
		};

		// 回写字段信息
		for (String field : fields) {
			Object fieldValue = aggregatedValueObject.getParentVO().getAttributeValue(field);
			if (fieldValue != null && StringUtils.isNotEmpty(fieldValue.toString())) {
				transportBillVO.getParentVO().setAttributeValue(field, fieldValue);
			}
		}

	}

	/**
	 * 平台动作批处理完成后，业务组的额外处理,该方法的返回值直接送到客户端
	 * 
	 * @param fullClientBillVOs
	 *            TODO 目前为后台查询的原始VO，应当为前台差异VO和原始VO的合集 动作脚本处理的VO
	 * @param retObjAfterAction
	 *            动作脚本返回的对象
	 * @param hmPfExParams
	 *            扩展参数
	 * @return
	 * @throws BusinessException
	 */
	public Object[] afterBatch(AggregatedValueObject[] fullClientBillVOs, Object[] retObjAfterAction,
			HashMap hmPfExParams) throws BusinessException {
		String actionType = hmPfExParams == null ? null : (String) hmPfExParams.get(ActionTypeConst.ACTION_TYPE);

		if (ArrayUtils.isEmpty(fullClientBillVOs) || ArrayUtils.isNotEmpty(retObjAfterAction)
				&& !(retObjAfterAction[0] instanceof AggregatedValueObject)) {
			return retObjAfterAction;
		}
		// 如果是删除操作
		if (ActionTypeConst.isDeleteAction(actionType)) {
			retObjAfterAction = fullClientBillVOs;
		}
		// 查询最新的VO数据 TODO 是否存在特殊的查询？如何处理？
		AggregatedValueObject[] bills = this.queryBillsAcceptNull((AbstractAggBill[]) retObjAfterAction);
		setPseudocolumn((AbstractAggBill[]) bills, (AbstractAggBill[]) retObjAfterAction);
		// 比较差异 返回差异数据
		Object[] pkBillVOs = getTransportBillVO(fullClientBillVOs, bills, actionType, hmPfExParams);

		return pkBillVOs;
	}

	private void setPseudocolumn(AbstractAggBill[] bills, AbstractAggBill[] retBills) {
		if (retBills == null || retBills.length == 0)
			return;
		HashMap<String, AbstractAggBill> retBillMap = new HashMap<String, AbstractAggBill>();
		for (AbstractAggBill retBill : retBills) {
			if (retBill != null)
				retBillMap.put(retBill.getPrimaryKey(), retBill);
		}
		for (AbstractAggBill bill : bills) {
			if (bill != null) {
				AbstractAggBill retBill = retBillMap.get(bill.getPrimaryKey());
				if (retBill != null)
					setPseudocolumn(bill, retBill);
			}
		}
	}

	private void setPseudocolumn(AbstractAggBill bill, AbstractAggBill retBill) {
		String[] tableCodes = bill.getTableCodes();
		for (String tableCode : tableCodes) {
			SuperVO[] bodyVOs = bill.getTableVO(tableCode);
			SuperVO[] retBodyVOs = retBill.getTableVO(tableCode);
			if (bodyVOs != null && retBodyVOs != null) {
				setPseudocolumn(bodyVOs, retBodyVOs);
			}
		}
	}

	private void setPseudocolumn(SuperVO[] bodys, SuperVO[] retBodys) {
		HashMap<String, SuperVO> retBodyMap = new HashMap<String, SuperVO>();
		for (SuperVO retBodyVO : retBodys) {
			retBodyMap.put(retBodyVO.getPrimaryKey(), retBodyVO);
		}
		for (SuperVO body : bodys) {
			SuperVO retBody = retBodyMap.get(body.getPrimaryKey());
			if (retBody != null) {
				Object pseudocolumn = retBody.getAttributeValue(PseudoColumnAttribute.PSEUDOCOLUMN);
				if (pseudocolumn != null)
					body.setAttributeValue(PseudoColumnAttribute.PSEUDOCOLUMN,
							retBody.getAttributeValue(PseudoColumnAttribute.PSEUDOCOLUMN));
			}
		}
	}

	/**
	 * TODO 此方法需要抽取出去
	 * 
	 * @param fullClientBillVOs
	 * @param bills
	 *            未被删除的单据
	 * @return
	 * @throws BusinessException
	 */
	private Object[] getTransportBillVO(AggregatedValueObject[] fullClientBillVOs, AggregatedValueObject[] bills,
			String actionType, HashMap hmPfExParams) throws BusinessException {
		Object[] retBills = null;
		// 说明为删除操作
		if (ActionTypeConst.isDeleteAction(actionType)) {
			retBills = DataCompressServerTool.fetchDeletedData(fullClientBillVOs, bills);
		} else {
			retBills = DataCompressServerTool.fetchDiffData(fullClientBillVOs, bills, hmPfExParams);
		}

		return retBills;
	}

	// /**
	// *
	// * @param fullClientBillVOs
	// * @param bills
	// * @param actionType
	// * @param hmPfExParams
	// * @return
	// * @throws BusinessException
	// */
	// private Object[] getTransportBillVO(AggregatedValueObject[]
	// fullClientBillVOs, AggregatedValueObject[] bills,
	// String actionType,Map hmPfExParams) throws BusinessException {
	// Object[] retBills = null;
	// // 说明为删除操作
	// if (ActionTypeConst.isDeleteAction(actionType)) {
	// retBills = DataCompressServerTool.fetchDeletedData(fullClientBillVOs,
	// bills);
	// } else {
	// retBills = DataCompressServerTool.fetchDiffData(fullClientBillVOs,
	// bills);
	// }
	//
	// return retBills;
	// }

	protected AggregatedValueObject[] queryBillsAcceptNull(AbstractAggBill[] billvos) throws BusinessException {
		if (billvos == null)
			return null;
		List<AbstractAggBill> notNullList = new ArrayList<AbstractAggBill>();
		List<Integer> nullIndexList = new ArrayList<Integer>();
		for (int i = 0; i < billvos.length; i++) {
			if (billvos[i] != null) {
				notNullList.add(billvos[i]);
			} else {
				nullIndexList.add(i);
			}
		}
		if (notNullList.size() == 0) {
			// 全是空，返回
			return billvos;
		} else {
			AbstractAggBill[] notNullBillVOs = notNullList.toArray(new AbstractAggBill[notNullList.size()]);
			AggregatedValueObject[] reloadvos = queryBills(notNullBillVOs);
			AggregatedValueObject[] result = (AggregatedValueObject[]) Array.newInstance(reloadvos.getClass()
					.getComponentType(), billvos.length);
			if (reloadvos != null && reloadvos.length > 0) {
				int j = 0;
				for (int i = 0; i < result.length; ++i) {
					if (!nullIndexList.contains(i)) {
						result[i] = reloadvos[j++];
					}
				}
			}
			return result;
		}
	}

	/**
	 * 查询最新单据VO
	 * 
	 * @param billvos
	 * @return
	 * @throws BusinessException
	 */
	protected AggregatedValueObject[] queryBills(AbstractAggBill[] billvos) throws BusinessException {
		if (billvos == null)
			return null;
		// update by taorz1 提供事务支持。
		AggregatedValueObject[] reloadvos = AMProxy.lookup(IDataService.class).queryBillVOsByPK(billvos[0].getClass(),
				BaseVOUtils.getBillIds(billvos), true);

		//		AggregatedValueObject[] result = (AggregatedValueObject[]) Array.newInstance(reloadvos.getClass()
		//				.getComponentType(), billvos.length);
		return reloadvos;
	}

	// /**
	// * 查询最新单据VO
	// *
	// * @param billvos
	// * @return
	// * @throws BusinessException
	// */
	// protected AggregatedValueObject[] queryBills(AbstractAggBill[] billvos)
	// throws BusinessException {
	// if (billvos == null)
	// return null;
	// List<String> keyList = new ArrayList<String>();
	// List<Integer> nullIndexList = new ArrayList<Integer>();
	// for (int i = 0; i < billvos.length; i++) {
	// String billId = null;
	// if (billvos[i] != null) {
	// billId = billvos[i].getParentVO().getPrimaryKey();
	// keyList.add(billId);
	// } else {
	// nullIndexList.add(i);
	// }
	// }
	// if (keyList.size() == 0) {
	// // 全是空，返回
	// return billvos;
	// } else {
	// String[] keys = keyList.toArray(new String[keyList.size()]);
	// // update by taorz1 提供事务支持。
	// AggregatedValueObject[] reloadvos =
	// AMProxy.lookup(IDataService.class).queryBillVOsByPK(
	// billvos[0].getClass(), keys, true);
	//
	// AggregatedValueObject[] result = (AggregatedValueObject[])
	// Array.newInstance(reloadvos.getClass()
	// .getComponentType(), billvos.length);
	// if (reloadvos != null && reloadvos.length > 0) {
	// int j = 0;
	// for (int i = 0; i < result.length; ++i) {
	// if (!nullIndexList.contains(i)) {
	// result[i] = reloadvos[j++];
	// }
	// }
	// }
	// return result;
	// }
	// }

	/**
	 * 审批流回调接口实现方法
	 * 
	 * @see nc.bs.pub.pf.ICheckStatusCallback#callCheckStatus(nc.bs.pub.pf.CheckStatusCallbackContext)
	 */
	public void callCheckStatus(CheckStatusCallbackContext cscc) throws BusinessException {
		AbstractAggBill billVO = (AbstractAggBill) cscc.getBillVo();

		String billStatusField = BillStatusUtils.getBillStatusFieldName(billVO);
		SuperVO headVO = (SuperVO) billVO.getParentVO();
		if (cscc.getApproveId() == null) {
			headVO.setAttributeValue(CommonKeyConst.Check_Opinion, null);
		} else {
			headVO.setAttributeValue(CommonKeyConst.Check_Opinion, cscc.getCheckNote());
		}

		headVO.setAttributeValue(CommonKeyConst.audittime, cscc.getApproveDate());
		headVO.setAttributeValue(CommonKeyConst.auditor, cscc.getApproveId());
		headVO.setStatus(VOStatus.UPDATED);

		if (StringUtils.isEmpty(billStatusField)) {
			billStatusField = CommonKeyConst.bill_status;
		}

		VOPersistUtil.update(new String[] { CommonKeyConst.auditor, CommonKeyConst.audittime, billStatusField,
				CommonKeyConst.Check_Opinion }, headVO);

	}

	/**
	 * @return 动作脚本执行前的VO，改VO对象将传给IPfAfterAction处理
	 */
	public AggregatedValueObject[] getCloneVO() {
		return billVOsClone;
	}

	@Override
	public AggregatedValueObject beforeAction(Object billVO, Object userObj, HashMap hmPfExParams)
			throws BusinessException {
		AggregatedValueObject[] billVOs = ArrayConstructor.getArray(billVO);
		Object[] userObjAry = ArrayConstructor.getArray(userObj);
		return beforeBatch(billVOs, userObjAry, hmPfExParams)[0];
	}

	@Override
	public AggregatedValueObject[] beforeBatch(Object[] billVOs, Object[] userObjAry, HashMap hmPfExParams)
			throws BusinessException {

		AbstractAggBill[] clientBills = (AbstractAggBill[]) ArrayConstructor.getArray(billVOs);
		if (VoStatusUtils.isNewStatus(clientBills)){
			billVOsClone = new AbstractAggBill[clientBills.length];
			for (int i = 0; i < clientBills.length; i++) {
				billVOsClone[i] = (AbstractAggBill) clientBills[i].clone();
			}
			return clientBills;
		}
		// 如果需要加锁则进行枷锁处理
		if (hmPfExParams != null && hmPfExParams.get(PfUtilBaseTools.PARAM_NO_LOCK) != null
				&& !Boolean.TRUE.equals(hmPfExParams.get(PfUtilBaseTools.PARAM_NO_LOCK))) {
			// 加锁，防止出现并发操作删除了数据
			LockManager.lock(new MDLockData(clientBills), new String[] { CommonKeyConst.ts }, null);
		}
		TimeLog.logStart();

		AbstractAggBill[] fullBills = null;
		if (hmPfExParams != null && UFBoolean.FALSE.equals(hmPfExParams.get(PFConst.IS_LIGHT_VO))) {
			fullBills = clientBills;
		} else {
			AbstractAggBill[] serverBills = (AbstractAggBill[]) queryBillsAcceptNull(clientBills);
			//前台数据因为架构问题没有将子表主键缓存到前台。这里会出现缺失子表主键的异常。直接使用后台数据
//			fullBills = new BillCombineTool<AbstractAggBill>().combine(serverBills, clientBills);
			fullBills=serverBills;
			//modified by CYF at 2020-07-20
		}
		// update by taorz1 调整一下克隆的方式。
		if (fullBills != null) {
			billVOsClone = new AbstractAggBill[fullBills.length];
			for (int i = 0; i < fullBills.length; i++) {
				billVOsClone[i] = (AbstractAggBill) fullBills[i].clone();
			}
		}

		// billVOsClone = CloneUtil.clone(fullBills);
		TimeLog.info(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("ampub_0", "04501000-0705")/*
		 * @
		 * res
		 * "%%%%%%%%%%%%%%%%%%%%beforeBatch方法执行时间*********************************"
		 */);

		return fullBills;
	}

	/**
	 * 流程平台发送消息接口 add by wukq
	 * 
	 * @date 2012-07-11
	 */
	@Override
	public SourceBillInfo[] findSourceBill(String pk_srcBilltype, Object billEntity) throws BusinessException {
		IBean bean = PfMetadataTools.queryMetaOfBilltype(pk_srcBilltype);
		AggregatedValueObject aggVo = (AggregatedValueObject) billEntity;
		NCObject ncObj = NCObject.newInstance(aggVo);
		IBusinessEntity relatedBean = (IBusinessEntity) ncObj.getRelatedBean();
		List<SourceBillInfo> infos = new ArrayList<SourceBillInfo>();
		if (relatedBean.isImplementBizInterface(IFlowBizItf.class.getName())) {
			Map<String, String> flowBizInfoMap = relatedBean.getBizInterfaceMapInfo(IFlowBizItf.class.getName());
			Object obj = ncObj.getAttributeValue(flowBizInfoMap.get(IFlowBizItf.ATTRIBUTE_SRCBILLID));
			if (obj != null && obj instanceof Object[]) {
				Object[] objs = (Object[]) obj;
				String[] pks = ArrayUtils.convertArrayType(objs, String.class);
				// 查找来源单据
				Collection collection = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPKs(
						getBillVOClass(bean), pks, false);// 查询本身会去重
				Set<AggregatedValueObject> srcAggVOs = new HashSet<AggregatedValueObject>(collection);// 同一张来源只会发一个消息
				for (AggregatedValueObject srcAggVO : srcAggVOs) {
					NCObject srcNcObj = NCObject.newInstance(srcAggVO);// 上游VO
					IFlowBizItf srcFlowBizItf = srcNcObj.getBizInterface(IFlowBizItf.class);
					SourceBillInfo info = new SourceBillInfo();
					info.setBillmaker(srcFlowBizItf.getBillMaker());
					info.setApprover(srcFlowBizItf.getApprover());
					info.setBillId(srcAggVO.getParentVO().getPrimaryKey());
					infos.add(info);
				}
			} else if (obj != null && !(obj instanceof Object[])) {
				NCObject srcNcObj = MDPersistenceService.lookupPersistenceQueryService().queryBillOfNCObjectByPK(
						getBillVOClass(bean), (String) obj);
				IFlowBizItf srcFlowBizItf = srcNcObj.getBizInterface(IFlowBizItf.class);
				SourceBillInfo info = new SourceBillInfo();
				info.setBillmaker(srcFlowBizItf.getBillMaker());
				info.setApprover(srcFlowBizItf.getApprover());
				info.setBillId((String) obj);
				infos.add(info);
			}
		}

		return infos.toArray(new SourceBillInfo[] {});
	}

	private Class<? extends AbstractAggBill> getBillVOClass(IBean bean) {

		AggVOStyle aggBeanSty = (AggVOStyle) bean.getBeanStyle();
		try {
			return (Class<? extends AbstractAggBill>) Class.forName(aggBeanSty.getAggVOClassName());
		} catch (ClassNotFoundException e) {
			log.error(e.getMessage());
		}
		return null;
	}

}