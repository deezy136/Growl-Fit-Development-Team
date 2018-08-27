package otf.project.otf.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by denismalcev on 05.06.17.
 */

public class OTFClient extends RealmObject {

    @PrimaryKey
    private String id;
    private String name;
    private int group;

    public OTFClient() {
    }

    public OTFClient(String id, String name, int group) {
        this.id = id;
        this.name = name;
        this.group = group;
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

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }
}
