package dev.thource.wurmunlimited.clientmods.minimap;

import lombok.Getter;
import lombok.Setter;

public class Settings {
  @Getter @Setter private static int tileSize = 32;
  @Getter @Setter private static boolean transparentWater = true;
  @Getter @Setter private static boolean renderHeight = true;

  private Settings() {}
}
