package com.wurmonline.client.renderer.gui;

import com.wurmonline.client.game.World;

public class MinimapWindow extends WWindow {

  private final WurmBorderPanel mainPanel;

  public MinimapWindow(World world) {
    super("Minimap");
    this.setTitle("Minimap!");
    this.resizable = false;
    MinimapView minimapView = new MinimapView("Minimap view", 256, 256);
    this.mainPanel = new WurmBorderPanel("Minimap panel");
    this.mainPanel.setComponent(minimapView, 3);
    this.setComponent(this.mainPanel);
    this.setInitialSize(294, 281, false);
    this.layout();
    this.sizeFlags = 3;
  }

  public void closePressed() {
    hud.toggleComponent(this);
  }

  public void toggle() {
    hud.toggleComponent(this);
  }
}
