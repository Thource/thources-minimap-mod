package dev.thource.wurmunlimited.clientmods.minimap.renderer.component;

import static com.wurmonline.mesh.Tiles.TileRoadDirection.DIR_NE;
import static com.wurmonline.mesh.Tiles.TileRoadDirection.DIR_NW;
import static com.wurmonline.mesh.Tiles.TileRoadDirection.DIR_SE;
import static com.wurmonline.mesh.Tiles.TileRoadDirection.DIR_SW;

import com.wurmonline.client.game.NearTerrainDataBuffer;
import com.wurmonline.client.game.TerrainChangeListener;
import com.wurmonline.client.game.World;
import com.wurmonline.mesh.Tiles;
import dev.thource.wurmunlimited.clientmods.minimap.Settings;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.ImageManager;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.LayerRenderer;
import java.awt.image.BufferedImage;

public class WorldTileRenderer extends TileRenderer implements TerrainChangeListener {

  public WorldTileRenderer(World world, LayerRenderer layerRenderer) {
    super(layerRenderer);
    tileBuffer = world.getNearTerrainBuffer();
    ((NearTerrainDataBuffer) tileBuffer).addListener(this);
  }

  @Override
  public RenderedTile renderTile(int tileX, int tileY) {
    RenderedTile renderedTile = super.renderTile(tileX, tileY);

    drawRoad(renderedTile, tileX, tileY);
    drawWaterAndGeometry(renderedTile, tileX, tileY);

    return renderedTile;
  }

  private void drawRoad(RenderedTile renderedTile, int tileX, int tileY) {
    Tiles.Tile tileType = tileBuffer.getTileType(tileX, tileY);
    if (!tileType.isRoad()) {
      return;
    }

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
      BufferedImage northTileImage = null;
      BufferedImage eastTileImage = null;
      BufferedImage southTileImage = null;
      BufferedImage westTileImage = null;
      if (roadDirection == DIR_SE || roadDirection == DIR_SW) {
        northTileImage =
            ImageManager.tileImages.getOrDefault(
                nearTerrainBuffer.getSecondaryType(tileX, tileY - 1), ImageManager.missingImage);
      }
      if (roadDirection == DIR_NW || roadDirection == DIR_SW) {
        eastTileImage =
            ImageManager.tileImages.getOrDefault(
                nearTerrainBuffer.getSecondaryType(tileX + 1, tileY), ImageManager.missingImage);
      }
      if (roadDirection == DIR_NE || roadDirection == DIR_NW) {
        southTileImage =
            ImageManager.tileImages.getOrDefault(
                nearTerrainBuffer.getSecondaryType(tileX, tileY + 1), ImageManager.missingImage);
      }
      if (roadDirection == DIR_SE || roadDirection == DIR_NE) {
        westTileImage =
            ImageManager.tileImages.getOrDefault(
                nearTerrainBuffer.getSecondaryType(tileX - 1, tileY), ImageManager.missingImage);
      }

      for (int py = 0; py < Settings.getTileSize(); py++) {
        for (int px = 0; px < Settings.getTileSize(); px++) {
          if ((roadDirection == DIR_SE || roadDirection == DIR_SW)
              && py <= Settings.getTileSize() / 2 - 1
              && px - py >= 0
              && px + py <= Settings.getTileSize() - 1) {
            renderedTile.getImage().setRGB(px, py, northTileImage.getRGB(px, py));
          }
          if ((roadDirection == DIR_NW || roadDirection == DIR_SW)
              && px >= Settings.getTileSize() / 2
              && py - (Settings.getTileSize() - 1 - px) >= 0
              && py + (Settings.getTileSize() - 1 - px) <= Settings.getTileSize() - 1) {
            renderedTile.getImage().setRGB(px, py, eastTileImage.getRGB(px, py));
          }
          if ((roadDirection == DIR_NE || roadDirection == DIR_NW)
              && py >= Settings.getTileSize() / 2
              && px - (Settings.getTileSize() - 1 - py) >= 0
              && px + (Settings.getTileSize() - 1 - py) <= Settings.getTileSize() - 1) {
            renderedTile.getImage().setRGB(px, py, southTileImage.getRGB(px, py));
          }
          if ((roadDirection == DIR_NE || roadDirection == DIR_SE)
              && px <= Settings.getTileSize() / 2 - 1
              && py - px >= 0
              && py + px <= Settings.getTileSize() - 1) {
            renderedTile.getImage().setRGB(px, py, westTileImage.getRGB(px, py));
          }
        }
      }
    }
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
  protected float getHeight(int tileX, int tileY) {
    return ((NearTerrainDataBuffer) tileBuffer).getHeight(tileX, tileY);
  }

  @Override
  public boolean isTileValid(int tileX, int tileY) {
    return ((NearTerrainDataBuffer) tileBuffer).isValid(tileX * 4f, tileY * 4f);
  }

  @Override
  public void terrainUpdated(
      int startX, int startY, int endX, int endY, boolean heightsChanged, boolean bigUpdate) {
    new Thread(() -> setDirty(startX - 1, startY - 1, endX + 1, endY + 1)).start();
  }
}
