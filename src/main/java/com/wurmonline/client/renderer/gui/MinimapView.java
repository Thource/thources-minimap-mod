//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.wurmonline.client.renderer.gui;

import com.wurmonline.client.game.PlayerObj;
import com.wurmonline.client.game.PlayerPosition;
import com.wurmonline.client.game.World;
import com.wurmonline.client.renderer.WorldRender;
import com.wurmonline.client.renderer.backend.Queue;
import com.wurmonline.client.renderer.structures.StructureData;
import com.wurmonline.client.resources.textures.ImageTexture;
import com.wurmonline.client.resources.textures.ImageTextureLoader;
import com.wurmonline.client.resources.textures.PreProcessedTextureData;
import com.wurmonline.client.resources.textures.TextureLoader;
import com.wurmonline.math.Vector2f;
import com.wurmonline.shared.util.MovementChecker;
import dev.thource.wurmunlimited.clientmods.minimap.Constants;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.CaveLayerRenderer;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.ImageManager;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.LayerRenderer;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.WorldLayerRenderer;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.component.RenderedBridge;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.component.RenderedFence;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

public class MinimapView extends FlexComponent {

  private final World world;
  private final WorldLayerRenderer worldLayerRenderer;
  private final CaveLayerRenderer caveLayerRenderer;
  private final Method preprocessImage;
  // TODO: move these somewhere configurable
  boolean isNorthFacing = false;
  int imageSize = Constants.TILE_SIZE * 302;
  private ImageTexture texture;

  private float zoomLevel = 1f;

  MinimapView(String name, int width, int height) {
    super(name);
    this.setInitialSize(width, height, false);
    this.sizeFlags = 3;
    try {
      this.world =
          ReflectionUtil.getPrivateField(hud, ReflectionUtil.getField(hud.getClass(), "world"));
    } catch (IllegalAccessException | NoSuchFieldException e) {
      throw new RuntimeException(e);
    }

    try {
      this.preprocessImage =
          ReflectionUtil.getMethod(
              TextureLoader.class,
              "preprocessImage",
              new Class[] {BufferedImage.class, Boolean.TYPE});
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }

    this.worldLayerRenderer = new WorldLayerRenderer(world);
    this.caveLayerRenderer = new CaveLayerRenderer(world);
  }

  @Override
  void mouseWheeled(int xMouse, int yMouse, int wheelDelta) {
    zoomLevel *= Math.pow(1.1, -wheelDelta / 3f);
    zoomLevel = Math.max(zoomLevel, 0.1f);
    System.out.println("wheeled, new zoom: " + zoomLevel);
  }

  public void fullRedraw() {
    new Thread(() -> worldLayerRenderer.render(true)).start();
  }

