package fi.aalto.omi.securitymodule.db;

import ch.qos.logback.classic.Level;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.StringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class DatabaseInit {
    
    private static final String dbName = "OMISec.db";
    private static final String propertyFilePath = "configs/securitymodule.conf";
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private static final DatabaseInit instance = new DatabaseInit();
    
    
    private DatabaseInit() {
        configureDB();
    }

    private int DEFAULT_GROUP_ID;
    private Connection connection;

    public static DatabaseInit getInstance() {
        return instance;
    }

    public boolean configureDB()
    {
        String jdbcDriver = "jdbc:sqlite:"+ dbName;

        File file = new File(dbName);
        boolean dbExists = file.exists();

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(jdbcDriver);
        } catch ( Exception e ) {
            logger.error("DatabaseInit.checkUserPermissions - Exception > While loading JDBC driver: ",e);
            return false;
        }

        if (!dbExists)
        {
            logger.info("DatabaseInit.configureDB > Creating new database");

            try {
                createTables();
            } catch (SQLException ex)
            {
                logger.error("DatabaseInit.configureDB - SQLException >  Error while creating tables: ", ex);
                return false;
            }

            logger.info("DatabaseInit.configureDB > Created tables successfully.");
        } else {
            logger.info("DatabaseInit.configureDB > Opened database successfully. Path:"+file.getAbsolutePath());

            DEFAULT_GROUP_ID = getGroupID("Default");
            if (DEFAULT_GROUP_ID == -1)
                DEFAULT_GROUP_ID = createGroup("Default");
        }
        createAdministrators();
        return true;
    }
        
        
    private void createTables() throws SQLException
    {
        Statement stmt = connection.createStatement();
        String sql = "CREATE TABLE USERS " +
                "(ID INTEGER PRIMARY KEY     NOT NULL," +
                " USERNAME       VARCHAR(256)    NOT NULL,"+
                " EMAIL       VARCHAR(256)    UNIQUE NOT NULL)";
        stmt.executeUpdate(sql);

        sql = "CREATE TABLE RULES " +
                "(ID INTEGER PRIMARY KEY     NOT NULL," +
                " HID            TEXT     NOT NULL," +
                " GROUP_ID          INT    NOT NULL," +
                " WRITE_PERMISSIONS   INT     NOT NULL," +
                " OBJECT_RULE INT NOT NULL)";
        stmt.executeUpdate(sql);

        sql = "CREATE TABLE GROUPS " +
                "(ID INTEGER PRIMARY KEY     NOT NULL," +
                " GROUP_NAME          VARCHAR(256)    UNIQUE NOT NULL)";
        stmt.executeUpdate(sql);

        sql = "CREATE TABLE USERS_GROUPS_RELATION " +
                "(ID INTEGER PRIMARY KEY     NOT NULL," +
                " USER_ID          INT    NOT NULL," +
                " GROUP_ID          INT    NOT NULL)";
        stmt.executeUpdate(sql);

        sql = "CREATE TABLE ADMINISTRATORS " +
                "(ID INTEGER PRIMARY KEY NOT NULL," +
                "EMAIL VARCHAR(256) UNIQUE NOT NULL)";
        stmt.executeUpdate(sql);
        stmt.close();

        DEFAULT_GROUP_ID = createGroup("Default");
    }
    
    public int createGroup(String groupName)
    {
        try {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO GROUPS(GROUP_NAME) VALUES(?)");
            stmt.setString(1,groupName);
            stmt.executeUpdate();

            int res = stmt.getGeneratedKeys().getInt(1);
            stmt.close();
            logger.info("DatabaseInit.createGroup > Group with name:"+groupName+" successfully created. ID="+res);
            return res;

        } catch (SQLException ex)
        {
            logger.error("DatabaseInit.createGroup - SQLException > ",ex);
            return -1;
        }
    }
    
    public int getGroupID(String groupName) {
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT ID FROM GROUPS WHERE GROUP_NAME=?");
            stmt.setString(1, groupName);
            ResultSet rs = stmt.executeQuery();
            if ( rs.next() ) {
                int res = rs.getInt("ID");
                stmt.close();
                return res;
            }
            stmt.close();
            return -1;
        } catch (SQLException ex)
        {
            logger.error("DatabaseInit.getGroupID - SQLException > ",ex);
            return -1;
        }
    }

    private void createAdmin(String email) {

        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO ADMINISTRATORS(EMAIL) VALUES(?)")){
            stmt.setString(1,email);
            stmt.executeUpdate();

            int res = stmt.getGeneratedKeys().getInt(1);
            stmt.close();
            logger.info("DatabaseInit.createAdmin > Administrator Created with email:"+email+" successfully created. ID="+res);

        } catch (SQLException ex)
        {
            logger.error("DatabaseInit.createAdmin - SQLException > ",ex);
        }
    }

    private void removeOldAdministrators() {
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("DELETE FROM ADMINISTRATORS");
            stmt.close();

        } catch (SQLException ex)
        {
            logger.error("DatabaseInit.removeOldAdministrators - SQLException > ",ex);
        }
    }

    private void createAdministrators()
    {
        removeOldAdministrators();
        String adminsCSV = readConfigurationFile();
        
        StringTokenizer token = new StringTokenizer(adminsCSV.trim(), ",");

        while (token.hasMoreTokens()) {
               createAdmin(token.nextToken());
        }
    }
    
    
    private String readConfigurationFile() {
        
	Properties prop = new Properties();
	InputStream input = null;
        String adminsCSV = "";

	try {
            // load a properties file
            input = new FileInputStream(propertyFilePath);
            prop.load(input);
            adminsCSV = prop.getProperty("administrators");
            
	} catch (IOException ex) {
		logger.error("DatabaseInit.createAdministrators - SQLException > ",ex);
	} finally {
		if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                        logger.error("DatabaseInit.createAdministrators - SQLException > ",ex);
                }
            }
        }
        return adminsCSV;
    }

     

}
