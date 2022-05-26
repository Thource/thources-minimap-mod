package dev.thource.wurmunlimited.clientmods.minimap.renderer;

import com.wurmonline.client.game.PlayerObj;
import com.wurmonline.client.game.PlayerPosition;
import com.wurmonline.client.game.World;
import com.wurmonline.client.renderer.structures.BridgeData;
import com.wurmonline.client.renderer.structures.FenceData;
import com.wurmonline.client.renderer.structures.HouseData;
import com.wurmonline.client.renderer.structures.StructureData;
import dev.thource.wurmunlimited.clientmods.minimap.Constants;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.component.RenderedBridge;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.component.RenderedFence;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.component.RenderedTile;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.component.TileRenderer;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.Getter;

public abstract class LayerRenderer {

  protected final World world;
  protected final int layerId;
  protected final Object imageLock = new Object();
  private final int bufferSize = 302;
  protected BufferedImage image =
      new BufferedImage(
          bufferSize * Constants.TILE_SIZE,
          bufferSize * Constants.TILE_SIZE,
          BufferedImage.TYPE_INT_RGB);
  protected BufferedImage transferImage =
      new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
  @Getter protected HashMap<Long, RenderedFence> fences = new HashMap<>();
  @Getter protected HashMap<Long, RenderedBridge> bridges = new HashMap<>();
  @Getter protected int centerX = -1000;
  @Getter protected int centerY = -1000;
  protected TileRenderer tileRenderer;

  LayerRenderer(World world, int layerId) {
    this.world = world;
    this.layerId = layerId;

    //    ClassPool pool = ClassPool.getDefault();
    //    CtClass ctClass;
    //    try {
    //      ctClass = pool.get("com.wurmonline.client.comm.ServerConnectionListenerClass");
    //    } catch (NotFoundException e) {
    //      throw new RuntimeException(e);
    //    }
    //    if (ctClass.isFrozen()) {
    //      ctClass.defrost();
    //    }

    //    try {
    //      CtClass ctClass =
    //          HookManager.getInstance()
    //              .getClassPool()
    //              .get("com.wurmonline.client.renderer.cell.CellRenderer");
    //      if (ctClass.isFrozen()) {
    //        ctClass.defrost();
    //      }
    //
    //      CtMethod addStructureMethod =
    //          ctClass.getMethod(
    //              "addStructure", "(Lcom/wurmonline/client/renderer/structures/StructureData;)V");
    //      addStructureMethod.insertAfter(
    //          "java.lang.System.out.println(\"addStructure bridge: \"\n"
    //              + "                          + (structure instanceof
    // com.wurmonline.client.renderer.structures.BridgeData)\n"
    //              + "                          + \", fence: \"\n"
    //              + "                          + (structure instanceof
    // com.wurmonline.client.renderer.structures.FenceData)\n"
    //              + "                          + \", house: \"\n"
    //              + "                          + (structure instanceof
    // com.wurmonline.client.renderer.structures.HouseData)\n"
    //              + "                          + \", layer: \"\n"
    //              + "                          + structure.getLayer());");
    //
    //      ctClass.freeze();
    //    } catch (NotFoundException | CannotCompileException e) {
    //      throw new RuntimeException(e);
    //    }

    //    HookManager.getInstance()
    //        .registerHook(
    //            "com.wurmonline.client.renderer.cell.CellRenderer",
    //            "addStructure",
    //            "(Lcom/wurmonline/client/renderer/structures/StructureData;)V",
    //            () ->
    //                (proxy, method, args) -> {
    //                  method.invoke(proxy, args);
    //
    //                  StructureData structureData = (StructureData) args[0];
    //                  System.out.println(
    //                      "addStructure bridge: "
    //                          + (structureData instanceof BridgeData)
    //                          + ", fence: "
    //                          + (structureData instanceof FenceData)
    //                          + ", house: "
    //                          + (structureData instanceof HouseData)
    //                          + ", layer: "
    //                          + structureData.getLayer());
    //                  if (structureData.getLayer() == this.layerId) {
    //                    if (structureData instanceof BridgeData) {
    //                      bridges.put(
    //                          structureData.getId(), new RenderedBridge((BridgeData)
    // structureData));
    //                    } else if (structureData instanceof FenceData) {
    //                      //            fenceRenderer.render(structuresImage, (FenceData)
    //                      // structureData);
    //                    } else if (structureData instanceof HouseData) {
    //                      //            houseRenderer.render(structuresImage, (HouseData)
    //                      // structureData);
    //                    }
    //                  }
    //
    //                  //noinspection SuspiciousInvocationHandlerImplementation
    //                  return null;
    //                });
    //
    //    HookManager.getInstance()
    //        .registerHook(
    //            "com.wurmonline.client.renderer.cell.CellRenderer",
    //            "removeStructure",
    //            "(Lcom/wurmonline/client/renderer/structures/StructureData;)V",
    //            () ->
    //                (proxy, method, args) -> {
    //                  method.invoke(proxy, args);
    //
    //                  StructureData structureData = (StructureData) args[0];
    //                  System.out.println(
    //                      "removeStructure bridge: "
    //                          + (structureData instanceof BridgeData)
    //                          + ", fence: "
    //                          + (structureData instanceof FenceData)
    //                          + ", house: "
    //                          + (structureData instanceof HouseData)
    //                          + ", layer: "
    //                          + structureData.getLayer());
    //                  if (structureData.getLayer() == this.layerId) {
    //                    if (structureData instanceof BridgeData) {
    //                      bridges.remove(structureData.getId());
    //                    } else if (structureData instanceof FenceData) {
    //                      //            fenceRenderer.render(structuresImage, (FenceData)
    //                      // structureData);
    //                    } else if (structureData instanceof HouseData) {
    //                      //            houseRenderer.render(structuresImage, (HouseData)
    //                      // structureData);
    //                    }
    //                  }
    //
    //                  //noinspection SuspiciousInvocationHandlerImplementation
    //                  return null;
    //                });

    //    ctClass.freeze();
  }

