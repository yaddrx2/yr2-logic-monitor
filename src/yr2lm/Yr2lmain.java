package yr2lm;

import arc.Events;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.mod.*;
import yr2lm.ui.LogicMonitor;

public class Yr2lmain extends Mod{
    public Yr2lmain(){
        Events.on(EventType.ClientLoadEvent.class, e -> Time.runTask(10f, () -> {
            LogicMonitor logicMonitor = new LogicMonitor("logic");
            logicMonitor.addToScene();
            logicMonitor.visible(() -> Vars.state.isGame() && Vars.ui.hudfrag.shown);
        }));
    }
}
