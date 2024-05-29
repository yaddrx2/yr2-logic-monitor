package yr2lm.ui;

import arc.Core;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustry.world.blocks.logic.MemoryBlock;

import java.math.BigDecimal;
import java.util.ArrayList;

import static arc.scene.ui.TextField.TextFieldFilter.digitsOnly;

public class MemoryMonitor extends Monitor {
    private final MemoryBlock.MemoryBuild memoryBuild;
    private int start = 0, end, step = 1, col = 8;
    private boolean editMode = false, binMode = false;
    private double[] memoryBuf;
    private final Table memTools, nomPage, binPage;

    private class BinCell extends Table {

        private final ArrayList<String> binText;

        public BinCell(int index) {
            super();
            binText = new ArrayList<>();
            for (int i = 0; i < 64; i++) binText.add("0");
            label(() -> binText.get(0)).grow();
            table().grow();
            for (int i = 1; i < 12; i++) {
                int finalI = i;
                label(() -> binText.get(finalI)).grow();
            }
            row();
            for (int i = 12; i < 64; i++) {
                int finalI = i;
                label(() -> binText.get(finalI)).grow();
                if (i % 13 == 11) row();
            }
            update(() -> {
                String[] binaryStr = Long.toBinaryString(Double.doubleToLongBits(memoryBuild.memory[index])).split("");
                int length = 64 - binaryStr.length;
                for (int i = 0; i < length; i++) binText.set(i, "0");
                for (int i = length; i < 64; i++) binText.set(i, binaryStr[i - length]);
            });
        }
    }

    public MemoryMonitor(String s, MemoryBlock.MemoryBuild memoryBuild, Vec2 pos) {
        super(s, memoryBuild, pos);
        this.memoryBuild = memoryBuild;
        memTools = new Table();
        nomPage = new Table();
        binPage = new Table();
        end = ((MemoryBlock) this.memoryBuild.block).memoryCapacity;
        memToolsBuild();
        init();
    }

    @Override
    public void init() {
        nomPageBuild();
        binPageBuild();
        monitorTableBuild();
    }

    private void monitorTableBuild() {
        monitorTable.clear();
        monitorTable.top();
        monitorTable.add(memTools).growX();
        monitorTable.row();
        if (binMode) monitorTable.add(binPage).grow();
        else monitorTable.add(nomPage).grow();
    }

    private void memToolsBuild() {
        memTools.table(t -> {
            t.field(String.valueOf(start), digitsOnly, s -> {
                try {
                    start = Math.min(Math.max(0, Integer.parseInt(s)), ((MemoryBlock) this.memoryBuild.block).memoryCapacity);
                } catch (NumberFormatException exception) {
                    start = 0;
                }
            }).minWidth(0).grow().pad(0, 5, 0, 5);
            t.field(String.valueOf(end), digitsOnly, s -> {
                try {
                    end = Math.min(Math.max(0, Integer.parseInt(s)), ((MemoryBlock) this.memoryBuild.block).memoryCapacity);
                } catch (NumberFormatException exception) {
                    end = ((MemoryBlock) this.memoryBuild.block).memoryCapacity;
                }
            }).minWidth(0).grow().pad(0, 5, 0, 5);
            t.field(String.valueOf(step), digitsOnly, s -> {
                try {
                    step = Integer.parseInt(s);
                } catch (NumberFormatException exception) {
                    step = 1;
                }
            }).minWidth(0).grow().pad(0, 5, 0, 5);
            t.field(String.valueOf(col), digitsOnly, s -> {
                try {
                    col = Math.max(Integer.parseInt(s), 1);
                } catch (NumberFormatException exception) {
                    col = 8;
                }
            }).minWidth(0).grow().pad(0, 5, 0, 5);
            t.button(Icon.refresh, Styles.emptyi, this::init).size(50);
            t.button(Icon.grid, Styles.emptyi, () -> {
                binMode = !binMode;
                if (editMode) {
                    memoryBuild.memory = memoryBuf;
                    editMode = false;
                }
                monitorTableBuild();
            }).size(50);
            t.button(Icon.edit, Styles.emptyi, () -> {
                if (editMode) {
                    memoryBuild.memory = memoryBuf;
                    editMode = false;
                } else {
                    memoryBuf = memoryBuild.memory.clone();
                    editMode = true;
                }
                init();
            }).size(50);
        }).height(40).growX();
    }

    private void nomPageBuild() {
        nomPage.clear();
        if (editMode) nomPage.table(t -> t.pane(p -> {
            p.top();
            int bound = end / step;
            for (int i = start; i < bound; i++) {
                int index = i * step;
                if (i % col == 0) p.labelWrap("#" + index).size(60, 40).pad(0, 10, 0, 5);
                p.field(BigDecimal.valueOf(memoryBuild.memory[index]).stripTrailingZeros().toPlainString(), s -> {
                    try {
                        memoryBuf[index] = Double.parseDouble(s);
                    } catch (NumberFormatException exception) {
                        memoryBuf[index] = 0;
                    }
                }).minWidth(0).growX().pad(0, 5, 0, 5);
                if (i % col == col - 1) p.row();
            }
        }).grow().update(p -> {
            Element e = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
            if (e != null && e.isDescendantOf(p)) p.requestScroll();
            else if (p.hasScroll()) Core.scene.setScrollFocus(null);
        }).with(p -> {
            p.setupFadeScrollBars(0.5f, 0.25f);
            p.setFadeScrollBars(true);
            p.setScrollingDisabled(true, false);
        })).grow();
        else nomPage.table(t -> t.pane(p -> {
            p.top();
            int bound = end / step;
            for (int i = start; i < bound; i++) {
                int index = i * step;
                if (i % col == 0) p.labelWrap("#" + index).size(60, 35).pad(0, 10, 0, 5);
                Label label = new Label(() -> BigDecimal.valueOf(memoryBuild.memory[index]).stripTrailingZeros().toPlainString());
                label.setWrap(true);
                label.setAlignment(Align.right);
                p.add(label).minHeight(35).growX().pad(0, 5, 0, 5);
                if (i % col == col - 1) p.row();
            }
        }).grow().update(p -> {
            Element e = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
            if (e != null && e.isDescendantOf(p)) p.requestScroll();
            else if (p.hasScroll()) Core.scene.setScrollFocus(null);
        }).with(p -> {
            p.setupFadeScrollBars(0.5f, 0.25f);
            p.setFadeScrollBars(true);
            p.setScrollingDisabled(true, false);
        })).grow();
    }

    private void binPageBuild() {
        binPage.clear();
        binPage.table(t -> t.pane(p -> {
            p.top();
            int bound = end / step;
            for (int i = start; i < bound; i++) {
                int index = i * step;
                p.labelWrap("#" + index).size(60, 35).top().pad(0, 10, 0, 5);
                p.add(new BinCell(index)).height(175).growX().pad(0, 5, 0, 10);
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
        })).grow();
    }

    @Override
    public MemoryBlock.MemoryBuild getBuilding() {
        return memoryBuild;
    }
}
