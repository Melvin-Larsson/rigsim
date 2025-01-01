package org.example;

import java.awt.*;

import org.ejml.simple.*; 
import org.ejml.data.*;

public class Body {
	private SimpleMatrix position;
	private SimpleMatrix velocity;
	private float mass;
	private float radius;
	
	public Body(Point position, float mass, float radius) {
		this.position =  SimpleMatrix.wrap(new DMatrixRMaj(new double[] {position.x, position.y}));
		this.mass = mass;
		this.velocity = SimpleMatrix.filled(2, 1, 0);
		this.radius = radius;
	}
	
	public void applyForce(SimpleMatrix force, float duration){
		velocity = velocity.plus(force.scale(duration/mass));
	}
	
	public void step(float duration) {
		position = position.plus(velocity.scale(duration));
	}
	
	public Point getPosition() {
		return new Point(position.getIndex(0, 0), position.getIndex(0, 1));
	}
	
	public SimpleMatrix getPositionVector() {
		return position;
	}
	
	public float getRadius() {
		return radius;
	}

}
