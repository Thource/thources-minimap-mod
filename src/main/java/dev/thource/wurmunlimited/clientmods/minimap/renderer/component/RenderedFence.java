package dev.thource.wurmunlimited.clientmods.minimap.renderer.component;

import com.wurmonline.client.renderer.structures.FenceData;
import dev.thource.wurmunlimited.clientmods.minimap.Constants;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.ImageManager;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import lombok.Getter;

public class RenderedFence extends RenderedStructure {
  static {
    PADDING = (int) Math.max(Constants.TILE_SIZE / 4f, 1);
  }

  @Getter private final boolean horizontal;
  private final FenceData data;

  public RenderedFence(FenceData data) {
    id = data.getId();

    tileX = data.getTileX();
    tileY = data.getTileY();
    horizontal = data.getTileXEnd() != tileX;
    image =
        new BufferedImage(
            horizontal ? Constants.TILE_SIZE + PADDING : PADDING,
            horizontal ? PADDING : Constants.TILE_SIZE + PADDING,
            BufferedImage.TYPE_INT_ARGB);
    this.data = data;
    fullRedraw();
  }

  @Override
  protected void fullRedraw() {
    Graphics2D graphics = image.createGraphics();
    graphics.setPaint(
        ImageManager.fenceColors.getOrDefault(data.getType().material, Color.MAGENTA));
    if (data.isGate()) {
      graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
    }

    graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

    graphics.dispose();
  }
}
