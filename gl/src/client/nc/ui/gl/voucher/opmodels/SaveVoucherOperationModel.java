package nc.ui.gl.voucher.opmodels;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import javax.swing.JComponent;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.desktop.ui.WorkbenchEnvironment;
import nc.itf.gl.pubreconcile.IPubReconcile;
import nc.ui.gl.datacache.AccountCache;
import nc.ui.gl.datacache.GLParaDataCache;
import nc.ui.gl.eventprocess.VoucherPowerCheckUtil;
import nc.ui.gl.gateway.glworkbench.GlWorkBench;
import nc.ui.gl.voucher.dlg.InputAttachmentDlg;
import nc.ui.gl.voucher.reg.VoucherFunctionRegister;
import nc.ui.gl.voucherdata.VoucherDataBridge;
import nc.ui.gl.vouchertools.VoucherDataCenter;
import nc.ui.gl.vouchertools.VoucherOperatLogTool;
import nc.ui.glpub.UiManager;
import nc.ui.glrp.vouch.DataFromVoucher;
import nc.ui.pub.ToftPanel;
import nc.ui.pub.beans.MessageDialog;
import nc.vo.gateway60.pub.GlBusinessException;
import nc.vo.gl.pubreconcile.PubReconcileInfoVO;
import nc.vo.gl.pubvoucher.DetailVO;
import nc.vo.gl.pubvoucher.OperationResultVO;
import nc.vo.gl.pubvoucher.VoucherVO;
import nc.vo.glcom.nodecode.GlNodeConst;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;

/**
 * ����ƾ֤
 */
