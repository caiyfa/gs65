package nc.ui.gl.voucher.opmodels;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import javax.swing.JComponent;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.gl.glconst.systemtype.SystemtypeConst;
import nc.itf.gl.pubreconcile.IPubReconcile;
import nc.itf.gl.reconcile.IReconcileExtend;
import nc.ui.gl.datacache.AccountCache;
import nc.ui.gl.datacache.GLParaDataCache;
import nc.ui.gl.eventprocess.VoucherPowerCheckUtil;
import nc.ui.gl.gateway.glworkbench.GlWorkBench;
import nc.ui.gl.voucher.dlg.InputAttachmentDlg;
import nc.ui.gl.voucher.reg.VoucherFunctionRegister;
import nc.ui.gl.voucherdata.VoucherDataBridge;
import nc.ui.gl.vouchermodels.AbstractOperationModel;
import nc.ui.gl.vouchertools.VoucherDataCenter;
import nc.ui.gl.vouchertools.VoucherOperatLogTool;
import nc.ui.glpub.UiManager;
import nc.ui.glrp.vouch.DataFromVoucher;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.ToftPanel;
import nc.ui.pub.beans.MessageDialog;
import nc.vo.gateway60.pub.GlBusinessException;
import nc.vo.gl.pubreconcile.PubReconcileInfoVO;
import nc.vo.gl.pubvoucher.DetailVO;
import nc.vo.gl.pubvoucher.OperationResultVO;
import nc.vo.gl.pubvoucher.VoucherVO;
import nc.vo.glcom.nodecode.GlNodeConst;
import nc.vo.glcom.tools.GLPubProxy;
import nc.vo.glrp.pub.VerifyMsg;
import nc.vo.glrp.verify.VerifyDetailKey;
import nc.vo.glrp.verify.VerifyDetailVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;

import org.apache.commons.lang.StringUtils;



