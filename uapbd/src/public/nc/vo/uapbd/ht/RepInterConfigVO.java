package nc.vo.uapbd.ht;

import nc.vo.pub.SuperVO;

public class RepInterConfigVO extends SuperVO{
	
	private String pk_interconfig; 
	private String rptcode; 
	private String rptname; 
	private String midtablename; 
	private String fileprefix; 
	private String def1; 
	private String def2; 
	private String def3; 
	private String def4; 
	private String def5; 
	private String def6; 
	private String def7; 
	private String def8; 
	private String def9; 
	private String def10; 	
	private Integer dr;
	private String ts;
	
	private static String PK_INTERCONFIG="pk_interconfig"; 
	private static String RPTCODE ="rptcode"; 
	private static String RPTNAME="rptname"; 
	private static String MIDTABLENAME="midtablename"; 
	private static String FILEPREFIX="fileprefix"; 	
	
	public String getPk_interconfig() {
		return pk_interconfig;
	}
	public void setPk_interconfig(String pk_interconfig) {
		this.pk_interconfig = pk_interconfig;
	}
	public String getRptcode() {
		return rptcode;
	}
	public void setRptcode(String rptcode) {
		this.rptcode = rptcode;
	}
	public String getRptname() {
		return rptname;
	}
	public void setRptname(String rptname) {
		this.rptname = rptname;
	}
	public String getMidtablename() {
		return midtablename;
	}
	public void setMidtablename(String midtablename) {
		this.midtablename = midtablename;
	}
	public String getFileprefix() {
		return fileprefix;
	}
	public void setFileprefix(String fileprefix) {
		this.fileprefix = fileprefix;
	}
	public String getDef1() {
		return def1;
	}
	public void setDef1(String def1) {
		this.def1 = def1;
	}
	public String getDef2() {
		return def2;
	}
	public void setDef2(String def2) {
		this.def2 = def2;
	}
	public String getDef3() {
		return def3;
	}
	public void setDef3(String def3) {
		this.def3 = def3;
	}
	public String getDef4() {
		return def4;
	}
	public void setDef4(String def4) {
		this.def4 = def4;
	}
	public String getDef5() {
		return def5;
	}
	public void setDef5(String def5) {
		this.def5 = def5;
	}
	public String getDef6() {
		return def6;
	}
	public void setDef6(String def6) {
		this.def6 = def6;
	}
	public String getDef7() {
		return def7;
	}
	public void setDef7(String def7) {
		this.def7 = def7;
	}
	public String getDef8() {
		return def8;
	}
	public void setDef8(String def8) {
		this.def8 = def8;
	}
	public String getDef9() {
		return def9;
	}
	public void setDef9(String def9) {
		this.def9 = def9;
	}
	public String getDef10() {
		return def10;
	}
	public void setDef10(String def10) {
		this.def10 = def10;
	}
	public Integer getDr() {
		return dr;
	}
	public void setDr(Integer dr) {
		this.dr = dr;
	}
	public String getTs() {
		return ts;
	}
	public void setTs(String ts) {
		this.ts = ts;
	} 	
	@Override
	public String getPKFieldName() {
		return "pk_interconfig";
	}	
	@Override
	public String getTableName() {
		return "rep_interconfig";
	}
}