  protected void renderComponent(Queue queue, float alpha) {
    super.renderComponent(queue, alpha);

    PlayerObj player = world.getPlayer();
    PlayerPosition pos = player.getPos();
    int tileX = pos.getTileX();
    int tileY = pos.getTileY();
    int layer = world.getPlayerLayer();
    float xOffset = 1f - ((pos.getX() / 4.0f) - tileX);
    float yOffset = 1f - ((pos.getY() / 4.0f) - tileY);

    WorldRender worldRenderer = world.getWorldRenderer();
    BufferedImage framedImg = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
    Graphics2D framedGfx = framedImg.createGraphics();
    framedGfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    float cameraRotX;
    try {
      cameraRotX =
          ReflectionUtil.getPrivateField(
              worldRenderer, ReflectionUtil.getField(worldRenderer.getClass(), "cameraRotX"));
    } catch (IllegalAccessException | NoSuchFieldException e) {
      throw new RuntimeException(e);
    }

    if (!isNorthFacing) {
      framedGfx.rotate(-cameraRotX, 128, 128);
    }
    LayerRenderer layerRenderer = layer == -1 ? caveLayerRenderer : worldLayerRenderer;
    BufferedImage layerImage = layerRenderer.render();
    Vector2f layerPos =
        worldPosToPixelPos(
            (layerRenderer.getCenterX() - 151) * 4f, (layerRenderer.getCenterY() - 151) * 4f);
    framedGfx.drawImage(
        layerImage,
        (int) layerPos.x + 128,
        (int) layerPos.y + 128,
        Math.round(layerImage.getWidth() * zoomLevel),
        Math.round(layerImage.getHeight() * zoomLevel),
        null);

    MovementChecker movementChecker;
    try {
      movementChecker =
          ReflectionUtil.getPrivateField(
              world.getPlayer(), ReflectionUtil.getField(PlayerObj.class, "movementChecker"));
    } catch (IllegalAccessException | NoSuchFieldException e) {
      throw new RuntimeException(e);
    }

    for (RenderedFence fence : layerRenderer.getFences().values()) {
      Vector2f fencePixelPos = worldPosToPixelPos(fence.getTileX() * 4f, fence.getTileY() * 4f);
      framedGfx.drawImage(
          fence.getImage(),
          (int) fencePixelPos.x
              + 128
              - Math.round(
                  (fence.isHorizontal() ? 0 : RenderedFence.FENCE_THICKNESS / 2f) * zoomLevel),
          (int) fencePixelPos.y
              + 128
              - Math.round(
                  (fence.isHorizontal() ? RenderedFence.FENCE_THICKNESS / 2f : 0) * zoomLevel),
          Math.round(fence.getImage().getWidth() * zoomLevel),
          Math.round(fence.getImage().getHeight() * zoomLevel),
          null);
    }

    for (RenderedBridge bridge : layerRenderer.getBridges().values()) {
      boolean playerUnderBridge =
          movementChecker.getBridgeId() != bridge.getId() && bridge.isBridgeTile(tileX, tileY);

      if (playerUnderBridge) {
        framedGfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
      }

      Vector2f bridgePixelPos = worldPosToPixelPos(bridge.getTileX() * 4f, bridge.getTileY() * 4f);
      framedGfx.drawImage(
          bridge.getImage(),
          (int) bridgePixelPos.x + 128,
          (int) bridgePixelPos.y + 128,
          Math.round(bridge.getImage().getWidth() * zoomLevel),
          Math.round(bridge.getImage().getHeight() * zoomLevel),
          null);

      if (playerUnderBridge) {
        framedGfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
      }
    }

    world
        .getServerConnection()
        .getServerConnectionListener()
        .getCreatures()
        .forEach(
            (id, creature) -> {
              BufferedImage icon = ImageManager.getIconForCreature(creature);
              Vector2f creaturePixelPos =
                  worldPosToPixelPos(creature.getXPos(), creature.getYPos());
              framedGfx.drawImage(
                  icon,
                  (int) creaturePixelPos.x - icon.getWidth() / 2 + 128,
                  (int) creaturePixelPos.y - icon.getHeight() / 2 + 128,
                  null);
            });

    framedGfx.rotate(cameraRotX, 128, 128);
    framedGfx.drawImage(
        ImageManager.playerCursorImage,
        128 - ImageManager.playerCursorImage.getWidth() / 2,
        128 - ImageManager.playerCursorImage.getHeight() / 2,
        null);

    //    world.getServerConnection().getServerConnectionListener().getCreatures()
    //        .forEach((id, creature) -> {
    //          BufferedImage icon = ImageManager.getIconForCreature(creature);
    //          Vector2f creaturePixelPos = getPixelPos(creature.getXPos(), creature.getYPos());
    //          iconOverlayGfx.drawImage(icon, null, (int) creaturePixelPos.x - 4,
    //              (int) creaturePixelPos.y - 4);
    //        });

    framedGfx.dispose();

    if (this.texture == null) {
      this.texture = ImageTextureLoader.loadNowrapNearestTexture(framedImg, false);
    } else {
      try {
        PreProcessedTextureData data =
            ReflectionUtil.callPrivateMethod(
                TextureLoader.class, this.preprocessImage, new Object[] {framedImg, true});
        this.texture.deferInit(data, TextureLoader.Filter.NEAREST, false, false, false);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException var5) {
        throw new RuntimeException(var5);
      }
    }

    Renderer.texturedQuadAlphaBlend(
        queue, this.texture, 1.0F, 1.0F, 1.0F, 1.0F, this.x, this.y, 256, 256, 0, 0, 1, 1);
  }

  public Vector2f worldPosToPixelPos(float worldX, float worldY) {
    PlayerObj player = world.getPlayer();
    PlayerPosition pos = player.getPos();
    float centerX = pos.getX();
    float centerY = pos.getY();

    return new Vector2f(
        Math.round((worldX - centerX) / 4f * Constants.TILE_SIZE * zoomLevel),
        Math.round((worldY - centerY) / 4f * Constants.TILE_SIZE * zoomLevel));
  }

  private Vector2f getPixelPos(float xPos, float yPos) {
    PlayerObj player = world.getPlayer();
    PlayerPosition pos = player.getPos();
    int tileX = pos.getTileX();
    int tileY = pos.getTileY();
    float xDiff = (xPos / 4f) - tileX;
    float yDiff = (yPos / 4f) - tileY;

    return new Vector2f(
        (int) (imageSize / 2f) + (xDiff * Constants.TILE_SIZE) - (Constants.TILE_SIZE / 2f),
        (int) (imageSize / 2f) + (yDiff * Constants.TILE_SIZE) - (Constants.TILE_SIZE / 2f));
  }

