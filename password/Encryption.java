
import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by guonan on 15/9/25.
 * 
 * Ŀǰ�����ļ��ܷ�ʽ��: �ԳƼ��� --> AES��DES �ǶԳƼ��� --> RSA��DSA
 */
public class Encryption {
	/**
	 * ���16��������
	 * @param data
	 * @param key
	 * @param iv
	 * @return
	 * @throws Exception
	 */
	public static String encryptHEX(String data, String key, String iv)throws Exception{
		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		byte[] dataBytes = data.getBytes();

		// ������������һ��Ϊ˽Կ�ֽ����飬 �ڶ���Ϊ���ܷ�ʽ AES����DES
		SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "DES");
		IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

		/**
		 * ��ʼ�����˷������Բ������ַ�ʽ����������Ҫ������ӡ� (1)�޵��������� --> iv (2)����������ΪSecureRandom
		 * random = new SecureRandom();��random�����������(AES���ɲ������ַ���)
		 * (3)���ô˴����е�IVParameterSpec --> ָ�����˵�
		 * 
		 * ����ʹ�� DECRYPT_MODE ��ʽ
		 */
		cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
		byte[] encrypted = cipher.doFinal(dataBytes);
		return byte2Hex(encrypted);
	}
	/**
	 * ��16�������Ľ��н���
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
	 *            Ҫ���ܵ��ַ���
	 * @param key
	 *            ˽Կ: AES�̶���ʽΪ128/192/256 bits.��:16/24/32bytes��
	 *            DES�̶���ʽΪ128bits����8bytes��
	 * 
	 * @param iv
	 *            ��ʼ���������� AES Ϊ16bytes. DES Ϊ8bytes.
	 * @return
	 * @throws Exception
	 */
	public static String encrypt(String data, String key, String iv)
			throws Exception {
		try {
			/**
			 * 
			 * AES��DES һ����4�ֹ���ģʽ: 1.�������뱾ģʽ(ECB) -- ȱ������ͬ�����ļ��ܳ���ͬ�����ģ����ĵĹ��ɴ������ġ�
			 * 2.���ܷ�������ģʽ(CBC) 3.���ܷ���ģʽ(CFB) 4.�������ģʽ(OFB)����ģʽ
			 * 
			 * PKCS5Padding: ��䷽ʽ
			 * 
			 * ���ܷ�ʽ/����ģʽ/��䷽ʽ DES/CBC/PKCS5Padding
			 */
			Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			byte[] dataBytes = data.getBytes();

			// ������������һ��Ϊ˽Կ�ֽ����飬 �ڶ���Ϊ���ܷ�ʽ AES����DES
			SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "DES");
			IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

			/**
			 * ��ʼ�����˷������Բ������ַ�ʽ����������Ҫ������ӡ� (1)�޵��������� --> iv (2)����������ΪSecureRandom
			 * random = new SecureRandom();��random�����������(AES���ɲ������ַ���)
			 * (3)���ô˴����е�IVParameterSpec --> ָ�����˵�
			 * 
			 * ����ʹ�� DECRYPT_MODE ��ʽ
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
			// ����Base64����
			byte[] encrypted1 = new BASE64Decoder().decodeBuffer(dataEncode);
			Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "DES");
			IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

			// ����ʹ�� DECRYPT_MODE ��ʽ
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

		System.out.println("����: " + data);
		System.out.println("����: " + en);
		System.out.println("ת��: " + hex2Byte(en));
//		System.out.println("����: " + de);
	}
}