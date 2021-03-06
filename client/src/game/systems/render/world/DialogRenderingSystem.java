package game.systems.render.world;

import com.artemis.Aspect;
import com.artemis.E;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import entity.character.parts.Body;
import entity.world.Dialog;
import game.handlers.DescriptorHandler;
import game.utils.Fonts;
import position.Pos2D;
import position.WorldPos;
import shared.model.map.Tile;
import shared.util.Util;

@Wire(injectInherited=true)
public class DialogRenderingSystem extends RenderingSystem {

    private static final int ALPHA_TIME = 2;
    private static final int MAX_LENGTH = (int) (120 * SCALE);
    private static final int DISTANCE_TO_TOP = (int) (5 * SCALE);
    private static final float TIME = 0.3f;
    private static final float VELOCITY = DISTANCE_TO_TOP / TIME;

    public DialogRenderingSystem(SpriteBatch batch) {
        super(Aspect.all(Dialog.class, Body.class, WorldPos.class), batch, CameraKind.WORLD);
    }

    @Override
    protected void process(E player) {
        Pos2D playerPos = Util.toScreen(player.worldPosPos2D());
        Dialog dialog = player.getDialog();
        dialog.time -= world.getDelta();
        if (dialog.time > 0) {
            BitmapFont font = dialog.kind.equals(Dialog.Kind.MAGIC_WORDS) ? Fonts.MAGIC_FONT : Fonts.DIALOG_FONT;
            Color copy = font.getColor().cpy();
            if (dialog.time < ALPHA_TIME) {
                dialog.alpha = dialog.time / ALPHA_TIME;
                font.getColor().a = dialog.alpha;
            }

            Fonts.dialogLayout.setText(font, dialog.text);
            float width = Math.min(Fonts.dialogLayout.width, MAX_LENGTH);
            Fonts.dialogLayout.setText(font, dialog.text, font.getColor(), width, Align.center | Align.top, true);
            final float fontX = playerPos.x + (Tile.TILE_PIXEL_WIDTH - width) / 2;
            float up = Dialog.DEFAULT_TIME - dialog.time <= TIME ? (Dialog.DEFAULT_TIME - dialog.time) * VELOCITY : DISTANCE_TO_TOP;
            float offsetY = DescriptorHandler.getBody(player.getBody().index).getHeadOffsetY() * SCALE;
            final float fontY = playerPos.y - 60 * SCALE + offsetY - up + Fonts.dialogLayout.height;
            font.draw(getBatch(), Fonts.dialogLayout, fontX, fontY);
            font.setColor(copy);
        } else {
            player.removeDialog();
        }
    }

}
