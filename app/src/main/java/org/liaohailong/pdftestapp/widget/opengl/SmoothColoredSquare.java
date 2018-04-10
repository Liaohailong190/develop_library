package org.liaohailong.pdftestapp.widget.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Describe as : 渐变方块
 * Created by LHL on 2018/4/10.
 */

public class SmoothColoredSquare extends Square {


    private FloatBuffer colorBuffer;

    public void setColors(float[] colors) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(colors.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        colorBuffer = byteBuffer.asFloatBuffer();
        colorBuffer.put(colors).position(0);
    }

    @Override
    public void draw(GL10 gl) {
        //设置顶点坐标
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, getVertexBuffer());
        //激活颜色数组
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        //填入颜色值
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);
        super.draw(gl);
        //还原颜色操作
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
    }
}
