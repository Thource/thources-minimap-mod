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
import dev.thource.wurmunlimited.clientmods.minimap.renderer.component.RenderedHouse;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.imageio.ImageIO;
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

  protected void renderComponent(Queue queue, float alpha) {
    super.renderComponent(queue, alpha);

    PlayerObj player = world.getPlayer();
    PlayerPosition pos = player.getPos();
    int tileX = pos.getTileX();
    int tileY = pos.getTileY();

    WorldRender worldRenderer = world.getWorldRenderer();
    BufferedImage framedImg = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
    Graphics2D framedGfx = framedImg.createGraphics();
    framedGfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    framedGfx.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
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
    LayerRenderer layerRenderer =
        world.getPlayerLayer() == -1 ? caveLayerRenderer : worldLayerRenderer;
    BufferedImage layerImage = layerRenderer.getTileRenderer().getImage();
    Vector2f layerPos =
        worldPosToPixelPos(
            (layerRenderer.getTileRenderer().getCenterX() - 151) * 4f,
            (layerRenderer.getTileRenderer().getCenterY() - 151) * 4f);
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
          (int) fencePixelPos.x + 128 - (int) Math.floor(RenderedFence.PADDING / 2f * zoomLevel),
          (int) fencePixelPos.y + 128 - (int) Math.floor(RenderedFence.PADDING / 2f * zoomLevel),
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

    for (RenderedHouse house : layerRenderer.getHouses().values()) {
      BufferedImage houseImage = house.getLevelImage(tileX, tileY, (int) (pos.getH() * 10));

      if (houseImage != null) {
        Vector2f housePixelPos = worldPosToPixelPos(house.getTileX() * 4f, house.getTileY() * 4f);
        framedGfx.drawImage(
            houseImage,
            (int) housePixelPos.x + 128 - (int) Math.floor(RenderedHouse.PADDING / 2f * zoomLevel),
            (int) housePixelPos.y + 128 - (int) Math.floor(RenderedHouse.PADDING / 2f * zoomLevel),
            Math.round(house.getImage().getWidth() * zoomLevel),
            Math.round(house.getImage().getHeight() * zoomLevel),
            null);
      }
    }

    world
        .getServerConnection()
        .getServerConnectionListener()
        .getCreatures()
        .forEach(
            (id, creature) -> {
              ImageManager.getIconForCreature(creature)
                  .ifPresent(
                      icon -> {
                        Vector2f creaturePixelPos =
                            worldPosToPixelPos(creature.getXPos(), creature.getYPos());
                        framedGfx.drawImage(
                            icon,
                            (int) creaturePixelPos.x - icon.getWidth() / 2 + 128,
                            (int) creaturePixelPos.y - icon.getHeight() / 2 + 128,
                            null);
                      });
            });

    framedGfx.rotate(cameraRotX, 128, 128);
    framedGfx.drawImage(
        ImageManager.playerCursorImage,
        128 - ImageManager.playerCursorImage.getWidth() / 2,
        128 - ImageManager.playerCursorImage.getHeight() / 2,
        null);

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
        (float) Math.floor((worldX - centerX) / 4f * Constants.TILE_SIZE * zoomLevel),
        (float) Math.floor((worldY - centerY) / 4f * Constants.TILE_SIZE * zoomLevel));
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

  public void dump() {
    new Thread(
            () -> {
              File outputFile =
                  new File("tests/" + System.currentTimeMillis() + "-world-render.png");
              try {
                ImageIO.write(worldLayerRenderer.getTileRenderer().getImage(), "png", outputFile);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
        .start();

    new Thread(
            () -> {
              File outputFile =
                  new File("tests/" + System.currentTimeMillis() + "-cave-render.png");
              try {
                ImageIO.write(caveLayerRenderer.getTileRenderer().getImage(), "png", outputFile);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
        .start();
  }
}

// TODO: perf test it, try out dweia's deed switfwood
