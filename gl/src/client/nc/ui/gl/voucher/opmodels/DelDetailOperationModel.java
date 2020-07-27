package nc.ui.gl.voucher.opmodels;

import javax.swing.JComponent;

import nc.bs.framework.common.NCLocator;
import nc.itf.uap.IUAPQueryBS;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.ui.gl.vouchercard.IVoucherModel;
import nc.vo.gateway60.pub.GlBusinessException;
import nc.vo.gl.pubvoucher.DetailVO;
import nc.vo.gl.pubvoucher.VoucherVO;
import nc.vo.pub.BusinessException;

/**
 * 删除分录
 */
public class DelDetailOperationModel extends nc.ui.gl.vouchermodels.AbstractOperationModel {
    /**
     * 此处插入方法说明。 创建日期：(2002-6-26 14:27:28)
     *
     * @return java.lang.Object
     */
    public Object doOperation() {
        Boolean isInSum = (Boolean) getMasterModel().getParameter("isInSumMode");
        if (isInSum != null && isInSum.booleanValue())
        {
            return null;
        }
        getMasterModel().setParameter("stopediting", null);
        VoucherVO voucher = (VoucherVO) getMasterModel().getParameter("vouchervo");
        voucher = (VoucherVO) voucher.clone();
        if(voucher.getPk_accountingbook() == null){
			nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage((JComponent)getMasterModel().getUI(), nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("glpub_0","02002003-0102")/*@res "提示"*/, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("glpub_0","02002003-0193")/*@res "请选择核算账簿。"*/);
			return null;
		}
        if (voucher == null || voucher.getDiscardflag().booleanValue() || voucher.getPk_casher() != null || voucher.getPk_checked() != null || voucher.getPk_manager() != null || voucher.getDetailmodflag() == null || !voucher.getDetailmodflag().booleanValue())
            throw new GlBusinessException(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000135")/* @res "凭证本身禁止增、删、改分录。" */);
        int[] selectedindexes = (int[]) getMasterModel().getParameter("selectedindexes");
        if (selectedindexes == null || selectedindexes.length == 0)
            return null;
        for (int i = 0; i < selectedindexes.length; i++)
        {
            if (voucher.getDetail(selectedindexes[i] - i).getIsmatched() != null && voucher.getDetail(selectedindexes[i] - i).getIsmatched().booleanValue())
                throw new GlBusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55","UPP2002gl55-000356",null,new String[]{String.valueOf(selectedindexes[i]+1)})/*@res "分录已对账或者已协同：：第{0}条分录。" */);
            //CYF 校验分录是否可以删除
            String pk_detail=voucher.getDetail(selectedindexes[i] - i).getPk_detail();
            checkDetailDeleteAble(pk_detail);
            // CYF add end  
            voucher.deleteDetail(selectedindexes[i] - i);
        }
        DetailVO[] details = voucher.getDetails();
        if (selectedindexes[0] > 0) {
        	((IVoucherModel)getMasterModel()).setSelectedIndex( new int[] { selectedindexes[0] - 1 },true);
        }
        else
        {
            if (details.length > 1) {
            	((IVoucherModel)getMasterModel()).setSelectedIndex(new int[] { 0 },true);
            }
            else {
            	((IVoucherModel)getMasterModel()).setSelectedIndex(null,true);
            }
        }
        getMasterModel().setParameter("details", details);
        if (selectedindexes[0] > 0) {
        	((IVoucherModel)getMasterModel()).setSelectedIndex(new int[] { selectedindexes[0] - 1 },true);
        }
        else
        {
            if (details.length > 1)
            	((IVoucherModel)getMasterModel()).setSelectedIndex(new int[] { 0 },true);
            else
            	((IVoucherModel)getMasterModel()).setSelectedIndex(null,true);
        }
        return null;
    }
    private void checkDetailDeleteAble(String pk_detail)throws GlBusinessException{
    try {
	Integer contrastflag=	(Integer) NCLocator.getInstance().lookup(IUAPQueryBS.class).executeQuery("select contrastflag  from gl_detail where pk_detail='"+pk_detail+"'", new ColumnProcessor());
	if(contrastflag!=null&&contrastflag==1){
		throw new GlBusinessException("该条分录正在对账中。请勿删除！");
	}
    } catch (BusinessException e) {
		throw new GlBusinessException(e);
	}
    }
}