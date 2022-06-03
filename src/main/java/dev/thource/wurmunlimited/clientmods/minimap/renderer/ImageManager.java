package dev.thource.wurmunlimited.clientmods.minimap.renderer;

import static com.wurmonline.mesh.Tiles.Tile.TILE_CAVE;
import static com.wurmonline.mesh.Tiles.Tile.TILE_CAVE_EXIT;
import static com.wurmonline.mesh.Tiles.Tile.TILE_CAVE_FLOOR_REINFORCED;
import static com.wurmonline.mesh.Tiles.Tile.TILE_CAVE_WALL_MARBLE;
import static com.wurmonline.mesh.Tiles.Tile.TILE_CAVE_WALL_ORE_ADAMANTINE;
import static com.wurmonline.mesh.Tiles.Tile.TILE_CAVE_WALL_ORE_COPPER;
import static com.wurmonline.mesh.Tiles.Tile.TILE_CAVE_WALL_ORE_GLIMMERSTEEL;
import static com.wurmonline.mesh.Tiles.Tile.TILE_CAVE_WALL_ORE_GOLD;
import static com.wurmonline.mesh.Tiles.Tile.TILE_CAVE_WALL_ORE_IRON;
import static com.wurmonline.mesh.Tiles.Tile.TILE_CAVE_WALL_ORE_LEAD;
import static com.wurmonline.mesh.Tiles.Tile.TILE_CAVE_WALL_ORE_SILVER;
import static com.wurmonline.mesh.Tiles.Tile.TILE_CAVE_WALL_ORE_TIN;
import static com.wurmonline.mesh.Tiles.Tile.TILE_CAVE_WALL_ORE_ZINC;
import static com.wurmonline.mesh.Tiles.Tile.TILE_CAVE_WALL_SANDSTONE;
import static com.wurmonline.mesh.Tiles.Tile.TILE_CAVE_WALL_SLATE;
import static com.wurmonline.mesh.Tiles.Tile.TILE_CLAY;
import static com.wurmonline.mesh.Tiles.Tile.TILE_CLIFF;
import static com.wurmonline.mesh.Tiles.Tile.TILE_COBBLESTONE;
import static com.wurmonline.mesh.Tiles.Tile.TILE_COBBLESTONE_ROUGH;
import static com.wurmonline.mesh.Tiles.Tile.TILE_COBBLESTONE_ROUND;
import static com.wurmonline.mesh.Tiles.Tile.TILE_DIRT;
import static com.wurmonline.mesh.Tiles.Tile.TILE_DIRT_PACKED;
import static com.wurmonline.mesh.Tiles.Tile.TILE_ENCHANTED_GRASS;
import static com.wurmonline.mesh.Tiles.Tile.TILE_ENCHANTED_TREE_OAK;
import static com.wurmonline.mesh.Tiles.Tile.TILE_FIELD;
import static com.wurmonline.mesh.Tiles.Tile.TILE_FIELD2;
import static com.wurmonline.mesh.Tiles.Tile.TILE_GRASS;
import static com.wurmonline.mesh.Tiles.Tile.TILE_GRAVEL;
import static com.wurmonline.mesh.Tiles.Tile.TILE_HOLE;
import static com.wurmonline.mesh.Tiles.Tile.TILE_KELP;
import static com.wurmonline.mesh.Tiles.Tile.TILE_LAVA;
import static com.wurmonline.mesh.Tiles.Tile.TILE_LAWN;
import static com.wurmonline.mesh.Tiles.Tile.TILE_MARBLE_BRICKS;
import static com.wurmonline.mesh.Tiles.Tile.TILE_MARBLE_SLABS;
import static com.wurmonline.mesh.Tiles.Tile.TILE_MARSH;
import static com.wurmonline.mesh.Tiles.Tile.TILE_MOSS;
import static com.wurmonline.mesh.Tiles.Tile.TILE_MYCELIUM;
import static com.wurmonline.mesh.Tiles.Tile.TILE_PEAT;
import static com.wurmonline.mesh.Tiles.Tile.TILE_PLANKS;
import static com.wurmonline.mesh.Tiles.Tile.TILE_PLANKS_TARRED;
import static com.wurmonline.mesh.Tiles.Tile.TILE_POTTERY_BRICKS;
import static com.wurmonline.mesh.Tiles.Tile.TILE_REED;
import static com.wurmonline.mesh.Tiles.Tile.TILE_ROCK;
import static com.wurmonline.mesh.Tiles.Tile.TILE_SAND;
import static com.wurmonline.mesh.Tiles.Tile.TILE_SANDSTONE_BRICKS;
import static com.wurmonline.mesh.Tiles.Tile.TILE_SANDSTONE_SLABS;
import static com.wurmonline.mesh.Tiles.Tile.TILE_SLATE_BRICKS;
import static com.wurmonline.mesh.Tiles.Tile.TILE_SLATE_SLABS;
import static com.wurmonline.mesh.Tiles.Tile.TILE_STEPPE;
import static com.wurmonline.mesh.Tiles.Tile.TILE_STONE_SLABS;
import static com.wurmonline.mesh.Tiles.Tile.TILE_TAR;
import static com.wurmonline.mesh.Tiles.Tile.TILE_TREE_OAK;
import static com.wurmonline.mesh.Tiles.Tile.TILE_TUNDRA;