@SuppressWarnings("deprecation")
public class SaveVoucherOperationModel extends nc.ui.gl.vouchermodels.AbstractOperationModel {
	/**
	 * �˴����뷽��˵���� �������ڣ�(2002-6-26 14:27:28)
	 *
	 * @return java.lang.Object
	 */
	public Object doOperation() {

		Boolean isInSum = (Boolean) getMasterModel().getParameter("isInSumMode");
		if (isInSum != null && isInSum.booleanValue()) {
			return null;
		}
		getMasterModel().setParameter("stopediting", null);

		boolean isdap = false;
		String dapsubstr = null;

		Component jc = (Component) getMasterModel().getUI();
		while (!(jc instanceof UiManager)) {
			jc = (Component) jc.getParent();
		}
		UiManager tfpanel = (UiManager) jc;
		tfpanel.showHintMessage(nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100555", "UPP2002100555-000046")/*
																													 * @res
																													 * "���ڱ���"
																													 */);

		String funcname = (String) getMasterModel().getParameter("functionname");
		VoucherVO voucher = (VoucherVO) getMasterModel().getParameter("vouchervo");
		//����Ƿ���Ա��棨�Ѷ������ݲ����޸ģ�add by CYF 
				try {
					VoucherVO voucherFromDB = VoucherDataBridge.getInstance().queryByPk(voucher.getPk_voucher());
					if(voucherFromDB!=null){
						for(DetailVO detail:voucherFromDB.getDetails()){
							if(detail.getContrastflag()==1){
								throw new Exception("�޸ĵ�ƾ֤�а������ڶ��˵�ƾ֤��¼��");
							}
						}
					}
				} catch (Exception e) {
					throw new GlBusinessException(e.getMessage());
				}
				//CYF add end 
		if(voucher.getPk_accountingbook() == null){
			nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage((JComponent)getMasterModel().getUI(), nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("glpub_0","02002003-0102")/*@res "��ʾ"*/, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("glpub_0","02002003-0193")/*@res "��ѡ������˲���"*/);
			return null;
		}
		//
		if (voucher.getUserData() instanceof String[]) {
			String[] new_name = (String[]) voucher.getUserData();
			String prefix = "dap_save_Action";
			if (voucher.getFree7() != null && voucher.getFree7().startsWith(prefix)) {
				isdap = true;
				dapsubstr = voucher.getFree7().substring(prefix.length());
				//voucher.setFree7(null);
			}
		}
		//jiji
		
		// reconcile begin
		if (funcname.equals("preparevoucher")) {
			for (int i = 0; i < voucher.getDetails().length; i++) {
				if (voucher.getDetail(i).getUserData() != null && voucher.getDetail(i).getUserData().getClass().toString().equals("class nc.vo.gl.reconcileinit.ModelsetvoucherVO")) {
					int result = MessageDialog.showOkCancelDlg((javax.swing.JComponent) getMasterModel().getUI(), nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100557", "UPP2002100557-000067")/*
																																																	 * @res
																																																	 * "��ʾ"
																																																	 */, nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100557", "UPP2002100557-000069")/*
																											 * @res
																											 * "ƾ֤�Ѿ�����Эͬ,�Ƿ��ձ���Эͬ���д���?"
																											 */);
					if (result == MessageDialog.ID_OK) {
						getMasterModel().doOperation(VoucherFunctionRegister.FUNCTION_LPY_Reconsave);
						return null;
					}
					break;
				}
			}
		}
		// reconcile end
		voucher = (VoucherVO) voucher.clone();
		voucher.setTempsaveflag(UFBoolean.FALSE);
		Integer editvoucherno = voucher.getNo() == null ? null : Integer.valueOf(voucher.getNo().intValue());
		String pk_voucher = voucher.getPk_voucher();
		if (pk_voucher == null) {
			Boolean isNoChanged = (Boolean) getMasterModel().getParameter("isNoChanged");
			if (!isNoChanged.booleanValue())
				voucher.setNo(Integer.valueOf(0));
		}
		String err = "";
		if (funcname.equals("checkvoucher")) {
			if (voucher.getDiscardflag() != null && voucher.getDiscardflag().booleanValue()) {
				err = err + nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000066")/*
																											 * @res
																											 * "#������#"
																											 */;
			}
			if (voucher.getPk_manager() != null) {
				err = err + nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000063")/*
																											 * @res
																											 * "#�Ѽ���#"
																											 */;
			}
		} else {
			if (funcname.equals("offsetvoucher") && voucher.getPk_voucher() != null)
				return null;
			if (voucher.getDiscardflag() != null && voucher.getDiscardflag().booleanValue()) {
				err = err + nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000066")/*
																											 * @res
																											 * "#������#"
																											 */;
			}
			if (voucher.getPk_casher() != null) {
				err = err + nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000065")/*
																											 * @res
																											 * "#��ǩ��#"
																											 */;
			}
			if (voucher.getPk_checked() != null) {
				err = err + nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000064")/*
																											 * @res
																											 * "#�����#"
																											 */;
			}
			if (voucher.getPk_manager() != null) {
				err = err + nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000063")/*
																											 * @res
																											 * "#�Ѽ���#"
																											 */;
			}
		}
		// 50
		if ((funcname.equals("preparevoucher") || funcname.equals("voucherbridge")) && VoucherDataCenter.isInputAttachment(voucher.getPk_accountingbook())
				&& (voucher.getAttachment() == null || voucher.getAttachment().intValue() == 0)) {
			// getMasterModel().doOperation(VoucherFunctionRegister.FUNCTION_LPY_INPUTATTACHMENT);
			// voucher=(VoucherVO) getMasterModel().getParameter("vouchervo");
			InputAttachmentDlg inputattachment = new InputAttachmentDlg();
			inputattachment.showModal();
			voucher.setAttachment(inputattachment.getNumber());

			// showInputAttchmentDialog();
			getMasterModel().setParameter("vouchervo", voucher);

		}
		
		String pk_user = WorkbenchEnvironment.getInstance().getLoginUser().getPrimaryKey();
		voucher.setPk_prepared(pk_user);//add by zhaoyangm 2013-07-12 ���ƽ̨�����������������ť�����Ƶ���Ϊ��ǰ��½��

		if (err.trim().length() > 0)
			throw new GlBusinessException(err);
		
		//begin-ncm-yanxf-NC2014051200173-2014��5��30��13:29:56
				/** yanxf1
				 * ���˲�������Ϊ����ƾ֤ʱ���������������������һ�Ŵ����ֽ��Ŀ��ƾ֤����ʱ������ֽ����ģ��������ڰѴ���ƾ֤�ķ�¼�޸�Ϊ���ֽ����п�Ŀ������ʱ�Ͳ�����ֽ����ˣ���ԭ������ƾ֤���ֽ���������������ʾ�ͻᵼ���ҵ��ֽ����������ݲ���ȷ
				  */
//				 ���ֽ�ƾ֤���ڱ����ʱ����������ֽ�������Ϣ¼�룬����Ҫ��ʾ�Ƿ��������
				 boolean iscashflow = false;
				for (int i = 0; i < voucher.getDetails().length; i++) {
					if (voucher.getDetails()[i].getPk_accasoa() != null
							&& AccountCache.getInstance().getAccountVOByPK(voucher.getPk_accountingbook(), voucher.getDetails()[i].getPk_accasoa(),voucher.getPrepareddate().toStdString())
							.getCashtype().intValue() > 0) {
						iscashflow = true;
						break;
					}
				}

				if (!iscashflow) {
					for (int i = 0; i < voucher.getDetails().length; i++) {
						if (voucher.getDetails()[i].getCashFlow() != null
								&& voucher.getDetails()[i].getCashFlow().length > 0) {
							int r = MessageDialog.showOkCancelDlg(
									(Container) getMasterModel().getUI(), nc.ui.ml.NCLangRes.getInstance().getStrByID("200235","UPP200235-000044")/*@res "��ʾ"*/,
									nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100555", "UPP2002100555-000078")/*
									 * @res
									 * "���ֽ�ƾ֤¼�����ֽ�����,Ҫ������?"
									 */);
							if (r == MessageDialog.ID_CANCEL) {
								return null;
							}
							break;
						}
					}
					for (int i = 0; i < voucher.getDetails().length; i++) {
						if (voucher.getDetails()[i].getCashFlow() != null
								&& voucher.getDetails()[i].getCashFlow().length > 0) {
							voucher.getDetails()[i].setCashFlow(null);
							voucher.getDetails()[i].setOtheruserdata(null);
						}
					}
				}
				//begin-ncm-yanxf-NC2014051200173-2014��5��30��13:29:56

		OperationResultVO[] result = save(voucher);

		if (result != null) {
			voucher = (VoucherVO) result[0].m_userIdentical;
			getMasterModel().setParameter("updatevouchers", new VoucherVO[] { voucher });
			getMasterModel().setParameter("saveflag", new Boolean(true));

			StringBuffer strMsg = new StringBuffer();
			boolean errflag = false;
			for (int i = 0; i < result.length; i++) {
				switch (result[i].m_intSuccess) {
				case 0:
					while (!(jc instanceof UiManager)) {
						jc = (Component) jc.getParent();
					}
					tfpanel = (UiManager) jc;
					tfpanel.showHintMessage(nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100555", "UPP2002100555-000006")/*
																																 * @res
																																 * "����ɹ�"
																																 */);

					break;
				case 1:
					strMsg.append("Warning:" + result[i].m_strDescription + "\n");
					while (!(jc instanceof UiManager)) {

						jc = (Component) jc.getParent();
					}
					tfpanel = (UiManager) jc;
					tfpanel.showHintMessage(nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100555", "UPP2002100555-000006")/*
																																 * @res
																																 * "����ɹ�"
																																 */);

					break;
				case 2:
					errflag = true;
					strMsg.append("Error:" + result[i].m_strDescription + "\n");
					while (!(jc instanceof UiManager)) {

						jc = (Component) jc.getParent();
					}
					tfpanel = (UiManager) jc;
					tfpanel.showHintMessage(nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100555", "UPP2002100555-000007")/*
																																 * @res
																																 * "����ʧ��"
																																 */);
					// tfpanel.showHintMessage("����ʧ��");
					break;
				default:
					strMsg.append("Message:" + result[i].m_strDescription + "\n");
				}
			}
			if (!errflag) {
				new VoucherOperatLogTool().InsertLogByVoucher(voucher, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55","UC001-0000001")/*@res "����"*/, GlNodeConst.GLNODE_VOUCHERPREPARE);
			}
			
//			if(!errflag) {
//				//�������ɹ��򽫻��ƽ̨��Ϣ�����
//				voucher.setFipInfo(null);
//				getMasterModel().setParameter("vouchervo",voucher);
//			}
			
			if (strMsg.length() > 0)
				nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage((Container) getMasterModel().getUI(), nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000048")/*
																																												 * @res
																																												 * "����"
																																												 */, strMsg.toString());
		}
		if (editvoucherno != null && editvoucherno.intValue() != voucher.getNo().intValue() && editvoucherno.intValue() != 0) {
			nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage((Container) getMasterModel().getUI(), nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100555", "UPP2002100555-000047")/*
																																												 * @res
																																												 * "��ʾ"
																																												 */,  nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100555", "UPP2002100555-000048"/*
																																															 * @res
																																															 * "�ѱ���ƾ֤��ƾ֤���������ƾ֤�Ų�ͬ,ԭ�����ƾ֤��Ϊ {0},�ѱ���ƾ֤��ƾ֤��Ϊ "
																																																						 */+ voucher.getNo(),null,new String[]{String.valueOf(editvoucherno)}) + " .");

		}
		if ((funcname.equals("preparevoucher") || funcname.equals("voucherbridge")) && VoucherDataCenter.isVoucherSaveRTVerify(voucher.getPk_accountingbook())) {
			// ����ƾ֤����ȡ�õ�һ��������Ŀ�ķ�¼���
			Object ob = getMasterModel().getParameter("selectedindex");
			DetailVO setselectdetail = null;
			try {

				if (ob == null) {

					setselectdetail = new DataFromVoucher().getFirstVerifyDetailVO(voucher, null);

				} else {
					setselectdetail = new DataFromVoucher().getFirstVerifyDetailVO(voucher, Integer.valueOf(ob.toString()));

				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			if (setselectdetail == null) {
				// getMasterModel().setParameter("selectedindex", null);
			} else {
				getMasterModel().setParameter("selectedindexes", new int[] { setselectdetail.getDetailindex().intValue() - 1 });
				getMasterModel().setParameter("updatevouchers", new VoucherVO[] { voucher });
				getMasterModel().doOperation(VoucherFunctionRegister.FUNCTION_REALTIMEVERIFY);
			}

		}
		// if (!funcname.equals("preparevoucher") || pk_voucher != null)
		// nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage((Container)
		// getMasterModel().getUI(),
		// nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100557",
		// "UPP2002100557-000067")/*
		// * @res
		// * "��ʾ"
		// */, nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005",
		// "UPP20021005-000079")/*
		// * @res
		// * "ƾ֤����ɹ�"
		// */);
		if (VoucherDataCenter.isPrintAfterSave(voucher.getPk_accountingbook()))
			getMasterModel().doOperation(VoucherFunctionRegister.FUNCTION_PRINTVOUCHER);
		
		// if
		// (AccountBookUtil.getAccountingBookVOByPrimaryKey(voucher.getPk_accountingbook())
		// != null &&
		// AccountBookUtil.getAccountingBookVOByPrimaryKey(voucher.getPk_accountingbook()).getType().intValue()
		// == 0)
		//63EHP2���㸲��
		if (GLParaDataCache.getInstance().isInstantProductVoucher(voucher.getPk_accountingbook()) != null && GLParaDataCache.getInstance().isInstantProductVoucher(voucher.getPk_accountingbook()).booleanValue()) {
			getMasterModel().doOperation(VoucherFunctionRegister.FUNCTION_REALTIMECONVERT);
			// return null;
		}else{
			if (voucher.getConvertflag() != null && voucher.getConvertflag().booleanValue()) {
				int r = MessageDialog.showWarningDlg((Container) getMasterModel().getUI(), "", nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000738")/*
																																											 * @res
																																											 * "�޸ĵ�ƾ֤�Ѿ���������������½������㡣"
																																											 */);
				if (r == MessageDialog.ID_YES)
					getMasterModel().doOperation(VoucherFunctionRegister.FUNCTION_REALTIMECONVERT);
			}
		}
		//��ʱЭͬȥ��
//		if (GLParaDataCacheUseUap.getRunTimeReconcile(voucher.getPk_accountingbook()) == ParaMacro.RUNTIMERECONCILE_YES) {
//			//runtimeReconcile(voucher);
//
//			getMasterModel().doOperation(VoucherFunctionRegister.FUNCTION_REFRESHVOUCHER);
//			// getMasterModel().setParameter("updatevouchers", new VoucherVO[] {
//			// voucher });
//		} else if (GLParaDataCacheUseUap.getRunTimeReconcile(voucher.getPk_accountingbook()) == ParaMacro.RUNTIMERECONCILE_HINT) {
//			int r = MessageDialog.showYesNoCancelDlg((Container) getMasterModel().getUI(), nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002GL502", "UPP2002GL502-000023")/*
//																																													 * @res
//																																													 * "��ʾ"
//																																													 */,
//					nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002GL502", "UPP2002GL502-000033")/*
//																												 * @res
//																												 * "�Ƿ�ʱЭͬ"
//																												 */);
//			if (r == MessageDialog.ID_YES) {
//				runtimeReconcile(voucher);
//
//				getMasterModel().doOperation(VoucherFunctionRegister.FUNCTION_REFRESHVOUCHER);
//				// getMasterModel().setParameter("updatevouchers", new
//				// VoucherVO[] { voucher });
//			}
//		}
		// getMasterModel().doOperation(VoucherFunctionRegister.FUNCTION_REALTIMEVERIFY);
		if (funcname != null) {
			if (funcname.equals("voucherbridge")) {
				getMasterModel().doOperation(VoucherFunctionRegister.FUNCTION_NEXT);
			} else if (funcname.equals("preparevoucher")) {
				// if (pk_voucher == null) {
				getMasterModel().doOperation(VoucherFunctionRegister.FUNCTION_COPYSMALLVOUCHER);
				// }
			}
		}

		// ͨ��ƾ֤�ŷ�ʽ����ƾ֤��ǰ̨����Ĵ���ͨ������ķ�ʽ���ýӿ�
		// �ӿ� IVoucherUISave aftervoucher(voucher vo)
		String modulecode = (String) getMasterModel().getParameter("modulecode");
		if (funcname.equals("voucherbridge") && !modulecode.startsWith("2002")) {

		}

		if (isdap) {
			forUfidaDapUI(voucher, dapsubstr);
		}

		return null;
	}

	private void forUfidaDapUI(VoucherVO voucher, String dapsubstr) {
		if (dapsubstr != null) {
			String substr = dapsubstr;
			String dapclass = "nc.ui.dap.voucher.DapMessageCenter";
			Class dc;
			try {
				dc = Class.forName(dapclass);
				Object o = dc.newInstance();
				Method seme = dc.getMethod("sendMessage", new Class[] { String.class, Object.class });
				seme.invoke(o, new Object[] { substr, null });
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				Logger.error(e.getMessage(), e);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				Logger.error(e.getMessage(), e);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				Logger.error(e.getMessage(), e);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				Logger.error(e.getMessage(), e);
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				Logger.error(e.getMessage(), e);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				Logger.error(e.getMessage(), e);
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				Logger.error(e.getMessage(), e);
			}

		}

	}

	/**
	 * �˴����뷽��˵���� �������ڣ�(2003-10-1 19:14:33)
	 */
	private OperationResultVO[] save(VoucherVO voucher) {
		Component jc = (Component) getMasterModel().getUI();
		while (!(jc instanceof ToftPanel) && jc != null) {
			jc = (Component) jc.getParent();
		}
		ToftPanel tfpanel = (ToftPanel) jc;
		try {
			String userID = GlWorkBench.getLoginUser()/*nc.ui.pub.ClientEnvironment.getInstance().getUser().getPrimaryKey()*/;
			Integer attachment = voucher.getAttachment();
			if (voucher.getPk_prepared() == null)
				voucher.setPk_prepared(userID);
			if (VoucherDataCenter.isVoucherSelfEditDelete(voucher.getPk_accountingbook()) && !userID.equals(voucher.getPk_prepared())) {
				throw new GlBusinessException(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000080")/*
																															 * @res
																															 * "ƾֻ֤���ɱ��˽����޸ģ�"
																															 */);
			}

			// voucher.clearEmptyDetail();
			voucher.clearDetail();
			voucher.setExplanation(voucher.getNumDetails() > 0 ? voucher.getDetail(0).getExplanation() : null);
			String operate;
			String pk_voucher = voucher.getPk_voucher();
			operate = "mod";
			if (voucher.getPk_voucher() != null)
				voucher.setPk_prepared(userID);
			// setV_Preparedname(getClientEnvironment().getUser().getUser_name());
			// setV_Signflag(nc.vo.pub.lang.UFBoolean.FALSE);
			// boolean isuseddatapower =
			// nc.ui.bd.datapower.DataPowerServiceBO_Client.isUsedDataPower("bd_accsubj",
			// voucher.getPk_corp());
			HashMap tmp_hashmap = new HashMap();
			// if (isuseddatapower)
			// {
			// String[] subjpks =
			// VoucherDataCenter.getInstance().getUsableAccsubjPK(getVoucherVO().getPk_prepared(),
			// getVoucherVO().getPk_corp());
			// if (subjpks != null && subjpks.length > 0)
			// {
			// for (int i = 0; i < subjpks.length; i++)
			// {
			// tmp_hashmap.put(subjpks[i], subjpks[i]);
			// }
			// }
			// }
			// for (int i = 0; i < getVoucherVO().getNumDetails(); i++)
			// {
			// if (getDetail(i).getPk_accsubj() == null ||
			// getDetail(i).getPk_accsubj().trim().equals(""))
			// throw new GlBusinessException("��" + (i + 1) + "����¼��ĿΪ�ա�");
			// if (isuseddatapower)
			// if (tmp_hashmap.get(getDetail(i).getPk_accsubj()) == null)
			// {
			// throw new GlBusinessException("��" + (i + 1) +
			// "����¼�Ŀ�Ŀ��û��Ȩ��ʹ�ã���������Ȩ�ޡ�");
			// }
			// nc.vo.bd.account.AccountVO accvo =
			// VoucherDataCenter.getInstance().getAccsubjByPK(getDetail(i).getPk_accsubj());
			// if (accvo.getCashbankflag().intValue() == 2 ||
			// accvo.getCashbankflag().intValue() == 1 ||
			// accvo.getCashbankflag().intValue() == 3)
			// {
			// setV_Signflag(nc.vo.pub.lang.UFBoolean.TRUE);
			// }
			// if
			// (!VoucherDataCenter.getInstance().getGlparameter().Parameter_isfreevalueallownull.booleanValue())
			// if (accvo.getSubjass() != null && accvo.getSubjass().size() > 0)
			// {
			// if (getDetail(i).getAssid() == null)
			// throw new GlBusinessException("��" + (i + 1) +
			// "����¼�ĸ�������δ¼���ÿ�Ŀ���趨�������㣬�������㲻����Ϊ�ա�");
			// }
			// }
			// if (!m_isNowrited && m_VoucherVO.getPk_voucher() == null)
			// {
			// m_VoucherVO.setNo(Integer.valueOf(0));
			// }
			VoucherPowerCheckUtil.checkVoucherPower(voucher);
			
            UFDate oldPreparedate = voucher.getPrepareddate();
			
			//Ϊ�˴����Ƶ�����ʱ����һ�µ����⣬�����ʱ�����
            UFDate date = new UFDate(
            		new UFDateTime(voucher.getPrepareddate().toStdString()+" "+new UFDateTime().getTime()).getMillis());
            
            voucher.setPrepareddate(date);
            
            DetailVO[] details = voucher.getDetails();
            if(details != null && details.length >0) {
            	for(DetailVO detailVo :details) {
            		if(detailVo.getVerifydate() != null && detailVo.getVerifydate().equals(oldPreparedate.toString())) {
            			detailVo.setPrepareddate(date);
            			detailVo.setVerifydate(date.toString());
            		}
            	}
            }
			
			OperationResultVO[] result = VoucherDataBridge.getInstance().save(voucher, new Boolean(true));
			// setVoucherVO((VoucherVO) result[0].m_userIdentical);
			// if (getListListener() != null)
			// getListListener().afterOperation((VoucherVO)
			// result[0].m_userIdentical, operate);
			// m_issaved = true;
			// if (pk_voucher == null)
			// {
			// if (getVoucherUI().getFunctionName().equals("preparevoucher"))
			// {
			// if
			// (VoucherDataCenter.getInstance().getGlparameter().Parameter_isinstantprint.booleanValue())
			// getVoucherUI().doVoucher("Print");
			// VoucherVO vo = new VoucherVO();
			// vo.setPk_corp(getVoucherVO().getPk_corp());
			// vo.setCorpname(getVoucherVO().getCorpname());
			// vo.setPk_system("GL");
			// vo.setPrepareddate(getVoucherVO().getPrepareddate());
			// vo.setPeriod(getVoucherVO().getPeriod());
			// vo.setNo(Integer.valueOf(0));
			// vo.setModifyflag("YYY");
			// vo.setAttachment(Integer.valueOf(0));
			// vo.setPk_vouchertype(getVoucherVO().getPk_vouchertype());
			// vo.setVouchertypename(VoucherDataCenter.getInstance().getVouchertype(getVoucherVO().getPk_vouchertype()).getName());
			// vo.setTotalcredit(new nc.vo.pub.lang.UFDouble(0));
			// vo.setTotaldebit(new nc.vo.pub.lang.UFDouble(0));
			// vo.setYear(getVoucherVO().getYear());
			// vo.setPk_prepared(this.getClientEnvironment().getUser().getPrimaryKey());
			// vo.setPreparedname(this.getClientEnvironment().getUser().getUser_name());
			// vo.setVoucherkind(Integer.valueOf(0));
			// vo.setDiscardflag(nc.vo.pub.lang.UFBoolean.FALSE);
			// vo.setDetailmodflag(nc.vo.pub.lang.UFBoolean.TRUE);
			// vo.setNo(GLPubProxy.getRemoteVoucher().getCorrectVoucherNo(vo));
			// java.util.Vector vecdetails = new java.util.Vector();
			// vo.setDetail(vecdetails);

			// setVoucherVO(vo);
			// getVoucherUI().setFocus(0, VoucherKey.V_PREPAREDDATE);
			// }
			// else if
			// (getVoucherUI().getFunctionName().equals("voucherbridge"))
			// {
			// next();
			// }
			// }
			return result;
		} catch (GlBusinessException e) {

			tfpanel.showHintMessage(nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100555", "UPP2002100555-000007")/*
																														 * @res
																														 * "����ʧ��"
																														 */);
			throw e;
		} catch (Exception e) {

			tfpanel.showHintMessage(nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100555", "UPP2002100555-000007")/*
																														 * @res
																														 * "����ʧ��"
																														 */);
			Logger.error(e.getMessage(), e);
			if (voucher.getPk_voucher() == null) {
				getMasterModel().setParameter("isNoChanged", new Boolean(false));
				getMasterModel().setParameter("startediting", null);
			}
			throw new GlBusinessException(e.getMessage());
		}
	}

	public void runtimeReconcile(VoucherVO voucher) {
		// TODO Auto-generated method stub
		
		IPubReconcile pubreconcile = (IPubReconcile) NCLocator.getInstance().lookup(IPubReconcile.class.getName());
		PubReconcileInfoVO reconcileinfo = new PubReconcileInfoVO();
		reconcileinfo.setUserid(GlWorkBench.getLoginUser()/*ClientEnvironment.getInstance().getUser().getPrimaryKey()*/);
		reconcileinfo.setReconcileDate(GlWorkBench.getBusiDate()/*ClientEnvironment.getInstance().getBusinessDate()*/);
		reconcileinfo.setPk_glorgbook(voucher.getPk_accountingbook());
		try {
			pubreconcile.pubReconcile(voucher, reconcileinfo);
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			Logger.error(e.getMessage(), e);
		}
		// Object vo;
		// Set<String> matched_d_set = new HashSet<String>();
		// try {
		// Class voclass =
		// Class.forName("nc.vo.gl.pubreconcile.PubReconArithVO");
		// Class matchSubjclass =
		// Class.forName("nc.itf.gl.pubreconcile.IMatchSubj");
		// Class genunitClass =
		// Class.forName("nc.itf.gl.pubreconcile.IGenOtherUnit");
		// Class genOtherVoucherClass =
		// Class.forName("nc.itf.gl.pubreconcile.IGenOtherVoucher");
		// Class dataDisposeClass =
		// Class.forName("nc.ui.gl.reconcilepub.DataDispose");
		// Class reconcileOccurAllVOclass =
		// Class.forName("nc.vo.gl.reconcilecenter.ReconcileOccurAllVO");
		// vo = voclass.newInstance();
		//
		// Method setSelfVoucher = voclass.getMethod("setSelfVoucher", new
		// Class[] { VoucherVO.class });
		// setSelfVoucher.invoke(vo, voucher);
		//
		// Object matchsubj =
		// NCLocator.getInstance().lookup(matchSubjclass.getName());
		// Method isMatchSubj = matchsubj.getClass().getMethod("isMatchSubj",
		// new Class[] { vo.getClass() });
		// vo = isMatchSubj.invoke(matchsubj, vo);
		// Method getIsSubjMatched = voclass.getMethod("getIsSubjMatched",
		// null);
		// Object IsSubjMatched = getIsSubjMatched.invoke(vo, null);
		// if (IsSubjMatched != null && new
		// Boolean(IsSubjMatched.toString()).booleanValue()) {
		// Object genunit =
		// NCLocator.getInstance().lookup(genunitClass.getName());
		// Method isGenOtherUnit = genunitClass.getMethod("isGenOtherUnit", new
		// Class[] { voclass });
		// vo = isGenOtherUnit.invoke(genunit, vo);
		// Method getIsGenOtherUnit = voclass.getMethod("getIsGenOtherUnit",
		// null);
		// Object genOtherUnitresult = getIsGenOtherUnit.invoke(vo, null);
		// if (genOtherUnitresult != null && new
		// Boolean(genOtherUnitresult.toString()).booleanValue()) {
		// Object genvoucher =
		// NCLocator.getInstance().lookup(genOtherVoucherClass.getName());
		// Method isGenOtherVoucher =
		// genOtherVoucherClass.getMethod("isGenOtherVoucher", new Class[] {
		// voclass });
		// vo = isGenOtherVoucher.invoke(genvoucher, vo);
		// Method getOtherVoucher = voclass.getMethod("getOtherVoucher", null);
		// VoucherVO[] othervoucher = (VoucherVO[]) getOtherVoucher.invoke(vo,
		// null);
		// if (othervoucher != null && othervoucher.length > 0) {
		// // ����Эͬ��Ϣ������
		// Class[] cla = new Class[1];
		// cla[0] = voclass;
		// Method getTempSaveVOsByOthersPubreconcile =
		// dataDisposeClass.getMethod("getTempSaveVOsByOthersPubreconcile",
		// cla);
		// Object voall = null;
		// voall =
		// getTempSaveVOsByOthersPubreconcile.invoke(dataDisposeClass.newInstance(),
		// vo);
		// voall =
		// GRPrvProxy.getRemoteIReconcileExtend().getClass().getMethod("saveOtherTemp",
		// reconcileOccurAllVOclass).invoke(GRPrvProxy.getRemoteIReconcileExtend(),
		// voall);
		// for (int i = 0; i < othervoucher.length; i++) {
		// VoucherVO ov = othervoucher[i];
		// Vector<DetailVO> ov_dvec = ov.getDetail();
		// for (Iterator iter = ov_dvec.iterator(); iter.hasNext();) {
		// DetailVO element = (DetailVO) iter.next();
		// if (element.getUserData() instanceof DetailVO) {
		// DetailVO new_name = (DetailVO) element.getUserData();
		// if (!matched_d_set.contains(new_name.getPk_detail())) {
		// matched_d_set.add(new_name.getPk_detail());
		// }
		// }
		// }
		// }
		// for (int i = 0; i < voucher.getDetail().size(); i++) {
		// if (matched_d_set.contains(voucher.getDetail(i).getPk_detail())) {
		// voucher.getDetail(i).setIsmatched(nc.vo.pub.lang.UFBoolean.TRUE);
		// }
		// }
		//
		// }
		// }
		// }
		// } catch (InstantiationException e1) {
		// // TODO Auto-generated catch block
//nc.bs.logging.Logger.error(ex.getMessage(), ex);
		// } catch (IllegalAccessException e1) {
		// // TODO Auto-generated catch block
//nc.bs.logging.Logger.error(ex.getMessage(), ex);
		// } catch (SecurityException e) {
		// // TODO Auto-generated catch block
		// Logger.error(e.getMessage(), e);
		// } catch (NoSuchMethodException e) {
		// // TODO Auto-generated catch block
		// Logger.error(e.getMessage(), e);
		// } catch (IllegalArgumentException e) {
		// // TODO Auto-generated catch block
		// Logger.error(e.getMessage(), e);
		// } catch (InvocationTargetException e) {
		// // TODO Auto-generated catch block
		// Logger.error(e.getMessage(), e);
		// } catch (ClassNotFoundException e) {
		// // TODO Auto-generated catch block
		// Logger.error(e.getMessage(), e);
		// } catch (Exception e) {
		// Logger.error(e.getMessage(), e);
		// }

	}
}
