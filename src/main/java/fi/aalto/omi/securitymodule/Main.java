package fi.aalto.omi.securitymodule;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;
import fi.aalto.omi.securitymodule.db.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main
{
    private final static Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        
        Properties prop = new Properties();
        InputStream input = null;

        try {
                //Load a properties file
                input = new FileInputStream("configs/securitymodule.conf");
                prop.load(input);
                int http_port = Integer.parseInt(prop.getProperty("http_port"));
                int https_port = Integer.parseInt(prop.getProperty("https_port"));
                        
                //Init Permissions Database
                DatabaseInit.getInstance();
                logger.info("Main.main() > Database Initialized Successfully");
                
                //Start HTTP Server
                JettyStart(http_port,https_port);
                logger.info("Main.main() > Jetty Started Successfully");

        } catch (IOException ex) {
            logger.error("Main.main() > Execption: " + ex ); 
        } finally {
            if (input != null) {
                    try {
                        input.close();
                    } catch (IOException ex) {
                        logger.error("Main.main() > Execption: " + ex );
                    }
                }
            }
    }
            
    private static void JettyStart(int http_port, int https_port){

        
        try {

            String webappDirLocation = "html/SecurityModule";

            Server server = new Server();

            //LOAD SERVLET described in web.xml
            WebAppContext root = new WebAppContext();
            root.setContextPath("/");
            root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
            root.setResourceBase(webappDirLocation);
            root.setParentLoaderPriority(true);
            server.setHandler(root);

            //HTTPS
            ServerConnector connector = new ServerConnector(server);
            connector.setPort(http_port);
            HttpConfiguration https = new HttpConfiguration();
            https.addCustomizer(new SecureRequestCustomizer());

            SslContextFactory sslContextFactory = new SslContextFactory();
            //Note: Generate your certificate with this command: keytool -genkey -alias sitename -keyalg RSA -keystore keystore.jks -keysize 2048
            sslContextFactory.setKeyStorePath("configs/keystore.jks");
            sslContextFactory.setKeyStorePassword("123456"); //Same password used in the creation of keystore.jks
            sslContextFactory.setKeyManagerPassword("123456");

            ServerConnector sslConnector = new ServerConnector(server, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(https));
            sslConnector.setPort(https_port);

            server.setConnectors(new Connector[] { connector, sslConnector });

            server.start();
            server.join();

        } catch (Exception ex) {
            logger.error("Main.JettyStart() > Execption: " + ex );
        }
        
    }
       
}