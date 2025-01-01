package org.example;

import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;
import java.util.List;

public class RigidBodySystem {
    private SimpleMatrix system;
    private SimpleMatrix state;
    private SimpleMatrix masses;
    private SimpleMatrix forces;

    private SimpleMatrix W;

    private List<RigidBody> bodies;
    private List<Constraint> constraints;

    public RigidBodySystem(){
        this.bodies = new ArrayList<>();
        this.constraints = new ArrayList<>();
    }

    public void applyForce(RigidBody rigidBody, Vector2 force){
        this.forces.set(rigidBody.index * 2, 0, force.getX());
        this.forces.set(rigidBody.index * 2 + 1, 0, force.getY());
    }

    public RigidBody addRigidBody(Vector2 position, float radius, float mass){
        RigidBody rigidBody = new RigidBody(bodies.size(), position, radius, mass);
        bodies.add(rigidBody);

        return rigidBody;
    }

    public void addConstraint(Constraint constraint){
        this.constraints.add(constraint);
    }

    public void compile(){
        this.system = SimpleMatrix.filled(bodies.size() * 4, bodies.size() * 4, 0);
        this.masses = SimpleMatrix.filled(bodies.size() * 4, bodies.size() * 2, 0);
        this.forces = SimpleMatrix.filled(bodies.size() * 2, 1, 0);
        this.state = SimpleMatrix.filled(bodies.size() * 4, 1, 0);
        this.W = SimpleMatrix.filled(bodies.size() * 2, bodies.size() * 2, 0);

        int i = 0;
        for (RigidBody body : bodies){
            this.system.set(i, i + 2, 1);
            this.system.set(i + 1, i + 3, 1);

            this.masses.set(i + 2, i / 2, 1/body.mass);
            this.masses.set(i + 3, i / 2 + 1, 1/body.mass);

            this.state.set(i, 0, body.position.getX());
            this.state.set(i + 1, 0, body.position.getY());

            this.W.set(i / 2, i / 2, body.mass);
            this.W.set(i / 2 + 1, i / 2 + 1, body.mass);

            i += 4;
        }

        this.W = this.W.invert();
    }

    public void updateState(float time){
        SimpleMatrix J = SimpleMatrix.filled(this.constraints.size(), this.bodies.size() * 2, 0);
        SimpleMatrix J_ = SimpleMatrix.filled(this.constraints.size(), this.bodies.size() * 2, 0);

        int i = 0;
        for (Constraint constraint : constraints){
            SimpleMatrix row = constraint.derivative(this.state);
            SimpleMatrix row_ = constraint.sndDerivative(this.state);

            for (int j = 0; j < this.bodies.size(); j++){
                J.set(i, j, row.get(0,j));
                J_.set(i, j, row_.get(0,j));
            }
            i++;
        }
        SimpleMatrix dX = this.system.mult(this.state).plus(this.masses.mult(this.forces));

        SimpleMatrix dQ = SimpleMatrix.filled(this.bodies.size() * 2, 1, 0);

        for(int j = 0; j < this.bodies.size(); j++){
            dQ.set(j * 2, 0, dX.get(j * 4, 0));
            dQ.set(j * 2 + 1, 0, dX.get(j * 4 + 1, 0));
        }

        System.out.println(J_);
        System.out.println(dQ);

        SimpleMatrix right = J_.scale(-1).mult(dQ).minus(J.mult(W).mult(this.forces));
        SimpleMatrix left = J.mult(W).mult(J.transpose());
        SimpleMatrix Fc = left.solve(right);

        SimpleMatrix dX2 = this.system.mult(this.state).plus(this.W.mult(this.forces.plus(Fc)));
        this.state = this.state.plus(dX2.scale(time));
        updateBodies();
    }

    private void updateBodies(){
        int i = 0;
        for (RigidBody body : bodies){
            float x = (float)this.state.get(i, 0);
            float y = (float)this.state.get(i + 1, 0);
            body.position = new Vector2(x,y);

            i += 4;
        }

    }

    private void setVector(SimpleMatrix matrix, int col, int row, Vector2 vector){
       matrix.set(row * 2, col, vector.getX());
       matrix.set(row * 2 + 1, col, vector.getX());
    }

    private Vector2 getVector(SimpleMatrix matrix, int col, int row){
        float x = (float)matrix.get(row, col);
        float y = (float)matrix.get(row, col);
        return  new Vector2(x,y);
    }

    private int getExternalForceIndex(RigidBody rigidBody){
        return rigidBody.index * 2;
    }

    private int getConstraintForceIndex(RigidBody rigidBody){
        return rigidBody.index * 2 + 1;
    }

    private int getPositionIndex(RigidBody rigidBody){
        return rigidBody.index * 2;
    }

    private int getVelocityIndex(RigidBody rigidBody){
        return rigidBody.index * 2 + 1;
    }

    public class RigidBody {
        private int index;
        private Vector2 position;
        private Vector2 velocity;

        private float radius;
        private float mass;


        public RigidBody(int index, Vector2 position, float radius, float mass) {
            this.index = index;
            this.position = position;
            this.velocity = new Vector2(0,0);
            this.radius = radius;
            this.mass = mass;
        }

        public float getRadius() {
            return radius;
        }

        public Vector2 getPosition() {
            return position;
        }
    }
}
