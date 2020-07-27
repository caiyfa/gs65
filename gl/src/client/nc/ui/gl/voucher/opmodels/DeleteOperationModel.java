package nc.ui.gl.voucher.opmodels;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JComponent;

import nc.bs.logging.Logger;
import nc.ui.gl.eventprocess.VoucherPowerCheckUtil;
import nc.ui.gl.gateway.glworkbench.GlWorkBench;
import nc.ui.gl.voucher.reg.VoucherFunctionRegister;
import nc.ui.gl.vouchercard.VoucherTabbedPane;
import nc.ui.gl.voucherdata.VoucherDataBridge;
import nc.ui.gl.vouchermodels.AbstractOperationModel;
import nc.ui.gl.vouchertools.VoucherOperatLogTool;
import nc.ui.pub.ToftPanel;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.pub.beans.UITabbedPane;
import nc.vo.gateway60.pub.GlBusinessException;
import nc.vo.gl.pubvoucher.DetailVO;
import nc.vo.gl.pubvoucher.OperationResultVO;
import nc.vo.gl.pubvoucher.VoucherVO;
import nc.vo.glcom.nodecode.GlNodeConst;
/**
 * ɾ��ƾ֤���ܣ�ƾ֤���棩
 */
public class DeleteOperationModel extends AbstractOperationModel {
/**
 * CashFlowOperationModel ������ע�⡣
 */
public DeleteOperationModel() {
	super();
}
/**
 * �˴����뷽��˵����
 * �������ڣ�(2002-6-26 14:27:28)
 * @return java.lang.Object
 */
public java.lang.Object doOperation()
{
	if (showConfirmDialog())
		doVoucherDelete();
	return null;
}
/**
 * �˴����뷽��˵����
 * �������ڣ�(2002-6-26 14:27:28)
 * @return java.lang.Object
 */
private void doVoucherDelete()
{
	getMasterModel().setParameter("stopediting", null);
	VoucherVO voucher = (VoucherVO) getMasterModel().getParameter("vouchervo");
	if (voucher.getPk_voucher() == null)
	{
		throw new GlBusinessException(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005","UPP20021005-000109")/*@res "��δ�����ƾ֤�޷�ɾ����"*/);
	}
	//����Ƿ���Ա��棨�Ѷ������ݲ����޸ģ�add by CYF 
			try {
				VoucherVO voucherFromDB = VoucherDataBridge.getInstance().queryByPk(voucher.getPk_voucher());
				if(voucherFromDB!=null){
					for(DetailVO detail:voucherFromDB.getDetails()){
						if(detail.getContrastflag()==1){
							throw new Exception("����ɾ����ƾ֤�а������ڶ��˵�ƾ֤��¼��");
						}
					}
				}
			} catch (Exception e) {
				throw new GlBusinessException(e.getMessage());
			}
			//CYF add end
	boolean successflag = true;
	// hurh ��տշ�¼
	voucher.clearDetail();
	voucher = (VoucherVO) voucher.clone();
	 Component jc=(Component)getMasterModel().getUI();
		Boolean ispower = new Boolean(false);
     while (! (jc instanceof ToftPanel)&&jc!=null)  {
     	jc=(Component) jc.getParent();
		}
     ToftPanel tfpanel=(ToftPanel)jc;

     //ɾ��Ŀ������ƾ֤ˢ����Դƾ֤��
     Component pane = (Component)getMasterModel().getUI();
     UITabbedPane voucherTabPane = null;
     while (pane.getParent() != null)
     {
         if (pane instanceof UITabbedPane)
         {
          voucherTabPane = (UITabbedPane) pane;
             break;
         }
         else
        	 pane = pane.getParent();
     }
	try
	{
		//VoucherVO vo = VoucherDataBridge.getInstance().queryByPk(strVoucherPK);
		//getVoucherPanel().setVO(vo);
		ispower = VoucherDataBridge.getInstance().isAccsubjPower(voucher,  GlWorkBench.getLoginUser()/*ClientEnvironment.getInstance().getUser().getPrimaryKey()*/);
		if (ispower.booleanValue()) {
			nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage((javax.swing.JComponent) getMasterModel().getUI(), nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100555","UPP2002100555-000047")/*@res "��ʾ"*/,  nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100557","UPP2002100557-000068")/*@res "ƾ֤����Щ��Ŀû��Ȩ��,����ɾ����ƾ֤."*/);
		return;
		}
		VoucherPowerCheckUtil.checkVoucherDelPower(new VoucherVO[]{voucher});
		OperationResultVO[] result = VoucherDataBridge.getInstance().deleteByPks(new String[] { voucher.getPk_voucher()}, GlWorkBench.getLoginUser()/*ClientEnvironment.getInstance().getUser().getPrimaryKey()*/);
		
		if(result != null && result.length > 0){
			StringBuilder sb = new StringBuilder();
			for(OperationResultVO vo : result){
				sb.append(vo.m_strDescription + "\n");
				if(vo.m_intSuccess == OperationResultVO.ERROR){
					successflag = false;
				}
			}
			nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage((JComponent) getMasterModel().getUI(), nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100555","UPP2002100555-000047")/*@res "��ʾ"*/,  sb.toString());
		}
	}
	catch (GlBusinessException e)
	{
		Logger.error(e.getMessage(), e);
		throw e;
	}
	catch (Exception e)
	{
		//Logger.error(e.getMessage(), e);
		Logger.error(e.getMessage(), e);
		throw new GlBusinessException(e.getMessage());
	}
	
	if(successflag){
		// hurh V60 ɾ��ƾ֤ʱ��ҳǩ����
		Object doOperation = getMasterModel().doOperation(VoucherFunctionRegister.FUNCTION_REMOVETAB_BY_VOUCHERPK);
			if (doOperation != null&& "delconvertvoucher".equals(doOperation)) {
				if (voucherTabPane != null&& voucherTabPane.getParent() != null&& voucherTabPane.getParent() instanceof VoucherTabbedPane) {
					VoucherTabbedPane selfPane = (VoucherTabbedPane) voucherTabPane.getParent();
					selfPane.getVoucherPanel().getVoucherModel().doOperation(
									VoucherFunctionRegister.FUNCTION_REFRESHVOUCHER);
				}
			}
        getMasterModel().setParameter("saveflag", new Boolean(true));
		getMasterModel().doOperation(VoucherFunctionRegister.FUNCTION_FORWARD);
		getMasterModel().setParameter("removevouchers", new VoucherVO[] { voucher });
		new VoucherOperatLogTool().InsertLogByVoucher(voucher, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55","UC001-0000039")/*@res "ɾ��"*/, GlNodeConst.GLNODE_VOUCHERPREPARE);
	}
	if(successflag){
		tfpanel.showHintMessage(nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100555","UPP2002100555-000041")/*@res "ɾ�����"*/);
	}
}
private boolean showConfirmDialog()
{
	return MessageDialog.showYesNoDlg((Container) getMasterModel().getUI(), nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005","UPP20021005-000110")/*@res "��ʾ"*/, nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005","UPP20021005-000111")/*@res "���Ҫɾ����ƾ֤��"*/,MessageDialog.ID_NO) == MessageDialog.ID_YES;
}
}