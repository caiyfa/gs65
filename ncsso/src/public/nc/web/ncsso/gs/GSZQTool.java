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
	 * 重定向地址的我们的地址地址
	 */
	private static final String Redirect_URI = "SSO01";
	/**
	 * 注册在SSO系统里的对应我们系统的id
	 */
	private static final String Client_ID = "SSO02";
	/**
	 * 注册在SSO系统的密钥
	 */
	private static final String Client_Secret = "SSO03";
	/**
	 * 检测心跳地址
	 */
	private static final String Check_SSO_Method_URI = "SSO04";
	/**
	 * 根据跳转过来的code 获取令牌地址
	 */
	private static final String Access_Token_URI = "SSO05";
	/**
	 * 根据令牌获取账户信息
	 */
	private static final String Profile_URI = "SSO06";

	/**
	 * 未获取到code跳转回单点登录页面
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
