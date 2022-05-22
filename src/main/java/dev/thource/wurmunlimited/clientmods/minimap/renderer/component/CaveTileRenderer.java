package dev.thource.wurmunlimited.clientmods.minimap.renderer.component;

import com.wurmonline.client.game.CaveDataBuffer;
import com.wurmonline.client.game.NearTerrainDataBuffer;
import com.wurmonline.client.game.World;
import com.wurmonline.mesh.Tiles;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.ImageManager;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.LayerRenderer;
import java.awt.image.BufferedImage;

public class CaveTileRenderer extends TileRenderer {

  public CaveTileRenderer(World world, LayerRenderer layerRenderer) {
    super(world, layerRenderer);
    tileBuffer = world.getCaveBuffer();
  }

  public BufferedImage render(int tileX, int tileY) {
    BufferedImage tileImage = super.render(tileX, tileY);

    Tiles.Tile tileType = tileBuffer.getTileType(tileX, tileY);
    if (tileImage == ImageManager.missingImage && tileType.isSolidCave()) {
      tileImage = ImageManager.holeImage;
    }

    if (tileImage == ImageManager.missingImage) {
      System.out.println("Missing image for tile: " + tileType.getName());
    }

//    tileImage = renderWater(tileImage, tileX, tileY);
    return tileImage;
  }

  @Override
  protected float getWaterHeight(int tileX, int tileY) {
    return ((CaveDataBuffer) tileBuffer).getWaterHeight(tileX, tileY);
  }

  @Override
  protected float getInterpolatedHeight(float worldX, float worldY) {
    return ((CaveDataBuffer) tileBuffer).getInterpolatedHeight(worldX, worldY);
  }

  @Override
  public boolean isTileValid(int tileX, int tileY) {
    return ((CaveDataBuffer) tileBuffer).isValid(tileX * 4, tileY * 4);
  }
}
