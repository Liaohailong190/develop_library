package org.liaohailong.pdftestapp.widget.opengl;


import javax.microedition.khronos.opengles.GL10;

/**
 * Describe as : 单色方块
 * Created by LHL on 2018/4/10.
 */

public class FlatColoredSquare extends Square {

    private float color[] = new float[4];

    public void setColor(float[] color) {
        this.color = color;
    }

    @Override
    public void draw(GL10 gl) {
        gl.glColor4f(color[0], color[1], color[2], color[3]);
        super.draw(gl);
    }
}
