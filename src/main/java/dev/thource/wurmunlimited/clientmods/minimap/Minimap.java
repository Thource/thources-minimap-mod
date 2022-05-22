package dev.thource.wurmunlimited.clientmods.minimap;

import com.wurmonline.client.game.World;
import com.wurmonline.client.renderer.gui.HeadsUpDisplay;
import com.wurmonline.client.renderer.gui.MainMenu;
import com.wurmonline.client.renderer.gui.MinimapWindow;
import com.wurmonline.client.renderer.gui.WurmComponent;
import com.wurmonline.client.settings.SavePosManager;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.WurmClientMod;
import org.gotti.wurmunlimited.modsupport.console.ConsoleListener;
import org.gotti.wurmunlimited.modsupport.console.ModConsole;

public class Minimap implements WurmClientMod, Initable, PreInitable, ConsoleListener {

  private boolean isOpen = false;
  private MinimapWindow minimapWindow;

  @Override
  public void preInit() {

  }

  @Override
  public void init() {
    ClassLoader cl = ClassLoader.getSystemClassLoader();
    URL[] urls = ((URLClassLoader) cl).getURLs();

    try {
      String clientJar = urls[0].toString();
      URL jarURL = new URL(clientJar.substring(0, clientJar.lastIndexOf('/'))
          + "/mods/thources-minimap-mod/thources-minimap-mod-1.0.0.jar");
      // This adds the mod jar to the class path, which allows loading of jar resources
      ReflectionUtil.callPrivateMethod(cl,
          ReflectionUtil.getMethod(cl.getClass(), "addURL", new Class[]{URL.class}), jarURL);
    } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | MalformedURLException e) {
      throw new RuntimeException(e);
    }

    urls = ((URLClassLoader) cl).getURLs();

    for (URL url : urls) {
      System.out.println(url.getFile());
    }

    HookManager.getInstance()
        .registerHook("com.wurmonline.client.renderer.gui.HeadsUpDisplay", "init", "(II)V",
            () -> (proxy, method, args) -> {
              method.invoke(proxy, args);
              Minimap.this.initMap((HeadsUpDisplay) proxy);
              //noinspection SuspiciousInvocationHandlerImplementation
              return null;
            });

    ModConsole.addConsoleListener(this);
  }

  private void initMap(final HeadsUpDisplay hud) {
    try {
      World world = ReflectionUtil.getPrivateField(hud,
          ReflectionUtil.getField(hud.getClass(), "world"));
      Minimap.this.minimapWindow = new MinimapWindow(world);
      MainMenu mainMenu = ReflectionUtil.getPrivateField(hud,
          ReflectionUtil.getField(hud.getClass(), "mainMenu"));
      mainMenu.registerComponent("Minimap", Minimap.this.minimapWindow);
      List<WurmComponent> components = ReflectionUtil.getPrivateField(hud,
          ReflectionUtil.getField(hud.getClass(), "components"));
      components.add(Minimap.this.minimapWindow);
      SavePosManager savePosManager = ReflectionUtil.getPrivateField(hud,
          ReflectionUtil.getField(hud.getClass(), "savePosManager"));
      savePosManager.registerAndRefresh(Minimap.this.minimapWindow, "minimapwindow");
    } catch (IllegalAccessException | ClassCastException | NoSuchFieldException | IllegalArgumentException var5) {
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
      }
    }

    System.out.printf("[%s] Valid commands are: open, close, toggle%n", Minimap.class.getName());
    return true;
  }
}
