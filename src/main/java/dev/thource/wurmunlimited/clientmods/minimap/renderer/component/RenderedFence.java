package dev.thource.wurmunlimited.clientmods.minimap.renderer.component;

import com.wurmonline.client.renderer.structures.FenceData;
import dev.thource.wurmunlimited.clientmods.minimap.Settings;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.ImageManager;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import lombok.Getter;

public class RenderedFence extends RenderedStructure {
  static {
    PADDING = (int) Math.max(Settings.getTileSize() / 4f, 1);
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
            horizontal ? Settings.getTileSize() + PADDING : PADDING,
            horizontal ? PADDING : Settings.getTileSize() + PADDING,
            BufferedImage.TYPE_INT_ARGB);
    this.data = data;
    fullRedraw(image);
  }

  @Override
  public void render() {
    // Fences can't change
  }

  @Override
  protected void recalculateDimensions() {
    // Fences are always 1x1
  }

  @Override
  protected void fullRedraw(BufferedImage image) {
    Graphics2D graphics = image.createGraphics();
    graphics.setPaint(
        ImageManager.fenceColors.getOrDefault(data.getType().material, Color.MAGENTA));
    if (data.isGate() || data.getCollisionHeight() == 0f || data.getCollisionThickness() == 0f) {
      graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
    }

    graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

    graphics.dispose();
  }
}
