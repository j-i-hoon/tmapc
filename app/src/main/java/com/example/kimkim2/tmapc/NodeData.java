package com.example.kimkim2.tmapc;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by kimkim2 on 2016-09-14.
 */
public class NodeData implements Serializable {
    String index;
    String nodeType;
    String coordinate;
    String turntype;

    public NodeData(String index,String nodeType,String coordinate,String turntype){ //Turntype = Point 일떄
        this.index=index;
        this.nodeType=nodeType;
        this.coordinate=coordinate;
        this.turntype=turntype;
    }
    public NodeData(String index,String nodeType,String coordinate){ //Turntype = Line 일때
        this.index=index;
        this.nodeType=nodeType;
        this.coordinate=coordinate;
    }

}
