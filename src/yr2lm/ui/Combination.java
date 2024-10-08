package yr2lm.ui;

import arc.Core;
import arc.graphics.Color;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;
import arc.util.serialization.SerializationException;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.io.JsonIO;
import mindustry.ui.Styles;
import mindustry.world.blocks.logic.LogicBlock;
import mindustry.world.blocks.logic.MemoryBlock;
import mindustry.world.blocks.logic.MessageBlock;
import yr2lm.Yr2lmain;
import yr2lm.graphics.DrawExt;

import java.math.BigDecimal;
import java.util.ArrayList;

public class Combination extends Yrailiuxa2 {

    private Table combinationTable;
    private final Table monitorsTable;

    private boolean binding = false, copying = false, pasting = false;

    private class MonitorCell extends Table {
        public Building building;
        private final Monitor monitor;

        public MonitorCell(Monitor monitorInit) {
            super();
            monitor = monitorInit;
            building = monitor.getBuilding();
            table(t -> {
                t.table(tt -> tt.labelWrap(() -> {
                    Element e = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
                    if (e != null && e.isDescendantOf(monitor)) {
                        return "[#00ffff]" + monitor.name;
                    } else return monitor.name;
                }).grow()).grow().pad(0, 10, 0, 5);
                t.table(tt -> {
                    ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle(Styles.emptyi);
                    ImageButton visibleButton = tt.button(Icon.eyeSmall, Styles.emptyi, () -> {}).size(35).get();
                    visibleButton.clicked(() -> {
                        monitor.hidden = !monitor.hidden;
                        style.imageUp = monitor.hidden ? Icon.eyeOffSmall : Icon.eyeSmall;
                        visibleButton.setStyle(style);
                    });
                    tt.button(Icon.refresh, Styles.emptyi, monitor::init).size(35);
                    tt.button(Icon.trash, Styles.emptyi, () -> {
                        monitors.remove(monitor);
                        monitor.removeFromScene();
                        monitorsTableBuild();
                    }).size(35);
                }).pad(0, 5, 0, 10);
            }).height(35).growX();
        }

        public void drawInfo() {
            DrawExt.select(building, Color.valueOf("00ffff"));
            DrawExt.screenWorldLine(new Vec2(Core.input.mouse()), building, Color.valueOf("00ffff"));
            if (!monitor.hidden) {
                DrawExt.screenRect(monitor.pos, monitor.size, Color.valueOf("00ffff"));
                DrawExt.screenLine(new Vec2(Core.input.mouse()), new Vec2(monitor.pos).mulAdd(monitor.size, 0.5f), Color.valueOf("00ffff"));
            }
        }

        public void removeFromScene() {
            monitor.removeFromScene();
            monitors.remove(monitor);
        }
    }

    private final ArrayList<Monitor> monitors;
    private final ArrayList<Class<? extends Building>> molds;
    private final ArrayList<MonitorCell> monitorCells;

    public Combination() {
        super("yr2lm-" + Vars.mods.getMod(Yr2lmain.class).meta.version);
        size.set(400, 300);
        minSize.set(200, 150);
        monitorsTable = new Table();
        combinationTableInit();
        mainTable.add(combinationTable).grow().left();
        monitors = new ArrayList<>();
        molds = new ArrayList<>();
        molds.add(LogicBlock.LogicBuild.class);
        molds.add(MemoryBlock.MemoryBuild.class);
        molds.add(MessageBlock.MessageBuild.class);
        monitorCells = new ArrayList<>();
    }

    private void combinationTableInit() {
        combinationTable = new Table(t -> {
            t.table(tt -> {
                tt.button("[grey]add", Styles.cleart, () -> binding = !binding).grow().update(b -> {
                    if (binding) {
                        b.setText("add");
                        Building selected = getWorldBuild();
                        if (selected != null && molds.contains(selected.getClass())) {
                            DrawExt.select(selected, Color.valueOf("00ffff"));
                            if (Core.input.isTouched()) {
                                addToCombination(selected);
                                selected.deselect();
                            }
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
                            DrawExt.select(selected, Color.valueOf("ffff00"));
                            if (Core.input.isTouched()) {
                                copyConfig(selected);
                                selected.deselect();
                            }
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
                            DrawExt.select(selected, Color.valueOf("ff00ff"));
                            if (Core.input.isTouched()) {
                                pasteConfig(selected);
                                selected.deselect();
                            }
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
        } else if (building instanceof MessageBlock.MessageBuild messageBuild) {
            String x = BigDecimal.valueOf(messageBuild.x / 8).stripTrailingZeros().toPlainString();
            String y = BigDecimal.valueOf(messageBuild.y / 8).stripTrailingZeros().toPlainString();
            monitor = new MessageMonitor(messageBuild.block.name + "(" + x + ", " + y + ")", messageBuild, Core.input.mouse());
        }
        assert monitor != null;
        monitor.addToScene();
        monitors.add(monitor);
        monitorsTableBuild();
    }

    private void monitorsTableBuild() {
        monitorCells.clear();
        monitorsTable.clear();
        monitorsTable.table(t -> t.pane(p -> {
            p.top();
            monitors.forEach(monitor -> {
                MonitorCell monitorCell = new MonitorCell(monitor);
                monitorCells.add(monitorCell);
                p.add(monitorCell).growX();
                p.row();
            });
        }).grow().update(p -> {
            Element e = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
            if (e != null && e.isDescendantOf(p)) {
                p.requestScroll();
                monitorCells.stream().filter(e::isDescendantOf).forEach(MonitorCell::drawInfo);
            } else if (p.hasScroll()) Core.scene.setScrollFocus(null);
            for (MonitorCell monitorCell : monitorCells) {
                if (Vars.world.build(monitorCell.building.pos()) != monitorCell.building) {
                    monitorCell.removeFromScene();
                    monitorsTableBuild();
                    return;
                }
            }
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
        else if (building instanceof MemoryBlock.MemoryBuild memoryBuild)
            Core.app.setClipboardText(JsonIO.write(memoryBuild.memory));
        else if (building instanceof MessageBlock.MessageBuild messageBuild)
            Core.app.setClipboardText(JsonIO.write(messageBuild.message.toString()));
    }

    private void pasteConfig(Building building) {
        String clipText = Core.app.getClipboardText();
        if (clipText == null) return;

        if (building instanceof LogicBlock.LogicBuild logicBuild) {
            logicBuild.updateCode(clipText.replace("\r\n", "\n"));
        } else if (building instanceof MemoryBlock.MemoryBuild memoryBuild) {
            try {
                double[] memory = JsonIO.read(
                    memoryBuild.memory.getClass(),
                    clipText
                );
                System.arraycopy(
                    memory,
                    0,
                    memoryBuild.memory,
                    0,
                    Math.min(memoryBuild.memory.length, memory.length)
                );
            } catch (SerializationException ignored) {}
        } else if (building instanceof MessageBlock.MessageBuild messageBuild) {
            messageBuild.configure(clipText.replace("\r\n", "\n"));
        }
    }

    public void clearMonitor() {
        monitors.forEach(Yrailiuxa2::removeFromScene);
        monitors.clear();
        monitorsTableBuild();
    }

}
