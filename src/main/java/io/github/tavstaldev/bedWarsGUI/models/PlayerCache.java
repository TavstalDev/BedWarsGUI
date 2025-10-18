package io.github.tavstaldev.bedWarsGUI.models;

import com.samjakob.spigui.menu.SGMenu;
import io.github.tavstaldev.bedWarsGUI.gui.ArenaGUI;
import io.github.tavstaldev.bedWarsGUI.gui.MainGUI;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.api.game.Game;

import java.util.ArrayList;
import java.util.List;

/**
 * The PlayerCache class stores and manages the GUI-related state for a specific player.
 * It includes information about the player's current GUI state, selected mode, and available arenas.
 */
public class PlayerCache {
    private final Player _player; // The player associated with this cache
    private boolean _isGUIOpened; // Whether the GUI is currently opened for the player
    private SGMenu _mainMenu; // The main menu GUI instance
    private int _mainPage; // The current page of the main menu
    private SGMenu _arenaMenu; // The arena menu GUI instance
    private ArenaMode _selectedMode; // The currently selected arena mode
    private List<Game> _availableArenas; // The list of available arenas for the player
    private int _arenaPage; // The current page of the arena menu

    /**
     * Constructs a PlayerCache instance for the specified player.
     *
     * @param _player The player associated with this cache.
     */
    public PlayerCache(Player _player) {
        this._player = _player;
        this._isGUIOpened = false;
        this._mainMenu = null;
        this._mainPage = 0;
        this._arenaMenu = null;
        this._arenaPage = 0;
        this._selectedMode = null;
        this._availableArenas = new ArrayList<>();
    }

    /**
     * Checks if the GUI is currently opened for the player.
     *
     * @return true if the GUI is opened, false otherwise.
     */
    public boolean isGuiOpened() {
        return _isGUIOpened;
    }

    /**
     * Sets the GUI opened state for the player.
     *
     * @param isGUIOpened true to mark the GUI as opened, false otherwise.
     */
    public void setGuiOpened(boolean isGUIOpened) {
        this._isGUIOpened = isGUIOpened;
    }

    //#region Main Menu

    /**
     * Retrieves the main menu GUI for the player, creating it if it does not already exist.
     *
     * @return The SGMenu instance representing the main menu.
     */
    public SGMenu getMainMenu() {
        if (_mainMenu == null) {
            _mainMenu = MainGUI.create(_player);
        }
        return _mainMenu;
    }

    /**
     * Retrieves the current page of the main menu.
     *
     * @return The current page number of the main menu.
     */
    public int getMainPage() {
        return _mainPage;
    }

    /**
     * Sets the current page of the main menu.
     *
     * @param page The page number to set.
     */
    public void setMainPage(int page) {
        this._mainPage = page;
    }
    //#endregion

    //#region Arena Menu

    /**
     * Retrieves the arena menu GUI for the player, creating it if it does not already exist.
     *
     * @return The SGMenu instance representing the arena menu.
     */
    public SGMenu getArenaMenu() {
        if (_arenaMenu == null) {
            _arenaMenu = ArenaGUI.create(_player);
        }
        return _arenaMenu;
    }

    /**
     * Retrieves the current page of the arena menu.
     *
     * @return The current page number of the arena menu.
     */
    public int getArenaPage() {
        return _arenaPage;
    }

    /**
     * Sets the current page of the arena menu.
     *
     * @param page The page number to set.
     */
    public void setArenaPage(int page) {
        this._arenaPage = page;
    }

    /**
     * Retrieves the currently selected arena mode.
     *
     * @return The selected ArenaMode instance.
     */
    public ArenaMode getSelectedMode() {
        return _selectedMode;
    }

    /**
     * Sets the currently selected arena mode.
     *
     * @param mode The ArenaMode instance to set as selected.
     */
    public void setSelectedMode(ArenaMode mode) {
        this._selectedMode = mode;
    }

    /**
     * Retrieves the list of available arenas for the player.
     *
     * @return A list of Game instances representing the available arenas.
     */
    public List<Game> getAvailableArenas() {
        return _availableArenas;
    }

    /**
     * Sets the list of available arenas for the player.
     *
     * @param arenas A list of Game instances to set as available arenas.
     */
    public void setAvailableArenas(List<Game> arenas) {
        this._availableArenas = arenas;
    }
    //#endregion
}
