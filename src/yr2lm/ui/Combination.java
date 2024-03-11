package yr2lm.ui;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;
import arc.util.serialization.SerializationException;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.graphics.Drawf;
import mindustry.io.JsonIO;
import mindustry.ui.Styles;
import mindustry.world.blocks.logic.LogicBlock;
import mindustry.world.blocks.logic.MemoryBlock;

import java.math.BigDecimal;
import java.util.ArrayList;

public class Combination extends Yrailiuxa2 {

    private Table combinationTable;
    private final Table monitorsTable;

    private boolean binding, copying, pasting;

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

            t.table(tt -> {
                tt.button("[grey]add", Styles.cleart, () -> binding = !binding).grow().update(b -> {
                    if (binding) {
                        b.setText("add");
                        Building selected = getWorldBuild();
                        if (selected != null && molds.contains(selected.getClass())) {
                            Drawf.select(selected.x, selected.y, selected.block.size * 4, Color.valueOf("00ffff"));
                            if (Core.input.isTouched()) addToCombination(selected);
                        }
                        if (Core.input.isTouched()) {
                            b.setText("[grey]add");
                            binding = false;
                        }
                    }
                }).grow();
                tt.button("[grey]copy", Styles.cleart, () -> copying = !copying).grow().update(b -> {
                    if (copying) {
                        b.setText("copy");
                        Building selected = getWorldBuild();
                        if (selected != null && molds.contains(selected.getClass())) {
                            Drawf.select(selected.x, selected.y, selected.block.size * 4, Color.valueOf("ffff00"));
                            if (Core.input.isTouched()) copyConfig(selected);
                        }
                        if (Core.input.isTouched()) {
                            b.setText("[grey]copy");
                            copying = false;
                        }
                    }
                }).grow();
                tt.button("[grey]paste", Styles.cleart, () -> pasting = !pasting).grow().update(b -> {
                    if (pasting) {
                        b.setText("paste");
                        Building selected = getWorldBuild();
                        if (selected != null && molds.contains(selected.getClass())) {
                            Drawf.select(selected.x, selected.y, selected.block.size * 4, Color.valueOf("ffff00"));
                            if (Core.input.isTouched()) pasteConfig(selected);
                        }
                        if (selected == null && Core.input.isTouched()) {
                            b.setText("[grey]paste");
                            pasting = false;
                        }

                    }
                }).grow();
            }).height(50).growX();
            t.row();
            t.add(monitorsTable).grow();
        });
    }

    private void addToCombination(Building building) {
        Monitor monitor = null;
        if (building instanceof LogicBlock.LogicBuild logicBuild) {
            String x = BigDecimal.valueOf(logicBuild.x / 8).stripTrailingZeros().toPlainString();
            String y = BigDecimal.valueOf(logicBuild.y / 8).stripTrailingZeros().toPlainString();
            monitor = new LogicMonitor(logicBuild.block.name + "(" + x + ", " + y + ")", logicBuild, Core.input.mouse());
        } else if (building instanceof MemoryBlock.MemoryBuild memoryBuild) {
            String x = BigDecimal.valueOf(memoryBuild.x / 8).stripTrailingZeros().toPlainString();
            String y = BigDecimal.valueOf(memoryBuild.y / 8).stripTrailingZeros().toPlainString();
            monitor = new MemoryMonitor(memoryBuild.block.name + "(" + x + ", " + y + ")", memoryBuild, Core.input.mouse());
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
            p.top();
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
                    if (e.isDescendantOf(monitorTable))
                        Drawf.select(monitorTable.building.x, monitorTable.building.y, monitorTable.building.block.size * 4, Color.valueOf("00ffff"));
                }
            } else if (p.hasScroll()) Core.scene.setScrollFocus(null);
        }).with(p -> {
            p.setupFadeScrollBars(0.5f, 0.25f);
            p.setFadeScrollBars(true);
        })).grow();

    }

    private Building getWorldBuild() {
        int x = (int) (Core.input.mouseWorldX() / 8 + 0.5f);
        int y = (int) (Core.input.mouseWorldY() / 8 + 0.5f);
        return Vars.world.build(x, y);
    }

    private void copyConfig(Building building) {
        if (building instanceof LogicBlock.LogicBuild logicBuild)
            Core.app.setClipboardText(logicBuild.code);
        if (building instanceof MemoryBlock.MemoryBuild memoryBuild)
            Core.app.setClipboardText(JsonIO.write(memoryBuild.memory));
    }

    private void pasteConfig(Building building) {
        try {
            if (building instanceof LogicBlock.LogicBuild logicBuild)
                logicBuild.updateCode(Core.app.getClipboardText().replace("\r\n", "\n"));
            if (building instanceof MemoryBlock.MemoryBuild memoryBuild) {
                double[] memory = JsonIO.read(memoryBuild.memory.getClass(), Core.app.getClipboardText());
                System.arraycopy(memory, 0, memoryBuild.memory, 0, Math.min(memoryBuild.memory.length, memory.length));
            }
        } catch (SerializationException ignored) {
        }

    }
}
