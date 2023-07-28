package com.cegesoft.ai.models;

import com.cegesoft.game.position.BoardPosition;
import com.cegesoft.game.position.FullPosition;
import org.tensorflow.Graph;
import org.tensorflow.Result;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.ndarray.NdArrays;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.buffer.DataBuffers;
import org.tensorflow.op.Ops;
import org.tensorflow.op.core.Placeholder;
import org.tensorflow.op.core.Variable;
import org.tensorflow.op.math.Add;
import org.tensorflow.op.nn.Relu;
import org.tensorflow.types.TFloat32;

import java.io.Closeable;
import java.io.IOException;

public class Model implements Closeable {
    private final Graph graph;
    private final Ops ops;
    private final Placeholder<TFloat32> input;
    private final Add<TFloat32> outputLayer;
    private final Session session;

    public Model() {
        this.graph = new Graph();
        this.ops = Ops.create(this.graph);
        this.input = ops.placeholder(TFloat32.class, Placeholder.shape(Shape.of(1, 32)));

        Variable<TFloat32> weights1 = ops.variable(ops.constant(new float[32][64]));
        Variable<TFloat32> biases1 = ops.variable(ops.constant(new float[64]));
        Variable<TFloat32> weights2 = ops.variable(ops.constant(new float[64][2]));
        Variable<TFloat32> biases2 = ops.variable(ops.constant(new float[2]));

        Relu<TFloat32> hiddenLayer = ops.nn.relu(ops.math.add(ops.linalg.matMul(input, weights1), biases1));
        this.outputLayer = ops.math.add(ops.linalg.matMul(hiddenLayer, weights2), biases2);

        this.session = new Session(graph);
    }

    public Float[] predict(float[] input) {
        try (TFloat32 inputTensor = TFloat32.tensorOf(NdArrays.wrap(Shape.of(1, 32), DataBuffers.of(input)))) {
            try (Result result = this.session.runner()
                    .feed(this.input.asOutput(), inputTensor)
                    .fetch(this.outputLayer.asOutput())
                    .run()) {
                return ((TFloat32) result.get(0)).streamOfObjects().toArray(Float[]::new);
            }
        }
    }

    public Float[] predict(BoardPosition position) {
        return this.predict(position.getPosition());
    }

    public void train(float[] input, float[] expected) {
        try (TFloat32 inputTensor = TFloat32.tensorOf(NdArrays.wrap(Shape.of(1, 32), DataBuffers.of(input)));
             TFloat32 expectedTensor = TFloat32.tensorOf(NdArrays.wrap(Shape.of(2), DataBuffers.of(expected)))) {
            this.session.runner()
                    .feed(this.input.asOutput(), inputTensor)
                    .feed(this.outputLayer.asOutput(), expectedTensor)
                    .addTarget(this.outputLayer.asOutput())
                    .run().close();
        }
    }

    public void train(FullPosition position) {
        this.train(position.getBoardPosition().getPosition(), position.getResults()[0].format());
    }

    public void save() throws IOException {
        SavedModelBundle.exporter("/model")
                .withSession(this.session)
                .export();
    }

    @Override
    public void close() {
        this.session.close();
        this.graph.close();
    }
}
