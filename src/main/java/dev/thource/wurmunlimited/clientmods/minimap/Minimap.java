package dev.thource.wurmunlimited.clientmods.minimap;

import com.wurmonline.client.renderer.gui.HeadsUpDisplay;
import com.wurmonline.client.renderer.gui.MainMenu;
import com.wurmonline.client.renderer.gui.MinimapWindow;
import com.wurmonline.client.renderer.gui.WurmComponent;
import com.wurmonline.client.renderer.structures.StructureData;
import com.wurmonline.client.settings.SavePosManager;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.WurmClientMod;
import org.gotti.wurmunlimited.modsupport.console.ConsoleListener;
import org.gotti.wurmunlimited.modsupport.console.ModConsole;

public class Minimap implements WurmClientMod, Initable, PreInitable, ConsoleListener {

  private final List<StructureData> structureDataQueue = new ArrayList<>();
  private boolean isOpen = false;
  private MinimapWindow minimapWindow;

  @Override
  public void preInit() {
    HookManager.getInstance()
        .registerHook(
            "com.wurmonline.client.renderer.cell.CellRenderer",
            "addStructure",
            "(Lcom/wurmonline/client/renderer/structures/StructureData;)V",
            () ->
                (proxy, method, args) -> {
                  method.invoke(proxy, args);

                  StructureData structureData = (StructureData) args[0];
                  if (minimapWindow != null) {
                    minimapWindow.addStructure(structureData);
                  } else {
                    structureDataQueue.add(structureData);
                  }

                  //noinspection SuspiciousInvocationHandlerImplementation
                  return null;
                });

    HookManager.getInstance()
        .registerHook(
            "com.wurmonline.client.renderer.cell.CellRenderer",
            "removeStructure",
            "(Lcom/wurmonline/client/renderer/structures/StructureData;)V",
            () ->
                (proxy, method, args) -> {
                  method.invoke(proxy, args);

                  // should never happen?
                  if (minimapWindow != null) {
                    StructureData structureData = (StructureData) args[0];
                    minimapWindow.removeStructure(structureData);
                  }

                  //noinspection SuspiciousInvocationHandlerImplementation
                  return null;
                });
  }

  @Override
  public void init() {
    ClassLoader cl = ClassLoader.getSystemClassLoader();
    URL[] urls = ((URLClassLoader) cl).getURLs();

    try {
      String clientJar = urls[0].toString();
      URL jarURL =
          new URL(
              clientJar.substring(0, clientJar.lastIndexOf('/'))
                  + "/mods/thources-minimap-mod/thources-minimap-mod-1.0.0.jar");
      // This adds the mod jar to the class path, which allows loading of jar resources
      ReflectionUtil.callPrivateMethod(
          cl, ReflectionUtil.getMethod(cl.getClass(), "addURL", new Class[] {URL.class}), jarURL);
    } catch (IllegalAccessException
        | NoSuchMethodException
        | InvocationTargetException
        | MalformedURLException e) {
      throw new RuntimeException(e);
    }

    urls = ((URLClassLoader) cl).getURLs();

    for (URL url : urls) {
      System.out.println(url.getFile());
    }

    HookManager.getInstance()
        .registerHook(
            "com.wurmonline.client.renderer.gui.HeadsUpDisplay",
            "init",
            "(II)V",
            () ->
                (proxy, method, args) -> {
                  method.invoke(proxy, args);
                  initMap((HeadsUpDisplay) proxy);
                  //noinspection SuspiciousInvocationHandlerImplementation
                  return null;
                });

    ModConsole.addConsoleListener(this);
  }

  private void initMap(final HeadsUpDisplay hud) {
    try {
      minimapWindow = new MinimapWindow();
      structureDataQueue.forEach(structureData -> minimapWindow.addStructure(structureData));
      structureDataQueue.clear();

      MainMenu mainMenu =
          ReflectionUtil.getPrivateField(hud, ReflectionUtil.getField(hud.getClass(), "mainMenu"));
      mainMenu.registerComponent("Minimap", minimapWindow);
      List<WurmComponent> components =
          ReflectionUtil.getPrivateField(
              hud, ReflectionUtil.getField(hud.getClass(), "components"));
      components.add(minimapWindow);
      SavePosManager savePosManager =
          ReflectionUtil.getPrivateField(
              hud, ReflectionUtil.getField(hud.getClass(), "savePosManager"));
      savePosManager.registerAndRefresh(minimapWindow, "minimapwindow");
    } catch (IllegalAccessException
        | ClassCastException
        | NoSuchFieldException
        | IllegalArgumentException var5) {
      throw new RuntimeException(var5);
    }
  }

  private void open() {
    isOpen = true;
  }

  private void close() {
    isOpen = false;
  }

  private void toggle() {
    if (isOpen) {
      close();
      return;
    }

    open();
  }

  @Override
  public boolean handleInput(String string, Boolean aBoolean) {
    if (string == null) {
      return false;
    }

    String[] args = string.split("\\s+");
    if (!args[0].equals("minimap")) {
      return false;
    }

    if (args.length > 1) {
      String command = args[1];
      switch (command) {
        case "open":
          open();
          System.out.printf("[%s] Opened%n", Minimap.class.getName());
          return true;
        case "close":
          close();
          System.out.printf("[%s] Closed%n", Minimap.class.getName());
          return true;
        case "toggle":
          toggle();
          System.out.printf("[%s] %s%n", Minimap.class.getName(), isOpen ? "Opened" : "Closed");
          return true;
        case "dump":
          minimapWindow.dump();
          System.out.printf("[%s] Dumping%n", Minimap.class.getName());
          return true;
      }
    }

    System.out.printf("[%s] Valid commands are: open, close, toggle%n", Minimap.class.getName());
    return true;
  }
}
