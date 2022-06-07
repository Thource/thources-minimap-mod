package dev.thource.wurmunlimited.clientmods.minimap.renderer;

import com.wurmonline.client.renderer.structures.BridgeData;
import com.wurmonline.client.renderer.structures.BridgePartData;
import com.wurmonline.client.renderer.structures.FenceData;
import com.wurmonline.client.renderer.structures.HouseData;
import com.wurmonline.client.renderer.structures.HouseFloorData;
import com.wurmonline.client.renderer.structures.HouseRoofData;
import com.wurmonline.client.renderer.structures.HouseWallData;
import com.wurmonline.client.renderer.structures.StructureData;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.component.RenderedBridge;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.component.RenderedFence;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.component.RenderedHouse;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.component.TileRenderer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

public abstract class LayerRenderer {

  protected final int layerId;
  @Getter protected final HashMap<Long, RenderedFence> fences = new HashMap<>();
  @Getter protected final HashMap<Long, RenderedBridge> bridges = new HashMap<>();
  @Getter protected final HashMap<Long, RenderedHouse> houses = new HashMap<>();
  @Getter protected TileRenderer tileRenderer;

  LayerRenderer(int layerId) {
    this.layerId = layerId;

    ScheduledExecutorService tileRenderExecutor = Executors.newSingleThreadScheduledExecutor();
    tileRenderExecutor.scheduleAtFixedRate(
        () -> {
          if (tileRenderer == null) {
            return;
          }

          try {
            tileRenderer.render();
          } catch (Exception e) {
            System.out.println(
                "Caught exception ("
                    + e
                    + ") in ScheduledExecutorService. StackTrace:\n"
                    + Arrays.toString(e.getStackTrace()));
            throw e;
          }
        },
        100,
        100,
        TimeUnit.MILLISECONDS);

    ScheduledExecutorService structureRenderExecutor = Executors.newSingleThreadScheduledExecutor();
    structureRenderExecutor.scheduleAtFixedRate(
        () -> {
          try {
            synchronized (bridges) {
              bridges.values().forEach(RenderedBridge::render);
            }
            synchronized (houses) {
              houses.values().forEach(RenderedHouse::render);
            }
          } catch (Exception e) {
            System.out.println(
                "Caught exception ("
                    + e
                    + ") in ScheduledExecutorService. StackTrace:\n"
                    + Arrays.toString(e.getStackTrace()));
            throw e;
          }
        },
        100,
        100,
        TimeUnit.MILLISECONDS);
  }

  public void addStructure(StructureData structureData) {
    if (structureData.getLayer() != this.layerId) {
      return;
    }

    if (structureData instanceof BridgeData) {
      if (!bridges.containsKey(structureData.getId())) {
        RenderedBridge bridge = new RenderedBridge((BridgeData) structureData);

        synchronized (bridges) {
          bridges.put(structureData.getId(), bridge);
        }
      }

      return;
    } else if (structureData instanceof HouseData) {
      if (!houses.containsKey(structureData.getId())) {
        RenderedHouse house = new RenderedHouse((HouseData) structureData);

        synchronized (houses) {
          houses.put(structureData.getId(), house);
        }
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
    } else if (structureData instanceof HouseFloorData) {
      try {
        synchronized (houses) {
          HouseData houseData =
              ReflectionUtil.getPrivateField(
                  structureData, ReflectionUtil.getField(HouseFloorData.class, "house"));
          RenderedHouse house = houses.get(houseData.getId());
          if (house != null) {
            house.removeHouseFloor((HouseFloorData) structureData);
          }
        }
      } catch (IllegalAccessException | NoSuchFieldException e) {
        throw new RuntimeException(e);
      }
    } else if (structureData instanceof HouseWallData) {
      try {
        synchronized (houses) {
          HouseData houseData =
              ReflectionUtil.getPrivateField(
                  structureData, ReflectionUtil.getField(HouseWallData.class, "house"));
          RenderedHouse house = houses.get(houseData.getId());
          if (house != null) {
            house.removeHouseWall((HouseWallData) structureData);
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
            house.removeHouseRoof((HouseRoofData) structureData);
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
  }
}
