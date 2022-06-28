import sys
import numpy.random
import pandas as pd
import json
import torch
#import logging

from constructor.function_for_db import upload, insert, update, connect
import constructor.constructor as ctor
import constructor.structures as structures


def train():
    task_id = int(sys.argv[1])

    con = connect()
    cur = con.cursor()

    task_info = structures.Task(upload(cur, "tasksqueue", "task_id", task_id))
    dataset = structures.Dataset(upload(cur, "datasets", "dataset_id", task_info.dataset))
    info = json.loads(upload(cur, "neuralnetworks", "nn_id", task_info.nnDescription)[1])

    model = ctor.NeuralNetwork()
    model.load_info(info)

    if task_info.model is not None:
        model_params = upload(cur, "nntrained_models", "nn_trained_model_id", task_info.model)
        ctor.restore_net(model_params[1], model)

    if task_info.task_type == structures.TaskType.applyToData:
        res = ctor.get_prediction(model, dataset.data)
        df = pd.DataFrame(dict(label=res))
        data = df.to_csv(index=False)

        generate_answer_id = numpy.random.randint(1000000)
        insert(cur, "predictions", "prediction_id", "predictions", generate_answer_id, data, task_id)
        update(cur, "prediction_id", generate_answer_id, task_id)

    else:
        nn_type = structures.TrainType(int(task_info.task_type))
        if model.type is None:
            model.load_type(nn_type)
        history = []
        optimizer = torch.optim.Adam(model.parameters(), lr=info['learningRate'])
        ctor.train(model, dataset, optimizer, history, task_info.epoch)
        network = ctor.save(model, optimizer, history)
        generate_model_id = numpy.random.randint(1000000)
        insert(cur, "nntrained_models", "nn_trained_model_id", "nn_trained_model", generate_model_id, network, task_id)
        update(cur, "model_id", generate_model_id, task_id)

    #logging.info("Done")

    con.commit()
    con.close()

if __name__ == '__main__':
    train()
