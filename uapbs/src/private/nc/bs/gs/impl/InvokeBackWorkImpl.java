package nc.bs.gs.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.gs65.backgroundwork.GLRecoWithHFMBackWork;
import nc.bs.gs65.backgroundwork.MakeVoucherAction;
import nc.bs.gs65.backgroundwork.MakeVoucherFromMidTablePlugin;
import nc.gs.itf.ItfInvokeBackWord;
import nc.plugin.uapbd.ht.sync.HtOriginalDataTofaExcelPlugin;
import nc.vo.pub.pa.CurrEnvVO;

public class InvokeBackWorkImpl implements ItfInvokeBackWord {

	@Override
	public String invokeAction(String action) throws Exception {
		try{
			InvocationInfoProxy.getInstance().setUserDataSource("SET01");
			GLRecoWithHFMBackWork work=new GLRecoWithHFMBackWork();
			
			CurrEnvVO bgwc=new CurrEnvVO();
			bgwc.getKeyMap().put("subject", "1002");
			bgwc.getKeyMap().put("checkType", "0024");
			bgwc.getKeyMap().put("from", "2020-06-24");
			work.executeTask(bgwc);
			
			
			
			/*MakeVoucherFromMidTablePlugin plugin=new MakeVoucherFromMidTablePlugin();
			plugin.executeTask(bgwc);*/
			
			/*BufferedReader reader =new  BufferedReader(new InputStreamReader(new FileInputStream(new File("C:\\voucher\\voucher1.xml"))));
			
			StringBuffer buffer=new StringBuffer();
			String tmp=null;
			while((tmp=reader.readLine())!=null){
				buffer.append(tmp);
			}
			reader.close();
			
			
			MakeVoucherAction voucherAction=new MakeVoucherAction();
			voucherAction.doSendMessage(buffer.toString());*/
			
			/*HtOriginalDataTofaExcelPlugin plugin=new HtOriginalDataTofaExcelPlugin();
			plugin.executeTask(new CurrEnvVO());*/
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return "action"+action;
	}

}
