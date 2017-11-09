package edu.jhuapl.saavtk.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.math3.geometry.euclidean.threed.NotARotationMatrixException;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;

import edu.jhuapl.saavtk.gui.JTextFieldDoubleVerifier;
import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.gui.Renderer.ProjectionType;
import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;

public class CameraDialog extends JDialog implements ActionListener
{
    private Renderer renderer;
    private JButton applyButton;
    private JButton resetButton;
    private JButton okayButton;
    private JButton cancelButton;
    private JTextField fovField;
    private JTextField cameraAltitudeField;
    private JComboBox projComboBox;
    private JTextField cameraLatitudeField;
    private JTextField cameraLongitudeField;
    private JTextField cameraRollField;
    private JTextField viewPointLatitiudeField;
    private JTextField viewPointLongitudeField;
    private JTextField viewPointAltitudeField;
    private boolean nadirFocalPoint;
    private static final double JupiterScale = 75000;
    private double cameraRadius = 0.0, viewRadius = 0.0;


    private void printCameraOrientation()
    {
        double[] position = new double[3];
        double[] cx = new double[3];
        double[] cy = new double[3];
        double[] cz = new double[3];
        double[] viewAngle = new double[1];
        renderer.getCameraOrientation(position, cx, cy, cz, viewAngle);

        try
        {
            double[][] m = {
                    {cx[0], cx[1], cx[2]},
                    {cy[0], cy[1], cy[2]},
                    {cz[0], cz[1], cz[2]}
            };

            Rotation rotation = new Rotation(m, 1.0e-6);

            String str = "Camera position and orientation (quaternion):\n";
            str += position[0] + " " + position[1] + " " + position[2] + "\n";
            str += rotation.getQ0() + " " + rotation.getQ1() + " " + rotation.getQ2() + " " + rotation.getQ3();

            //str += "\n" + m[0][0] + " " + m[0][1] + " " + m[0][2];
            //str += "\n" + m[1][0] + " " + m[1][1] + " " + m[1][2];
            //str += "\n" + m[2][0] + " " + m[2][1] + " " + m[2][2];

            System.out.println(str);
        }
        catch (NotARotationMatrixException e)
        {
            e.printStackTrace();
        }
    }

