package yr2lm.ui;

import arc.Core;
import arc.graphics.Color;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.ui.ImageButton;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Unit;
import mindustry.logic.LExecutor;
import mindustry.ui.Styles;
import mindustry.world.blocks.logic.LogicBlock;
import yr2lm.graphics.DrawExt;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

public class LogicMonitor extends Monitor {
    private final LogicBlock.LogicBuild logicBuild;

    private String varFilter = "";
    private boolean filterCc = false, filterW = false;
    private boolean showVarPage = true, showEditPage = false;
    private boolean draw = false;
    private boolean pause = false, forward = false, skip = false, stop = false;
    private int counter;
    private final Table varTools, varPage, editTools, editPage;
    private ScrollPane varPanel, editPanel;

    private class VarCell extends Table {
        public LExecutor.Var var;

        public VarCell(LExecutor.Var varInit, String varName) {
            super();
            this.var = varInit;
            table(t -> {
                t.table(tt -> tt.labelWrap(varName).grow()).grow().pad(0, 10, 0, 5);
                t.table(tt -> tt.labelWrap(() -> formatVarText(this.var)).grow()).grow().pad(0, 5, 0, 10);
            }).minHeight(35).growX().update(t -> {
                if (!var.isobj || var.objval instanceof String) return;
                Element e = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
                if (draw || e != null && e.isDescendantOf(t)) {
                    if (var.objval instanceof Unit unit)
                        DrawExt.info(new Vec2(logicBuild.x, logicBuild.y), unit, var.name, Color.valueOf("00ffff"));
                    else if (var.objval instanceof Building building)
                        DrawExt.info(new Vec2(logicBuild.x, logicBuild.y), building, var.name, Color.valueOf("00ffff"));
                }
                if (e != null && e.isDescendantOf(t)) {
                    if (var.objval instanceof Unit unit)
                        DrawExt.screenWorldLine(new Vec2(Core.input.mouse()), unit, Color.valueOf("00ffff"));
                    else if (var.objval instanceof Building building)
                        DrawExt.screenWorldLine(new Vec2(Core.input.mouse()), building, Color.valueOf("00ffff"));
                }
            });
        }

        private String formatVarText(LExecutor.Var var) {
            if (var.isobj) {
                if (var.objval instanceof String) return '"' + var.objval.toString() + '"';
                if (var.objval == null) return "null";
                if (var.objval instanceof Unit unit)
                    return '[' + unit.type.name + '#' + unit.id + "]\n[" + BigDecimal.valueOf(unit.flag).stripTrailingZeros().toPlainString() + "]";
                if (var.objval instanceof Building building)
                    return building.block.name + '#' + building.id;
                return var.objval.toString();
            }
            if (Double.isNaN(var.numval)) return String.valueOf(counter);
            return BigDecimal.valueOf(var.numval).stripTrailingZeros().toPlainString();
        }
    }

    private class CodeCell extends Table {
        private final int line;
        public String code;
        private final String codeOrigin;

        private boolean edit;

        public CodeCell(int line, String code, boolean edit) {
            super();
            this.line = line;
            this.code = code;
            this.codeOrigin = code;
            this.edit = edit;
            codeCellBuild();
        }

        public CodeCell(int line, String code) {
            this(line, code, false);
            codeCells.add(this);
        }

        private void codeCellBuild() {
            clear();
            table(t -> {
                t.label(() -> {
                    if (pause ? counter == line : logicBuild.executor.vars.length > 0 && logicBuild.executor.vars[0].numval == line) {
                        if (breakpoints.contains(line)) return ">[red]>";
                        return ">>";
                    }
                    if (breakpoints.contains(line)) return " [red]>";
                    return "";
                }).width(30).growY().padLeft(10).get().clicked(() -> {
                    if (breakpoints.contains(line)) breakpoints.remove(line);
                    else breakpoints.add(line);
                });
                if (edit) t.field(code, s -> code = s).minWidth(0).grow().pad(0, 5, 0, 5);
                else t.labelWrap(() -> code).grow().pad(0, 5, 0, 5);
                t.button(Icon.pencilSmall, Styles.emptyi, () -> {
                    edit = !edit;
                    codeCellBuild();
                }).size(35).right();
                t.button(Icon.addSmall, Styles.emptyi, () -> {
                    codeCells.add(codeCells.indexOf(this) + 1, new CodeCell(-Math.abs(line), "", true));
                    rebuild(editPanel.getScrollPercentY());
                }).size(35).right();
                t.button(Icon.downSmall, Styles.emptyi, () -> {
                    String clipboard = Core.app.getClipboardText().replace("\r\n", "\n");
                    ArrayList<CodeCell> clipboardList = Arrays.stream(clipboard.split("\n")).map(word -> {
                        ArrayList<String> words = Arrays.stream(word.split(" ")).collect(Collectors.toCollection(ArrayList::new));
                        if (words.get(0).equals("jump")) words.set(1, String.valueOf(Integer.parseInt(words.get(1)) + Math.abs(line) + 1));
                        return new CodeCell(-Math.abs(line), String.join(" ", words), true);
                    }).collect(Collectors.toCollection(ArrayList::new));
                    codeCells.forEach((codeCell -> {
                        ArrayList<String> words = Arrays.stream(codeCell.code.split(" ")).collect(Collectors.toCollection(ArrayList::new));
                        if (words.get(0).equals("jump") && Integer.parseInt(words.get(1)) > Math.abs(line))
                            words.set(1, String.valueOf(Integer.parseInt(words.get(1)) + clipboardList.size()));
                        codeCell.code = String.join(" ", words);
                    }));
                    codeCells.addAll(Math.abs(line) + 1, clipboardList);
                    rebuild(editPanel.getScrollPercentY());
                }).size(35).right();
                t.button(Icon.refreshSmall, Styles.emptyi, () -> {
                    code = codeOrigin;
                    codeCellBuild();
                }).size(35).right();
                t.button(Icon.cancelSmall, Styles.emptyi, () -> {
                    codeCells.remove(this);
                    rebuild(editPanel.getScrollPercentY());
                }).size(35).right();
                t.labelWrap(line < 0 ? "+" : String.valueOf(line)).width(45);
            }).minHeight(35).growX();

        }

