package com.company;

/*
   This applet scrolls the message "hello world (for absolutely the last time)!" across
   the screen.
*/

import java.awt.*;
import java.applet.Applet;

public class ScrollingHelloWorld extends Applet implements  Runnable {

    Thread runner;  // a thread that is responsible for cycling the message colors.

    private final static int GO = 0,           // Constants for use as value of status.
            SUSPEND = 1,
            TERMINATE = 2;

    private volatile int status ;   // This variable is used for communication between
    // the applet and the thread.  The value is set by
    // the applet to tell the thread what to do.

    String message = "Hello World (for absolutely the last time)!";  // message to be scrolled


    int messagePosition = -1;   // Current position of the left end of the message, given as the
    //   distance in pixels from the left edge of the applet.
    //   In each frame, this is incremented by one character width
    //   until the message has scrolled completely off the applet.
    //   Then it is reset to zero.  The value of -1 indicates that
    //   the scrolling has not yet begun.

    Font messageFont;   // Font used to display the message.

    int messageHeight;  // Data about size of message and of a single character.
    int messageWidth;
    int charWidth;


    /* Some variable used for double-buffering */

    Image OSC;  // The off-screen canvas (created and used in update()).

    int widthOfOSC, heightOfOSC;  // Current widht and height of OSC.  These
    // are checked against the size of the applet,
    // to detect any change in the applet's size.
    // If the size has changed, a new OSC is created.


    public void init() {
        setBackground(Color.white);
        messageFont = new Font("Monospaced", Font.BOLD, 30);
        FontMetrics fm = getFontMetrics(messageFont);
        messageWidth = fm.stringWidth(message);
        messageHeight = fm.getAscent();
        charWidth = fm.charWidth('H');
    }


    public void update(Graphics g) {
        // To implement double-buffering, the update method calls paint to
        // draw the contents of the applet on an off-screen canvas.  Then
        // the canvas is copied onto the screen.  This method is responsible
        // for creating the off-screen canvas.  It will make a new OSC if
        // the size of the applet changes.
        if (OSC == null || widthOfOSC != getSize().width || heightOfOSC != getSize().height) {
            // Create the OSC, or make a new one if applet size has changed.
            OSC = null;  // (If OSC already exists, this frees up the memory.)
            OSC = createImage(getSize().width, getSize().height);
            widthOfOSC = getSize().width;
            heightOfOSC = getSize().height;
        }
        Graphics OSG = OSC.getGraphics();  // Graphics context for drawing to OSC.
        OSG.setColor(getBackground());
        OSG.fillRect(0, 0, widthOfOSC, heightOfOSC);
        OSG.setColor(getForeground());
        OSG.setFont(getFont());
        paint(OSG);  // Draw applet contents to OSC.
        g.drawImage(OSC,0,0,this);  // Copy OSC to screen.
    }


    synchronized public void paint(Graphics g) {
        // Draw the current frame.
        if (messagePosition > 0) {  // (Otherwise, no frame has yet been computed.)
            g.setColor(Color.red);
            g.setFont(messageFont);
            g.drawString(message, getSize().width - messagePosition, getSize().height/2 + messageHeight/2);
        }
    }


    synchronized public void start() {
        // Called when the applet is being started or restarted.
        // Create a new thread or restart the existing thread.
        status = GO;
        if (runner == null || ! runner.isAlive()) {  // Thread doens't yet exist or has died for some reason.
            runner = new Thread(this);
            runner.start();
        }
        else
            notify();
    }


    synchronized public void stop() {
        // Called when the applet is about to be stopped.
        // Suspend the thread.
        status = SUSPEND;
        notify();
    }


    synchronized public void destroy() {
        // Called when the applet is about to be permanently destroyed;
        // Stop the thread.
        status = TERMINATE;
        notify();
    }


    synchronized void nextFrame() {
        // Compute and display the next frame of the animation.  Called by run().
        messagePosition += charWidth;
        if (getSize().width - messagePosition + messageWidth < 0)  // message has moved off left edge
            messagePosition = 0;
        repaint();
    }


    public void run() {
        // The run method scrolls the image while the value of status is GO,
        // and it ends when the value of status becomes TERMINATE.  If the
        // status is SUSPEND, the thread will pause until the notify()
        // method is called by another thread.
        while (status != TERMINATE) {
            synchronized(this) {
                while (status == SUSPEND)
                    waitDelay();
            }
            if (status == GO)
                nextFrame();
            if (status == GO)
                waitDelay(250);
        }
    } // end run()


    synchronized void waitDelay(int milliseconds) {
        // Pause for the specified number of milliseconds
        // OR until the notify() method is called by some other thread.
        try {
            wait(milliseconds);
        }
        catch (InterruptedException e) {
        }
    }

    synchronized void waitDelay() {
        // Pause until the notify() method is called by some other thread.
        try {
            wait();
        }
        catch (InterruptedException e) {
        }
    }


} // end class ScrollingHelloWorld