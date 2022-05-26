package dev.thource.wurmunlimited.clientmods.minimap.renderer.topology;

import com.wurmonline.client.game.IDataBuffer;
import dev.thource.wurmunlimited.clientmods.minimap.Constants;
import java.awt.Color;

public class ShadedRelief {
  static final float ALTITUDE = 45f;
  static final float AZIMUTH = 315f;

  private ShadedRelief() {}

  private static float getAveragePointHeight(
      IDataBuffer buffer, float worldX, float worldY, float dirX, float dirY, int steps, float stepSize) {
    float average = buffer.getInterpolatedHeight(worldX + dirX * stepSize, worldY + dirY * stepSize);
    if (steps <= 1) {
      return average;
    }

    return (average + buffer.getInterpolatedHeight(worldX + dirX * stepSize * steps, worldY + dirY * stepSize * steps)) / 2f;
  }

  public static Color getColor(IDataBuffer buffer, float worldX, float worldY, float stepSize) {
    //    float northWestHeight = buffer.getInterpolatedHeight(worldX - stepSize, worldY -
    // stepSize);
    //    float northHeight = buffer.getInterpolatedHeight(worldX, worldY - stepSize);
    //    float northEastHeight = buffer.getInterpolatedHeight(worldX + stepSize, worldY -
    // stepSize);
    //    float westHeight = buffer.getInterpolatedHeight(worldX - stepSize, worldY);
    //    float eastHeight = buffer.getInterpolatedHeight(worldX + stepSize, worldY);
    //    float southWestHeight = buffer.getInterpolatedHeight(worldX - stepSize, worldY +
    // stepSize);
    //    float southHeight = buffer.getInterpolatedHeight(worldX, worldY + stepSize);
    //    float southEastHeight = buffer.getInterpolatedHeight(worldX + stepSize, worldY +
    // stepSize);
    float northWestHeight =
        getAveragePointHeight(buffer, worldX, worldY, -1, -1, Math.max(Constants.TILE_SIZE / 4, 1), stepSize);
    float northHeight =
        getAveragePointHeight(buffer, worldX, worldY, 0, -1, Math.max(Constants.TILE_SIZE / 4, 1), stepSize);
    float northEastHeight =
        getAveragePointHeight(buffer, worldX, worldY, 1, -1, Math.max(Constants.TILE_SIZE / 4, 1), stepSize);
    float westHeight =
        getAveragePointHeight(buffer, worldX, worldY, -1, 0, Math.max(Constants.TILE_SIZE / 4, 1), stepSize);
    float eastHeight =
        getAveragePointHeight(buffer, worldX, worldY, 1, 0, Math.max(Constants.TILE_SIZE / 4, 1), stepSize);
    float southWestHeight =
        getAveragePointHeight(buffer, worldX, worldY, -1, 1, Math.max(Constants.TILE_SIZE / 4, 1), stepSize);
    float southHeight =
        getAveragePointHeight(buffer, worldX, worldY, 0, 1, Math.max(Constants.TILE_SIZE / 4, 1), stepSize);
    float southEastHeight =
        getAveragePointHeight(buffer, worldX, worldY, 1, 1, Math.max(Constants.TILE_SIZE / 4, 1), stepSize);

    double horizontalDelta =
        ((southWestHeight + southHeight * 2f + southEastHeight)
                - (northWestHeight + northHeight * 2f + northEastHeight))
            / (8f * 16f);
    double verticalDelta =
        ((northEastHeight + eastHeight * 2f + southEastHeight)
                - (northWestHeight + westHeight * 2f + southWestHeight))
            / (8f * 16f);
    double slope = Math.atan(Math.sqrt(Math.pow(horizontalDelta, 2) + Math.pow(verticalDelta, 2)));
    double aspect = Math.atan2(verticalDelta, horizontalDelta);
    double value =
        Math.min(
            Math.max(
                ((Math.cos(90 - ALTITUDE) * Math.cos(slope)
                            + Math.sin(90 - ALTITUDE)
                                * Math.sin(slope)
                                * Math.cos(AZIMUTH - aspect))
                        - 0.5f)
                    * 2f,
                -1f),
            1f);

    return new Color(
        value < 0 ? 0 : 255,
        value < 0 ? 0 : 255,
        value < 0 ? 0 : 255,
        (int) (Math.abs(value) * 255));
  }
}
