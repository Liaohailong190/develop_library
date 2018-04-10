package org.liaohailong.pdftestapp.widget.opengl;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;

import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Describe as : OpenGL 绘制器
 * Created by LHL on 2018/4/10.
 */

public class OpenGLRenderer implements GLSurfaceView.Renderer {

    private List<GLDrawer> mGLDrawerList;

    public OpenGLRenderer() {
        this(new LinkedList<GLDrawer>());
    }

    public OpenGLRenderer(List<GLDrawer> glDrawerList) {
        mGLDrawerList = glDrawerList;
    }

    public void setGLDrawer(List<GLDrawer> newDrawer) {
        mGLDrawerList = newDrawer;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //设置背景颜色
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);

        //启用平滑阴影，默认不需要
        gl.glShadeModel(GL10.GL_SMOOTH);

        //深度缓冲区的设置
        gl.glClearDepthf(1.0f);

        //启用深度测试
        gl.glEnable(GL10.GL_DEPTH_TEST);

        //要做的深度测试的类型
        gl.glDepthFunc(GL10.GL_LEQUAL);

        //非常好的透视计算
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //将当前视图端口设置为新大小
        gl.glViewport(0, 0, width, height);

        //选择投影矩阵
        gl.glMatrixMode(GL10.GL_PROJECTION);

        //重置投影矩阵
        gl.glLoadIdentity();

        //计算窗口的长宽比
        float aspect = (width * 1f) / (height * 1f);
        GLU.gluPerspective(gl, 45.0f, aspect, 0.1f, 100f);

        //选择模型视图的矩阵
        gl.glMatrixMode(GL10.GL_MODELVIEW);

        //重置投影矩阵
        gl.glLoadIdentity();
    }

    private float angle = 0f;

    @Override
    public void onDrawFrame(GL10 gl) {

        //清除屏幕和深度缓冲区
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        //用恒等矩阵替换当前矩阵
        gl.glLoadIdentity();

        //位移画面位置
        gl.glTranslatef(0f, 0f, -12f);

        //绘制方块A
        gl.glPushMatrix();
        gl.glRotatef(angle, 0, 0, 1f);
        mGLDrawerList.get(0).draw(gl);
        gl.glPopMatrix();

        //绘制方块B
        gl.glPushMatrix();
        gl.glRotatef(-angle, 0, 0, 1f);
        gl.glTranslatef(2, 0, 0);
        gl.glScalef(0.5f, 0.5f, 0.5f);
        mGLDrawerList.get(1).draw(gl);


        gl.glPushMatrix();
        gl.glRotatef(-angle, 0, 0, 1f);
        gl.glTranslatef(2, 0, 0);
        gl.glScalef(0.5f, 0.5f, 0.5f);
        gl.glRotatef(angle * 10, 0, 0, 1f);
        mGLDrawerList.get(2).draw(gl);
        gl.glPopMatrix();

        gl.glPopMatrix();

        angle++;
    }
}
