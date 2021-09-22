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
import com.wurmonline.client.renderer.structures.*;
import com.wurmonline.client.resources.textures.ImageTexture;
import com.wurmonline.client.resources.textures.ImageTextureLoader;
import com.wurmonline.client.resources.textures.PreProcessedTextureData;
import com.wurmonline.client.resources.textures.TextureLoader;
import com.wurmonline.math.Vector2f;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.shared.constants.BridgeConstants;
import com.wurmonline.shared.constants.StructureMaterialEnum;
import com.wurmonline.shared.util.MovementChecker;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static com.wurmonline.mesh.Tiles.Tile.*;
import static com.wurmonline.mesh.Tiles.TileRoadDirection.*;

public class MinimapView extends FlexComponent {
    private final Map<Tiles.Tile, BufferedImage> tileImages = initTileImages();
    private final Map<BridgeConstants.BridgeMaterial, BufferedImage> bridgeImages = initBridgeImages();
    private final Map<StructureMaterialEnum, Color> fenceColors = initFenceColors();
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
            waterImage = loadImage("/textures/terrain/water.png");
            playerCursorImage = loadImage("/sprites/player-arrow.png");
            neutralIcon = loadImage("/sprites/icon-neutral.png");
            friendIcon = loadImage("/sprites/icon-friend.png");
            allyIcon = loadImage("/sprites/icon-ally.png");
            hostileIcon = loadImage("/sprites/icon-hostile.png");
            neutralPlayerIcon = loadImage("/sprites/icon-player-neutral.png");
            friendPlayerIcon = loadImage("/sprites/icon-player-friend.png");
            allyPlayerIcon = loadImage("/sprites/icon-player-ally.png");
            hostilePlayerIcon = loadImage("/sprites/icon-player-hostile.png");
            gmPlayerIcon = loadImage("/sprites/icon-player-gm.png");
            devPlayerIcon = loadImage("/sprites/icon-player-dev.png");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private BufferedImage loadImage(String s) throws IOException {
        return ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream(s)));
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
        Map<Tiles.Tile, String> simpleMap = new HashMap<>();
        simpleMap.put(TILE_CLAY, "clay");
        simpleMap.put(TILE_CLIFF, "cliff");
        simpleMap.put(TILE_COBBLESTONE, "cobble");
        simpleMap.put(TILE_COBBLESTONE_ROUGH, "cobble2");
        simpleMap.put(TILE_COBBLESTONE_ROUND, "cobble3");
        simpleMap.put(TILE_DIRT, "dirt");
        simpleMap.put(TILE_DIRT_PACKED, "packed");
        simpleMap.put(TILE_ENCHANTED_GRASS, "enchantedgrass");
        simpleMap.put(TILE_ENCHANTED_TREE_OAK, "enchantedforest");
        simpleMap.put(TILE_FIELD, "farm");
        simpleMap.put(TILE_FIELD2, "farm");
        simpleMap.put(TILE_GRASS, "grass");
        simpleMap.put(TILE_GRAVEL, "gravel");
        simpleMap.put(TILE_LAVA, "lava");
        simpleMap.put(TILE_LAWN, "lawn");
        simpleMap.put(TILE_MARBLE_SLABS, "marbleslab");
        simpleMap.put(TILE_MARBLE_BRICKS, "marbleBricks");
        simpleMap.put(TILE_MARSH, "marsh");
        simpleMap.put(TILE_MOSS, "moss");
        simpleMap.put(TILE_MYCELIUM, "mycelium");
        simpleMap.put(TILE_PEAT, "peat");
        simpleMap.put(TILE_PLANKS, "planks");
        simpleMap.put(TILE_POTTERY_BRICKS, "potterybrickpaving");
        simpleMap.put(TILE_REED, "reed");
        simpleMap.put(TILE_ROCK, "rock");
        simpleMap.put(TILE_SAND, "sand");
        simpleMap.put(TILE_SANDSTONE_BRICKS, "sandstonebrick");
        simpleMap.put(TILE_SANDSTONE_SLABS, "sandstoneslab");
        simpleMap.put(TILE_SLATE_BRICKS, "slatebricks");
        simpleMap.put(TILE_SLATE_SLABS, "slateTiles");
        simpleMap.put(TILE_STEPPE, "steppe");
        simpleMap.put(TILE_STONE_SLABS, "slab");
        simpleMap.put(TILE_TAR, "tar");
        simpleMap.put(TILE_TREE_OAK, "forest");
        simpleMap.put(TILE_TUNDRA, "tundra");

        Map<Tiles.Tile, BufferedImage> map = new HashMap<>();
        simpleMap.forEach((tileType, imagePath) -> {
            try {
                map.put(tileType, loadImage("/textures/terrain/" + imagePath + ".png"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return Collections.unmodifiableMap(map);
    }

    private Map<BridgeConstants.BridgeMaterial, BufferedImage> initBridgeImages() {
        Map<BridgeConstants.BridgeMaterial, String> simpleMap = new HashMap<>();
        simpleMap.put(BridgeConstants.BridgeMaterial.BRICK, "Stone/bridgeTiling");
        simpleMap.put(BridgeConstants.BridgeMaterial.POTTERY, "Stone/bridgeBrickTiling");
        simpleMap.put(BridgeConstants.BridgeMaterial.RENDERED, "Stone/bridgeRenderedTiling");
        simpleMap.put(BridgeConstants.BridgeMaterial.SANDSTONE, "Stone/bridgeSandstoneTiling");
        simpleMap.put(BridgeConstants.BridgeMaterial.SLATE, "Stone/bridgeSlateTiling");
        simpleMap.put(BridgeConstants.BridgeMaterial.ROUNDED_STONE, "Stone/bridgeRoundedTiling");
        simpleMap.put(BridgeConstants.BridgeMaterial.MARBLE, "Marble/bridgeTilingMarble");

        Map<BridgeConstants.BridgeMaterial, BufferedImage> map = new HashMap<>();
        simpleMap.forEach((tileType, imagePath) -> {
            try {
                map.put(tileType, loadImage("/textures/Bridges/" + imagePath + ".png"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return Collections.unmodifiableMap(map);
    }

    private Map<StructureMaterialEnum, Color> initFenceColors() {
        Color hedgeColor = new Color(0x464f2c);
        Color woodColor = new Color(0x634e3c);
        Color stoneColor = new Color(0x5b5a5d);
        Map<StructureMaterialEnum, Color> map = new HashMap<>();
        map.put(StructureMaterialEnum.WOOD, woodColor);
        map.put(StructureMaterialEnum.LOG, woodColor);
        map.put(StructureMaterialEnum.CRUDE_WOOD, woodColor);
        map.put(StructureMaterialEnum.FLOWER1, hedgeColor);
        map.put(StructureMaterialEnum.FLOWER2, hedgeColor);
        map.put(StructureMaterialEnum.FLOWER3, hedgeColor);
        map.put(StructureMaterialEnum.FLOWER4, hedgeColor);
        map.put(StructureMaterialEnum.FLOWER5, hedgeColor);
        map.put(StructureMaterialEnum.FLOWER6, hedgeColor);
        map.put(StructureMaterialEnum.FLOWER7, hedgeColor);
        map.put(StructureMaterialEnum.STONE, stoneColor);
        map.put(StructureMaterialEnum.ROUNDED_STONE, stoneColor);
        map.put(StructureMaterialEnum.PLAIN_STONE, stoneColor);
        map.put(StructureMaterialEnum.RENDERED, stoneColor);
        map.put(StructureMaterialEnum.IRON, new Color(0x82807f));
        map.put(StructureMaterialEnum.SLATE, new Color(0x292624));
        map.put(StructureMaterialEnum.SANDSTONE, new Color(0xcdb193));
        map.put(StructureMaterialEnum.POTTERY, new Color(0x724224));
        map.put(StructureMaterialEnum.MARBLE, new Color(0xccc8bf));
        map.put(StructureMaterialEnum.FIRE, new Color(0xFF8400));
        map.put(StructureMaterialEnum.ICE, new Color(0x7BCBFF));

        return Collections.unmodifiableMap(map);
    }

    private BufferedImage createMissingImage() {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D gfx = image.createGraphics();
        gfx.setBackground(Color.MAGENTA);
        gfx.clearRect(0, 0, 1, 1);
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
            tileType = tileType.isEnchanted() ? TILE_ENCHANTED_TREE_OAK : TILE_TREE_OAK;

        BufferedImage tileImage = tileImages.getOrDefault(tileType, missingImage);
        if (tileType.isRoad()) {
            int roadDir = nearTerrainBuffer.getData(x, y) & 7;
            Tiles.TileRoadDirection roadDirection = Tiles.TileRoadDirection.DIR_STRAIGHT;
            if (roadDir == 1) {
                roadDirection = DIR_NW;
            } else if (roadDir == 2) {
                roadDirection = DIR_NE;
            } else if (roadDir == 3) {
                roadDirection = DIR_SE;
            } else if (roadDir == 4) {
                roadDirection = DIR_SW;
            }

            if (roadDirection != Tiles.TileRoadDirection.DIR_STRAIGHT) {
                tileImage = cloneImage(tileImage);

                BufferedImage northTileImage = null;
                BufferedImage eastTileImage = null;
                BufferedImage southTileImage = null;
                BufferedImage westTileImage = null;
                if (roadDirection == DIR_SE || roadDirection == DIR_SW) {
                    northTileImage = tileImages.getOrDefault(nearTerrainBuffer.getSecondaryType(x, y - 1), missingImage);
                }
                if (roadDirection == DIR_NW || roadDirection == DIR_SW) {
                    eastTileImage = tileImages.getOrDefault(nearTerrainBuffer.getSecondaryType(x + 1, y), missingImage);
                }
                if (roadDirection == DIR_NE || roadDirection == DIR_NW) {
                    southTileImage = tileImages.getOrDefault(nearTerrainBuffer.getSecondaryType(x, y + 1), missingImage);
                }
                if (roadDirection == DIR_SE || roadDirection == DIR_NE) {
                    westTileImage = tileImages.getOrDefault(nearTerrainBuffer.getSecondaryType(x - 1, y), missingImage);
                }

                for (int py = 0; py < 64; py++) {
                    for (int px = 0; px < 64; px++) {
                        if ((roadDirection == DIR_SE || roadDirection == DIR_SW) && py <= 31 && px - py >= 0 && px + py <= 63) {
                            tileImage.setRGB(px, py, northTileImage.getRGB(px, py));
                        }
                        if ((roadDirection == DIR_NW || roadDirection == DIR_SW) && px >= 32 && py - (63 - px) >= 0 && py + (63 - px) <= 63) {
                            tileImage.setRGB(px, py, eastTileImage.getRGB(px, py));
                        }
                        if ((roadDirection == DIR_NE || roadDirection == DIR_NW) && py >= 32 && px - (63 - py) >= 0 && px + (63 - py) <= 63) {
                            tileImage.setRGB(px, py, southTileImage.getRGB(px, py));
                        }
                        if ((roadDirection == DIR_NE || roadDirection == DIR_SE) && px <= 31 && py - px >= 0 && py + px <= 63) {
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
                float worldX = ((pTileX) + (px / (float) (tileSize - 1))) * 4f;
                int pTileY = tileY - (tilesToRender / 2);
                float worldY = ((pTileY) + (py / (float) (tileSize - 1))) * 4f;

                float pointHeight = nearTerrainBuffer.getInterpolatedHeight(worldX, worldY);
                if (waterHeight >= pointHeight)
                    renderedImage.setRGB(px, py, waterImage.getRGB((px % tileSize) * (64 / tileSize), (py % tileSize) * (64 / tileSize)));
            }
        }

        renderedTileX = tileX;
        renderedTileY = tileY;
        isDirty = false;

        world.getServerConnection().getServerConnectionListener().getStructures().forEach((id, structure) -> drawStructure(gfx, structure));

        gfx.dispose();
    }

    private void drawStructure(Graphics2D gfx, StructureData structure) {
        System.out.println("structure: " + structure.getClass().getName() + ", tileX: " + structure.getTileX() + ", tileY: " + structure.getTileY());
        int tileX = structure.getTileX() - renderedTileX;
        int tileY = structure.getTileY() - renderedTileY;

        if (structure instanceof BridgeData) {
            MovementChecker movementChecker;
            try {
                movementChecker = ReflectionUtil.getPrivateField(world.getPlayer(), ReflectionUtil.getField(PlayerObj.class, "movementChecker"));
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }

            final boolean[] isOverPlayer = {false};
            if (structure.getId() != movementChecker.getBridgeId()) {
                ((BridgeData) structure).getBridgeParts().forEach((id, bridgePart) -> {
                    if (isOverPlayer[0]) return;

                    if (bridgePart.getTileX() == renderedTileX && bridgePart.getTileY() == renderedTileY) {
                        isOverPlayer[0] = true;
                    }
                });
            }

            ((BridgeData) structure).getBridgeParts().values().stream().sorted(Comparator.comparingInt(bp -> (bp.getTileX() + bp.getTileY()))).forEach((bridgePart) -> drawBridgePart(gfx, bridgePart, isOverPlayer[0]));
            return;
        } else if (structure instanceof HouseFloorData) {
            gfx.setPaint(Color.WHITE);
        } else if (structure instanceof HouseRoofData) {
            gfx.setPaint(Color.RED);
        } else if (structure instanceof FloorData) {
            gfx.setPaint(Color.CYAN);
        } else if (structure instanceof RoofData) {
            gfx.setPaint(Color.PINK);
        } else if (structure instanceof MineDoorData) {
            gfx.setPaint(Color.BLACK);
        } else if (structure instanceof FenceData) {
            gfx.setPaint(fenceColors.getOrDefault(((FenceData) structure).getType().material, Color.MAGENTA));

            int thickness = (int) Math.floor(tileSize / 4f);
            int xEnd = ((FenceData) structure).getTileXEnd();
            int yEnd = ((FenceData) structure).getTileYEnd();
            if (thickness < 1 || xEnd == tileX && yEnd == tileY) return;

            int xSize = Math.max(thickness, tileSize * (xEnd - structure.getTileX()));
            int ySize = Math.max(thickness, tileSize * (yEnd - structure.getTileY()));
            int xStart = Math.round((tileX + (int) Math.floor(tilesToRender / 2f)) * tileSize);
            int yStart = Math.round((tileY + (int) Math.floor(tilesToRender / 2f)) * tileSize);
            if (xSize == thickness) xStart -= thickness / 2;
            if (ySize == thickness) yStart -= thickness / 2;

            if (((FenceData) structure).isGate())
                gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));

            gfx.fillRect(xStart, yStart, xSize, ySize);

            if (((FenceData) structure).isGate())
                gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

            return;
        } else if (structure instanceof HouseData) {
            gfx.setPaint(Color.GRAY);
        } else {
            return;
        }

        if (Math.min(tileX, tileY) < (int) -Math.floor(tilesToRender / 2f) - 1
                || Math.max(tileX, tileY) > (int) Math.floor(tilesToRender / 2f)) return;

        gfx.drawRect(
                Math.round((tileX + (int) Math.floor(tilesToRender / 2f)) * tileSize),
                Math.round((tileY + (int) Math.floor(tilesToRender / 2f)) * tileSize),
                tileSize,
                tileSize
        );

//            gfx.drawImage(
//                    missingImage,
//                    Math.round((structureTileX + (int) Math.floor(tilesToRender / 2f)) * tileSize),
//                    Math.round((structureTileY + (int) Math.floor(tilesToRender / 2f)) * tileSize),
//                    tileSize,
//                    tileSize,
//                    null
//            );
    }

    private void drawBridgePart(Graphics2D gfx, BridgePartData structure, boolean isOverPlayer) {
        int tileX = structure.getTileX() - renderedTileX;
        int tileY = structure.getTileY() - renderedTileY;

        if (isOverPlayer)
            gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));

        BufferedImage bridgeImage = bridgeImages.getOrDefault(structure.getMaterial(), missingImage);
        gfx.drawImage(
                bridgeImage,
                Math.round((tileX + (int) Math.floor(tilesToRender / 2f)) * tileSize),
                Math.round((tileY + (int) Math.floor(tilesToRender / 2f)) * tileSize),
                tileSize,
                tileSize,
                null
        );

        int shadowThickness = (int) Math.floor(tileSize / 8f);
        if (shadowThickness >= 1) {
            boolean horizontal = structure.getDir() / 2 % 2 == 1;
            for (int i = 0; i < shadowThickness; i++) {
                gfx.setPaint(new Color(0, 0, 0, (0.5f / shadowThickness) * (shadowThickness - i)));
                gfx.fillRect(
                        Math.round((tileX + (horizontal ? 0 : 1) + (int) Math.floor(tilesToRender / 2f)) * tileSize + (horizontal ? 0 : i)),
                        Math.round((tileY + (horizontal ? 1 : 0) + (int) Math.floor(tilesToRender / 2f)) * tileSize + (horizontal ? i : 0)),
                        horizontal ? tileSize : 1,
                        horizontal ? 1 : tileSize
                );
            }
        }

        if (isOverPlayer)
            gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
    }
}

// TODO: perf test it, try out dweia's deed switfwood
