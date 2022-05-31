package com.wurmonline.client.renderer.gui;

import com.wurmonline.client.game.World;
import com.wurmonline.client.renderer.structures.StructureData;

public class MinimapWindow extends WWindow {

  private final WurmBorderPanel mainPanel;
  private final MinimapView minimapView;

  public MinimapWindow() {
    super("Minimap");
    this.setTitle("Minimap!");
    this.resizable = false;
    minimapView = new MinimapView("Minimap view", 256, 256);
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

  public void addStructure(StructureData structureData) {
    minimapView.addStructure(structureData);
  }

  public void removeStructure(StructureData structureData) {
    minimapView.removeStructure(structureData);
  }

  public void dump() {
    minimapView.dump();
  }
}
