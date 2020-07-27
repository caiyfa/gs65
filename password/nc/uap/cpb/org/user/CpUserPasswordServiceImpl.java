package nc.uap.cpb.org.user;

import java.util.HashMap;
import java.util.List;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.uap.rbac.MiddleEntityPersister;
import nc.itf.uap.rbac.IUserExService;
import nc.itf.uap.rbac.IUserLockService;
import nc.itf.uap.rbac.IUserManage;
import nc.itf.uap.rbac.IUserManageQuery;
import nc.itf.uap.rbac.userpassword.IUserPasswordChecker;
import nc.itf.uap.rbac.userpassword.IUserPasswordManage;
import nc.uap.cpb.baseservice.util.BDPKLockUtil;
import nc.uap.cpb.log.CpLogger;
import nc.uap.cpb.org.itf.ICpUserPasswordService;
import nc.uap.cpb.org.vos.CpUserVO;
import nc.uap.cpb.persist.dao.PtBaseDAO;
import nc.uap.lfw.core.exception.LfwRuntimeException;
import nc.vo.org.GroupVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.sm.UserVO;
import nc.vo.uap.rbac.UserShareVO;
import nc.vo.uap.rbac.userpassword.PasswordSecurityLevelFinder;
import nc.vo.uap.rbac.userpassword.PasswordSecurityLevelVO;
import nc.vo.uap.rbac.userpassword.UserPasswordVO;
import nc.vo.uap.rbac.util.RbacUserPwdUtil;
import nc.vo.uap.rbac.util.UserExManageUtil;
import nc.vo.util.innercode.RandomSeqUtil;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import uap.lfw.core.locator.ServiceLocator;
import uap.lfw.core.ml.LfwResBundle;
import uap.web.bd.pub.CpSqlTranslateUtil;

public class CpUserPasswordServiceImpl implements ICpUserPasswordService {

	@Override
	public String resetUserPassWord(String userId) {
		IUserPasswordManage passMgr = NCLocator.getInstance().lookup(IUserPasswordManage.class);
		try {
			return passMgr.resetUserPassWord(userId);
		} 
		catch (BusinessException e) {
			CpLogger.error(e);
			throw new LfwRuntimeException(LfwResBundle.getInstance().getStrByID("ad", "CpUserPasswordServiceImpl-000000")/*重置用户密码时出错*/, e.getMessage());
		}
	}

	@Override
	public String getEncodedPassword(CpUserVO cpUserVO, String expresslyPWD) {
		if(cpUserVO == null || StringUtils.isBlank(cpUserVO.getPrimaryKey()))
			throw new LfwRuntimeException("illegal arguments");
		
		UserVO userVO = new UserVO();
		try {
			BeanUtils.copyProperties(userVO, cpUserVO);
			String codecPWD = null;
			codecPWD = RbacUserPwdUtil.getEncodedPassword(userVO, expresslyPWD);
			return codecPWD;
		} catch (Exception e) {
			CpLogger.error(e.getMessage(), e);
			throw new LfwRuntimeException(e.getMessage());
		} 
	}

	@Override
	public String getUserDefaultPassword(String pk_group) throws BusinessException {
		IUserManageQuery userQry = NCLocator.getInstance().lookup(IUserManageQuery.class);
		return userQry.getUserDefaultPassword(pk_group);
	}

	@Override
	public void updateNcUserPassword(CpUserVO cpUserVO, String inputOldPwd,
			String inputNewPwd) throws BusinessException {

	}
	
	/**
	 * 修改密码时按照密码等级要求校验新密码
	 */
	@Override
	public void checkPwdLevel(CpUserVO cpUserVO)throws BusinessException{
	}

	@Override
	public String getResetCpUserPassWord(CpUserVO cpUserVO) throws BusinessException {
		UserVO ncuser = new UserVO();
		try {
			BeanUtils.copyProperties(ncuser, cpUserVO);
		} catch (Exception e) {
			CpLogger.error(e.getMessage(), e);
			throw new BusinessException(e);
		} 
		PasswordSecurityLevelVO pslVO = PasswordSecurityLevelFinder.getPWDLV(ncuser);
		int len = pslVO.getMinimumLength() == null ? 0 : pslVO.getMinimumLength().intValue();
		// TODO 这里生成一个随机串 以后可能根据密码配置来进行
		String randomSeq = len > 0 ? RandomSeqUtil.getRandomSeq(len) : RandomSeqUtil.getRandomSeq();
		return randomSeq;
	}
	
	@Override
	public void doStaticPasswordVerify(CpUserVO cpUserVO, String password)
			throws BusinessException{
	
	}
	
	

	@Override
	public void addPwdResetUser(String userid) throws BusinessException {
		ServiceLocator.getService(IUserExService.class).addPwdResetUser(userid);
		
	}

	@Override
	public void addInitUser(String userid) throws BusinessException {
		ServiceLocator.getService(IUserExService.class).addInitUser(userid);
	}

