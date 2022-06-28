package org.hse.nnbuilder.services;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.hse.nnbuilder.modification_types.AddLayer;
import org.hse.nnbuilder.modification_types.ChangeActivationFunction;
import org.hse.nnbuilder.modification_types.ChangeNumberOfNeuron;
import org.hse.nnbuilder.modification_types.DelLayer;
import org.hse.nnbuilder.modification_types.Modification;
import org.hse.nnbuilder.nn.ConvolutionalNN;
import org.hse.nnbuilder.nn.FeedForwardNN;
import org.hse.nnbuilder.nn.Layer;
import org.hse.nnbuilder.nn.LongShortTermMemoryNN;
import org.hse.nnbuilder.nn.RecurrentNN;
import org.hse.nnbuilder.nn.store.NeuralNetworkStorage;
import org.hse.nnbuilder.nn.store.NeuralNetworkStored;
import org.hse.nnbuilder.services.Enums.ActionType;
import org.hse.nnbuilder.services.Enums.NetworkType;
import org.hse.nnbuilder.services.Nnmodification.ActivationFunction;
import org.hse.nnbuilder.services.Nnmodification.NNCreationResponse;
import org.hse.nnbuilder.services.Nnmodification.NNModificationRequest;
import org.hse.nnbuilder.services.Nnmodification.NNModificationResponse;
import org.hse.nnbuilder.user.User;
import org.hse.nnbuilder.user.UserService;
import org.hse.nnbuilder.version_controller.GeneralNeuralNetwork;
import org.hse.nnbuilder.version_controller.GeneralNeuralNetworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;

@GrpcService
public class NNModificationService extends NNModificationServiceGrpc.NNModificationServiceImplBase {

    @Autowired
    private NeuralNetworkStorage neuralNetworkStorage;

    @Autowired
    private GeneralNeuralNetworkService generalNeuralNetworkService;

    @Autowired
    private UserService userService;

    @Override
    public void modifynn(NNModificationRequest request, StreamObserver<NNModificationResponse> responseObserver) {
        Long nnId = request.getNnId();
        NeuralNetworkStored loaded = neuralNetworkStorage.getByIdOrThrow(nnId);

        Modification _modification = null;

        if (request.hasAddLayer()) {
            _modification = new AddLayer(
                    request.getAddLayer().getIndex(), request.getAddLayer().getLType());

            loaded.getNeuralNetwork()
                    .addLayer(
                            request.getAddLayer().getIndex(), // i
                            request.getAddLayer().getLType() // lType
                            );
        }
        if (request.hasDelLayer()) {
            int index = request.getDelLayer().getIndex();
            Layer deletedLayer = loaded.getNeuralNetwork().getLayers().get(index);
            _modification = new DelLayer(index, deletedLayer);

            loaded.getNeuralNetwork()
                    .delLayer(
                            request.getDelLayer().getIndex() // i
                            );
        }
        if (request.hasChangeActivationFunction()) {
            int index = request.getChangeActivationFunction().getIndex();
            ActivationFunction oldActivationFunction =
                    loaded.getNeuralNetwork().getLayers().get(index).getActivationFunction();
            ActivationFunction newActivationFunction =
                    request.getChangeActivationFunction().getF();
            _modification = new ChangeActivationFunction(index, oldActivationFunction, newActivationFunction);

            loaded.getNeuralNetwork()
                    .changeActivationFunction(
                            request.getChangeActivationFunction().getIndex(), // i
                            request.getChangeActivationFunction().getF() // f
                            );
        }
        if (request.hasChangeNumberOfNeuron()) {
            int index = request.getChangeActivationFunction().getIndex();
            long oldNumber = loaded.getNeuralNetwork().getLayers().get(index).getNeurons();
            long newNumber = request.getChangeNumberOfNeuron().getNumber();
            _modification = new ChangeNumberOfNeuron(index, oldNumber, newNumber);

            loaded.getNeuralNetwork()
                    .changeNumberOfNeuron(
                            request.getChangeNumberOfNeuron().getIndex(), // i
                            request.getChangeNumberOfNeuron().getNumber() // n
                            );
        }

        //loaded.getNeuralNetwork().addNewModification(modification);
        neuralNetworkStorage.save(loaded);

        NNModificationResponse responseWithOk =
                NNModificationResponse.newBuilder().build();
        responseObserver.onNext(responseWithOk);
        responseObserver.onCompleted();
    }

    @Secured("ROLE_USER")
    @Override
    public void createnn(
            Nnmodification.NNCreationRequest request,
            StreamObserver<Nnmodification.NNCreationResponse> responseObserver) {
        String userEmail =
                SecurityContextHolder.getContext().getAuthentication().getName();
        // String userEmail = "c";

        long nnId = creatennForUser(userEmail, request.getNnType(), request.getName(), request.getActionType());

        NNCreationResponse responseWithOk =
                NNCreationResponse.newBuilder().setNnId(nnId).build();
        responseObserver.onNext(responseWithOk);
        responseObserver.onCompleted();
    }

    public long creatennForUser(String userEmail, Enums.NetworkType nnType, String name, ActionType action) {
        User user = userService.findByEmail(userEmail);

        long nnId = 0;

        // creating new project
        GeneralNeuralNetwork generalNeuralNetwork = generalNeuralNetworkService.create(user, name, action);

        if (nnType == NetworkType.FF) {
            FeedForwardNN ffnn = FeedForwardNN.buildDefaultFastForwardNN();
            NeuralNetworkStored nnStored = new NeuralNetworkStored(ffnn, generalNeuralNetwork);
            neuralNetworkStorage.save(nnStored);
            nnId = nnStored.getNnId();
        } else if (nnType == NetworkType.RNN) {
            RecurrentNN rnn = RecurrentNN.buildDefaultRecurrentNN();
            NeuralNetworkStored nnStored = new NeuralNetworkStored(rnn, generalNeuralNetwork);
            neuralNetworkStorage.save(nnStored);
            nnId = nnStored.getNnId();
        } else if (nnType == NetworkType.LSTM) {
            LongShortTermMemoryNN lstmnn = LongShortTermMemoryNN.buildDefaultLongTermMemoryNN();
            NeuralNetworkStored nnStored = new NeuralNetworkStored(lstmnn, generalNeuralNetwork);
            neuralNetworkStorage.save(nnStored);
            nnId = nnStored.getNnId();
        } else if (nnType == NetworkType.CNN) {
            ConvolutionalNN cnn = ConvolutionalNN.buildDefaultConvolutionalNN();
            NeuralNetworkStored nnStored = new NeuralNetworkStored(cnn, generalNeuralNetwork);
            neuralNetworkStorage.save(nnStored);
            nnId = nnStored.getNnId();
        } else {
            throw new IllegalArgumentException(String.format("Unexpected neural network type %s", nnType));
        }

        return nnId;
    }
}
