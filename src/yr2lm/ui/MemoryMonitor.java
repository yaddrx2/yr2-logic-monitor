package yr2lm.ui;

import arc.Core;
import arc.math.geom.Vec2;
import arc.scene.Element;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustry.world.blocks.logic.MemoryBlock;

import static arc.scene.ui.TextField.TextFieldFilter.digitsOnly;

public class MemoryMonitor extends Monitor {
    private final MemoryBlock.MemoryBuild memoryBuild;
    private int start, end, step, col;
    public MemoryMonitor(String s, MemoryBlock.MemoryBuild memoryBuild, Vec2 pos) {
        super(s, memoryBuild, pos);
        this.memoryBuild = memoryBuild;
        start = 0;
        end = ((MemoryBlock) this.memoryBuild.block).memoryCapacity;
        step = 1;
        col = 8;
        init();
    }

    @Override
    public void init() {
        monitorTable.clear();
        monitorTable.table(t -> {
            t.field(String.valueOf(start), digitsOnly, s -> {
                try {
                    start = Math.min(Math.max(0, Integer.parseInt(s)), ((MemoryBlock) this.memoryBuild.block).memoryCapacity);
                } catch (NumberFormatException exception) {
                    start = 0;
                }
            }).minWidth(50).grow();
            t.field(String.valueOf(end), digitsOnly, s -> {
                try {
                    end = Math.min(Math.max(0, Integer.parseInt(s)), ((MemoryBlock) this.memoryBuild.block).memoryCapacity);
                } catch (NumberFormatException exception) {
                    end = ((MemoryBlock) this.memoryBuild.block).memoryCapacity;
                }
            }).minWidth(50).grow();
            t.field(String.valueOf(step), digitsOnly, s -> {
                try {
                    step = Integer.parseInt(s);
                } catch (NumberFormatException exception) {
                    step = 1;
                }
            }).minWidth(50).grow();
            t.field(String.valueOf(col), digitsOnly, s -> {
                try {
                    col = Math.max(Integer.parseInt(s), 1);
                } catch (NumberFormatException exception) {
                    col = 8;
                }
            }).minWidth(50).grow();
            t.button(Icon.refresh, Styles.emptyi, this::init).grow();
        }).height(40).grow();
        monitorTable.row();
        monitorTable.table(t -> t.pane(p -> {
            for (int i = start; i < end; i += step) {
                int finalI = i;
                p.labelWrap(() -> String.valueOf(memoryBuild.memory[finalI])).minHeight(35).growX().pad(0, 5, 0, 5);
                if (i % col == col - 1) p.row();
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
}
