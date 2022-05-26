package dev.thource.wurmunlimited.clientmods.minimap.renderer.component;

import com.wurmonline.client.renderer.structures.StructureData;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import lombok.Getter;

@Getter
abstract class RenderedStructure<D extends StructureData> {

  protected long id;
  protected BufferedImage image;
  protected int tileX;
  protected int tileY;

  protected Rectangle getBounds(D data) {
    return new Rectangle();
  }
}
