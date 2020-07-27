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
 * �ο���nc.vo.gl.pubvoucher.DetailVO
 * 
 * @author CYF
 * 
 */
@SuppressWarnings("serial")
public class DetailVO extends SuperVO {
	private String pk_voucher;
	/** ��Ŀ���� */
	private String accountcode;
	/**
	 * �����ڼ�
	 * <p>
	 * ȡֵ��ΧΪϵͳ������ڼ���ߵ����ڼ�
	 * </p>
	 */
	private String adjustperiod;
	/**
	 * ���������ʶ
	 * <p>
	 * ȡֵ��Χ:NULL-�޸�������,����ͨ������ϵͳ�ṩ�ĸ�������ӿ�ȡ����ASSID
	 * </p>
	 */
	private String assid;
	/** �����ʺ� */
	private String bankaccount;
	/** Ʊ������ */
	private String billtype;
	/** ҵ��ϵͳЭͬ�� */
	private String busireconno;
	/** �������� */
	private UFDate checkdate; // ��������
	/** ����� */
	private String checkno; // �����
	/**
	 * ���˱�־ �����ֶ�
	 */
	private Integer contrastflag;
	/**
	 * �����־ ȡֵ��Χ��Y-���㣬N-δ����
	 */
	private UFBoolean convertflag;
	/** ԭ�Ҵ������ */
	private UFDouble creditamount; // ԭ�Ҵ������

	/** �������� */
	private UFDouble creditquantity; // ��������
	/** ԭ�ҽ跽��� */
	private UFDouble debitamount; // ԭ�ҽ跽���

	/** �跽���� */
	private UFDouble debitquantity; // �跽����

	/** ��¼�� */
	private Integer detailindex; // ��¼��
	/** ������� */
	private String direction;
	/**
	 * ���ϱ�־
	 * <p>
	 * ȡֵ��Χ��Y-����ƾ֤,N-������ƾ֤
	 * </p>
	 */
	private UFBoolean discardflag; // ���ϱ�־
	private Integer dr;
	/** �����Ϣ */
	private String errmessage; // �����Ϣ
	/** ��������Ϣ */
	private String errmessage2; // �����Ϣ
	/** ������ʷ��Ϣ */
	private String errmessageh;
	/**
	 * �۸�����
	 * 
	 * @deprecated
	 */
	private UFDouble excrate1; // ����1//�۸�����
	/** �۱����� **/
	private UFDouble excrate2; // ����2//�۱�����
	/** ���ű��Ҷ�Ӧ���� */
	private UFDouble excrate3; // ����3//���ű��Ҷ�Ӧ����
	/** ȫ�ֱ��Ҷ�Ӧ���� */
	private UFDouble excrate4; // ����4//ȫ�ֱ��Ҷ�Ӧ����

	/** ժҪ���� */
	private String explanation; // ժҪ����
	/**
	 * ���Ҵ������
	 * 
	 * @deprecated
	 */
	private UFDouble fraccreditamount; // ���Ҵ������

	/**
	 * ���ҽ跽���
	 * 
	 * @deprecated
	 */
	private UFDouble fracdebitamount; // ���ҽ跽���
	private UFDouble globalcreditamount; // ȫ�ֱ��Ҵ������
	private UFDouble globaldebitamount; // ȫ�ֱ��ҽ跽���

	private UFDouble groupcreditamount; // ���ű��Ҵ������
	private UFDouble groupdebitamount; // ���ű��ҽ跽���
	/** �ڲ�����ҵ������ */
	private String innerbusdate;
	/** �ڲ�����ҵ��� */
	private String innerbusno;
	/** �Ƿ����ƾ֤�����������ֶ� */
	private UFBoolean isdifflag;
	/** ���Ҵ������ */
	private UFDouble localcreditamount; // ���Ҵ������
	/** ���ҽ跽��� */
	private UFDouble localdebitamount; // ���ҽ跽���

