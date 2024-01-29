package yr2lm.ui;

import arc.Core;
import arc.graphics.Color;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.ui.layout.Table;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.logic.LExecutor;
import mindustry.world.blocks.logic.LogicBlock;

import java.math.BigDecimal;
import java.util.ArrayList;

public class LogicMonitor extends Yrailiuxa2{

    private final Table monitorTable = new Table();
    private final LogicBlock.LogicBuild logicBuild;

    private final ArrayList<LExecutor.Var> constants = new ArrayList<>();
    private final ArrayList<LExecutor.Var> links = new ArrayList<>();

    public LogicMonitor(String text, LogicBlock.LogicBuild logicBuild, Vec2 pos) {
        super(text);
        this.logicBuild = logicBuild;
        this.pos.set(pos.sub(0, 300));
        size.set(400, 300);
        minSize.set(400, 300);
        monitorTableInit();
        mainTable.add(monitorTable).grow().left();
    }

    public void monitorTableInit() {
        monitorTable.clear();
        constants.clear();
        links.clear();
        if (logicBuild == null) return;
        monitorTable.table(t -> t.pane(p -> {
            for (LExecutor.Var var : logicBuild.executor.vars) {
                if (!var.constant) {
                    p.table(tt -> {
                        tt.table(ttt -> ttt.labelWrap(var.name).minWidth(150).grow()).grow().pad(0, 10, 0, 5);
                        tt.table(ttt -> ttt.labelWrap(() -> formatVarText(var)).minWidth(200).grow()).grow().pad(0, 5, 0, 10);
                    }).minHeight(35).growX();
                    p.row();
                } else if (var.name.startsWith("@")) {
                    constants.add(var);
                } else if (!var.name.startsWith("___")) {
                    links.add(var);
                }
            }
            for (LExecutor.Var var : constants) {
                p.table(tt -> {
                    tt.table(ttt -> ttt.labelWrap(var.name).minWidth(150).grow()).grow().pad(0, 10, 0, 5);
                    tt.table(ttt -> ttt.labelWrap(() -> formatVarText(var)).minWidth(200).grow()).grow().pad(0, 5, 0, 10);
                }).minHeight(35).growX();
                p.row();
            }
            for (int i = 0; i < links.size(); i++) {
                LExecutor.Var var = links.get(i);
                int finalI = i;
                p.table(tt -> {
                    tt.table(ttt -> ttt.labelWrap("[" + finalI + "] " + var.name).minWidth(150).grow()).grow().pad(0, 10, 0, 5);
                    tt.table(ttt -> ttt.labelWrap(() -> formatVarText(var)).minWidth(200).grow()).grow().pad(0, 5, 0, 10);
                }).minHeight(35).growX();
                p.row();
            }
        }).grow().update(p -> {
            Element e = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
            if (e != null && e.isDescendantOf(p)) {
                Drawf.select(logicBuild.x, logicBuild.y, logicBuild.block.size * 4, Color.valueOf("00ffff"));
                p.requestScroll();
            }
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
                return '[' + unit.type.name + '#' + unit.id + "]\n[" + unit.flag + "]";
            } else if (var.objval instanceof Building building) {
                return building.block.name + '#' + building.id;
            } else return var.objval.toString();
        else return new BigDecimal(Double.toString(var.numval)).toPlainString();
    }
}
