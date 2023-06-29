import os

import numpy as np
from matplotlib import pyplot as plt

from file_manager import read_statistic_files, StatisticFile

def plot_3D_graphs(file: StatisticFile):
    plt.figure()
    ax = plt.axes(projection='3d')
    x, y = np.meshgrid(np.array([i for i in range(file.get_shape()[1])]), np.array([j for j in range(file.get_shape()[0])]))
    ax.plot_surface(x, y, file.get_data(), cmap='viridis', edgecolor='none')
    ax.set_title(file.get_name())
    ax.set_xlabel('Angle')
    ax.set_ylabel('Norm')
    ax.set_zlabel(file.get_name())

def plot_2D_graph(file: StatisticFile):
    plt.figure()
    ax = plt.axes(projection='2d')
    ax.plot(np.array([i for i in range(file.get_shape()[0])]), file.get_data())
    plt.title(file.get_name())

def plot_graph(file: StatisticFile):
    if len(file.get_shape()) == 1:
        plot_2D_graph(file)
    else:
        plot_3D_graphs(file)


def plot_directory(directory: str):
    if directory[0] != "/" and directory[0] != "\\":
        directory = os.getcwd() + "\\" + directory
    files = read_statistic_files(directory)
    for file in files:
        plot_graph(file)
    plt.show()

def plot_simulation(id: int):
    plot_directory(r"data\simulation-" + str(id))


plot_simulation(3)

# Angle et norme optimales : 3, 2 => 500,119 ; 500 * 119 = 59500
