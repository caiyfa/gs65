package nc.ui.gl.pubvoucher;

import java.awt.Container;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.UIManager;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.desktop.ui.WorkbenchEnvironment;
import nc.itf.gl.pub.ITransferHistoryPrv;
import nc.itf.org.IOrgMetaDataIDConst;
import nc.pubitf.accperiod.AccountCalendar;
import nc.pubitf.bd.accessor.GeneralAccessorFactory;
import nc.pubitf.bd.accessor.IGeneralAccessor;
import nc.ui.gl.cachefeed.CacheRequestFactory;
import nc.ui.gl.datacache.AccountCache;
import nc.ui.gl.datacache.CurrencyDataCache;
import nc.ui.gl.datacache.FreeValueDataCache;
import nc.ui.gl.datacache.GLParaDataCache;
import nc.ui.gl.datacache.SystemUserDataCache;
import nc.ui.gl.eventprocess.VoucherPowerCheckUtil;
import nc.ui.gl.gateway.glworkbench.GlWorkBench;
import nc.ui.gl.remotecall.GlRemoteCallProxy;
import nc.ui.gl.voucher.IPara;
import nc.ui.gl.voucher.VoucherChangeEvent;
import nc.ui.gl.voucher.VoucherChangeListener;
import nc.ui.gl.voucher.VoucherChangeSupport;
import nc.ui.gl.voucherdata.VoucherDataBridge;
import nc.ui.gl.voucherlist.IListModel;
import nc.ui.gl.voucherlist.IListUI;
import nc.ui.gl.vouchertools.VoucherDataCenter;
import nc.ui.gl.vouchertools.VoucherOperatLogTool;
import nc.ui.glcom.displayformattool.ShowContentCenter;
import nc.ui.glcom.numbertool.GlCurrAmountFormat;
import nc.ui.glcom.numbertool.GlNumberFormat;
import nc.ui.glpub.IPeerListener;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.MessageDialog;
import nc.vo.bd.accessor.IBDData;
import nc.vo.gateway60.accountbook.AccountBookUtil;
import nc.vo.gateway60.itfs.CalendarUtilGL;
import nc.vo.gateway60.pub.GlBusinessException;
import nc.vo.gateway60.pub.VoucherTypeDataCache;
import nc.vo.gl.pubvoucher.DetailVO;
import nc.vo.gl.pubvoucher.OperationResultVO;
import nc.vo.gl.pubvoucher.VoucherKey;
import nc.vo.gl.pubvoucher.VoucherListPrintVO;
import nc.vo.gl.pubvoucher.VoucherPrintVO;
import nc.vo.gl.pubvoucher.VoucherVO;
import nc.vo.gl.voucherlist.VoucherIndexKey;
import nc.vo.gl.voucherlist.VoucherIndexVO;
import nc.vo.gl.voucherquery.VoucherQueryConditionVO;
import nc.vo.gl.vouchertools.QueryElementVO;
import nc.vo.gl.vouchertools.XML_VoucherTranslator;
import nc.vo.glcom.constant.IGlDataPowerConst;
import nc.vo.glcom.nodecode.GlNodeConst;
import nc.vo.glcom.tools.GLPubProxy;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDouble;
import nc.vo.sm.UserVO;

import org.apache.commons.lang.StringUtils;
import org.apache.xerces.dom.DocumentImpl;

/**
 * 此处插入类型说明。 创建日期：(01-8-21 12:01:20)
 *
 * @author：王建华
 */

public class ListModel extends nc.ui.gl.vouchermodels.AbstractMasterModel implements IPeerListener, IListModel {
	// 总账参数缓存
	// private Hashtable m_glparametercache;

	// private Hashtable m_dapsystem;

	// private HashMap m_currencycache;

	private int[] m_CurrentIndex; 

	private IPara m_para;

	private IListUI m_listUI;
	
	// hurh 帐表联查凭证使用，减少连接数
	private String funcCode; // 联查前一个界面的句柄

	private VoucherChangeSupport voucherlistener = new VoucherChangeSupport(this);

	public Vector m_VoucherLists = new Vector();

	protected transient java.beans.PropertyChangeSupport propertyChange = new java.beans.PropertyChangeSupport(this);

	private VoucherQueryConditionVO[] conditionVO;

	private GlCurrAmountFormat numberformat = new GlCurrAmountFormat();

	private HashMap m_voucherpkcache = new HashMap();

	private int m_printsubjlevel = 0;

	private int m_printasslevel = -1;

	private HashMap<String, String> m_tipmap = new HashMap<String, String>();

	private HashMap<String, VoucherVO> m_vomap = new HashMap<String, VoucherVO>();

	private java.awt.Component m_defaultUI;

	private Boolean m_istip = false;

	/**
	 * TableModel 构造子注解。
	 */
	public ListModel() {
		super();
	}

	/**
	 * TableModel 构造子注解。
	 */
	public ListModel(IPara para) {
		super();
		setPara(para);

	}

	public String[] abandonCurrent(String flag) throws Exception {
		int[] indexs = getCurrentIndexs();
		if(null == indexs){
			getListUI().showWarningMessage(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("glpub_0","02002003-0166")/*@res "没有可作废的凭证"*/);
			return null;
		}
		String userid = GlWorkBench.getLoginUser();
		String username = GlWorkBench.getLoginUserName();
		String[] pks = null;
		Vector vecpks = new Vector();
		HashMap<String, VoucherIndexVO> key_hm=new HashMap<String, VoucherIndexVO>();
		Vector<String> vecpkspower = new Vector<String>();
		if (flag.equals(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000014")/*
																										 * @res
																										 * "作废"
																										 */)) {
			for (int i = 0; i < indexs.length; i++) {
				VoucherIndexVO tempVO = (VoucherIndexVO) m_VoucherLists.elementAt(indexs[i]);
				key_hm.put(tempVO.getPk_voucher(), tempVO);
				Boolean ispower = new Boolean(false);
				VoucherVO vo = VoucherDataBridge.getInstance().queryByPk(tempVO.getPk_voucher());
				VoucherPowerCheckUtil.checkVoucher(vo, IGlDataPowerConst.OPERATE_ABANDON);
				//
				ispower = VoucherDataBridge.getInstance().isAccsubjPower(vo, GlWorkBench.getLoginUser());
				if (ispower.booleanValue()) {
					vecpkspower.addElement(tempVO.getPk_voucher());
				}
				if ((!ispower.booleanValue()) && ((VoucherDataCenter.isVoucherSelfEditDelete(tempVO.getPk_glorgbook()) && tempVO.getPk_prepared().equals(userid)) || !VoucherDataCenter.isVoucherSelfEditDelete(tempVO.getPk_glorgbook())) && tempVO.getPk_casher() == null && tempVO.getPk_checked() == null && tempVO.getPk_manager() == null && tempVO.getErrmessage() == null
						&& (tempVO.getIsmatched() == null || !tempVO.getIsmatched().booleanValue()) && !tempVO.getDiscardflag().booleanValue())
					vecpks.addElement(tempVO.getPk_voucher());
			}
			pks = new String[vecpks.size()];
			vecpks.copyInto(pks);
			pks = appendWriteoffPks(pks);
			OperationResultVO[] result = GLPubProxy.getRemoteVoucherList().abandonVoucherByPks(pks, userid, new Boolean(true));
			Vector tmp_successPKs = new Vector();
			Vector falseresult = new Vector();
			if (result != null && result.length > 0) {
				HashMap tmp_falsePKs = new HashMap();
				for (int i = 0; i < result.length; i++) {
					if (result[i] != null && result[i].m_intSuccess != 0) {
						falseresult.addElement(result[i]);
						tmp_falsePKs.put(result[i].m_strPK, result[i]);
						key_hm.remove(result[i].m_strPK);
					}
				}
				// for (int i = 0; i < pks.length; i++) {
				// if (tmp_falsePKs.get(pks[i]) == null) {
				// tmp_successPKs.addElement(pks[i]);
				// }
				// }
				// getListUI().nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,result.m_userIdentical.toString());
				// refresh();
			}
			// pks = new String[tmp_successPKs.size()];
			// tmp_successPKs.copyInto(pks);
			if (falseresult.size() > 0) {
				OperationResultVO[] tmp_falseresultvos = new OperationResultVO[falseresult.size()];
				falseresult.copyInto(tmp_falseresultvos);
				for (int i = 0; i < tmp_falseresultvos.length; i++) {
					if (tmp_falseresultvos[i].m_userIdentical == null) {
						tmp_falseresultvos[i].m_userIdentical = getVoucherIndexVO(tmp_falseresultvos[i].m_strPK);
					}
				}
				getListUI().showResultMessage(tmp_falseresultvos);
			}
			// if (result != null && result.m_intSuccess == 1)
			// {
			// getListUI().nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,result.m_userIdentical.toString());
			// refresh();
			// }

		} else {
			for (int i = 0; i < indexs.length; i++) {
				VoucherIndexVO tempVO = (VoucherIndexVO) m_VoucherLists.elementAt(indexs[i]);
				key_hm.put(tempVO.getPk_voucher(), tempVO);
				if (tempVO.getPk_prepared().equals(userid)
						&& tempVO.getPk_casher() == null
						&& tempVO.getPk_checked() == null
						&& tempVO.getPk_manager() == null
						&& tempVO.getErrmessage() == null
						&& tempVO.getDiscardflag().booleanValue()
						&& (tempVO.getVoucherVO().getVoucherkind().intValue() == 1 || VoucherDataCenter.getGLSettlePeriod(tempVO.getPk_glorgbook()) == null || (VoucherDataCenter.getGLSettlePeriod(tempVO.getPk_glorgbook()) != null && (tempVO.getYear().compareTo(VoucherDataCenter.getGLSettlePeriod(tempVO.getPk_glorgbook())[0]) > 0 || (tempVO.getYear().compareTo(
								VoucherDataCenter.getGLSettlePeriod(tempVO.getPk_glorgbook())[0]) == 0 && tempVO.getPeriod().compareTo(VoucherDataCenter.getGLSettlePeriod(tempVO.getPk_glorgbook())[1]) > 0)))))
					vecpks.addElement(tempVO.getPk_voucher());
				VoucherVO vo = VoucherDataBridge.getInstance().queryByPk(tempVO.getPk_voucher());
				VoucherPowerCheckUtil.checkVoucher(vo, IGlDataPowerConst.OPERATE_UNABANDON);
			}
			pks = new String[vecpks.size()];
			vecpks.copyInto(pks);
			pks = appendWriteoffPks(pks);
			OperationResultVO[] result = GLPubProxy.getRemoteVoucherList().abandonVoucherByPks(pks, userid, new Boolean(false));
			Vector tmp_successPKs = new Vector();
			Vector falseresult = new Vector();
			if (result != null && result.length > 0) {
				HashMap tmp_falsePKs = new HashMap();
				for (int i = 0; i < result.length; i++) {
					if (result[i] != null && result[i].m_intSuccess != 0) {
						falseresult.addElement(result[i]);
						tmp_falsePKs.put(result[i].m_strPK, result[i]);
						key_hm.remove(result[i].m_strPK);
					}
				}
				// for (int i = 0; i < pks.length; i++) {
				// if (tmp_falsePKs.get(pks[i]) == null) {
				// tmp_successPKs.addElement(pks[i]);
				// }
				// }
				// getListUI().nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,result.m_userIdentical.toString());
				// refresh();
			}
			// pks = new String[tmp_successPKs.size()];
			// tmp_successPKs.copyInto(pks);
			if (falseresult.size() > 0) {
				OperationResultVO[] tmp_falseresultvos = new OperationResultVO[falseresult.size()];
				falseresult.copyInto(tmp_falseresultvos);
				for (int i = 0; i < tmp_falseresultvos.length; i++) {
					if (tmp_falseresultvos[i].m_userIdentical == null) {
						tmp_falseresultvos[i].m_userIdentical = getVoucherIndexVO(tmp_falseresultvos[i].m_strPK);
					}
				}
				getListUI().showResultMessage(tmp_falseresultvos);
			}
		}
		updateVoucherList(pks, UFBoolean.valueOf(flag.equals(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000014")/*
																																		 * @res
																																		 * "作废"
																																		 */)), VoucherIndexKey.V_DISCARDFLAG);
		if (flag.equals(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000014")/*
																										 * @res
																										 * "作废"
																										 */)) {
			updateVoucherList(pks, userid, VoucherIndexKey.V_PK_PREPARED);
			updateVoucherList(pks, username, VoucherIndexKey.V_PREPAREDNAME);
		}

		firePropertyChange("refresh", null, null);

		new VoucherOperatLogTool().InsertLogByVoucherIndexVOs(key_hm.values().toArray(new VoucherIndexVO[0]), flag, GlNodeConst.GLNODE_VOUCHERPREPARE);

		return vecpkspower.size() == 0 ? null : vecpkspower.toArray(new String[vecpkspower.size()]);
		// fireVoucherChange(
		// VoucherKey.P_VOUCHER,
		// null,
		// ((VoucherIndexVO) getCurrentIndexVO()).getVoucherVO());
	}
	
