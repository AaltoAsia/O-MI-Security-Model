package fi.aalto.omi.securitymodule.servlet;

import com.auth0.Auth0User;
import com.auth0.SessionUtils;
import fi.aalto.omi.securitymodule.db.DBHelper;
import fi.aalto.omi.securitymodule.db.objects.OMIUser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.SessionCookieConfig;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomeServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(HomeServlet.class);
    
    public void onStartup(ServletContext servletContext) throws ServletException 
    {
        SessionCookieConfig scc = getServletContext().getSessionCookieConfig();
        scc.setPath("/");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        final Auth0User user = SessionUtils.getAuth0User(req);
        if (user != null) {
            
            try {
                req.setAttribute("user", user);
                OMIUser newUser = new OMIUser();
                newUser.username = user.getName();
                newUser.email =  user.getEmail();
                boolean authenticated = DBHelper.getInstance().createUserIfNotExists(newUser);

                if (authenticated) {
                    logger.info("User Autenticated: " + user.getEmail() + "," + newUser.username);
                    
                    HttpSession session = req.getSession(true);
                    session.setAttribute("userID", newUser.email );
                    
                    String cookieValue = "email="+ newUser.email + ",name=" +  newUser.username;
                    Cookie OMIuserCookie = new Cookie("OMIuser", cookieValue);
                    OMIuserCookie.setSecure(true);
                    OMIuserCookie.setMaxAge(-1); // (-1)=delted once the browser is closed
                    OMIuserCookie.setPath("/");
                    res.addCookie(OMIuserCookie);
                }
            }catch (Exception ex) {
                    logger.warn(ex.getCause() + ":" + ex.getMessage());
            }
        }
        res.sendRedirect(getServletConfig().getInitParameter("onLoginRedirectTo"));
    }
}