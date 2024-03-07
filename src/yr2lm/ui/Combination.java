package yr2lm.ui;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.graphics.Drawf;
import mindustry.ui.Styles;
import mindustry.world.blocks.logic.LogicBlock;
import mindustry.world.blocks.logic.MemoryBlock;

import java.util.ArrayList;

public class Combination extends Yrailiuxa2 {

    private Table combinationTable;
    private final Table monitorsTable;

    private boolean binding;

    private class MonitorTable extends Table {
        public Building building;

        public MonitorTable(Monitor monitor) {
            super();
            building = monitor.getBuilding();
            table(t -> {
                t.table(tt -> tt.labelWrap(monitor.name).grow()).grow().pad(0, 10, 0, 5);
                t.table(tt -> {
                    ImageButton visibleButton = new ImageButton();
                    ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle(Styles.emptyi);
                    style.imageUp = Icon.eyeSmall;
                    visibleButton.setStyle(style);
                    visibleButton.resizeImage(35);
                    visibleButton.clicked(() -> {
                        monitor.hidden = !monitor.hidden;
                        style.imageUp = monitor.hidden ? Icon.eyeOffSmall : Icon.eyeSmall;
                        visibleButton.setStyle(style);
                    });
                    tt.add(visibleButton);
                    tt.button(Icon.refresh, Styles.emptyi, monitor::init).size(35);
                    tt.button(Icon.trash, Styles.emptyi, () -> {
                        monitors.remove(monitor);
                        monitor.removeFromScene();
                        monitorsTableBuild();
                    }).size(35);
                }).pad(0, 5, 0, 10);
            }).height(35).growX();
        }
    }

    private final ArrayList<Monitor> monitors;
    private final ArrayList<Class<? extends Building>> molds;
    private final ArrayList<MonitorTable> monitorTables;

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
        monitorTables = new ArrayList<>();
    }

    private void combinationTableInit() {
        combinationTable = new Table(t -> {
            t.table(tt -> tt.button("add", Styles.cleart, () -> binding = !binding).grow().update(b -> {
                if (binding) {
                    b.setText("choose building");
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
        monitorTables.clear();
        monitorsTable.clear();
        monitorsTable.table(t -> t.pane(p -> {
            for (Monitor monitor : monitors) {
                MonitorTable monitorTable = new MonitorTable(monitor);
                monitorTables.add(monitorTable);
                p.add(monitorTable).growX();
                p.row();
            }
        }).grow().update(p -> {
            Element e = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
            if (e != null && e.isDescendantOf(p)) {
                p.requestScroll();
                for (MonitorTable monitorTable : monitorTables) {
                    Log.info(e.isDescendantOf(monitorTable));
                    Log.info(monitorTable.isDescendantOf(e));
                    if (e.isDescendantOf(monitorTable))
                        Drawf.select(monitorTable.building.x, monitorTable.building.y, monitorTable.building.block.size * 4, Color.valueOf("00ffff"));
                }
            } else if (p.hasScroll()) Core.scene.setScrollFocus(null);
        }).with(p -> {
            p.setupFadeScrollBars(0.5f, 0.25f);
            p.setFadeScrollBars(true);
        })).grow();

    }
}
