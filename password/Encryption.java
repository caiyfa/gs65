
import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by guonan on 15/9/25.
 * 
 * 目前主流的加密方式有: 对称加密 --> AES、DES 非对称加密 --> RSA、DSA
 */
public class Encryption {
	/**
	 * 输出16进制密文
	 * @param data
	 * @param key
	 * @param iv
	 * @return
	 * @throws Exception
	 */
	public static String encryptHEX(String data, String key, String iv)throws Exception{
		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		byte[] dataBytes = data.getBytes();

		// 两个参数，第一个为私钥字节数组， 第二个为加密方式 AES或者DES
		SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "DES");
		IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

		/**
		 * 初始化，此方法可以采用三种方式，按服务器要求来添加。 (1)无第三个参数 --> iv (2)第三个参数为SecureRandom
		 * random = new SecureRandom();中random对象，随机数。(AES不可采用这种方法)
		 * (3)采用此代码中的IVParameterSpec --> 指定好了的
		 * 
		 * 解密使用 DECRYPT_MODE 方式
		 */
		cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
		byte[] encrypted = cipher.doFinal(dataBytes);
		return byte2Hex(encrypted);
	}
	/**
	 * 由16进制密文进行解密
	 * @param data
	 * @param key
	 * @param iv
	 * @return
	 * @throws Exception
	 */
	public static String desEncryptHEX(String data,String key,String iv)throws Exception{
		return null;
	}
	private static String hex2Byte(String data) throws Exception{
		if(data==null||data.length()<2){
			return null;
		}
		data=data.toLowerCase();
		int len=data.length()/2;
		byte[] res=new byte[len];
		for(int i=0;i<len;++i){
			String tmp=data.substring(2*i, 2*i+2);
			res[i]=(byte)(Integer.parseInt(tmp, 16)&0xFF);
		}
		System.out.println(new String(res,"gb2312"));
		return null;
	}
	private static String byte2Hex(byte[] bytes){
		StringBuffer sb=new StringBuffer(bytes.length*2);
		String tmp="";
		for(int n=0;n<bytes.length;n++){
			tmp=(Integer.toHexString(bytes[n]&0XFF));
			if(tmp.length()==1){
				sb.append("0");
			}
			sb.append(tmp);
		}
		return sb.toString().toUpperCase();
		
	}
	/**
	 * 
	 * @param data
	 *            要加密的字符串
	 * @param key
	 *            私钥: AES固定格式为128/192/256 bits.即:16/24/32bytes。
	 *            DES固定格式为128bits，即8bytes。
	 * 
	 * @param iv
	 *            初始化向量参数 AES 为16bytes. DES 为8bytes.
	 * @return
	 * @throws Exception
	 */
	public static String encrypt(String data, String key, String iv)
			throws Exception {
		try {
			/**
			 * 
			 * AES和DES 一共有4种工作模式: 1.电子密码本模式(ECB) -- 缺点是相同的明文加密成相同的密文，明文的规律带到密文。
			 * 2.加密分组链接模式(CBC) 3.加密反馈模式(CFB) 4.输出反馈模式(OFB)四种模式
			 * 
			 * PKCS5Padding: 填充方式
			 * 
			 * 加密方式/工作模式/填充方式 DES/CBC/PKCS5Padding
			 */
			Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			byte[] dataBytes = data.getBytes();

			// 两个参数，第一个为私钥字节数组， 第二个为加密方式 AES或者DES
			SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "DES");
			IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

			/**
			 * 初始化，此方法可以采用三种方式，按服务器要求来添加。 (1)无第三个参数 --> iv (2)第三个参数为SecureRandom
			 * random = new SecureRandom();中random对象，随机数。(AES不可采用这种方法)
			 * (3)采用此代码中的IVParameterSpec --> 指定好了的
			 * 
			 * 解密使用 DECRYPT_MODE 方式
			 */
			cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
			byte[] encrypted = cipher.doFinal(dataBytes);

			return new sun.misc.BASE64Encoder().encode(encrypted);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String desEncrypt(String dataEncode, String key, String iv)
			throws Exception {
		try {
			// 先用Base64解码
			byte[] encrypted1 = new BASE64Decoder().decodeBuffer(dataEncode);
			Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "DES");
			IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

			// 解密使用 DECRYPT_MODE 方式
			cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

			byte[] original = cipher.doFinal(encrypted1);
			String originalString = new String(original);
			return originalString;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String args[]) throws Exception {
		String data = "{\"phone\":\"18752096103\",\"invoiceNum\":\"19004837\",\"mail\":\"caiyfa@yonyou.com\",\"invoiceCode\":\"131880930142\"}";
		String key = "12345678";
		String iv = "abcdefgh";

		String en = encryptHEX(data, key, iv);
//		String de = desEncrypt(en, key, iv);

		System.out.println("明文: " + data);
		System.out.println("加密: " + en);
		System.out.println("转换: " + hex2Byte(en));
//		System.out.println("解密: " + de);
	}
}