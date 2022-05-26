package dev.thource.wurmunlimited.clientmods.minimap.renderer.component;

import static com.wurmonline.mesh.Tiles.Tile.TILE_ENCHANTED_TREE_OAK;
import static com.wurmonline.mesh.Tiles.Tile.TILE_TREE_OAK;
import static com.wurmonline.mesh.Tiles.TileRoadDirection.DIR_NE;
import static com.wurmonline.mesh.Tiles.TileRoadDirection.DIR_NW;
import static com.wurmonline.mesh.Tiles.TileRoadDirection.DIR_SE;
import static com.wurmonline.mesh.Tiles.TileRoadDirection.DIR_SW;

import com.wurmonline.client.game.NearTerrainDataBuffer;
import com.wurmonline.client.game.TerrainChangeListener;
import com.wurmonline.client.game.World;
import com.wurmonline.mesh.Tiles;
import dev.thource.wurmunlimited.clientmods.minimap.Constants;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.ImageManager;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.LayerRenderer;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class WorldTileRenderer extends TileRenderer implements TerrainChangeListener {

  public WorldTileRenderer(World world, LayerRenderer layerRenderer) {
    super(world, layerRenderer);
    tileBuffer = world.getNearTerrainBuffer();
    ((NearTerrainDataBuffer) tileBuffer).addListener(this);
  }

  @Override
  public RenderedTile render(int tileX, int tileY) {
    Tiles.Tile tileType = tileBuffer.getTileType(tileX, tileY);
    if (tileType.isTree() || tileType.isBush()) {
      tileType = tileType.isEnchanted() ? TILE_ENCHANTED_TREE_OAK : TILE_TREE_OAK;
    }

    BufferedImage tileImage =
        ImageManager.tileImages.getOrDefault(tileType, ImageManager.missingImage);
    if (tileImage == ImageManager.missingImage) {
      System.out.println("Missing image for tile: " + tileType.getName());
    }

    RenderedTile renderedTile = new RenderedTile(tileX, tileY);
    Graphics2D graphics = renderedTile.getImage().createGraphics();
    graphics.drawImage(tileImage, 0, 0, null);
    graphics.dispose();

    renderRoad(renderedTile, tileX, tileY);
    renderWaterAndGeometry(renderedTile, tileX, tileY);

    return renderedTile;
  }

  private void renderRoad(RenderedTile renderedTile, int tileX, int tileY) {
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

      for (int py = 0; py < Constants.TILE_SIZE; py++) {
        for (int px = 0; px < Constants.TILE_SIZE; px++) {
          if ((roadDirection == DIR_SE || roadDirection == DIR_SW)
              && py <= Constants.TILE_SIZE / 2 - 1
              && px - py >= 0
              && px + py <= Constants.TILE_SIZE - 1) {
            renderedTile.getImage().setRGB(px, py, northTileImage.getRGB(px, py));
          }
          if ((roadDirection == DIR_NW || roadDirection == DIR_SW)
              && px >= Constants.TILE_SIZE / 2
              && py - (Constants.TILE_SIZE - 1 - px) >= 0
              && py + (Constants.TILE_SIZE - 1 - px) <= Constants.TILE_SIZE - 1) {
            renderedTile.getImage().setRGB(px, py, eastTileImage.getRGB(px, py));
          }
          if ((roadDirection == DIR_NE || roadDirection == DIR_NW)
              && py >= Constants.TILE_SIZE / 2
              && px - (Constants.TILE_SIZE - 1 - py) >= 0
              && px + (Constants.TILE_SIZE - 1 - py) <= Constants.TILE_SIZE - 1) {
            renderedTile.getImage().setRGB(px, py, southTileImage.getRGB(px, py));
          }
          if ((roadDirection == DIR_NE || roadDirection == DIR_SE)
              && px <= Constants.TILE_SIZE / 2 - 1
              && py - px >= 0
              && py + px <= Constants.TILE_SIZE - 1) {
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
    new Thread(() -> layerRenderer.renderTiles(startX - 1, startY - 1, endX + 1, endY + 1, true))
        .start();
  }
}
