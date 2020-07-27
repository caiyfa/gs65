package nc.gs65.vo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Pattern;

import nc.vo.glcom.ass.AssVO;
import nc.vo.glrp.verify.VerifyDetailVO;
import nc.vo.pub.NullFieldException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.ValidationException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;

/**
 * 参考自nc.vo.gl.pubvoucher.DetailVO
 * 
 * @author CYF
 * 
 */
@SuppressWarnings("serial")
public class DetailVO extends SuperVO {
	private String pk_voucher;
	/** 科目编码 */
	private String accountcode;
	/**
	 * 调整期间
	 * <p>
	 * 取值范围为系统定义的期间或者调整期间
	 * </p>
	 */
	private String adjustperiod;
	/**
	 * 辅助核算标识
	 * <p>
	 * 取值范围:NULL-无辅助核算,或者通过总账系统提供的辅助核算接口取到的ASSID
	 * </p>
	 */
	private String assid;
	/** 银行帐号 */
	private String bankaccount;
	/** 票据类型 */
	private String billtype;
	/** 业务系统协同号 */
	private String busireconno;
	/** 结算日期 */
	private UFDate checkdate; // 结算日期
	/** 结算号 */
	private String checkno; // 结算号
	/**
	 * 对账标志 保留字段
	 */
	private Integer contrastflag;
	/**
	 * 折算标志 取值范围：Y-折算，N-未折算
	 */
	private UFBoolean convertflag;
	/** 原币贷方金额 */
	private UFDouble creditamount; // 原币贷方金额

	/** 贷方数量 */
	private UFDouble creditquantity; // 贷方数量
	/** 原币借方金额 */
	private UFDouble debitamount; // 原币借方金额

	/** 借方数量 */
	private UFDouble debitquantity; // 借方数量

	/** 分录号 */
	private Integer detailindex; // 分录号
	/** 借贷方向 */
	private String direction;
	/**
	 * 作废标志
	 * <p>
	 * 取值范围：Y-作废凭证,N-非作废凭证
	 * </p>
	 */
	private UFBoolean discardflag; // 作废标志
	private Integer dr;
	/** 标错信息 */
	private String errmessage; // 标错信息
	/** 主表标错信息 */
	private String errmessage2; // 标错信息
	/** 标错的历史信息 */
	private String errmessageh;
	/**
	 * 折辅汇率
	 * 
	 * @deprecated
	 */
	private UFDouble excrate1; // 汇率1//折辅汇率
	/** 折本汇率 **/
	private UFDouble excrate2; // 汇率2//折本汇率
	/** 集团本币对应汇率 */
	private UFDouble excrate3; // 汇率3//集团本币对应汇率
	/** 全局本币对应汇率 */
	private UFDouble excrate4; // 汇率4//全局本币对应汇率

	/** 摘要内容 */
	private String explanation; // 摘要内容
	/**
	 * 辅币贷方金额
	 * 
	 * @deprecated
	 */
	private UFDouble fraccreditamount; // 辅币贷方金额

	/**
	 * 辅币借方金额
	 * 
	 * @deprecated
	 */
	private UFDouble fracdebitamount; // 辅币借方金额
	private UFDouble globalcreditamount; // 全局本币贷方金额
	private UFDouble globaldebitamount; // 全局本币借方金额

	private UFDouble groupcreditamount; // 集团本币贷方金额
	private UFDouble groupdebitamount; // 集团本币借方金额
	/** 内部交易业务日期 */
	private String innerbusdate;
	/** 内部交易业务号 */
	private String innerbusno;
	/** 是否差异凭证，主表冗余字段 */
	private UFBoolean isdifflag;
	/** 本币贷方金额 */
	private UFDouble localcreditamount; // 本币贷方金额
	/** 本币借方金额 */
	private UFDouble localdebitamount; // 本币借方金额

	/**
	 * 修改标志（该字段是个长度为16的字符串，其各个字符代表含义如下：） 1）科目编码 2）摘要内容 3）币种编码 4）辅助核算 5）对方科目 6）单价
	 * 7）记账汇率1 8）记账汇率2 9）借方数量 10）借方金额1(原币) 11）借方金额2(辅币) 12）借方金额3(本币) 13）贷方数量
	 * 14）贷方金额1(原币) 15）贷方金额2(辅币) 16）贷方金额3(本币) 每个字段传"Y"表示可改,传"N"表示不可改
	 * */
	private String modifyflag; // 修改标志（该字段是个长度为16的字符串，其各个字符代表含义如下：）

