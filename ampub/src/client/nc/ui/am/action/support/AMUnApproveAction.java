/**
 *
 */
package nc.ui.am.action.support;

import java.awt.event.ActionEvent;

import nc.bs.uif2.validation.DefaultValidationService;
import nc.bs.uif2.validation.IValidationService;
import nc.itf.am.prv.IDataService;
import nc.pub.am.common.validator.BillStatusUnApproveValidator;
import nc.ui.am.action.info.ActionInfoInitalizer;
import nc.ui.am.action.info.IAMActionCode;
import nc.ui.am.action.taskpf.PFScriptAction;
import nc.ui.am.status.StatusUtils;
import nc.ui.trade.businessaction.IPFACTION;
import nc.vo.am.common.AbstractAggBill;
import nc.vo.am.common.util.ArrayUtils;
import nc.vo.am.common.util.BaseVOUtils;
import nc.vo.am.constant.BillStatusConst;
import nc.vo.am.proxy.AMProxy;
import nc.vo.pub.AggregatedValueObject;

/**
 * ����������
 *
 * @author mxh
 */
public class AMUnApproveAction extends PFScriptAction {

	private static final long serialVersionUID = 1L;

	public AMUnApproveAction() {
		super();
		ActionInfoInitalizer.initializeAction(this, IAMActionCode.UNAPPROVE);
	}

	@Override
	public void doAction(ActionEvent e) throws Exception {
		AggregatedValueObject[] billVOs = getSelectedData(true);

		IValidationService validationservice = getValidationService();
		if(validationservice instanceof DefaultValidationService){
			((DefaultValidationService) validationservice).addValidator(new BillStatusUnApproveValidator());
			setValidationService(validationservice);
		}
		setDefaultValue(billVOs);
		procFlow(billVOs, e);
//		// ���������
//		setPfContext(null);
		// �л�״̬
		toStatus();
	}

	@Override
	public void processReturnObjs(Object[] returnObj){
		// ���½����ϵ�����
		//�Ӻ�̨�������չʾ����̨ modified by CYF at 2020-07-20
		nc.vo.am.common.AbstractAggBill[] oldAggVOs=(AbstractAggBill[]) returnObj;
		
		nc.vo.am.common.AbstractAggBill[] newAggVOs =AMProxy.lookup(IDataService.class).queryBillVOsByPK(oldAggVOs[0].getClass(),
				BaseVOUtils.getBillIds(oldAggVOs), true);
		getModel().directlyUpdate(newAggVOs);
		//CYF modify end 
		if(returnObj.length == 1){
			showHintMessage(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("ampub_0","04501000-0358")/*@res "ȡ�������ɹ�"*/);
		}
	}

	protected void setDefaultValue(Object obj) {

	}

	@Override
	protected void toStatus() {
		getModel().toAuditStatus();
	}

	@Override
	protected boolean isActionEnable() {
        boolean enable = getModel().containOperateStatus(StatusUtils.view) // ���̬
                && !getModel().containDataStatus(StatusUtils.noData) // ������
                && ( // ���̬�����ͨ��������У� add by weizq��Ҫ������˲�ͨ��ʱҲ��������(�����������˵����׼��ť�����)
                getModel().containAuditStatus(StatusUtils.auditPass) || getModel()
                        .containAuditStatus(StatusUtils.auditing)
                        || getModel().containAuditStatus(StatusUtils.auditNoPass));
        AggregatedValueObject[] selectedDatas = getSelectedData();
        if (ArrayUtils.isNotEmpty(selectedDatas) && selectedDatas.length > 1) {
            enable = true;
        }
        return enable;
    }

	@Override
	public String getActionCode() {
		String actionName = IPFACTION.UNAPPROVE;
		return actionName;
	}

	@Override
	protected int[] getRightBillStatus() {
		int[] status = new int[] { BillStatusConst.check_going,
				BillStatusConst.check_pass, BillStatusConst.check_nopass };
		return status;
	}

}