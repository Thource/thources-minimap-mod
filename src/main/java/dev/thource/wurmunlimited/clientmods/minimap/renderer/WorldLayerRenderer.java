package dev.thource.wurmunlimited.clientmods.minimap.renderer;

import com.wurmonline.client.game.World;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.component.WorldTileRenderer;

public class WorldLayerRenderer extends LayerRenderer {

  public WorldLayerRenderer(World world) {
    super(world, 0);
    tileRenderer = new WorldTileRenderer(world, this);
    render(true);
  }
}