	/**
	 * 根据凭证pk
	 * @param pks
	 * @return
	 * @throws BusinessException
	 */
	private String[] appendWriteoffPks(String[] pks) throws BusinessException {
		if(pks == null || pks.length ==0)
			return pks;
		Set<String> pkSet = new HashSet<String>();
		pkSet.addAll(Arrays.asList(pks));
		String[] offPks = NCLocator.getInstance().lookup(ITransferHistoryPrv.class).queryWriteoffVouchers(pks);
		if(offPks != null && offPks.length >0) {
			pkSet.addAll(Arrays.asList(offPks));
		}
		return pkSet.toArray(new String[0]);
	}

	public void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
		propertyChange.addPropertyChangeListener(listener);
	}

	public void addQueryByVO(nc.vo.gl.voucherquery.VoucherQueryConditionVO vo) {
		try {



			if (conditionVO != null && conditionVO.length != 0) {
				Vector vecT = new Vector();
				for (int i = 0; i < conditionVO.length; i++) {
					vecT.addElement(conditionVO[i]);
				}
				vecT.addElement(vo);
				conditionVO = new VoucherQueryConditionVO[vecT.size()];
				vecT.copyInto(conditionVO);
			} else
				conditionVO = new nc.vo.gl.voucherquery.VoucherQueryConditionVO[] { vo };
			nc.vo.gl.pubvoucher.VoucherVO[] vouchers = VoucherDataBridge.getInstance().queryByConditionVO(new nc.vo.gl.voucherquery.VoucherQueryConditionVO[] { vo }, new Boolean(false));
			if (vouchers == null)
				vouchers = new VoucherVO[0];
			VoucherIndexVO[] resultVO = new nc.vo.gl.voucherlist.VoucherIndexVO[vouchers.length];
			for (int i = 0; i < vouchers.length; i++) {
				VoucherIndexVO tempvo = new VoucherIndexVO();
				tempvo.setVoucherVO(vouchers[i]);
				resultVO[i] = tempvo;
			}
			int beginNum = m_VoucherLists.size();
			for (int i = 0; i < resultVO.length; i++) {
				addVoucher(resultVO[i]);
			}
			int endNum = m_VoucherLists.size();
			firePropertyChange("message", null, nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000483"/*
																																 * @res
																																 * "本次查询共查出符合条件的凭证{0}张，其中{1}张在原列表已存在，已被合并。列表中现有凭证{2}张。"
																											 */,null,new String[]{String.valueOf(vouchers.length),String.valueOf(vouchers.length - (endNum - beginNum)),String.valueOf(endNum)}));
			firePropertyChange("refresh", null, null);
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			throw new nc.vo.gateway60.pub.GlBusinessException(e.getMessage());
		}
		if (m_VoucherLists.size() > 0)
			setCurrentIndex(new int[] { 0 }, true);
		else
			setCurrentIndex(null, true);
	}

	/**
	 * addVO 方法注解。
	 */
	public void addVO(java.lang.Object objValue) throws java.lang.Exception {
		if (objValue.getClass().getName().equals("nc.vo.gl.pubvoucher.VoucherVO"))
			addVoucher((VoucherVO) objValue);
		else if (objValue.getClass().getName().equals("VoucherIndexVO"))
			addVoucher((VoucherIndexVO) objValue);
		else {
			try {
				VoucherVO[] vos = (VoucherVO[]) objValue;
				addVouchers(vos);
			} catch (Exception e) {
				throw e;
			}
		}

	}

	/**
	 * 此处插入方法说明。 创建日期：(01-10-15 16:11:07)
	 */
	public void addVoucher(VoucherVO voucher) throws Exception {
		addVoucher(new VoucherIndexVO(voucher));
	}

	protected void addVoucher(VoucherIndexVO tempVO) {
		if (tempVO.getPk_voucher() != null) {
			if (m_voucherpkcache.get(tempVO.getPk_voucher()) != null)
				return;
			m_voucherpkcache.put(tempVO.getPk_voucher(), "0000");
			m_VoucherLists.addElement(tempVO);
		} else {
			m_VoucherLists.addElement(tempVO);
		}
		firePropertyChange("AddVoucherIndexVO", null, tempVO);
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-9-8 10:09:36)
	 *
	 * @param listener
	 *            java.beans.PropertyChangeListener
	 */
	public synchronized void addVoucherChangeListener(VoucherChangeListener listener) {
		this.voucherlistener.addVoucherChangeListener(listener);
	}

	/**
	 * 此处插入方法说明。 创建日期：(01-10-15 16:11:07)
	 */
	public void addVouchers(VoucherVO[] vouchers) throws Exception {
		for (int i = 0; i < vouchers.length; i++) {
			addVoucher(vouchers[i]);
		}
		firePropertyChange("refresh", null, null);
	}

	public void afterOperation(Object VO, Object objUserData) throws Exception {

		String strFlag = objUserData.toString();
		if (strFlag.equals("add"))
			addVO(VO);
		else if (strFlag.equals("mod")) {
			modifyVO(VO);
		} else if (strFlag.equals("del")) {
			removeVO(VO);
		} else if (strFlag.equals("remove")) {
			VoucherVO[] voucher = (VoucherVO[]) VO;
			if (voucher == null)
				return;
			String[] pks = new String[voucher.length];
			for (int i = 0; i < voucher.length; i++) {
				pks[i] = voucher[i].getPk_voucher();
			}
			removeVOByPks(pks);
		}
		return;
	}

	public void beforeOperation(Object VO, Object objUserData) {
		nc.bs.logging.Logger.debug("beforeAdd");
		return;
	}

	public void checkCurrent(String flag) throws Exception {
		int[] indexs = getCurrentIndexs();
		String userid = GlWorkBench.getLoginUser();
		String username = GlWorkBench.getLoginUserName();
		String[] pks = null;
		Vector vecpks = new Vector();
		Vector falseresult = new Vector();
		if (flag.equals(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000021")/*
																										 * @res
																										 * "审核"
																										 */)) {
			for (int i = 0; i < indexs.length; i++) {
				VoucherIndexVO tempVO = (VoucherIndexVO) m_VoucherLists.elementAt(indexs[i]);
				if ((VoucherDataCenter.isRequireCasherSigned(tempVO.getPk_glorgbook()) && tempVO.getSignflag() != null && tempVO.getSignflag().booleanValue() && tempVO.getPk_casher() != null) || tempVO.getSignflag() == null || !tempVO.getSignflag().booleanValue() || !(VoucherDataCenter.isRequireCasherSigned(tempVO.getPk_glorgbook())) && tempVO.getPk_checked() == null
						&& tempVO.getPk_manager() == null && !tempVO.getDiscardflag().booleanValue() && !tempVO.getPk_prepared().equals(userid))
					vecpks.addElement(tempVO.getPk_voucher());
			}
			pks = new String[vecpks.size()];
			vecpks.copyInto(pks);
			OperationResultVO[] result = GLPubProxy.getRemoteVoucherAudit().checkVoucherByPks(pks, userid, new Boolean(true));
			Vector tmp_successPKs = new Vector();
			if (result != null && result.length > 0) {
				HashMap tmp_falsePKs = new HashMap();
				for (int i = 0; i < result.length; i++) {
					if (result[i] != null && result[i].m_intSuccess == 2) {
						falseresult.addElement(result[i]);
						tmp_falsePKs.put(result[i].m_strPK, result[i]);
					}
				}
				for (int i = 0; i < pks.length; i++) {
					if (tmp_falsePKs.get(pks[i]) == null) {
						tmp_successPKs.addElement(pks[i]);
					}
				}
				// getListUI().nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,result.m_userIdentical.toString());
				// refresh();
			}
			if (tmp_successPKs.size() > 0) {
				pks = new String[tmp_successPKs.size()];
				tmp_successPKs.copyInto(pks);
			}
			if (falseresult.size() > 0) {
				OperationResultVO[] tmp_falseresultvos = new OperationResultVO[falseresult.size()];
				falseresult.copyInto(tmp_falseresultvos);
				for (int i = 0; i < tmp_falseresultvos.length; i++) {
					if (tmp_falseresultvos[i].m_userIdentical == null) {
						tmp_falseresultvos[i].m_userIdentical = getVoucherIndexVO(tmp_falseresultvos[i].m_strPK);
					}
				}
				getListUI().showResultMessage(tmp_falseresultvos);
			}
		} else {
			for (int i = 0; i < indexs.length; i++) {
				VoucherIndexVO tempVO = (VoucherIndexVO) m_VoucherLists.elementAt(indexs[i]);
				if (tempVO.getPk_checked() != null && tempVO.getPk_checked().equals(userid) && tempVO.getPk_manager() == null && tempVO.getErrmessage() == null && !tempVO.getDiscardflag().booleanValue())
					vecpks.addElement(tempVO.getPk_voucher());
			}
			pks = new String[vecpks.size()];
			vecpks.copyInto(pks);
			GLPubProxy.getRemoteVoucherAudit().checkVoucherByPks(pks, userid, new Boolean(false));
		}
		updateVoucherList(pks, flag.equals(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000021")/*
																														 * @res
																														 * "审核"
																														 */) ? userid : null, VoucherIndexKey.V_PK_CHECKED);
		updateVoucherList(pks, flag.equals(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000021")/*
																														 * @res
																														 * "审核"
																														 */) ? username : null, VoucherIndexKey.V_CHECKEDNAME);
		firePropertyChange("refresh", null, null);
		// fireVoucherChange(
		// VoucherKey.P_VOUCHER,
		// null,
		// ((VoucherIndexVO) getCurrentIndexVO()).getVoucherVO());
	}

	public String[] delCurrent() throws Exception {
		OperationResultVO[] result = null;

		int[] indexs = getCurrentIndexs();
		if(indexs == null || indexs.length <= 0){
			return null;
		}
		
		Vector vecpks = new Vector();
		Vector vecVos = new Vector();
		Vector<String> vecpkspower = new Vector<String>();
		String[] pks = null;
		HashMap<String, VoucherIndexVO> key_hm=new HashMap<String, VoucherIndexVO>();
		
		// hurh 性能优化
		String[] pk_vouchers = new String[indexs.length];
		VoucherIndexVO tempVO = null;
		for (int i = 0; i < indexs.length; i++) {
			tempVO = (VoucherIndexVO) m_VoucherLists.elementAt(indexs[i]);
			pk_vouchers[i] = tempVO.getPk_voucher();
		}
		
		VoucherVO[] vouchers = VoucherDataBridge.getInstance().queryByPks(pk_vouchers);
		
		//检测是否可以删除Add by CYF
		for(VoucherVO vo:vouchers){
			for(DetailVO detail:vo.getDetails()){
				if(detail.getContrastflag()==1){
					throw new Exception("删除的凭证中包含正在对账的凭证分录。");
				}
			}
		}
		//add end 
		HashMap<String, VoucherVO> voucherMap = new HashMap<String, VoucherVO>();
		if(vouchers != null){
			for(VoucherVO vo : vouchers){
				voucherMap.put(vo.getPk_voucher(), vo);
			}
		}

		for (int i = 0; i < indexs.length; i++) {
			tempVO = (VoucherIndexVO) m_VoucherLists.elementAt(indexs[i]);
			VoucherVO vo = voucherMap.get(tempVO.getPk_voucher());

			  //vo=VoucherDataBridge.getInstance().queryByPk(tempVO.getPk_voucher());



			Boolean ispower = new Boolean(false);
			// getVoucherPanel().setVO(vo);
			//TODO 60这个方法没用
			ispower = VoucherDataBridge.getInstance().isAccsubjPower(vo, GlWorkBench.getLoginUser());
			if (ispower.booleanValue()) {
				vecpkspower.addElement(tempVO.getPk_voucher());
			}
			if ((tempVO.getIsmatched() == null || !tempVO.getIsmatched().booleanValue()) && !ispower.booleanValue()){
				vecpks.addElement(tempVO.getPk_voucher());
				key_hm.put(tempVO.getPk_voucher(), tempVO);
				vecVos.addElement(vo);
			}
		}
		pks = new String[vecpks.size()];
		vecpks.copyInto(pks);
		vouchers = new VoucherVO[vecVos.size()];
		vecVos.copyInto(vouchers);
		VoucherPowerCheckUtil.checkVoucherDelPower(vouchers);
		pks = appendWriteoffPks(pks);
		result = VoucherDataBridge.getInstance().deleteByPks(pks, GlWorkBench.getLoginUser());
		
		// hurh
		List<String> removeList = new LinkedList<String>();
		removeList.addAll(Arrays.asList(pks));
		
		if(result != null && result.length > 0){
			for(OperationResultVO r : result){
				if(r.m_strPK != null){
					r.m_userIdentical = voucherMap.get(r.m_strPK);
					removeList.remove(r.m_strPK);
				}
			}
			getListUI().showResultMessage(result);
		}
		//updateSrcVoucherDifFlag(pks);//add by Liyongru for V55 at 2008-06-17
		
		//removeVOByPks(pks);
		removeVOByPks(removeList.toArray(new String[0]));
		setCurrentIndex(null, true);
		firePropertyChange("refresh", null, null);
		new VoucherOperatLogTool().InsertLogByVoucherIndexVOs(key_hm.values().toArray(new VoucherIndexVO[0]), nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55","UC001-0000039")/*@res "删除"*/, GlNodeConst.GLNODE_VOUCHERPREPARE);
		return vecpkspower.size() == 0 ? null : (String[]) vecpkspower.toArray(new String[vecpkspower.size()]);

	}


	public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		propertyChange.firePropertyChange(propertyName, oldValue, newValue);
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-9-8 10:24:25)
	 *
	 * @param propertyName
	 *            java.lang.String
	 * @param oldValue
	 *            java.lang.Object
	 * @param newValue
	 *            java.lang.Object
	 */
	public void fireVoucherChange(int voucherKey, Object oldValue, Object newValue) {
		this.voucherlistener.fireVoucherChange(voucherKey, oldValue, newValue);
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-9-8 10:24:25)
	 *
	 * @param propertyName
	 *            java.lang.String
	 * @param oldValue
	 *            java.lang.Object
	 * @param newValue
	 *            java.lang.Object
	 */
	public void fireVoucherChange(VoucherChangeEvent e) {
		this.voucherlistener.fireVoucherChange(e);
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-11-1 11:43:11)
	 *
	 * @return java.lang.String
	 * @param value
	 *            nc.vo.pub.lang.UFDouble
	 * @param iKey
	 *            int
	 */
	public String formatUFDoubleForUI(UFDouble value, int iKey, int iIndex) {
		if (value.equals(new UFDouble(0)))
			return "";
		String pk_orgbook = getVoucherVO(iIndex).getPk_accountingbook();
		switch (iKey) {
		case nc.vo.gl.voucherlist.VoucherIndexKey.V_TOTALCREDIT:
		case nc.vo.gl.voucherlist.VoucherIndexKey.V_TOTALDEBIT: {
			try {
				int[] digitAndRoundtype = nc.itf.fi.pub.Currency.getCurrDigitAndRoundtype(VoucherDataCenter.getMainCurrencyPK(pk_orgbook));
				return nc.ui.glcom.numbertool.GlNumberFormat.formatUFDouble(nc.ui.glcom.numbertool.GlNumberFormat.formatUFDouble(value, digitAndRoundtype[0], digitAndRoundtype[1]));
			} catch (Exception e) {
				return nc.ui.glcom.numbertool.GlNumberFormat.formatUFDouble(value);
			}
		}
		}
		return "";
	}

	public Object getCurrent() throws Exception {
		if (getCurrentIndexs() == null || getCurrentIndexs().length == 0 || m_VoucherLists == null || m_VoucherLists.size() == 0)
			return null;
		VoucherIndexVO temp = (VoucherIndexVO) m_VoucherLists.elementAt(getCurrentIndexs()[0]);
		if (((nc.vo.gl.pubvoucher.VoucherVO) temp.getVoucherVO()).getNumDetails() == 0) {
			VoucherVO vou = VoucherDataBridge.getInstance().queryByPk(temp.getPk_voucher());
			String userid = GlWorkBench.getLoginUser();
			Boolean ispower = new Boolean(false);
			ispower = VoucherDataBridge.getInstance().isAccsubjPower(vou, userid);
			if (ispower.booleanValue()) {
				vou = VoucherDataBridge.getInstance().filterDetailByAccsubjPower(vou, userid);
			}
			if (vou == null)
				throw new GlBusinessException(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000538")/*
																															 * @res
																															 * "记账"
																															 */);
			temp.setVoucherVO(vou);
		}
		return temp.getVoucherVO().clone();
	}

	public int getCurrentIndex() {
		return (m_CurrentIndex == null || m_CurrentIndex.length == 0) ? -1 : m_CurrentIndex[0];
	}

	public int[] getCurrentIndexs() {
		return m_CurrentIndex;
	}

	public Object getData(Object objUserData) throws Exception {
		String strUserData = objUserData.toString();

		if (strUserData.equals("station")) {
			if (m_VoucherLists.size() == 0 || m_VoucherLists.size() == 1)
				return "single";
			else if (getCurrentIndexs() != null && getCurrentIndexs().length == 1 && getCurrentIndexs()[0] == 0)
				return "first";
			else if (getCurrentIndexs() != null && getCurrentIndexs().length == 1 && getCurrentIndexs()[0] == m_VoucherLists.size() - 1)
				return "last";
			else
				return "normal";
		} else if (strUserData.equals("first"))
			setCurrentIndex(new int[] { 0 }, true);
		else if (strUserData.equals("last"))
			setCurrentIndex(new int[] { m_VoucherLists.size() - 1 }, true);
		else if (strUserData.equals("previous")) {
			int m = 0;
			if (getCurrentIndexs() == null || getCurrentIndexs().length == 0 || getCurrentIndexs()[0] == 0)
				m = 0;
			else
				m = getCurrentIndexs()[0] - 1;
			if (m_VoucherLists.size() == 0 || m < 0 || m > m_VoucherLists.size())
				return null;
			setCurrentIndex(new int[] { m }, true);
		} else if (strUserData.equals("next")) {
			int m = 0;
			if (getCurrentIndexs() == null || getCurrentIndexs().length == 0)
				m = 0;
			else if (getCurrentIndexs()[0] == m_VoucherLists.size() - 1)
				m = m_VoucherLists.size() - 1;
			else
				m = getCurrentIndexs()[0] + 1;

			if (m_VoucherLists.size() == 0 || m < 0 || m > m_VoucherLists.size())
				return null;
			setCurrentIndex(new int[] { m }, true);
		} else if (strUserData.equals("nowselectvoucher")) {
			getCurrent();
		}

		return getCurrent();
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-12-13 17:46:52)
	 *
	 * @return nc.ui.gl.voucherlist.IListUI
	 */
	private IListUI getListUI() {
		return m_listUI;
	}

	private IPara getPara() {
		return m_para;
	}

	/**
	 * 返回按参数取的数据。
	 */
	public java.lang.Object getParameter(java.lang.Object key) {
		if (key == null)
			return null;
		if (key instanceof String) {
			String strkey = key.toString().trim();
			if (strkey.equals("selectedvouchers")) {
				int[] indexes = getCurrentIndexs();
				if (indexes == null || indexes.length == 0 || indexes[0] < 0)
					return null;
//				VoucherVO[] vouchers = new VoucherVO[indexes.length];
//				for (int i = 0; i < vouchers.length; i++) {
//					vouchers[i] = getVoucherVO(indexes[i]);
//				}
				List<VoucherVO> list = new LinkedList<VoucherVO>();
				VoucherVO voTemp = null;
				for (int i = 0; i < indexes.length; i++) {
					voTemp = getVoucherVO(indexes[i]);
					if(voTemp != null){
						list.add(voTemp);
					}
				}
				return list.toArray(new VoucherVO[0]);
			} else if (key.toString().equals("modulecode")) {
				if (getListUI().getToftPanel() != null)
					return getListUI().getToftPanel().getModuleCode();
			}
		}
		return null;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2002-6-12 9:34:42)
	 *
	 * @return nc.vo.gl.pubvoucher.VoucherPrintVO[]
	 */
	private VoucherPrintVO[] getPrintVOs() {
		VoucherVO[] vouchers;
		VoucherPrintVO[] printvos = null;
		try {
			if (getCurrentIndexs() == null || getCurrentIndexs().length == 0 || getCurrentIndexs()[0] == -1)
				throw new nc.vo.gateway60.pub.GlBusinessException(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000073")/*
																																		 * @res
																																		 * "没有被选中的凭证！请先选择凭证，然后打印。"
																																		 */);
			String[] pk_vouchers = new String[getCurrentIndexs().length];
			for (int i = 0; i < getCurrentIndexs().length; i++) {
				pk_vouchers[i] = getVoucherIndexVO(getCurrentIndexs()[i]).getPk_voucher();
			}
			vouchers = VoucherDataBridge.getInstance().queryByPks(pk_vouchers);
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			throw new nc.vo.gateway60.pub.GlBusinessException(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000487")/*
																																	 * @res
																																	 * "凭证信息有错，请检查后重新尝试该操作。"
																																	 */);
		}
		if (vouchers != null) {
			printvos = new VoucherPrintVO[vouchers.length];
			for (int i = 0; i < vouchers.length; i++) {
				printvos[i] = new VoucherPrintVO();
				printvos[i].setPrintsubjlevel(m_printsubjlevel);
				printvos[i].setVoucherVO(vouchers[i]);
			}
		}
		return printvos;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2002-6-12 9:34:42)
	 *
	 * @return nc.vo.gl.pubvoucher.VoucherPrintVO[]
	 */
	private VoucherPrintVO[] getPrintVOs(String[] pk_vouchers) {
		VoucherVO[] vouchers;
		VoucherPrintVO[] printvos = null;
		try {
			vouchers = VoucherDataBridge.getInstance().queryByPks(pk_vouchers);
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			throw new nc.vo.gateway60.pub.GlBusinessException(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000487")/*
																																	 * @res
																																	 * "凭证信息有错，请检查后重新尝试该操作。"
																																	 */);
		}
		if (vouchers != null) {
			printvos = new VoucherPrintVO[vouchers.length];
			for (int i = 0; i < vouchers.length; i++) {
				printvos[i] = new VoucherPrintVO();
				printvos[i].setPrintsubjlevel(m_printsubjlevel);
				printvos[i].setVoucherVO(vouchers[i]);
			}
		}
		return printvos;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2003-6-6 10:39:35)
	 *
	 * @return nc.vo.gl.pubvoucher.VoucherVO
	 * @param voucher
	 *            nc.vo.gl.pubvoucher.VoucherVO
	 */
	private VoucherVO getPrintVoucher(VoucherVO voucher) {
		VoucherVO tmpvoucher = (VoucherVO) voucher.clone();
		tmpvoucher.clearEmptyDetail();
		if (m_printsubjlevel > 0) {
			int iSubjcodeLength = 0;
			for (int i = 0; i < m_printsubjlevel; i++) {
				if (i >= VoucherDataCenter.getSubjLevelScheme(tmpvoucher.getPk_accountingbook()).length)
					break;
				iSubjcodeLength = iSubjcodeLength + VoucherDataCenter.getSubjLevelScheme(tmpvoucher.getPk_accountingbook())[i];
			}
			DetailVO[] tmpdetails = tmpvoucher.getDetails();
			Vector debitdetails = new Vector();
			Vector creditdetails = new Vector();
			for (int i = 0; i < tmpdetails.length; i++) {
				if (tmpdetails[i].getAssid() == null)
					tmpdetails[i].setAssid("null");
				if (tmpdetails[i].getAccsubjcode().length() > iSubjcodeLength) {
					String tmpsubjcode = tmpdetails[i].getAccsubjcode().substring(0, iSubjcodeLength);
					nc.vo.bd.account.AccountVO tmpsubjvo = VoucherDataCenter.getAccsubjByCode(tmpvoucher.getPk_accountingbook(), tmpsubjcode, tmpvoucher.getPrepareddate().toStdString());
					tmpdetails[i].setPk_accasoa(tmpsubjvo.getPk_accasoa());
					tmpdetails[i].setAccsubjcode(tmpsubjvo.getCode());
					tmpdetails[i].setAccsubjname(tmpsubjvo.getName());
				}
				if (m_printasslevel == 0) {
					tmpdetails[i].setAssid(null);
					tmpdetails[i].setAss(null);
				}
				if (!tmpdetails[i].getLocalcreditamount().equals(new UFDouble(0)))
					creditdetails.addElement(tmpdetails[i]);
				else
					debitdetails.addElement(tmpdetails[i]);
			}
			DetailVO[] tmpdebit = new DetailVO[debitdetails.size()];
			DetailVO[] tmpcredit = new DetailVO[creditdetails.size()];
			debitdetails.copyInto(tmpdebit);
			creditdetails.copyInto(tmpcredit);
			int intSortIndex[] = null;
			if (m_printasslevel == 0) {
				intSortIndex = new int[] { VoucherKey.D_PK_ACCSUBJ, VoucherKey.D_PK_CURRTYPE };
			} else {
				intSortIndex = new int[] { VoucherKey.D_PK_ACCSUBJ, VoucherKey.D_PK_CURRTYPE, VoucherKey.D_ASSID };
			}
			tmpdebit = nc.vo.gl.vouchertools.DetailTool.sumDetails(tmpdebit, intSortIndex);
			tmpcredit = nc.vo.gl.vouchertools.DetailTool.sumDetails(tmpcredit, intSortIndex);
			Vector vecdetails = new Vector();
			if (tmpdebit != null)
				for (int i = 0; i < tmpdebit.length; i++) {
					vecdetails.addElement(tmpdebit[i]);
				}
			if (tmpcredit != null)
				for (int i = 0; i < tmpcredit.length; i++) {
					vecdetails.addElement(tmpcredit[i]);
				}
			tmpvoucher.setDetail(vecdetails);
		}
		return tmpvoucher;
	}

	/**
	 * 此处插入方法说明。 创建日期：(01-10-15 16:29:19)
	 */
	public OperationResultVO[] getResult() throws Exception {
		OperationResultVO[] result = new OperationResultVO[m_VoucherLists.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = ((VoucherIndexVO) m_VoucherLists.elementAt(i)).getResult();
		}
		return result;
	}

	public int getSize() {
		return m_VoucherLists.size();
	}

	/**
	 * 返回MasterModel的界面类，以处理部分与界面有关的操作。
	 */
	public java.awt.Component getUI() {
		return m_defaultUI;
	}

	// 对当前行的VO进行操作
	public Object getValue(int intKey) throws Exception {
		return ((VoucherIndexVO) getVoucherIndexVO(getCurrentIndexs()[0])).getValue(intKey);
	}

	/**
	 * getVO 方法注解。
	 */
	public java.lang.Object getVO(int iIndex) throws java.lang.Exception {
		return null;
	}

	public Object getVoucherByPk(String strPk_voucher) throws Exception {
		return VoucherDataBridge.getInstance().queryByPk(strPk_voucher);
	}

	/**
	 * getVO 方法注解。
	 */
	public VoucherIndexVO getVoucherIndexVO(int iIndex) {
		if (iIndex < 0 || iIndex >= m_VoucherLists.size())
			return null;
		return (VoucherIndexVO) m_VoucherLists.elementAt(iIndex);
	}

	/**
	 * getVO 方法注解。
	 */
	public VoucherIndexVO getVoucherIndexVO(String voucherpk) {
		for (int i = 0; i < size(); i++) {
			VoucherIndexVO tmp_vo = (VoucherIndexVO) m_VoucherLists.elementAt(i);
			if (tmp_vo.getPk_voucher().equals(voucherpk))
				return tmp_vo;
		}
		return null;
	}

	/**
	 * getVO 方法注解。
	 */
	public VoucherVO getVoucherVO(int iIndex) {
		if (iIndex < 0 || iIndex >= m_VoucherLists.size())
			return null;
		return (VoucherVO) getVoucherIndexVO(iIndex).getVoucherVO();
	}

	/**
	 * 此处插入方法说明。 创建日期：(2002-6-12 9:34:42)
	 *
	 * @return nc.vo.gl.pubvoucher.VoucherPrintVO[]
	 */
	private VoucherVO[] getVoucherVOs(String[] pk_vouchers) {
		VoucherVO[] vouchers;
		VoucherPrintVO[] printvos = null;
		try {
			vouchers = VoucherDataBridge.getInstance().queryByPks(pk_vouchers);
			HashMap tmp_map = new HashMap();
			if (vouchers != null)
				for (int i = 0; i < vouchers.length; i++) {
					if (vouchers[i] != null)
						tmp_map.put(vouchers[i].getPk_voucher(), vouchers[i]);
				}
			Vector vv = new Vector();
			for (int i = 0; i < pk_vouchers.length; i++) {
				if (tmp_map.get(pk_vouchers[i]) != null)
					vv.addElement(tmp_map.get(pk_vouchers[i]));
			}
			VoucherVO[] rr = null;
			if (vv.size() > 0) {
				rr = new VoucherVO[vv.size()];
				vv.copyInto(rr);
			}
			return rr;
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			throw new nc.vo.gateway60.pub.GlBusinessException(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000487")/*
																																	 * @res
																																	 * "凭证信息有错，请检查后重新尝试该操作。"
																																	 */);
		}
	}

	public Object invoke(Object objData, Object objUserData) throws Exception {
		if (objUserData.toString().equals("getvoucherbypk"))
			return getVoucherByPk((String) objData);
		else if (objUserData.toString().equals("setvoucher")) {
			m_VoucherLists.clear();
			m_voucherpkcache.clear();
			setCurrentIndex(null, true);
			addVoucher((nc.vo.gl.pubvoucher.VoucherVO) objData);
		} else if (objUserData.toString().equals("setvouchers")) {
			// hurh 会计平台制单连接数
			VoucherVO[] vouchers = (VoucherVO[]) objData;
			if(getFuncCode() != null){
				GlRemoteCallProxy.callProxy(CacheRequestFactory.newAddVoucherContextVO(getFuncCode(),vouchers[0].getPk_accountingbook())); // 一次联查不可能跨核算账簿
			}
			m_VoucherLists.clear();
			m_voucherpkcache.clear();
			setCurrentIndex(null, true);
			addVouchers(vouchers);
			setCurrentIndex(new int[] { 0 }, true);
			firePropertyChange("EditCurrent", null, getCurrent());
		} else if (objUserData.toString().equals("getresult"))
			return getResult();
		else if (objUserData.toString().equals("setVoucherPk")) {
			m_VoucherLists.clear();
			m_voucherpkcache.clear();
			setCurrentIndex(null, true);
			VoucherVO[] vouchers = VoucherDataBridge.getInstance().queryByPks((String[]) objData);
			// hurh 60帐表联查连接数优化
			if(getFuncCode() != null){
				GlRemoteCallProxy.callProxy(CacheRequestFactory.newAddVoucherContextVO(getFuncCode(),vouchers[0].getPk_accountingbook())); // 一次联查不可能跨核算账簿
			}
			if (vouchers != null && vouchers.length > 0) {
				Vector<VoucherVO> vec = new Vector<VoucherVO>();
				String userid = GlWorkBench.getLoginUser();
				for (int i = 0; i < vouchers.length; i++) {
					Boolean isAccsubjpower = VoucherDataBridge.getInstance().isAccsubjPower(vouchers[i], userid);
					if (isAccsubjpower.booleanValue()) {
						vouchers[i] = VoucherDataBridge.getInstance().filterDetailByAccsubjPower(vouchers[i], userid);
					}
					vec.add(vouchers[i]);
				}
				vouchers = vec.toArray(new VoucherVO[vouchers.length]);
				addVouchers(vouchers);
				setCurrentIndex(new int[] { 0 }, true);
				((nc.ui.gl.uicfg.IBasicView) getUI()).setEditable(false);
			}
			// firePropertyChange("StopEditing", null, getCurrent());
		} else if (objUserData.toString().equals("addPropertyChangeListener")) {
			addPropertyChangeListener((java.beans.PropertyChangeListener) objData);
		} else if (objUserData.toString().equals("removePropertyChangeListener")) {
			removePropertyChangeListener((java.beans.PropertyChangeListener) objData);
		} else if (objUserData.toString().equals("addVoucherChangeListener")) {
			addVoucherChangeListener((VoucherChangeListener) objData);
		} else if (objUserData.toString().equals("removeVoucherChangeListener")) {
			removeVoucherChangeListener((VoucherChangeListener) objData);
		} else if (objUserData.toString().equals("ui")) {
			if (m_defaultUI == null)
				;
			{
				m_defaultUI = (java.awt.Component) objData;
			}
		}

		return null;
	}

	/**
	 * modifyVO 方法注解。
	 */
	public void modifyVO(java.lang.Object objValue) throws java.lang.Exception {
		VoucherIndexVO tempVO1 = new VoucherIndexVO();
		tempVO1.setVoucherVO((VoucherVO) ((VoucherVO) objValue).clone());
		String strPK = tempVO1.getPk_voucher();
		for (int i = 0; i < m_VoucherLists.size(); i++) {
			VoucherIndexVO tempVO = (VoucherIndexVO) m_VoucherLists.elementAt(i);
			if (strPK.equals(tempVO.getPk_voucher()) || (i == getCurrentIndex() && tempVO.getPk_voucher() == null)) {
				m_VoucherLists.setElementAt(tempVO1, i);
				// hurh 不需要刷新，后面单独fire了刷新事件
				setCurrentIndex(new int[]{i}, false);

				firePropertyChange("ModVoucherIndexVO", null, tempVO1);
				fireVoucherChange(VoucherKey.P_VOUCHER, null, tempVO1.getVoucherVO());
				return;
			}
		}
		addVO(((VoucherVO) objValue).clone());
		for (int i = 0; i < m_VoucherLists.size(); i++) {
			VoucherIndexVO tempVO = (VoucherIndexVO) m_VoucherLists.elementAt(i);
			if (strPK.equals(tempVO.getPk_voucher())) {
				if (getCurrentIndex() != i)
					setCurrentIndex(new int[] { i }, true);
			}
		}
	}

	/**
	 * 此处插入方法说明。 创建日期：(2002-4-12 14:51:30)
	 *
	 * @param filepath
	 *            java.lang.String
	 * @param fileground
	 *            boolean
	 * @param selectionmode
	 *            int
	 */
	public void outputVouchers(Container parent,String filepath, boolean fileground, int selectionmode) throws Exception {
		if (fileground) {
			File file = new File(filepath);
			if (file.exists()) {
				int result = MessageDialog.showYesNoDlg(parent, NCLangRes.getInstance().getStrByID("excelimport", "ExcelImporter-000006")/*提示*/, NCLangRes.getInstance().getStrByID("excelimport", "ExcelImporter-000007", null, new String[]{filepath})/*文件 {0} 已存在是否覆盖？*/);
				if (result != MessageDialog.ID_YES) {
					return;
				}
			} 
			
			VoucherVO[] vouchers = null;
			if (selectionmode == 1) {
				if (getCurrentIndexs() == null || getCurrentIndexs().length == 0 || getCurrentIndexs()[0] == -1)
					throw new nc.vo.gateway60.pub.GlBusinessException(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000488")/*
																																			 * @res
																																			 * "没有被选中的凭证！请先选择凭证，然后导出。"
																																			 */);
				String[] pk_vouchers = new String[getCurrentIndexs().length];
				for (int i = 0; i < getCurrentIndexs().length; i++) {
					pk_vouchers[i] = getVoucherIndexVO(getCurrentIndexs()[i]).getPk_voucher();
				}
				vouchers = VoucherDataBridge.getInstance().queryByPks(pk_vouchers);
			}

			if (vouchers != null && vouchers.length != 0) {
				String pk_accountingbook = vouchers[0].getPk_accountingbook();
				String pk_org = AccountBookUtil.getPk_orgByAccountBookPk(pk_accountingbook);
				String pk_group = GlWorkBench.getLoginGroup();
				IGeneralAccessor accessor = GeneralAccessorFactory.getAccessor(IOrgMetaDataIDConst.ORG);
				IBDData orgVO = accessor.getDocByPk(pk_org);
				IBDData groupVO = accessor.getDocByPk(pk_group);
				
				org.w3c.dom.Document document = new DocumentImpl();
				org.w3c.dom.Element root = document.createElement("ufinterface");
				root.setAttribute("account", "develop");
				root.setAttribute("billtype", "vouchergl");
				root.setAttribute("businessunitcode", "develop");
				root.setAttribute("filename", null);
				root.setAttribute("groupcode", groupVO==null?null:groupVO.getCode());
				root.setAttribute("isexchange", null);
				root.setAttribute("orgcode", orgVO==null?null:orgVO.getCode());
				root.setAttribute("receiver", pk_group);
				root.setAttribute("replace", null);
				root.setAttribute("roottag", null);
				root.setAttribute("sender", "001");

				for (int i = 0; i < vouchers.length; i++) {
					org.w3c.dom.Node voucherNode = XML_VoucherTranslator.getVoucherNode(document, vouchers[i]);
					root.appendChild(voucherNode);
				}
				document.appendChild(root);
				StringBuffer fileBuffer = new StringBuffer();
				XML_VoucherTranslator.writeXMLFormatString(fileBuffer , document, -2);
				XML_VoucherTranslator.saveToFile(filepath, fileBuffer);
			}

		} else {
			if (selectionmode == 1) {
				if (getCurrentIndexs() == null || getCurrentIndexs().length == 0 || getCurrentIndexs()[0] == -1)
					throw new nc.vo.gateway60.pub.GlBusinessException(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000488")/*
																																			 * @res
																																			 * "没有被选中的凭证！请先选择凭证，然后导出。"
																																			 */);
				String[] pk_vouchers = new String[getCurrentIndexs().length];
				for (int i = 0; i < getCurrentIndexs().length; i++) {
					pk_vouchers[i] = getVoucherIndexVO(getCurrentIndexs()[i]).getPk_voucher();
				}
				GLPubProxy.getRemoteVoucherList().outputVoucherByPKs(pk_vouchers);
			}
		}
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-12-10 9:56:57)
	 */
	public void print(int flag) {
		nc.ui.pub.print.PrintEntry pEntry = null;
		int printpagelength = 0;
		switch (flag) {
		case 0: // 凭证列表
		{
			if(m_VoucherLists.size() <= 0){
				throw new GlBusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("glpub_0","02002003-0167")/*@res "没有被选中的凭证！请先选择凭证，然后打印。"*/);
			}
			VoucherListPrintVO listprintvo = new VoucherListPrintVO();
			VoucherVO[] tmp_vos = new VoucherVO[m_VoucherLists.size()];
			for (int i = 0; i < m_VoucherLists.size(); i++) {
				tmp_vos[i] = ((VoucherIndexVO) m_VoucherLists.elementAt(i)).getVoucherVO();
			}
			listprintvo.setVoucherVOs(tmp_vos);
			pEntry = new nc.ui.pub.print.PrintEntry((java.awt.Component) getListUI(), listprintvo);
			
			String pk_group = WorkbenchEnvironment.getInstance().getGroupVO().getPk_group();

			pEntry.setTemplateID(pk_group, GlNodeConst.GLNODE_VOUCHERSIGN/**"20021010"**/, GlWorkBench.getLoginUser(), null, GlNodeConst.GLNODE_VOUCHERSIGN+"1",null);
			pEntry.preview();
			break;
		}
		case 1: // 凭证明细
		{
			if (getCurrentIndexs() == null || getCurrentIndexs().length == 0 || getCurrentIndexs()[0] == -1)
				throw new GlBusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("glpub_0","02002003-0167")/*@res "没有被选中的凭证！请先选择凭证，然后打印。"*/);
			String[] pk_vouchers = new String[getCurrentIndexs().length];
			for (int i = 0; i < getCurrentIndexs().length; i++) {
				pk_vouchers[i] = getVoucherIndexVO(getCurrentIndexs()[i]).getPk_voucher();
			}
			// VoucherPrintVO printvo = new VoucherPrintVO();
//			if (pEntry == null) {
				pEntry = new nc.ui.pub.print.PrintEntry((java.awt.Component) getListUI());
				String pk_group = WorkbenchEnvironment.getInstance().getGroupVO().getPk_group();
				pEntry.setTemplateID(pk_group, GlNodeConst.GLNODE_VOUCHERPREPARE, GlWorkBench.getLoginUser(), null, null, null);
				if (pEntry.selectTemplate() < 0)
					return;
				printpagelength = pEntry.getBreakPos();
//				pEntry.beginVoucherBatchPrint();
				for (int i = 0; i < pk_vouchers.length; i++) {
					VoucherVO[] vouchers = getVoucherVOs(new String[] { pk_vouchers[i] });
					if (vouchers == null)
						continue;
					VoucherPrintVO printvo = new VoucherPrintVO();
					printvo.setPrintpagelength(printpagelength);
					printvo.setPrintsubjlevel(m_printsubjlevel);
					printvo.setPrintasslevel(m_printasslevel);
					printvo.setVoucherVO(getPrintVoucher(vouchers[0]));
					pEntry.setDataSource(printvo);
				}
				pEntry.preview();
//			}
			break;
		}
		case 2: // 凭证清单
		{
			if (getCurrentIndexs() == null || getCurrentIndexs().length == 0 || getCurrentIndexs()[0] == -1||m_VoucherLists==null||m_VoucherLists.size()==0){
				throw new GlBusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("glpub_0","02002003-0167")/*@res "没有被选中的凭证！请先选择凭证，然后打印。"*/);
			}
			VoucherListPrintVO listprintvo = new VoucherListPrintVO();
			VoucherVO[] tmp_vos = new VoucherVO[getCurrentIndexs().length];
			for (int i = 0; i < getCurrentIndexs().length; i++) {
				tmp_vos[i] = ((VoucherIndexVO) m_VoucherLists.elementAt(getCurrentIndexs()[i])).getVoucherVO();
			}
			listprintvo.setVoucherVOs(tmp_vos);
			pEntry = new nc.ui.pub.print.PrintEntry((java.awt.Component) getListUI(), listprintvo);
			String pk_group = WorkbenchEnvironment.getInstance().getGroupVO().getPk_group();
			pEntry.setTemplateID(pk_group, /**"20021010"**/GlNodeConst.GLNODE_VOUCHERSIGN, GlWorkBench.getLoginUser(), null, GlNodeConst.GLNODE_VOUCHERSIGN+"1", null);
			pEntry.preview();
			break;
		}
		}
	}

	private void QueryAll() throws Exception {
		QueryElementVO[] tempqvo = new QueryElementVO[3];
		tempqvo[0] = new QueryElementVO();
		tempqvo[0].setDatatype("String");
		tempqvo[0].setOperator("=");
		tempqvo[0].setIsAnd(nc.vo.pub.lang.UFBoolean.TRUE);
		tempqvo[0].setRestrictfield("gl_voucher.pk_accountingbook");
		String pk_accbook = VoucherDataCenter.getClientPk_orgbook();
		if(StringUtils.isEmpty(pk_accbook)){
			Logger.error(getClass().getName()+"->主体账簿为空");
			return;
		}
		tempqvo[0].setDatas(new String[] { VoucherDataCenter.getClientPk_orgbook() });
		tempqvo[1] = new QueryElementVO();
		tempqvo[1].setDatatype("String");
		tempqvo[1].setOperator("=");
		tempqvo[1].setIsAnd(nc.vo.pub.lang.UFBoolean.TRUE);
		tempqvo[1].setRestrictfield("gl_voucher.year");
		tempqvo[1].setDatas(new String[] { GlWorkBench.getLoginYear() });
		tempqvo[2] = new QueryElementVO();
		tempqvo[2].setDatatype("String");
		tempqvo[2].setOperator("=");
		tempqvo[2].setIsAnd(nc.vo.pub.lang.UFBoolean.TRUE);
		tempqvo[2].setRestrictfield("gl_voucher.period");
		
		AccountCalendar calendar = CalendarUtilGL.getAccountCalendarByAccountBook(pk_accbook);
		calendar.setDate(GlWorkBench.getBusiDate());
		
		tempqvo[2].setDatas(new String[] { calendar.getMonthVO().getAccperiodmth() });
		try {
			// if
			// (nc.ui.bd.datapower.DataPowerServ.isUsedDataPowerByOrgTypeCode("bd_vouchertype",
			// "凭证类别", OrgnizeTypeVO.ZHUZHANG_TYPE,
			// VoucherDataCenter.getClientPk_orgbook()))
			if (VoucherDataBridge.getInstance().isVouchertypeUseddaDatapower(VoucherDataCenter.getClientPk_orgbook(), null).booleanValue()) {
//				QueryElementVO[] tempqvo1 = new QueryElementVO[4];
//				for (int i = 0; i < tempqvo.length; i++) {
//					tempqvo1[i] = tempqvo[i];
//				}
//				tempqvo = tempqvo1;
//				tempqvo[3] = new QueryElementVO();
//				tempqvo[3].setDatatype("String");
//				tempqvo[3].setOperator("=");
//				tempqvo[3].setIsAnd(nc.vo.pub.lang.UFBoolean.TRUE);
//				tempqvo[3].setRestrictfield("gl_voucher.pk_vouchertype");
//				//60x String[] pks = nc.ui.bd.datapower.DataPowerServ.hasPowerForGlOrgType(OrgnizeTypeVO.ZHUZHANG_TYPE, VoucherDataCenter.getClientPk_orgbook(), "bd_vouchertype", nc.ui.ml.NCLangRes.getInstance().getStrByID("common", "UC000-0000479")/*
//																																																													 * @res
//																																																													 * "凭证类别"
//																																																													 */, nc.ui.pub.ClientEnvironment.getInstance().getUser().getPrimaryKey(), ClientEnvironment.getInstance().getCorporation().getPk_corp());
				//tempqvo[3].setDatas(pks);
				//60x-end
			}
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			throw new nc.vo.gateway60.pub.GlBusinessException(e.getMessage());
		}

		VoucherQueryConditionVO qvo = new VoucherQueryConditionVO();
		qvo.setNormalconditionvo(tempqvo);
		qvo.setIspreparedpower(true);
		VoucherVO[] vouchers = VoucherDataBridge.getInstance().queryByConditionVO(new VoucherQueryConditionVO[] { qvo }, new Boolean(false));
		if (vouchers != null && vouchers.length > 0) {
			nc.vo.gl.voucherlist.VoucherIndexVO[] resultVO = new nc.vo.gl.voucherlist.VoucherIndexVO[vouchers.length];
			for (int i = 0; i < vouchers.length; i++) {
				nc.vo.gl.voucherlist.VoucherIndexVO tempvo = new nc.vo.gl.voucherlist.VoucherIndexVO();
				tempvo.setVoucherVO(vouchers[i]);
				resultVO[i] = tempvo;
			}
			for (int i = 0; i < resultVO.length; i++) {
				addVoucher(resultVO[i]);
			}
			firePropertyChange("message", null, nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000489"/*
					 * @res
					 * "本次查询共查出符合条件的凭证{0}张，列表中现有凭证{1}张。"
					 		*/,null,new String[]{String.valueOf(m_VoucherLists.size()),String.valueOf(m_VoucherLists.size())}));
		}
	}

	private void QueryByOldVO() {
		try {
			nc.vo.gl.pubvoucher.VoucherVO[] vouchers = VoucherDataBridge.getInstance().queryByConditionVO(conditionVO, new Boolean(false));
			if(vouchers == null)
				return;
			VoucherIndexVO[] resultVO = new nc.vo.gl.voucherlist.VoucherIndexVO[vouchers.length];
			for (int i = 0; i < vouchers.length; i++) {
				VoucherIndexVO tempvo = new VoucherIndexVO();
				tempvo.setVoucherVO(vouchers[i]);
				resultVO[i] = tempvo;
			}
			for (int i = 0; i < resultVO.length; i++) {
				addVoucher(resultVO[i]);
			}
			firePropertyChange("message", null, nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000489"/*
					 * @res
					 * "本次查询共查出符合条件的凭证{0}张，列表中现有凭证{1}张。"
					 		*/,null,new String[]{String.valueOf(m_VoucherLists.size()),String.valueOf(m_VoucherLists.size())}));
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			throw new nc.vo.gateway60.pub.GlBusinessException(e.getMessage());
		}
	}

	public void QueryByVO(nc.vo.gl.voucherquery.VoucherQueryConditionVO vo) {
		try {
			conditionVO = new nc.vo.gl.voucherquery.VoucherQueryConditionVO[] { vo };
			removeAll();
			nc.vo.gl.pubvoucher.VoucherVO[] vouchers = VoucherDataBridge.getInstance().queryByConditionVO(conditionVO, new Boolean(false));
			if (vouchers == null)
				vouchers = new VoucherVO[0];
			//
			//HashSet<String> userpkset=new HashSet<String>();
			//
			VoucherIndexVO[] resultVO = new nc.vo.gl.voucherlist.VoucherIndexVO[vouchers.length];
			for (int i = 0; i < vouchers.length; i++) {
				VoucherIndexVO tempvo = new VoucherIndexVO();
				tempvo.setVoucherVO(vouchers[i]);
				UserVO us = SystemUserDataCache.getInstance().getUserBypk(tempvo.getPk_glorgbook(), tempvo.getPk_prepared());
				if (us != null) {
					tempvo.setPrepared(us.getUser_name());
				}
				// hurh 
				us = SystemUserDataCache.getInstance().getUserBypk(tempvo.getPk_glorgbook(), tempvo.getPk_casher());
				if (us != null) {
					tempvo.setCasher(us.getUser_name());
				}
				us = SystemUserDataCache.getInstance().getUserBypk(tempvo.getPk_glorgbook(), tempvo.getPk_checked());
				if (us != null) {
					tempvo.setChecked(us.getUser_name());
				}
				us = SystemUserDataCache.getInstance().getUserBypk(tempvo.getPk_glorgbook(), tempvo.getPk_manager());
				if (us != null) {
					tempvo.setManager(us.getUser_name());
				}
				
				// hurh
				tempvo.setVouchertype(VoucherDataCenter.getVouchertypeNameByPk_orgbook(tempvo.getPk_glorgbook(), tempvo.getPk_vouchertype()));
				resultVO[i] = tempvo;
				//
//				userpkset.add(vouchers[i].getPk_prepared());
//				userpkset.add(vouchers[i].getPk_casher());
//				userpkset.add(vouchers[i].getPk_checked());
//				userpkset.add(vouchers[i].getPk_manager());
			}
			//
		//	SystemUserDataCache.getInstance().getUsersBypkS(userpkset.toArray(new String[0]));
			for (int i = 0; i < resultVO.length; i++) {
				addVoucher(resultVO[i]);
			}
			firePropertyChange("message", null, nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000489"/*
					 * @res
					 * "本次查询共查出符合条件的凭证{0}张，列表中现有凭证{1}张。"
					 		*/,null,new String[]{String.valueOf(m_VoucherLists.size()),String.valueOf(m_VoucherLists.size())}));
			// firePropertyChange("refresh", null, null);
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			throw new nc.vo.gateway60.pub.GlBusinessException(e.getMessage());
		}
		if (m_VoucherLists.size() > 0)
			setCurrentIndex(new int[] { 0 }, true);
		else
			setCurrentIndex(null, true);
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-11-22 16:31:50)
	 */
	public void refresh() throws Exception {
		removeAll();
		if (conditionVO != null)
			QueryByOldVO();
		else
			QueryAll();
		if (m_VoucherLists.size() > 0)
			setCurrentIndex(new int[] { 0 }, true);
		else
			setCurrentIndex(null, true);
	}

	public void removeAll() throws Exception {
		m_voucherpkcache.clear();
		m_VoucherLists.clear();
		m_tipmap.clear();
		firePropertyChange("RemoveAll", null, new String(""));
	}

	public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
		propertyChange.removePropertyChangeListener(listener);
	}

	/**
	 * removeVO 方法注解。
	 */
	public void removeVO(java.lang.Object objValue) throws java.lang.Exception {
		VoucherIndexVO tempVO = new VoucherIndexVO();
		try {
			tempVO.setVoucherVO((VoucherVO) objValue);
		} catch (ClassCastException e) {
			tempVO = (VoucherIndexVO) objValue;
		}
		String strPK = tempVO.getPk_voucher();
		for (int i = 0; i < m_VoucherLists.size(); i++) {
			tempVO = (VoucherIndexVO) m_VoucherLists.elementAt(i);
			if (strPK.equals(tempVO.getPk_voucher())) {
				m_VoucherLists.removeElement(tempVO);
				if (m_VoucherLists.size() > 0) {
					if (getCurrentIndex() > 0)
						setCurrentIndex(new int[] { getCurrentIndex() - 1 }, true);
					else
						setCurrentIndex(new int[] { 0 }, true);
				} else
					setCurrentIndex(null, true);
				firePropertyChange("DelVoucherIndexVO", null, tempVO);
				break;
			}
		}
	}

	/**
	 * removeVO 方法注解。
	 */
	public void removeVOByPks(String[] pks) {
		for (int i = 0; i < size(); i++) {
			VoucherIndexVO tempIndexVO = getVoucherIndexVO(i);
			for (int j = 0; j < pks.length; j++) {
				if (pks[j].equals(tempIndexVO.getPk_voucher())) {
					m_VoucherLists.remove(i);
					i--;
					break;
				}
			}
		}
		firePropertyChange("refresh", null, null);
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-9-8 10:14:21)
	 *
	 * @param listener
	 *            java.beans.PropertyChangeListener
	 */
	public synchronized void removeVoucherChangeListener(VoucherChangeListener listener) {
		this.voucherlistener.removeVoucherChangeListener(listener);
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-12-13 16:26:35)
	 *
	 * @param indexs
	 *            int[]
	 */
	public void setCurrentIndex(int[] indexs, boolean needfire) {
		int[] oldindex = m_CurrentIndex;
		m_CurrentIndex = indexs;
		if (needfire)
			firePropertyChange("CurrentIndex", null, m_CurrentIndex);
		if (m_CurrentIndex != null && m_CurrentIndex.length == 1)
			fireVoucherChange(VoucherKey.P_VOUCHER, getVoucherVO(oldindex == null ? -1 : (oldindex.length == 0 ? -1 : oldindex[0])), getVoucherVO(m_CurrentIndex[0]));
		if (needfire && m_CurrentIndex == null || m_CurrentIndex.length == 0)
			fireVoucherChange(VoucherKey.P_VOUCHER, null, null);
	}

	public void setCurrentIndexByPKs(String[] pks, boolean needfire) {
		if (pks == null || pks.length == 0)
			return;
		HashMap<String, Integer> tmpmap = new HashMap<String, Integer>();
		for (int i = 0; i < m_VoucherLists.size(); i++) {
			VoucherIndexVO vvo = (VoucherIndexVO) m_VoucherLists.elementAt(i);
			tmpmap.put(vvo.getPk_voucher(), Integer.valueOf(i));
		}
		int[] inde = new int[pks.length];
		for (int i = 0; i < pks.length; i++) {
			Integer in = tmpmap.get(pks[i]);
			if (in != null) {
				inde[i] = in.intValue();
			} else {
				inde[i] = -1;
			}
		}
		setCurrentIndex(inde, needfire);
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-12-13 17:46:52)
	 *
	 * @param newM_listUI
	 *            nc.ui.gl.voucherlist.IListUI
	 */
	public void setListUI(IListUI newM_listUI) {
		m_listUI = newM_listUI;
		if (m_defaultUI == null)
			m_defaultUI = (java.awt.Component) m_listUI;
	}

	private void setPara(IPara para) {
		m_para = para;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2003-1-6 14:38:26)
	 *
	 * @return java.lang.Object
	 * @param key
	 *            java.lang.Object
	 * @param userdata
	 *            java.lang.Object
	 */
	public java.lang.Object setParameter(java.lang.Object key, java.lang.Object userdata) {
		if (key == null)
			return null;
		if (key instanceof String) {
			String strkey = key.toString().trim();
			if (strkey.equals("updatevouchers")) {
				VoucherVO[] vouchers = (VoucherVO[]) userdata;
				updateVoucherList(vouchers);
			}
		}
		return null;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2002-7-8 16:26:33)
	 *
	 * @param subjlevel
	 *            int
	 */
	public void setPrintSubjLevel(int subjlevel) {
		m_printsubjlevel = subjlevel;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2002-7-8 16:26:33)
	 *
	 * @param subjlevel
	 *            int
	 */
	public void setSumAss(boolean b) {
		if (b)
			m_printasslevel = -1;
		else
			m_printasslevel = 0;
	}

	public void signCurrent(String flag) throws Exception {
		int[] indexs = getCurrentIndexs();
		String userid = GlWorkBench.getLoginUser();;
		String username = GlWorkBench.getLoginUserName();
		String[] pks = null;
		Vector vecpks = new Vector();
		Vector falseresult = new Vector();
		if (flag.equals(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000023")/*
																										 * @res
																										 * "签字"
																										 */)) {
			for (int i = 0; i < indexs.length; i++) {
				VoucherIndexVO tempVO = (VoucherIndexVO) m_VoucherLists.elementAt(indexs[i]);
				if (tempVO.getPk_casher() == null && tempVO.getPk_checked() == null && tempVO.getPk_manager() == null && tempVO.getErrmessage() == null && !tempVO.getDiscardflag().booleanValue() && tempVO.getSignflag() != null && tempVO.getSignflag().booleanValue())
					vecpks.addElement(tempVO.getPk_voucher());
			}
			pks = new String[vecpks.size()];
			vecpks.copyInto(pks);
			OperationResultVO[] result = GLPubProxy.getRemoteVoucherSign().signVoucherByPks(pks, userid, new Boolean(true));
			Vector tmp_successPKs = new Vector();
			if (result != null && result.length > 0) {
				HashMap tmp_falsePKs = new HashMap();
				for (int i = 0; i < result.length; i++) {
					if (result[i] != null && result[i].m_intSuccess == 2) {
						falseresult.addElement(result[i]);
						tmp_falsePKs.put(result[i].m_strPK, result[i]);
					}
				}
				for (int i = 0; i < pks.length; i++) {
					if (tmp_falsePKs.get(pks[i]) == null) {
						tmp_successPKs.addElement(pks[i]);
					}
				}
				// getListUI().nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,result.m_userIdentical.toString());
				// refresh();
			}
			if (tmp_successPKs.size() > 0) {
				pks = new String[tmp_successPKs.size()];
				tmp_successPKs.copyInto(pks);
			}
			if (falseresult.size() > 0) {
				OperationResultVO[] tmp_falseresultvos = new OperationResultVO[falseresult.size()];
				falseresult.copyInto(tmp_falseresultvos);
				for (int i = 0; i < tmp_falseresultvos.length; i++) {
					if (tmp_falseresultvos[i].m_userIdentical == null) {
						tmp_falseresultvos[i].m_userIdentical = getVoucherIndexVO(tmp_falseresultvos[i].m_strPK);
					}
				}
				getListUI().showResultMessage(tmp_falseresultvos);
			}
		} else {
			for (int i = 0; i < indexs.length; i++) {
				VoucherIndexVO tempVO = (VoucherIndexVO) m_VoucherLists.elementAt(indexs[i]);
				if (tempVO.getPk_casher() != null && tempVO.getPk_casher().equals(userid) && tempVO.getPk_checked() == null && tempVO.getPk_manager() == null && !tempVO.getDiscardflag().booleanValue())
					vecpks.addElement(tempVO.getPk_voucher());
			}
			pks = new String[vecpks.size()];
			vecpks.copyInto(pks);
			OperationResultVO[] result = GLPubProxy.getRemoteVoucherSign().signVoucherByPks(pks, userid, new Boolean(false));
			// if (result != null && result.m_intSuccess == 1)
			// {
			// getListUI().nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,result.m_userIdentical.toString());
			// refresh();
			// }
		}
		updateVoucherList(pks, flag.equals(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000023")/*
																														 * @res
																														 * "签字"
																														 */) ? userid : null, VoucherIndexKey.V_PK_CASHER);
		updateVoucherList(pks, flag.equals(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000023")/*
																														 * @res
																														 * "签字"
																														 */) ? username : null, VoucherIndexKey.V_CASHERNAME);
		firePropertyChange("refresh", null, null);
		// fireVoucherChange(
		// VoucherKey.P_VOUCHER,
		// null,
		// ((VoucherIndexVO) getCurrentIndexVO()).getVoucherVO());
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-12-13 15:57:33)
	 *
	 * @return int
	 */
	public int size() {
		return m_VoucherLists.size();
	}

	/**
	 * 此处插入方法说明。 创建日期：(2002-6-27 11:23:44)
	 *
	 * @param rowIndex1
	 *            int
	 * @param rowIndex2
	 *            int
	 */
	public void swap(int rowIndex1, int rowIndex2) {
		Object tmpVO = m_VoucherLists.elementAt(rowIndex1);
		m_VoucherLists.setElementAt(m_VoucherLists.elementAt(rowIndex2), rowIndex1);
		m_VoucherLists.setElementAt(tmpVO, rowIndex2);
	}

	public String getTooltipString(int row) {

		String tip = null;
		if (m_istip.booleanValue()) {
			StringBuffer ssb = new StringBuffer();
			// dd
			String multiLineToolTipUIClassName = "nc.ui.gl.pubvoucher.ComponentToolTipUI";
			UIManager.put("ToolTipUI", multiLineToolTipUIClassName);
//	        UIManager.put("ToolTip.background", Color.cyan);
//	        UIManager.put("ToolTip.foreground", Color.blue);
//	        UIManager.put("ToolTip.font", new Font("Courier", Font.BOLD, 20));
			try {
				UIManager.put(multiLineToolTipUIClassName, Class.forName(multiLineToolTipUIClassName));
			} catch (ClassNotFoundException ex) {
				// TODO Auto-generated catch block
nc.bs.logging.Logger.error(ex.getMessage(), ex);
			}
			try {
				VoucherVO tempVO = (VoucherVO) getVoucherVO(row);
				appendVoucher(tempVO, ssb);
				VoucherVO voucher = tempVO;
				if (tempVO.getDetails() == null || tempVO.getDetails().length == 0) {
					if (m_tipmap.get(tempVO.getPk_voucher()) == null) {
						if (m_vomap.get(tempVO.getPk_voucher()) == null) {
							initVomap();
							voucher = m_vomap.get(tempVO.getPk_voucher());
						} else {
							voucher = m_vomap.get(tempVO.getPk_voucher());
						}
						// voucher = (VoucherVO)
						// getVoucherByPk(tempVO.getPk_voucher());
						// m_vomap.put(tempVO.getPk_voucher(), voucher);
						appendDetail(voucher, ssb);
						tip = ssb.toString();
						m_tipmap.put(tempVO.getPk_voucher(), tip);
					} else {
						tip = m_tipmap.get(tempVO.getPk_voucher());
					}
					return tip;
				}
				appendDetail(voucher, ssb);
				tip = ssb.toString();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				Logger.error(e.getMessage(), e);
			}
		}
		return tip;
	}

	private void initVomap() {
		// TODO Auto-generated method stub
		String[] pk_vouchers = new String[m_VoucherLists.size()];
		for (int i = 0; i < m_VoucherLists.size(); i++) {
			pk_vouchers[i] = ((VoucherIndexVO) m_VoucherLists.elementAt(i)).getPk_voucher();
		}
		try {
			VoucherVO[] vos = VoucherDataBridge.getInstance().queryByPks(pk_vouchers);
			for (VoucherVO voucherVO : vos) {
				m_vomap.put(voucherVO.getPk_voucher(), voucherVO);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logger.error(e.getMessage(), e);
		}
	}

	private void appendDetail(VoucherVO voucher, StringBuffer ssb) {
		voucher.clearEmptyDetail();

		String userid = GlWorkBench.getLoginUser();
		Boolean ispower = new Boolean(false);
		ispower = VoucherDataBridge.getInstance().isAccsubjPower(voucher, userid);
		if (ispower.booleanValue()) {
			voucher = VoucherDataBridge.getInstance().filterDetailByAccsubjPower(voucher, userid);
		}
		DetailVO[] details = voucher.getDetails();
		if (details != null && details.length > 0)
			for (DetailVO detailVO : details) {
				// ssb.append("分录");
				// ssb.append(detailVO.getDetailindex().toString());
				// ssb.append(" ");
				ssb.append(detailVO.getExplanation());
				ssb.append(" ");
				ssb.append(AccountCache.getInstance().getAccountVOByPK(detailVO.getPk_accountingbook(), detailVO.getPk_accasoa(), detailVO.getPrepareddate().toStdString()).getName());
				ssb.append(" ");
				if (detailVO.getAssid() != null) {
					//ssb.append(nc.ui.glcom.displayformattool.ShowContentCenter.getShowAss(detailVO.getPk_accountingbook(), FreeValueDataCache.getInstance().getAssvosByID(detailVO.getAssid())));
					ssb.append(ShowContentCenter.getShowAss(detailVO.getPk_glorgbook(), detailVO.getPk_accasoa(),voucher.getPrepareddate().toStdString(),detailVO.getAss() != null ? detailVO.getAss() : FreeValueDataCache.getInstance().getAssvosByID(detailVO.getAssid())));
					ssb.append(" ");
				}
				ssb.append(CurrencyDataCache.getInstance().getCurrtypeBypk(detailVO.getPk_currtype()).getName());
				ssb.append(" ");
//				int ilDigit = ((CurrtypeVO) CurrencyDataCache.getInstance().getCurrtypeBypk(GLParaDataCache.getInstance().PkLocalCurr(detailVO.getPk_glorgbook()))).getCurrdigit().intValue();
				
				int[] digitAndRoundtype = nc.itf.fi.pub.Currency.getCurrDigitAndRoundtype(GLParaDataCache.getInstance().PkLocalCurr(detailVO.getPk_glorgbook()));
				
				if (detailVO.getLocaldebitamount().abs().doubleValue() > 0) {
					ssb.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002GL502","UPP2002GL502-000035")/*@res "借方:"*/);
					ssb.append(GlNumberFormat.formatUFDouble(detailVO.getLocaldebitamount().setScale(digitAndRoundtype[0],digitAndRoundtype[1])));
					ssb.append(" ");
				} else {
					// ssb.append("借方:");
					// ssb.append(GlNumberFormat.formatUFDouble(new
					// UFDouble(0).setScale(ilDigit, UFDouble.ROUND_HALF_UP)));
					// ssb.append(" ");
				}
				if (detailVO.getLocalcreditamount().abs().doubleValue() > 0) {
					ssb.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002GL502","UPP2002GL502-000036")/*@res "贷方:"*/);
					ssb.append(GlNumberFormat.formatUFDouble(detailVO.getLocalcreditamount().setScale(digitAndRoundtype[0],digitAndRoundtype[1])));
					ssb.append(" ");
				} else {
					// ssb.append("贷方:");
					// ssb.append(GlNumberFormat.formatUFDouble(new
					// UFDouble(0).setScale(ilDigit, UFDouble.ROUND_HALF_UP)));
					// ssb.append(" ");
				}
				ssb.append("\n");

			}
	}

	private void appendVoucher(VoucherVO voucher, StringBuffer ssb) {
		String glorgbook = new AccountBookUtil().getAccountingBookNameByPk(voucher.getPk_accountingbook());
		ssb.append(glorgbook);
		ssb.append("  ");

		UFDate date = voucher.getPrepareddate();
		if (date != null) {
			ssb.append(date.toString());
			ssb.append("  ");
		}

		if (voucher.getPk_vouchertype() != null) {
			String vouchertype = VoucherTypeDataCache.getInstance().getVtBypkorgbookAndpkvt(voucher.getPk_accountingbook(), voucher.getPk_vouchertype()).getName();
			ssb.append(vouchertype);
			ssb.append("  ");
		}


		Integer no = voucher.getNo();
		if (no != null) {
			ssb.append(no.toString());
		}
		ssb.append("\n");
	}

//	public void tallyCurrent(String flag) throws Exception {
//
//		int[] indexs = getCurrentIndexs();
//
//		if (indexs == null || indexs.length == 0)
//			return;
//
//		String userid = GlWorkBench.getLoginUser();
//		String username = GlWorkBench.getLoginUserName();
//
//		Vector falseresult = new Vector();
//		String[] pks = null;
//		Vector vecpks = new Vector();
//		if (flag.equals(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000022")/*
//																										 * @res
//																										 * "记账"
//																										 */)) {
//			String tmp_year = getVoucherIndexVO(indexs[0]).getYear();
//			String tmp_corp = getVoucherIndexVO(indexs[0]).getPk_corp();
//			for (int i = 0; i < indexs.length; i++) {
//				VoucherIndexVO tempVO = (VoucherIndexVO) m_VoucherLists.elementAt(indexs[i]);
//				if (!tempVO.getYear().equals(tmp_year))
//					throw new nc.vo.gateway60.pub.GlBusinessException(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000490")/*
//																																			 * @res
//																																			 * "不允许跨年度记账。"
//																																			 */);
//				if (!tempVO.getPk_corp().equals(tmp_corp))
//					throw new nc.vo.gateway60.pub.GlBusinessException(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000491")/*
//																																			 * @res
//																																			 * "不允许跨公司记账。"
//																																			 */);
//				if (VoucherDataCenter.isTallyAfterChecked(tempVO.getPk_glorgbook()) && tempVO.getPk_checked() == null) {
//					OperationResultVO rs = new OperationResultVO();
//					rs.m_strPK = tempVO.getPk_voucher();
//					rs.m_intSuccess = 2; // 失败
//					rs.m_strDescription = nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000492")/*
//																														 * @res
//																														 * "该凭证需要审核。"
//																														 */;
//					rs.m_userIdentical = tempVO;
//					falseresult.addElement(rs);
//				} else if (VoucherDataCenter.isRequireCasherSigned(tempVO.getPk_glorgbook()) && tempVO.getSignflag() != null && tempVO.getSignflag().booleanValue() && tempVO.getPk_casher() == null) {
//					OperationResultVO rs = new OperationResultVO();
//					rs.m_strPK = tempVO.getPk_voucher();
//					rs.m_intSuccess = 2; // 失败
//					rs.m_strDescription = nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000493")/*
//																														 * @res
//																														 * "该凭证需要签字。"
//																														 */;
//					rs.m_userIdentical = tempVO;
//					falseresult.addElement(rs);
//				} else if (tempVO.getPk_manager() != null) {
//					OperationResultVO rs = new OperationResultVO();
//					rs.m_strPK = tempVO.getPk_voucher();
//					rs.m_intSuccess = 2; // 失败
//					rs.m_strDescription = nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000494")/*
//																														 * @res
//																														 * "该凭证已记账。"
//																														 */;
//					rs.m_userIdentical = tempVO;
//					falseresult.addElement(rs);
//				} else if (tempVO.getErrmessage() != null) {
//					OperationResultVO rs = new OperationResultVO();
//					rs.m_strPK = tempVO.getPk_voucher();
//					rs.m_intSuccess = 2; // 失败
//					rs.m_strDescription = nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000495")/*
//																														 * @res
//																														 * "该凭证是有错凭证。"
//																														 */;
//					rs.m_userIdentical = tempVO;
//					falseresult.addElement(rs);
//				} else if (tempVO.getDiscardflag().booleanValue()) {
//					OperationResultVO rs = new OperationResultVO();
//					rs.m_strPK = tempVO.getPk_voucher();
//					rs.m_intSuccess = 2; // 失败
//					rs.m_strDescription = nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000496")/*
//																														 * @res
//																														 * "该凭证是作废凭证。"
//																														 */;
//					rs.m_userIdentical = tempVO;
//					falseresult.addElement(rs);
//				} else if (tempVO.getVoucherVO().getVoucherkind().intValue() != 1 && VoucherDataCenter.getGLSettlePeriod(tempVO.getPk_glorgbook()) != null && VoucherDataCenter.getGLSettlePeriod(tempVO.getPk_glorgbook()).length == 2 && VoucherDataCenter.getGLSettlePeriod(tempVO.getPk_glorgbook())[0] != null && VoucherDataCenter.getGLSettlePeriod(tempVO.getPk_glorgbook())[1] != null) {
//					if (tempVO.getYear().compareTo(VoucherDataCenter.getGLSettlePeriod(tempVO.getPk_glorgbook())[0]) < 0 || (tempVO.getYear().compareTo(VoucherDataCenter.getGLSettlePeriod(tempVO.getPk_glorgbook())[0]) == 0 && tempVO.getPeriod().compareTo(VoucherDataCenter.getGLSettlePeriod(tempVO.getPk_glorgbook())[1]) <= 0)) {
//						OperationResultVO rs = new OperationResultVO();
//						rs.m_strPK = tempVO.getPk_voucher();
//						rs.m_intSuccess = 2; // 失败
//						rs.m_strDescription = nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000069")/*
//																															 * @res
//																															 * "#凭证所在期间已结账#"
//																															 */;
//						falseresult.addElement(rs);
//					}
//				} else {
//					vecpks.addElement(tempVO.getPk_voucher());
//				}
//			}
//			if (vecpks.size() > 0) {
//				pks = new String[vecpks.size()];
//				vecpks.copyInto(pks);
//			} else {
//				return;
//			}
//			OperationResultVO[] result = GLPubProxy.getRemoteVoucherTally().tallyVoucherByPks(pks, userid, GlWorkBench.getBusiDate(), new Boolean(true));
//			Vector tmp_successPKs = new Vector();
//			if (result != null && result.length > 0) {
//				HashMap tmp_falsePKs = new HashMap();
//				for (int i = 0; i < result.length; i++) {
//					if (result[i] != null && result[i].m_intSuccess == 2) {
//						falseresult.addElement(result[i]);
//						tmp_falsePKs.put(result[i].m_strPK, result[i]);
//					}
//				}
//				for (int i = 0; i < pks.length; i++) {
//					if (tmp_falsePKs.get(pks[i]) == null) {
//						tmp_successPKs.addElement(pks[i]);
//					}
//				}
//				// getListUI().nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,result.m_userIdentical.toString());
//				// refresh();
//			}
//			pks = new String[tmp_successPKs.size()];
//			tmp_successPKs.copyInto(pks);
//			if (falseresult.size() > 0) {
//				OperationResultVO[] tmp_falseresultvos = new OperationResultVO[falseresult.size()];
//				falseresult.copyInto(tmp_falseresultvos);
//				for (int i = 0; i < tmp_falseresultvos.length; i++) {
//					if (tmp_falseresultvos[i].m_userIdentical == null) {
//						tmp_falseresultvos[i].m_userIdentical = getVoucherIndexVO(tmp_falseresultvos[i].m_strPK);
//					}
//				}
//				getListUI().showResultMessage(tmp_falseresultvos);
//			}
//		} else {
//			for (int i = 0; i < indexs.length; i++) {
//				VoucherIndexVO tempVO = (VoucherIndexVO) m_VoucherLists.elementAt(indexs[i]);
//				if (VoucherDataCenter.isUnTallyable(tempVO.getPk_glorgbook()) && tempVO.getPk_manager() != null && tempVO.getPk_manager().equals(userid) && tempVO.getErrmessage() == null && !tempVO.getDiscardflag().booleanValue()
//						&& (VoucherDataCenter.getGLSettlePeriod(tempVO.getPk_glorgbook())[0] + VoucherDataCenter.getGLSettlePeriod(tempVO.getPk_glorgbook())[1]).compareTo(tempVO.getYear() + tempVO.getPeriod()) < 0)
//					vecpks.addElement(tempVO.getPk_voucher());
//			}
//			pks = new String[vecpks.size()];
//			vecpks.copyInto(pks);
//			GLPubProxy.getRemoteVoucherTally().tallyVoucherByPks(pks, userid, GlWorkBench.getBusiDate(), new Boolean(false));
//		}
//		updateVoucherList(pks, flag.equals(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000022")/*
//																														 * @res
//																														 * "记账"
//																														 */) ? userid : null, VoucherIndexKey.V_PK_MANAGER);
//		updateVoucherList(pks, flag.equals(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000022")/*
//																														 * @res
//																														 * "记账"
//																														 */) ? username : null, VoucherIndexKey.V_MANAGERNAME);
//		firePropertyChange("refresh", null, null);
//		// fireVoucherChange(
//		// VoucherKey.P_VOUCHER,
//		// null,
//		// ((VoucherIndexVO) getCurrentIndexVO()).getVoucherVO());
//	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-12-14 22:10:44)
	 *
	 * @param pks
	 *            java.lang.String[]
	 * @param data
	 *            java.lang.Object
	 * @param iKey
	 *            int
	 */
	public void updateVoucherList(String[] pks, Object data, int iKey) {
		if (pks == null || pks.length == 0)
			return;
		HashMap tmp_hashmap = new HashMap();
		for (int i = 0; i < pks.length; i++) {
			tmp_hashmap.put(pks[i], "");
		}
		for (int i = 0; i < size(); i++) {
			VoucherIndexVO tempIndexVO = getVoucherIndexVO(i);
			if (tmp_hashmap.get(tempIndexVO.getPk_voucher()) != null) {
				tempIndexVO.setValue(iKey, data);
				if (tempIndexVO.getVoucherVO() != null)
					((VoucherVO) tempIndexVO.getVoucherVO()).setValue(iKey, data);
				m_VoucherLists.setElementAt(tempIndexVO, i);
			}
		}
		// firePropertyChange("refresh", null, null);
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-12-14 22:10:44)
	 *
	 * @param vouchers
	 *            java.lang.String[]
	 * @param data
	 *            java.lang.Object
	 * @param iKey
	 *            int
	 */
	public void updateVoucherList(VoucherVO[] vouchers) {
		if (vouchers == null || vouchers.length == 0)
			return;
		HashMap tmp_hashmap = new HashMap();
		for (int i = 0; i < vouchers.length; i++) {
			tmp_hashmap.put(vouchers[i].getPk_voucher(), vouchers[i]);
		}
		for (int i = 0; i < size(); i++) {
			VoucherIndexVO tempIndexVO = getVoucherIndexVO(i);
			VoucherVO voucher = (VoucherVO) tmp_hashmap.get(tempIndexVO.getPk_voucher());
			if (voucher != null) {
				tempIndexVO.setVoucherVO(voucher);
				m_VoucherLists.setElementAt(tempIndexVO, i);
			}
		}
		firePropertyChange("refresh", null, null);
	}

	public Boolean getM_istip() {

		return m_istip;
	}

	public void setM_istip(Boolean m_istip) {
		this.m_istip = m_istip;
	}

	public void refreshByVouchers(VoucherVO[] vouchers) {
		if (vouchers == null || vouchers.length == 0)
			return;
		VoucherIndexVO[] resultVO = new nc.vo.gl.voucherlist.VoucherIndexVO[vouchers.length];
		for (int i = 0; i < vouchers.length; i++) {
			VoucherIndexVO tempvo = new VoucherIndexVO();
			tempvo.setVoucherVO(vouchers[i]);
			tempvo.setVouchertype(VoucherDataCenter.getVouchertypeNameByPk_orgbook(tempvo.getPk_glorgbook(), tempvo.getPk_vouchertype()));
			resultVO[i] = tempvo;
		}
		for (int i = 0; i < resultVO.length; i++) {
			addVoucher(resultVO[i]);
		}
		firePropertyChange("message", null, nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000489"/*
				 * @res
				 * "本次查询共查出符合条件的凭证{0}张，列表中现有凭证{1}张。"
				 		*/,null,new String[]{String.valueOf(m_VoucherLists.size()),String.valueOf(m_VoucherLists.size())}));
		firePropertyChange("refresh", null, null);
	}

	public void setVoucherIndexVO(int index,VoucherIndexVO voucherIndexVO){
		m_VoucherLists.setElementAt(voucherIndexVO,index);
	}

	public void setFuncCode(String funcCode) {
		this.funcCode = funcCode;
	}

	public String getFuncCode() {
		return funcCode;
	}
}