  public void addStructure(StructureData structureData) {
    worldLayerRenderer.addStructure(structureData);
    caveLayerRenderer.addStructure(structureData);
  }

  public void removeStructure(StructureData structureData) {
    worldLayerRenderer.removeStructure(structureData);
    caveLayerRenderer.removeStructure(structureData);
  }
  //
  //    private void drawStructure(Graphics2D gfx, StructureData structure) {
  //        int tileX = structure.getTileX() - renderedTileX;
  //        int tileY = structure.getTileY() - renderedTileY;
  //
  //        if (structure instanceof BridgeData) {
  //            MovementChecker movementChecker;
  //            try {
  //                movementChecker = ReflectionUtil.getPrivateField(world.getPlayer(),
  // ReflectionUtil.getField(PlayerObj.class, "movementChecker"));
  //            } catch (IllegalAccessException | NoSuchFieldException e) {
  //                throw new RuntimeException(e);
  //            }
  //
  //            final boolean[] isOverPlayer = {false};
  //            if (structure.getId() != movementChecker.getBridgeId()) {
  //                ((BridgeData) structure).getBridgeParts().forEach((id, bridgePart) -> {
  //                    if (isOverPlayer[0]) return;
  //
  //                    if (bridgePart.getTileX() == renderedTileX && bridgePart.getTileY() ==
  // renderedTileY) {
  //                        isOverPlayer[0] = true;
  //                    }
  //                });
  //            }
  //
  //            ((BridgeData)
  // structure).getBridgeParts().values().stream().sorted(Comparator.comparingInt(bp ->
  // (bp.getTileX() + bp.getTileY()))).forEach((bridgePart) -> drawBridgePart(gfx, bridgePart,
  // isOverPlayer[0]));
  //            return;
  //        } else if (structure instanceof HouseFloorData) {
  //            gfx.setPaint(Color.WHITE);
  //        } else if (structure instanceof HouseRoofData) {
  //            gfx.setPaint(Color.RED);
  //        } else if (structure instanceof FloorData) {
  //            gfx.setPaint(Color.CYAN);
  //        } else if (structure instanceof RoofData) {
  //            gfx.setPaint(Color.PINK);
  //        } else if (structure instanceof MineDoorData) {
  //            gfx.setPaint(Color.BLACK);
  //        } else if (structure instanceof FenceData) {
  //            gfx.setPaint(fenceColors.getOrDefault(((FenceData) structure).getType().material,
  // Color.MAGENTA));
  //
  //            int thickness = (int) Math.floor(tileSize / 4f);
  //            int xEnd = ((FenceData) structure).getTileXEnd();
  //            int yEnd = ((FenceData) structure).getTileYEnd();
  //            if (thickness < 1 || xEnd == tileX && yEnd == tileY) return;
  //
  //            int xSize = Math.max(thickness, tileSize * (xEnd - structure.getTileX()));
  //            int ySize = Math.max(thickness, tileSize * (yEnd - structure.getTileY()));
  //            int xStart = Math.round((tileX + (int) Math.floor(tilesToRender / 2f)) * tileSize);
  //            int yStart = Math.round((tileY + (int) Math.floor(tilesToRender / 2f)) * tileSize);
  //            if (xSize == thickness) xStart -= thickness / 2;
  //            if (ySize == thickness) yStart -= thickness / 2;
  //
  //            if (((FenceData) structure).isGate())
  //                gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
  //
  //            gfx.fillRect(xStart, yStart, xSize, ySize);
  //
  //            if (((FenceData) structure).isGate())
  //                gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
  //
  //            return;
  //        } else if (structure instanceof HouseData) {
  //            try {
  //                Map<Long, HouseFloorData> floorMap = ReflectionUtil.getPrivateField(structure,
  // ReflectionUtil.getField(HouseData.class, "floors"));
  //                Map<Long, HouseWallData> wallMap = ReflectionUtil.getPrivateField(structure,
  // ReflectionUtil.getField(HouseData.class, "walls"));
  //                Map<Long, HouseRoofData> roofMap = ReflectionUtil.getPrivateField(structure,
  // ReflectionUtil.getField(HouseData.class, "roofs"));
  //                final boolean[] isPlayerInHouse = {false};
  //                floorMap.forEach((id, floor) -> {
  //                    if (isPlayerInHouse[0]) return;
  //
  //                    if (floor.getTileX() == renderedTileX && floor.getTileY() == renderedTileY)
  //                        isPlayerInHouse[0] = true;
  //                });
  //
  //                if (!isPlayerInHouse[0]) {
  //                    roofMap.forEach((id, roof) -> {
  //                        if (isPlayerInHouse[0]) return;
  //
  //                        if (roof.getTileX() == renderedTileX && roof.getTileY() ==
  // renderedTileY)
  //                            isPlayerInHouse[0] = true;
  //                    });
  //                }
  //
  //                Stream<HouseFloorData> floors =
  // floorMap.values().stream().sorted(Comparator.comparingInt(StructureData::getLayer));
  //                Stream<HouseWallData> walls =
  // wallMap.values().stream().sorted(Comparator.comparingInt(StructureData::getLayer));
  //                Stream<HouseRoofData> roofs =
  // roofMap.values().stream().sorted(Comparator.comparingInt(StructureData::getLayer));
  //
  //                if (isPlayerInHouse[0]) {
  //                    float playerPosH = world.getPlayerPosH();
  //                    floors = floors.filter((floor) -> floor.getHPos() <= playerPosH);
  //                    walls = walls.filter((wall) -> wall.getHPos() <= playerPosH);
  //                    roofs = roofs.filter((roof) -> roof.getHPos() <= playerPosH);
  //                }
  //
  //                ArrayList<ArrayList<HouseFloorData>> layeredFloors = new ArrayList<>();
  //                ArrayList<ArrayList<HouseWallData>> layeredWalls = new ArrayList<>();
  //                ArrayList<ArrayList<HouseRoofData>> layeredRoofs = new ArrayList<>();
  //
  //                floors.forEach((floor) -> {
  //                    int layerInd = floor.getHeightOffset() / 30;
  //                    while (layerInd >= layeredFloors.size()) layeredFloors.add(new
  // ArrayList<>());
  //                    ArrayList<HouseFloorData> layer = layeredFloors.get(layerInd);
  //
  //                    layer.add(floor);
  //                });
  //
  //                walls.forEach((wall) -> {
  //                    int layerInd = wall.getHeightOffset() / 30;
  //                    while (layerInd >= layeredWalls.size()) layeredWalls.add(new ArrayList<>());
  //                    ArrayList<HouseWallData> layer = layeredWalls.get(layerInd);
  //
  //                    layer.add(wall);
  //                });
  //
  //                roofs.forEach((roof) -> {
  //                    int layerInd = roof.getHeightOffset() / 30;
  //                    while (layerInd >= layeredRoofs.size()) layeredRoofs.add(new ArrayList<>());
  //                    ArrayList<HouseRoofData> layer = layeredRoofs.get(layerInd);
  //
  //                    layer.add(roof);
  //                });
  //
  //                for (int i = 0; i < Math.max(Math.max(layeredFloors.size(),
  // layeredWalls.size()), layeredRoofs.size()); i++) {
  //                    if (layeredFloors.size() > i) layeredFloors.get(i).forEach((floor) ->
  // renderHouseFloor(gfx, floor));
  //                    if (layeredWalls.size() > i) layeredWalls.get(i).forEach((wall) ->
  // renderHouseWall(gfx, wall));
  //                    if (layeredRoofs.size() > i) layeredRoofs.get(i).forEach((roof) ->
  // renderHouseRoof(gfx, roof));
  //                }
  //
  //                return;
  //            } catch (IllegalAccessException | NoSuchFieldException e) {
  //                throw new RuntimeException(e);
  //            }
  //        } else {
  //            System.out.println("structure: " + structure.getClass().getName() + ", tileX: " +
  // structure.getTileX() + ", tileY: " + structure.getTileY());
  //            return;
  //        }
  //
  //        if (Math.min(tileX, tileY) < (int) -Math.floor(tilesToRender / 2f) - 1
  //                || Math.max(tileX, tileY) > (int) Math.floor(tilesToRender / 2f)) return;
  //
  //        gfx.drawRect(
  //                Math.round((tileX + (int) Math.floor(tilesToRender / 2f)) * tileSize),
  //                Math.round((tileY + (int) Math.floor(tilesToRender / 2f)) * tileSize),
  //                tileSize,
  //                tileSize
  //        );
  //
  ////            gfx.drawImage(
  ////                    missingImage,
  ////                    Math.round((structureTileX + (int) Math.floor(tilesToRender / 2f)) *
  // tileSize),
  ////                    Math.round((structureTileY + (int) Math.floor(tilesToRender / 2f)) *
  // tileSize),
  ////                    tileSize,
  ////                    tileSize,
  ////                    null
  ////            );
  //    }
  //
  //    private void renderHouseRoof(Graphics2D gfx, HouseRoofData houseRoof) {
  //        int tileX = houseRoof.getTileX() - renderedTileX;
  //        int tileY = houseRoof.getTileY() - renderedTileY;
  //
  //        BufferedImage image = houseMaterialImages.getOrDefault(houseRoof.getMaterial(),
  // missingImage);
  //        gfx.drawImage(
  //                image,
  //                Math.round((tileX + (int) Math.floor(tilesToRender / 2f)) * tileSize),
  //                Math.round((tileY + (int) Math.floor(tilesToRender / 2f)) * tileSize),
  //                tileSize,
  //                tileSize,
  //                null
  //        );
  //    }
  //
  //    private void renderHouseWall(Graphics2D gfx, HouseWallData houseWall) {
  //        int tileX = houseWall.getTileX() - renderedTileX;
  //        int tileY = houseWall.getTileY() - renderedTileY;
  //
  //        gfx.setPaint(fenceColors.getOrDefault(houseWall.getType().material, Color.MAGENTA));
  //
  //        int thickness = (int) Math.floor(tileSize / 4f);
  //        int xEnd = houseWall.getTileXEnd();
  //        int yEnd = houseWall.getTileYEnd();
  //        if (thickness < 1 || xEnd == tileX && yEnd == tileY) return;
  //
  //        int xSize = Math.max(thickness, tileSize * (xEnd - houseWall.getTileX()));
  //        int ySize = Math.max(thickness, tileSize * (yEnd - houseWall.getTileY()));
  //        int xStart = Math.round((tileX + (int) Math.floor(tilesToRender / 2f)) * tileSize);
  //        int yStart = Math.round((tileY + (int) Math.floor(tilesToRender / 2f)) * tileSize);
  //        if (xSize == thickness) xStart -= thickness / 2;
  //        if (ySize == thickness) yStart -= thickness / 2;
  //
  //        if (houseWall.isGate())
  //            gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
  //
  //        gfx.fillRect(xStart, yStart, xSize, ySize);
  //
  //        if (houseWall.isGate())
  //            gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
  //    }
  //
  //    private void renderHouseFloor(Graphics2D gfx, HouseFloorData houseFloor) {
  //        int tileX = houseFloor.getTileX() - renderedTileX;
  //        int tileY = houseFloor.getTileY() - renderedTileY;
  //
  //        BufferedImage image = houseMaterialImages.getOrDefault(houseFloor.getMaterial(),
  // missingImage);
  //        gfx.drawImage(
  //                image,
  //                Math.round((tileX + (int) Math.floor(tilesToRender / 2f)) * tileSize),
  //                Math.round((tileY + (int) Math.floor(tilesToRender / 2f)) * tileSize),
  //                tileSize,
  //                tileSize,
  //                null
  //        );
  //    }
  //
  //    private void drawBridgePart(Graphics2D gfx, BridgePartData bridgePart, boolean isOverPlayer)
  // {
  //        int tileX = bridgePart.getTileX() - renderedTileX;
  //        int tileY = bridgePart.getTileY() - renderedTileY;
  //
  //        if (isOverPlayer)
  //            gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
  //
  //        BufferedImage image = bridgeImages.getOrDefault(bridgePart.getMaterial(), missingImage);
  //        gfx.drawImage(
  //                image,
  //                Math.round((tileX + (int) Math.floor(tilesToRender / 2f)) * tileSize),
  //                Math.round((tileY + (int) Math.floor(tilesToRender / 2f)) * tileSize),
  //                tileSize,
  //                tileSize,
  //                null
  //        );
  //
  //        int shadowThickness = (int) Math.floor(tileSize / 8f);
  //        if (shadowThickness >= 1) {
  //            boolean horizontal = bridgePart.getDir() / 2 % 2 == 1;
  //            for (int i = 0; i < shadowThickness; i++) {
  //                gfx.setPaint(new Color(0, 0, 0, (0.5f / shadowThickness) * (shadowThickness -
  // i)));
  //                gfx.fillRect(
  //                        Math.round((tileX + (horizontal ? 0 : 1) + (int)
  // Math.floor(tilesToRender / 2f)) * tileSize + (horizontal ? 0 : i)),
  //                        Math.round((tileY + (horizontal ? 1 : 0) + (int)
  // Math.floor(tilesToRender / 2f)) * tileSize + (horizontal ? i : 0)),
  //                        horizontal ? tileSize : 1,
  //                        horizontal ? 1 : tileSize
  //                );
  //            }
  //        }
  //
  //        if (isOverPlayer)
  //            gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
  //    }
}

// TODO: perf test it, try out dweia's deed switfwood
