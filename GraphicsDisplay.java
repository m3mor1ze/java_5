import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel {
    private final BasicStroke graphicsStroke;
    private final BasicStroke axisStroke;
    private final BasicStroke markerStroke;
    private final BasicStroke dashed = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{10, 10, 10, 10, 10, 10, 5, 10, 5, 10, 5, 10}, 0);
    private final Font axisFont;
    Point2D mousePoint, mousePoint2;
    double newY, newY2;
    int indexX, indexX2;
    private Double[][] graphicsData, graphicsData2;
    private boolean showAxis = true, showAxis2 = true;
    private boolean showMarkers = true, showMarkers2 = true;
    private double minX, minX2, maxX, maxX2, minY, minY2, maxY, maxY2, scale, scale2;
    private boolean isRotated = false, isRotated2 = false;
    private Point2D selectedPoint = null, selectedPoint2 = null;
    private boolean displaySecond = false;


    public GraphicsDisplay() {

        setBackground(Color.DARK_GRAY);
        graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, null, 0.0f);
        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        axisFont = new Font("Serif", Font.BOLD, 36);

        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent e) {
                try {
                    if (graphicsData != null) {

                        Point2D mousePoint = xyToValue(e.getX(), e.getY(), false);
                        Point2D nearestPoint = findNearestPoint(mousePoint, false);
                        selectedPoint = nearestPoint;
                    }
                    if (graphicsData2 != null) {
                        Point2D mousePoint2 = xyToValue(e.getX(), e.getY(), true);
                        Point2D nearestPoint2 = findNearestPoint(mousePoint2, true);
                        if (displaySecond) {
                            selectedPoint2 = nearestPoint2;
                        }

                    }
                } catch (Exception ignored) {

                }

            }

            @Override
            public void mouseDragged(MouseEvent event) {
                try {

                    mousePoint = xyToValue(event.getX(), event.getY(), false);
                    if (mousePoint != null) newY = mousePoint.getY();

                    mousePoint2 = xyToValue(event.getX(), event.getY(), true);
                    if (mousePoint2 != null) newY2 = mousePoint2.getY();

                    if (selectedPoint != null) selectedPoint = findNearestPoint(mousePoint, false);
                    if (selectedPoint2 != null) selectedPoint2 = findNearestPoint(mousePoint2, true);
                    if (selectedPoint != null && selectedPoint2 != null)
                        if ((selectedPoint.getX() < selectedPoint2.getX())) {
                            indexX = findIndexOfSelectedPointX(selectedPoint, false);
                            if (indexX > -1) {
                                graphicsData[indexX][1] = newY;
                            }
                        } else {
                            indexX2 = findIndexOfSelectedPointX(selectedPoint2, true);
                            if (indexX2 > -1) {
                                graphicsData2[indexX2][1] = newY2;
                            }
                        }

                    if (selectedPoint == null && selectedPoint2 != null) {
                        indexX2 = findIndexOfSelectedPointX(selectedPoint2, true);
                        if (indexX2 > -1) {
                            graphicsData2[indexX2][1] = newY2;
                        }
                    }

                    if (selectedPoint2 == null) {
                        indexX = findIndexOfSelectedPointX(selectedPoint, false);
                        if (indexX > -1) {
                            graphicsData[indexX][1] = newY;
                        }
                    }


                } catch (Exception ignored) {
                }
                repaint();
            }
        });

    }

    public void setDisplaySecond(boolean ch) {
        this.displaySecond = ch;
        repaint();
    }

    public void setRotation(boolean ch) {
        isRotated = ch;
        repaint();
    }

    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }

    public void setRotation2(boolean ch) {
        isRotated2 = ch;
        repaint();
    }

    public void setShowAxis2(boolean showAxis2) {
        this.showAxis2 = showAxis2;
        repaint();
    }

    public void setShowMarkers2(boolean showMarkers2) {
        this.showMarkers2 = showMarkers2;
        repaint();
    }

    public void showGraphics(Double[][] graphicsData, Double[][] graphicsData2) {
        if (graphicsData != null) {
            this.graphicsData = graphicsData;
        }
        if (graphicsData2 != null) {
            this.graphicsData2 = graphicsData2;
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graphicsData == null || graphicsData.length == 0) return;
        if (graphicsData == null || graphicsData.length == 0) return;


        updateScaleAndBounds(false);

        Graphics2D canvas = (Graphics2D) g;
        AffineTransform initialTransform = canvas.getTransform();
        if (isRotated) {
            AffineTransform rotateTransform = new AffineTransform();
            rotateTransform.rotate(-Math.PI / 2, getSize().getWidth() / 2, getSize().getHeight() / 2);
            canvas.setTransform(rotateTransform);
        }
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();
        if (showAxis) paintAxis(canvas, false);
        if (showMarkers) paintMarkers(canvas, false);

        paintGraphics(canvas, false);

        canvas.setTransform(initialTransform);
        drawCoordinates(canvas, false);


        if (displaySecond) {
            if (graphicsData2 == null || graphicsData2.length == 0) return;

            if (graphicsData2 == null || graphicsData2.length == 0) return;

            updateScaleAndBounds(true);
            if (isRotated2) {
                AffineTransform rotateTransform = new AffineTransform();
                rotateTransform.rotate(-Math.PI / 2, getSize().getWidth() / 2, getSize().getHeight() / 2);
                canvas.setTransform(rotateTransform);
            }

            if (showAxis2) paintAxis(canvas, true);
            if (showMarkers2) paintMarkers(canvas, true);
            paintGraphics(canvas, true);

            canvas.setTransform(initialTransform);
            drawCoordinates(canvas, true);
        }
        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);

    }

    private void paintGraphics(Graphics2D canvas, boolean ch) {
        GeneralPath graphics = new GeneralPath();
        canvas.setStroke(graphicsStroke);
        canvas.setColor(Color.RED);
        Double[][] gd = graphicsData.clone();
        if (ch) {
            gd = graphicsData2.clone();
            canvas.setStroke(dashed);
            canvas.setColor(Color.CYAN);
        }
        for (int i = 0; i < gd.length; i++) {
            Point2D.Double point = xyToPoint(gd[i][0], gd[i][1], ch);
            if (i > 0) {
                graphics.lineTo(point.getX(), point.getY());
            } else {
                graphics.moveTo(point.getX(), point.getY());
            }
        }
        canvas.draw(graphics);
        repaint();
    }

    private void drawCoordinates(Graphics g, boolean ch) {
        try {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Serif", Font.PLAIN, 14));
            int marginX = 10, marginY = getHeight() - 60;
            String coordinates = "";
            if (!ch) coordinates = "1: " + String.format("(%.2f, %.2f)", selectedPoint.getX(), selectedPoint.getY());
            if (ch) {
                coordinates = "2: " + String.format("(%.2f, %.2f)", selectedPoint2.getX(), selectedPoint2.getY());
                marginY = marginY + 30;
            }
            g.drawString(coordinates, marginX, marginY);
        } catch (Exception ignored) {

        }
    }

    private void paintMarkers(Graphics2D canvas, boolean ch) {
        canvas.setStroke(markerStroke);
        Double[][] gd = graphicsData.clone();
        if (ch) gd = graphicsData2.clone();
        for (Double[] point : gd) {
            double x = point[0];
            double y = point[1];

            Point2D.Double center = xyToPoint(x, y, false);
            if (ch) center = xyToPoint(x, y, true);
            int integerPart = (int) y;
            int sumOfDigits = 0;
            while (integerPart != 0) {
                sumOfDigits += integerPart % 10;
                integerPart /= 10;
            }
            if (sumOfDigits < 10) canvas.setColor(Color.green);
            else canvas.setColor(Color.BLACK);
            Ellipse2D.Double marker = new Ellipse2D.Double(center.getX() - 5.5, center.getY() - 5.5, 11, 11);
            canvas.draw(marker);
            canvas.drawLine((int) center.getX(), (int) center.getY() - 5, (int) center.getX(), (int) center.getY() + 5);
            canvas.drawLine((int) center.getX() - 5, (int) center.getY(), (int) center.getX() + 5, (int) center.getY());
        }
    }

    private void paintAxis(Graphics2D canvas, boolean ch) {
        canvas.setStroke(axisStroke);
        canvas.setColor(Color.green);
        canvas.setPaint(Color.BLACK);
        canvas.setFont(axisFont);
        FontRenderContext context = canvas.getFontRenderContext();

        double tmp_maxX = 0, tmp_minX = 0, tmp_maxY = 0, tmp_minY = 0;
        String axisLabel = "1";
        if (!ch) {
            tmp_maxX = maxX;
            tmp_minX = minX;
            tmp_maxY = maxY;
            tmp_minY = minY;
        }
        if (ch) {
            tmp_maxX = maxX2;
            tmp_minX = minX2;
            tmp_maxY = maxY2;
            tmp_minY = minY2;
            axisLabel = "2";
        }

        if (tmp_minX <= 0.0 && tmp_maxX >= 0.0) {
            canvas.draw(new Line2D.Double(xyToPoint(0, tmp_maxY, ch), xyToPoint(0, tmp_minY, ch)));
            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(0, tmp_maxY, ch);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX() + 5, arrow.getCurrentPoint().getY() + 20);
            arrow.lineTo(arrow.getCurrentPoint().getX() - 10, arrow.getCurrentPoint().getY());
            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);
            Rectangle2D bounds = axisFont.getStringBounds("y2", context);
            Point2D.Double labelPos = xyToPoint(0, tmp_maxY, ch);
            canvas.drawString("y" + axisLabel, (float) labelPos.getX() + 10, (float) (labelPos.getY() - bounds.getY()));
        }

        if (tmp_minY <= 0.0 && tmp_maxY >= 0.0) {
            canvas.draw(new Line2D.Double(xyToPoint(tmp_minX, 0, ch), xyToPoint(tmp_maxX, 0, ch)));
            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(tmp_maxX, 0, ch);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX() - 20, arrow.getCurrentPoint().getY() - 5);
            arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY() + 10);
            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);
            Rectangle2D bounds = axisFont.getStringBounds("x2", context);
            Point2D.Double labelPos = xyToPoint(tmp_maxX, 0, ch);
            canvas.drawString("x" + axisLabel, (float) (labelPos.getX() - bounds.getWidth() - 10), (float) (labelPos.getY() + bounds.getY()));
        }
    }


    public void saveToGraphicsFile(File selectedFile, boolean ch) {
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(selectedFile));
            int len = 0;
            if (!ch) len = graphicsData2.length;
            if (ch) len = graphicsData.length;

            for (int i = 0; i < len; i++) {
                out.writeDouble((Double) getValueAt(i, 0, ch));
                out.writeDouble((Double) getValueAt(i, 1, ch));
            }

            out.close();
        } catch (Exception e) {
        }
    }

    private Object getValueAt(int row, int col, boolean ch) {
        if (!ch) return graphicsData[row][col];
        return graphicsData2[row][col];
    }

    private Point2D xyToValue(double x, double y, boolean ch) {
        try {
            double xCenter = getSize().getWidth() / 2;
            double yCenter = getSize().getHeight() / 2;
            double tmp_scale = 0, tmp_maxX = 0, tmp_minX = 0, tmp_maxY = 0, tmp_minY = 0;
            boolean tmp_rotation = false;
            if (!ch) {
                tmp_scale = scale;
                tmp_maxX = maxX;
                tmp_minX = minX;
                tmp_maxY = maxY;
                tmp_minY = minY;
                tmp_rotation = isRotated;
            }
            if (ch) {
                tmp_scale = scale2;
                tmp_maxX = maxX2;
                tmp_minX = minX2;
                tmp_maxY = maxY2;
                tmp_minY = minY2;
                tmp_rotation = isRotated2;
            }
            double xValue = (x - xCenter) / tmp_scale + (tmp_maxX + tmp_minX) / 2;
            double yValue = (yCenter - y) / tmp_scale + (tmp_maxY + tmp_minY) / 2;
            if (tmp_rotation) {
                AffineTransform rotateTransform = new AffineTransform();
                rotateTransform.rotate(-Math.PI / 2, xCenter, yCenter);
                Point2D screenPoint = new Point2D.Double(x, y);
                try {
                    rotateTransform.inverseTransform(screenPoint, screenPoint);
                } catch (NoninvertibleTransformException ex) {
                }
                xValue = (screenPoint.getX() - xCenter) / tmp_scale + (tmp_maxX + tmp_minX) / 2;
                yValue = (yCenter - screenPoint.getY()) / tmp_scale + (tmp_maxY + tmp_minY) / 2;
            }
            return new Point2D.Double(xValue, yValue);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Point2D findNearestPoint(Point2D mousePoint, boolean ch) {
        try {
            double minDistance = Double.MAX_VALUE;
            Point2D nearestPoint = null;
            Double[][] gd = graphicsData.clone();
            if (ch) gd = graphicsData2.clone();
            for (Double[] point : gd) {
                double x = point[0];
                double y = point[1];

                if (Math.abs(Math.abs(mousePoint.getY()) - Math.abs(y)) <= 0.7)
                    if (Math.abs(Math.abs(mousePoint.getX()) - Math.abs(x)) <= 0.7) {
                        double distance = mousePoint.distance(x, y);
                        if (distance < minDistance) {
                            minDistance = distance;
                            nearestPoint = new Point2D.Double(x, y);
                        }
                    }
            }
            return nearestPoint;
        } catch (Exception ignored) {
            return null;
        }
    }

    private Point2D.Double xyToPoint(double x, double y, boolean ch) {
        try {
            if (!ch) return new Point2D.Double((x - minX) * scale, (maxY - y) * scale);
            return new Point2D.Double((x - minX2) * scale2, (maxY2 - y) * scale2);
        } catch (Exception ignored) {
            return null;
        }
    }

    private int findIndexOfSelectedPointX(Point2D selectedPoint, boolean ch) {
        Double[][] gd = graphicsData.clone();
        if (ch) {
            gd = graphicsData2.clone();
        }
        for (int i = 0; i < gd.length; i++) {
            if (selectedPoint.equals(new Point2D.Double(gd[i][0], gd[i][1]))) {
                return i;
            }
        }
        return -1;
    }

    private void updateScaleAndBounds(boolean isSecondGraph) {
        if (!isSecondGraph) {
            minX = graphicsData[0][0];
            maxX = graphicsData[graphicsData.length - 1][0];
            minY = graphicsData[0][1];
            maxY = minY;


            for (int i = 1; i < graphicsData.length; i++) {
                if (graphicsData[i][1] < minY) {
                    minY = graphicsData[i][1];
                }
                if (graphicsData[i][1] > maxY) {
                    maxY = graphicsData[i][1];
                }
            }
            double scaleX = getSize().getWidth() / (maxX - minX);
            double scaleY = getSize().getHeight() / (maxY - minY);
            scale = Math.min(scaleX, scaleY);
            if (scale == scaleX) {
                double yIncrement = (getSize().getHeight() / scale - (maxY - minY)) / 2;
                maxY += yIncrement;
                minY -= yIncrement;
            }
            if (scale == scaleY) {
                double xIncrement = (getSize().getWidth() / scale - (maxX - minX)) / 2;
                maxX += xIncrement;
                minX -= xIncrement;
            }
        }
        if (isSecondGraph) {
            minX2 = graphicsData2[0][0];
            maxX2 = graphicsData2[graphicsData2.length - 1][0];
            minY2 = graphicsData2[0][1];
            maxY2 = minY2;


            for (int i = 1; i < graphicsData2.length; i++) {
                if (graphicsData2[i][1] < minY2) {
                    minY2 = graphicsData2[i][1];
                }
                if (graphicsData2[i][1] > maxY2) {
                    maxY2 = graphicsData2[i][1];
                }
            }
            double scaleX = getSize().getWidth() / (maxX2 - minX2);
            double scaleY = getSize().getHeight() / (maxY2 - minY2);
            scale2 = Math.min(scaleX, scaleY);
            if (scale2 == scaleX) {
                double yIncrement = (getSize().getHeight() / scale2 - (maxY2 - minY2)) / 2;
                maxY2 += yIncrement;
                minY2 -= yIncrement;
            }
            if (scale2 == scaleY) {
                double xIncrement = (getSize().getWidth() / scale2 - (maxX2 - minX2)) / 2;
                maxX2 += xIncrement;
                minX2 -= xIncrement;
            }
        }

    }


}