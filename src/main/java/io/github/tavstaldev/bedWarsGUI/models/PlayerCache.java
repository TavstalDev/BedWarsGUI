package io.github.tavstaldev.bedWarsGUI.models;

import com.samjakob.spigui.menu.SGMenu;
import io.github.tavstaldev.bedWarsGUI.gui.ArenaGUI;
import io.github.tavstaldev.bedWarsGUI.gui.MainGUI;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.api.game.Game;

import java.util.ArrayList;
import java.util.List;

public class PlayerCache {
    private final Player _player;
    private boolean _isGUIOpened;
    private SGMenu _mainMenu;
    private int _mainPage;
    private SGMenu _arenaMenu;
    private ArenaMode _selectedMode;
    private List<Game> _availableArenas;
    private int _arenaPage;

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

    public boolean isGuiOpened() {
        return _isGUIOpened;
    }

    public void setGuiOpened(boolean isGUIOpened) {
        this._isGUIOpened = isGUIOpened;
    }

    //#region Main Menu
    public SGMenu getMainMenu() {
        if (_mainMenu == null) {
            _mainMenu = MainGUI.create(_player);
        }
        return _mainMenu;
    }

    public int getMainPage() {
        return _mainPage;
    }

    public void setMainPage(int page) {
        this._mainPage = page;
    }
    //#endregion

    //#region Arena Menu
    public SGMenu getArenaMenu() {
        if (_arenaMenu == null) {
            _arenaMenu = ArenaGUI.create(_player);
        }
        return _arenaMenu;
    }

    public int getArenaPage() {
        return _arenaPage;
    }

    public void setArenaPage(int page) {
        this._arenaPage = page;
    }

    public ArenaMode getSelectedMode() {
        return _selectedMode;
    }

    public void setSelectedMode(ArenaMode mode) {
        this._selectedMode = mode;
    }

    public List<Game> getAvailableArenas() {
        return _availableArenas;
    }

    public void setAvailableArenas(List<Game> arenas) {
        this._availableArenas = arenas;
    }
    //#endregion
}
