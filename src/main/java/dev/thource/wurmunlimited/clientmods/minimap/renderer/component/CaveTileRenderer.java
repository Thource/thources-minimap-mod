package dev.thource.wurmunlimited.clientmods.minimap.renderer.component;

import com.wurmonline.client.game.CaveDataBuffer;
import com.wurmonline.client.game.World;
import com.wurmonline.client.renderer.cave.CaveBufferChangeListener;
import com.wurmonline.mesh.Tiles;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.LayerRenderer;

public class CaveTileRenderer extends TileRenderer implements CaveBufferChangeListener {

  public CaveTileRenderer(World world, LayerRenderer layerRenderer) {
    super(layerRenderer);
    tileBuffer = world.getCaveBuffer();
    ((CaveDataBuffer) tileBuffer).addCaveBufferListener(this);
  }

  @Override
  public RenderedTile renderTile(int tileX, int tileY) {
    RenderedTile renderedTile = super.renderTile(tileX, tileY);

    Tiles.Tile tileType = tileBuffer.getTileType(tileX, tileY);
    if (!tileType.isSolidCave()) {
      drawWaterAndGeometry(renderedTile, tileX, tileY);
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
    new Thread(() -> setDirty(startX - 2, startY - 2, endX + 2, endY + 2)).start();
  }
}
