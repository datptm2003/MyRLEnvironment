package com.example.myrlenvironment.model.geometry;

import com.google.ar.sceneform.math.Vector3;

import java.util.ArrayList;
import java.util.List;

public class Triangle {
    private final Vector3 v1;
    private final Vector3 v2;
    private final Vector3 v3;

    public Triangle(Vector3 v1, Vector3 v2, Vector3 v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }

    public List<Vector3> getVertices() {
        List<Vector3> vertices = new ArrayList<>();
        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);
        return vertices;
    }
}
