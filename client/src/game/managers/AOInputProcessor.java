package game.managers;

import com.artemis.E;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Stage;
import game.AOGame;
import game.screens.GameScreen;
import game.systems.camera.CameraSystem;
import game.systems.network.TimeSync;
import game.ui.GUI;
import game.utils.AOKeys;
import game.utils.AlternativeKeys;
import game.utils.Cursors;
import game.utils.WorldUtils;
import shared.model.AttackType;
import shared.model.Spell;
import shared.network.combat.AttackRequest;
import shared.network.combat.SpellCastRequest;
import shared.network.interaction.DropItem;
import shared.network.interaction.MeditateRequest;
import shared.network.interaction.TakeItemRequest;
import shared.network.interaction.TalkRequest;
import shared.network.inventory.ItemActionRequest;

import java.util.Optional;
import java.util.Random;

import static com.artemis.E.E;

public class AOInputProcessor extends Stage {

    private static final Random r = new Random();
    public static boolean alternativeKeys = false;

    @Override
    public boolean scrolled(int amount) {
        System.out.println("Scrolled: " + amount);
        CameraSystem system = GameScreen.getWorld().getSystem(CameraSystem.class);
        system.zoom(amount, system.ZOOM_TIME);
        return super.scrolled(amount);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        boolean result = super.touchUp(screenX, screenY, pointer, button);
        if (GUI.getSpellView().isOver() || GUI.getInventory().isOver()) {
            return result;
        }
        WorldUtils.mouseToWorldPos().ifPresent(worldPos -> {
            final Optional<Spell> toCast = GUI.getSpellView().toCast;
            if (toCast.isPresent()) {
                Spell spell = toCast.get();
                E player = E.E(GameScreen.getPlayer());
                if (!player.hasAttack() || player.getAttack().interval - GameScreen.getWorld().getDelta() < 0) {
                    TimeSync timeSyncSystem = GameScreen.getWorld().getSystem(TimeSync.class);
                    long rtt = timeSyncSystem.getRtt();
                    long timeOffset = timeSyncSystem.getTimeOffset();
                    GameScreen.getClient().sendToAll(new SpellCastRequest(spell, worldPos, rtt + timeOffset));
                    player.attack();
                } else {
                    // TODO can't attack because interval
                }
                Cursors.setCursor("hand");
                GUI.getSpellView().toCast = Optional.empty();
            } else {
                Optional<String> name = WorldManager.getEntities()
                        .stream()
                        .filter(entity -> E(entity).hasWorldPos() && E(entity).getWorldPos().equals(worldPos))
                        .filter(entity -> E(entity).hasName())
                        .map(entity -> E(entity).getName().text)
                        .findFirst();
                if (name.isPresent()) {
                    GUI.getConsole().addInfo("Ves a " + name.get());
                } else {
                    GUI.getConsole().addInfo("No ves nada interesante");
                }

            }
        });
        return result;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (!GUI.getDialog().isVisible()) {
            if (alternativeKeys) {
                doAlternativeActions(keycode);
            } else {
                doActions(keycode);
            }
        }
        switch (keycode) {
            case AlternativeKeys.TALK:
                toggleDialogText();
                break;
            case Input.Keys.F1:
                alternativeKeys = !alternativeKeys;
                break;
        }

        return super.keyUp(keycode);
    }


    private void doActions(int keycode) {
        switch (keycode) {
            case AOKeys.INVENTORY:
                toggleInventory();
                break;
            case AOKeys.SPELLS:
                toggleSpells();
                break;
            case AOKeys.MEDITATE:
                toggleMeditate();
                break;
            case AOKeys.DROP:
                dropItem();
                break;
            case AOKeys.TAKE:
                takeItem();
                break;
            case AOKeys.EQUIP:
                equip();
                break;
            case AOKeys.USE:
                use();
                break;
            case AOKeys.ATTACK_1:
                attack();
                break;
            case AOKeys.ATTACK_2:
                attack();
                break;
            case Input.Keys.ESCAPE:
                // Disconnect & go back to LoginScreen
                AOGame game = (AOGame) Gdx.app.getApplicationListener();
                // TODO implement
        }
    }

    private void doAlternativeActions(int keycode) {
        switch (keycode) {
            case AlternativeKeys.INVENTORY:
                toggleInventory();
                break;
            case AlternativeKeys.SPELLS:
                toggleSpells();
                break;
            case AlternativeKeys.MEDITATE:
                toggleMeditate();
                break;
            case AlternativeKeys.DROP:
                dropItem();
                break;
            case AlternativeKeys.TAKE:
                takeItem();
                break;
            case AlternativeKeys.EQUIP:
                equip();
                break;
            case AlternativeKeys.USE:
                use();
                break;
            case AlternativeKeys.ATTACK_1:
                attack();
                break;
            case AlternativeKeys.ATTACK_2:
                attack();
                break;
            case Input.Keys.ESCAPE:
                // Disconnect & go back to LoginScreen
                AOGame game = (AOGame) Gdx.app.getApplicationListener();
                // TODO implement
        }
    }

    private void use() {
        GUI.getInventory().getSelected().ifPresent(slot -> {
            GameScreen.getClient().sendToAll(new ItemActionRequest(GUI.getInventory().selectedIndex()));
        });
    }

    private void attack() {
        E player = E(GameScreen.getPlayer());
        if (!player.hasAttack() || player.getAttack().interval - GameScreen.getWorld().getDelta() <= 0) {
            GameScreen.getClient().sendToAll(new AttackRequest(AttackType.PHYSICAL));
            player.attack();
        }
    }

    private void equip() {
        GUI.getInventory().getSelected().ifPresent(slot -> {
            GameScreen.getClient().sendToAll(new ItemActionRequest(GUI.getInventory().selectedIndex()));
        });
    }

    private void takeItem() {
        GameScreen.getClient().sendToAll(new TakeItemRequest());
    }

    // drop selected item (count 1 for the time being)
    private void dropItem() {
        GUI.getInventory().getSelected().ifPresent(selected -> {
            int player = GameScreen.getPlayer();
            GameScreen.getClient().sendToAll(new DropItem(E(player).getNetwork().id, GUI.getInventory().selectedIndex(), E(player).getWorldPos()));
        });
    }

    private void toggleDialogText() {
        if (GUI.getDialog().isVisible()) {
            String message = GUI.getDialog().getMessage();
            GameScreen.getClient().sendToAll(new TalkRequest(message));
        }
        GUI.getDialog().toggle();
        E(GameScreen.getPlayer()).writing(GUI.getDialog().isVisible());
    }

    private void toggleMeditate() {
        GameScreen.getClient().sendToAll(new MeditateRequest());
    }

    private void toggleInventory() {
        GUI.getInventory().setVisible(!GUI.getInventory().isVisible());
    }

    private void toggleSpells() {
        GUI.getSpellView().setVisible(!GUI.getSpellView().isVisible());
    }

}
