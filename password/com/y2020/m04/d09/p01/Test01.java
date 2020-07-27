package com.y2020.m04.d09.p01;
import ism.systemcheck.util.DESUtils;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
/**
 * @CLASSNAME: com.y2020.m04.d09.p01
 * @DESCRIPTION:
 * @AUTHOR: 
 * @DATE: 2020/4/8 
 */
public class Test01 { 
	public static void main(String args) throws Exception{
		byte[] str=DESUtils.encrypt("{data:[{'name':'ÄãºÃ','age':20},{'name':'zd','age':18}]}", "12345678abcdefgh");
	}
}