package org.openmicroscopy.shoola.agents.metadata.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.JButton;

import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.util.ui.RatingComponent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * A {@link AnnotationTaskPaneUI} for displaying the user rating
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class RatingTaskPaneUI extends AnnotationTaskPaneUI implements
        PropertyChangeListener {

    /** The rating component */
    RatingComponent rating;

    /** Button to remove the rating */
    JButton unrateButton;

    /** The last selected value. */
    private int selectedValue;

    /** The initial value of the rating. */
    private int initialValue;

    /**
     * Creates a new instance
     * 
     * @param model
     *            Reference to the {@link EditorModel}
     * @param view
     *            Reference to the {@link EditorUI}
     * @param controller
     *            Reference to the {@link EditorControl}
     */
    public RatingTaskPaneUI(EditorModel model, final EditorUI view,
            EditorControl controller) {
        super(model, view, controller);

        selectedValue = 0;
        initialValue = selectedValue;

        setBackground(UIUtilities.BACKGROUND_COLOR);

        rating = new RatingComponent(selectedValue, RatingComponent.MEDIUM_SIZE);
        rating.setOpaque(false);
        rating.setBackground(UIUtilities.BACKGROUND_COLOR);
        rating.addPropertyChangeListener(this);

        IconManager icons = IconManager.getInstance();

        unrateButton = new JButton(icons.getIcon(IconManager.MINUS_12));
        UIUtilities.unifiedButtonLookAndFeel(unrateButton);
        unrateButton.setBackground(UIUtilities.BACKGROUND_COLOR);
        unrateButton.setToolTipText("Unrate.");
        unrateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rating.setValue(0);
                view.saveData(true);
            }
        });

        add(rating);
        add(Box.createHorizontalStrut(2));
        add(unrateButton);
    }

    @Override
    void refreshUI() {
        rating.removePropertyChangeListener(RatingComponent.RATE_PROPERTY, this);
        rating.setValue(model.getAllUserRating());
        rating.addPropertyChangeListener(RatingComponent.RATE_PROPERTY, this);
    }

    /**
     * Sets the currently selected rating value.
     * 
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        if (RatingComponent.RATE_PROPERTY.equals(name)) {
            int newValue = (Integer) evt.getNewValue();
            if (newValue != selectedValue) {
                selectedValue = newValue;
                view.saveData(true);
            }
        } else if (RatingComponent.RATE_END_PROPERTY.equals(name)) {
            view.saveData(true);
        }
    }
}