	/**
	 * �޸ı�־�����ֶ��Ǹ�����Ϊ16���ַ�����������ַ����������£��� 1����Ŀ���� 2��ժҪ���� 3�����ֱ��� 4���������� 5���Է���Ŀ 6������
	 * 7�����˻���1 8�����˻���2 9���跽���� 10���跽���1(ԭ��) 11���跽���2(����) 12���跽���3(����) 13����������
	 * 14���������1(ԭ��) 15���������2(����) 16���������3(����) ÿ���ֶδ�"Y"��ʾ�ɸ�,��"N"��ʾ���ɸ�
	 * */
	private String modifyflag; // �޸ı�־�����ֶ��Ǹ�����Ϊ16���ַ�����������ַ����������£���

	/** ������ʶ */
	private String netbankflag; // ������ʶ
	/** ƾ֤�� */
	private Integer nov;

	/** �Է���Ŀ */
	private String oppositesubj; // �Է���Ŀ
	/**
	 * ����ڼ�
	 * <p>
	 * ȡֵ��ΧΪϵͳ������ڼ�,�����������ڼ���Ϣ
	 * </p>
	 */
	private String periodv; // ����ڼ�
	/** ��Ŀ�������� */
	private String pk_accasoa; // ��Ŀ����
	/**
	 * ��Ŀ����
	 * 
	 * @since 6.0
	 */
	/** ��Ŀ�� */
	private String pk_accchart;
	/** ��Ŀ���� */
	private String pk_account;
	/**
	 * �����˲�����
	 */
	private String pk_accountingbook;
	/** �������� */
	private String pk_currtype; // ��������
	private String pk_detail;
	private String pk_group;
	/** �ڲ���λ���� */
	private String pk_innerorg;
	/** �ڲ��˲����� */
	private String pk_innersob; // �ڲ��˲�����
	/** ���������� */
	private String pk_managerv; // ����������
	/** �������ķ�¼���� */
	private String pk_offerdetail;
	/** ԭʼ��֯���� */
	private String pk_org; // ��˾����
	/** ��֯�汾�����Ƶ����ڶ�Ӧ�İ汾��֯���� */
	private String pk_org_v;//
	/** �ڲ����̶Է���˾pk */
	private String pk_othercorp;
	/** �ڲ����̶Է���˾pk_glorgbook */
	private String pk_otherorgbook;
	// �Ƶ�������
	private String pk_preparedv;
	/** �˲����� */
	private String pk_setofbook; // �˲�����
	/** ԴPK */
	private String pk_sourcepk;
	private String pk_systemv;
	/** ԭʼҵ��Ԫ */
	private String pk_unit; // ��˾����
	/** ҵ��Ԫ�汾��Ϣ */
	private String pk_unit_v;
	/** ƾ֤������� */
	private String pk_vouchertypev; // ƾ֤�������
	
	/** �Ƶ����� */
	private UFDate prepareddatev; // �Ƶ�����
	
	/** ���� */
	private UFDouble price; // ����
	/*
	 * 1����Ŀ���� 2��ժҪ���� 3�����ֱ��� 4���������� 5���Է���Ŀ 6������ 7�����˻���1 8�����˻���2 9���跽���� 10���跽���1(ԭ��) 11���跽���2(����) 12���跽���3(����) 13���������� 14���������1(ԭ��) 15���������2(����) 16���������3(����)
	 */
	/** ���ݴ����� */
	private String recieptclass; // ���ݴ�����
	private String signdatev;
	/** �ݴ��־ */
	private UFBoolean tempsaveflag; // �ݴ��־
	private UFDateTime ts = null;
	/** ҵ��Ԫ���� */
	private String unitname; // ��˾����
	/** �������� */
	private String verifydate; // Ԥ���ֶ�2����չ�� //��ʹ�ã���������
	/** ������ */
	private String verifyno;
	/** ƾ֤���� */
	private Integer voucherkindv; // ƾ֤����
	/** ������ */
	private String year; // ������
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
