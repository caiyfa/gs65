package nc.impl.uap.rbac.userpassword;

import nc.itf.uap.rbac.userpassword.IUserPasswordChecker;
import nc.vo.pub.BusinessException;
import nc.vo.sm.UserVO;
import nc.vo.uap.rbac.userpassword.PasswordSecurityLevelVO;

/**
 * 密码级别校验服务实现类
 * 
 * @author ewei 2007-10-29
 */
public class UserPasswordCheckerImpl implements IUserPasswordChecker {
		
	/*
	 * private void checkDefaultPwd(PasswordSecurityLevelVO pwdLevel, String
	 * sPassword) throws BusinessException {
	 * 
	 * IUserPasswordLevel iaQuery = (IUserPasswordLevel) NCLocator
	 * .getInstance().lookup(IUserPasswordLevel.class.getName());
	 * PasswordSecurityConfig pwdConf = null; try { pwdConf =
	 * iaQuery.getPasswordSecurityConfig(); } catch (Exception e) {
	 * Logger.error(e.getMessage(), e); } for (int i = 0; i <
	 * pwdConf.getPasswordlevel().length; i++) { String defaultpwd =
	 * pwdConf.getPasswordlevel()[i].getDefaultPassword(); if (defaultpwd !=
	 * null && defaultpwd.equals(sPassword)) throw new
	 * RbacException(NCLangResOnserver.getInstance() .getStrByID("smcomm",
	 * "UPP1005-000281")"请不要使用默认密码"); } }
	 */

	



	

	public void checkNewpassword(UserVO user, String sPassword,
			PasswordSecurityLevelVO pwdLevel, int usertype)
			throws BusinessException {
	}

	

	public void updateUPVO(String uid, String password,
			PasswordSecurityLevelVO pwdLevel) throws BusinessException {

	}



	@Override
	public String getValidateTip(String startTime,
			PasswordSecurityLevelVO pwdLevel) {
		return null;
	}



	@Override
	public String getPwdCheckMsg(UserVO user, PasswordSecurityLevelVO pwdLevel,
			String implicitPwd) {
		return "ok";
	}

}
