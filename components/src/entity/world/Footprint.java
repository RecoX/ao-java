package entity.world;

import com.artemis.Component;
import com.badlogic.gdx.utils.TimeUtils;

import java.io.Serializable;

public class Footprint extends Component implements Serializable {

    public long timestamp;
    public int entityId;

    public Footprint() {
    }

    public Footprint(int entityId) {
        this.timestamp = TimeUtils.millis();
        this.entityId = entityId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }
}
