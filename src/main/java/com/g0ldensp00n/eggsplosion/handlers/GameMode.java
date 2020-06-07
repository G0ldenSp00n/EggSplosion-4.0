package com.g0ldensp00n.eggsplosion.handlers;

import org.bukkit.Bukkit;
import java.util.*;
import org.bukkit.entity.Player;

public class GameMode {
    enum GameMode {
      LOBBY,
      DEATH_MATCH
    };

    Map<GameMode, Player[]> lobbies = new HashTable<>();


}
