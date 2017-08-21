/*
 * The code is licensed under the LGPL Version 3 license http://www.gnu.org/licenses/lgpl-3.0.en.html.
 *
 * The collected imagery is protected & available under the CC BY-SA version 4 International license.
 * https://creativecommons.org/licenses/by-sa/4.0/legalcode.
 *
 * Copyright ©2017, Telenav, Inc. All Rights Reserved
 */
package org.openstreetmap.josm.plugins.openstreetcam.gui.details;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import org.openstreetmap.josm.plugins.openstreetcam.util.cnf.GuiConfig;
import com.telenav.josm.common.entity.Pair;
import com.telenav.josm.common.gui.builder.LabelBuilder;


/**
 * Displays the selected image.
 *
 * @author Beata
 * @version $Revision$
 */
class PhotoPanel extends JPanel implements MouseWheelListener {

    private static final long serialVersionUID = -1550900781158007580L;
    private static final int HALF = 2;
    private static final int MAX_ZOOM = 5;

    private transient BufferedImage image;

    /** a rectangle where the image is incorporated related to the outside panel */
    private Rectangle frame;

    /** a part of the image that is currently displayed */
    private Rectangle currentView;

    /** the image coordinate where the mouse dragging was started */
    private Point startPoint;

    /** the dimension of the panel, it is used to detect if the user had maximized or not the panel */
    private Dimension size;


    PhotoPanel() {
        super(new BorderLayout());
        setBackground(Color.white);
        setBorder(BorderFactory.createLineBorder(Color.gray));
        addMouseWheelListener(this);
        addMouseListener(new MousePressedAdapter());
        addMouseMotionListener(new MouseDraggedAdapter());
    }


    void updateUI(final BufferedImage image) {
        removeAll();
        this.image = image;
        initializeCurrentImageView();
        revalidate();
        repaint();
    }

    /**
     * Initialize the current view of the image with the image itself.
     */
    void initializeCurrentImageView() {
        if (image != null) {
            currentView = new Rectangle(0, 0, image.getWidth(), image.getHeight());
        } else {
            currentView = null;
            frame = null;
        }
    }