import com.wurmonline.client.renderer.cell.CreatureCellRenderable;
import com.wurmonline.client.renderer.cell.PlayerCellRenderable;
import com.wurmonline.client.renderer.gui.MinimapView;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.shared.constants.BridgeConstants;
import com.wurmonline.shared.constants.StructureConstants;
import com.wurmonline.shared.constants.StructureMaterialEnum;
import dev.thource.wurmunlimited.clientmods.minimap.Settings;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.imageio.ImageIO;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

public class ImageManager {

  public static final Map<BridgeConstants.BridgeMaterial, BufferedImage> bridgeImages =
      initBridgeImages();
  public static final Map<StructureMaterialEnum, Color> fenceColors = initFenceColors();
  public static final Map<StructureConstants.FloorMaterial, BufferedImage> houseMaterialImages =
      initHouseMaterialImages();
  public static final BufferedImage missingImage = createImageFromColor(Color.MAGENTA);
  public static final BufferedImage holeImage = createImageFromColor(new Color(0x202020));
  public static final Map<Tiles.Tile, BufferedImage> tileImages = initTileImages();
  public static final BufferedImage waterImage = loadImage("/textures/terrain/water.png");
  public static final BufferedImage playerCursorImage =
      loadImage("/sprites/player-arrow.png", false);
  public static final BufferedImage neutralIcon = loadImage("/sprites/icon-neutral.png", false);
  public static final BufferedImage friendIcon = loadImage("/sprites/icon-friend.png", false);
  public static final BufferedImage allyIcon = loadImage("/sprites/icon-ally.png", false);
  public static final BufferedImage hostileIcon = loadImage("/sprites/icon-hostile.png", false);
  public static final BufferedImage neutralPlayerIcon =
      loadImage("/sprites/icon-player-neutral.png", false);
  public static final BufferedImage friendPlayerIcon =
      loadImage("/sprites/icon-player-friend.png", false);
  public static final BufferedImage allyPlayerIcon =
      loadImage("/sprites/icon-player-ally.png", false);
  public static final BufferedImage hostilePlayerIcon =
      loadImage("/sprites/icon-player-hostile.png", false);
  public static final BufferedImage gmPlayerIcon = loadImage("/sprites/icon-player-gm.png", false);
  public static final BufferedImage devPlayerIcon =
      loadImage("/sprites/icon-player-dev.png", false);

  private static BufferedImage loadImage(String path) {
    return loadImage(path, true);
  }

