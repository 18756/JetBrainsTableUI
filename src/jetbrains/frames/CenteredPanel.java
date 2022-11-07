package jetbrains.frames;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CenteredPanel extends JPanel {
    private static final int BORDER_SIZE = 10;

    public CenteredPanel(List<ResizableComponent> resizableComponents,
                         double widthBorderResizeCoefficient,
                         double heightBorderResizeCoefficient) {
        setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;

        setUpGridBagConstraints(gridBagConstraints, 0.0, heightBorderResizeCoefficient, 0, 0);
        add(Box.createVerticalStrut(BORDER_SIZE), gridBagConstraints);

        int gridy = 0;
        for (ResizableComponent resizableComponent : resizableComponents) {
            gridy++;
            setUpGridBagConstraints(gridBagConstraints, widthBorderResizeCoefficient, 0.0, 0, gridy);
            add(Box.createHorizontalStrut(BORDER_SIZE), gridBagConstraints);

            setUpGridBagConstraints(gridBagConstraints, resizableComponent.weightx, resizableComponent.weighty, 1, gridy);
            add(resizableComponent.component, gridBagConstraints);

            setUpGridBagConstraints(gridBagConstraints, widthBorderResizeCoefficient, 0.0, 2, gridy);
            add(Box.createHorizontalStrut(BORDER_SIZE), gridBagConstraints);
        }

        setUpGridBagConstraints(gridBagConstraints, 0.0, heightBorderResizeCoefficient, 0, gridy + 1);
        add(Box.createVerticalStrut(BORDER_SIZE), gridBagConstraints);
    }

    private void setUpGridBagConstraints(GridBagConstraints gridBagConstraints,
                                        double weightx,
                                        double weighty,
                                        int gridx,
                                        int gridy) {
        gridBagConstraints.weightx = weightx;
        gridBagConstraints.weighty = weighty;
        gridBagConstraints.gridx = gridx;
        gridBagConstraints.gridy = gridy;
    }

    public static class ResizableComponent {
        Component component;
        double weightx;
        double weighty;

        public ResizableComponent(Component component, double weightx, double weighty) {
            this.component = component;
            this.weightx = weightx;
            this.weighty = weighty;
        }
    }
}
