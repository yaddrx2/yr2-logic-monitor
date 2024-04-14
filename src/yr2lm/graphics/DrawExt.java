package yr2lm.graphics;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.geom.Vec2;
import arc.util.Align;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;

public class DrawExt {
    public static void select(Vec2 pos, float rad, Color color) {
        float zoom = Core.camera.width / Core.graphics.getWidth();
        Lines.stroke(zoom * 6, Pal.gray);
        Lines.square(pos.x, pos.y, rad + zoom * 2);
        Lines.stroke(zoom * 2, color);
        Lines.square(pos.x, pos.y, rad);
        Draw.reset();
    }

    public static void select(Building building, Color color) {
        select(new Vec2(building.x, building.y), building.block.size * 4, color);
    }

    public static void info(Vec2 pos1, Vec2 pos2, float rad, String name, Color color) {
        select(pos2, rad, color);
        worldLine(pos1, pos2, color);
        screenWorldLine(new Vec2(Core.input.mouse()), pos2, color);
        Fonts.outline.draw(name, pos2.x, pos2.y - rad - 4, color, 0.4f, false, Align.center);
    }

    public static void info(Vec2 pos, Unit unit, String name, Color color) {
        info(pos, new Vec2(unit.x, unit.y), unit.type.hitSize, name, color);
    }

    public static void info(Vec2 pos, Building building, String name, Color color) {
        info(pos, new Vec2(building.x, building.y), building.block.size * 4, name, color);
    }

    public static void screenRect(Vec2 pos, Vec2 size, Color color) {
        float zoom = Core.camera.width / Core.graphics.getWidth();
        Vec2 worldPos = Core.camera.unproject(new Vec2(pos));
        float w = size.x * zoom;
        float h = size.y * zoom;
        Lines.stroke(zoom * 6, Pal.gray);
        Lines.rect(worldPos.x - zoom * 4, worldPos.y - zoom * 4, w + zoom * 8, h + zoom * 8);
        Lines.stroke(zoom * 2, color);
        Lines.rect(worldPos.x - zoom * 2, worldPos.y - zoom * 2, w + zoom * 4, h + zoom * 4);
        Draw.reset();
    }

    public static void screenLine(Vec2 pos1, Vec2 pos2, Color color) {
        worldLine(Core.camera.unproject(new Vec2(pos1)), Core.camera.unproject(new Vec2(pos2)), color);
    }

    public static void screenWorldLine(Vec2 pos1, Vec2 pos2, Color color) {
        worldLine(Core.camera.unproject(new Vec2(pos1)), pos2, color);
    }

    public static void screenWorldLine(Vec2 pos1, Building building, Color color) {
        worldLine(Core.camera.unproject(new Vec2(pos1)), new Vec2(building.x, building.y), color);
    }

    public static void worldLine(Vec2 pos1, Vec2 pos2, Color color) {
        float zoom = Core.camera.width / Core.graphics.getWidth();
        Lines.stroke(zoom * 6);
        Draw.color(Pal.gray, color.a);
        Lines.line(pos1.x, pos1.y, pos2.x, pos2.y);
        Lines.stroke(zoom * 2, color);
        Lines.line(pos1.x, pos1.y, pos2.x, pos2.y);
        Draw.reset();
    }
}
