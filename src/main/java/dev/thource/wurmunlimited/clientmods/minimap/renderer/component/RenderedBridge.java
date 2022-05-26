package dev.thource.wurmunlimited.clientmods.minimap.renderer.component;

import com.wurmonline.client.renderer.structures.BridgeData;
import com.wurmonline.client.renderer.structures.BridgePartData;
import dev.thource.wurmunlimited.clientmods.minimap.Constants;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.ImageManager;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import lombok.Getter;

public class RenderedBridge extends RenderedStructure<BridgeData> {
  private static final int SHADOW_THICKNESS = (int) Math.max(Constants.TILE_SIZE / 8f, 1);
  @Getter private final int width;
  @Getter private final int length;

  public RenderedBridge(BridgeData data) {
    id = data.getId();

    Rectangle bounds = getBounds(data);
    tileX = bounds.x;
    tileY = bounds.y;
    width = bounds.width;
    length = bounds.height;
    image =
        new BufferedImage(
            bounds.width * Constants.TILE_SIZE + SHADOW_THICKNESS,
            bounds.height * Constants.TILE_SIZE + SHADOW_THICKNESS,
            BufferedImage.TYPE_INT_ARGB);
    draw(data);
  }

  private void draw(BridgeData data) {
    Graphics2D graphics = image.createGraphics();

    data.getBridgeParts().values().stream()
        .sorted(Comparator.comparingInt(bp -> (bp.getTileX() + bp.getTileY())))
        .forEach(
            bridgePart -> {
              BufferedImage bridgePartImage =
                  ImageManager.bridgeImages.getOrDefault(
                      bridgePart.getMaterial(), ImageManager.missingImage);

              graphics.drawImage(
                  bridgePartImage,
                  (bridgePart.getTileX() - tileX) * Constants.TILE_SIZE,
                  (bridgePart.getTileY() - tileY) * Constants.TILE_SIZE,
                  null);

              boolean horizontal = bridgePart.getDir() / 2 % 2 == 1;
              for (int i = 0; i < SHADOW_THICKNESS; i++) {
                graphics.setPaint(
                    new Color(0, 0, 0, (0.5f / SHADOW_THICKNESS) * (SHADOW_THICKNESS - i)));
                graphics.fillRect(
                    ((bridgePart.getTileX() - tileX) + (horizontal ? 0 : 1)) * Constants.TILE_SIZE
                        + (horizontal ? 0 : i),
                    ((bridgePart.getTileY() - tileY) + (horizontal ? 1 : 0)) * Constants.TILE_SIZE
                        + (horizontal ? i : 0),
                    horizontal ? Constants.TILE_SIZE : 1,
                    horizontal ? 1 : Constants.TILE_SIZE);
              }
            });

    graphics.dispose();
  }

  public boolean isBridgeTile(int tileX, int tileY) {
    return tileX >= this.tileX
        && tileX < this.tileX + width
        && tileY >= this.tileY
        && tileY < this.tileY + length;
  }

  @Override
  protected Rectangle getBounds(BridgeData data) {
    int minX = 999999;
    int maxX = -999999;
    int minY = 999999;
    int maxY = -999999;

    for (BridgePartData bridgePart : data.getBridgeParts().values()) {
      minX = Math.min(bridgePart.getTileX(), minX);
      maxX = Math.max(bridgePart.getTileX(), maxX);
      minY = Math.min(bridgePart.getTileY(), minY);
      maxY = Math.max(bridgePart.getTileY(), maxY);
    }

    if (minX == 999999 || maxX == -999999 || minY == 999999 || maxY == -999999) {
      return new Rectangle();
    }

    return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
  }
}
