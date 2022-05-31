package dev.thource.wurmunlimited.clientmods.minimap.renderer.component;

import com.wurmonline.client.renderer.structures.HouseData;
import com.wurmonline.client.renderer.structures.HouseFloorData;
import com.wurmonline.client.renderer.structures.HouseRoofData;
import com.wurmonline.client.renderer.structures.HouseWallData;
import com.wurmonline.shared.constants.StructureConstants.FloorType;
import com.wurmonline.shared.constants.StructureConstantsEnum;
import dev.thource.wurmunlimited.clientmods.minimap.Constants;
import dev.thource.wurmunlimited.clientmods.minimap.renderer.ImageManager;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RenderedHouse extends RenderedStructure {
  //  private static final List<FloorType> TRANSPARENT_FLOOR_TYPES = List.of(FloorType.OPENING);

  static {
    PADDING = (int) Math.max(Constants.TILE_SIZE / 4f, 1);
  }

  private final List<BufferedImage> levelImages = new ArrayList<>();
  private final List<HouseFloorData> houseFloors = new ArrayList<>();
  private final List<HouseWallData> houseWalls = new ArrayList<>();
  private final List<HouseRoofData> houseRoofs = new ArrayList<>();
  private int baseHeight = 999999;
  private int levels = 1;
  private HouseData data;

  public RenderedHouse(HouseData data) {
    this.data = data;
    id = data.getId();

    // These are the wrong way around on purpose, it's how the data comes through
    tileX = data.getTileY();
    tileY = data.getTileX();
    image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
  }

  private boolean isInsideHouse(int tileX, int tileY, int height) {
    return houseFloors.stream()
            .anyMatch(
                floor ->
                    floor.getTileX() == tileX
                        && floor.getTileY() == tileY
                        && floor.getHPos() * 10 > height)
        || houseRoofs.stream()
            .anyMatch(
                roof ->
                    roof.getTileX() == tileX
                        && roof.getTileY() == tileY
                        && roof.getHPos() * 10 > height);
  }

  public void addHouseFloor(HouseFloorData houseFloor) {
    synchronized (imageLock) {
      houseFloors.add(houseFloor);
      dirty = true;

      if (houseFloor.getHeightOffset() == 0 && baseHeight == 999999) {
        baseHeight = (int) (houseFloor.getHPos() * 10);
      }
    }
  }

  public void addHouseWall(HouseWallData houseWall) {
    if (houseWall.getType() == StructureConstantsEnum.NO_WALL) {
      return;
    }

    synchronized (imageLock) {
      houseWalls.add(houseWall);
      dirty = true;

      if (houseWall.getHeightOffset() == 0 && baseHeight == 999999) {
        baseHeight = (int) (houseWall.getHPos() * 10);
      }
    }
  }

  public void addHouseRoof(HouseRoofData houseRoof) {
    synchronized (imageLock) {
      houseRoofs.add(houseRoof);
      dirty = true;
    }
  }

  public void removeHouseFloor(HouseFloorData houseFloor) {
    synchronized (imageLock) {
      houseFloors.remove(houseFloor);
      dirty = true;
    }
  }

  public void removeHouseWall(HouseWallData houseWall) {
    synchronized (imageLock) {
      houseWalls.remove(houseWall);
      dirty = true;
    }
  }

  public void removeHouseRoof(HouseRoofData houseRoof) {
    synchronized (imageLock) {
      houseRoofs.remove(houseRoof);
      dirty = true;
    }
  }

  private void drawHouseFloor(HouseFloorData houseFloor, Graphics2D graphics) {
    BufferedImage floorImage =
        new BufferedImage(Constants.TILE_SIZE, Constants.TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
    Graphics2D floorGraphics = floorImage.createGraphics();
    floorGraphics.drawImage(
        ImageManager.houseMaterialImages.getOrDefault(
            houseFloor.getMaterial(), ImageManager.missingImage),
        0,
        0,
        null);

    if (houseFloor.getType() == FloorType.OPENING) {
      floorGraphics.setBackground(new Color(0, 0, 0, 0));
      floorGraphics.clearRect(
          Constants.TILE_SIZE / 4,
          Constants.TILE_SIZE / 4,
          Constants.TILE_SIZE / 2,
          Constants.TILE_SIZE / 2);
    }

    graphics.drawImage(
        floorImage,
        (houseFloor.getTileX() - tileX) * Constants.TILE_SIZE + PADDING / 2,
        (houseFloor.getTileY() - tileY) * Constants.TILE_SIZE + PADDING / 2,
        null);
  }

  private void drawHouseWall(HouseWallData houseWall, Graphics2D graphics) {
    graphics.setPaint(
        ImageManager.fenceColors.getOrDefault(houseWall.getType().material, Color.MAGENTA));

    if (houseWall.isGate()
        || houseWall.getCollisionHeight() == 0f
        || houseWall.getCollisionThickness() == 0f) {
      graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
    }

    boolean horizontal = houseWall.getTileXEnd() != houseWall.getTileX();
    graphics.fillRect(
        (houseWall.getTileX() - tileX) * Constants.TILE_SIZE,
        (houseWall.getTileY() - tileY) * Constants.TILE_SIZE,
        horizontal ? Constants.TILE_SIZE + PADDING : PADDING,
        horizontal ? PADDING : Constants.TILE_SIZE + PADDING);

    if (houseWall.isGate()
        || houseWall.getCollisionHeight() == 0f
        || houseWall.getCollisionThickness() == 0f) {
      graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
    }
  }

  private void drawHouseRoof(HouseRoofData houseRoof, Graphics2D graphics) {
    graphics.drawImage(
        ImageManager.houseMaterialImages.getOrDefault(
            houseRoof.getMaterial(), ImageManager.missingImage),
        (houseRoof.getTileX() - tileX) * Constants.TILE_SIZE + PADDING / 2,
        (houseRoof.getTileY() - tileY) * Constants.TILE_SIZE + PADDING / 2,
        null);
  }

  @Override
  protected boolean hasSizeChanged() {
    return super.hasSizeChanged() || levelImages.size() != levels;
  }

  @Override
  protected void fullRedraw(BufferedImage image) {
    IntStream.range(0, levelImages.size()).forEach(this::redrawLevel);

    redrawImage(image);
  }

  @Override
  protected BufferedImage resize() {
    BufferedImage newImage = super.resize();
    if (newImage == image) {
      return newImage;
    }

    levelImages.clear();
    for (int l = 0; l < levels; l++) {
      levelImages.add(
          new BufferedImage(
              newImage.getWidth(), newImage.getHeight(), BufferedImage.TYPE_INT_ARGB));
    }

    return newImage;
  }

  @Override
  protected void recalculateDimensions() {
    tileX = 9999;
    tileY = 9999;
    baseHeight = 999999;
    AtomicInteger maxTileX = new AtomicInteger(-1);
    AtomicInteger maxTileY = new AtomicInteger(-1);
    AtomicInteger maxHeight = new AtomicInteger(-1);

    Stream.of(houseFloors.stream(), houseWalls.stream(), houseRoofs.stream())
        .flatMap(s -> s)
        .forEach(
            data -> {
              tileX = Math.min(tileX, data.getTileX());
              tileY = Math.min(tileY, data.getTileY());
              baseHeight = Math.min((int) (data.getHPos() * 10), baseHeight);
              maxTileX.set(Math.max(maxTileX.get(), data.getTileX()));
              maxTileY.set(Math.max(maxTileY.get(), data.getTileY()));
              maxHeight.set(Math.max(maxHeight.get(), (int) (data.getHPos() * 10)));
            });

    width = Math.max(maxTileX.get() - tileX + 1, 1);
    length = Math.max(maxTileY.get() - tileY + 1, 1);
    levels = Math.max(((maxHeight.get() - baseHeight) / 30) + 1, 1);
  }

  private void redrawImage(BufferedImage image) {
    Graphics2D graphics = image.createGraphics();
    graphics.setBackground(new Color(0, 0, 0, 0));
    graphics.clearRect(0, 0, image.getWidth(), image.getHeight());
    graphics.drawImage(levelImages.get(levelImages.size() - 1), 0, 0, null);
    graphics.dispose();
  }

  private void redrawLevel(int level) {
    if (level >= levelImages.size()) {
      return;
    }

    Graphics2D graphics = levelImages.get(level).createGraphics();
    graphics.setBackground(new Color(0, 0, 0, 0));
    graphics.clearRect(0, 0, image.getWidth(), image.getHeight());

    if (level > 0) {
      graphics.drawImage(levelImages.get(level - 1), 0, 0, null);
    }

    houseFloors.stream()
        .filter(houseFloor -> houseFloor.getHeightOffset() / 30 == level)
        .sorted(
            Comparator.comparingInt(HouseFloorData::getTileX)
                .thenComparingInt(HouseFloorData::getTileY))
        .forEach(houseFloor -> drawHouseFloor(houseFloor, graphics));

    houseWalls.stream()
        .filter(houseWall -> houseWall.getHeightOffset() / 30 == level)
        .sorted(
            Comparator.comparingInt(HouseWallData::getTileX)
                .thenComparingInt(HouseWallData::getTileY))
        .forEach(houseWall -> drawHouseWall(houseWall, graphics));

    houseRoofs.stream()
        .filter(houseRoof -> houseRoof.getHeightOffset() / 30 == level)
        .sorted(
            Comparator.comparingInt(HouseRoofData::getTileX)
                .thenComparingInt(HouseRoofData::getTileY))
        .forEach(houseRoof -> drawHouseRoof(houseRoof, graphics));

    graphics.dispose();
  }

  public BufferedImage getLevelImage(int tileX, int tileY, int height) {
    if (!isInsideHouse(tileX, tileY, height)) {
      return image;
    }

    int level = (height - 2 - baseHeight) / 30;
    if (level < 0 || level >= levelImages.size()) {
      return image;
    }

    return levelImages.get(level);
  }
}
