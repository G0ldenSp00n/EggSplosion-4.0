package com.g0ldensp00n.eggsplosion.handlers.Utils;

import java.util.ArrayList;
import java.util.List;

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
}
