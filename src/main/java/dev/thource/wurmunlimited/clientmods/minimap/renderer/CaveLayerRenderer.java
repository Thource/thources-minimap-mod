package dev.thource.wurmunlimited.clientmods.minimap.renderer;

import com.wurmonline.client.game.World;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.component.CaveTileRenderer;

public class CaveLayerRenderer extends LayerRenderer {

  public CaveLayerRenderer(World world) {
    super(world);
    tileRenderer = new CaveTileRenderer(world, this);
    render(true);
  }
}
