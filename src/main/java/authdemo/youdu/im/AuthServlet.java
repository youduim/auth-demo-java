package authdemo.youdu.im;

import com.google.gson.Gson;
import im.youdu.sdk.client.AppClient;
import im.youdu.sdk.entity.ReceiveAuth;
import im.youdu.sdk.entity.YDApp;
import authdemo.youdu.im.action.Crypto;
import authdemo.youdu.im.untity.BackResponse;
import authdemo.youdu.im.untity.EncryptBody;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Properties;


import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;



public class AuthServlet extends HttpServlet {
    Logger logger = Logger.getLogger(AuthServlet.class);
    private int ydBuin;
    private String ydAppId;
    private String ydSrvHost;
    private String ydEncodingAESKey;
    private YDApp ydApp;
    private AppClient ydAppClient;


    static int ERRCODE_OK = 0; //认证成功
    static int ERRCODE_ACCOUNT_AUTHFAILED = 300002; //认证失败
    static int ERRCODE_ACCOUNT_NOTEXIST  =	300001; //帐号不存在
    static int ERRCODE_INTERNALERR	=	-1; //内部错误

    @Override
    public void init() throws ServletException {
        logger.setLevel(Level.DEBUG);

        Properties properties = new Properties();
        try {
            String cfgPath = this.getServletContext().getRealPath("/WEB-INF/classes/AuthDemo.properties");
            logger.info( "config file path:"+cfgPath);

            // 使用ClassLoader加载properties配置文件生成对应的输入流
            InputStream in = new BufferedInputStream (new FileInputStream(cfgPath));
            properties.load(in);
            ydBuin = Integer.valueOf(properties.getProperty("buin"));
            ydAppId = properties.getProperty("appId");
            ydSrvHost = properties.getProperty("srvHost");
            ydEncodingAESKey = properties.getProperty("encodingAESKey");
            ydApp = new YDApp(ydBuin, ydSrvHost, "", ydAppId, "", ydEncodingAESKey);
            ydAppClient = new AppClient(ydApp);
        }catch (FileNotFoundException e){
            logger.error(e.toString() );
        }catch (IOException e){
            logger.error(e.toString() );
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        logger.setLevel(Level.DEBUG);
        BasicConfigurator.configure();
        try {
            Crypto crypto = new Crypto(ydAppClient);
            //获取request的数据流
            String encryptRequest = crypto.getInput(request);
            //把请求数据转换为json对象
            Object inputData = new Gson().fromJson(encryptRequest, EncryptBody.class);
            String encryptMsg = ((EncryptBody) inputData).getEncrypt();
            //解密收到的数据
            ReceiveAuth auth = crypto.decrypt(encryptMsg);
            logger.info("receive auth request from youdu:" + auth );
            logger.info( "account:" + auth.getFromUser() + " pwd:" + auth.getPasswd());   //需要记录的地方使用

            //TODO:将用户名和密码转到第三方系统去验证，并将验证结果转为有度认证结果
            
            //演示返回登录成功
            String respond = this.createAuthRespond(ERRCODE_OK);
            logger.info("send auth respond to youdu:"+ respond );

            //返回数据
            response.setContentType("application/json; charset=utf-8");
            PrintWriter out = response.getWriter();
            out.print(respond);
            out.close();
            response.flushBuffer();

        } catch (IOException e) {
            logger.error(e.toString() );
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().write("get action is blank");
    }

    //要返回给发送者的数据
    public String createAuthRespond(int errcode) {
        BackResponse backResponse = new BackResponse();
        backResponse.setErrcode(errcode);
        backResponse.setErrmsg("ok");
        backResponse.setEncrypt("");
        String src = new Gson().toJson(backResponse);
        return src;
    }

}