	@Override
	public void delResetUserInfo(String user_id) {
		UserExManageUtil.getInstance().delResetUserInfo(user_id);
	}
	
	

	@Override
	public void addUserPswHistory(CpUserVO cpUserVO, String pswWord) throws BusinessException {
		
		UserVO ncuser = new UserVO();
		try {
			BeanUtils.copyProperties(ncuser, cpUserVO);
		} catch (Exception e) {
			CpLogger.error(e.getMessage(), e);
			throw new BusinessException(e);
		} 
		PasswordSecurityLevelVO pwdLevel = PasswordSecurityLevelFinder.getPWDLV(ncuser);
		
		UserPasswordVO pswvo = new UserPasswordVO();
		pswvo.setCuserid(ncuser.getCuserid());
		pswvo.setUser_password(pswWord);
		ServiceLocator.getService(IUserPasswordChecker.class).updateUPVO(ncuser.getCuserid(), pswWord, pwdLevel);
	}

	@Override
	public void delInitUserInfo(String user_id) throws BusinessException {
		UserExManageUtil.getInstance().delInitUser(user_id);
	}

	@Override
	public boolean isInitUser(String user_id) throws BusinessException {
		return false;
	}

	@Override
	public boolean isResetUser(String user_id) throws BusinessException {
		boolean isReset = false;
		return isReset;
	}

	@Override
	public void lockNcUser(CpUserVO user) throws BusinessException {
		ServiceLocator.getService(IUserLockService.class).updateLockedTag(user.getNcpk(), true);
	}

	@Override
	public String getNcUserPKByUserCode(String code) throws BusinessException {
		String cuserid = null;
		try {
			String where = " user_code_q = '" + CpSqlTranslateUtil.tmsql(code.trim().toUpperCase()) + "' ";
			PtBaseDAO dao = new PtBaseDAO();
			@SuppressWarnings("unchecked")
			List<UserVO>users =  (List<UserVO>) dao.retrieveByClause(UserVO.class, where);
			//UserVO ncUserVo = NCLocator.getInstance().lookup(IUserManageQuery.class).findUserByCode(code);
			if (users != null && users.size() >0){
				UserVO ncUserVo = users.get(0);
				if(ncUserVo != null)
					cuserid = ncUserVo.getCuserid();
			}
			return cuserid;
		} catch (Exception e) {
			CpLogger.error(e);
			throw new BusinessException(e.getMessage());
		}
	}

	@Override
	public boolean isUsedInNC(String cuserId, String pk_base_doc) throws BusinessException {
		boolean isUsedInNC = false;
		//ncPK不为空
		String where = " pk_base_doc = '" + pk_base_doc + "' and cuserId !='" + cuserId +"'";
		PtBaseDAO dao = new PtBaseDAO();
		try {
			SuperVO[] users = dao.queryByCondition(UserVO.class, where);
			if(!ArrayUtils.isEmpty(users))
				isUsedInNC = true;
		} catch (DAOException e) {
			CpLogger.error(e);
			throw new BusinessException(e.getMessage());
		}
		
		return isUsedInNC;
	}

	@Override
	public void updateNcUserCode(String cuserId, String newCode)
			throws BusinessException {
		try {
			UserVO ncUserVo = (UserVO) new PtBaseDAO().retrieveByPK(UserVO.class, cuserId);
			ncUserVo.setUser_code(newCode);
			ServiceLocator.getService(IUserManage.class).updateUser(ncUserVo);
		} catch (Exception e) {
			CpLogger.error(e);
			throw new BusinessException(e.getMessage());
		}
		
	}

	@Override
	public void shareUser2Group(String cuserid, String[] original_group,
			String[] new_group) throws BusinessException {
//		ServiceLocator.getService(IUserManage.class).shareUser2Group(cuserid, original_group, new_group);
		
		CpUserVO cpuser = (CpUserVO) new BaseDAO().retrieveByPK(CpUserVO.class, cuserid);
		UserVO user = CpbUtil.convert(cpuser);
	    // 加锁
	    BDPKLockUtil.lockSuperVO(user);
	    // 插入用户--共享集团关联
	    HashMap<String, Object> name_value_map = new HashMap<String, Object>();
	    name_value_map.put("cuserid", cuserid);
	    MiddleEntityPersister<UserShareVO> persister =
	        new MiddleEntityPersister<UserShareVO>(UserShareVO.class,
	            name_value_map, "pk_group");
	    persister.doPersist(original_group, new_group);
	    GroupVO[] orgs =
	    		ServiceLocator.getService(IUserManageQuery.class).queryUserSharedGroup(cuserid);

	    this.writeUserShareLog(user, orgs);

//	    return orgs;
	}
	
	private void writeUserShareLog(UserVO user, GroupVO[] orgs)
		      throws BusinessException {
	}

	
}