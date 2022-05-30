package dev.thource.wurmunlimited.clientmods.minimap.renderer;

import com.wurmonline.client.game.PlayerObj;
import com.wurmonline.client.game.PlayerPosition;
import com.wurmonline.client.game.World;
import com.wurmonline.client.renderer.structures.BridgeData;
import com.wurmonline.client.renderer.structures.BridgePartData;
import com.wurmonline.client.renderer.structures.FenceData;
import com.wurmonline.client.renderer.structures.HouseData;
import com.wurmonline.client.renderer.structures.HouseFloorData;
import com.wurmonline.client.renderer.structures.HouseRoofData;
import com.wurmonline.client.renderer.structures.HouseWallData;
import com.wurmonline.client.renderer.structures.StructureData;
import dev.thource.wurmunlimited.clientmods.minimap.Constants;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.component.RenderedBridge;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.component.RenderedFence;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.component.RenderedHouse;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.component.RenderedTile;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.component.TileRenderer;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import lombok.Getter;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

public abstract class LayerRenderer {

  protected final World world;
  protected final int layerId;
  protected final Object imageLock = new Object();
  @Getter protected final HashMap<Long, RenderedFence> fences = new HashMap<>();
  @Getter protected final HashMap<Long, RenderedBridge> bridges = new HashMap<>();
  @Getter protected final HashMap<Long, RenderedHouse> houses = new HashMap<>();
  private final int bufferSize = 302;
  protected BufferedImage image =
      new BufferedImage(
          bufferSize * Constants.TILE_SIZE,
          bufferSize * Constants.TILE_SIZE,
          BufferedImage.TYPE_INT_RGB);
  protected BufferedImage transferImage =
      new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
  @Getter protected int centerX = -1000;
  @Getter protected int centerY = -1000;
  protected TileRenderer tileRenderer;
  ScheduledExecutorService renderExecutor = Executors.newSingleThreadScheduledExecutor();

  LayerRenderer(World world, int layerId) {
    this.world = world;
    this.layerId = layerId;

    renderExecutor.scheduleAtFixedRate(
        () -> {
          synchronized (bridges) {
            bridges.values().forEach(RenderedBridge::render);
          }
          synchronized (houses) {
            houses.values().forEach(RenderedHouse::render);
          }
        },
        0,
        100,
        TimeUnit.MILLISECONDS);
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

    System.out.println("addStructure: " + structureData.getClass().getName());
    if (structureData instanceof BridgeData && !bridges.containsKey(structureData.getId())) {
      RenderedBridge bridge = new RenderedBridge((BridgeData) structureData);

      synchronized (bridges) {
        bridges.put(structureData.getId(), bridge);
      }
      return;
    } else if (structureData instanceof HouseData && !houses.containsKey(structureData.getId())) {
      RenderedHouse house = new RenderedHouse((HouseData) structureData);

      synchronized (houses) {
        houses.put(structureData.getId(), house);
      }
      return;
    }

    new Thread(
            () -> {
              if (structureData instanceof BridgePartData) {
                synchronized (bridges) {
                  RenderedBridge bridge =
                      bridges.get(((BridgePartData) structureData).getBridgeId());
                  if (bridge != null) {
                    bridge.addBridgePart((BridgePartData) structureData);
                  }
                }
              } else if (structureData instanceof FenceData) {
                synchronized (fences) {
                  if (!fences.containsKey(structureData.getId())) {
                    RenderedFence fence = new RenderedFence((FenceData) structureData);

                    fences.put(structureData.getId(), fence);
                  }
                }
              } else if (structureData instanceof HouseWallData) {
                try {
                  synchronized (houses) {
                    HouseData houseData =
                        ReflectionUtil.getPrivateField(
                            structureData, ReflectionUtil.getField(HouseWallData.class, "house"));
                    RenderedHouse house = houses.get(houseData.getId());
                    if (house != null) {
                      house.addHouseWall((HouseWallData) structureData);
                    }
                  }
                } catch (IllegalAccessException | NoSuchFieldException e) {
                  throw new RuntimeException(e);
                }
              } else if (structureData instanceof HouseRoofData) {
                try {
                  synchronized (houses) {
                    HouseData houseData =
                        ReflectionUtil.getPrivateField(
                            structureData, ReflectionUtil.getField(HouseRoofData.class, "house"));
                    RenderedHouse house = houses.get(houseData.getId());
                    if (house != null) {
                      house.addHouseRoof((HouseRoofData) structureData);
                    }
                  }
                } catch (IllegalAccessException | NoSuchFieldException e) {
                  throw new RuntimeException(e);
                }
              } else if (structureData instanceof HouseFloorData) {
                try {
                  synchronized (houses) {
                    HouseData houseData =
                        ReflectionUtil.getPrivateField(
                            structureData, ReflectionUtil.getField(HouseFloorData.class, "house"));
                    RenderedHouse house = houses.get(houseData.getId());
                    if (house != null) {
                      house.addHouseFloor((HouseFloorData) structureData);
                    }
                  }
                } catch (IllegalAccessException | NoSuchFieldException e) {
                  throw new RuntimeException(e);
                }
              } else {
                System.out.println(
                    "NOT HANDLED: "
                        + structureData.getClass().getName()
                        + ", tileX: "
                        + structureData.getTileX()
                        + ", tileY: "
                        + structureData.getTileY());
              }
            })
        .start();
  }

  public void removeStructure(StructureData structureData) {
    if (structureData.getLayer() != this.layerId) {
      return;
    }

    System.out.println("removeStructure: " + structureData.getClass().getName());
    if (structureData instanceof BridgeData) {
      synchronized (bridges) {
        bridges.remove(structureData.getId());
      }
    } else if (structureData instanceof BridgePartData) {
      synchronized (bridges) {
        RenderedBridge bridge = bridges.get(((BridgePartData) structureData).getBridgeId());
        if (bridge != null) {
          bridge.removeBridgePart((BridgePartData) structureData);
        }
      }
    } else if (structureData instanceof FenceData) {
      synchronized (fences) {
        fences.remove(structureData.getId());
      }
    } else if (structureData instanceof HouseData) {
      synchronized (houses) {
        houses.remove(structureData.getId());
      }
    } else {
      System.out.println(
          "NOT HANDLED: "
              + structureData.getClass().getName()
              + ", tileX: "
              + structureData.getTileX()
              + ", tileY: "
              + structureData.getTileY());
    }
  }
}
