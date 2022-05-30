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
import java.util.ListIterator;

public class RenderedHouse extends RenderedStructure {
  //  private static final List<FloorType> TRANSPARENT_FLOOR_TYPES = List.of(FloorType.OPENING);

  static {
    PADDING = (int) Math.max(Constants.TILE_SIZE / 4f, 1);
  }

  private final List<BufferedImage> levelImages = new ArrayList<>();
  private final List<HouseFloorData> houseFloors = new ArrayList<>();
  private final List<HouseWallData> houseWalls = new ArrayList<>();
  private final List<HouseRoofData> houseRoofs = new ArrayList<>();
  private final Object imageLock = new Object();
  private int baseHeight = 999999;

  public RenderedHouse(HouseData data) {
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

      if (houseFloor.getHeightOffset() == 0 && baseHeight == 999999) {
        baseHeight = (int) (houseFloor.getHPos() * 10);
      }

      if (!resize(houseFloor.getTileX(), houseFloor.getTileY(), houseFloor.getHeightOffset())) {
        redrawLevel(houseFloor.getHeightOffset() / 30);
      }
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

  public void addHouseWall(HouseWallData houseWall) {
    if (houseWall.getType() == StructureConstantsEnum.NO_WALL) {
      return;
    }

    synchronized (imageLock) {
      houseWalls.add(houseWall);

      if (houseWall.getHeightOffset() == 0 && baseHeight == 999999) {
        baseHeight = (int) (houseWall.getHPos() * 10);
      }

      if (!resize(houseWall.getTileX(), houseWall.getTileY(), houseWall.getHeightOffset())) {
        redrawLevel(houseWall.getHeightOffset() / 30);
      }
    }
  }

  private void drawHouseWall(HouseWallData houseWall, Graphics2D graphics) {
    graphics.setPaint(
        ImageManager.fenceColors.getOrDefault(houseWall.getType().material, Color.MAGENTA));

    if (houseWall.isGate()) {
      graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
    }

    boolean horizontal = houseWall.getTileXEnd() != houseWall.getTileX();
    graphics.fillRect(
        (houseWall.getTileX() - tileX) * Constants.TILE_SIZE,
        (houseWall.getTileY() - tileY) * Constants.TILE_SIZE,
        horizontal ? Constants.TILE_SIZE + PADDING : PADDING,
        horizontal ? PADDING : Constants.TILE_SIZE + PADDING);
  }

  public void addHouseRoof(HouseRoofData houseRoof) {
    synchronized (imageLock) {
      houseRoofs.add(houseRoof);

      if (!resize(houseRoof.getTileX(), houseRoof.getTileY(), houseRoof.getHeightOffset())) {
        redrawLevel(houseRoof.getHeightOffset() / 30);
      }
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
  protected boolean resize(int newTileX, int newTileY, int heightOffset) {
    if (newTileX < tileX) {
      width += tileX - newTileX;
      tileX = newTileX;
    } else {
      width = Math.max(width, newTileX - tileX + 1);
    }

    if (newTileY < tileY) {
      length += tileY - newTileY;
      tileY = newTileY;
    } else {
      length = Math.max(length, newTileY - tileY + 1);
    }

    boolean sizeChanged =
        image.getWidth() != width * Constants.TILE_SIZE + PADDING
            || image.getHeight() != length * Constants.TILE_SIZE + PADDING;
    if (!sizeChanged) {
      int newTopFloor = levelImages.size();
      for (int l = levelImages.size(); l <= heightOffset / 30; l++) {
        levelImages.add(
            new BufferedImage(
                width * Constants.TILE_SIZE + PADDING,
                length * Constants.TILE_SIZE + PADDING,
                BufferedImage.TYPE_INT_ARGB));
      }

      if (newTopFloor != levelImages.size()) {
        redrawLevel(newTopFloor);
      }

      return false;
    }

    ListIterator<BufferedImage> iterator = levelImages.listIterator();
    while (iterator.hasNext()) {
      iterator.next();
      iterator.set(
          new BufferedImage(
              width * Constants.TILE_SIZE + PADDING,
              length * Constants.TILE_SIZE + PADDING,
              BufferedImage.TYPE_INT_ARGB));
    }

    image =
        new BufferedImage(
            width * Constants.TILE_SIZE + PADDING,
            length * Constants.TILE_SIZE + PADDING,
            BufferedImage.TYPE_INT_ARGB);
    fullRedraw();

    return true;
  }

  @Override
  protected void fullRedraw() {
    redrawLevel(0);
  }

  private void redrawImage() {
    Graphics2D graphics = image.createGraphics();
    graphics.setBackground(new Color(0, 0, 0, 0));
    graphics.clearRect(0, 0, image.getWidth(), image.getHeight());
    graphics.drawImage(levelImages.get(levelImages.size() - 1), 0, 0, null);
    graphics.dispose();
  }

  private void redrawLevel(int floor) {
    redrawLevel(floor, true);
  }

  private void redrawLevel(int level, boolean redrawHigherFloors) {
    if (level >= levelImages.size()) {
      return;
    }

    if (redrawHigherFloors) {
      for (int l = level; l < levelImages.size(); l++) {
        redrawLevel(l, false);
      }

      redrawImage();

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
