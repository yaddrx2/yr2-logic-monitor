package yr2lm.ui;

import arc.Core;
import arc.graphics.Color;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.ui.TextButton;
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

public class LogicMonitor extends Monitor {
    private final LogicBlock.LogicBuild logicBuild;

    private String varFilter = "";
    private boolean filterCc = false, filterW = false;
    private boolean showVarPage = true, showEditPage = false;
    private final Table varTools, varPage, editTools, editPage;

    private class VarTable extends Table {
        public LExecutor.Var var;

        public VarTable(LExecutor.Var var, String varName) {
            super();
            this.var = var;
            table(t -> {
                t.table(tt -> tt.labelWrap(varName).grow()).grow().pad(0, 10, 0, 5);
                t.table(tt -> tt.labelWrap(() -> formatVarText(this.var)).grow()).grow().pad(0, 5, 0, 10);
            }).minHeight(35).growX();
        }
    }

    private final ArrayList<LExecutor.Var> constants;
    private final ArrayList<LExecutor.Var> links;
    private final ArrayList<VarTable> varTables;

    public LogicMonitor(String text, LogicBlock.LogicBuild logicBuild, Vec2 pos) {
        super(text, logicBuild, pos);
        this.logicBuild = logicBuild;
        varTools = new Table();
        varPage = new Table();
        editTools = new Table();
        editPage = new Table();
        constants = new ArrayList<>();
        links = new ArrayList<>();
        varTables = new ArrayList<>();
        varToolsBuild();
        varPageBuild();
        editToolsBuild();
        editPageBuild();
        init();
    }

    @Override
    public void init() {
        monitorTable.clear();
        monitorTable.defaults().uniform();
        if (showVarPage) monitorTable.add(varPage).grow();
        if (showEditPage) monitorTable.add(editPage).grow();
    }

    private void varToolsBuild() {
        varTools.table(t -> {
            t.button(Icon.rotate, Styles.emptyi, () -> {
                if (logicBuild.executor.vars.length == 0) return;
                logicBuild.executor.vars[0].numval = 0;
            }).grow();
            t.button(Icon.trash, Styles.emptyi, () -> {
                logicBuild.updateCode(logicBuild.code);
                varPageBuild();
            }).grow();
            t.button(Icon.edit, Styles.emptyi, () -> {
                if (showEditPage) showVarPage = false;
                else showEditPage = true;
                init();
            }).grow();
        }).height(40).growX();
        varTools.row();
        varTools.table(t -> {
            t.defaults().uniform();
            t.field(varFilter, s -> varFilter = s).minWidth(0).padLeft(10).grow();
            t.button(Icon.zoom, Styles.emptyi, this::varPageBuild).grow();
            TextButton buttonCc = new TextButton(filterCc ? "Cc" : "[grey]Cc", Styles.cleart);
            buttonCc.clicked(() -> {
                filterCc = !filterCc;
                buttonCc.setText(filterCc ? "Cc" : "[grey]Cc");
            });
            t.add(buttonCc).grow();
            TextButton buttonW = new TextButton(filterW ? "W" : "[grey]W", Styles.cleart);
            buttonW.clicked(() -> {
                filterW = !filterW;
                buttonW.setText(filterW ? "W" : "[grey]W");
            });
            t.add(buttonW).grow();
        }).height(40).growX();
    }

    private void varPageBuild() {
        varPage.clear();
        varPage.top();
        varPage.add(varTools).growX();
        varPage.row();
        constants.clear();
        links.clear();
        varTables.clear();
        varPage.pane(p -> {
            p.top();
            for (LExecutor.Var var : logicBuild.executor.vars) {
                if (!var.constant) {
                    if (checkVarName(var.name)) {
                        VarTable varTable = new VarTable(var, var.name);
                        varTables.add(varTable);
                        p.add(varTable).growX();
                        p.row();
                    }
                } else if (var.name.startsWith("@")) {
                    if (checkVarName(var.name)) constants.add(var);
                } else if (!var.name.startsWith("___")) {
                    if (checkVarName(var.name)) links.add(var);
                }
            }
            for (LExecutor.Var var : constants) {
                VarTable varTable = new VarTable(var, var.name);
                varTables.add(varTable);
                p.add(varTable).growX();
                p.row();
            }
            for (int i = 0; i < links.size(); i++) {
                LExecutor.Var var = links.get(i);
                VarTable varTable = new VarTable(var, "[" + i + "] " + var.name);
                varTables.add(varTable);
                p.add(varTable).growX();
                p.row();
            }
        }).grow().update(p -> {
            Element e = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
            if (e != null && e.isDescendantOf(p)) {
                p.requestScroll();
                for (VarTable varTable : varTables) {
                    if (e.isDescendantOf(varTable))
                        if (varTable.var.isobj && !(varTable.var.objval instanceof String))
                            if (varTable.var.objval instanceof Unit unit) {
                                Drawf.select(unit.x, unit.y, unit.type.hitSize, Color.valueOf("00ffff"));
                                Drawf.line(Color.valueOf("00ffff"), logicBuild.x, logicBuild.y, unit.x, unit.y);
                                Fonts.outline.draw(varTable.var.name, unit.x, unit.y - unit.type.hitSize - 4, Color.valueOf("00ffff"), 0.4f, false, Align.center);
                            } else if (varTable.var.objval instanceof Building building) {
                                Drawf.select(building.x, building.y, building.block.size * 4, Color.valueOf("00ffff"));
                                Drawf.line(Color.valueOf("00ffff"), logicBuild.x, logicBuild.y, building.x, building.y);
                                Fonts.outline.draw(varTable.var.name, building.x, building.y - building.block.size * 4 - 4, Color.valueOf("00ffff"), 0.4f, false, Align.center);
                            }
                }
            } else if (p.hasScroll()) Core.scene.setScrollFocus(null);
        }).with(p -> {
            p.setupFadeScrollBars(0.5f, 0.25f);
            p.setFadeScrollBars(true);
        });
    }

    private void editToolsBuild() {
        editTools.table(t -> {
            t.button(Icon.edit, Styles.emptyi, () -> {
                if (showVarPage) showEditPage = false;
                else showVarPage = true;
                init();
            }).grow();
            t.row();
        }).height(40).growX();
    }

    private void editPageBuild() {
        editPage.clear();
        editPage.top();
        editPage.add(editTools).growX();
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

    private boolean checkVarName(String name) {
        int index = getIndex(name);
        if (index < 0) return false;
        if (filterW) {
            boolean filter = true;
            if (index > 0)
                filter = !Character.isAlphabetic(name.charAt(index - 1));
            if (index + varFilter.length() < name.length())
                filter &= !Character.isAlphabetic(name.charAt(index + varFilter.length()));
            return filter;
        }
        return true;
    }

    private int getIndex(String name) {
        if (filterCc) {
            return name.indexOf(varFilter);
        } else {
            return name.toUpperCase().indexOf(varFilter.toUpperCase());
        }
    }

    @Override
    public LogicBlock.LogicBuild getBuilding() {
        return logicBuild;
    }
}
