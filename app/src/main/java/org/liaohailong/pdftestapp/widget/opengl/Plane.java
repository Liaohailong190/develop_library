package org.liaohailong.pdftestapp.widget.opengl;

import javax.microedition.khronos.opengles.GL10;

/**
 * Describe as : 平面 模型
 * Created by LHL on 2018/4/11.
 */

public class Plane extends Mesh {

    public Plane() {
        this(1, 1);
    }

    public Plane(float width, float height) {
        this(width, height, 1, 1);
    }

    public Plane(float width, float height, float widthSegments, float heightSegments) {
        float[] vertices = new float[(int) ((widthSegments + 1) * (heightSegments + 1) * 3)];
        short[] indices = new short[(int) ((widthSegments + 1) * (heightSegments + 1) * 6)];

        float xOffset = width / -2f;
        float yOffset = height / -2f;
        float xWidth = width / widthSegments;
        float yHeight = height / heightSegments;
        int currentVertex = 0;
        int currentIndex = 0;

        short w = (short) (widthSegments + 1);

        for (int y = 0; y < heightSegments + 1; y++) {
            for (int x = 0; x < widthSegments + 1; x++) {
                vertices[currentVertex] = xOffset + x * xWidth;
                vertices[currentVertex + 1] = yOffset + y * yHeight;
                vertices[currentVertex + 2] = 0;
                currentVertex += 3;

                int n = (int) (y * (widthSegments + 1) + x);
                if (y < heightSegments && x < widthSegments) {
                    //第一个面
                    indices[currentIndex] = (short) n;
                    indices[currentIndex + 1] = (short) (n + 1);
                    indices[currentIndex + 2] = (short) (n + w);

                    //第二个面
                    indices[currentIndex + 3] = (short) (n + 1);
                    indices[currentIndex + 4] = (short) (n + 1 + w);
                    indices[currentIndex + 5] = (short) (n + 1 + w - 1);

                    currentIndex += 6;
                }
            }
        }

        setVertices(vertices);
        setIndices(indices);
    }


    @Override
    public void draw(GL10 gl) {
        super.draw(gl);
    }
}
