package com.wurmonline.client.renderer.gui;

import com.wurmonline.client.renderer.structures.StructureData;

public class MinimapWindow extends WWindow {

  private final WurmBorderPanel mainPanel;
  private final MinimapView minimapView;

  public MinimapWindow() {
    super("Minimap");
    setTitle("Minimap!");
    minimapView = new MinimapView("Minimap view", 512, 512);
    mainPanel = new WurmBorderPanel("Minimap panel");
    mainPanel.setComponent(minimapView, 3);
    setComponent(this.mainPanel);
    setInitialSize(294, 281, false);
    layout();

    minimapView.setActualWidth(width - 6);
    minimapView.setActualHeight(height - 32);
  }

  @Override
  void componentResized() {
    width = Math.min(Math.max(width, 128 + 6), 512 + 6);
    height = Math.min(Math.max(height, 128 + 32), 512 + 32);
    super.componentResized();

    minimapView.setActualWidth(width - 6);
    minimapView.setActualHeight(height - 32);

//    int viewSize = Math.max(width - 6, height - 32);
//    minimapView.width = viewSize;
//    minimapView.height = viewSize;
//    mainPanel.layout();
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
