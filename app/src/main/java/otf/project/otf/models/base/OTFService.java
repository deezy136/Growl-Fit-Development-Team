package otf.project.otf.models.base;

/**
 * Created by denismalcev on 03.06.17.
 */

public abstract class OTFService {

    private final String id;

    public OTFService(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public abstract String getName();
    public abstract String getIp();
    public abstract int getPort();
    public abstract long getCreateTime();

    public String getGroupId() {
        return "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OTFService service = (OTFService) o;

        return id != null ? id.equals(service.id) : service.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