	/** 网银标识 */
	private String netbankflag; // 网银标识
	/** 凭证号 */
	private Integer nov;

	/** 对方科目 */
	private String oppositesubj; // 对方科目
	/**
	 * 会计期间
	 * <p>
	 * 取值范围为系统定义的期间,不包含调整期间信息
	 * </p>
	 */
	private String periodv; // 会计期间
	/** 科目关联主键 */
	private String pk_accasoa; // 科目主键
	/**
	 * 科目主键
	 * 
	 * @since 6.0
	 */
	/** 科目表 */
	private String pk_accchart;
	/** 科目主键 */
	private String pk_account;
	/**
	 * 主体账簿主键
	 */
	private String pk_accountingbook;
	/** 币种主键 */
	private String pk_currtype; // 币种主键
	private String pk_detail;
	private String pk_group;
	/** 内部单位主键 */
	private String pk_innerorg;
	/** 内部账簿主键 */
	private String pk_innersob; // 内部账簿主键
	/** 记账人主键 */
	private String pk_managerv; // 记账人主键
	/** 被冲销的分录主键 */
	private String pk_offerdetail;
	/** 原始组织主键 */
	private String pk_org; // 公司主键
	/** 组织版本，与制单日期对应的版本组织主键 */
	private String pk_org_v;//
	/** 内部客商对方公司pk */
	private String pk_othercorp;
	/** 内部客商对方公司pk_glorgbook */
	private String pk_otherorgbook;
	// 制单人主键
	private String pk_preparedv;
	/** 账簿主键 */
	private String pk_setofbook; // 账簿主键
	/** 源PK */
	private String pk_sourcepk;
	private String pk_systemv;
	/** 原始业务单元 */
	private String pk_unit; // 公司主键
	/** 业务单元版本信息 */
	private String pk_unit_v;
	/** 凭证类别主键 */
	private String pk_vouchertypev; // 凭证类别主键
	
	/** 制单日期 */
	private UFDate prepareddatev; // 制单日期
	
	/** 单价 */
	private UFDouble price; // 单价
	/*
	 * 1）科目编码 2）摘要内容 3）币种编码 4）辅助核算 5）对方科目 6）单价 7）记账汇率1 8）记账汇率2 9）借方数量 10）借方金额1(原币) 11）借方金额2(辅币) 12）借方金额3(本币) 13）贷方数量 14）贷方金额1(原币) 15）贷方金额2(辅币) 16）贷方金额3(本币)
	 */
	/** 单据处理类 */
	private String recieptclass; // 单据处理类
	private String signdatev;
	/** 暂存标志 */
	private UFBoolean tempsaveflag; // 暂存标志
	private UFDateTime ts = null;
	/** 业务单元名称 */
	private String unitname; // 公司主键
	/** 核销日期 */
	private String verifydate; // 预留字段2，扩展用 //已使用，核销日期
	/** 核销号 */
	private String verifyno;
	/** 凭证类型 */
	private Integer voucherkindv; // 凭证类型
	/** 会计年度 */
	private String year; // 会计年度
	private String free1;
	private String free2;
	private String free3;
	private String free4;
	private String free5;
	private String free6;
	private String free7;
	private String free8;
	private String free9;
	private String free10;
	private String free11;
	
