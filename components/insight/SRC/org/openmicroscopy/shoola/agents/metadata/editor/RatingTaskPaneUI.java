package org.openmicroscopy.shoola.agents.metadata.editor;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;

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
    private RatingComponent rating;
    
    /** Label showing the other user's rating */
    private JLabel otherRating;

    /** The original rating value */
    private int originalValue;
    
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

        originalValue = 0;

        setBackground(UIUtilities.BACKGROUND_COLOR);

        rating = new RatingComponent(originalValue, RatingComponent.MEDIUM_SIZE);
        rating.setOpaque(false);
        rating.setBackground(UIUtilities.BACKGROUND_COLOR);
        rating.addPropertyChangeListener(this);
        add(rating);
        
        add(Box.createHorizontalStrut(2));
        
        otherRating = new JLabel();
        otherRating.setBackground(UIUtilities.BACKGROUND_COLOR);
        Font font = otherRating.getFont();
        otherRating.setFont(font.deriveFont(Font.ITALIC, font.getSize()-2));
        add(otherRating);
    }

    @Override
    void clearDisplay() {
        
    }
    
    @Override
    void refreshUI() {
        StringBuilder buffer = new StringBuilder();
        if (!model.isMultiSelection()) {
            originalValue = model.getUserRating();
            int n = model.getRatingCount(EditorModel.ALL);
            if (n > 0) {
                buffer.append("(avg:"+model.getRatingAverage(EditorModel.ALL)+
                        " | "+n+" vote");
                if (n > 1) buffer.append("s");
                buffer.append(")");
            }
            otherRating.setVisible(n > 0);
            
        } else {
            originalValue = model.getRatingAverage(EditorModel.ME);
            int n = model.getRatingCount(EditorModel.ME);
            if (n > 0) {
                buffer.append("out of "+n);
                 buffer.append(" rating");
                if (n > 1) buffer.append("s");
            }
            otherRating.setVisible(true);
        }
        
        otherRating.setText(buffer.toString()); 
        
        rating.removePropertyChangeListener(RatingComponent.RATE_PROPERTY, this);
        rating.setValue(originalValue);
        rating.addPropertyChangeListener(RatingComponent.RATE_PROPERTY, this);
    }

    
    @Override
    List<JButton> getToolbarButtons() {
        List<JButton> buttons = new ArrayList<JButton>();
        
        IconManager icons = IconManager.getInstance();
        
        JButton unrateButton = new JButton(icons.getIcon(IconManager.MINUS_12));
        UIUtilities.unifiedButtonLookAndFeel(unrateButton);
        unrateButton.setBackground(UIUtilities.BACKGROUND_COLOR);
        unrateButton.setToolTipText("Unrate.");
        unrateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rating.setValue(0);
                view.saveData(true);
            }
        });
        buttons.add(unrateButton);
        
        return buttons;
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
            if (newValue != originalValue) {
                originalValue = newValue;
                view.saveData(true);
            }
        } else if (RatingComponent.RATE_END_PROPERTY.equals(name)) {
            view.saveData(true);
        }
    }
}
