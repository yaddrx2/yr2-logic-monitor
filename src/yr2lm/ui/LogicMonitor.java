package yr2lm.ui;

import arc.Core;
import arc.graphics.Color;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.logic.LExecutor;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.world.blocks.logic.LogicBlock;

import java.math.BigDecimal;
import java.util.ArrayList;

public class LogicMonitor extends Yrailiuxa2{

    private final Table monitorTable = new Table();
    private final LogicBlock.LogicBuild logicBuild;

    private final ArrayList<LExecutor.Var> constants = new ArrayList<>();
    private final ArrayList<LExecutor.Var> links = new ArrayList<>();
    private class VarTable extends Table {
        public VarTable(LExecutor.Var var, String varName) {
            super();
            table(t -> {
                t.table(ttt -> ttt.labelWrap(varName).minWidth(150).grow()).grow().pad(0, 10, 0, 5);
                t.table(ttt -> ttt.labelWrap(() -> formatVarText(var)).minWidth(200).grow()).grow().pad(0, 5, 0, 10);
            }).minHeight(35).growX();
            if (var.isobj) {
                if (var.objval instanceof String) return;
                update(() -> {
                    Element e = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
                    if (e == null) return;
                    if (e.isDescendantOf(this)) {
                        if (var.objval instanceof Unit unit) {
                            Drawf.select(unit.x, unit.y, unit.type.hitSize, Color.valueOf("00ffff"));
                            Drawf.line(Color.valueOf("00ffff"), logicBuild.x, logicBuild.y, unit.x, unit.y);
                            Fonts.outline.draw(var.name, unit.x, unit.y - unit.type.hitSize - 4, Color.valueOf("00ffff"), 0.4f, false, Align.center);
                        } else if (var.objval instanceof Building building) {
                            Drawf.select(building.x, building.y, building.block.size * 4, Color.valueOf("00ffff"));
                            Drawf.line(Color.valueOf("00ffff"), logicBuild.x, logicBuild.y, building.x, building.y);
                            Fonts.outline.draw(var.name, building.x, building.y - building.block.size * 4 - 4, Color.valueOf("00ffff"), 0.4f, false, Align.center);
                        }
                    }
                });
            }
        }
    }

    public LogicMonitor(String text, LogicBlock.LogicBuild logicBuild, Vec2 pos) {
        super(text);
        this.logicBuild = logicBuild;
        this.pos.set(pos.sub(0, 300));
        size.set(400, 300);
        minSize.set(400, 300);
        monitorTableInit();
        mainTable.add(monitorTable).grow().left();
        mainTable.update(() -> {
            Element e = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
            if (e != null && e.isDescendantOf(mainTable))
                Drawf.select(logicBuild.x, logicBuild.y, logicBuild.block.size * 4, Color.valueOf("00ffff"));
        });
    }

    public void monitorTableInit() {
        monitorTable.clear();
        constants.clear();
        links.clear();
        monitorTable.table(t -> {
            t.button(Icon.rotate, Styles.emptyi, () -> {
                if (logicBuild.executor.vars.length == 0) return;
                logicBuild.executor.vars[0].numval = 0;
            }).grow();
            t.button(Icon.trash, Styles.emptyi, () -> {
                logicBuild.updateCode(logicBuild.code);
                monitorTableInit();
            }).grow();

        }).height(40).grow();
        monitorTable.row();
        monitorTable.table(t -> t.pane(p -> {
            for (LExecutor.Var var : logicBuild.executor.vars) {
                if (!var.constant) {
                    VarTable varTable = new VarTable(var, var.name);
                    p.add(varTable).growX();
                    p.row();
                } else if (var.name.startsWith("@")) {
                    constants.add(var);
                } else if (!var.name.startsWith("___")) {
                    links.add(var);
                }
            }
            for (LExecutor.Var var : constants) {
                VarTable varTable = new VarTable(var, var.name);
                p.add(varTable).growX();
                p.row();
            }
            for (int i = 0; i < links.size(); i++) {
                LExecutor.Var var = links.get(i);
                VarTable varTable = new VarTable(var, "[" + i + "] " + var.name);
                p.add(varTable).growX();
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

    private String formatVarText(LExecutor.Var var) {
        if (var.isobj)
            if (var.objval instanceof String) return '"' + var.objval.toString() + '"';
            else if (var.objval == null) return "null";
            else if (var.objval instanceof Unit unit) {
                return '[' + unit.type.name + '#' + unit.id + "]\n[" + BigDecimal.valueOf(unit.flag).stripTrailingZeros().toPlainString() + "]";
            } else if (var.objval instanceof Building building) {
                return building.block.name + '#' + building.id;
            } else return var.objval.toString();
        else return BigDecimal.valueOf(var.numval).stripTrailingZeros().toPlainString();
    }
}
