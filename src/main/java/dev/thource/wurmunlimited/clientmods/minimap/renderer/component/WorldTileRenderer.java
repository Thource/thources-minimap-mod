package dev.thource.wurmunlimited.clientmods.minimap.renderer.component;

import static com.wurmonline.mesh.Tiles.TileRoadDirection.DIR_NE;
import static com.wurmonline.mesh.Tiles.TileRoadDirection.DIR_NW;
import static com.wurmonline.mesh.Tiles.TileRoadDirection.DIR_SE;
import static com.wurmonline.mesh.Tiles.TileRoadDirection.DIR_SW;

import com.wurmonline.client.game.NearTerrainDataBuffer;
import com.wurmonline.client.game.TerrainChangeListener;
import com.wurmonline.client.game.World;
import com.wurmonline.mesh.Tiles;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.ImageManager;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.LayerRenderer;
import java.awt.image.BufferedImage;

public class WorldTileRenderer extends TileRenderer implements TerrainChangeListener {

  public WorldTileRenderer(World world, LayerRenderer layerRenderer) {
    super(world, layerRenderer);
    tileBuffer = world.getNearTerrainBuffer();
    ((NearTerrainDataBuffer) tileBuffer).addListener(this);
  }

  public BufferedImage render(int tileX, int tileY) {
    BufferedImage tileImage = super.render(tileX, tileY);
    Tiles.Tile tileType = tileBuffer.getTileType(tileX, tileY);

    if (tileImage == ImageManager.missingImage) {
      System.out.println("Missing image for tile: " + tileType.getName());
    }

    tileImage = renderRoad(tileImage, tileX, tileY);
//    tileImage = renderWater(tileImage, tileX, tileY);

    return tileImage;
  }

  private BufferedImage renderRoad(BufferedImage originalImage, int tileX, int tileY) {
    Tiles.Tile tileType = tileBuffer.getTileType(tileX, tileY);
    if (!tileType.isRoad()) {
      return originalImage;
    }

    BufferedImage tileImage = cloneImage(originalImage);

    NearTerrainDataBuffer nearTerrainBuffer = ((NearTerrainDataBuffer) tileBuffer);
    int roadDir = nearTerrainBuffer.getData(tileX, tileY) & 7;
    Tiles.TileRoadDirection roadDirection = Tiles.TileRoadDirection.DIR_STRAIGHT;
    if (roadDir == 1) {
      roadDirection = DIR_NW;
    } else if (roadDir == 2) {
      roadDirection = DIR_NE;
    } else if (roadDir == 3) {
      roadDirection = DIR_SE;
    } else if (roadDir == 4) {
      roadDirection = DIR_SW;
    }

    if (roadDirection != Tiles.TileRoadDirection.DIR_STRAIGHT) {
      tileImage = cloneImage(tileImage);

      BufferedImage northTileImage = null;
      BufferedImage eastTileImage = null;
      BufferedImage southTileImage = null;
      BufferedImage westTileImage = null;
      if (roadDirection == DIR_SE || roadDirection == DIR_SW) {
        northTileImage = ImageManager.tileImages.getOrDefault(
            nearTerrainBuffer.getSecondaryType(tileX, tileY - 1), ImageManager.missingImage);
      }
      if (roadDirection == DIR_NW || roadDirection == DIR_SW) {
        eastTileImage = ImageManager.tileImages.getOrDefault(
            nearTerrainBuffer.getSecondaryType(tileX + 1, tileY), ImageManager.missingImage);
      }
      if (roadDirection == DIR_NE || roadDirection == DIR_NW) {
        southTileImage = ImageManager.tileImages.getOrDefault(
            nearTerrainBuffer.getSecondaryType(tileX, tileY + 1), ImageManager.missingImage);
      }
      if (roadDirection == DIR_SE || roadDirection == DIR_NE) {
        westTileImage = ImageManager.tileImages.getOrDefault(
            nearTerrainBuffer.getSecondaryType(tileX - 1, tileY), ImageManager.missingImage);
      }

      for (int py = 0; py < 64; py++) {
        for (int px = 0; px < 64; px++) {
          if ((roadDirection == DIR_SE || roadDirection == DIR_SW) && py <= 31 && px - py >= 0
              && px + py <= 63) {
            tileImage.setRGB(px, py, northTileImage.getRGB(px, py));
          }
          if ((roadDirection == DIR_NW || roadDirection == DIR_SW) && px >= 32
              && py - (63 - px) >= 0 && py + (63 - px) <= 63) {
            tileImage.setRGB(px, py, eastTileImage.getRGB(px, py));
          }
          if ((roadDirection == DIR_NE || roadDirection == DIR_NW) && py >= 32
              && px - (63 - py) >= 0 && px + (63 - py) <= 63) {
            tileImage.setRGB(px, py, southTileImage.getRGB(px, py));
          }
          if ((roadDirection == DIR_NE || roadDirection == DIR_SE) && px <= 31 && py - px >= 0
              && py + px <= 63) {
            tileImage.setRGB(px, py, westTileImage.getRGB(px, py));
          }
        }
      }
    }

    return tileImage;
  }

  @Override
  protected float getWaterHeight(int tileX, int tileY) {
    return ((NearTerrainDataBuffer) tileBuffer).getWaterHeight(tileX, tileY);
  }

  @Override
  protected float getInterpolatedHeight(float worldX, float worldY) {
    return ((NearTerrainDataBuffer) tileBuffer).getInterpolatedHeight(worldX, worldY);
  }

  @Override
  public boolean isTileValid(int tileX, int tileY) {
    return ((NearTerrainDataBuffer) tileBuffer).isValid(tileX * 4, tileY * 4);
  }

  @Override
  public void terrainUpdated(int startX, int startY, int endX, int endY, boolean heightsChanged,
      boolean bigUpdate) {
    layerRenderer.renderTiles(startX, startY, endX, endY);
  }
}
