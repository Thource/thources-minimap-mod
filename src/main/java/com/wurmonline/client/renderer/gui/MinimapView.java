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
import dev.thource.wurmunlimited.clientmods.minimap.Settings;
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
import lombok.Setter;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

public class MinimapView extends FlexComponent {

  private final World world;
  private final WorldLayerRenderer worldLayerRenderer;
  private final CaveLayerRenderer caveLayerRenderer;
  private final Method preprocessImage;
  // TODO: move these somewhere configurable
  boolean isNorthFacing = false;
  int imageSize = Settings.getTileSize() * 302;
  private ImageTexture texture;

  private float zoomLevel = 24f / Settings.getTileSize();
  @Setter private int actualWidth = width;
  @Setter private int actualHeight = height;

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
    zoomLevel =
        (float)
            Math.min(
                Math.max(
                    zoomLevel * Math.pow(1.1f, -wheelDelta / 3f),
                    0.1f * (24f / Settings.getTileSize())),
                2 * (24f / Settings.getTileSize()));
  }

  protected void renderComponent(Queue queue, float alpha) {
    super.renderComponent(queue, alpha);

    PlayerObj player = world.getPlayer();
    PlayerPosition pos = player.getPos();
    int tileX = pos.getTileX();
    int tileY = pos.getTileY();

    WorldRender worldRenderer = world.getWorldRenderer();
    BufferedImage framedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
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
      framedGfx.rotate(-cameraRotX, actualWidth / 2f, actualHeight / 2f);
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
        (int) layerPos.x + actualWidth / 2,
        (int) layerPos.y + actualHeight / 2,
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
              + actualWidth / 2
              - (int) Math.floor(RenderedFence.PADDING / 2f * zoomLevel),
          (int) fencePixelPos.y
              + actualHeight / 2
              - (int) Math.floor(RenderedFence.PADDING / 2f * zoomLevel),
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
          (int) bridgePixelPos.x + actualWidth / 2,
          (int) bridgePixelPos.y + actualHeight / 2,
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
            (int) housePixelPos.x
                + actualWidth / 2
                - (int) Math.floor(RenderedHouse.PADDING / 2f * zoomLevel),
            (int) housePixelPos.y
                + actualHeight / 2
                - (int) Math.floor(RenderedHouse.PADDING / 2f * zoomLevel),
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
                            (int) creaturePixelPos.x - icon.getWidth() / 2 + actualWidth / 2,
                            (int) creaturePixelPos.y - icon.getHeight() / 2 + actualHeight / 2,
                            null);
                      });
            });

    framedGfx.rotate(cameraRotX, actualWidth / 2f, actualHeight / 2f);
    framedGfx.drawImage(
        ImageManager.playerCursorImage,
        actualWidth / 2 - ImageManager.playerCursorImage.getWidth() / 2,
        actualHeight / 2 - ImageManager.playerCursorImage.getHeight() / 2,
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
        queue, this.texture, 1.0F, 1.0F, 1.0F, 1.0F, this.x, this.y, width, height, 0, 0, 1, 1);
  }

  private BufferedImage scaleImage(BufferedImage image, float scale) {
    if (scale == 1) {
      return image;
    }

    int scaledWidth = Math.round(image.getWidth() * scale);
    int scaledHeight = Math.round(image.getHeight() * scale);
    BufferedImage scalingImage =
        new BufferedImage(
            Math.max(image.getWidth(), scaledWidth),
            Math.max(image.getHeight(), scaledHeight),
            image.getType());
    Graphics2D graphics = scalingImage.createGraphics();
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    if (scale > 1) {
      graphics.drawImage(image, 0, 0, scaledWidth, scaledHeight, null);
      graphics.dispose();
      return scalingImage;
    }

    graphics.drawImage(image, 0, 0, null);
    int lastWidth = image.getWidth();
    int lastHeight = image.getHeight();
    while (lastWidth > scaledWidth && lastHeight > scaledHeight) {
      int nextWidth = Math.max(scaledWidth, lastWidth / 2);
      int nextHeight = Math.max(scaledHeight, lastHeight / 2);

      graphics.drawImage(
          scalingImage.getSubimage(0, 0, lastWidth, lastHeight), 0, 0, nextWidth, nextHeight, null);

      lastWidth = nextWidth;
      lastHeight = nextHeight;
    }
    graphics.dispose();

    return scalingImage.getSubimage(0, 0, scaledWidth, scaledHeight);
  }

  private Vector2f worldPosToPixelPos(float worldX, float worldY) {
    return worldPosToPixelPos(worldX, worldY, zoomLevel);
  }

  private Vector2f worldPosToPixelPos(float worldX, float worldY, float zoomLevel) {
    PlayerObj player = world.getPlayer();
    PlayerPosition pos = player.getPos();
    float centerX = pos.getX();
    float centerY = pos.getY();

    return new Vector2f(
        (float) Math.floor((worldX - centerX) / 4f * Settings.getTileSize() * zoomLevel),
        (float) Math.floor((worldY - centerY) / 4f * Settings.getTileSize() * zoomLevel));
  }

  public void addStructure(StructureData structureData) {
    worldLayerRenderer.addStructure(structureData);
    caveLayerRenderer.addStructure(structureData);
  }

  public void removeStructure(StructureData structureData) {
    worldLayerRenderer.removeStructure(structureData);
    caveLayerRenderer.removeStructure(structureData);
  }

  private BufferedImage createDumpImage(LayerRenderer layerRenderer) {
    PlayerObj player = world.getPlayer();
    PlayerPosition pos = player.getPos();
    int tileX = pos.getTileX();
    int tileY = pos.getTileY();

    BufferedImage dumpImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = dumpImage.createGraphics();
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

    BufferedImage layerImage = layerRenderer.getTileRenderer().getImage();
    graphics.drawImage(layerImage, 0, 0, null);

    MovementChecker movementChecker;
    try {
      movementChecker =
          ReflectionUtil.getPrivateField(
              world.getPlayer(), ReflectionUtil.getField(PlayerObj.class, "movementChecker"));
    } catch (IllegalAccessException | NoSuchFieldException e) {
      throw new RuntimeException(e);
    }

    for (RenderedFence fence : layerRenderer.getFences().values()) {
      Vector2f fencePixelPos =
          worldPosToPixelPos(
              fence.getTileX() * 4f + (pos.getX() - tileX * 4),
              fence.getTileY() * 4f + (pos.getY() - tileY * 4),
              1);
      graphics.drawImage(
          fence.getImage(),
          (int) fencePixelPos.x + imageSize / 2 - (int) Math.floor(RenderedFence.PADDING / 2f),
          (int) fencePixelPos.y + imageSize / 2 - (int) Math.floor(RenderedFence.PADDING / 2f),
          null);
    }

    for (RenderedBridge bridge : layerRenderer.getBridges().values()) {
      boolean playerUnderBridge =
          movementChecker.getBridgeId() != bridge.getId() && bridge.isBridgeTile(tileX, tileY);

      if (playerUnderBridge) {
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
      }

      Vector2f bridgePixelPos =
          worldPosToPixelPos(
              bridge.getTileX() * 4f + (pos.getX() - tileX * 4),
              bridge.getTileY() * 4f + (pos.getY() - tileY * 4),
              1);
      graphics.drawImage(
          bridge.getImage(),
          (int) bridgePixelPos.x + imageSize / 2,
          (int) bridgePixelPos.y + imageSize / 2,
          null);

      if (playerUnderBridge) {
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
      }
    }

    for (RenderedHouse house : layerRenderer.getHouses().values()) {
      BufferedImage houseImage = house.getLevelImage(tileX, tileY, (int) (pos.getH() * 10));

      if (houseImage != null) {
        Vector2f housePixelPos =
            worldPosToPixelPos(
                house.getTileX() * 4f + (pos.getX() - tileX * 4),
                house.getTileY() * 4f + (pos.getY() - tileY * 4),
                1);
        graphics.drawImage(
            houseImage,
            (int) housePixelPos.x + imageSize / 2 - (int) Math.floor(RenderedHouse.PADDING / 2f),
            (int) housePixelPos.y + imageSize / 2 - (int) Math.floor(RenderedHouse.PADDING / 2f),
            null);
      }
    }

    graphics.dispose();
    return dumpImage;
  }

  public void dump() {
    new Thread(
            () -> {
              File outputFile =
                  new File("minimap-dumps/" + System.currentTimeMillis() + "-world-render.png");
              //noinspection ResultOfMethodCallIgnored
              outputFile.mkdirs();
              try {
                ImageIO.write(createDumpImage(worldLayerRenderer), "png", outputFile);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
        .start();

    new Thread(
            () -> {
              File outputFile =
                  new File("minimap-dumps/" + System.currentTimeMillis() + "-cave-render.png");
              //noinspection ResultOfMethodCallIgnored
              outputFile.mkdirs();
              try {
                ImageIO.write(createDumpImage(caveLayerRenderer), "png", outputFile);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
        .start();
  }

  public void rerender() {
    new Thread(
            () ->
                worldLayerRenderer
                    .getTileRenderer()
                    .setDirty(
                        worldLayerRenderer.getTileRenderer().getCenterX() - 151,
                        worldLayerRenderer.getTileRenderer().getCenterY() - 151,
                        worldLayerRenderer.getTileRenderer().getCenterX() + 151,
                        worldLayerRenderer.getTileRenderer().getCenterY() + 151))
        .start();
    new Thread(
            () ->
                caveLayerRenderer
                    .getTileRenderer()
                    .setDirty(
                        caveLayerRenderer.getTileRenderer().getCenterX() - 151,
                        caveLayerRenderer.getTileRenderer().getCenterY() - 151,
                        caveLayerRenderer.getTileRenderer().getCenterX() + 151,
                        caveLayerRenderer.getTileRenderer().getCenterY() + 151))
        .start();
  }
}

// TODO: perf test it, try out dweia's deed switfwood
