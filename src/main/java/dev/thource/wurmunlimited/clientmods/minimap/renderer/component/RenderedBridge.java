package dev.thource.wurmunlimited.clientmods.minimap.renderer.component;

import com.wurmonline.client.renderer.structures.BridgeData;
import com.wurmonline.client.renderer.structures.BridgePartData;
import com.wurmonline.shared.constants.BridgeConstants.BridgeState;
import dev.thource.wurmunlimited.clientmods.minimap.Settings;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.ImageManager;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RenderedBridge extends RenderedStructure {
  static {
    PADDING = (int) Math.max(Settings.getTileSize() / 8f, 1);
  }

  private final List<BridgePartData> bridgeParts = new ArrayList<>();
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
      dirty = true;
    }
  }

  public void removeBridgePart(BridgePartData bridgePart) {
    synchronized (imageLock) {
      bridgeParts.remove(bridgePart);
      dirty = true;
    }
  }

  @Override
  protected void fullRedraw(BufferedImage image) {
    Graphics2D graphics = image.createGraphics();

    graphics.setBackground(new Color(0, 0, 0, 0));
    graphics.clearRect(0, 0, image.getWidth(), image.getHeight());

    for (int i = 0; i < PADDING; i++) {
      graphics.setPaint(new Color(0, 0, 0, (0.5f / PADDING) * (PADDING - i)));
      graphics.fillRect(
          horizontal ? 0 : Settings.getTileSize() * width + i,
          horizontal ? Settings.getTileSize() * length + i : 0,
          horizontal ? Settings.getTileSize() * width : 1,
          horizontal ? 1 : Settings.getTileSize() * length);
    }

    bridgeParts.stream()
        .sorted(
            Comparator.comparingInt(BridgePartData::getTileX)
                .thenComparingInt(BridgePartData::getTileY))
        .forEach(bridgePart -> drawBridgePart(bridgePart, graphics));

    graphics.dispose();
  }

  @Override
  protected void recalculateDimensions() {
    tileX = 9999;
    tileY = 9999;
    int maxTileX = -1;
    int maxTileY = -1;

    for (BridgePartData bridgePart : bridgeParts) {
      tileX = Math.min(tileX, bridgePart.getTileX());
      tileY = Math.min(tileY, bridgePart.getTileY());
      maxTileX = Math.max(maxTileX, bridgePart.getTileX());
      maxTileY = Math.max(maxTileY, bridgePart.getTileY());
      horizontal = bridgePart.getDir() / 2 % 2 == 1;
    }

    width = Math.max(maxTileX - tileX + 1, 1);
    length = Math.max(maxTileY - tileY + 1, 1);
  }

  private void drawBridgePart(BridgePartData bridgePart, Graphics2D graphics) {
    BufferedImage bridgePartImage =
        ImageManager.bridgeImages.getOrDefault(bridgePart.getMaterial(), ImageManager.missingImage);

    if (bridgePart.getState() != BridgeState.COMPLETED) {
      graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
    }

    graphics.drawImage(
        bridgePartImage,
        (bridgePart.getTileX() - tileX) * Settings.getTileSize(),
        (bridgePart.getTileY() - tileY) * Settings.getTileSize(),
        null);

    if (bridgePart.getState() != BridgeState.COMPLETED) {
      graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
    }
  }
}
