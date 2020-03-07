package engine.gui.stage;

import com.github.mouse0w0.observable.collection.ObservableCollections;
import com.github.mouse0w0.observable.collection.ObservableList;
import com.github.mouse0w0.observable.value.*;
import engine.graphics.display.Window;
import engine.graphics.display.callback.*;
import engine.graphics.image.ReadOnlyImage;
import engine.gui.Scene;
import engine.gui.internal.GUIPlatform;
import engine.gui.internal.SceneHelper;
import engine.gui.internal.StageHelper;

import java.util.ArrayList;

public class Stage {

    private static final ObservableList<Stage> stages = ObservableCollections.observableList(new ArrayList<>());
    private static final ObservableList<Stage> unmodifiableStages = ObservableCollections.unmodifiableObservableList(stages);

    private Stage owner;

    private MutableIntValue x;
    private MutableIntValue y;

    private MutableIntValue width;
    private MutableIntValue height;

    private final MutableFloatValue scaleX = new SimpleMutableFloatValue(1);
    private final MutableFloatValue scaleY = new SimpleMutableFloatValue(1);

    private MutableFloatValue userScaleX;
    private MutableFloatValue userScaleY;

    private final MutableObjectValue<Scene> scene = new SimpleMutableObjectValue<>();

    {
        scene.addChangeListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                SceneHelper.setStage(oldValue, null);
            }
            if (newValue != null) {
                SceneHelper.setStage(newValue, Stage.this);
            }
        });
    }

    private MutableStringValue title;

    private ObservableList<ReadOnlyImage> icons;

    private MutableBooleanValue focused;
    private MutableBooleanValue iconified;
    private MutableBooleanValue maximized;
    private MutableBooleanValue resizable;
    private MutableBooleanValue alwaysOnTop;

    private final MutableBooleanValue showing = new SimpleMutableBooleanValue();

    Window window;

    private boolean primary;

    static {
        StageHelper.setStageAccessor(new StageHelper.StageAccessor() {
            @Override
            public Window getWindow(Stage stage) {
                return stage.window;
            }

            @Override
            public void setWindow(Stage stage, Window window) {
                stage.window = window;
            }

            @Override
            public boolean isPrimary(Stage stage) {
                return stage.isPrimary();
            }

            @Override
            public void setPrimary(Stage stage, boolean primary) {
                stage.setPrimary(primary);
            }

            @Override
            public MutableBooleanValue getShowingProperty(Stage stage) {
                return stage.showing;
            }

            @Override
            public void doVisibleChanged(Stage stage, boolean value) {
                stage.doVisibleChanged(value);
            }

            @Override
            public void setViewport(Stage stage, int width, int height, float scaleX, float scaleY) {
                stage.setViewport(width, height, scaleX, scaleY);
            }
        });
    }

    public static ObservableList<Stage> getStages() {
        return unmodifiableStages;
    }

    public Stage() {
    }

    public Stage getOwner() {
        return owner;
    }

    public void setOwner(Stage owner) {
        if (window != null) {
            throw new IllegalStateException("Cannot set owner once stage has been shown");
        }

        if (isPrimary()) {
            throw new IllegalStateException("Cannot set owner for the primary stage");
        }
        this.owner = owner;
    }

    private MutableIntValue xImpl() {
        if (x == null) {
            x = new SimpleMutableIntValue();
        }
        return x;
    }

    public final ObservableIntValue x() {
        return xImpl().toUnmodifiable();
    }

    public final int getX() {
        return x == null ? 0 : x.get();
    }

    private MutableIntValue yImpl() {
        if (y == null) {
            y = new SimpleMutableIntValue();
        }
        return y;
    }

    public final ObservableIntValue y() {
        return yImpl().toUnmodifiable();
    }

    public final int getY() {
        return y == null ? 0 : y.get();
    }

    public final void centerOnScreen() {
        if (window != null) window.centerOnScreen();
    }

    private MutableIntValue widthImpl() {
        if (width == null) {
            width = new SimpleMutableIntValue();
        }
        return width;
    }

    public final ObservableIntValue width() {
        return widthImpl().toUnmodifiable();
    }

    public final int getWidth() {
        return width == null ? 0 : width.get();
    }

    private MutableIntValue heightImpl() {
        if (height == null) {
            height = new SimpleMutableIntValue();
        }
        return height;
    }

    public final ObservableIntValue height() {
        return height.toUnmodifiable();
    }

    public final int getHeight() {
        return height == null ? 0 : height.get();
    }

    public final ObservableFloatValue scaleX() {
        return scaleX.toUnmodifiable();
    }

    public final float getScaleX() {
        return scaleX.get();
    }

    public final ObservableFloatValue scaleY() {
        return scaleY.toUnmodifiable();
    }

    public final float getScaleY() {
        return scaleY.get();
    }

    public final MutableFloatValue userScaleX() {
        if (userScaleX == null) {
            userScaleX = new SimpleMutableFloatValue();
            userScaleX.addChangeListener((observable, oldValue, newValue) ->
                    setViewport(getWidth(), getHeight(), newValue, getScaleY()));
        }
        return userScaleX;
    }

    public final float getUserScaleX() {
        return userScaleX == null ? 1f : userScaleX.get();
    }

    public final MutableFloatValue userScaleY() {
        if (userScaleY == null) {
            userScaleY = new SimpleMutableFloatValue();
            userScaleY.addChangeListener((observable, oldValue, newValue) ->
                    setViewport(getWidth(), getHeight(), getScaleX(), newValue));
        }
        return userScaleY;
    }

    public final float getUserScaleY() {
        return userScaleY == null ? 1f : userScaleY.get();
    }

    public final void setUserScale(float scaleX, float scaleY) {
        userScaleX().set(scaleX);
        userScaleY().set(scaleY);
    }

    private void setViewport(int width, int height, float scaleX, float scaleY) {
        float finalScaleX = scaleX * getUserScaleX();
        float finalScaleY = scaleY * getUserScaleY();

        widthImpl().set(width);
        heightImpl().set(height);
        this.scaleX.set(finalScaleX);
        this.scaleY.set(finalScaleY);
        scene().ifPresent(scene -> SceneHelper.resize(scene, width / finalScaleX, height / finalScaleY));
    }

    public final MutableObjectValue<Scene> scene() {
        return scene;
    }

    public final Scene getScene() {
        return scene.get();
    }

    public final void setScene(Scene scene) {
        this.scene.set(scene);
    }

    public final MutableStringValue title() {
        if (title == null) {
            title = new SimpleMutableStringValue() {
                @Override
                public void set(String value) {
                    super.set(value);
                    if (window != null) {
                        window.setTitle(value);
                    }
                }
            };
        }
        return title;
    }

    public final String getTitle() {
        return title == null ? "" : title.get();
    }

    public final void setTitle(String title) {
        title().set(title);
    }

    public ObservableList<ReadOnlyImage> getIcons() {
        if (icons == null) {
            icons = ObservableCollections.observableList(new ArrayList<>());
            icons.addChangeListener(change -> {
                if (window != null) {
                    window.setIcon(icons.toArray(ReadOnlyImage[]::new));
                }
            });
        }
        return icons;
    }

    private MutableBooleanValue focusedImpl() {
        if (focused == null) {
            focused = new SimpleMutableBooleanValue();
        }
        return focused;
    }

    public final ObservableBooleanValue focused() {
        return focusedImpl().toUnmodifiable();
    }

    public final boolean isFocused() {
        return focused != null && focused.get();
    }

    public final void focus() {
        if (window != null) {
            window.focus();
        }
    }

    private MutableBooleanValue iconifiedImpl() {
        if (iconified == null) {
            iconified = new SimpleMutableBooleanValue();
        }
        return iconified;
    }

    public final ObservableBooleanValue iconified() {
        return iconified.toUnmodifiable();
    }

    public final boolean isIconified() {
        return iconified != null && iconified.get();
    }

    private MutableBooleanValue maximizedImpl() {
        if (maximized == null) {
            maximized = new SimpleMutableBooleanValue();
        }
        return maximized;
    }

    public final ObservableBooleanValue getMaximized() {
        return maximizedImpl().toUnmodifiable();
    }

    public final boolean isMaximized() {
        return maximized != null && maximized.get();
    }

    public final void iconify() {
        iconifiedImpl().set(true);
        if (window != null) {
            window.iconify();
        }
    }

    public final void maximize() {
        maximizedImpl().set(true);
        if (window != null) {
            window.maximize();
        }
    }

    public final void restore() {
        iconifiedImpl().set(false);
        maximizedImpl().set(false);
        if (window != null) {
            window.restore();
        }
    }

    public final MutableBooleanValue resizable() {
        if (resizable == null) {
            resizable = new SimpleMutableBooleanValue(true) {
                @Override
                public void set(boolean value) {
                    super.set(value);
                    if (window != null) {
                        window.setResizable(value);
                    }
                }
            };
        }
        return resizable;
    }

    public final boolean isResizable() {
        return resizable == null || resizable.get();
    }

    public final void setResizable(boolean resizable) {
        resizable().set(resizable);
    }

    public final MutableBooleanValue alwaysOnTop() {
        if (alwaysOnTop == null) {
            alwaysOnTop = new SimpleMutableBooleanValue(true) {
                @Override
                public void set(boolean value) {
                    super.set(value);
                    if (window != null) {
                        window.setFloating(value);
                    }
                }
            };
        }
        return alwaysOnTop;
    }

    public final boolean isAlwaysOnTop() {
        return alwaysOnTop != null && alwaysOnTop.get();
    }

    public final void setAlwaysOnTop(boolean alwaysOnTop) {
        alwaysOnTop().set(alwaysOnTop);
    }

    public final ObservableBooleanValue showing() {
        return showing.toUnmodifiable();
    }

    public final boolean isShowing() {
        return showing.get();
    }

    public void show() {
        GUIPlatform.getInstance().getStageHelper().show(this);
    }

    public void hide() {
        GUIPlatform.getInstance().getStageHelper().hide(this);
    }

    private boolean isPrimary() {
        return primary;
    }

    private void setPrimary(boolean primary) {
        this.primary = primary;
    }

    private WindowPosCallback posCallback;
    //    private WindowSizeCallback sizeCallback;
    private WindowFocusCallback focusCallback;
    private WindowIconifyCallback iconifyCallback;
    private WindowMaximizeCallback maximizeCallback;
    private WindowCloseCallback closeCallback;

    private void doVisibleChanged(boolean value) {
        if (value) {
            window.setTitle(getTitle());
            if (icons != null) {
                window.setIcon(icons.toArray(ReadOnlyImage[]::new));
            }
            window.setResizable(isResizable());
            window.setFloating(isAlwaysOnTop());
            if (isIconified() || isMaximized()) {
                if (isIconified()) window.iconify();
                if (isMaximized()) window.maximize();
            } else {
                window.restore();
            }

            if (posCallback == null) posCallback = (window, x, y) -> {
                xImpl().set(x);
                yImpl().set(y);
            };
            window.addWindowPosCallback(posCallback);
            if (x == null && y == null) {
                centerOnScreen();
            } else {
                xImpl().set(window.getX());
                yImpl().set(window.getY());
            }

//            if (sizeCallback == null) sizeCallback = (window, width, height) -> {
//                widthImpl().set(width);
//                heightImpl().set(height);
//            };
//            window.addWindowSizeCallback(sizeCallback);
//            widthImpl().set(window.getWidth());
//            heightImpl().set(window.getHeight());

            if (focusCallback == null) focusCallback = (window, focused) -> focusedImpl().set(focused);
            window.addWindowFocusCallback(focusCallback);
            focusedImpl().set(window.isFocused());

            if (iconifyCallback == null) iconifyCallback = (window, iconified) -> iconifiedImpl().set(iconified);
            window.addWindowIconifyCallback(iconifyCallback);
            iconifiedImpl().set(window.isIconified());

            if (maximizeCallback == null) maximizeCallback = (window, maximized) -> maximizedImpl().set(maximized);
            window.addWindowMaximizeCallback(maximizeCallback);
            maximizedImpl().set(window.isMaximized());

            if (closeCallback == null) closeCallback = window -> hide();
            window.addWindowCloseCallback(closeCallback);

            setViewport(window.getWidth(), window.getHeight(), window.getContentScaleX(), window.getContentScaleY());

            stages.add(this);
        } else {
            stages.remove(this);
            window.removeWindowPosCallback(posCallback);
//            window.removeWindowSizeCallback(sizeCallback);
            window.removeWindowFocusCallback(focusCallback);
            window.removeWindowIconifyCallback(iconifyCallback);
            window.removeWindowMaximizeCallback(maximizeCallback);
            window.removeWindowCloseCallback(closeCallback);
        }
    }
}
