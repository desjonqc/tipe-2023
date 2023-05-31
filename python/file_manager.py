import os

import numpy as np

class StatisticFile:
    def __init__(self, file_name):
        self.file_name = file_name
        self.file = open(file_name, 'r')
        self.name = file_name.split('/')[-1].split('.')[0]
        shape = []
        coefficients = []
        data_raw = []
        normalize = True
        for line in self.file:
            if '#' in line:
                continue
            if len(shape) == 0:
                shape = [int(i) for i in line.split(',')]
            elif len(coefficients) == 0:
                coefficients = [int(i) for i in line.split(',')]
            elif len(data_raw) == 0:
                data_raw.append([float(i) for i in line.split(',')])
            else:
                normalize = bool(line)

        self.shape = tuple(shape)
        np_data_raw = np.reshape(np.array(data_raw), self.shape)
        self.coefficients = coefficients
        if normalize:
            total_simu = np.zeros(self.get_shape())
            for i in range(shape[0]):
                for j in range(shape[1]):
                    angle = coefficients[0] + j * coefficients[1]
                    norm = coefficients[2] + i * coefficients[3]
                    total_sim = angle * norm
                    total_simu[i, j] = total_sim
                    np_data_raw[i, j] = np_data_raw[i, j] / total_sim
            self.total_simu = total_simu
        self.data_raw = np_data_raw

    def get_data(self):
        return self.data_raw

    def get_coefficients(self):
        return self.coefficients

    def get_shape(self):
        return self.shape

    def get_total_simu(self):
        return self.total_simu

    def get_name(self):
        return self.name


def read_statistic_files(directory):
    result = []
    for file_name in os.listdir(directory):
        if file_name.endswith(".statistic"):
            result.append(StatisticFile(directory + "/" + file_name))
    return result
