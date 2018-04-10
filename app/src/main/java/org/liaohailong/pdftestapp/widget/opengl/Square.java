package org.liaohailong.pdftestapp.widget.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Describe as : OpenGL 模型：正方形
 * Created by LHL on 2018/4/10.
 */

public class Square implements GLDrawer {

    //定义顶点
    private float[] vertices = {
            -1.0f, 1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,
            1.0f, 1.0f, 0.0f
    };

    private short[] indices = {0, 1, 2, 0, 2, 3};

    //顶点缓存区
    private FloatBuffer vertexBuffer;

    //绘制顺序缓存区
    private ShortBuffer indexBuffer;

    Square() {
        //                                                  a float is 4 bytes
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(vertices).position(0);

        //                                      a short is 2 bytes
        byteBuffer = ByteBuffer.allocateDirect(indices.length * 2);
        byteBuffer.order(ByteOrder.nativeOrder());
        indexBuffer = byteBuffer.asShortBuffer();
        indexBuffer.put(indices).position(0);
    }

    FloatBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    @Override
    public void draw(GL10 gl) {
        //逆时针方向缠绕
        gl.glFrontFace(GL10.GL_CCW);

        //开启面的淘汰模式，不需要绘制的不绘制
        gl.glEnable(GL10.GL_CULL_FACE);

        //淘汰背面
        gl.glCullFace(GL10.GL_BACK);

        //激活顶点模式
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        //设置顶点坐标
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);

        //绘制元素
        gl.glDrawElements(GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_SHORT, indexBuffer);

        //取消顶点模式
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

        //绘制完毕，取消面淘汰模式
        gl.glEnable(GL10.GL_CULL_FACE);
    }
}
