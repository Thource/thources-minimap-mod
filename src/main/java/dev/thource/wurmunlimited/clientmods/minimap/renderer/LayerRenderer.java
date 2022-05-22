package dev.thource.wurmunlimited.clientmods.minimap.renderer;

import com.wurmonline.client.game.PlayerObj;
import com.wurmonline.client.game.PlayerPosition;
import com.wurmonline.client.game.World;
import dev.thource.wurmunlimited.clientmods.minimap.Constants;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.component.TileRenderer;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public abstract class LayerRenderer {

  protected final World world;
  private final int bufferSize = 302;
  protected BufferedImage image = new BufferedImage(bufferSize * Constants.TILE_SIZE,
      bufferSize * Constants.TILE_SIZE,
      BufferedImage.TYPE_INT_RGB);
  protected BufferedImage transferImage = new BufferedImage(image.getWidth(),
      image.getHeight(), image.getType());
  protected int centerX = -1000;
  protected int centerY = -1000;
  protected TileRenderer tileRenderer;

  LayerRenderer(World world) {
    this.world = world;
  }

  public BufferedImage render() {
    return render(false);
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

    int xShift = tileX - centerX;
    int yShift = tileY - centerY;
    Graphics2D imageGfx = transferImage.createGraphics();
    imageGfx.drawImage(image, -xShift * Constants.TILE_SIZE, -yShift * Constants.TILE_SIZE, null);
    imageGfx.dispose();

    BufferedImage temp = transferImage;
    transferImage = image;
    image = temp;

    centerX = tileX;
    centerY = tileY;
    return true;
  }

  public BufferedImage render(boolean fullRender) {
    boolean didRecenter = recenter();

    if (fullRender) {
      Graphics2D imageGfx = image.createGraphics();
      for (int x = -bufferSize / 2; x < bufferSize / 2; x++) {
        for (int y = -bufferSize / 2; y < bufferSize / 2; y++) {
          int absoluteX = centerX + x;
          int absoluteY = centerY + y;
          // check that the tile is loaded, this is required because some tiles can be loaded before the minimap is loaded
          if (!tileRenderer.isTileValid(absoluteX, absoluteY)) {
            continue;
          }

          renderTile(imageGfx, absoluteX, absoluteY);
        }
      }
      imageGfx.dispose();
    }

    if (didRecenter) {
      File outputfile = new File("tests/" + System.currentTimeMillis() + "-render.png");
      try {
        ImageIO.write(image, "png", outputfile);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

//        NearTerrainDataBuffer nearTerrainBuffer = world.getNearTerrainBuffer();
//        float waterHeight = nearTerrainBuffer.getWaterHeight(x, y);
//        for (int py = 0; py < imageSize; py++) {
//            for (int px = 0; px < imageSize; px++) {
//                int pTileX = tileX - (tilesToRender / 2);
//                float worldX = ((pTileX) + (px / (float) (tileSize - 1))) * 4f;
//                int pTileY = tileY - (tilesToRender / 2);
//                float worldY = ((pTileY) + (py / (float) (tileSize - 1))) * 4f;
//
//                float pointHeight = nearTerrainBuffer.getInterpolatedHeight(worldX, worldY);
//                if (waterHeight >= pointHeight)
//                    renderedImage.setRGB(px, py, waterImage.getRGB((px % tileSize) * (64 / tileSize), (py % tileSize) * (64 / tileSize)));
//            }
//        }
//
//        renderedTileX = tileX;
//        renderedTileY = tileY;
//        renderedLayer = layer;
//        isDirty = false;
//
//        world.getServerConnection().getServerConnectionListener().getStructures().values().stream().sorted(Comparator.comparingInt((structure) -> {
//            if (structure instanceof FenceData) return 0;
//            if (structure instanceof BridgeData) return 1;
//            if (structure instanceof HouseData) return 2;
//
//            return 100;
//        })).forEach((structure) -> {
//            if (structure.getLayer() == layer) drawStructure(gfx, structure);
//        });

    return image;
  }

  private void renderTile(Graphics2D imageGfx, int tileX, int tileY) {
    int canvasX = (tileX - centerX) + (bufferSize / 2);
    int canvasY = (tileY - centerY) + (bufferSize / 2);
    imageGfx.drawImage(
        tileRenderer.render(tileX, tileY),
        canvasX * Constants.TILE_SIZE,
        canvasY * Constants.TILE_SIZE,
        Constants.TILE_SIZE,
        Constants.TILE_SIZE,
        null
    );
  }

  public void renderTiles(int startX, int startY, int endX, int endY) {
    recenter();

    Graphics2D imageGfx = image.createGraphics();
    System.out.println(
        "renderTiles startX: " + startX + " endX: " + endX + " startY: " + startY + " endY: " + endY
            + " centerX: " + centerX + " centerY: " + centerY);
    System.out.println(
        "canvasX: " + ((startX - centerX) + (bufferSize / 2)) + " canvasY: " + ((startY - centerY)
            + (bufferSize / 2)));
    for (int x = startX; x < endX; x++) {
      for (int y = startY; y < endY; y++) {
        renderTile(imageGfx, x, y);
      }
    }
    imageGfx.dispose();
  }
}
