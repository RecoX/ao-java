package entity.world;

import com.artemis.Component;
import com.artemis.annotations.DelayedComponentRemoval;

import java.io.Serializable;

@DelayedComponentRemoval
public class CombatMessage extends Component implements Serializable {

    public static float DEFAULT_TIME = 1.7f;
    public static float START_ALPHA = 1.7f;
    public static float DEFAULT_OFFSET = 30 * 2;
    public Kind kind;

    public String text;
    public float time = DEFAULT_TIME;
    public float alpha = START_ALPHA;
    public float offset = DEFAULT_OFFSET;

    public CombatMessage() {
    }

    public CombatMessage(String text, Kind kind) {
        this.text = text;
        this.kind = kind;
    }

    public static CombatMessage magic(String text) {
        return new CombatMessage(text, Kind.MAGIC);
    }

    public static CombatMessage stab(String text) {
        return new CombatMessage(text, Kind.STAB);
    }

    public static CombatMessage physic(String text) {
        return new CombatMessage(text, Kind.PHYSICAL);
    }

    public static CombatMessage energy(String text) { return new CombatMessage(text, Kind.ENERGY);}

    public enum Kind {
        MAGIC,
        PHYSICAL,
        ENERGY,
        STAB
    }
}
