package org.openmicroscopy.shoola.agents.metadata.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import com.google.common.base.CharMatcher;

/**
 * A Component for editing texts
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class EditableTextComponent extends JPanel {

    /** The default number of characters to show in non-edit mode */
    public static final int DEFAULT_NUMBER_OF_CHARACTERS = 30;

    /** Property name for an edit event */
    public static final String EDIT_PROPERTY = "EDIT_PROPERTY";

    /** The border in edit mode */
    private static final Border EDIT_BORDER_BLACK = BorderFactory
            .createLineBorder(Color.BLACK);

    /** Button to edit the text. */
    private JToggleButton editButton;

    /** The text component */
    private JTextArea textPane;

    /** The default border in non-edit mode */
    private Border defaultBorder;

    /** The original text */
    private String originalText;

    /** The current text */
    private String text;

    /** Number of characters to show in non-edit mode */
    private int showCharacters;

    /** Flag to indicate to allow empty text */
    private boolean permitEmpty;

    /** Reference to the button listener */
    private ItemListener buttonListener;

    /** Tooltip text for the edit button */
    private String tooltip;

    /**
     * Creates a new instance
     * 
     * @param modifiable
     *            Flag to make the component editable
     * @param permitEmpty
     *            Flag to allow empty text
     * @param tooltip
     *            Tooltip text for the edit button (can be <code>null</code>)
     */
    public EditableTextComponent(boolean modifiable, boolean permitEmpty,
            String tooltip) {
        this("", DEFAULT_NUMBER_OF_CHARACTERS, modifiable, permitEmpty, tooltip);
    }

    /**
     * Creates a new instance
     * 
     * @param text
     *            The text to show
     * @param showCharacters
     *            The number of characters to show in non-edit mode
     * @param modifiable
     *            Flag to make the component editable
     * @param permitEmpty
     *            Flag to allow empty text
     * @param tooltip
     *            Tooltip text for the edit button (can be <code>null</code>)
     */
    public EditableTextComponent(String text, int showCharacters,
            boolean modifiable, boolean permitEmpty, String tooltip) {
        this.showCharacters = showCharacters;
        this.permitEmpty = permitEmpty;
        this.tooltip = tooltip;

        initComponent();
        buildUI(text, modifiable);
    }

    /**
     * Initializes the components
     */
    private void initComponent() {
        textPane = new JTextArea();
        textPane.setOpaque(false);
        textPane.setBackground(UIUtilities.BACKGROUND_COLOR);

        textPane.setEditable(false);

        Font f = textPane.getFont();
        textPane.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent arg0) {

            }

            @Override
            public void keyReleased(KeyEvent arg0) {
                if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
                    save();
                }
            }

            @Override
            public void keyPressed(KeyEvent arg0) {

            }
        });

        defaultBorder = textPane.getBorder();
        textPane.setFont(f.deriveFont(Font.BOLD));

        IconManager icons = IconManager.getInstance();
        editButton = new JToggleButton(icons.getIcon(IconManager.EDIT_12));

        editButton.setOpaque(false);
        UIUtilities.unifiedButtonLookAndFeel(editButton);
        editButton.setBackground(UIUtilities.BACKGROUND_COLOR);
        if (tooltip != null)
            editButton.setToolTipText(tooltip);

        buttonListener = new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                JToggleButton b = (JToggleButton) e.getSource();
                if (b.isSelected()) {
                    editField(true);
                } else {
                    save();
                    editField(false);
                }
            }
        };

        editButton.addItemListener(buttonListener);
    }

    /**
     * Build/Refreshes the UI
     * 
     * @param text
     *            The text to show
     * @param modifiable
     *            Flag to make the component editable
     */
    protected void buildUI(String text, boolean modifiable) {
        this.originalText = text;
        this.text = text;

        removeAll();

        editButton.setEnabled(modifiable);
        textPane.setEditable(modifiable);
        textPane.setText(UIUtilities.formatPartialName(text, showCharacters));
        textPane.setToolTipText(text);

        // disable line wrap and only enable it in editing mode;
        // otherwise keeping it enabled has a weird effect on the layout
        textPane.setLineWrap(false);

        setBackground(UIUtilities.BACKGROUND_COLOR);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 2, 2, 2);
        c.gridy = 0;
        c.gridx = 0;
        c.weightx = 1;

        add(textPane, c);
        c.gridx++;

        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.anchor = GridBagConstraints.EAST;
        add(editButton, c);
    }

    /**
     * Toggle between edit and non-edit mode
     * 
     * @param edit
     *            Pass <code>true</code> to switch to edit mode,
     *            <code>false</code> otherwise
     */
    private void editField(boolean edit) {
        if (edit) {
            textPane.setEditable(true);
            textPane.setLineWrap(true);
            textPane.setText(text);
            textPane.setBorder(EDIT_BORDER_BLACK);
            textPane.setMaximumSize(textPane.getSize());

            textPane.requestFocus();
            textPane.select(0, 0);
            textPane.setCaretPosition(0);
        } else {
            textPane.setEditable(false);
            textPane.setLineWrap(false);
            textPane.setText(UIUtilities
                    .formatPartialName(text, showCharacters));
            textPane.setBorder(defaultBorder);
            textPane.setMaximumSize(textPane.getSize());
            editButton.removeItemListener(buttonListener);
            editButton.setSelected(false);
            editButton.addItemListener(buttonListener);
        }
    }

    /**
     * Fires the {@link #EDIT_PROPERTY} property and returns to non-edit mode
     */
    private void save() {
        this.text = CharMatcher.JAVA_ISO_CONTROL.removeFrom(textPane.getText());
        if (this.text.trim().isEmpty() && !permitEmpty) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    EditableTextComponent.this.text = originalText;
                    editField(false);
                }
            });
            return;
        }

        firePropertyChange(EDIT_PROPERTY, originalText, text);
        originalText = text;

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                editField(false);
            }
        });
    }

    /**
     * Sets the text
     * 
     * @param text
     *            The text show
     */
    void setText(String text) {
        this.text = text;
        this.originalText = text;
        textPane.setText(UIUtilities.formatPartialName(text, showCharacters));
    }

}
