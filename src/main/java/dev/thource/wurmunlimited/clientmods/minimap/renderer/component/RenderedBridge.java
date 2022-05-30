package dev.thource.wurmunlimited.clientmods.minimap.renderer.component;

import com.wurmonline.client.renderer.structures.BridgeData;
import com.wurmonline.client.renderer.structures.BridgePartData;
import dev.thource.wurmunlimited.clientmods.minimap.Constants;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.ImageManager;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.Getter;

public class RenderedBridge extends RenderedStructure {
  static {
    PADDING = (int) Math.max(Constants.TILE_SIZE / 8f, 1);
  }

  private final List<BridgePartData> bridgeParts = new ArrayList<>();
  private final Object imageLock = new Object();
  private boolean horizontal;

  public RenderedBridge(BridgeData data) {
    id = data.getId();

    // These are the wrong way around on purpose, it's how the data comes through
    tileX = data.getTileY();
    tileY = data.getTileX();
    image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
  }

  public boolean isBridgeTile(int tileX, int tileY) {
    return tileX >= this.tileX
        && tileX < this.tileX + width
        && tileY >= this.tileY
        && tileY < this.tileY + length;
  }

  public void addBridgePart(BridgePartData bridgePart) {
    synchronized (imageLock) {
      bridgeParts.add(bridgePart);

      if (!resize(bridgePart.getTileX(), bridgePart.getTileY(), bridgePart.getHeightOffset())) {
        drawBridgePart(bridgePart);
      }
    }
  }

  @Override
  protected void fullRedraw() {
    Graphics2D graphics = image.createGraphics();

    for (int i = 0; i < PADDING; i++) {
      graphics.setPaint(new Color(0, 0, 0, (0.5f / PADDING) * (PADDING - i)));
      graphics.fillRect(
          horizontal ? 0 : Constants.TILE_SIZE * width + i,
          horizontal ? Constants.TILE_SIZE * length + i : 0,
          horizontal ? Constants.TILE_SIZE * width : 1,
          horizontal ? 1 : Constants.TILE_SIZE * length);
    }

    bridgeParts.stream()
        .sorted(
            Comparator.comparingInt(BridgePartData::getTileX)
                .thenComparingInt(BridgePartData::getTileY))
        .forEach(bridgePart -> drawBridgePart(bridgePart, graphics));

    graphics.dispose();
  }

  private void drawBridgePart(BridgePartData bridgePart, Graphics2D graphics) {
    BufferedImage bridgePartImage =
        ImageManager.bridgeImages.getOrDefault(bridgePart.getMaterial(), ImageManager.missingImage);

    graphics.drawImage(
        bridgePartImage,
        (bridgePart.getTileX() - tileX) * Constants.TILE_SIZE,
        (bridgePart.getTileY() - tileY) * Constants.TILE_SIZE,
        null);

    horizontal = bridgePart.getDir() / 2 % 2 == 1;
  }

  private void drawBridgePart(BridgePartData bridgePart) {
    Graphics2D graphics = image.createGraphics();
    drawBridgePart(bridgePart, graphics);
    graphics.dispose();
  }
}
