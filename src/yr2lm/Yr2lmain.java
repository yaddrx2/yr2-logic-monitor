package yr2lm;

import arc.Events;
import arc.util.Time;
import mindustry.game.EventType;
import mindustry.mod.*;
import yr2lm.ui.Combination;


public class Yr2lmain extends Mod{
    public Yr2lmain(){
        Events.on(EventType.ClientLoadEvent.class, e -> Time.runTask(10f, () -> {
            Combination combination = new Combination("com");
            combination.addToScene();
        }));
    }
}
