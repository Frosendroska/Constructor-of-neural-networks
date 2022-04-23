package org.hse.nnbuilder.nn;

import java.io.Serializable;
import org.hse.nnbuilder.services.Nnmodification.ActivationFunction;
import org.hse.nnbuilder.services.Nnmodification.LayerType;

public final class Layer implements Serializable {

    /* List of neurons on this layer */
    private int neurons;
    /* Layer type */
    private final LayerType layerType;
    /* Function for activation */
    private ActivationFunction activationFunction;

    public Layer() {
        this.layerType = LayerType.Undefined;
    }

    public Layer(int n, ActivationFunction f, LayerType t) {
        neurons = n;
        layerType = t;
        activationFunction = f;
    }

    public Layer(int n, LayerType t) {
        this(n, ActivationFunction.None, t);
    }

    public Layer(LayerType t) {
        this(1, ActivationFunction.None, t);
    }

    public LayerType getLayerType() {
        return layerType;
    }

    public ActivationFunction getActivationFunction() {
        return activationFunction;
    }

    public int getNeurons() {
        return neurons;
    }

    /**
     * @param n Current number of neurons in this layer
     * Set new number if neurons
     */
    public void changeNumberOfNeuron(int n) {
        assert (n > 0);
        neurons = n;
    }

    /**
     * @param f New function
     * Set another activation function
     */
    public void setActivationFunction(ActivationFunction f) {
        activationFunction = f;
    }
}