        private void rebuild(float scrollPercentY) {
            editPage.clear();
            editPage.top();
            editPage.add(editTools).growX();
            editPage.row();
            editPanel = editPage.pane(p -> {
                p.top();
                codeCells.forEach(codeCell -> {
                    p.add(codeCell).growX();
                    p.row();
                });
            }).grow().update(p -> {
                Element e = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
                if (e != null && e.isDescendantOf(p)) p.requestScroll();
                else if (p.hasScroll()) Core.scene.setScrollFocus(null);
            }).with(p -> {
                p.setupFadeScrollBars(0.5f, 0.25f);
                p.setFadeScrollBars(true);
                p.setScrollingDisabled(true, false);
                Time.run(1f, () -> p.setScrollPercentY(scrollPercentY));
            }).get();
        }
    }

    private final ArrayList<LExecutor.Var> constants, links;
    private final ArrayList<CodeCell> codeCells;
    private final HashSet<Integer> breakpoints;

    public LogicMonitor(String text, LogicBlock.LogicBuild logicBuild, Vec2 pos) {
        super(text, logicBuild, pos);
        this.logicBuild = logicBuild;
        varTools = new Table();
        varPage = new Table();
        editTools = new Table();
        editPage = new Table();
        constants = new ArrayList<>();
        links = new ArrayList<>();
        codeCells = new ArrayList<>();
        breakpoints = new HashSet<>();
        varToolsBuild();
        varPageBuild(0f);
        editToolsBuild();
        editPageBuild(0f);
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
                if (pause) logicPause();
            }).grow();
            t.button(Icon.trash, Styles.emptyi, () -> {
                logicBuild.updateCode(logicBuild.code);
                if (pause) logicPause();
                varPageBuild(varPanel.getScrollPercentY());
            }).grow();
            ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle(Styles.emptyi);
            ImageButton drawButton = t.button(Icon.eyeOffSmall, Styles.emptyi, () -> {
            }).grow().get();
            drawButton.clicked(() -> {
                draw = !draw;
                style.imageUp = draw ? Icon.eyeSmall : Icon.eyeOffSmall;
                drawButton.setStyle(style);
            });
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
            t.button(Icon.zoom, Styles.emptyi, () -> varPageBuild(varPanel.getScrollPercentY())).grow();
            TextButton buttonCc = t.button(filterCc ? "Cc" : "[grey]Cc", Styles.cleart, () -> {
            }).grow().get();
            buttonCc.clicked(() -> {
                filterCc = !filterCc;
                buttonCc.setText(filterCc ? "Cc" : "[grey]Cc");
                varPageBuild(varPanel.getScrollPercentY());
            });
            TextButton buttonW = t.button(filterW ? "W" : "[grey]W", Styles.cleart, () -> {
            }).grow().get();
            buttonW.clicked(() -> {
                filterW = !filterW;
                buttonW.setText(filterW ? "W" : "[grey]W");
                varPageBuild(varPanel.getScrollPercentY());
            });
        }).height(40).growX();
    }

    private void varPageBuild(float scrollPercentY) {
        varPage.clear();
        varPage.top();
        varPage.add(varTools).growX();
        varPage.row();
        constants.clear();
        links.clear();
        varPanel = varPage.pane(p -> {
            p.top();
            Arrays.stream(logicBuild.executor.vars).forEach(var -> {
                if (!var.constant) {
                    if (checkVarName(var.name)) {
                        p.add(new VarCell(var, var.name)).growX();
                        p.row();
                    }
                } else if (var.name.startsWith("@")) {
                    if (checkVarName(var.name)) constants.add(var);
                } else if (!var.name.startsWith("___")) {
                    if (checkVarName(var.name)) links.add(var);
                }
            });
            constants.forEach(var -> {
                p.add(new VarCell(var, var.name)).growX();
                p.row();
            });
            for (int i = 0; i < links.size(); i++) {
                LExecutor.Var var = links.get(i);
                p.add(new VarCell(var, "[" + i + "] " + var.name)).growX();
                p.row();
            }
        }).grow().update(p -> {
            Element e = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
            if (e != null && e.isDescendantOf(p)) p.requestScroll();
            else if (p.hasScroll()) Core.scene.setScrollFocus(null);
        }).with(p -> {
            p.setupFadeScrollBars(0.5f, 0.25f);
            p.setFadeScrollBars(true);
            Time.run(1f, () -> p.setScrollPercentY(scrollPercentY));
        }).get();
    }

    private void editToolsBuild() {
        editTools.table(t -> {
            t.button(Icon.refresh, Styles.emptyi, () -> editPageBuild(editPanel.getScrollPercentY())).grow();
            t.button(Icon.save, Styles.emptyi, this::uploadCode).grow();
            t.button(Icon.edit, Styles.emptyi, () -> {
                if (showVarPage) showEditPage = false;
                else showVarPage = true;
                init();
            }).grow();
        }).height(40).growX();
        editTools.row();
        editTools.table(t -> {
            ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle(Styles.emptyi);
            ImageButton pauseButton = t.button(Icon.pause, Styles.emptyi, () -> {
            }).grow().get();
            t.button(Icon.left, Styles.emptyi, () -> {
                if (pause) forward = true;
            }).grow();
            t.button(Icon.undo, Styles.emptyi, () -> {
                if (pause) {
                    forward = true;
                    if (skip) skip = false;
                    else if (!breakpoints.isEmpty()) skip = true;
                }
            }).grow();
            pauseButton.clicked(() -> {
                pause = !pause;
                forward = false;
                skip = false;
                stop = false;
                if (pause) {
                    logicPause();
                    style.imageUp = Icon.play;
                } else {
                    logicRerun();
                    style.imageUp = Icon.pause;
                }
                pauseButton.setStyle(style);
            });
            pauseButton.update(() -> {
                if (!pause) return;
                if (stop && logicBuild.executor.vars[0].numval != counter) {
                    if (logicBuild.executor.vars[0].numval == codeCells.size())
                        logicBuild.executor.vars[0].numval = 0;
                    if (skip && !breakpoints.contains((int) logicBuild.executor.vars[0].numval)) {
                        counter = (int) logicBuild.executor.vars[0].numval;
                        editPanel.setScrollPercentY(counter / (codeCells.size() - 1f));
                        return;
                    }
                    forward = false;
                    skip = false;
                    stop = false;
                    logicPause();
                } else if (forward) {
                    stop = true;
                    logicRerun();
                }
            });
        }).height(40).growX();
    }

    private void editPageBuild(float scrollPercentY) {
        editPage.clear();
        editPage.top();
        editPage.add(editTools).growX();
        editPage.row();
        codeCells.clear();
        breakpoints.clear();
        editPanel = editPage.pane(p -> {
            p.top();
            ArrayList<String> codeList = Arrays.stream(logicBuild.code.split("\n")).collect(Collectors.toCollection(ArrayList::new));
            for (int i = 0; i < codeList.size(); i++) {
                p.add(new CodeCell(i, codeList.get(i))).growX();
                p.row();
            }
        }).grow().update(p -> {
            Element e = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
            if (e != null && e.isDescendantOf(p)) p.requestScroll();
            else if (p.hasScroll()) Core.scene.setScrollFocus(null);
        }).with(p -> {
            p.setupFadeScrollBars(0.5f, 0.25f);
            p.setFadeScrollBars(true);
            p.setScrollingDisabled(true, false);
            Time.run(1f, () -> p.setScrollPercentY(scrollPercentY));
        }).get();
    }

    private boolean checkVarName(String name) {
        int index;
        if (filterCc) index = name.indexOf(varFilter);
        else index = name.toUpperCase().indexOf(varFilter.toUpperCase());
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

    private void uploadCode() {
        logicBuild.updateCode(codeCells.stream()
                .filter(codeCell -> !codeCell.code.equals(""))
                .map(codeCell -> codeCell.code + "\n")
                .collect(Collectors.joining())
        );
        editPageBuild(editPanel.getScrollPercentY());
    }

    private void logicPause() {
        if (logicBuild.executor.vars.length > 0) counter = (int) logicBuild.executor.vars[0].numval;
        logicBuild.executor.vars[0].numval = Double.NaN;
        editPanel.setScrollPercentY(counter / (codeCells.size() - 1f));
    }

    private void logicRerun() {
        logicBuild.executor.vars[0].numval = counter;
    }

    @Override
    public LogicBlock.LogicBuild getBuilding() {
        return logicBuild;
    }
}
