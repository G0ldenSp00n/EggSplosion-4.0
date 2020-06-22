package com.g0ldensp00n.eggsplosion.handlers.Utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Color;

public class Utils {
  public static List<String> FilterTabComplete(String argument, List<String> tabCompleteOptions) {
    List<String> filteredList = new ArrayList<String>();
    for (String commandPiece: tabCompleteOptions) {
      if (commandPiece.indexOf(argument) > -1) {
        filteredList.add(commandPiece);
      }
    }

    return filteredList;
  }

  public static Color chatColorToColor(ChatColor chatColor) {
    Color color;
      switch (chatColor) {
        case DARK_RED:
          color = Color.fromRGB(11141120);
          break;
        case RED:
          color = Color.fromRGB(16733525);
          break;
        case GOLD:
          color = Color.fromRGB(16755200);
          break;
        case YELLOW:
          color = Color.fromRGB(16777045);
          break;
        case DARK_GREEN:
          color = Color.fromRGB(43520);
          break;
        case GREEN:
          color = Color.fromRGB(5635925);
          break;
        case AQUA:
          color = Color.fromRGB(5636095);
          break;
        case DARK_AQUA:
          color = Color.fromRGB(43690);
          break;
        case DARK_BLUE:
          color = Color.fromRGB(170);
          break;
        case BLUE:
          color = Color.fromRGB(5592575);
          break;
        case LIGHT_PURPLE:
          color = Color.fromRGB(16733695);
          break;
        case DARK_PURPLE:
          color = Color.fromRGB(11141290);
        case WHITE:
          color = Color.fromRGB(16777215);
          break;
        case GRAY:
          color = Color.fromRGB(11184810);
          break;
        case DARK_GRAY:
          color = Color.fromRGB(5592405);
          break;
        case BLACK:
          color = Color.fromRGB(0);
          break;
        default:
          color = Color.fromRGB(16777215);
      }
      return color;
  }
}
