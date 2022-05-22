package dev.thource.wurmunlimited.clientmods.minimap.renderer.component;

import static com.wurmonline.mesh.Tiles.Tile.TILE_ENCHANTED_TREE_OAK;
import static com.wurmonline.mesh.Tiles.Tile.TILE_TREE_OAK;

import com.wurmonline.client.game.TerrainDataInformationProvider;
import com.wurmonline.client.game.World;
import com.wurmonline.mesh.Tiles;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.ImageManager;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.LayerRenderer;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public abstract class TileRenderer {

  protected final LayerRenderer layerRenderer;
  public TerrainDataInformationProvider tileBuffer;

  public TileRenderer(World world, LayerRenderer layerRenderer) {
    this.layerRenderer = layerRenderer;
  }

  public BufferedImage render(int tileX, int tileY) {
    Tiles.Tile tileType = tileBuffer.getTileType(tileX, tileY);
    if (tileType.isTree() || tileType.isBush()) {
      tileType = tileType.isEnchanted() ? TILE_ENCHANTED_TREE_OAK : TILE_TREE_OAK;
    }

    return ImageManager.tileImages.getOrDefault(tileType, ImageManager.missingImage);
  }

  protected abstract float getWaterHeight(int tileX, int tileY);

  protected abstract float getInterpolatedHeight(float worldX, float worldY);

  protected BufferedImage renderWater(BufferedImage originalImage, int tileX, int tileY) {
    BufferedImage tileImage = cloneImage(originalImage);

    float waterHeight = getWaterHeight(tileX, tileY);
    for (int py = 0; py < 64; py++) {
      for (int px = 0; px < 64; px++) {
        float worldX = (tileX + (px / 63f)) * 4f;
        float worldY = (tileY + (py / 63f)) * 4f;

        float pointHeight = getInterpolatedHeight(worldX, worldY);
        if (waterHeight >= pointHeight) {
          tileImage.setRGB(px, py, ImageManager.waterImage.getRGB(px, py));
        }
      }
    }

    return tileImage;
  }

  protected BufferedImage cloneImage(BufferedImage original) {
    BufferedImage clone = new BufferedImage(original.getWidth(), original.getHeight(),
        original.getType());
    Graphics2D gfx = clone.createGraphics();
    gfx.drawImage(original, null, 0, 0);
    gfx.dispose();

    return clone;
  }

  public abstract boolean isTileValid(int tileX, int tileY);
}
