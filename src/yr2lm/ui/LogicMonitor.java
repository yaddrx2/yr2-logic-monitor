package yr2lm.ui;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.logic.LExecutor;
import mindustry.ui.Styles;
import mindustry.world.blocks.logic.LogicBlock;

import java.math.BigDecimal;
import java.util.ArrayList;

public class LogicMonitor extends Yrailiuxa2{

    private Table monitorTable;
    private Table varsTable;
    private LogicBlock.LogicBuild logicBuild;
    private boolean binding;

    private final ArrayList<LExecutor.Var> constants = new ArrayList<>();
    private final ArrayList<LExecutor.Var> links = new ArrayList<>();

    public LogicMonitor(String text) {
        super(text);
    }
    @Override
    public void init() {
        size.set(400, 300);
        minSize.set(400, 300);
        monitorTableInit();
        mainTable.add(monitorTable).grow().left();
    }

    private void monitorTableInit() {
        monitorTable = new Table(t -> {
            t.table(tt -> tt.button("click to bind", Styles.cleart, () -> binding = !binding).grow().update(b -> {
                if (logicBuild != null) {
                    Element e = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
                    if (e != null && e.isDescendantOf(b))
                        Drawf.select(logicBuild.x, logicBuild.y, logicBuild.block.size * 4, Color.valueOf("00ffff"));
                }
                if (binding) {
                    b.setText("binding");
                    if (Core.input.isTouched()) {
                        Building selected = Vars.world.build((int) (Core.input.mouseWorldX() / 8 + 0.5f), (int) (Core.input.mouseWorldY() / 8 + 0.5f));
                        if (selected instanceof LogicBlock.LogicBuild) {
                            logicBuild = (LogicBlock.LogicBuild) selected;
                            b.setText(logicBuild.x / 8 + ", " + logicBuild.y / 8);
                        } else {
                            logicBuild = null;
                            b.setText("click to bind");
                        }
                        binding = false;
                        varsTableBuild();
                    }
                }
            })).growX().minWidth(5).height(50);
            t.row();
            varsTable = new Table();
            t.add(varsTable).grow();
        });
    }

    private void varsTableBuild() {
        varsTable.clear();
        constants.clear();
        links.clear();
        if (logicBuild == null) return;
        varsTable.table(t -> t.pane(p -> {
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
                return '[' + unit.type.name + '#' + unit.id + "]\n[" + unit.flag + "]";
            } else if (var.objval instanceof Building building) {
                return building.block.name + '#' + building.id;
            } else return var.objval.toString();
        else return new BigDecimal(Double.toString(var.numval)).toPlainString();
    }
}
