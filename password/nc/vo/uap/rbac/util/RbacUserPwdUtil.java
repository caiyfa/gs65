package nc.vo.uap.rbac.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nc.bs.framework.common.NCLocator;
import nc.bs.uif2.BusinessExceptionAdapter;
import nc.itf.uap.rbac.IUserManageQuery;
import nc.login.vo.INCUserTypeConstant;
import nc.vo.pub.BusinessException;
import nc.vo.sm.UserVO;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

/**
 * RBAC�û����빤��
 * @author hanyw1
 * @since uap6.1
 */
public class RbacUserPwdUtil {

	/**Ϊ���������û������Ƿ�md5�����꣬�ڱ�md5���ܵĴ�ǰ���ǰ׺**/
	public final static String MD5PWD_PREFIX = "U_U++--V";
	
	//���������ǰ׺���ڼ򵥣���Ϊ�˼����ȱ���һ��ʱ�䡣 ���滻
	@Deprecated
	public final static String MD5PWD_PREFIX_Deprecated = "md5";
	
	/**
	 * ��ȡ��Сд��ĸ�����ֻ���8λ�����������
	 * @return
	 */
	public static String getRandomSeq() {
		StringBuffer buff = new StringBuffer();
		int index = 0;
		for (int i = 0; i < 8; i++) {
			int random = (int) (Math.random() * 1000);
			if (i >= 3) {
				index = random % 3;
			} else {
				index = i;
			}
			switch (index) {
			case 0:
				buff.append((char) (97 + random % 26));
				break;
			case 1:
				buff.append((char) (65 + random % 26));
				break;				
			case 2:
				buff.append((char) (48 + random % 10));
				break;
			}
		}
		return buff.toString();
	}
	
    // У�������б������һ����ĸ������
	public boolean checkPwdType(String pwd) {
		String regExABC = "a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z|" +
		                  "A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z";
		Pattern patABC = Pattern.compile(regExABC);
		Matcher matABC = patABC.matcher(pwd); 
		String regEx123 = "0|1|2|3|4|5|6|7|8|9";
		Pattern pat123 = Pattern.compile(regEx123);
		Matcher mat123 = pat123.matcher(pwd);
		return matABC.find() && mat123.find();
	}
	
	/**
	 * У���û����루���Ǿ�̬������ܺ�ļ򵥱ȶԣ�
	 * @param user �û�VO
	 * @param expresslyPWD ��У����������
	 * @return У��ͨ������ true,  У�鲻ͨ������ false
	 */
	public static boolean checkUserPassword(UserVO user,String expresslyPWD) {
//		if(user == null)
//			return false;
//		
//		String userActualCodecPwd = getUserActualCodecPwd(user);
//		
//		Integer user_type = user.getUser_type();
//		
//		//�ǳ�������Ա���벻��Ϊ�գ���������Ա�������Ϊ��
//		if(user_type != null && user_type != INCUserTypeConstant.USER_TYPE_SUPER_ADM 
//				&& (StringUtils.isBlank(userActualCodecPwd) || StringUtils.isBlank(expresslyPWD)) )
//			return false;
//		
//		
//		try{
//			String toCheckCodecPwd = getEncodedPassword(user, expresslyPWD);
//			
//			boolean isValidByMD5 = userActualCodecPwd.equals(toCheckCodecPwd);
//			
//			if(!isValidByMD5){
//				
//				//���ݾ�ǰ׺
//				boolean checkByOldPrefix = checkByMD5WithOldPrefix(user, expresslyPWD,userActualCodecPwd);
//				if(checkByOldPrefix)
//					return checkByOldPrefix;
//				
//				if(user_type == INCUserTypeConstant.USER_TYPE_SUPER_ADM){
//					Encode encoder = new Encode();
//					String codecPwdByEncoder = encoder.encode(expresslyPWD);
//					
//					return userActualCodecPwd.equals(codecPwdByEncoder);
//				}else
//					return isValidByMD5;
//			}
//			
//			return isValidByMD5;
//			
//		}catch(Exception ex){
//			Logger.debug(ex.getMessage());
//			return false;
//		}
		return true;
	}

	@SuppressWarnings("unused")
	private static String getUserActualCodecPwd(UserVO user) {
		try {
			if(user.getUser_type() == INCUserTypeConstant.USER_TYPE_SUPER_ADM){
				IUserManageQuery userQry = NCLocator.getInstance().lookup(IUserManageQuery.class);
				return userQry.getSuperAdminEncodecPwd(user.getUser_code());
			}
			
			String cuserid = user.getPrimaryKey();
			IUserManageQuery userQry = NCLocator.getInstance().lookup(IUserManageQuery.class);
			UserVO dbUser = userQry.getUser(cuserid);
			String userActualCodecPwd = null;
			if(dbUser != null)
				userActualCodecPwd = dbUser.getUser_password();
			
			return userActualCodecPwd;
		} catch (BusinessException e) {
			throw new BusinessExceptionAdapter(e);
		}		
	}

	@SuppressWarnings("unused")
	private static boolean checkByMD5WithOldPrefix(UserVO user,	String expresslyPWD, String userActualCodecPwd)
			throws BusinessException {
		
		String toCheckCodecPwdWithOldPrefix = getEncodedPassword_Deprecated(user, expresslyPWD);
		return userActualCodecPwd.equals(toCheckCodecPwdWithOldPrefix);
	}
	
	/**
	 * ��UserVO �� ���������� ���ܺ���û�����
	 * @param user
	 * @param expresslyPWD ��������
	 * @return  ���ܺ�����봮
	 * @throws BusinessException
	 */
	public static String getEncodedPassword(UserVO user , String expresslyPWD) throws BusinessException{
		if(user == null || StringUtils.isBlank(user.getPrimaryKey()))
			throw new BusinessException("illegal arguments");
		
		String codecPWD = DigestUtils.md5Hex(user.getPrimaryKey() + StringUtils.stripToEmpty(expresslyPWD));
		
		return MD5PWD_PREFIX+codecPWD;
	}
	
	//���������ǰ׺���ڼ򵥣���Ϊ�˼����ȱ���һ��ʱ�䡣 ���滻
	@Deprecated
	private static String getEncodedPassword_Deprecated(UserVO user , String expresslyPWD) throws BusinessException{
		if(user == null || StringUtils.isBlank(user.getPrimaryKey()))
			throw new BusinessException("illegal arguments");
		
		String codecPWD = DigestUtils.md5Hex(user.getPrimaryKey() + StringUtils.stripToEmpty(expresslyPWD));
		
		return MD5PWD_PREFIX_Deprecated+codecPWD;
	}
	
	
	public static void main(String[] args){
		UserVO user = new UserVO();
		
		user.setPrimaryKey("0001AA1000000000015I");
		
		try{
			System.out.println(getEncodedPassword(user, "ufida_ufida"));
			
		}catch(BusinessException ex){
			ex.printStackTrace();
		}
		
	}
}
