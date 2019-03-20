package authdemo.youdu.im.action;

import authdemo.youdu.im.AuthServlet;
import im.youdu.sdk.client.AppClient;
import im.youdu.sdk.encrypt.AESCrypto;
import im.youdu.sdk.entity.ReceiveAuth;
import im.youdu.sdk.exception.GeneralEntAppException;
import im.youdu.sdk.exception.ParamParserException;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Crypto {
    Logger logger=Logger.getLogger(AuthServlet.class);

    private AppClient appClient;
    private AESCrypto crypto;
    public Crypto(){

    }

    public Crypto(AppClient appClient){
        this.appClient = appClient;
        crypto = new AESCrypto(appClient.getAppId(), appClient.getAppAeskey());
    }

    public String getInput(HttpServletRequest request){
        //读取用户发送的request请求中的数据
        String resStr = "";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
            StringBuffer sb = new StringBuffer("");
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            resStr = sb.toString();
        }catch (IOException e) {
            logger.error(e.toString() );
        }
        return resStr;
    }

    //接收客户端传来的加密数据并解析
    public ReceiveAuth decrypt(String encryptMsg){
        ReceiveAuth receiveAuth = null;
        try {
            receiveAuth = appClient.decryptAuth(encryptMsg);
        }catch (IOException e){
            logger.error(e.toString() );
        }catch (ParamParserException e){
            logger.error(e.toString() );
        }catch (GeneralEntAppException e){
            logger.error(e.toString() );
        }
        return receiveAuth;
    }
}
