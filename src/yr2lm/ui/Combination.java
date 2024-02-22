package yr2lm.ui;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.ui.Styles;
import mindustry.world.blocks.logic.*;

import java.util.ArrayList;

public class Combination extends Yrailiuxa2 {

    private Table combinationTable;
    private final Table monitorsTable;
    private final ArrayList<Monitor> monitors;
    private final ArrayList<Object> molds;

    private boolean binding;

    public Combination(String text) {
        super(text);
        size.set(400, 300);
        minSize.set(200, 150);
        monitorsTable = new Table();
        combinationTableInit();
        mainTable.add(combinationTable).grow().left();
        monitors = new ArrayList<>();
        molds = new ArrayList<>();
        molds.add(LogicBlock.LogicBuild.class);
        molds.add(MemoryBlock.MemoryBuild.class);
    }

    private void combinationTableInit() {
        combinationTable = new Table(t -> {
            t.table(tt -> tt.button("add", Styles.cleart, () -> binding = !binding).grow().update(b -> {
                if (binding) {
                    b.setText("choose logic-process");
                    int x = (int) (Core.input.mouseWorldX() / 8 + 0.5f);
                    int y = (int) (Core.input.mouseWorldY() / 8 + 0.5f);
                    Building selected = Vars.world.build(x, y);
                    if (selected != null && molds.contains(selected.getClass())) {
                        Drawf.select(selected.x, selected.y, selected.block.size * 4, Color.valueOf("00ffff"));
                        if (Core.input.isTouched()) addToCombination(selected);
                    }
                    if (Core.input.isTouched()) {
                        b.setText("add");
                        binding = false;
                    }
                }
            })).growX().height(50);
            t.row();
            t.add(monitorsTable).grow();
        });
    }

    private void addToCombination(Building building) {
        Monitor monitor = null;
        if (building instanceof LogicBlock.LogicBuild logicBuild) {
            monitor = new LogicMonitor(building.block.name + "(" + building.x / 8 + ", " + building.y / 8 + ")", logicBuild, Core.input.mouse());
        } else if (building instanceof MemoryBlock.MemoryBuild memoryBuild) {
            monitor = new MemoryMonitor(building.block.name + "(" + building.x / 8 + ", " + building.y / 8 + ")", memoryBuild, Core.input.mouse());
        }
        assert monitor != null;
        monitor.addToScene();
        monitors.add(monitor);
        monitorsTableBuild();
    }

    private void monitorsTableBuild() {
        monitorsTable.clear();
        monitorsTable.table(t -> t.pane(p -> {
            for (Monitor monitor : monitors) {
                p.table(tt -> {
                    tt.table(ttt -> ttt.labelWrap(monitor.name).grow()).grow().pad(0, 10, 0, 5);
                    tt.table(ttt -> {
                        ttt.button("show", Styles.cleart, () -> monitor.hidden = !monitor.hidden).grow()
                                .update(b -> b.setText(monitor.visible ? "show" : "hidden"));
                        ttt.button("refresh", Styles.cleart, monitor::init).grow();
                        ttt.button("delete", Styles.cleart, () -> {
                            monitors.remove(monitor);
                            monitor.removeFromScene();
                            monitorsTableBuild();
                        }).grow();
                    }).grow().pad(0, 5, 0, 10);
                }).height(35).growX();
                p.row();
            }
        }).grow().update(p -> {
            Element e = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
            if (e != null && e.isDescendantOf(p)) p.requestScroll();
            else if (p.hasScroll()) Core.scene.setScrollFocus(null);
        }).with(p -> {
            p.setupFadeScrollBars(0.5f, 0.25f);
            p.setFadeScrollBars(true);
        })).grow();

    }
}