  private static BufferedImage loadImage(String path, boolean scale) {
    try {
      BufferedImage image =
          ImageIO.read(Objects.requireNonNull(MinimapView.class.getResourceAsStream(path)));
      if (!scale) {
        return image;
      }

      Image scaledImage =
          image.getScaledInstance(
              Settings.getTileSize(), Settings.getTileSize(), Image.SCALE_SMOOTH);

      BufferedImage scaledBufferedImage =
          new BufferedImage(Settings.getTileSize(), Settings.getTileSize(), image.getType());
      Graphics2D graphics = scaledBufferedImage.createGraphics();
      graphics.drawImage(scaledImage, 0, 0, null);
      graphics.dispose();

      return scaledBufferedImage;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Map<Tiles.Tile, BufferedImage> initTileImages() {
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
    simpleMap.put(TILE_PLANKS_TARRED, "planks");
    simpleMap.put(TILE_KELP, "dirt");
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
    simpleMap.put(TILE_CAVE, "cave512");
    simpleMap.put(TILE_CAVE_EXIT, "reinforcedcaveFloor_v1");
    simpleMap.put(TILE_CAVE_FLOOR_REINFORCED, "reinforcedcaveFloor_v1");

    Map<Tiles.Tile, BufferedImage> map = new HashMap<>();
    simpleMap.forEach(
        (tileType, imagePath) ->
            map.put(tileType, loadImage("/textures/terrain/" + imagePath + ".png")));
    map.put(TILE_HOLE, holeImage);
    map.put(TILE_CAVE_WALL_ORE_IRON, createImageFromColor(new Color(0x9A4444)));
    map.put(TILE_CAVE_WALL_ORE_COPPER, createImageFromColor(new Color(0x61AF95)));
    map.put(TILE_CAVE_WALL_ORE_GOLD, createImageFromColor(new Color(0xD7BD0A)));
    map.put(TILE_CAVE_WALL_ORE_ADAMANTINE, createImageFromColor(new Color(0x60a0ff)));
    map.put(TILE_CAVE_WALL_ORE_GLIMMERSTEEL, createImageFromColor(new Color(0xFFF1A7)));
    map.put(TILE_CAVE_WALL_ORE_LEAD, createImageFromColor(new Color(0x576B7E)));
    map.put(TILE_CAVE_WALL_ORE_SILVER, createImageFromColor(new Color(0xE6E6E6)));
    map.put(TILE_CAVE_WALL_ORE_TIN, createImageFromColor(new Color(0xB4C8D2)));
    map.put(TILE_CAVE_WALL_ORE_ZINC, createImageFromColor(new Color(0x8C8C8C)));
    map.put(TILE_CAVE_WALL_SANDSTONE, createImageFromColor(new Color(0xFFF1C3)));
    map.put(TILE_CAVE_WALL_SLATE, createImageFromColor(new Color(0x404040)));
    map.put(TILE_CAVE_WALL_MARBLE, createImageFromColor(new Color(0xAAAAAA)));
    return Collections.unmodifiableMap(map);
  }

  private static Map<StructureConstants.FloorMaterial, BufferedImage> initHouseMaterialImages() {
    Map<StructureConstants.FloorMaterial, String> simpleMap = new HashMap<>();
    simpleMap.put(StructureConstants.FloorMaterial.CLAY_BRICK, "clay_brick_dist");
    simpleMap.put(StructureConstants.FloorMaterial.MARBLE_SLAB, "marble_floor_dist");
    simpleMap.put(StructureConstants.FloorMaterial.SANDSTONE_SLAB, "slab_floor_dist");
    simpleMap.put(StructureConstants.FloorMaterial.SLATE_SLAB, "slate_floor_dist");
    simpleMap.put(StructureConstants.FloorMaterial.STONE_BRICK, "brick_floor_dist");
    simpleMap.put(StructureConstants.FloorMaterial.STONE_SLAB, "slab_floor_dist2");
    simpleMap.put(StructureConstants.FloorMaterial.THATCH, "thatched");
    simpleMap.put(StructureConstants.FloorMaterial.WOOD, "floor_dist");
    simpleMap.put(StructureConstants.FloorMaterial.STANDALONE, "Floor_Plan_dist");

    Map<StructureConstants.FloorMaterial, BufferedImage> map = new HashMap<>();
    simpleMap.forEach(
        (tileType, imagePath) ->
            map.put(tileType, loadImage("/textures/house/" + imagePath + ".png")));
    return Collections.unmodifiableMap(map);
  }

  private static Map<BridgeConstants.BridgeMaterial, BufferedImage> initBridgeImages() {
    Map<BridgeConstants.BridgeMaterial, String> simpleMap = new HashMap<>();
    simpleMap.put(BridgeConstants.BridgeMaterial.BRICK, "Stone/bridgeTiling");
    simpleMap.put(BridgeConstants.BridgeMaterial.POTTERY, "Stone/bridgeBrickTiling");
    simpleMap.put(BridgeConstants.BridgeMaterial.RENDERED, "Stone/bridgeRenderedTiling");
    simpleMap.put(BridgeConstants.BridgeMaterial.SANDSTONE, "Stone/bridgeSandstoneTiling");
    simpleMap.put(BridgeConstants.BridgeMaterial.SLATE, "Stone/bridgeSlateTiling");
    simpleMap.put(BridgeConstants.BridgeMaterial.ROUNDED_STONE, "Stone/bridgeRoundedTiling");
    simpleMap.put(BridgeConstants.BridgeMaterial.MARBLE, "Marble/bridgeTilingMarble");

    Map<BridgeConstants.BridgeMaterial, BufferedImage> map = new HashMap<>();
    simpleMap.forEach(
        (tileType, imagePath) ->
            map.put(tileType, loadImage("/textures/Bridges/" + imagePath + ".png")));
    return Collections.unmodifiableMap(map);
  }

  private static Map<StructureMaterialEnum, Color> initFenceColors() {
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
    map.put(StructureMaterialEnum.TIMBER_FRAMED, new Color(0xcccccc));

    return Collections.unmodifiableMap(map);
  }

  private static BufferedImage createImageFromColor(Color color) {
    BufferedImage image =
        new BufferedImage(
            Settings.getTileSize(), Settings.getTileSize(), BufferedImage.TYPE_INT_RGB);
    Graphics2D gfx = image.createGraphics();
    gfx.setBackground(color);
    gfx.clearRect(0, 0, Settings.getTileSize(), Settings.getTileSize());
    gfx.dispose();

    return image;
  }

  public static Optional<BufferedImage> getIconForCreature(CreatureCellRenderable creature) {
    // TODO: check if creature is a cart, chair, etc and return null
    if (creature.isItem()) {
      return Optional.empty();
    }

    int attitude;
    try {
      attitude =
          ReflectionUtil.getPrivateField(
              creature, ReflectionUtil.getField(creature.getClass(), "attitude"));
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
          return Optional.of(neutralPlayerIcon);
        case "friend":
          return Optional.of(friendPlayerIcon);
        case "hostile":
          return Optional.of(hostilePlayerIcon);
        case "ally":
          return Optional.of(allyPlayerIcon);
        case "gm":
          return Optional.of(gmPlayerIcon);
        case "dev":
          return Optional.of(devPlayerIcon);
      }
    }

    switch (status) {
      case "neutral":
      case "gm":
      case "dev":
        return Optional.of(neutralIcon);
      case "friend":
        return Optional.of(friendIcon);
      case "hostile":
        return Optional.of(hostileIcon);
      case "ally":
        return Optional.of(allyIcon);
    }

    // should never be hit
    throw new RuntimeException("No icon, attitude: " + attitude + ", isPlayer: " + isPlayer);
  }
}
