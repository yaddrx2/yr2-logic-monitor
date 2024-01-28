package yr2lm.ui;

import arc.Core;
import arc.input.KeyCode;
import arc.math.geom.Vec2;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import mindustry.ui.Styles;

public class Yrailiuxa2 extends Table {
    private final Table headTable = new Table();
    private final Table sideTable = new Table();
    private final Table bottomTable = new Table();
    private final Table cornerTable = new Table();
    protected final Table mainTable = new Table();
    protected final Vec2 pos = new Vec2(), size = new Vec2(), bias = new Vec2();
    protected final Vec2 minSize = new Vec2();
    public Yrailiuxa2(String text) {
        init();
        setSize(minSize.x, minSize.y);
        background(Styles.black3).top();
        headTableInit(text);
        sizeTableInit();
        add(headTable).growX();
        row();
        table(t -> {
            t.add(mainTable).top().growX();
            t.add(sideTable).width(30).growY();
        }).grow();
        row();
        table(t -> {
            t.add(bottomTable).height(30).growX();
            t.add(cornerTable).size(30);
        }).height(30).growX();
        update(() -> {
            setPosition(pos.x, pos.y);
            setSize(size.x, size.y);
        });
    }
    private void headTableInit(String text) {
        headTable.touchable = Touchable.enabled;
        headTable.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float xDown, float yDown, int pointer, KeyCode button){
                bias.set(xDown, yDown);
                return true;
            }
            @Override
            public void touchDragged(InputEvent event, float xDragged, float yDragged, int pointer){
                pos.add(xDragged - bias.x, yDragged - bias.y);
            }
        });
        Label title = new Label(text);
        title.setAlignment(Align.left);
        headTable.add(title).pad(0, 10, 0, 10).height(30).growX();
    }
    private void sizeTableInit() {
        sideTableInit();
        bottomTableInit();
        cornerTableInit();
    }
    private void sideTableInit() {
        sideTable.touchable = Touchable.enabled;
        sideTable.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float xDown, float yDown, int pointer, KeyCode button){
                bias.x = xDown;
                return true;
            }
            @Override
            public void touchDragged(InputEvent event, float xDragged, float yDragged, int pointer){
                size.x = Math.max(minSize.x, size.x + xDragged - bias.x);
            }
        });
    }
    private void bottomTableInit() {
        bottomTable.touchable = Touchable.enabled;
        bottomTable.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float xDown, float yDown, int pointer, KeyCode button){
                bias.y = yDown;
                return true;
            }
            @Override
            public void touchDragged(InputEvent event, float xDragged, float yDragged, int pointer){
                if (minSize.y < size.y - yDragged + bias.y) {
                    size.y = size.y - yDragged + bias.y;
                    pos.y += yDragged - bias.y;
                } else {
                    pos.y += size.y - minSize.y;
                    size.y = minSize.y;
                }
            }
        });
    }
    private void cornerTableInit() {
        cornerTable.touchable = Touchable.enabled;
        cornerTable.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float xDown, float yDown, int pointer, KeyCode button){
                bias.set(xDown, yDown);
                return true;
            }
            @Override
            public void touchDragged(InputEvent event, float xDragged, float yDragged, int pointer){
                size.x = Math.max(minSize.x, size.x + xDragged - bias.x);
                if (minSize.y < size.y - yDragged + bias.y) {
                    size.y = size.y - yDragged + bias.y;
                    pos.y += yDragged - bias.y;
                } else {
                    pos.y += size.y - minSize.y;
                    size.y = minSize.y;
                }
            }
        });
    }
    public void init() {}
    public void addToScene() {
        Core.scene.root.addChild(this);
    }
}