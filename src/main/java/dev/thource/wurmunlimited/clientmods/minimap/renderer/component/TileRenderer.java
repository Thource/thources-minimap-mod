package dev.thource.wurmunlimited.clientmods.minimap.renderer.component;

import com.wurmonline.client.game.IDataBuffer;
import com.wurmonline.client.game.TerrainDataInformationProvider;
import com.wurmonline.client.game.World;
import dev.thource.wurmunlimited.clientmods.minimap.Constants;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.ImageManager;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.LayerRenderer;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.topology.ShadedRelief;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public abstract class TileRenderer {

  protected final LayerRenderer layerRenderer;
  public TerrainDataInformationProvider tileBuffer;

  public TileRenderer(World world, LayerRenderer layerRenderer) {
    this.layerRenderer = layerRenderer;
  }

  public abstract RenderedTile render(int tileX, int tileY);

  protected abstract float getWaterHeight(int tileX, int tileY);

  protected abstract float getInterpolatedHeight(float worldX, float worldY);

  protected void renderWaterAndGeometry(RenderedTile renderedTile, int tileX, int tileY) {
    float waterHeight = getWaterHeight(tileX, tileY);
    float nwCorner = getHeight(tileX, tileY);
    float neCorner = getHeight(tileX + 1, tileY);
    float swCorner = getHeight(tileX, tileY + 1);
    float seCorner = getHeight(tileX + 1, tileY + 1);
    if (nwCorner < waterHeight
        && neCorner < waterHeight
        && swCorner < waterHeight
        && seCorner < waterHeight) {
      Graphics2D graphics = renderedTile.getImage().createGraphics();
      graphics.drawImage(ImageManager.waterImage, 0, 0, null);
      graphics.dispose();
      return;
    }

    // height rendering
    boolean dryTile =
        nwCorner >= waterHeight
            && neCorner >= waterHeight
            && swCorner >= waterHeight
            && seCorner >= waterHeight;

    for (int py = 0; py < Constants.TILE_SIZE; py++) {
      for (int px = 0; px < Constants.TILE_SIZE; px++) {
        float worldX = (tileX + ((float) px / (Constants.TILE_SIZE - 1))) * 4f;
        float worldY = (tileY + ((float) py / (Constants.TILE_SIZE - 1))) * 4f;

        float pointHeight = getInterpolatedHeight(worldX, worldY);
        if (!dryTile && waterHeight >= pointHeight) {
          renderedTile.getImage().setRGB(px, py, ImageManager.waterImage.getRGB(px, py));
        } else {
          renderHeight(px, py, renderedTile, pointHeight, worldX, worldY);
        }
      }
    }

    //    if (nwCorner >= waterHeight &&
    //        neCorner >= waterHeight &&
    //        swCorner >= waterHeight &&
    //        seCorner >= waterHeight) {
    //      return originalImage;
    //    }
    //
    //    BufferedImage tileImage = cloneImage(originalImage);
    //    for (int py = 0; py < Constants.TILE_SIZE; py++) {
    //      for (int px = 0; px < Constants.TILE_SIZE; px++) {
    //        float worldX = (tileX + ((float) px / (Constants.TILE_SIZE - 1))) * 4f;
    //        float worldY = (tileY + ((float) py / (Constants.TILE_SIZE - 1))) * 4f;
    //
    //        float pointHeight = getInterpolatedHeight(worldX, worldY);
    //        if (waterHeight >= pointHeight) {
    //          tileImage.setRGB(px, py, ImageManager.waterImage.getRGB(px, py));
    //        }
    //      }
    //    }

  }

  private float getAveragePointHeight(
      float worldX, float worldY, int px, int py, float dirX, float dirY, int pixels) {
    float average = 0;

    for (int i = 0; i < pixels; i++) {
      average +=
          getInterpolatedHeight(
              worldX + (px * (1f / Constants.TILE_SIZE)) + (dirX * (1f / Constants.TILE_SIZE) * i),
              worldY + (py * (1f / Constants.TILE_SIZE)) + (dirY * (1f / Constants.TILE_SIZE) * i));
    }

    return average / pixels;
  }

  private void renderHeight(
      int px, int py, RenderedTile renderedTile, float pointHeight, float worldX, float worldY) {
    Color heightColor =
        ShadedRelief.getColor(
            (IDataBuffer) tileBuffer, worldX, worldY, (1f / Constants.TILE_SIZE) * 4f);
    float alpha = heightColor.getAlpha() / 255f;
    Color currentColor = new Color(renderedTile.getImage().getRGB(px, py));
    Color newColor =
        new Color(
            (int) (currentColor.getRed() * (1 - alpha) + heightColor.getRed() * alpha),
            (int) (currentColor.getGreen() * (1 - alpha) + heightColor.getGreen() * alpha),
            (int) (currentColor.getBlue() * (1 - alpha) + heightColor.getBlue() * alpha));

    renderedTile.getImage().setRGB(px, py, newColor.getRGB());

    //    float southPointHeight = getAveragePointHeight(worldX, worldY, px, py, 0, 1,
    // Math.max(Constants.TILE_SIZE / 4, 1));
    //    float eastPointHeight = getAveragePointHeight(worldX, worldY, px, py, 1, 0,
    // Math.max(Constants.TILE_SIZE / 4, 1));
    //    if (Math.sqrt(southPointHeight * eastPointHeight) > pointHeight) {
    //      float factor =
    //          (float) Math.min(
    //              (Math.sqrt(southPointHeight * eastPointHeight) - pointHeight) / 2f,
    //              1f);
    //      Color currentColor = new Color(tileImage.getRGB(px, py));
    //      Color newColor = new Color((int) (currentColor.getRed() * (1 - factor) + 255 * factor),
    //          (int) (currentColor.getGreen() * (1 - factor) + 255 * factor),
    //          (int) (currentColor.getBlue() * (1 - factor) + 255 * factor));
    //
    //      tileImage.setRGB(px, py, newColor.getRGB());
    //    } else {
    //      float northPointHeight = getAveragePointHeight(worldX, worldY, px, py, 0, -1,
    // Math.max(Constants.TILE_SIZE / 4, 1));
    //      float westPointHeight = getAveragePointHeight(worldX, worldY, px, py, -1, 0,
    // Math.max(Constants.TILE_SIZE / 4, 1));
    //      if (Math.sqrt(northPointHeight * westPointHeight) > pointHeight) {
    //        float factor =
    //            (float) Math.min(
    //                (Math.sqrt(northPointHeight * westPointHeight) - pointHeight)
    //                    / 2f,
    //                1f);
    //        Color currentColor = new Color(tileImage.getRGB(px, py));
    //        Color newColor = new Color((int) (currentColor.getRed() * (1 - factor)),
    //            (int) (currentColor.getGreen() * (1 - factor)),
    //            (int) (currentColor.getBlue() * (1 - factor)));
    //
    //        tileImage.setRGB(px, py, newColor.getRGB());
    //      }
    //    }
  }

  protected abstract float getHeight(int tileX, int tileY);

  protected BufferedImage cloneImage(BufferedImage original) {
    BufferedImage clone =
        new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
    Graphics2D gfx = clone.createGraphics();
    gfx.drawImage(original, null, 0, 0);
    gfx.dispose();

    return clone;
  }

  public abstract boolean isTileValid(int tileX, int tileY);
}