    public CameraDialog(Renderer renderer)
    {
      this.renderer = renderer;

      JPanel panel = new JPanel();
      panel.setLayout(new MigLayout("", "[][grow][]", "[][][][][][][]"));

      // Create "Vertical Field of View" text entry box and add to 1st row
      JLabel fovLabel = new JLabel("Vertical Field of View");
      fovField = new JTextField();
      fovField.setPreferredSize(new Dimension(125, 23));
      fovField.setInputVerifier(JTextFieldDoubleVerifier.getVerifier(fovField, 0.00000001, 179.0));
      JLabel degreesLabel = new JLabel("degrees");
      panel.add(fovLabel, "cell 0 0");
      panel.add(fovField, "cell 1 0,growx");
      panel.add(degreesLabel, "cell 2 0");
      
      // Create "View Point Latitude" text entry box and add to 2nd row
      JLabel vpLat = new JLabel("View Point Latitude");
      viewPointLatitiudeField = new JTextField();
      viewPointLatitiudeField.setInputVerifier(JTextFieldDoubleVerifier.getVerifier(viewPointLatitiudeField, -90.0, 90.0));
      JLabel vpLatDegree = new JLabel("degrees");
      panel.add(vpLat, "cell 0 1");
      panel.add(viewPointLatitiudeField, "cell 1 1,growx");
      panel.add(vpLatDegree, "cell 2 1");
      
      // Create "View Point Longitude" text entry box and add to 3rd row
      JLabel vpLong = new JLabel("View Point Longitude");
      viewPointLongitudeField = new JTextField();
      viewPointLongitudeField.setInputVerifier(JTextFieldDoubleVerifier.getVerifier(viewPointLongitudeField, -180.0, 180.0));
      JLabel vpLongDegree = new JLabel("degrees");
      panel.add(vpLong, "cell 0 2");
      panel.add(viewPointLongitudeField, "cell 1 2,growx");
      panel.add(vpLongDegree, "cell 2 2");
      
      // Create "View Point Altitude" text entry box and add to 4th row
      JLabel vpAlt = new JLabel("View Point Altitude");
      viewPointAltitudeField = new JTextField();
      viewPointAltitudeField.setInputVerifier(JTextFieldDoubleVerifier.getVerifier(viewPointAltitudeField));
      JLabel vpAltDistance = new JLabel("km");
      panel.add(vpAlt, "cell 0 3");
      panel.add(viewPointAltitudeField, "cell 1 3,growx");
      panel.add(vpAltDistance, "cell 2 3");
      
      // Create "Projection Type" combo box and add to 5th row
      JLabel projLabel = new JLabel("Projection Type");
      projComboBox = new JComboBox(Renderer.ProjectionType.values());
      panel.add(projLabel, "cell 0 4,alignx trailing");
      panel.add(projComboBox, "cell 1 4,growx");

      // Create "Camera Latitude" text entry box and add to 6th row
      panel.add(new JLabel("Camera Latitude"), "cell 0 5,alignx trailing");
      cameraLatitudeField = new JTextField();
      cameraLatitudeField.setInputVerifier(JTextFieldDoubleVerifier.getVerifier(cameraLatitudeField, -90.0, 90.0));
      panel.add(cameraLatitudeField, "cell 1 5,growx");
      panel.add(new JLabel("degrees"), "cell 2 5");

      // Create "Camera Longitude" text entry box and add to 7th row
      panel.add(new JLabel("Camera Longitude"), "cell 0 6,alignx trailing");
      cameraLongitudeField = new JTextField();
      cameraLongitudeField.setInputVerifier(JTextFieldDoubleVerifier.getVerifier(cameraLongitudeField, -180.0, 180.0));
      panel.add(cameraLongitudeField, "cell 1 6,growx");
      panel.add(new JLabel("degrees"), "cell 2 6");
      
      // Create "Camera Altitude" text entry box and add to 8th row
      JLabel altLabel = new JLabel("Camera Altitude");
      panel.add(altLabel, "cell 0 7,alignx trailing");
      cameraAltitudeField = new JTextField();
      cameraAltitudeField.setInputVerifier(JTextFieldDoubleVerifier.getVerifier(cameraAltitudeField));
      panel.add(cameraAltitudeField, "cell 1 7,growx");
      JLabel kmLabel = new JLabel("km");
      panel.add(kmLabel, "cell 2 7");

      // Create "Camera Roll" text entry box and add to 9th row
      panel.add(new JLabel("Camera Roll"), "cell 0 8,alignx trailing");
      cameraRollField = new JTextField();
      cameraRollField.setInputVerifier(JTextFieldDoubleVerifier.getVerifier(cameraRollField, -360.0, 360.0));
      panel.add(cameraRollField, "cell 1 8,growx");
      panel.add(new JLabel("degrees"), "cell 2 8");

      // Create "Apply", "Reset", "OK", and "Cancel" buttons and add to 10th row
      JPanel buttonPanel = new JPanel(new MigLayout());
      applyButton = new JButton("Apply");
      applyButton.addActionListener(this);
      resetButton = new JButton("Reset");
      resetButton.addActionListener(this);
      okayButton = new JButton("OK");
      okayButton.addActionListener(this);
      cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(this);
      buttonPanel.add(applyButton);
      buttonPanel.add(resetButton);
      buttonPanel.add(okayButton);
      buttonPanel.add(cancelButton);
      panel.add(buttonPanel, "cell 0 9 3 1,alignx right");

      setModalityType(Dialog.ModalityType.APPLICATION_MODAL);

      getContentPane().add(panel, BorderLayout.CENTER);
      pack();

      printCameraOrientation();
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == applyButton || e.getSource() == okayButton)
        {
            try
            {
                double newFov = Double.parseDouble(fovField.getText());
                renderer.setCameraViewAngle(newFov);
            }
            catch (NumberFormatException ex)
            {
            }
            // Reset the text field in case the requested change was not fulfilled.
// REMOVED because it causes Renderer to freeze. -turnerj1
//            fovField.setText(String.valueOf(renderer.getCameraViewAngle()));

            try
            {
              GenericPolyhedralModel model = renderer.getGenericPolyhedralModel();
              double altitude = Double.valueOf(cameraAltitudeField.getText());
              LatLon cameraLatLong = new LatLon(Double.valueOf(cameraLatitudeField.getText()), Double.valueOf(cameraLongitudeField.getText()));
              double[] pos = MathUtil.latrec(cameraLatLong.toRadians());
              double[] viewDirection = new double[3];
              double[] origin = new double[3];
              MathUtil.unorm(pos, pos);
              MathUtil.vscl(JupiterScale, pos, origin);
              MathUtil.vscl(-1.0, pos, viewDirection);
//              pos[0] *= altitude;
//              pos[1] *= altitude;
//              pos[2] *= altitude;
              int result = model.computeRayIntersection(origin, viewDirection, pos);
              cameraRadius = MathUtil.vnorm(pos);
//              System.out.println("RadiusCamera: " + cameraRadius);
              //MathUtil.unorm(pos, pos);
              //MathUtil.vscl(radius + altitude, pos, pos);
              renderer.setCameraDistance(cameraRadius + altitude);
            }
            catch (NumberFormatException ex)
            {
            }
            // Reset the text field in case the requested change was not fulfilled.
// REMOVED because it causes Renderer to freeze. -turnerj1
//            distanceField.setText(String.valueOf(renderer.getCameraDistance()));

            renderer.setProjectionType((ProjectionType)projComboBox.getSelectedItem());

            // Set camera position latitude/longitude fields
            try
            {
                // Compute camera position
                double latitude = Double.parseDouble(cameraLatitudeField.getText());
                double longitude = Double.parseDouble(cameraLongitudeField.getText());
                renderer.setCameraLatLon(new LatLon(latitude, longitude));
            }
            catch (NumberFormatException ex)
            {
            }

            // Set camera attitude
            try
            {       
                // Point camera Nadir (toward origin) with specified roll angle
//              renderer.setCameraFocalPoint(new double[] {0,0,0});
              double roll = Double.parseDouble(cameraRollField.getText());
              renderer.setCameraRoll(roll);
            }
            catch (NumberFormatException ex)
            {
            }

            // Set camera view point
            try {
                GenericPolyhedralModel model = renderer.getGenericPolyhedralModel();
                double altitude = Double.valueOf(viewPointAltitudeField.getText());
                LatLon viewpointLatLong = new LatLon(Double.valueOf(viewPointLatitiudeField.getText()), Double.valueOf(viewPointLongitudeField.getText()));
                double[] pos = MathUtil.latrec(viewpointLatLong.toRadians());
                double[] viewDirection = new double[3];
                double[] origin = new double[3];
                MathUtil.unorm(pos, pos);
                MathUtil.vscl(JupiterScale, pos, origin);
                MathUtil.vscl(-1.0, pos, viewDirection);
//                pos[0] *= altitude;
//                pos[1] *= altitude;
//                pos[2] *= altitude;
                int result = model.computeRayIntersection(origin, viewDirection, pos);
                double radius = MathUtil.vnorm(pos);
//                System.out.println("RadiusPoint: " + radius);
                MathUtil.unorm(pos, pos);
                MathUtil.vscl(radius + altitude, pos, pos);
                renderer.setCameraFocalPoint(pos);
                renderer.setViewPointLatLong();
            } 
            catch (Exception e2)
            {
            }
        }
        else if (e.getSource() == resetButton)
        {
            renderer.resetToDefaultCameraViewAngle();
// REMOVED because it causes Renderer to freeze. -turnerj1
//            fovField.setText(String.valueOf(renderer.getCameraViewAngle()));
        }

