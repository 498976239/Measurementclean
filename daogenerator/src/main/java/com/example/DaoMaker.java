package com.example;



import java.io.IOException;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class DaoMaker {
    public static void main(String[] args){
        //生成数据库的实体类XXentity  对应的是数据库的表
        Schema schema = new Schema(1,"com.bean.entity");//第一个参数是数据库的版本号，第二个参数是填写默认的包名
        addBean(schema);
        schema.setDefaultJavaPackageDao("com.bean.dao");//设置数据的访问层，会在这里面生成一些操作数据库的文件
        try {
            //指定生成的路径
            new DaoGenerator().generateAll(schema,"D:\\AndroidStudioProgram\\Measurement\\app\\src\\main\\java-gen");
        } catch (Exception e) {
                e.printStackTrace();
        }
    }
    //创建数据库的表
    public static void addBean(Schema schema){
        Entity entity = schema.addEntity("Bean");//创建数据库的表，简单的理解entity就是对应一个具体的java对象，或者是一张表
        entity.addIdProperty();//主键，int类型
        entity.addStringProperty("name");//对应数据库的列
        entity.addStringProperty("data1");//对应数据库的列
        entity.addStringProperty("data2");//对应数据库的列
        entity.addStringProperty("data3");//对应数据库的列
        entity.addStringProperty("timeDetail");
        entity.addDateProperty("now");//对应数据库的列

    }
}
