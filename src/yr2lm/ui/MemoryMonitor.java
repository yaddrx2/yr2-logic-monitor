package yr2lm.ui;

import arc.Core;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.ui.Label;
import arc.util.Align;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustry.world.blocks.logic.MemoryBlock;

import java.math.BigDecimal;

import static arc.scene.ui.TextField.TextFieldFilter.digitsOnly;

public class MemoryMonitor extends Monitor {
    private final MemoryBlock.MemoryBuild memoryBuild;
    private int start = 0, end, step = 1, col = 8;
    private boolean editMode = false;
    private double[] memory;

    public MemoryMonitor(String s, MemoryBlock.MemoryBuild memoryBuild, Vec2 pos) {
        super(s, memoryBuild, pos);
        this.memoryBuild = memoryBuild;
        end = ((MemoryBlock) this.memoryBuild.block).memoryCapacity;
        init();
    }

    @Override
    public void init() {
        monitorTable.clear();
        monitorTable.top();
        monitorTable.table(t -> {
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
            t.button(Icon.edit, Styles.emptyi, () -> {
                if (editMode) {
                    memoryBuild.memory = memory;
                    editMode = false;
                } else {
                    memory = memoryBuild.memory.clone();
                    editMode = true;
                }
                init();
            }).size(50);
        }).height(40).growX();
        monitorTable.row();
        if (editMode) monitorTable.table(t -> t.pane(p -> {
            p.top();
            int bound = end / step;
            for (int i = start; i < bound; i++) {
                int index = i * step;
                if (i % col == 0) p.labelWrap("#" + index).size(60, 40).pad(0, 10, 0, 5);
                p.field(BigDecimal.valueOf(memoryBuild.memory[index]).stripTrailingZeros().toPlainString(), s -> {
                    try {
                        memory[index] = Double.parseDouble(s);
                    } catch (NumberFormatException exception) {
                        memory[index] = 0;
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
        else monitorTable.table(t -> t.pane(p -> {
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

    @Override
    public MemoryBlock.MemoryBuild getBuilding() {
        return memoryBuild;
    }
}
