package fi.aalto.omi.securitymodule.db;

import fi.aalto.omi.securitymodule.db.objects.OMIGroup;
import fi.aalto.omi.securitymodule.db.objects.OMIRule;
import fi.aalto.omi.securitymodule.db.objects.OMIUser;

import java.sql.*;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DBHelper {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private static final String dbName = "OMISec.db";
    private int DEFAULT_GROUP_ID;
    private Connection connection;
    
    private static final DBHelper instance = new DBHelper();
    
    private DBHelper() {
        
        String jdbcDriver = "jdbc:sqlite:"+ dbName;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(jdbcDriver);
        } catch ( Exception e ) {
            logger.error("DBHelper.DBHelper() - Exception > While loading JDBC driver: ",e);
        }
        
    }

    public static DBHelper getInstance() {

        return instance;
    }

    public boolean checkAdminPermissions(String userID)
    {
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM ADMINISTRATORS WHERE EMAIL=?");
            stmt.setString(1, userID);
            ResultSet rs = stmt.executeQuery();
            boolean res = rs.next();
            stmt.close();
            return res;

        } catch (SQLException ex)
        {
            logger.error("DBHelper.checkAdminPermissions - SQLException > ",ex);
            return false;
        }
    }

    public int createGroup(String groupName)
    {
        try {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO GROUPS(GROUP_NAME) VALUES(?)");
            stmt.setString(1,groupName);
            stmt.executeUpdate();

            int res = stmt.getGeneratedKeys().getInt(1);
            stmt.close();
            logger.info("DBHelper.createGroup > Group with name:"+groupName+" successfully created. ID="+res);
            return res;

        } catch (SQLException ex)
        {
            logger.error("DBHelper.createGroup - SQLException > ",ex);
            return -1;
        }
    }

    public boolean updateGroup(int groupID, String groupName)
    {
        try {
            PreparedStatement stmt = connection.prepareStatement("UPDATE GROUPS SET GROUP_NAME = ? WHERE ID = ?;");
            stmt.setString(1,groupName);
            stmt.setInt(2,groupID);
            stmt.executeUpdate();
            stmt.close();
            logger.info("DBHelper.updateGroup > Group with ID:"+groupID+" successfully updated.");
            return true;

        } catch (SQLException ex)
        {
            logger.error("DBHelper.updateGroup - SQLException > ",ex);
            return false;
        }
    }

    public boolean deleteGroup(int groupID)
    {
        try {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM GROUPS WHERE ID=?;");
            stmt.setInt(1,groupID);
            stmt.executeUpdate();

            stmt = connection.prepareStatement("DELETE FROM RULES WHERE GROUP_ID=?;");
            stmt.setInt(1,groupID);
            stmt.executeUpdate();

            stmt = connection.prepareStatement("DELETE FROM USERS_GROUPS_RELATION WHERE GROUP_ID=?;");
            stmt.setInt(1,groupID);
            stmt.executeUpdate();

            logger.info("DBHelper.deleteGroup > Group with ID="+groupID+" deleted successfully. Related rules were removed and users removed from the group");
            stmt.close();
            return true;

        } catch (SQLException ex)
        {
            logger.error("DBHelper.deleteGroup - SQLException > ",ex);
            return false;
        }
    }

    public ArrayList<OMIGroup> getGroups()
    {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM GROUPS" );
            ArrayList<OMIGroup> resultsArray = new ArrayList<OMIGroup>();
            while ( rs.next() ) {
                OMIGroup nextGroup = new OMIGroup();
                nextGroup.id = rs.getInt("ID");
                nextGroup.name = rs.getString("GROUP_NAME");

                //TODO: requires optimization!
                PreparedStatement prst = connection.prepareStatement("SELECT USER_ID FROM USERS_GROUPS_RELATION WHERE GROUP_ID=?;");
                prst.setInt(1, nextGroup.id);
                ResultSet rs2 = prst.executeQuery();

                ArrayList<Integer> userIDs = new ArrayList<Integer>();
                while ( rs2.next() ) {
                    userIDs.add(rs2.getInt("USER_ID"));
                }

                nextGroup.userIDs = userIDs;
                rs2.close();
                prst.close();

                resultsArray.add(nextGroup);
            }
            rs.close();
            stmt.close();
            logger.info("DBHelper.getGroups > Groups fetch request finished. Size:"+resultsArray.size());
            return resultsArray;

        } catch (SQLException ex)
        {
            logger.error("DBHelper.getGroups - SQLException > ",ex);
            return null;
        }
    }

    public ArrayList<OMIGroup> getGroups (int[] groupIDs)
    {
        try {
            if (groupIDs.length < 1)
            {
                return new ArrayList<OMIGroup>();
            }

            String query = "SELECT * FROM GROUPS WHERE ID IN(";

            for (int i = 0; i < groupIDs.length-1; i++) {
                query += "?,";
            }

            query += "?);";
            PreparedStatement stmt = connection.prepareStatement(query);

            for (int i = 0; i < groupIDs.length; i++) {
                stmt.setInt(i+1, groupIDs[i]);
            }

            ResultSet rs = stmt.executeQuery(query);
            ArrayList<OMIGroup> resultsArray = new ArrayList<OMIGroup>();
            while ( rs.next() ) {
                OMIGroup nextGroup = new OMIGroup();
                nextGroup.id = rs.getInt("ID");
                nextGroup.name = rs.getString("GROUP_NAME");

                resultsArray.add(nextGroup);
            }
            rs.close();
            stmt.close();
            logger.info("DBHelper.getGroups > Groups fetch request finished. Size:"+resultsArray.size());
            return resultsArray;

        } catch (SQLException ex)
        {
            logger.error("DBHelper.getGroups - SQLException > ",ex);
            return null;
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
            logger.error("DBHelper.getGroupID - SQLException > ",ex);
            return -1;
        }
    }

    public boolean addUserToGroup (int userID, int groupID)
    {
        try {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO USERS_GROUPS_RELATION(USER_ID,GROUP_ID) VALUES(?,?)");
            stmt.setInt(1,userID);
            stmt.setInt(2,groupID);
            stmt.executeUpdate();
            stmt.close();

            logger.info("DBHelper.addUserToGroup > User with ID="+userID+" successfully added to the group with ID="+groupID);
            return true;

        } catch (SQLException ex)
        {
            logger.error("DBHelper.addUserToGroup - SQLException > ",ex);
            return false;
        }
    }

    public boolean addUsersToGroup (int[] userIDs, int groupID)
    {
        try {
            String query = "INSERT INTO USERS_GROUPS_RELATION(USER_ID,GROUP_ID) VALUES ";

            for (int i = 0; i < userIDs.length-1; i++) {
                query += "(?,?),";
            }

            query += "(?,?);";

            PreparedStatement stmt = connection.prepareStatement(query);
            for (int i = 1; i < userIDs.length+1; i++) {
                stmt.setInt(2*i-1,userIDs[i-1]);
                stmt.setInt(2*i,groupID);
            }
            stmt.executeUpdate();
            stmt.close();

            logger.info("DBHelper.addUsersToGroup > Users list successfully added to the group with ID="+groupID);
            return true;

        } catch (SQLException ex)
        {
            logger.error("DBHelper.addUsersToGroup - SQLException > ",ex);
            return false;
        }
    }

    public boolean removeUserFromGroup (int userID, int groupID)
    {
        try {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM USERS_GROUPS_RELATION WHERE USER_ID=? AND GROUP_ID=?");
            stmt.setInt(1,userID);
            stmt.setInt(2,groupID);
            stmt.executeUpdate();
            stmt.close();

            logger.info("DBHelper.removeUserFromGroup > User with ID="+userID+" successfully removed from the group with ID="+groupID);
            return true;

        } catch (SQLException ex)
        {
            logger.error("DBHelper.removeUserFromGroup - SQLException > ",ex);
            return false;
        }
    }

    public boolean removeUsersFromGroup (int groupID)
    {
        try {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM USERS_GROUPS_RELATION WHERE GROUP_ID=?;");
            stmt.setInt(1, groupID);
            stmt.executeUpdate();
            stmt.close();

            logger.info("DBHelper.removeUsersFromGroup > All users successfully removed from the group with ID="+groupID);
            return true;

        } catch (SQLException ex)
        {
            logger.error("DBHelper.removeUsersFromGroup - SQLException > ",ex);
            return false;
        }
    }

    public boolean updateUsersForGroup (int[] userIDs, int groupID)
    {
        // Delete all users from current group (may be optimized)
        removeUsersFromGroup(groupID);

        // Add users from response
        addUsersToGroup(userIDs, groupID);

        logger.info("DBHelper.updateUsersForGroup > User list successfully updated for the group with ID="+groupID);
        return true;
    }

    public boolean updateOrCreateRule(String HID, int groupID, boolean writable, boolean objectRule)
    {
        try {
            PreparedStatement stmt = connection.prepareStatement("UPDATE RULES SET WRITE_PERMISSIONS = ?, OBJECT_RULE = ? WHERE HID = ? AND GROUP_ID = ?;");
            stmt.setBoolean(1,writable);
            stmt.setBoolean(2,objectRule);
            stmt.setString(3,HID);
            stmt.setInt(4,groupID);
            int rows = stmt.executeUpdate();
            if (rows == 0)
            {
                logger.info("DBHelper.updateOrCreateRule > Record for HID:"+HID+" not found. Creating new.");
                createRule(HID, groupID, writable, objectRule);
            } else {
                logger.info("DBHelper.updateOrCreateRule > Record for HID:"+HID+" was updated.");
            }
            stmt.close();
            return true;

        } catch (SQLException ex)
        {
            logger.error("DBHelper.updateOrCreateRule - SQLException > ",ex);
            return false;
        }
    }

    public boolean createRule(String HID, int groupID, boolean writable, boolean objectRule)
    {
        try {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO RULES(HID,GROUP_ID,WRITE_PERMISSIONS,OBJECT_RULE) VALUES(?,?,?,?)");
            stmt.setString(1,HID);
            stmt.setInt(2,groupID);
            stmt.setBoolean(3,writable);
            stmt.setBoolean(4,objectRule);
            stmt.executeUpdate();
            stmt.close();

            logger.info("DBHelper.createRule > Record for HID:"+HID+" successfully created.");
            return true;

        } catch (SQLException ex)
        {
            logger.error("DBHelper.createRule - SQLException > ",ex);
            return false;
        }
    }

    public ArrayList<OMIRule> getRules(int groupID)
    {
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM RULES WHERE GROUP_ID=?;");
            stmt.setInt(1, groupID);
            ResultSet rs = stmt.executeQuery();
            ArrayList<OMIRule> resultsArray = new ArrayList<OMIRule>();
            while ( rs.next() ) {
                OMIRule nextRule = new OMIRule();
                nextRule.id = rs.getInt("ID");
                nextRule.hid = rs.getString("HID");
                nextRule.groupID = rs.getInt("GROUP_ID");
                nextRule.writePermissions = rs.getInt("WRITE_PERMISSIONS");

                resultsArray.add(nextRule);
            }
            rs.close();
            stmt.close();

            logger.info("DBHelper.getRules > Rules fetch request finished for group:"+groupID+". Size:"+resultsArray.size());
            return resultsArray;

        } catch (SQLException ex)
        {
            logger.error("DBHelper.getRules - SQLException > ",ex);
            return null;
        }
    }

    public boolean deleteRule(String HID, int groupID)
    {
        try {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM RULES WHERE HID=? AND GROUP_ID=?");
            stmt.setString(1,HID);
            stmt.setInt(2,groupID);
            stmt.executeUpdate();
            stmt.close();

            logger.info("DBHelper.deleteRule > Record for HID:"+HID+" successfully deleted.");
            return true;

        } catch (SQLException ex)
        {
            logger.error("DBHelper.deleteRule - SQLException > ",ex);
            return false;
        }
    }

    public ArrayList<OMIUser> getUsers ()
    {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM USERS;" );
            ArrayList<OMIUser> resultsArray = new ArrayList<OMIUser>();
            while ( rs.next() ) {
                OMIUser nextUser = new OMIUser();
                nextUser.id = rs.getInt("ID");
                nextUser.username = rs.getString("USERNAME");
                nextUser.email = rs.getString("EMAIL");

                resultsArray.add(nextUser);
            }
            rs.close();
            stmt.close();

            logger.info("DBHelper.getUsers > Users fetch request finished. Size:"+resultsArray.size());
            return resultsArray;

        } catch (SQLException ex)
        {
            logger.error("DBHelper.getUsers - SQLException > ",ex);
            return null;
        }
    }

    public boolean checkIfUserExists(OMIUser user) {

        try {

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM USERS WHERE EMAIL=?");
            stmt.setString(1, user.email);
            ResultSet rs = stmt.executeQuery();

            return rs.isBeforeFirst();

        } catch (SQLException ex)
        {
            logger.error("DBHelper.checkIfUserExists - SQLException > ",ex);
            return false;
        }

    }

    public int createUser(OMIUser user)
    {
        try {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO USERS(USERNAME,EMAIL) VALUES(?,?)");
            stmt.setString(1,user.username);
            stmt.setString(2,user.email);
            stmt.executeUpdate();

            int res = stmt.getGeneratedKeys().getInt(1);
            stmt = connection.prepareStatement("INSERT INTO USERS_GROUPS_RELATION(USER_ID,GROUP_ID) VALUES(?,?)");
            stmt.setInt(1, res);
            stmt.setInt(2, DEFAULT_GROUP_ID);

            stmt.executeUpdate();
            stmt.close();

            logger.info("DBHelper.createUser > User with name:"+user.username+" and email:" +user.email + " successfully created. ID="+res);
            return res;

        } catch (SQLException ex)
        {
            logger.error("DBHelper.createUser - SQLException > ",ex);
            return -1;
        }
    }

    public boolean createUserIfNotExists(OMIUser user)
    {
        try {

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM USERS WHERE USERNAME=? AND EMAIL=?");
            stmt.setString(1,user.username);
            stmt.setString(2,user.email);
            ResultSet rs = stmt.executeQuery();

            // No such users, insert one
            if (!rs.isBeforeFirst()) {
                stmt = connection.prepareStatement("INSERT INTO USERS(USERNAME,EMAIL) VALUES(?,?)");
                stmt.setString(1,user.username);
                stmt.setString(2,user.email);
                boolean res = (stmt.executeUpdate() != 0);

                if (!res)
                    return false;


                // Add new user to Default group (everybody belongs it)
                int userID = stmt.getGeneratedKeys().getInt(1);
                stmt = connection.prepareStatement("INSERT INTO USERS_GROUPS_RELATION(USER_ID,GROUP_ID) VALUES(?,?)");
                stmt.setInt(1, userID);
                stmt.setInt(2, DEFAULT_GROUP_ID);

                res = (stmt.executeUpdate() != 0);
                stmt.close();
                logger.info("DBHelper.createUserIfNotExists > New user created successfully. ID="+userID);
                return res;
            } else {
                return true;
            }

        } catch (SQLException ex)
        {
            logger.error("DBHelper.createUserIfNotExists - SQLException > ",ex);
            return false;
        }
    }

    public ArrayList<String> getRulesByUser(String userEmail)
    {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT GROUP_ID FROM USERS_GROUPS_RELATION WHERE USER_ID=(SELECT ID FROM USERS WHERE EMAIL=?)")){
            stmt.setString(1,userEmail);
            ResultSet rs = stmt.executeQuery();
            //ArrayList<Integer> groupIDs = new ArrayList<>();

            String groupIDs = "(";

            boolean hasNext = rs.next();
            while (hasNext) {

                groupIDs += rs.getInt(1);
                hasNext = rs.next();
                if (hasNext) {
                    groupIDs += ",";
                }
            }
            groupIDs+= ")";
            stmt.close();


            Statement statement = connection.createStatement();
            rs = statement.executeQuery("SELECT HID FROM RULES WHERE GROUP_ID IN " + groupIDs);
            ArrayList<String> rules = new ArrayList<>();

            while (rs.next()) {
                rules.add(rs.getString(1));
            }
            statement.close();

            return rules;

        } catch (SQLException ex) {
            logger.error("DBHelper.getRulesByUser - SQLException > ",ex);
            return null;
        }
    }

    public boolean checkUserPermissions(ArrayList<String> paths, String userEmail, boolean isWrite)
    {
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT GROUP_ID FROM USERS_GROUPS_RELATION WHERE USER_ID=(SELECT ID FROM USERS WHERE EMAIL=?)");
            stmt.setString(1,userEmail);
            ResultSet rs = stmt.executeQuery();

            String groupIDs = "(";

            boolean hasNext = rs.next();
            while (hasNext) {

                groupIDs += rs.getInt(1);
                hasNext = rs.next();
                if (hasNext) {
                    groupIDs += ",";
                }
            }

            groupIDs+= ")";

            logger.info("DBHelper.checkUserPermissions > Resource access request for groups "+groupIDs);

            stmt.close();
            stmt = connection.prepareStatement("SELECT WRITE_PERMISSIONS FROM RULES WHERE " +
                    "(HID=? OR ((? LIKE '%'||HID||'%') AND OBJECT_RULE=1)) AND GROUP_ID IN " + groupIDs);

            for (String omiPath:paths) {
                stmt.setString(1, omiPath);
                stmt.setString(2, omiPath);
                rs = stmt.executeQuery();

                logger.info("DBHelper.checkUserPermissions >  Checking permissions for HID:"+omiPath);

                // No rules for that HID, deny all request
                if (!rs.isBeforeFirst()) {
                    logger.info("DBHelper.checkUserPermissions > No permissions for HID:"+omiPath+" found. Request denied.");
                    stmt.close();
                    return false;
                } else {
                    boolean db_write = false;
                    while (rs.next()) {
                        db_write = rs.getInt(1) == 1;

                        if (db_write == true)
                            break;
                    }

                    // If in DB we have read permissions but write is requested
                    if (!db_write && isWrite) {
                        logger.info("DBHelper.checkUserPermissions > Read permission for HID:"+omiPath+" found, but write requested. Request denied.");
                        stmt.close();
                        return false;
                    }

                    logger.info("DBHelper.checkUserPermissions > Requested permissions for HID:"+omiPath+" successfully found. Request allowed.");
                }
            }

            return true;

        } catch (SQLException ex)
        {
            logger.error("DBHelper.checkUserPermissions - SQLException > ",ex);
            return false;
        }
    }

}
