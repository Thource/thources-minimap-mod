//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.wurmonline.client.renderer.gui;

import com.wurmonline.client.game.NearTerrainDataBuffer;
import com.wurmonline.client.game.PlayerObj;
import com.wurmonline.client.game.PlayerPosition;
import com.wurmonline.client.game.World;
import com.wurmonline.client.renderer.WorldRender;
import com.wurmonline.client.renderer.backend.Queue;
import com.wurmonline.client.renderer.cell.CreatureCellRenderable;
import com.wurmonline.client.renderer.cell.PlayerCellRenderable;
import com.wurmonline.client.resources.textures.ImageTexture;
import com.wurmonline.client.resources.textures.ImageTextureLoader;
import com.wurmonline.client.resources.textures.PreProcessedTextureData;
import com.wurmonline.client.resources.textures.TextureLoader;
import com.wurmonline.math.Vector2f;
import com.wurmonline.mesh.Tiles;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MinimapView extends FlexComponent {
    private final Map<Tiles.Tile, BufferedImage> tileImages = initTileImages();
    private final BufferedImage missingImage = createMissingImage();
    private final BufferedImage waterImage;
    private final BufferedImage playerCursorImage;
    private final BufferedImage neutralIcon;
    private final BufferedImage friendIcon;
    private final BufferedImage allyIcon;
    private final BufferedImage hostileIcon;
    private final BufferedImage neutralPlayerIcon;
    private final BufferedImage friendPlayerIcon;
    private final BufferedImage allyPlayerIcon;
    private final BufferedImage hostilePlayerIcon;
    private final BufferedImage gmPlayerIcon;
    private final BufferedImage devPlayerIcon;

    {
        try {
            waterImage = ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream("/textures/water.png")));
            playerCursorImage = ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream("/sprites/player-arrow.png")));
            neutralIcon = ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream("/sprites/icon-neutral.png")));
            friendIcon = ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream("/sprites/icon-friend.png")));
            allyIcon = ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream("/sprites/icon-ally.png")));
            hostileIcon = ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream("/sprites/icon-hostile.png")));
            neutralPlayerIcon = ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream("/sprites/icon-player-neutral.png")));
            friendPlayerIcon = ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream("/sprites/icon-player-friend.png")));
            allyPlayerIcon = ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream("/sprites/icon-player-ally.png")));
            hostilePlayerIcon = ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream("/sprites/icon-player-hostile.png")));
            gmPlayerIcon = ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream("/sprites/icon-player-gm.png")));
            devPlayerIcon = ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream("/sprites/icon-player-dev.png")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final World world;

    private BufferedImage renderedImage;
    private boolean isDirty = false;
    private int renderedTileX = -1;
    private int renderedTileY = -1;

    // TODO: move these somewhere configurable
    int tileSize = 16;
    boolean isNorthFacing = false;
    // 362 is the result of Math.sqrt(256^2 + 256^2), it's the size the square needs to be for there to not be a gap when rotated
    int tilesToRender = (int) Math.ceil(362f / tileSize);

    {
        if (tilesToRender % 2 == 0) tilesToRender++; // if even, make it odd
    }

    int imageSize = tilesToRender * tileSize;

    private Map<Tiles.Tile, BufferedImage> initTileImages() {
        Map<Tiles.Tile, BufferedImage> map = new HashMap<>();
        try {
            map.put(Tiles.Tile.TILE_DIRT, ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream("/textures/dirt.png"))));
            map.put(Tiles.Tile.TILE_GRASS, ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream("/textures/grass.png"))));
            map.put(Tiles.Tile.TILE_FIELD, ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream("/textures/farm.png"))));
            map.put(Tiles.Tile.TILE_FIELD2, ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream("/textures/farm.png"))));
            map.put(Tiles.Tile.TILE_COBBLESTONE, ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream("/textures/cobble.png"))));
            map.put(Tiles.Tile.TILE_COBBLESTONE_ROUND, ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream("/textures/cobble3.png"))));
            map.put(Tiles.Tile.TILE_ROCK, ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream("/textures/rock.png"))));
            map.put(Tiles.Tile.TILE_GRAVEL, ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream("/textures/gravel.png"))));
            map.put(Tiles.Tile.TILE_DIRT_PACKED, ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream("/textures/packed.png"))));
            map.put(Tiles.Tile.TILE_LAWN, ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream("/textures/lawn.png"))));
            map.put(Tiles.Tile.TILE_STONE_SLABS, ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream("/textures/slab.png"))));
            map.put(Tiles.Tile.TILE_PLANKS, ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream("/textures/planks.png"))));
            map.put(Tiles.Tile.TILE_MARBLE_SLABS, ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream("/textures/marbleslab.png"))));
            map.put(Tiles.Tile.TILE_SAND, ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream("/textures/sand.png"))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Collections.unmodifiableMap(map);
    }

    private BufferedImage createMissingImage() {
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        Graphics2D gfx = image.createGraphics();
        gfx.setBackground(Color.MAGENTA);
        gfx.clearRect(0, 0, 32, 32);
        gfx.dispose();

        return image;
    }

    private final Method preprocessImage;
    private ImageTexture texture;

    MinimapView(String name, int width, int height) {
        super(name);
        this.setInitialSize(width, height, false);
        this.sizeFlags = 3;
        try {
            this.world = ReflectionUtil.getPrivateField(hud, ReflectionUtil.getField(hud.getClass(), "world"));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        try {
            this.preprocessImage = ReflectionUtil.getMethod(
                    TextureLoader.class,
                    "preprocessImage",
                    new Class[]{BufferedImage.class, Boolean.TYPE}
            );
        } catch (NoSuchMethodException var4) {
            throw new RuntimeException(var4);
        }
    }

    private BufferedImage cloneImage(BufferedImage original) {
        BufferedImage clone = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
        Graphics2D gfx = clone.createGraphics();
        gfx.drawImage(original, null, 0, 0);
        gfx.dispose();

        return clone;
    }

    private BufferedImage getImageForTile(int x, int y) {
        NearTerrainDataBuffer nearTerrainBuffer = world.getNearTerrainBuffer();

        Tiles.Tile tileType = nearTerrainBuffer.getTileType(x, y);
        if (tileType.isTree() || tileType.isBush())
            tileType = Tiles.Tile.TILE_GRASS;

        BufferedImage tileImage = tileImages.getOrDefault(tileType, missingImage);
        if (tileType.isRoad()) {
            int roadDir = nearTerrainBuffer.getData(x, y) & 7;
            Tiles.TileRoadDirection roadDirection = Tiles.TileRoadDirection.DIR_STRAIGHT;
            if (roadDir == 1) {
                roadDirection = Tiles.TileRoadDirection.DIR_NW;
            } else if (roadDir == 2) {
                roadDirection = Tiles.TileRoadDirection.DIR_NE;
            } else if (roadDir == 3) {
                roadDirection = Tiles.TileRoadDirection.DIR_SE;
            } else if (roadDir == 4) {
                roadDirection = Tiles.TileRoadDirection.DIR_SW;
            }

            if (roadDirection != Tiles.TileRoadDirection.DIR_STRAIGHT) {
                tileImage = cloneImage(tileImage);

                BufferedImage northTileImage = null;
                BufferedImage eastTileImage = null;
                BufferedImage southTileImage = null;
                BufferedImage westTileImage = null;
                if (roadDirection == Tiles.TileRoadDirection.DIR_SE || roadDirection == Tiles.TileRoadDirection.DIR_SW) {
                    northTileImage = tileImages.getOrDefault(nearTerrainBuffer.getSecondaryType(x, y - 1), missingImage);
                }
                if (roadDirection == Tiles.TileRoadDirection.DIR_NW || roadDirection == Tiles.TileRoadDirection.DIR_SW) {
                    eastTileImage = tileImages.getOrDefault(nearTerrainBuffer.getSecondaryType(x + 1, y), missingImage);
                }
                if (roadDirection == Tiles.TileRoadDirection.DIR_NE || roadDirection == Tiles.TileRoadDirection.DIR_NW) {
                    southTileImage = tileImages.getOrDefault(nearTerrainBuffer.getSecondaryType(x, y + 1), missingImage);
                }
                if (roadDirection == Tiles.TileRoadDirection.DIR_SE || roadDirection == Tiles.TileRoadDirection.DIR_NE) {
                    westTileImage = tileImages.getOrDefault(nearTerrainBuffer.getSecondaryType(x - 1, y), missingImage);
                }

                for (int py = 0; py < 32; py++) {
                    for (int px = 0; px < 32; px++) {
                        if ((roadDirection == Tiles.TileRoadDirection.DIR_SE || roadDirection == Tiles.TileRoadDirection.DIR_SW) && py <= 15 && px - py >= 0 && px + py <= 31) {
                            tileImage.setRGB(px, py, northTileImage.getRGB(px, py));
                        }
                        if ((roadDirection == Tiles.TileRoadDirection.DIR_NW || roadDirection == Tiles.TileRoadDirection.DIR_SW) && px >= 16 && py - (31 - px) >= 0 && py + (31 - px) <= 31) {
                            tileImage.setRGB(px, py, eastTileImage.getRGB(px, py));
                        }
                        if ((roadDirection == Tiles.TileRoadDirection.DIR_NE || roadDirection == Tiles.TileRoadDirection.DIR_NW) && py >= 16 && px - (31 - py) >= 0 && px + (31 - py) <= 31) {
                            tileImage.setRGB(px, py, southTileImage.getRGB(px, py));
                        }
                        if ((roadDirection == Tiles.TileRoadDirection.DIR_NE || roadDirection == Tiles.TileRoadDirection.DIR_SE) && px <= 15 && py - px >= 0 && py + px <= 31) {
                            tileImage.setRGB(px, py, westTileImage.getRGB(px, py));
                        }
                    }
                }
            }
        }

        return tileImage;
    }

    protected void renderComponent(Queue queue, float alpha) {
        super.renderComponent(queue, alpha);

        PlayerObj player = this.world.getPlayer();
        PlayerPosition pos = player.getPos();
        int tileX = pos.getTileX();
        int tileY = pos.getTileY();
        float xOffset = 1f - ((pos.getX() / 4.0f) - tileX);
        float yOffset = 1f - ((pos.getY() / 4.0f) - tileY);

        if (renderedTileX != tileX || renderedTileY != tileY) isDirty = true;
        if (isDirty) renderImage(tileX, tileY);

        WorldRender worldRenderer = this.world.getWorldRenderer();
        BufferedImage framedImg = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        Graphics2D framedGfx = framedImg.createGraphics();
        framedGfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        float cameraRotX;
        try {
            cameraRotX = ReflectionUtil.getPrivateField(worldRenderer, ReflectionUtil.getField(worldRenderer.getClass(), "cameraRotX"));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        if (!isNorthFacing)
            framedGfx.rotate(-cameraRotX, 128, 128);
        int imageTileOffset = -(imageSize - 256) / 2;
        framedGfx.drawImage(renderedImage, null, imageTileOffset - (tileSize / 2) + Math.round(tileSize * xOffset), imageTileOffset - (tileSize / 2) + Math.round(tileSize * yOffset));

        BufferedImage iconOverlay = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D iconOverlayGfx = iconOverlay.createGraphics();

        Vector2f playerPixelPos = getPixelPos(world.getPlayerPosX(), world.getPlayerPosY());
        iconOverlayGfx.rotate(cameraRotX, (int) playerPixelPos.x, (int) playerPixelPos.y);
        iconOverlayGfx.drawImage(playerCursorImage, null, (int) playerPixelPos.x - 5, (int) playerPixelPos.y - 7);
        iconOverlayGfx.rotate(-cameraRotX, (int) playerPixelPos.x, (int) playerPixelPos.y);

        world.getServerConnection().getServerConnectionListener().getCreatures().forEach((id, creature) -> {
            BufferedImage icon = getIconForCreature(creature);
            Vector2f creaturePixelPos = getPixelPos(creature.getXPos(), creature.getYPos());
            iconOverlayGfx.drawImage(icon, null, (int) creaturePixelPos.x - 4, (int) creaturePixelPos.y - 4);
        });
        iconOverlayGfx.dispose();

        framedGfx.drawImage(iconOverlay, null, imageTileOffset - (tileSize / 2) + Math.round(tileSize * xOffset), imageTileOffset - (tileSize / 2) + Math.round(tileSize * yOffset));
        framedGfx.dispose();

        if (this.texture == null) {
            this.texture = ImageTextureLoader.loadNowrapNearestTexture(framedImg, false);
        } else {
            try {
                PreProcessedTextureData data = ReflectionUtil.callPrivateMethod(TextureLoader.class, this.preprocessImage, new Object[]{framedImg, true});
                this.texture.deferInit(data, TextureLoader.Filter.NEAREST, false, false, false);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException var5) {
                throw new RuntimeException(var5);
            }
        }

        Renderer.texturedQuadAlphaBlend(queue, this.texture, 1.0F, 1.0F, 1.0F, 1.0F, (float) this.x, (float) this.y, (float) 256, (float) 256, 0, 0, 1, 1);
    }

    private Vector2f getPixelPos(float xPos, float yPos) {
        float xDiff = (xPos / 4f) - renderedTileX;
        float yDiff = (yPos / 4f) - renderedTileY;

        return new Vector2f((int) (imageSize / 2f) + (xDiff * tileSize) - (tileSize / 2f), (int) (imageSize / 2f) + (yDiff * tileSize) - (tileSize / 2f));
    }

    private BufferedImage getIconForCreature(CreatureCellRenderable creature) {
        // TODO: check if creature is a cart, chair, etc and return null

        int attitude;
        try {
            attitude = ReflectionUtil.getPrivateField(creature, ReflectionUtil.getField(creature.getClass(), "attitude"));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        boolean isPlayer = creature instanceof PlayerCellRenderable;
        String status = "neutral";
        switch (attitude) {
            case 1:
            case 5:
                status = "ally";
                break;
            case 2:
            case 4:
                status = "hostile";
                break;
            case 3:
                status = "gm";
                break;
            case 6:
                status = "dev";
                break;
            case 7:
                status = "friend";
                break;
        }

        if (isPlayer) {
            switch (status) {
                case "neutral":
                    return neutralPlayerIcon;
                case "friend":
                    return friendPlayerIcon;
                case "hostile":
                    return hostilePlayerIcon;
                case "ally":
                    return allyPlayerIcon;
                case "gm":
                    return gmPlayerIcon;
                case "dev":
                    return devPlayerIcon;
            }
        }

        switch (status) {
            case "neutral":
            case "gm":
            case "dev":
                return neutralIcon;
            case "friend":
                return friendIcon;
            case "hostile":
                return hostileIcon;
            case "ally":
                return allyIcon;
        }

        // should never be hit
        throw new RuntimeException("No icon, attitude: " + attitude + ", isPlayer: " + isPlayer);
    }

    private void renderImage(int tileX, int tileY) {
        renderedImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D gfx = renderedImage.createGraphics();

        for (int x = (int) -Math.floor(tilesToRender / 2f); x <= (int) Math.floor(tilesToRender / 2f); x++) {
            for (int y = (int) -Math.floor(tilesToRender / 2f); y <= (int) Math.floor(tilesToRender / 2f); y++) {
                gfx.drawImage(
                        getImageForTile(tileX + x, tileY + y),
                        Math.round((x + (int) Math.floor(tilesToRender / 2f)) * tileSize),
                        Math.round((y + (int) Math.floor(tilesToRender / 2f)) * tileSize),
                        tileSize,
                        tileSize,
                        null
                );
            }
        }

        NearTerrainDataBuffer nearTerrainBuffer = world.getNearTerrainBuffer();
        float waterHeight = nearTerrainBuffer.getWaterHeight(x, y);
        for (int py = 0; py < imageSize; py++) {
            for (int px = 0; px < imageSize; px++) {
                int pTileX = tileX - (tilesToRender / 2);
                float worldX = (pTileX + (px / (float) (tileSize - 1))) * 4f;
                int pTileY = tileY - (tilesToRender / 2);
                float worldY = (pTileY + (py / (float) (tileSize - 1))) * 4f;

                float pointHeight = nearTerrainBuffer.getInterpolatedHeight(worldX, worldY);
                if (waterHeight >= pointHeight)
                    renderedImage.setRGB(px, py, waterImage.getRGB((px % tileSize) * (32 / tileSize), (py % tileSize) * (32 / tileSize)));
            }
        }

        // TODO: check what the structure is, a lot of things are "structures", including hedges, walls, flowerbeds
        world.getServerConnection().getServerConnectionListener().getStructures().forEach((id, structure) -> {
            int structureTileX = structure.getTileX() - tileX;
            int structureTileY = structure.getTileY() - tileY;
            if (Math.min(structureTileX, structureTileY) < (int) -Math.floor(tilesToRender / 2f)
                    || Math.max(structureTileX, structureTileY) > (int) Math.floor(tilesToRender / 2f)) return;

            gfx.drawImage(
                    missingImage,
                    Math.round((structureTileX + (int) Math.floor(tilesToRender / 2f)) * tileSize),
                    Math.round((structureTileY + (int) Math.floor(tilesToRender / 2f)) * tileSize),
                    tileSize,
                    tileSize,
                    null
            );
        });

        gfx.dispose();

        renderedTileX = tileX;
        renderedTileY = tileY;
        isDirty = false;
    }
}

// TODO: perf test it, try out dweia's deed switfwood
