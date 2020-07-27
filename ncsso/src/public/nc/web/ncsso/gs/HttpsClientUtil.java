package nc.web.ncsso.gs;

import nc.vo.pub.BusinessException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
//import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
public class HttpsClientUtil {
	@SuppressWarnings("resource")
	public static String doPost(String url,/*String jsonstr,*/String charset) throws BusinessException{
        HttpClient httpClient = null;
        HttpPost httpPost = null;
        String result = null;
        try{
            httpClient = new FreeOfAuthHttpClient();
            httpPost = new HttpPost(url);
            httpPost.addHeader("Content-Type", "application/json");
//            StringEntity se = new StringEntity(jsonstr);
//            se.setContentType("text/json");
//            se.setContentEncoding(new BasicHeader("Content-Type", "application/json"));
//            httpPost.setEntity(se);
            HttpResponse response = httpClient.execute(httpPost);
            if(response != null){
                HttpEntity resEntity = response.getEntity();
                if(resEntity != null){
                    result = EntityUtils.toString(resEntity,charset);
                }
            }
        }catch(Exception e){
            throw new BusinessException(e);
        }
       
        return result;
    }
	public static String doPost(String url) throws BusinessException{
		return doPost(url,"UTF-8");
	}
	public static void main (String[] args) throws BusinessException{
		String url="https://ssotest.gszq.com/profile/oauth2/accessToken?client_secret=4f9c5c42-25f9-4994-93ab-769274c9c81c&grant_type=authorization_code&redirect_uri=http://127.0.0.1:8899/paraOsc/callback&code=ST-2-uUn53MzbDNvlWdKEk6wB&client_id=100008&nonce_str=09aa60ce-0a5e-4b41-8b05-19ce70d4fef0&oauth_timestamp=1534396033486&sign=1D50FA31D9F232333D4C3A65E56DDB09";
		 System.out.print(doPost(url,"utf-8"));
	}
}