        if (e.getSource() == okayButton || e.getSource() == cancelButton)
        {
            super.setVisible(false);
        }
    }

    public void setVisible(boolean b)
    {
      setTitle("Camera");

      fovField.setText(String.valueOf(renderer.getCameraViewAngle()));
      calculateCameraRadius();
//      System.out.println("Camera distance: " + renderer.getCameraDistance() + " cameraRadius: " + cameraRadius);
      cameraAltitudeField.setText(String.valueOf(renderer.getCameraDistance() - cameraRadius));
      projComboBox.setSelectedItem(renderer.getProjectionType());
      LatLon cameraLatLon = renderer.getCameraLatLon();
      cameraLatitudeField.setText(String.valueOf(cameraLatLon.lat));
      cameraLongitudeField.setText(String.valueOf(cameraLatLon.lon));
      cameraRollField.setText(String.valueOf(renderer.getCameraRoll()));
      
      LatLon viewPointLatLong = MathUtil.reclat(renderer.getCameraFocalPoint()).toDegrees();
      String lat = String.valueOf(viewPointLatLong.lat);
      String lon = String.valueOf(viewPointLatLong.lon);
      
      calculateViewRadius();
      if(nadirFocalPoint){
          System.out.println("11");
          viewPointLatitiudeField.setText("");
          viewPointLongitudeField.setText("");
          lat = String.valueOf(cameraLatLon.lat);
          lon = String.valueOf(cameraLatLon.lon);
      }
      double[] focalPoint = renderer.getCameraFocalPoint();
      viewPointAltitudeField.setText(String.valueOf(MathUtil.vnorm(focalPoint) - viewRadius ));
      viewPointLatitiudeField.setText(lat);
      viewPointLongitudeField.setText(lon);

      super.setVisible(b);
    }
    
    private void calculateCameraRadius(){
      GenericPolyhedralModel model = renderer.getGenericPolyhedralModel();
      LatLon cameraLatLong = renderer.getCameraLatLon();
//    System.out.println("Lat: " + cameraLatLong.lat + " Lon: " + cameraLatLong.lon);
      double[] pos = MathUtil.latrec(cameraLatLong.toRadians());
      double[] viewDirection = new double[3];
      double[] origin = new double[3];
      MathUtil.unorm(pos, pos);
      MathUtil.vscl(JupiterScale, pos, origin);
      MathUtil.vscl(-1.0, pos, viewDirection);
//      pos[0] *= altitude;
//      pos[1] *= altitude;
//      pos[2] *= altitude;
      int result = model.computeRayIntersection(origin, viewDirection, pos);
      cameraRadius = MathUtil.vnorm(pos);
  }
  
  private void calculateViewRadius(){
      GenericPolyhedralModel model = renderer.getGenericPolyhedralModel();
      LatLon viewPointLatLong = MathUtil.reclat(renderer.getCameraFocalPoint()).toDegrees();
//      System.out.println(viewPointLatLong.lat + " " + viewPointLatLong.lon);
//      System.out.println(MathUtil.latrec(viewPointLatLong.toRadians())[0] + MathUtil.latrec(viewPointLatLong.toRadians())[1] + MathUtil.latrec(viewPointLatLong.toRadians())[2]);
      double[] pos = renderer.getCameraFocalPoint();
      if(MathUtil.vnorm(pos) == 0){
          pos = renderer.getCameraPosition();
          nadirFocalPoint = true;
      }
      //pos = renderer.getCameraFocalPoint(); //MathUtil.latrec(viewPointLatLong.toRadians());
      double[] viewDirection = new double[3];
      double[] origin = new double[3];
      MathUtil.unorm(pos, pos);
      MathUtil.vscl(JupiterScale, pos, origin);
      MathUtil.vscl(-1.0, pos, viewDirection);
//      pos[0] *= altitude;
//      pos[1] *= altitude;
//      pos[2] *= altitude;
      int result = model.computeRayIntersection(origin, viewDirection, pos);
      viewRadius = MathUtil.vnorm(pos);
  }
}
