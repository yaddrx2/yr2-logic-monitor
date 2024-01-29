package yr2lm.ui;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.ui.Styles;
import mindustry.world.blocks.logic.LogicBlock;

import java.util.ArrayList;

public class Combination extends Yrailiuxa2 {

    private Table combinationTable;
    private Table monitorsTable;
    private final ArrayList<LogicMonitor> monitors = new ArrayList<>();
    private boolean binding;

    public Combination(String text) {
        super(text);
        size.set(400, 300);
        minSize.set(200, 150);
        combinationTableInit();
        mainTable.add(combinationTable).grow().left();
    }

    private void combinationTableInit() {
        combinationTable = new Table(t -> {
            t.table(tt -> tt.button("add", Styles.cleart, () -> binding = !binding).grow().update(b -> {
                if (binding) {
                    b.setText("choose logic-process");
                    int x = (int) (Core.input.mouseWorldX() / 8 + 0.5f);
                    int y = (int) (Core.input.mouseWorldY() / 8 + 0.5f);
                    Building selected = Vars.world.build(x, y);
                    if (selected instanceof LogicBlock.LogicBuild logicBuild) {
                        Drawf.select(logicBuild.x, logicBuild.y, logicBuild.block.size * 4, Color.valueOf("00ffff"));
                        if (Core.input.isTouched()) {
                            LogicMonitor logicMonitor = new LogicMonitor(logicBuild.block.name + "(" + x + ", " + y + ")", logicBuild, Core.input.mouse());
                            logicMonitor.addToScene();
                            monitors.add(logicMonitor);
                            monitorsTableBuild();
                        }
                    }
                    if (Core.input.isTouched()) {
                        b.setText("add");
                        binding = false;
                    }
                }
            })).growX().height(50);
            t.row();
            monitorsTable = new Table();
            t.add(monitorsTable).grow();
        });
    }

    private void monitorsTableBuild() {
        monitorsTable.clear();
        monitorsTable.table(t -> t.pane(p -> {
            for (LogicMonitor monitor : monitors) {
                p.table(tt -> {
                    tt.table(ttt -> ttt.labelWrap(monitor.name).grow()).grow().pad(0, 10, 0, 5);
                    tt.table(ttt -> {
                        ttt.button("show", Styles.cleart, () -> monitor.hidden = !monitor.hidden).grow()
                                .update(b -> b.setText(monitor.visible ? "show" : "hidden"));
                        ttt.button("refresh", Styles.cleart, monitor::monitorTableInit).grow();
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
