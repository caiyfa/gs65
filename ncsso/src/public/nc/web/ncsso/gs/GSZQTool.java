package nc.web.ncsso.gs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import nc.bs.framework.common.NCLocator;
import nc.itf.uap.IUAPQueryBS;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.vo.pub.BusinessException;
import nc.web.ncsso.Tools;

@SuppressWarnings("restriction")
public class GSZQTool extends Tools {
	/**
	 * �ض����ַ�����ǵĵ�ַ��ַ
	 */
	private static final String Redirect_URI = "SSO01";
	/**
	 * ע����SSOϵͳ��Ķ�Ӧ����ϵͳ��id
	 */
	private static final String Client_ID = "SSO02";
	/**
	 * ע����SSOϵͳ����Կ
	 */
	private static final String Client_Secret = "SSO03";
	/**
	 * ���������ַ
	 */
	private static final String Check_SSO_Method_URI = "SSO04";
	/**
	 * ������ת������code ��ȡ���Ƶ�ַ
	 */
	private static final String Access_Token_URI = "SSO05";
	/**
	 * �������ƻ�ȡ�˻���Ϣ
	 */
	private static final String Profile_URI = "SSO06";

	/**
	 * δ��ȡ��code��ת�ص����¼ҳ��
	 */
	private static final String authorize = "SSO07";
	private static Map<String, String> configMap = null;

	@SuppressWarnings("unchecked")
	public static void initConfig() throws BusinessException {
		if (configMap != null) {
			return;
		}
		configMap = new HashMap<String, String>();
		List list = (List) getQueryService().executeQuery(
						"select initcode,defaultvalue from pub_sysinittemp where initcode like 'SSO%'",
						new MapListProcessor());

		for (Object obj : list) {
			Map<String, String> map = (Map<String, String>) obj;
			configMap.put(map.get("initcode"), map.get("defaultvalue"));
		}
	}

	private static IUAPQueryBS queryService = null;

	private static IUAPQueryBS getQueryService() {
		if (queryService == null) {
			queryService = NCLocator.getInstance().lookup(IUAPQueryBS.class);
		}
		return queryService;
	}

	public GSZQTool(HttpServletRequest req) {
		super(req);
	}

	@Override
	public String getUserName() {
		String authCode = req.getParameter("code");
		String target_uri = req.getParameter("target_uri");
		if (authCode == null || target_uri == null) {
			return null;
		}
		return null;
	}

	@Override
	public boolean isMatch() throws BusinessException {
		return super.isMatch();
	}

	@Override
	public String getNcUrl() {
		return super.getNcUrl();
	}

}
