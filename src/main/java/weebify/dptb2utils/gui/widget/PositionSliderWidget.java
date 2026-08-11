package weebify.dptb2utils.gui.widget;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.DoubleConsumer;
import java.util.function.IntSupplier;

@Deprecated
public class PositionSliderWidget extends AbstractSliderButton {
    private final Component text;
    private final DoubleConsumer onChange;
    private final IntSupplier maxValSupplier;

    public PositionSliderWidget(int x, int y, int width, int height, Component text, double value, DoubleConsumer onChange, IntSupplier maxValSupplier) {
        super(x, y, width, height, text, value);
        this.text = text;
        this.onChange = onChange;
        this.maxValSupplier = maxValSupplier;
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        float display = ((int)(this.value * maxValSupplier.getAsInt() * 10)) / 10.f;
        this.setMessage(Component.nullToEmpty(this.text.getString() + ": " + display));
    }

    @Override
    protected void applyValue() {
        onChange.accept(this.value);
    }
}
