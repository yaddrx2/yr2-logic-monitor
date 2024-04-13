package yr2lm.ui;

import arc.Core;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustry.world.blocks.logic.MessageBlock;

import java.math.BigDecimal;
import java.util.ArrayList;

public class MessageMonitor extends Monitor {
    private final MessageBlock.MessageBuild messageBuild;
    private int mesHash;
    private boolean pause = false;
    private final Table mesTools;

    private class MesCell extends Table {
        public String message;

        public MesCell(String message) {
            super();
            this.message = message;
            table(t -> {
                t.labelWrap(message).grow().pad(0, 10, 0, 5);
                t.button(Icon.copySmall, Styles.emptyi, () -> Core.app.setClipboardText(message)).size(35).right();
                t.button(Icon.cancelSmall, Styles.emptyi, () -> {
                    mesCells.remove(this);
                    init();
                }).size(35).right();
                t.labelWrap(BigDecimal.valueOf(Math.floor(Time.time) % 10000).stripTrailingZeros().toPlainString()).size(60, 35);
            }).minHeight(35).growX();
        }
    }

    private final ArrayList<MesCell> mesCells;

    public MessageMonitor(String text, MessageBlock.MessageBuild messageBuildInit, Vec2 pos) {
        super(text, messageBuildInit, pos);
        messageBuild = messageBuildInit;
        mesHash = messageBuild.message.toString().hashCode();
        mesTools = new Table();
        mesCells = new ArrayList<>();
        mesToolsBuild();
        init();
    }

    @Override
    public void init() {
        if (mesCells.size() == 0) mesCells.add(new MesCell(messageBuild.message.toString()));
        monitorTable.clear();
        monitorTable.add(mesTools).growX();
        monitorTable.row();
        monitorTable.pane(p -> {
            p.top();
            mesCells.forEach(mesCell -> {
                p.add(mesCell).growX();
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
        });
    }

    private void mesToolsBuild() {
        mesTools.clear();
        mesTools.table(t -> {
            ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle(Styles.emptyi);
            ImageButton drawButton = t.button(Icon.pause, Styles.emptyi, () -> {
            }).grow().get();
            drawButton.clicked(() -> {
                pause = !pause;
                style.imageUp = pause ? Icon.play : Icon.pause;
                drawButton.setStyle(style);
            });
            t.button(Icon.refresh, Styles.emptyi, this::init).grow();
            t.button(Icon.trash, Styles.emptyi, () -> {
                mesCells.clear();
                init();
            }).grow();
        }).height(40).growX().update(t -> {
            if (pause) return;
            String message = messageBuild.message.toString();
            if (message.hashCode() != mesHash) {
                mesCells.add(0, new MesCell(message));
                mesHash = message.hashCode();
                init();
            }
        });
    }

    @Override
    public MessageBlock.MessageBuild getBuilding() {
        return messageBuild;
    }
}
