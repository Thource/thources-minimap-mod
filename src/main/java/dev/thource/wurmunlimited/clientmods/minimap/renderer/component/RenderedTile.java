package dev.thource.wurmunlimited.clientmods.minimap.renderer.component;

import dev.thource.wurmunlimited.clientmods.minimap.Settings;
import java.awt.image.BufferedImage;
import lombok.Getter;

@Getter
public class RenderedTile {
  private final int x;
  private final int y;
  private final BufferedImage image =
      new BufferedImage(Settings.getTileSize(), Settings.getTileSize(), BufferedImage.TYPE_INT_RGB);

  public RenderedTile(int x, int y) {
    this.x = x;
    this.y = y;
  }
}
