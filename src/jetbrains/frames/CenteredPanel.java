package jetbrains.frames;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CenteredPanel extends JPanel {
    public CenteredPanel(List<Component> components) {
        setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;

        for (Component component : components) {
            add(component, gridBagConstraints);
        }
    }
}
