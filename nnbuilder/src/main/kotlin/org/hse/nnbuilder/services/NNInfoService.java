package org.hse.nnbuilder.services;

import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;
import org.hse.nnbuilder.nn.Layer;
import org.hse.nnbuilder.nn.store.NeuralNetworkStorage;
import org.hse.nnbuilder.nn.store.NeuralNetworkStored;
import org.hse.nnbuilder.services.Nninfo.NNInfoRequest;
import org.hse.nnbuilder.services.Nninfo.NNInfoResponse;
import org.hse.nnbuilder.services.Nninfo.ProtoLayer;
import org.springframework.beans.factory.annotation.Autowired;

public class NNInfoService extends NNInfoServiceGrpc.NNInfoServiceImplBase {

    @Autowired
    private NeuralNetworkStorage neuralNetworkStorage;

    @Override
    public void getNNInfo(NNInfoRequest request, StreamObserver<NNInfoResponse> responseObserver) {
        Long nnId = request.getNnId();
        NeuralNetworkStored loaded = neuralNetworkStorage.getByIdOrThrow(nnId);

        responseObserver.onNext(NNInfoResponse.newBuilder()
                .setNnType(loaded.getNeuralNetwork().getNNType())
                .addAllLayers(NNInfoService.buildAvailableParts(
                        loaded.getNeuralNetwork().getLayers()))
                .setLearningRate(loaded.getNeuralNetwork().getLearningRate())
                .setDefaultNumberOfLayers(loaded.getNeuralNetwork().getDefaultNumberOfLayers())
                .build());
        responseObserver.onCompleted();
    }

    public static Iterable<? extends ProtoLayer> buildAvailableParts(List<Layer> layers) {
        List<ProtoLayer> newLayers = new ArrayList<>();
        for (var l : layers) {
            ProtoLayer curLayer = ProtoLayer.newBuilder()
                    .setNeurons(l.getNeurons())
                    .setLayerType(l.getLayerType())
                    .setActivationFunction(l.getActivationFunction())
                    .build();
            newLayers.add(curLayer);
        }
        return newLayers;
    }
}
