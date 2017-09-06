package org.liaohailong.pdftestapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.liaohailong.library.json.JsonInterface;

/**
 * 学生
 * Created by LHL on 2017/9/6.
 */
@DatabaseTable(tableName = "student")
public class Student implements JsonInterface, Parcelable {
    @DatabaseField(unique = true, generatedId = true)
    private int id;
    @DatabaseField
    private String name;
    @DatabaseField
    private int sex = 0;
    @DatabaseField
    private int age = 18;

    public Student() {
        id = -1;
        name = "";
    }

    public Student(String name, int sex, int age) {
        this.name = name;
        this.sex = sex;
        this.age = age;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeInt(this.sex);
        dest.writeInt(this.age);
    }

    protected Student(Parcel in) {
        this.name = in.readString();
        this.sex = in.readInt();
        this.age = in.readInt();
    }

    public static final Creator<Student> CREATOR = new Creator<Student>() {
        @Override
        public Student createFromParcel(Parcel source) {
            return new Student(source);
        }

        @Override
        public Student[] newArray(int size) {
            return new Student[size];
        }
    };
}
