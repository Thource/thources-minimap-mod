package dev.thource.wurmunlimited.clientmods.minimap.renderer.component;

import com.wurmonline.client.renderer.structures.StructureData;
import dev.thource.wurmunlimited.clientmods.minimap.Constants;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import lombok.Getter;

@Getter
abstract class RenderedStructure {

  public static int PADDING = 0;
  protected long id;
  protected BufferedImage image;
  protected int tileX;
  protected int tileY;
  @Getter protected int width = 1;
  @Getter protected int length = 1;

  protected abstract void fullRedraw();

  protected boolean resize(int newTileX, int newTileY, int heightOffset) {
    if (newTileX < tileX) {
      width += tileX - newTileX;
      tileX = newTileX;
    } else {
      width = Math.max(width, newTileX - tileX + 1);
    }

    if (newTileY < tileY) {
      length += tileY - newTileY;
      tileY = newTileY;
    } else {
      length = Math.max(length, newTileY - tileY + 1);
    }

    boolean sizeChanged =
        image.getWidth() != width * Constants.TILE_SIZE + PADDING
            || image.getHeight() != length * Constants.TILE_SIZE + PADDING;
    if (!sizeChanged) {
      return false;
    }

    image =
        new BufferedImage(
            width * Constants.TILE_SIZE + PADDING,
            length * Constants.TILE_SIZE + PADDING,
            BufferedImage.TYPE_INT_ARGB);
    fullRedraw();

    return true;
  }
}