    void displayErrorMessage() {
        removeAll();
        setBackground(Color.white);
        image = null;
        currentView = null;
        add(LabelBuilder.build(GuiConfig.getInstance().getErrorPhotoLoadingText(), Font.BOLD, Color.white),
                BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    void displayLoadingMessage() {
        removeAll();
        setBackground(Color.white);
        image = null;
        currentView = null;
        add(LabelBuilder.build(GuiConfig.getInstance().getWarningLoadingPhoto(), Font.BOLD, Color.white),
                BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    @Override
    public void mouseWheelMoved(final MouseWheelEvent e) {
        if (image != null) {
            final Point point = getPointOnImage(e.getPoint());
            zoom(point.x, point.y, e.getWheelRotation());
            repaint();
        }
    }

    private void zoom(final int x, final int y, final int wheelRotation) {
        final Pair<Integer, Integer> vertical;
        final Pair<Integer, Integer> horizontal;

        if (getWidth() > getHeight()) {
            vertical = getImageFixedDimension(y, currentView.y, currentView.height, image.getHeight(), wheelRotation);
            if (vertical.getSecond() == image.getHeight() && vertical.getFirst() == 0) {
                horizontal = new Pair<>(0, image.getWidth());
            } else {
                final int newWidth = ((vertical.getSecond() - vertical.getFirst()) * getWidth()) / getHeight();
                horizontal = getImageRelativeDimension(x, currentView.x, currentView.width, newWidth, image.getWidth(),
                        wheelRotation);
            }
        } else {
            horizontal = getImageFixedDimension(x, currentView.x, currentView.width, image.getWidth(), wheelRotation);
            if (horizontal.getSecond() == image.getWidth() && horizontal.getFirst() == 0) {
                vertical = new Pair<>(0, image.getHeight());
            } else {
                final int newHeight = (horizontal.getSecond() - horizontal.getFirst()) * getHeight() / getWidth();
                vertical = getImageRelativeDimension(y, currentView.y, currentView.height, newHeight, image.getHeight(),
                        wheelRotation);
            }
        }

        if (wheelRotation > 0 || (wheelRotation <= 0
                && horizontal.getSecond() - horizontal.getFirst() > image.getWidth() / MAX_ZOOM
                && vertical.getSecond() - vertical.getFirst() > image.getHeight() / MAX_ZOOM)) {
            currentView = new Rectangle(horizontal.getFirst(), vertical.getFirst(),
                    horizontal.getSecond() - horizontal.getFirst(), vertical.getSecond() - vertical.getFirst());
        }
    }

    private Pair<Integer, Integer> getImageFixedDimension(final int ref, final int currentViewMinCoord,
            final int currentViewDim, final int imgDim, final int wheelRotation) {
        final Pair<Integer, Integer> pair;

        // zoom in case
        if (wheelRotation < 0) {
            pair = getImagePart(ref, currentViewMinCoord, currentViewMinCoord + currentViewDim,
                    (currentViewDim * 4 / 5) / HALF);
        } else {
            if (currentViewDim != imgDim) {
                final int dif = (currentViewDim * 5 / 4) / HALF;
                int minRef = currentViewMinCoord - dif;
                int maxRef = currentViewMinCoord + currentViewDim + dif;

                minRef = (minRef < 0) ? 0 : minRef;
                maxRef = (maxRef > imgDim) ? imgDim : maxRef;

                pair = getImagePart(ref, minRef, maxRef, dif + 1);
            } else {
                pair = new Pair<>(0, imgDim);
            }
        }
        return pair;
    }

    /**
     * @param ref is an integer representing the x or the y coordinate of the mouse position, which is taken into
     * account on the new image part computation
     * @param currentViewMinCoord is an integer representing the x or the y coordinate of current view left corner
     * @param currentViewDim is an integer representing the width or the height of the image part which is currently
     * displayed on the screen
     * @param newDim is an integer which should be the new image part dimension (width or height)
     * @param imgDim is an integer representing the image dimension (on 0x axis or 0y axis)
     * @param wheelRotation is an integer, its negative values signifying zoom in and positive values signifying zoom
     * out
     * @return a pair {@code Pair} containing the left corner x or y coordinate and the right corner x or y coordinate
     * of the new image part which will be displayed
     */
    private Pair<Integer, Integer> getImageRelativeDimension(final int ref, final int currentViewMinCoord,
            final int currentViewDim, final int newDim, final int imgDim, final int wheelRotation) {
        // zoom in case
        if (wheelRotation < 0) {
            final int dimension = (newDim > currentViewDim) ? currentViewDim : newDim;
            return getImagePart(ref, currentViewMinCoord, currentViewMinCoord + currentViewDim, dimension / HALF);
        }

        // zoom out case
        return getImagePart(ref, 0, imgDim, newDim / HALF);
    }

    private Pair<Integer, Integer> getImagePart(final int ref, final int currentViewMinCoord,
            final int currentViewMaxCoord, final int cut) {
        final int minCoord = ref - cut;
        final int maxCoord = ref + cut;

        int modifiedFirstRef = minCoord;
        int modifiedSecondRef = maxCoord;

        if (minCoord < currentViewMinCoord) {
            modifiedFirstRef = currentViewMinCoord;
            modifiedSecondRef = modifiedSecondRef - minCoord + currentViewMinCoord;
        }

        if (maxCoord > currentViewMaxCoord) {
            modifiedFirstRef = modifiedFirstRef - maxCoord + currentViewMaxCoord;
            modifiedSecondRef = currentViewMaxCoord;
        }

        modifiedFirstRef = modifiedFirstRef < currentViewMinCoord ? currentViewMinCoord : modifiedFirstRef;
        modifiedSecondRef = (modifiedSecondRef > currentViewMaxCoord) ? currentViewMaxCoord : modifiedSecondRef;
        return new Pair<>(modifiedFirstRef, modifiedSecondRef);
    }

    /**
     * Translate a panel coordinate to an image coordinate (avoid the "empty" space around image).
     *
     * @param point is a coordinate from the panel
     * @return a new point {@code Point} from the image coordinate system
     */
    private Point getPointOnImage(final Point point) {
        final int x = currentView.x + (point.x - frame.x) * currentView.width / frame.width;
        final int y = currentView.y + (point.y - frame.y) * currentView.height / frame.height;
        return new Point(x, y);
    }

    @Override
    public void paintComponent(final Graphics g) {
        // clean the panel
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        if (image != null) {
            // draw the image
            if (!getSize().equals(size)) {
                panelDimensionsChanged();
            } else {
                matchImageOnPanel();
            }
            g.drawImage(image, frame.x, frame.y, frame.x + frame.width, frame.y + frame.height, currentView.x,
                    currentView.y, currentView.x + currentView.width, currentView.y + currentView.height, null);
        }
        size = getSize();
    }

    /**
     * The method match on the panel the new part of the image.
     */
    private void matchImageOnPanel() {
        int imageWidth = getWidth();
        int imageHeight = (getWidth() * currentView.height) / currentView.width;
        int marginLeft = 0;
        int marginTop = (getHeight() - imageHeight) / HALF;
        if (imageHeight > getHeight()) {
            imageHeight = getHeight();
            imageWidth = (getHeight() * currentView.width) / currentView.height;
            marginLeft = (getWidth() - imageWidth) / HALF;
            marginTop = 0;
        }
        frame = new Rectangle(marginLeft, marginTop, imageWidth, imageHeight);
    }

    private void panelDimensionsChanged() {
        if ((currentView != null) && (currentView.width != image.getWidth() || currentView.height != image
                .getHeight())) {
            if (getWidth() != size.getWidth()) {
                final Pair<Integer, Integer> newDimension = getImagePart(currentView.x + currentView.width / HALF, 0,
                        image.getWidth(), (currentView.height * getWidth() / frame.height) / HALF);
                currentView.x = newDimension.getFirst();
                currentView.width = newDimension.getSecond() - newDimension.getFirst();

                if (currentView.height == image.getHeight()) {
                    frame.x = 0;
                    frame.width = getWidth();
                } else if (currentView.width < image.getWidth()) {
                    frame.x = 0;
                    frame.width = getWidth();
                } else {
                    matchImageOnPanel();
                }
            }
            if (getHeight() != size.getHeight()) {
                final Pair<Integer, Integer> newDimension = getImagePart(currentView.y + currentView.height / HALF, 0,
                        image.getHeight(), (currentView.width * getHeight() / frame.width) / HALF);
                currentView.y = newDimension.getFirst();
                currentView.height = newDimension.getSecond() - newDimension.getFirst();

                if (currentView.width == image.getWidth()) {
                    frame.height = getHeight();
                    frame.y = 0;
                } else if (currentView.height < image.getHeight()) {
                    frame.height = getHeight();
                    frame.y = 0;
                } else {
                    matchImageOnPanel();
                }
            }
        } else {
            matchImageOnPanel();
        }
    }

    private class MousePressedAdapter extends MouseAdapter {

        @Override
        public void mousePressed(final MouseEvent e) {
            if (image != null) {
                startPoint = getPointOnImage(e.getPoint());
            }
        }
    }


    private class MouseDraggedAdapter extends MouseMotionAdapter {

        @Override
        public void mouseDragged(final MouseEvent e) {
            if (image != null) {
                final Point endPoint = getPointOnImage(e.getPoint());
                moveCurrentView(startPoint.x - endPoint.x, startPoint.y - endPoint.y);
                repaint();
            }
        }

        private void moveCurrentView(final int xDif, final int yDif) {
            currentView.x = currentView.x + xDif;
            currentView.y = currentView.y + yDif;

            if (currentView.x < 0) {
                currentView.x = 0;
            }

            if (currentView.y < 0) {
                currentView.y = 0;
            }

            if (currentView.x + currentView.width > image.getWidth()) {
                currentView.x = image.getWidth() - currentView.width;
            }

            if (currentView.y + currentView.height > image.getHeight()) {
                currentView.y = image.getHeight() - currentView.height;
            }
        }
    }
}