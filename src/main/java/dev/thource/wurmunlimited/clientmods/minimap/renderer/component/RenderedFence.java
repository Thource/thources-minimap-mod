package dev.thource.wurmunlimited.clientmods.minimap.renderer.component;

import com.wurmonline.client.renderer.structures.FenceData;
import dev.thource.wurmunlimited.clientmods.minimap.Constants;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.ImageManager;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import lombok.Getter;

public class RenderedFence extends RenderedStructure<FenceData> {
  public static final int FENCE_THICKNESS = (int) Math.max(Constants.TILE_SIZE / 4f, 1);
  @Getter private final boolean horizontal;

  public RenderedFence(FenceData data) {
    id = data.getId();

    tileX = data.getTileX();
    tileY = data.getTileY();
    horizontal = data.getTileXEnd() != tileX;
    image =
        new BufferedImage(
            horizontal ? Constants.TILE_SIZE : FENCE_THICKNESS,
            horizontal ? FENCE_THICKNESS : Constants.TILE_SIZE,
            BufferedImage.TYPE_INT_ARGB);
    draw(data);
  }

  private void draw(FenceData data) {
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