	public String getPk_voucher() {
		return pk_voucher;
	}
	public void setPk_voucher(String pk_voucher) {
		this.pk_voucher = pk_voucher;
	}
	public String getAccountcode() {
		return accountcode;
	}
	public void setAccountcode(String accountcode) {
		this.accountcode = accountcode;
	}
	public String getAdjustperiod() {
		return adjustperiod;
	}
	public void setAdjustperiod(String adjustperiod) {
		this.adjustperiod = adjustperiod;
	}
	public String getAssid() {
		return assid;
	}
	public void setAssid(String assid) {
		this.assid = assid;
	}
	public String getBankaccount() {
		return bankaccount;
	}
	public void setBankaccount(String bankaccount) {
		this.bankaccount = bankaccount;
	}
	public String getBilltype() {
		return billtype;
	}
	public void setBilltype(String billtype) {
		this.billtype = billtype;
	}
	public String getBusireconno() {
		return busireconno;
	}
	public void setBusireconno(String busireconno) {
		this.busireconno = busireconno;
	}
	public UFDate getCheckdate() {
		return checkdate;
	}
	public void setCheckdate(UFDate checkdate) {
		this.checkdate = checkdate;
	}
	public String getCheckno() {
		return checkno;
	}
	public void setCheckno(String checkno) {
		this.checkno = checkno;
	}
	public Integer getContrastflag() {
		return contrastflag;
	}
	public void setContrastflag(Integer contrastflag) {
		this.contrastflag = contrastflag;
	}
	public UFBoolean getConvertflag() {
		return convertflag;
	}
	public void setConvertflag(UFBoolean convertflag) {
		this.convertflag = convertflag;
	}
	public UFDouble getCreditamount() {
		return creditamount;
	}
	public void setCreditamount(UFDouble creditamount) {
		this.creditamount = creditamount;
	}
	public UFDouble getCreditquantity() {
		return creditquantity;
	}
	public void setCreditquantity(UFDouble creditquantity) {
		this.creditquantity = creditquantity;
	}
	public UFDouble getDebitamount() {
		return debitamount;
	}
	public void setDebitamount(UFDouble debitamount) {
		this.debitamount = debitamount;
	}
	public UFDouble getDebitquantity() {
		return debitquantity;
	}
	public void setDebitquantity(UFDouble debitquantity) {
		this.debitquantity = debitquantity;
	}
	public Integer getDetailindex() {
		return detailindex;
	}
	public void setDetailindex(Integer detailindex) {
		this.detailindex = detailindex;
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	public UFBoolean getDiscardflag() {
		return discardflag;
	}
	public void setDiscardflag(UFBoolean discardflag) {
		this.discardflag = discardflag;
	}
	public Integer getDr() {
		return dr;
	}
	public void setDr(Integer dr) {
		this.dr = dr;
	}
	public String getErrmessage() {
		return errmessage;
	}
	public void setErrmessage(String errmessage) {
		this.errmessage = errmessage;
	}
	public String getErrmessage2() {
		return errmessage2;
	}
	public void setErrmessage2(String errmessage2) {
		this.errmessage2 = errmessage2;
	}
	public String getErrmessageh() {
		return errmessageh;
	}
	public void setErrmessageh(String errmessageh) {
		this.errmessageh = errmessageh;
	}
	public UFDouble getExcrate1() {
		return excrate1;
	}
	public void setExcrate1(UFDouble excrate1) {
		this.excrate1 = excrate1;
	}
	public UFDouble getExcrate2() {
		return excrate2;
	}
	public void setExcrate2(UFDouble excrate2) {
		this.excrate2 = excrate2;
	}
	public UFDouble getExcrate3() {
		return excrate3;
	}
	public void setExcrate3(UFDouble excrate3) {
		this.excrate3 = excrate3;
	}
	public UFDouble getExcrate4() {
		return excrate4;
	}
	public void setExcrate4(UFDouble excrate4) {
		this.excrate4 = excrate4;
	}
	public String getExplanation() {
		return explanation;
	}
	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}
	public UFDouble getFraccreditamount() {
		return fraccreditamount;
	}
	public void setFraccreditamount(UFDouble fraccreditamount) {
		this.fraccreditamount = fraccreditamount;
	}
	public UFDouble getFracdebitamount() {
		return fracdebitamount;
	}
	public void setFracdebitamount(UFDouble fracdebitamount) {
		this.fracdebitamount = fracdebitamount;
	}
	public UFDouble getGlobalcreditamount() {
		return globalcreditamount;
	}
	public void setGlobalcreditamount(UFDouble globalcreditamount) {
		this.globalcreditamount = globalcreditamount;
	}
	public UFDouble getGlobaldebitamount() {
		return globaldebitamount;
	}
	public void setGlobaldebitamount(UFDouble globaldebitamount) {
		this.globaldebitamount = globaldebitamount;
	}
	public UFDouble getGroupcreditamount() {
		return groupcreditamount;
	}
	public void setGroupcreditamount(UFDouble groupcreditamount) {
		this.groupcreditamount = groupcreditamount;
	}
	public UFDouble getGroupdebitamount() {
		return groupdebitamount;
	}
	public void setGroupdebitamount(UFDouble groupdebitamount) {
		this.groupdebitamount = groupdebitamount;
	}
	public String getInnerbusdate() {
		return innerbusdate;
	}
	public void setInnerbusdate(String innerbusdate) {
		this.innerbusdate = innerbusdate;
	}
	public String getInnerbusno() {
		return innerbusno;
	}
	public void setInnerbusno(String innerbusno) {
		this.innerbusno = innerbusno;
	}
	public UFBoolean getIsdifflag() {
		return isdifflag;
	}
	public void setIsdifflag(UFBoolean isdifflag) {
		this.isdifflag = isdifflag;
	}
	public UFDouble getLocalcreditamount() {
		return localcreditamount;
	}
	public void setLocalcreditamount(UFDouble localcreditamount) {
		this.localcreditamount = localcreditamount;
	}
	public UFDouble getLocaldebitamount() {
		return localdebitamount;
	}
	public void setLocaldebitamount(UFDouble localdebitamount) {
		this.localdebitamount = localdebitamount;
	}
	public String getModifyflag() {
		return modifyflag;
	}
	public void setModifyflag(String modifyflag) {
		this.modifyflag = modifyflag;
	}
	public String getNetbankflag() {
		return netbankflag;
	}
	public void setNetbankflag(String netbankflag) {
		this.netbankflag = netbankflag;
	}
	public Integer getNov() {
		return nov;
	}
	public void setNov(Integer nov) {
		this.nov = nov;
	}
	public String getOppositesubj() {
		return oppositesubj;
	}
	public void setOppositesubj(String oppositesubj) {
		this.oppositesubj = oppositesubj;
	}
	public String getPeriodv() {
		return periodv;
	}
	public void setPeriodv(String periodv) {
		this.periodv = periodv;
	}
	public String getPk_accasoa() {
		return pk_accasoa;
	}
	public void setPk_accasoa(String pk_accasoa) {
		this.pk_accasoa = pk_accasoa;
	}
	public String getPk_accchart() {
		return pk_accchart;
	}
	public void setPk_accchart(String pk_accchart) {
		this.pk_accchart = pk_accchart;
	}
	public String getPk_account() {
		return pk_account;
	}
	public void setPk_account(String pk_account) {
		this.pk_account = pk_account;
	}
	public String getPk_accountingbook() {
		return pk_accountingbook;
	}
	public void setPk_accountingbook(String pk_accountingbook) {
		this.pk_accountingbook = pk_accountingbook;
	}
	public String getPk_currtype() {
		return pk_currtype;
	}
	public void setPk_currtype(String pk_currtype) {
		this.pk_currtype = pk_currtype;
	}
	public String getPk_detail() {
		return pk_detail;
	}
	public void setPk_detail(String pk_detail) {
		this.pk_detail = pk_detail;
	}
	public String getPk_group() {
		return pk_group;
	}
	public void setPk_group(String pk_group) {
		this.pk_group = pk_group;
	}
	public String getPk_innerorg() {
		return pk_innerorg;
	}
	public void setPk_innerorg(String pk_innerorg) {
		this.pk_innerorg = pk_innerorg;
	}
	public String getPk_innersob() {
		return pk_innersob;
	}
	public void setPk_innersob(String pk_innersob) {
		this.pk_innersob = pk_innersob;
	}
	public String getPk_managerv() {
		return pk_managerv;
	}
	public void setPk_managerv(String pk_managerv) {
		this.pk_managerv = pk_managerv;
	}
	public String getPk_offerdetail() {
		return pk_offerdetail;
	}
	public void setPk_offerdetail(String pk_offerdetail) {
		this.pk_offerdetail = pk_offerdetail;
	}
	public String getPk_org() {
		return pk_org;
	}
	public void setPk_org(String pk_org) {
		this.pk_org = pk_org;
	}
	public String getPk_org_v() {
		return pk_org_v;
	}
	public void setPk_org_v(String pk_org_v) {
		this.pk_org_v = pk_org_v;
	}
	public String getPk_othercorp() {
		return pk_othercorp;
	}
	public void setPk_othercorp(String pk_othercorp) {
		this.pk_othercorp = pk_othercorp;
	}
	public String getPk_otherorgbook() {
		return pk_otherorgbook;
	}
	public void setPk_otherorgbook(String pk_otherorgbook) {
		this.pk_otherorgbook = pk_otherorgbook;
	}
	public String getPk_preparedv() {
		return pk_preparedv;
	}
	public void setPk_preparedv(String pk_preparedv) {
		this.pk_preparedv = pk_preparedv;
	}
	public String getPk_setofbook() {
		return pk_setofbook;
	}
	public void setPk_setofbook(String pk_setofbook) {
		this.pk_setofbook = pk_setofbook;
	}
	public String getPk_sourcepk() {
		return pk_sourcepk;
	}
	public void setPk_sourcepk(String pk_sourcepk) {
		this.pk_sourcepk = pk_sourcepk;
	}
	public String getPk_systemv() {
		return pk_systemv;
	}
	public void setPk_systemv(String pk_systemv) {
		this.pk_systemv = pk_systemv;
	}
	public String getPk_unit() {
		return pk_unit;
	}
	public void setPk_unit(String pk_unit) {
		this.pk_unit = pk_unit;
	}
	public String getPk_unit_v() {
		return pk_unit_v;
	}
	public void setPk_unit_v(String pk_unit_v) {
		this.pk_unit_v = pk_unit_v;
	}
	public String getPk_vouchertypev() {
		return pk_vouchertypev;
	}
	public void setPk_vouchertypev(String pk_vouchertypev) {
		this.pk_vouchertypev = pk_vouchertypev;
	}
	public UFDate getPrepareddatev() {
		return prepareddatev;
	}
	public void setPrepareddatev(UFDate prepareddatev) {
		this.prepareddatev = prepareddatev;
	}
	public UFDouble getPrice() {
		return price;
	}
	public void setPrice(UFDouble price) {
		this.price = price;
	}
	public String getRecieptclass() {
		return recieptclass;
	}
	public void setRecieptclass(String recieptclass) {
		this.recieptclass = recieptclass;
	}
	public String getSigndatev() {
		return signdatev;
	}
	public void setSigndatev(String signdatev) {
		this.signdatev = signdatev;
	}
	public UFBoolean getTempsaveflag() {
		return tempsaveflag;
	}
	public void setTempsaveflag(UFBoolean tempsaveflag) {
		this.tempsaveflag = tempsaveflag;
	}
	public UFDateTime getTs() {
		return ts;
	}
	public void setTs(UFDateTime ts) {
		this.ts = ts;
	}
	public String getUnitname() {
		return unitname;
	}
	public void setUnitname(String unitname) {
		this.unitname = unitname;
	}
	public String getVerifydate() {
		return verifydate;
	}
	public void setVerifydate(String verifydate) {
		this.verifydate = verifydate;
	}
	public String getVerifyno() {
		return verifyno;
	}
	public void setVerifyno(String verifyno) {
		this.verifyno = verifyno;
	}
	public Integer getVoucherkindv() {
		return voucherkindv;
	}
	public void setVoucherkindv(Integer voucherkindv) {
		this.voucherkindv = voucherkindv;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getFree1() {
		return free1;
	}
	public void setFree1(String free1) {
		this.free1 = free1;
	}
	public String getFree2() {
		return free2;
	}
	public void setFree2(String free2) {
		this.free2 = free2;
	}
	public String getFree3() {
		return free3;
	}
	public void setFree3(String free3) {
		this.free3 = free3;
	}
	public String getFree4() {
		return free4;
	}
	public void setFree4(String free4) {
		this.free4 = free4;
	}
	public String getFree5() {
		return free5;
	}
	public void setFree5(String free5) {
		this.free5 = free5;
	}
	public String getFree6() {
		return free6;
	}
	public void setFree6(String free6) {
		this.free6 = free6;
	}
	public String getFree7() {
		return free7;
	}
	public void setFree7(String free7) {
		this.free7 = free7;
	}
	public String getFree8() {
		return free8;
	}
	public void setFree8(String free8) {
		this.free8 = free8;
	}
	public String getFree9() {
		return free9;
	}
	public void setFree9(String free9) {
		this.free9 = free9;
	}
	public String getFree10() {
		return free10;
	}
	public void setFree10(String free10) {
		this.free10 = free10;
	}
	public String getFree11() {
		return free11;
	}
	public void setFree11(String free11) {
		this.free11 = free11;
	}
	@Override
	public String getPKFieldName() {
		// TODO Auto-generated method stub
		return "pk_detail";
	}
	@Override
	public String getTableName() {
		// TODO Auto-generated method stub
		return "gl_detail";
	}

}
