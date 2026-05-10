package com.josse.tools;

public interface IIntersectable {
    Intersection getIntersection(Ray ray);
}


//La razon por la que es una interfaz es para permitir que diferentes tipos de objetos 3D puedan ser intersectados por rayos sin necesidad de heredar de 
// una clase base común. Esto proporciona flexibilidad y permite que cualquier clase que implemente IIntersectable pueda ser utilizada en el proceso de 
// ray tracing, independientemente de su jerarquía de clases.