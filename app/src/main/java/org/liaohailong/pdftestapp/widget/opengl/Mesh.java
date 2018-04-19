package org.liaohailong.pdftestapp.widget.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Describe as : 网络模型
 * Created by LHL on 2018/4/11.
 */

public class Mesh implements GLDrawer {

    //顶点坐标
    private FloatBuffer verticesBuffer = null;

    //绘制顺序
    private ShortBuffer indicesBuffer = null;

    //下标数量
    private int numOfIndices = -1;

    //平色
    private float[] rgba = new float[]{1.0f, 1.0f, 1.0f, 1.0f};

    //渐变色
    private FloatBuffer colorBuffer = null;

    //平移参数
    private float x = 0;
    private float y = 0;
    private float z = 0;

    //旋转参数
    private float rx = 0;
    private float ry = 0;
    private float rz = 0;

    void setVertices(float[] vertices) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        verticesBuffer = byteBuffer.asFloatBuffer();
        verticesBuffer.put(vertices).position(0);
    }

    void setIndices(short[] indices) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(indices.length * 2);
        byteBuffer.order(ByteOrder.nativeOrder());
        indicesBuffer = byteBuffer.asShortBuffer();
        indicesBuffer.put(indices).position(0);
        numOfIndices = indices.length;
    }

    public void setColor(float red, float green, float blue, float alpha) {
        rgba[0] = red;
        rgba[1] = green;
        rgba[2] = blue;
        rgba[3] = alpha;
    }

    public void setColors(float[] colors) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(colors.length * 4);
        colorBuffer = byteBuffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
        colorBuffer.put(colors).position(0);
    }

    @Override
    public void draw(GL10 gl) {
        //开启正面绘制
        gl.glFrontFace(GL10.GL_CCW);

        //开启淘汰面机制
        gl.glEnable(GL10.GL_CULL_FACE);

        //选择淘汰面模式
        gl.glCullFace(GL10.GL_BACK);

        //设置顶点坐标
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, verticesBuffer);

        //填充平色
        gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);

        if (colorBuffer != null) {
            //开启渐变色模式
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
            gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);
        }

        //设置平移
        gl.glTranslatef(x, y, z);
        //设置旋转
        gl.glRotatef(rx, 1, 0, 0);
        gl.glRotatef(ry, 0, 1, 0);
        gl.glRotatef(rz, 0, 0, 1);

        //绘制颜色
        gl.glDrawElements(GL10.GL_TRIANGLES, numOfIndices, GL10.GL_UNSIGNED_SHORT, indicesBuffer);

        //还原本次操作
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        gl.glDisable(GL10.GL_CULL_FACE);
    }
}