  public BufferedImage render() {
    return render(false);
  }

  // returns true if the image was re-centered
  private boolean recenter() {
    synchronized (imageLock) {
      PlayerObj player = world.getPlayer();
      PlayerPosition pos = player.getPos();
      int tileX = pos.getTileX();
      int tileY = pos.getTileY();
      if (centerX == tileX && centerY == tileY) {
        return false;
      }

      int xShift = tileX - centerX;
      int yShift = tileY - centerY;

      Graphics2D transferImageGfx = transferImage.createGraphics();
      transferImageGfx.drawImage(
          image, -xShift * Constants.TILE_SIZE, -yShift * Constants.TILE_SIZE, null);
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
  }

  public BufferedImage render(boolean fullRender) {
    if (fullRender) {
      synchronized (imageLock) {
        Graphics2D imageGfx = image.createGraphics();
        imageGfx.clearRect(0, 0, image.getWidth(), image.getHeight());
        imageGfx.dispose();
      }
      renderTiles(
          centerX - (bufferSize / 2),
          centerY - (bufferSize / 2),
          centerX + (bufferSize / 2),
          centerY + (bufferSize / 2),
          true);

      synchronized (imageLock) {
        File outputFile = new File("tests/" + System.currentTimeMillis() + "-render.png");
        try {
          ImageIO.write(image, "png", outputFile);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
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
    //                    renderedImage.setRGB(px, py, waterImage.getRGB((px % tileSize) * (64 /
    // tileSize), (py % tileSize) * (64 / tileSize)));
    //            }
    //        }
    //
    //        renderedTileX = tileX;
    //        renderedTileY = tileY;
    //        renderedLayer = layer;
    //        isDirty = false;
    //
    //
    // world.getServerConnection().getServerConnectionListener().getStructures().values().stream().sorted(Comparator.comparingInt((structure) -> {
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

  public void renderTiles(int startX, int startY, int endX, int endY, boolean checkForValidity) {
    List<RenderedTile> tiles = new ArrayList<>();
    for (int x = startX; x < endX; x++) {
      for (int y = startY; y < endY; y++) {
        // check that the tile is loaded, this is required because some tiles can be loaded before
        // the minimap is loaded
        if (checkForValidity && !tileRenderer.isTileValid(x, y)) {
          continue;
        }

        tiles.add(tileRenderer.render(x, y));
      }
    }

    recenter();

    synchronized (imageLock) {
      Graphics2D imageGfx = image.createGraphics();
      tiles.forEach(
          tile -> {
            int canvasX = (tile.getX() - centerX) + (bufferSize / 2);
            int canvasY = (tile.getY() - centerY) + (bufferSize / 2);
            imageGfx.drawImage(
                tile.getImage(),
                canvasX * Constants.TILE_SIZE,
                canvasY * Constants.TILE_SIZE,
                null);
          });
      imageGfx.dispose();
    }
  }

  public void addStructure(StructureData structureData) {
    if (structureData.getLayer() != this.layerId) {
      return;
    }

    if (structureData instanceof BridgeData) {
      bridges.put(structureData.getId(), new RenderedBridge((BridgeData) structureData));
    } else if (structureData instanceof FenceData) {
      fences.put(structureData.getId(), new RenderedFence((FenceData) structureData));
    } else if (structureData instanceof HouseData) {
      //            houseRenderer.render(structuresImage, (HouseData)
      // structureData);
    }
  }

  public void removeStructure(StructureData structureData) {
    if (structureData.getLayer() != this.layerId) {
      return;
    }

    if (structureData instanceof BridgeData) {
      bridges.remove(structureData.getId());
    } else if (structureData instanceof FenceData) {
      fences.remove(structureData.getId());
      // structureData);
    } else if (structureData instanceof HouseData) {
      //            houseRenderer.render(structuresImage, (HouseData)
      // structureData);
    }
  }
}
