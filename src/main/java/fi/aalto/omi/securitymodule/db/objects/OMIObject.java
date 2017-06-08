package fi.aalto.omi.securitymodule.db.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;

/**
 * Created by romanfilippov on 22/11/15.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class OMIObject {

    @XmlElement(name="id",namespace = "http://www.opengroup.org/xsd/odf/1.0/")
    private String id;

    public String xPath;

    @XmlElement(name="Object",namespace = "http://www.opengroup.org/xsd/odf/1.0/")
    private ArrayList<OMIObject> subObjects = new ArrayList<OMIObject>();

    @XmlElement(name="InfoItem",namespace = "http://www.opengroup.org/xsd/odf/1.0/")
    private ArrayList<OMIInfoItem> infoItems = new ArrayList<OMIInfoItem>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<OMIObject> getSubObjects() {
        return subObjects;
    }

    public void setSubObjects(ArrayList<OMIObject> subObjects) {
        this.subObjects = subObjects;
    }

    public ArrayList<OMIInfoItem> getInfoItems() {
        return infoItems;
    }

    public void setInfoItems(ArrayList<OMIInfoItem> infoItems) {
        this.infoItems = infoItems;
    }

}
