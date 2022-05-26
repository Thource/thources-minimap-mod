package dev.thource.wurmunlimited.clientmods.minimap.renderer.component;

import static com.wurmonline.mesh.Tiles.Tile.TILE_ENCHANTED_TREE_OAK;
import static com.wurmonline.mesh.Tiles.Tile.TILE_TREE_OAK;

import com.wurmonline.client.game.CaveDataBuffer;
import com.wurmonline.client.game.World;
import com.wurmonline.client.renderer.cave.CaveBufferChangeListener;
import com.wurmonline.mesh.Tiles;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.ImageManager;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.LayerRenderer;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class CaveTileRenderer extends TileRenderer implements CaveBufferChangeListener {

  public CaveTileRenderer(World world, LayerRenderer layerRenderer) {
    super(world, layerRenderer);
    tileBuffer = world.getCaveBuffer();
    ((CaveDataBuffer) tileBuffer).addCaveBufferListener(this);
  }

  @Override
  public RenderedTile render(int tileX, int tileY) {
    Tiles.Tile tileType = tileBuffer.getTileType(tileX, tileY);

    BufferedImage tileImage =
        ImageManager.tileImages.getOrDefault(tileType, ImageManager.missingImage);
    if (tileImage == ImageManager.missingImage && tileType.isSolidCave()) {
      tileImage = ImageManager.holeImage;
    }

    if (tileImage == ImageManager.missingImage) {
      System.out.println("Missing image for tile: " + tileType.getName());
    }

    RenderedTile renderedTile = new RenderedTile(tileX, tileY);
    Graphics2D graphics = renderedTile.getImage().createGraphics();
    graphics.drawImage(tileImage, 0, 0, null);
    graphics.dispose();

    if (!tileType.isSolidCave()) {
      renderWaterAndGeometry(renderedTile, tileX, tileY);
    }

    return renderedTile;
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
  protected float getHeight(int tileX, int tileY) {
    return ((CaveDataBuffer) tileBuffer).getHeight(tileX, tileY);
  }

  @Override
  public boolean isTileValid(int tileX, int tileY) {
    return ((CaveDataBuffer) tileBuffer).isValid(tileX * 4f, tileY * 4f)
        || tileBuffer.getTileType(tileX, tileY).isOreCave();
  }

  @Override
  public void caveChanged(int startX, int startY, int endX, int endY, boolean heightsChanged) {
    new Thread(() -> layerRenderer.renderTiles(startX - 1, startY - 1, endX + 1, endY + 1, true))
        .start();
  }
}
