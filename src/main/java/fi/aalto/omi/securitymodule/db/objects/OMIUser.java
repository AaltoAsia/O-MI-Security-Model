package fi.aalto.omi.securitymodule.db.objects;


public class OMIUser {

    /** type of a user */
    public enum OMIUserType {
        OAuth,
        Shibboleth,
        Unknown;

    }

    // is not stored anywhere in DB for now
//    public boolean isUserAuthorized;
    // is not stored anywhere in DB for now
//    public OMIUserType userType;

    public int id;
    public String username;
    public String email;

//    public OMIUser(OMIUserType userType)
//    {
//        this.userType = userType;
//    }


}
