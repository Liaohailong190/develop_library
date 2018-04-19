package org.liaohailong.pdftestapp.widget.opengl;

import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;

/**
 * Describe as : 管理多个空间几何形体
 * Created by LHL on 2018/4/11.
 */

public class Group extends Mesh{

    private Vector<Mesh> children = new Vector<>();

    public void add(int location,Mesh object){
        children.add(location,object);
    }

    public void add(Mesh object){
        children.add(object);
    }

    public void clear(){
        children.clear();
    }

    public Mesh get(int location){
        return children.get(location);
    }

    public void remove(int location){
        children.remove(location);
    }

    public void remove(Mesh object){
        children.remove(object);
    }

    public int size(){
        return children.size();

    }

    @Override
    public void draw(GL10 gl) {
        for (Mesh child : children) {
            child.draw(gl);
        }
    }
}
