package yr2lm;

import arc.Events;
import arc.util.Time;
import mindustry.game.EventType;
import mindustry.mod.Mod;


public class Yr2lmain extends Mod {
    public Yr2lmain() {
        Events.on(EventType.ContentInitEvent.class, e -> Time.runTask(10f, Yr2Vars.combination::addToScene));
        Events.on(EventType.WorldLoadEvent.class, e -> Yr2Vars.combination.clearMonitor());
    }
}
