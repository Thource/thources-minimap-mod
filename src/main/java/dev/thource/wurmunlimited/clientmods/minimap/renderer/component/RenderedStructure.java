package dev.thource.wurmunlimited.clientmods.minimap.renderer.component;

import dev.thource.wurmunlimited.clientmods.minimap.Constants;
import java.awt.image.BufferedImage;
import lombok.Getter;

@Getter
abstract class RenderedStructure {

  public static int PADDING = 0;
  protected final Object imageLock = new Object();
  protected long id;
  protected BufferedImage image;
  protected int tileX;
  protected int tileY;
  @Getter protected int width = 1;
  @Getter protected int length = 1;
  protected boolean dirty;

  protected abstract void fullRedraw(BufferedImage image);

  public void render() {
    synchronized (imageLock) {
      if (!dirty) {
        return;
      }

      BufferedImage newImage = resize();
      fullRedraw(newImage);
      image = newImage;
      dirty = false;
    }
  }

  protected abstract void recalculateDimensions();

  protected boolean hasSizeChanged() {
    return image.getWidth() != width * Constants.TILE_SIZE + PADDING
        || image.getHeight() != length * Constants.TILE_SIZE + PADDING;
  }

  protected BufferedImage resize() {
    recalculateDimensions();

    if (!hasSizeChanged()) {
      return image;
    }

    return new BufferedImage(
        width * Constants.TILE_SIZE + PADDING,
        length * Constants.TILE_SIZE + PADDING,
        BufferedImage.TYPE_INT_ARGB);
  }
}
