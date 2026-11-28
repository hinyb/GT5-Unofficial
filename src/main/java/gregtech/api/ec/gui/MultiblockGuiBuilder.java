package gregtech.api.ec.gui;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.OrderedSectionBuilder;
import gregtech.api.ec.signal.IGuiProvider;

public final class MultiblockGuiBuilder {

    private final OrderedSectionBuilder<IGuiProvider.GuiWidgetFactory> terminalTexts = new OrderedSectionBuilder<>();
    private final OrderedSectionBuilder<IGuiProvider.GuiWidgetFactory> leftGap = new OrderedSectionBuilder<>();
    private final OrderedSectionBuilder<IGuiProvider.GuiWidgetFactory> rightGap = new OrderedSectionBuilder<>();
    private final OrderedSectionBuilder<IGuiProvider.GuiWidgetFactory> rightCorner = new OrderedSectionBuilder<>();
    private final OrderedSectionBuilder<IGuiProvider.GuiWidgetFactory> leftCorner = new OrderedSectionBuilder<>();
    private final OrderedSectionBuilder<IGuiProvider.GuiWidgetFactory> powerPanel = new OrderedSectionBuilder<>();

    public @NotNull OrderedSectionBuilder<IGuiProvider.GuiWidgetFactory> terminalTexts() {
        return terminalTexts;
    }

    public MultiblockGuiBuilder addTerminalTexts(@NotNull IGuiProvider.TerminalSection section,
        @NotNull IGuiProvider.GuiWidgetFactory factory) {
        terminalTexts.add(section, factory);
        return this;
    }

    public @NotNull OrderedSectionBuilder<IGuiProvider.GuiWidgetFactory> leftGap() {
        return leftGap;
    }

    public MultiblockGuiBuilder addLeftGap(@NotNull IGuiProvider.LeftGapSection section,
        @NotNull IGuiProvider.GuiWidgetFactory factory) {
        leftGap.add(section, factory);
        return this;
    }

    public @NotNull OrderedSectionBuilder<IGuiProvider.GuiWidgetFactory> rightGap() {
        return rightGap;
    }

    public MultiblockGuiBuilder addRightGap(@NotNull IGuiProvider.RightGapSection section,
        @NotNull IGuiProvider.GuiWidgetFactory factory) {
        rightGap.add(section, factory);
        return this;
    }

    public @NotNull OrderedSectionBuilder<IGuiProvider.GuiWidgetFactory> rightCorner() {
        return rightCorner;
    }

    public MultiblockGuiBuilder addRightCorner(@NotNull IGuiProvider.RightCornerSection section,
        @NotNull IGuiProvider.GuiWidgetFactory factory) {
        rightCorner.add(section, factory);
        return this;
    }

    public @NotNull OrderedSectionBuilder<IGuiProvider.GuiWidgetFactory> leftCorner() {
        return leftCorner;
    }

    public MultiblockGuiBuilder addLeftCorner(@NotNull IGuiProvider.LeftCornerSection section,
        @NotNull IGuiProvider.GuiWidgetFactory factory) {
        leftCorner.add(section, factory);
        return this;
    }

    public @NotNull OrderedSectionBuilder<IGuiProvider.GuiWidgetFactory> powerPanel() {
        return powerPanel;
    }

    public MultiblockGuiBuilder addPowerPanel(@NotNull IGuiProvider.PowerPanelSection section,
        @NotNull IGuiProvider.GuiWidgetFactory factory) {
        powerPanel.add(section, factory);
        return this;
    }
}
