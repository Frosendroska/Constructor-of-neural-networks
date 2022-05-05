package org.hse.nnbuilder.services;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.hse.nnbuilder.nn.FeedForwardNN;
import org.hse.nnbuilder.nn.store.NeuralNetworkRepository;
import org.hse.nnbuilder.nn.store.NeuralNetworkStored;
import org.hse.nnbuilder.services.Nnmodification.NNBuildingResponse;
import org.hse.nnbuilder.services.Nnmodification.NNModificationResponse;
import org.hse.nnbuilder.services.Nnmodification.NetworkType;
import org.hse.nnbuilder.version_controller.GeneralNeuralNetwork;
import org.hse.nnbuilder.version_controller.GeneralNeuralNetworkRepository;
import org.springframework.beans.factory.annotation.Autowired;

@GrpcService
public class NNModificationService extends NNModificationServiceGrpc.NNModificationServiceImplBase {

    @Autowired
    private NeuralNetworkRepository neuralNetworkRepository;

    @Autowired
    private GeneralNeuralNetworkRepository generalNeuralNetworkRepository;

    @Override
    public void modifynn(Nnmodification.NNModificationRequest request,
                         StreamObserver<NNModificationResponse> responseObserver) {

        try {
            Long nnId = request.getNnId();
            if (request.hasAddLayer()) {
                NeuralNetworkStored loaded = neuralNetworkRepository.getById(nnId);
                loaded.getNeuralNetwork().addLayer(
                        request.getAddLayer().getIndex(), // i
                        request.getAddLayer().getLType() // lType
                );
            }
            if (request.hasDelLayer()) {
                NeuralNetworkStored loaded = neuralNetworkRepository.getById(nnId);
                loaded.getNeuralNetwork().delLayer(
                        request.getDelLayer().getIndex() // i
                );
            }
            if (request.hasChangeActivationFunction()) {
                NeuralNetworkStored loaded = neuralNetworkRepository.getById(nnId);
                loaded.getNeuralNetwork().changeActivationFunction(
                        request.getChangeActivationFunction().getIndex(), // i
                        request.getChangeActivationFunction().getF() // f
                );
            }
            if (request.hasChangeNumberOfNeuron()) {
                NeuralNetworkStored loaded = neuralNetworkRepository.getById(nnId);
                loaded.getNeuralNetwork().changeNumberOfNeuron(
                        request.getChangeNumberOfNeuron().getIndex(), // i
                        request.getChangeNumberOfNeuron().getNumber() // n
                );
            }
        } catch (IllegalArgumentException e) {
            NNModificationResponse responseWithError = NNModificationResponse
                    .newBuilder()
                    .setException(e.toString())
                    .build();
            responseObserver.onNext(responseWithError);
            responseObserver.onCompleted();
        }

        NNModificationResponse responseWithOk = NNModificationResponse
                .newBuilder()
                .build();
        responseObserver.onNext(responseWithOk);
        responseObserver.onCompleted();
    }

    @Override
    public void createnn(Nnmodification.NNBuildingRequest request,
                         StreamObserver<Nnmodification.NNBuildingResponse> responseObserver) {

        long nnId = 0;

        if (request.getNnType() == NetworkType.FF) {
            GeneralNeuralNetwork generalNeuralNetwork = new GeneralNeuralNetwork();
            //GeneralNeuralNetwork generalNeuralNetwork = generalNeuralNetworkRepository.getById(27L);
            generalNeuralNetworkRepository.save(generalNeuralNetwork);
            System.out.println("correct ID: " + generalNeuralNetwork.getId());

            FeedForwardNN ffnn = FeedForwardNN.buildDefaultFastForwardNN();
            NeuralNetworkStored nnStored = new NeuralNetworkStored(ffnn, generalNeuralNetwork);
            neuralNetworkRepository.save(nnStored);
            nnId = nnStored.getId();

           // generalNeuralNetworkRepository.save(generalNeuralNetwork);

            System.out.println("NeuralNetworkStored in general..:");
            GeneralNeuralNetwork temp = generalNeuralNetworkRepository.getById(generalNeuralNetwork.getId());
            if(temp.getNnversions() != null && !temp.getNnversions().isEmpty()) {
                for (NeuralNetworkStored i : temp.getNnversions()) {
                    System.out.println(i.getId());
                }
            }
            System.out.println("General in new stored: " + nnStored.generalNeuralNetwork.getId());

           generalNeuralNetworkRepository.save(generalNeuralNetwork);
       }

        // } else if (request.getNnType() == NetworkType.RNN) {
        //     RecurrentNN rnn = RecurrentNN.buildDefaultRecurrentNN();
        //     NeuralNetworkStored nnStored = new NeuralNetworkStored(rnn);
        //     neuralNetworkRepository.save(nnStored);
        //     nnId = nnStored.getId();
        //
        // } else if (request.getNnType() == NetworkType.LSTM) {
        //     LongShortTermMemoryNN lstmnn = LongShortTermMemoryNN.buildDefaultLongTermMemoryNN();
        //     NeuralNetworkStored nnStored = new NeuralNetworkStored(lstmnn);
        //     neuralNetworkRepository.save(nnStored);
        //     nnId = nnStored.getId();
        //
        // } else if (request.getNnType() == NetworkType.CNN) {
        //     ConvolutionalNN cnn = ConvolutionalNN.buildDefaultConvolutionalNN();
        //     NeuralNetworkStored nnStored = new NeuralNetworkStored(cnn);
        //     neuralNetworkRepository.save(nnStored);
        //     nnId = nnStored.getId();
        //
        // }

        NNBuildingResponse responseWithOk = NNBuildingResponse
                .newBuilder()
                .setNnId(nnId)
                .build();
        responseObserver.onNext(responseWithOk);
        responseObserver.onCompleted();
    }
}
