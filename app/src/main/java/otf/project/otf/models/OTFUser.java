package otf.project.otf.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by denismalcev on 04.06.17.
 */

public class OTFUser extends RealmObject {

    @PrimaryKey
    private String id;

    private String name;
    private String phoneNumber;
    private boolean isInstructor;

    private String groupName;
    private String instructorId;
    private int group;

    public OTFUser() {

    }

    public boolean isInstructor() {
        return isInstructor;
    }

    public void setInstructor(boolean instructor) {
        isInstructor = instructor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(String instructorId) {
        this.instructorId = instructorId;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