@SuppressWarnings("deprecation")
public class SaveTwoVoucherOperationModel extends AbstractOperationModel {
	/**
	 * 此处插入方法说明。 创建日期：(2002-6-26 14:27:28)
	 * 
	 * @return java.lang.Object
	 */
	public Object doOperation() {

		Boolean isInSum = (Boolean) getMasterModel().getParameter("isInSumMode");
		if (isInSum != null && isInSum.booleanValue()) {
			return null;
		}
		boolean isdap = false;
		String dapsubstr = null;
		getMasterModel().setParameter("stopediting", null);
		showHintMessage(nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100555", "UPP2002100555-000046")/*
																										 * @res "正在保存"
																										 */);

		String funcname = (String) getMasterModel().getParameter("functionname");
		VoucherVO voucher = (VoucherVO) getMasterModel().getParameter("vouchervo");
		//检查是否可以保存（已对账数据不许修改）add by CYF 
		try {
			VoucherVO voucherFromDB = VoucherDataBridge.getInstance().queryByPk(voucher.getPk_voucher());
			if(voucherFromDB!=null){
				for(DetailVO detail:voucherFromDB.getDetails()){
					if(detail.getContrastflag()==1){
						throw new Exception("修改的凭证中包含正在对账的凭证分录。");
					}
				}
			}
		} catch (Exception e) {
			throw new GlBusinessException(e.getMessage());
		}
		//CYF add end 
		if (voucher.getPk_accountingbook() == null) {
			nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage((JComponent) getMasterModel().getUI(), nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("glpub_0", "02002003-0102")/* @res "提示" */, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("glpub_0", "02002003-0193")/* @res "请选择核算账簿。" */);
			return null;
		}
		if (!isEqual(voucher)) {
			if (MessageDialog.showOkCancelDlg(this.getContainer(), NCLangRes.getInstance().getStrByID("20021201", "UPP20021201-000296")/*
					 * @res "是否强制核销"
					 */, VerifyMsg.getMSG_VERIFYDATA_UNLIKENESS()) == MessageDialog.ID_OK) {
			} else {
				return null;
			}
		}
		//
		//
		if (voucher.getUserData() instanceof String[]) {
			//String[] new_name = (String[]) voucher.getUserData();
			String prefix = "dap_save_Action";
			if (voucher.getFree7() != null && voucher.getFree7().startsWith(prefix)) {
				isdap = true;
				dapsubstr = voucher.getFree7().substring(prefix.length());
				// voucher.setFree7(null);
			}
		}
		//

		// reconcile begin
		if (funcname.equals("preparevoucher")) {
			for (int i = 0; i < voucher.getDetails().length; i++) {
				if (voucher.getDetail(i).getUserData() != null && voucher.getDetail(i).getUserData().getClass().toString().equals("class nc.vo.gl.reconcileinit.ModelsetvoucherVO")) {
					int result = MessageDialog.showOkCancelDlg((JComponent) getMasterModel().getUI(), nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100557", "UPP2002100557-000067")/*
																																														 * @res "提示"
																																														 */, nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100557", "UPP2002100557-000069")/*
																																																																			 * @res "凭证已经调用协同,是否按照保存协同进行处理?"
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
		Integer editvoucherno = voucher.getNo() == null ? null : new Integer(voucher.getNo().intValue());
		String pk_voucher = voucher.getPk_voucher();
		if (pk_voucher == null) {
			Boolean isNoChanged = (Boolean) getMasterModel().getParameter("isNoChanged");
			if (!isNoChanged.booleanValue())
				voucher.setNo(new Integer(0));
		}
		String err = "";
		if (funcname.equals("checkvoucher")) {
			if (voucher.getDiscardflag() != null && voucher.getDiscardflag().booleanValue()) {
				err = err + nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000066")/*
																										 * @res "#已作废#"
																										 */;
			}
			if (voucher.getPk_manager() != null) {
				err = err + nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000063")/*
																										 * @res "#已记账#"
																										 */;
			}
		} else {
			if (funcname.equals("offsetvoucher") && voucher.getPk_voucher() != null){
				err = err + nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPT2002100573-900045")/*
																										 * @res "冲销节点不允许修改凭证。"
																										 */;
			}
			if (voucher.getDiscardflag() != null && voucher.getDiscardflag().booleanValue()) {
				err = err + nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000066")/*
																										 * @res "#已作废#"
																										 */;
			}
			if (voucher.getPk_casher() != null) {
				err = err + nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000065")/*
																										 * @res "#已签字#"
																										 */;
			}
			if (voucher.getPk_checked() != null) {
				err = err + nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000064")/*
																										 * @res "#已审核#"
																										 */;
			}
			if (voucher.getPk_manager() != null) {
				err = err + nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000063")/*
																										 * @res "#已记账#"
																										 */;
			}
		}
		// 50
		if ((funcname.equals("preparevoucher") || funcname.equals("voucherbridge")) && VoucherDataCenter.isInputAttachment(voucher.getPk_accountingbook()) && (voucher.getAttachment() == null || voucher.getAttachment().intValue() == 0)) {
			// getMasterModel().doOperation(VoucherFunctionRegister.FUNCTION_LPY_INPUTATTACHMENT);
			// voucher=(VoucherVO) getMasterModel().getParameter("vouchervo");
			InputAttachmentDlg inputattachment = new InputAttachmentDlg();
			inputattachment.showModal();
			voucher.setAttachment(inputattachment.getNumber());

			// showInputAttchmentDialog();
			getMasterModel().setParameter("vouchervo", voucher);

		}

		if (err.trim().length() > 0)
			throw new GlBusinessException(err);
		
		//begin-ncm-yanxf-NC2014051200173-2014年5月30日13:29:56
		/**  yanxf1
		 * 总账参数设置为保存凭证时保存检查主表项，现在我做了一张带有现金科目的凭证保存时检查了现金流的，但是我在把此张凭证的分录修改为非现金银行科目，保存时就不检查现金流了，因原来这张凭证有现金流分析这样不提示就会导致我的现金流报表数据不正确
		 */
//		 非现金凭证，在保存的时候如果做了现金流量信息录入，则需要提示是否继续保存
		 boolean iscashflow = false;
		for (int i = 0; i < voucher.getDetails().length; i++) {
			if (voucher.getDetails()[i].getPk_accasoa() != null
					&& AccountCache.getInstance().getAccountVOByPK(voucher.getPk_accountingbook(), voucher.getDetails()[i].getPk_accasoa(),voucher.getPrepareddate().toStdString()) !=null){
				if(AccountCache.getInstance().getAccountVOByPK(voucher.getPk_accountingbook(), voucher.getDetails()[i].getPk_accasoa(),voucher.getPrepareddate().toStdString()).getCashtype().intValue() > 0){
					iscashflow = true;
					break;
				}
			}
		}

		if (!iscashflow) {
			for (int i = 0; i < voucher.getDetails().length; i++) {
				if (voucher.getDetails()[i].getCashFlow() != null
						&& voucher.getDetails()[i].getCashFlow().length > 0) {
					int r = MessageDialog.showOkCancelDlg(
							(Container) getMasterModel().getUI(), nc.ui.ml.NCLangRes.getInstance().getStrByID("200235","UPP200235-000044")/*@res "提示"*/,
							nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100555", "UPP2002100555-000078")/*
							 * @res
							 * "非现金凭证录入了现金流量,要继续吗?"
							 */);
					if (r == MessageDialog.ID_CANCEL) {
						return null;
					}
					break;
				}
			}
//			for (int i = 0; i < voucher.getDetails().length; i++) {
//				if (voucher.getDetails()[i].getCashFlow() != null
//						&& voucher.getDetails()[i].getCashFlow().length > 0) {
//					voucher.getDetails()[i].setCashFlow(null);
//					voucher.getDetails()[i].setOtheruserdata(null);
//				}
//			}
		}
		//begin-ncm-yanxf-NC2014051200173-2014年5月30日13:29:56

		OperationResultVO[] result = save(voucher);

		boolean errflag = false;
		if (result != null) {
			voucher = (VoucherVO) result[0].m_userIdentical;
			//--add by pangjsh 保存后刷新凭证，由于有后续业务（参照核销），表体要控制是否可编辑
			if(voucher.isHasRefVerify()){
				try {
					VoucherVO newVoucher = GLPubProxy.getRemoteVoucher().queryByPk(voucher.getPk_voucher());
					voucher = (VoucherVO)newVoucher.clone();
				} catch (BusinessException e) {
					throw new GlBusinessException(e.getMessage());
				}
			}
			getMasterModel().setParameter("updatevouchers", new VoucherVO[] {
				voucher
			});
			getMasterModel().setParameter("saveflag", new Boolean(true));

			StringBuffer strMsg = new StringBuffer();
			
			for (int i = 0; i < result.length; i++) {
				switch (result[i].m_intSuccess) {
					case 0:

						showHintMessage(nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100555", "UPP2002100555-000006")/*
																														 * @res "保存成功"
																														 */);
						break;
					case 1:

						strMsg.append("Warning:" + result[i].m_strDescription + "\n");
						showHintMessage(nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100555", "UPP2002100555-000006")/*
																														 * @res "保存成功"
																														 */);

						break;
					case 2:
						errflag = true;

						strMsg.append("Error:" + result[i].m_strDescription + "\n");
						showHintMessage(nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100555", "UPP2002100555-000007")/*
																														 * @res "保存失败"
																														 */);
						// tfpanel.showHintMessage("保存失败");
						break;
					default:
						strMsg.append("Message:" + result[i].m_strDescription + "\n");
				}
			}
			if (!errflag) {
				new VoucherOperatLogTool().InsertLogByVoucher(voucher, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55", "UC001-0000001")/*
																																						 * @res "保存"
																																						 */, GlNodeConst.GLNODE_VOUCHERPREPARE);
			}
			if(!errflag) {
				//如果保存成功则将会计平台信息清理掉
//				voucher.setFipInfo(null);
//				getMasterModel().setParameter("vouchervo",voucher);
				//保存成功信息
				showHintMessage(nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100555", "UPP2002100555-000006")/*
						 * @res "保存成功"
						 */);
			}
			
			if (strMsg.length() > 0)
				nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage((Container) getMasterModel().getUI(), nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000048")/*
																																												 * @res "警告"
																																												 */, strMsg.toString());
		}
		if (editvoucherno != null && editvoucherno.intValue() != voucher.getNo().intValue() && editvoucherno.intValue() != 0) {
			nc.ui.pub.beans.MessageDialog
					.showWarningDlg((Container) getMasterModel().getUI(), nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100555", "UPP2002100555-000047")/*
																																							 * @res "提示"
																																							 */, nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100555", "UPP2002100555-000048"/*
																																										 * @res
																																										 * "已保存凭证的凭证号与输入的凭证号不同,原输入的凭证号为 {0},已保存凭证的凭证号为 "
																																																	 */,null,new String[]{String.valueOf(editvoucherno)}) + voucher.getNo() + " .");

		}
		if ((funcname.equals("preparevoucher") || funcname.equals("voucherbridge")) && VoucherDataCenter.isVoucherSaveRTVerify(voucher.getPk_accountingbook())) {
			// 根据凭证数据取得第一条核销科目的分录序号
			Object ob = getMasterModel().getParameter("selectedindex");
			DetailVO setselectdetail = null;
			try {

				if (ob == null) {

					setselectdetail = new DataFromVoucher().getFirstVerifyDetailVO(voucher, null);

				} else {
					setselectdetail = new DataFromVoucher().getFirstVerifyDetailVO(voucher, new Integer(ob.toString()));

				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			if (setselectdetail == null) {
				// getMasterModel().setParameter("selectedindex", null);
			} else {
				getMasterModel().setParameter("selectedindexes", new int[] {
					setselectdetail.getDetailindex().intValue() - 1
				});
				getMasterModel().setParameter("updatevouchers", new VoucherVO[] {
					voucher
				});
				getMasterModel().doOperation(VoucherFunctionRegister.FUNCTION_REALTIMEVERIFY);
			}

		}

		if (VoucherDataCenter.isPrintAfterSave(voucher.getPk_accountingbook()))
			getMasterModel().doOperation(VoucherFunctionRegister.FUNCTION_PRINTVOUCHER);
		
		//63EHP2折算覆盖
		if (GLParaDataCache.getInstance().isInstantProductVoucher(voucher.getPk_accountingbook()) != null && GLParaDataCache.getInstance().isInstantProductVoucher(voucher.getPk_accountingbook()).booleanValue()) {
			getMasterModel().doOperation(VoucherFunctionRegister.FUNCTION_REALTIMECONVERT);
		}else{
			if (voucher.getConvertflag() != null && voucher.getConvertflag().booleanValue()) {
				int r = MessageDialog.showWarningDlg((Container) getMasterModel().getUI(), "", nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000738")/*
																																											 * @res "修改的凭证已经被折算过，请重新进行折算。"
																																											 */);
				if (r == MessageDialog.ID_YES)
					getMasterModel().doOperation(VoucherFunctionRegister.FUNCTION_REALTIMECONVERT);
			}
		}

		if (funcname != null) {
			if (funcname.equals("voucherbridge")) {
				String islast = (String) getMasterModel().getParameter("station");
				// int
				if (islast == null || islast.equals("single")) {
					// return null;
					// tfpanel.showHintMessage("&**************888");
					// System.out.print(this);
					// nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(tfpanel, "UUUU", "IIIIIIII");
				} else {

					getMasterModel().doOperation(VoucherFunctionRegister.FUNCTION_NEXT);
				}
			} else if (funcname.equals("preparevoucher")) {
				if (pk_voucher == null && voucher.getPk_system() != null && !SystemtypeConst.GL.equalsIgnoreCase(voucher.getPk_system())) {
					// getMasterModel().doOperation(VoucherFunctionRegister.FUNCTION_COPYSMALLVOUCHER);
					
					String islast = (String) getMasterModel().getParameter("station");
					if (islast == null || islast.equals("single")) {
						//nothing
					}else{
						try{
							VoucherVO nextVoucherVo = (VoucherVO) getMasterModel().getParameter("nextvoucher");
							if(nextVoucherVo != null && StringUtils.isEmpty(nextVoucherVo.getPk_voucher())) {
								getMasterModel().setParameter("saveflag", new Boolean(true));
								getMasterModel().doOperation(VoucherFunctionRegister.FUNCTION_NEXT);

							}
						}catch(Exception e) {
							Logger.error(e);
						}
					}
				}
			}
		}
		if (isdap) {
			forUfidaDapUI(voucher, dapsubstr);
		}

		// 通过凭证桥方式保存凭证后，前台界面的处理，通过反射的方式调用接口
		// 接口 IVoucherUISave aftervoucher(voucher vo)
		String modulecode = (String) getMasterModel().getParameter("modulecode");
		if (funcname.equals("voucherbridge") && !modulecode.startsWith("2002")) {

		}

//		showHintMessage(nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100555", "UPP2002100555-000007")/*
//				 * @res "保存失败"
//				 */);
		return null;
	}

	public void runtimeReconcile(VoucherVO voucher) throws BusinessException {
		// TODO Auto-generated method stub
		if (voucher.getErrmessage() != null) {
			return;
		}
		IReconcileExtend reconcilextend = NCLocator.getInstance().lookup(IReconcileExtend.class);
		try {
			boolean b = reconcilextend.checkCanReconByVoucherPK(voucher.getPk_voucher(), voucher.getPk_accountingbook());
			if (!b) {
				return;
			}
		} catch (BusinessException e1) {
			// TODO Auto-generated catch block

		}

		IPubReconcile pubreconcile = (IPubReconcile) NCLocator.getInstance().lookup(IPubReconcile.class.getName());
		PubReconcileInfoVO reconcileinfo = new PubReconcileInfoVO();
		reconcileinfo.setUserid(GlWorkBench.getLoginUser()/* ClientEnvironment.getInstance().getUser().getPrimaryKey() */);
		reconcileinfo.setReconcileDate(GlWorkBench.getBusiDate()/* ClientEnvironment.getInstance().getBusinessDate() */);
		reconcileinfo.setPk_glorgbook(voucher.getPk_accountingbook());
		try {
			pubreconcile.pubReconcile(voucher, reconcileinfo);
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}

	/**
	 * 此处插入方法说明。 创建日期：(2003-10-1 19:14:33)
	 */
	private OperationResultVO[] save(VoucherVO voucher) {
		Component jc = (Component) getMasterModel().getUI();
		while (!(jc instanceof ToftPanel) && jc != null) {
			jc = (Component) jc.getParent();
		}
		ToftPanel tfpanel = (ToftPanel) jc;
		try {
			String userID = GlWorkBench.getLoginUser();
			Integer attachment = voucher.getAttachment();
			if (voucher.getPk_prepared() == null) {
				voucher.setPk_prepared(userID);
				voucher.setCreator(userID);
			}
			if (VoucherDataCenter.isVoucherSelfEditDelete(voucher.getPk_accountingbook()) && !userID.equals(voucher.getPk_prepared())) {
				throw new GlBusinessException(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000080")/*
																															 * @res "凭证只能由本人进行修改！"
																															 */);
			}
			
//			UFDate oldPreparedate = voucher.getPrepareddate();

			//为了处理制单日期时分秒一致的问题，用于帐表排序
//            UFDate date = new UFDate(
//            		new UFDateTime(voucher.getPrepareddate().toLocalString()+new UFDateTime().toLocalString().substring(10)).getMillis());
//            
//            voucher.setPrepareddate(date);
//            
//            DetailVO[] details = voucher.getDetails();
//            if(details != null && details.length >0) {
//            	for(DetailVO detailVo :details) {
//            		if(detailVo.getVerifydate() != null && detailVo.getVerifydate().equals(oldPreparedate.toString())) {
//            			detailVo.setPrepareddate(date);
//            			detailVo.setVerifydate(date.toString());
//            		}
//            	}
//            }

			// voucher.clearEmptyDetail();
			voucher.clearDetail();
			voucher.setExplanation(voucher.getNumDetails() > 0 ? voucher.getDetail(0).getExplanation() : null);
			String operate;
			String pk_voucher = voucher.getPk_voucher();
			operate = "mod";

			if (voucher.getPk_voucher() != null)
				voucher.setPk_prepared(userID);
			else {
				if (!(SystemtypeConst.RECONCILEVOUCHER.equals(voucher.getPk_system()) 
						|| SystemtypeConst.GL.equals(voucher.getPk_system()) 
						|| SystemtypeConst.ORDINARY_TRANSFER.equals(voucher.getPk_system()) 
						|| SystemtypeConst.EXCHANGE_GAINS_AND_LOSSES.equals(voucher.getPk_system())  
						|| SystemtypeConst.PROFIT_AND_LOSS_CARRIED_FORWARD.equals(voucher.getPk_system())
						|| SystemtypeConst.RECLASSIFY.equals(voucher.getPk_system())
						|| voucher.getFipInfo() != null))
					voucher.setPk_prepared(userID);
			}

			HashMap tmp_hashmap = new HashMap();
			VoucherPowerCheckUtil.checkVoucherPower(voucher);
			// 暂存凭证保存时，自动保存为正常凭证 hurh
			voucher.setTempsaveflag(UFBoolean.FALSE);
			for (DetailVO detail : voucher.getDetails()) {
				detail.setTempsaveflag(UFBoolean.FALSE);
			}
			OperationResultVO[] result = VoucherDataBridge.getInstance().save(voucher, new Boolean(true));
			
			return result;
		} catch (GlBusinessException e) {

			tfpanel.showHintMessage(nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100555", "UPP2002100555-000007")/*
																													 * @res "保存失败"
																													 */);
			throw e;
		} catch (Exception e) {

			tfpanel.showHintMessage(nc.ui.ml.NCLangRes.getInstance().getStrByID("2002100555", "UPP2002100555-000007")/*
																													 * @res "保存失败"
																													 */);
			Logger.error(e.getMessage(), e);

			if (voucher.getPk_voucher() == null) {
				getMasterModel().setParameter("isNoChanged", new Boolean(false));
				// getMasterModel().setParameter("startediting", null);
			}
			throw new GlBusinessException(e.getMessage(), e);
		}
	}

	private void forUfidaDapUI(VoucherVO voucher, String dapsubstr) {
		if (dapsubstr != null) {
			String substr = dapsubstr;
			String dapclass = "nc.ui.dap.voucher.DapMessageCenter";
			Class dc;
			try {
				dc = Class.forName(dapclass);
				Object o = dc.newInstance();
				Method seme = dc.getMethod("sendMessage", new Class[] {
						String.class, Object.class
				});
				seme.invoke(o, new Object[] {
						substr, null
				});
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				nc.bs.logging.Logger.error(e.getMessage(), e);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				nc.bs.logging.Logger.error(e.getMessage(), e);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				nc.bs.logging.Logger.error(e.getMessage(), e);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				nc.bs.logging.Logger.error(e.getMessage(), e);
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				nc.bs.logging.Logger.error(e.getMessage(), e);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				nc.bs.logging.Logger.error(e.getMessage(), e);
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				nc.bs.logging.Logger.error(e.getMessage(), e);
			}
		}
	}

	private void showHintMessage(String message) {

		Container tfpanel = getContainer();
		try {
			if (tfpanel instanceof UiManager) {
				((UiManager) tfpanel).showHintMessage(message);

			} else if (Class.forName("nc.ui.cmp.UiManagerCMP").isInstance(tfpanel)) {
				((ToftPanel) tfpanel).showHintMessage(message);

			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			Logger.error(e);
		}
	}

	private Container getContainer() {
		Component jc = (Component) getMasterModel().getUI();
		Component tfpanel = null;

		try {
			while (!(jc instanceof UiManager)) {
				jc = (Component) jc.getParent();
			}
		} catch (NullPointerException e1) {
			Class t;
			try {
				Component jc1 = (Component) getMasterModel().getUI();
				t = Class.forName("nc.ui.cmp.UiManagerCMP");
				while (!(t.isInstance(jc1))) {
					jc1 = (Component) jc1.getParent();
				}
				tfpanel = (ToftPanel) jc1;
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				nc.bs.logging.Logger.error(e.getMessage(), e);
			}

		}
		if (jc == null) {

		} else {
			tfpanel = (UiManager) jc;
		}
		return (Container) tfpanel;
	}
	

	/**
	 * 功能：判断核销金额与分录金额是否相等 
	 * 
	 * @param voucher
	 * @return boolean
	 */
	private boolean isEqual(VoucherVO voucher) {
		for (int i = 0; i < voucher.getDetail().size(); i++) {
			DetailVO  detail = (DetailVO)voucher.getDetail().get(i);
			if(detail.getM_voCreditDetails()==null&&detail.getM_voDeditDetails()==null)
				continue;
			else{
				//只判断原币就可以，不会跨币种核销
				if(detail.getDebitamount()!=null&&detail.getDebitamount().compareTo(UFDouble.ZERO_DBL)!=0){
					VerifyDetailVO[] verDetailCredits = detail.getM_voCreditDetails();
					UFDouble creditSum_Y = calculateSum(verDetailCredits,VerifyDetailKey.CREDIT_JS_Y);
					if(detail.getDebitamount().compareTo(creditSum_Y)!=0)
						return false;
				} else{
					VerifyDetailVO[] verDetailDebits = detail.getM_voDeditDetails();
					UFDouble debitSum_Y = calculateSum(verDetailDebits,VerifyDetailKey.DEBIT_JS_Y);
					if(detail.getCreditamount().compareTo(debitSum_Y)!=0)
						return false;
				}
			}
			
			
		}
		return true;
	}
	
	/** 
	 * @Title: calculateSum 
	 * @Description: TODO(计算选中数据的合计) 
	 * @param @param voDetail
	 * @param @param iKey
	 * @param @return    
	 * @return UFDouble    
	 * @throws 
	 */
	private UFDouble calculateSum(VerifyDetailVO[] voDetail,int iKey) {
		UFDouble ufResult = UFDouble.ZERO_DBL;
		if (voDetail == null) {
			return VerifyMsg.ZERO;
		}
		Object oValue = null;
		for (int i = 0; i < voDetail.length; i++) {
			if (!voDetail[i].isSelected().booleanValue()) {
				continue;
			}
			try {
				oValue = voDetail[i].getValue(iKey);
				if (oValue != null) {
					ufResult = ufResult.add((UFDouble) oValue);
				}
			} catch (Exception e) {
				return VerifyMsg.ZERO;
			}
		}
		return ufResult;
	}
}
