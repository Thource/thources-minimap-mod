package dev.thource.wurmunlimited.clientmods.minimap.renderer.component;

import static com.wurmonline.client.renderer.cell.CellRenderable.world;
import static com.wurmonline.mesh.Tiles.Tile.TILE_ENCHANTED_TREE_OAK;
import static com.wurmonline.mesh.Tiles.Tile.TILE_TREE_OAK;

import com.wurmonline.client.game.IDataBuffer;
import com.wurmonline.client.game.PlayerObj;
import com.wurmonline.client.game.PlayerPosition;
import com.wurmonline.client.game.TerrainDataInformationProvider;
import com.wurmonline.mesh.Tiles;
import dev.thource.wurmunlimited.clientmods.minimap.Settings;
import dev.thource.wurmunlimited.clientmods.minimap.Vector2i;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.ImageManager;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.LayerRenderer;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.topology.ShadedRelief;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public abstract class TileRenderer {

  protected final LayerRenderer layerRenderer;
  protected final Map<Vector2i, Boolean> dirtyTiles = new HashMap<>();
  protected final Object imageLock = new Object();
  private final int bufferSize = 302;
  public TerrainDataInformationProvider tileBuffer;

  @Getter
  protected BufferedImage image =
      new BufferedImage(
          bufferSize * Settings.getTileSize(),
          bufferSize * Settings.getTileSize(),
          BufferedImage.TYPE_INT_RGB);

  protected BufferedImage transferImage =
      new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
  @Getter protected int centerX = -1000;
  @Getter protected int centerY = -1000;

  public TileRenderer(LayerRenderer layerRenderer) {
    this.layerRenderer = layerRenderer;
  }

  // returns true if the image was re-centered
  private boolean recenter() {
    PlayerObj player = world.getPlayer();
    PlayerPosition pos = player.getPos();
    int tileX = pos.getTileX();
    int tileY = pos.getTileY();
    if (centerX == tileX && centerY == tileY) {
      return false;
    }

    int shiftX = tileX - centerX;
    int shiftY = tileY - centerY;

    Graphics2D transferImageGfx = transferImage.createGraphics();
    transferImageGfx.drawImage(
        image, -shiftX * Settings.getTileSize(), -shiftY * Settings.getTileSize(), null);
    transferImageGfx.dispose();

    BufferedImage tmp = image;
    image = transferImage;
    transferImage = tmp;
    centerX = tileX;
    centerY = tileY;

    transferImageGfx = transferImage.createGraphics();
    transferImageGfx.clearRect(0, 0, transferImage.getWidth(), transferImage.getHeight());
    transferImageGfx.dispose();
    return true;
  }

  private void setDirty(int tileX, int tileY) {
    dirtyTiles.put(new Vector2i(tileX, tileY), true);
  }

  public void setDirty(int startX, int startY, int endX, int endY) {
    synchronized (dirtyTiles) {
      for (int x = startX; x < endX; x++) {
        for (int y = startY; y < endY; y++) {
          // check that the tile is loaded, this is required because some tiles can be loaded before
          // the minimap is loaded
          if (isTileValid(x, y)) {
            setDirty(x, y);
          }
        }
      }
    }
  }

  protected abstract float getWaterHeight(int tileX, int tileY);

  protected abstract float getInterpolatedHeight(float worldX, float worldY);

  protected void drawWaterAndGeometry(RenderedTile renderedTile, int tileX, int tileY) {
    float waterHeight = getWaterHeight(tileX, tileY);
    float nwCorner = getHeight(tileX, tileY);
    float neCorner = getHeight(tileX + 1, tileY);
    float swCorner = getHeight(tileX, tileY + 1);
    float seCorner = getHeight(tileX + 1, tileY + 1);
    if (!Settings.isTransparentWater()
        && nwCorner < waterHeight
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

    if (Settings.isRenderHeight()) {
      for (int py = 0; py < Settings.getTileSize(); py++) {
        float worldY = (tileY + ((float) py / (Settings.getTileSize() - 1))) * 4f;
        for (int px = 0; px < Settings.getTileSize(); px++) {
          float worldX = (tileX + ((float) px / (Settings.getTileSize() - 1))) * 4f;

          float pointHeight = getInterpolatedHeight(worldX, worldY);
          float alpha = Math.min(70 + waterHeight - pointHeight * 5, 255) / 255f;
          if ((Settings.isTransparentWater() || dryTile || waterHeight < pointHeight) && alpha < 1) {
            renderHeight(px, py, renderedTile, worldX, worldY);
          }
        }
      }
    }
    if (dryTile) {
      return;
    }

    for (int py = 0; py < Settings.getTileSize(); py++) {
      float worldY = (tileY + ((float) py / (Settings.getTileSize() - 1))) * 4f;
      for (int px = 0; px < Settings.getTileSize(); px++) {
        float worldX = (tileX + ((float) px / (Settings.getTileSize() - 1))) * 4f;

        float pointHeight = getInterpolatedHeight(worldX, worldY);
        if (waterHeight >= pointHeight) {
          float alpha = Math.min(70 + waterHeight - pointHeight * 5, 255) / 255f;
          if (Settings.isTransparentWater() && alpha < 1) {
            Color currentColor = new Color(renderedTile.getImage().getRGB(px, py));
            Color waterColor = new Color(ImageManager.waterImage.getRGB(px, py));
            Color newColor =
                new Color(
                    (int) (currentColor.getRed() * (1 - alpha) + waterColor.getRed() * alpha),
                    (int) (currentColor.getGreen() * (1 - alpha) + waterColor.getGreen() * alpha),
                    (int) (currentColor.getBlue() * (1 - alpha) + waterColor.getBlue() * alpha));
            renderedTile.getImage().setRGB(px, py, newColor.getRGB());
          } else {
            renderedTile.getImage().setRGB(px, py, ImageManager.waterImage.getRGB(px, py));
          }
        }
      }
    }
  }

  private void renderHeight(int px, int py, RenderedTile renderedTile, float worldX, float worldY) {
    Color heightColor =
        ShadedRelief.getColor(
            (IDataBuffer) tileBuffer, worldX, worldY, (1f / Settings.getTileSize()) * 4f);
    float alpha = heightColor.getAlpha() / 255f;
    Color currentColor = new Color(renderedTile.getImage().getRGB(px, py));
    Color newColor =
        new Color(
            (int) (currentColor.getRed() * (1 - alpha) + heightColor.getRed() * alpha),
            (int) (currentColor.getGreen() * (1 - alpha) + heightColor.getGreen() * alpha),
            (int) (currentColor.getBlue() * (1 - alpha) + heightColor.getBlue() * alpha));

    renderedTile.getImage().setRGB(px, py, newColor.getRGB());
  }

  protected abstract float getHeight(int tileX, int tileY);

  public abstract boolean isTileValid(int tileX, int tileY);

  private void drawDirtyTiles() {
    synchronized (dirtyTiles) {
      Graphics2D graphics = image.createGraphics();
      dirtyTiles
          .keySet()
          .forEach(
              pos -> {
                RenderedTile tile = renderTile(pos.x, pos.y);
                int canvasX = (tile.getX() - centerX) + (bufferSize / 2);
                int canvasY = (tile.getY() - centerY) + (bufferSize / 2);
                graphics.drawImage(
                    tile.getImage(),
                    canvasX * Settings.getTileSize(),
                    canvasY * Settings.getTileSize(),
                    null);
              });
      graphics.dispose();
      dirtyTiles.clear();
    }
  }

  protected RenderedTile renderTile(int x, int y) {
    Tiles.Tile tileType = tileBuffer.getTileType(x, y);

    if (tileType.isTree() || tileType.isBush()) {
      tileType = tileType.isEnchanted() ? TILE_ENCHANTED_TREE_OAK : TILE_TREE_OAK;
    }

    BufferedImage tileImage =
        ImageManager.tileImages.getOrDefault(tileType, ImageManager.missingImage);

    if (tileImage == ImageManager.missingImage && tileType.isSolidCave()) {
      tileImage = ImageManager.holeImage;
    }

    if (tileImage == ImageManager.missingImage) {
      System.out.println("Missing image for tile: " + tileType.getName());
    }

    RenderedTile renderedTile = new RenderedTile(x, y);
    Graphics2D graphics = renderedTile.getImage().createGraphics();
    graphics.drawImage(tileImage, 0, 0, null);
    graphics.dispose();

    return renderedTile;
  }

  public void render() {
    synchronized (imageLock) {
      recenter();
      drawDirtyTiles();
    }
  }
}
