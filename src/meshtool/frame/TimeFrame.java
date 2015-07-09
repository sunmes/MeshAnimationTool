/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package meshtool.frame;

import java.awt.Color;
import java.awt.Event;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class TimeFrame extends javax.swing.JPanel {

    final static Color $FontColor = new Color(0, 0, 0) //
            , $OddTimeFrameColor = new Color(160, 160, 160) //
            , $EvenTimeFrameColor = new Color(229, 229, 229) //
            , $OddLayerOddFrameColor = new Color(180, 180, 180) //
            , $OddLayerEvenFrameColor = new Color(200, 200, 200) //
            , $EvenLayerEvenFrameColor = new Color(240, 240, 240) //
            , $EvenLayerOddFrameColor = new Color(210, 210, 210)//
            , $CurrenFrameColor = new Color(248, 181, 81) //
            ;

    public interface FrameChangeEvent {

        void changeEvent(int currentFrame);
    }

    public class Frame {

        public Frame() {
        }

        public Frame(String nameString, float value) {
            this.name = nameString;
            this.defaultValue = value;
            this.currentValue = value;
            this.maxValue = value;
            this.currentFrame = 0;
            maxFrame = -1;
        }

        public String name;
        public float defaultValue;
        public float currentValue;
        public int currentFrame;
        public int maxFrame;
        public float maxValue;
        List<float[]> frames = new ArrayList<>();
        List<Integer> frameIndex = new ArrayList<>();

        public float getFrameValue(int frame) {
            if (frame == 0) {
                return defaultValue;
            }
            if (frame == currentFrame) {
                return currentValue;
            }
            if (frame >= maxFrame) {
                return maxValue;
            }
            currentFrame = 0;
            currentValue = defaultValue;
            for (float[] fs : frames) {
                if ((int) fs[0] >= frame) {
                    if ((int) fs[0] == frame) {
                        currentFrame = frame;
                        currentValue = fs[1];
                        break;
                    }
                    currentValue += (float) (fs[1] - currentValue) / (float) (fs[0] - currentFrame) * (frame - currentFrame);
                    currentFrame = frame;
                    break;
                } else {
                    currentFrame = (int) fs[0];
                    currentValue = fs[1];
                }
            }
            return currentValue;
        }

        public void addFrame(int frame, float value) {
            if (frame > maxFrame) {
                frames.add(new float[]{frame, value});
                frameIndex.add(frame);
                maxFrame = frame;
                maxValue = value;
            } else {
                for (int i = 0; i < frames.size(); i++) {
                    float[] fs = frames.get(i);
                    if ((int) fs[0] == frame) {
                        fs[1] = value;
                        return;
                    } else if ((int) fs[0] > frame) {
                        frames.add(i, new float[]{frame, value});
                        frameIndex.add(frame);
                        return;
                    }
                }
            }
        }

        public void deleteFrame(int frame) {
            for (float[] fs : frames) {
                if ((int) fs[0] == frame) {
                    if (frames.indexOf(fs) == frames.size() - 1) {
                        if (frames.size() == 1) {
                            maxFrame = 0;
                            maxValue = defaultValue;
                        } else {
                            float[] lastFrame = frames.get(frames.size() - 2);
                            maxFrame = (int) lastFrame[0];
                            maxValue = lastFrame[1];
                        }
                    }
                    frames.remove(fs);
                    frameIndex.remove(frame);
                    return;
                }
            }
        }

    }

    // 1f/60
    public int framePerSecond = 60;

    public int lastCurrentFrameIndex = 0;
    public int currentFrameIndex = 0;
    public int currentLayer = 0;

    public int offShowFrameIndex = 0;
    public int offShowFrame = 0;

    public int frameWidth = 10;
    public int layerHeight = 20;

    boolean moveDrag = false;
    int moveDragX = 0;

    private List<FrameChangeEvent> frameChangeEvents = new ArrayList<>();

    List<Frame> frames = new ArrayList<>();

    /**
     * Creates new form TimeFrame
     */
    public TimeFrame() {
        initComponents();
    }

    public void addFrameChangeEvent(FrameChangeEvent fe) {
        if (!frameChangeEvents.contains(fe)) {
            frameChangeEvents.add(fe);
        }
    }

    public void removeFrameChangeEvent(FrameChangeEvent fe) {
        if (frameChangeEvents.contains(fe)) {
            frameChangeEvents.remove(fe);
        }
    }

    public void clearFrameChangeEvent() {
        frameChangeEvents.clear();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g); //To change body of generated methods, choose Tools | Templates.

        g.setColor(Color.WHITE);
        g.clearRect(0, 0, this.getWidth(), this.getHeight());

        g.setColor(Color.GRAY);
        g.fillRect(0, 0, this.getWidth(), 20);

        int count = this.getWidth() / frameWidth + 1;
        int startIndex = 0;
        if (offShowFrame < 0) {
            count = (this.getWidth() - offShowFrame) / frameWidth + 1;
        } else if (offShowFrame > 0) {
            startIndex -= offShowFrame / frameWidth + 1;
        }

        //Draw TimeLine Frame
        for (int i = startIndex; i < count; i++) {
            if (currentFrameIndex == offShowFrameIndex + i) {
                g.setColor(Color.ORANGE);
                g.fillRect(i * frameWidth + offShowFrame, 0, frameWidth, 20);
            } else if ((offShowFrameIndex + i) % 2 == 0) {
                g.setColor(Color.DARK_GRAY);
                g.fillRect(i * frameWidth + offShowFrame, 0, frameWidth, 20);
            } else {

            }
        }

        //DrawFrames.
        for (int p = 0; p < frames.size(); p++) {
            Frame f = frames.get(p);
            if (p % 2 == 0) {
                for (int i = startIndex; i < count; i++) {
                    if (currentLayer == p && currentFrameIndex == i + offShowFrameIndex) {
                        g.setColor(Color.GREEN);
                    } else if ((offShowFrameIndex + i) % 2 == 0) {
                        g.setColor($EvenLayerOddFrameColor);
                    } else {
                        g.setColor($EvenLayerEvenFrameColor);
                    }
                    g.fillRect(i * frameWidth + offShowFrame, p * layerHeight + layerHeight, frameWidth, 20);

                    if (f.frameIndex.contains(i + offShowFrameIndex)) {
                        g.setColor(Color.red);
                        g.fillOval(i * frameWidth + offShowFrame + 2, p * layerHeight + layerHeight, 6, 20);
                    }
                }
            } else {
                for (int i = startIndex; i < count; i++) {
                    if (currentLayer == p && currentFrameIndex == i + offShowFrameIndex) {
                        g.setColor(Color.GREEN);
                    } else if ((offShowFrameIndex + i) % 2 == 0) {
                        g.setColor($OddLayerOddFrameColor);

                    } else {
                        g.setColor($OddLayerEvenFrameColor);
                    }
                    g.fillRect(i * frameWidth + offShowFrame, p * layerHeight + layerHeight, frameWidth, 20);

                    if (f.frameIndex.contains(i + offShowFrameIndex)) {
                        g.setColor(Color.red);
                        g.fillOval(i * frameWidth + offShowFrame + 2, p * layerHeight + layerHeight, 6, 20);
                    }
                }
            }
        }

        //Draw SelectLine
        g.setColor(Color.ORANGE);
        int framePointX = (currentFrameIndex - offShowFrameIndex + 1) * frameWidth - 6;
        g.fillRect(framePointX + offShowFrame, 0, 2, this.getHeight());

        //Draw FrameNumber
        g.setFont(g.getFont().deriveFont(Font.BOLD));
        g.setColor(Color.WHITE);
        for (int i = startIndex; i < count; i++) {
            if ((offShowFrameIndex + i) % 5 == 0) {
                g.drawString(String.valueOf(offShowFrameIndex + i), i * frameWidth + 1 + offShowFrame, 18);
            }
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                panelMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                panelMouseReleased(evt);
            }
        });
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                panelMouseDragged(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                panelMouseMoved(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 572, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 120, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    public void changeCurrentFrameIndex(int index) {
        currentFrameIndex = index;
        if (currentFrameIndex < 0) {
            currentFrameIndex = 0;
        }
        if (lastCurrentFrameIndex != currentFrameIndex) {
            lastCurrentFrameIndex = currentFrameIndex;
            for (FrameChangeEvent fe : frameChangeEvents) {
                fe.changeEvent(currentFrameIndex);
            }
        }
    }

    private void panelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panelMousePressed
        if (evt.getButton() == MouseEvent.BUTTON1) {
            int x = evt.getX();
            int y = evt.getY();
            changeCurrentFrameIndex(offShowFrameIndex + x / frameWidth + (x % frameWidth == 0 ? 0 : 1) - 1);

            currentLayer = y / layerHeight + (y % layerHeight == 0 ? 0 : 1) - 2;
            if (currentLayer < 0) {
                currentLayer = 0;
            } else if (currentLayer >= frames.size()) {
                currentLayer = frames.size() - 1;
            }
//            System.out.println(String.format("Y[%d] cl[%d]", y, currentLayer));
//            System.out.println(String.format("X[%d] cfi[%d]", x, currentFrameIndex));
            repaint();
        } else if (evt.getButton() == MouseEvent.BUTTON3) {
            moveDrag = true;
            moveDragX = evt.getX();
//            System.out.println("MoveDrag:" + moveDrag);
        }

    }//GEN-LAST:event_panelMousePressed

    private void panelMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panelMouseDragged
//        System.out.println("MouseDragged..Button:" + evt.getButton());
        if (moveDrag) {
            offShowFrame += (evt.getX() - moveDragX);
            if (offShowFrameIndex * frameWidth - offShowFrame < 0) {
                offShowFrameIndex = 0;
                offShowFrame = 0;
            }
//            System.out.println(String.format("off[%d] move[%d]", offShowFrameIndex, evt.getX() - moveDragX));
            moveDragX = evt.getX();
            repaint();
        } else {
            int x = evt.getX();
            int y = evt.getY();
            changeCurrentFrameIndex(offShowFrameIndex + x / frameWidth + (x % frameWidth == 0 ? 0 : 1) - 1);

            currentLayer = y / layerHeight + (y % layerHeight == 0 ? 0 : 1) - 2;
            if (currentLayer < 0) {
                currentLayer = 0;
            } else if (currentLayer >= frames.size()) {
                currentLayer = frames.size() - 1;
            }
//            System.out.println(String.format("Y[%d] cl[%d]", y, currentLayer));
//            System.out.println(String.format("X[%d] cfi[%d]", x, currentFrameIndex));
            repaint();
        }
    }//GEN-LAST:event_panelMouseDragged

    private void panelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panelMouseReleased
        if (evt.getButton() == MouseEvent.BUTTON3) {
            moveDrag = false;
//            System.out.println("MoveDrag:" + moveDrag);
            offShowFrameIndex -= offShowFrame / frameWidth;
            offShowFrame = 0;
            repaint();
        }
    }//GEN-LAST:event_panelMouseReleased

    private void panelMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panelMouseMoved
        //System.out.println("MouseMoved.");

    }//GEN-LAST:event_panelMouseMoved


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
