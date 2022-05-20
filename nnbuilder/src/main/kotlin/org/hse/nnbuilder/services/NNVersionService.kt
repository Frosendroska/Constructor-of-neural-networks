package org.hse.nnbuilder.services

import net.devh.boot.grpc.server.service.GrpcService
import org.hse.nnbuilder.exception.NeuralNetworkNotFoundException
import org.hse.nnbuilder.version_controller.GeneralNeuralNetworkService
import org.springframework.beans.factory.annotation.Autowired

@GrpcService
open class NNVersionService : NNVersionServiceGrpcKt.NNVersionServiceCoroutineImplBase() {

    @Autowired
    private lateinit var generalNeuralNetworkService: GeneralNeuralNetworkService

    @Override
    override suspend fun makeNNSnapshot(request: Nnversion.makeNNSnapshotRequest): Nnversion.makeNNSnapshotResponse {
        var exception = ""
        var newVersionId = 0L
        try {
            newVersionId = generalNeuralNetworkService.addNewVersion(request.nnId)
        } catch (e: NeuralNetworkNotFoundException) {
            exception = e.message
        }
        return Nnversion.makeNNSnapshotResponse.newBuilder()
                .setNnId(newVersionId)
                .setException(exception)
                .build()
    }

    @Override
    override suspend fun deleteNNVersion(request: Nnversion.deleteNNVersionRequest): Nnversion.deleteNNVersionResponse {
        val nnId = request.nnId
//        var generalNN = generalNeuralNetworkService.getByIdOfNNVersion(nnId)
//        for (i in generalNN.getNNVersions()) {
//            println(i.id)
//        }
        println("-----------------")

        generalNeuralNetworkService.deleteNNVersionById(nnId)

        try {
//            generalNN = generalNeuralNetworkService.getByIdOfNNVersion(-1856894684170175216)
//            for (i in generalNN.getNNVersions()) {
//                println(i.id)
//            }
        } catch (e: Exception) {
        }

        return Nnversion.deleteNNVersionResponse.newBuilder().build()
    }
}
