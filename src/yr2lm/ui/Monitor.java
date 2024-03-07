package yr2lm.ui;

import arc.Core;
import arc.graphics.Color;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.ui.layout.Table;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;

public class Monitor extends Yrailiuxa2 {
    protected final Table monitorTable;

    public Monitor(String text, Building building, Vec2 pos) {
        super(text);
        this.monitorTable = new Table();
        this.pos.set(pos.sub(0, 300));
        size.set(400, 300);
        minSize.set(400, 300);
        mainTable.add(monitorTable).grow().left();
        mainTable.update(() -> {
            Element e = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
            if (e != null && e.isDescendantOf(mainTable))
                Drawf.select(building.x, building.y, building.block.size * 4, Color.valueOf("00ffff"));
        });
    }

    public void init() {
    }

    public Building getBuilding() {
        return null;
    }
}
