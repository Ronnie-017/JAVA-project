import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

// =======================================================
// ImageViewer.java
// Simple window to display the student's uploaded image.
// =======================================================
public class ImageViewer extends JFrame implements ActionListener {

    JButton closeButton;

    public ImageViewer(String imagePath) {
        setTitle("Uploaded Image Viewer");
        setSize(500, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Check if image path exists
        if (imagePath == null || imagePath.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No image uploaded for this request.");
            dispose();
            return;
        }

        File imgFile = new File(imagePath);
        if (!imgFile.exists()) {
            JOptionPane.showMessageDialog(this, "Image file not found at: " + imagePath);
            dispose();
            return;
        }

        // Load and scale the image
        ImageIcon originalIcon = new ImageIcon(imagePath);
        Image img = originalIcon.getImage().getScaledInstance(450, 320, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(img), JLabel.CENTER);

        add(new JScrollPane(imageLabel), BorderLayout.CENTER);

        // Bottom panel with Close button
        JPanel bottomPanel = new JPanel();
        closeButton = new JButton("Close");
        closeButton.addActionListener(this);
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    // Event Handling
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == closeButton) {
            dispose(); // Close only this image window
        }
    }
}
