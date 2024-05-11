import os
import numpy as np
from scipy.integrate import solve_ivp

"""
Adaptation du code OpenCL en Python, avec la résolution faite par solve_ivp. Pour plus d'informations,
se référer à la documentation du code OpenCL.
"""


TYPE_SINGLE_SIM_BALLS = 0
TYPE_SINGLE_SIM_INFO = 1
TYPE_MULTI_SIM_BALLS = 2
TYPE_MULTI_SIM_INFO = 3

class DataContainer:
    def __init__(self, balls, editBalls, debug, shape, type):
        self.data = balls
        self.edit = editBalls
        self.debug = debug
        self.shape = shape
        self.type = type
        
class BoardDimensions:
    def __init__(self, height, width):
        self.height = height
        self.width = width

def getIndex(data, j, ballId):
    if data.type == TYPE_SINGLE_SIM_BALLS:
        return j + data.shape[0] * ballId
    elif data.type == TYPE_SINGLE_SIM_INFO:
        return j
    return 0


def readBallData(data, j, ballId):
    return data.data[getIndex(data, j, ballId)]

def writeAbsoluteBallData(data, j, ballId, value):
    data.edit[getIndex(data, j, ballId)] = value


def writeBallData(data, j, ballId, valueOffset):
    data.edit[getIndex(data, j, ballId)] += valueOffset


def readBallPosition(data, ballId):
    return np.array([readBallData(data, 0, ballId), readBallData(data, 1, ballId)], dtype=np.float32)


def readBallVelocity(data, ballId):
    return np.array([readBallData(data, 2, ballId), readBallData(data, 3, ballId)], dtype=np.float32)


def updatePosition(position, velocity, time):
    return position + velocity * time


def updateVelocity(velocity, alpha):
    if np.linalg.norm(velocity) < 0.1:
        return 0, 0
    
    return velocity + velocity * alpha

def rungeKuttaFloat4Converter(position, velocity):
    if np.linalg.norm(velocity) < 0.1:
        velocity = (0, 0)
    
    return np.array([position[0], position[1], velocity[0], velocity[1]], dtype=np.float32)


def rungeKuttaFunc(y, alpha):
    return np.array([y[2], y[3], -alpha * y[2], -alpha * y[3]], dtype=np.float32)


def rungeKutta(position, velocity, alpha, time):
    y = rungeKuttaFloat4Converter(position, velocity)
    k1 = rungeKuttaFunc(y, alpha)
    k2 = rungeKuttaFunc(y + (time / 2.0) * k1, alpha)
    k3 = rungeKuttaFunc(y + (time / 2.0) * k2, alpha)
    k4 = rungeKuttaFunc(y + time * k3, alpha)

    return y + (time / 6.0) * (k1 + 2 * k2 + 2 * k3 + k4)


def solve_ivp_method(position, velocity, alpha, time):
    y = rungeKuttaFloat4Converter(position, velocity)
    def f(t, y):
        return rungeKuttaFunc(y, alpha)

    return solve_ivp(f, [0, time], y, t_eval=[time]).y[:, 0]

def setBallPosition(data, ballId, positionOffset):
    writeAbsoluteBallData(data, 0, ballId, positionOffset[0])
    writeAbsoluteBallData(data, 1, ballId, positionOffset[1])

def setBallVelocity(data, ballId, velocityOffset):
    writeAbsoluteBallData(data, 2, ballId, velocityOffset[0])
    writeAbsoluteBallData(data, 3, ballId, velocityOffset[1])


def checkHole(i, data, dim, position, gameInformation, debug):
    absX = abs(position[0])
    absY = abs(position[1])
    if (absX > dim.width / 2 - 1.75 or absX < 1.75) and absY > dim.height / 2 - 1.75:
        writeAbsoluteBallData(data, 0, i, i * 3)
        writeAbsoluteBallData(data, 1, i, -dim.height / 2 - 5)
        writeAbsoluteBallData(data, 2, i, 0)
        writeAbsoluteBallData(data, 3, i, 0)
        writeAbsoluteBallData(data, 4, i, -1)

        if i == 0:
            writeBallData(gameInformation, 1, i, -12)
        elif i < 8:
            writeBallData(gameInformation, 1, i, 11)
        elif i > 8:
            writeBallData(gameInformation, 1, i, -10)
        else:
            s = 1
            for j in range(1, 8):
                if readBallData(data, 4, j) >= 0:
                    s = -1
                    break
                
            
            writeBallData(gameInformation, 1, i, s * 1000)
        
        return True
    
    return False


def updateWallCollision(position, velocity, dim):
    absX = abs(position[0])
    absY = abs(position[1])
    if absX > dim.width / 2 - 1:
        velocity[0] = -velocity[0]
    

    if absY > dim.height / 2 - 1:
        velocity[1] = -velocity[1]
    
    return velocity


def move_(k, balls, dim, alpha, time, gameInformation, debug):
    if readBallData(balls, 4, k) == -1:
        writeAbsoluteBallData(balls, 4, k, -1)
        writeAbsoluteBallData(balls, 0, k, readBallData(balls, 0, k))
        writeAbsoluteBallData(balls, 1, k, readBallData(balls, 1, k))
        writeAbsoluteBallData(balls, 2, k, readBallData(balls, 2, k))
        writeAbsoluteBallData(balls, 3, k, readBallData(balls, 3, k))
        return
    
    position = readBallPosition(balls, k)
    velocity = readBallVelocity(balls, k)

    positionOffset = position
    velocityOffset = velocity
    for j in range(balls.shape[1]):
        if j == k:
            continue

        bPosition = readBallPosition(balls, j)
        bVelocity = readBallVelocity(balls, j)
        if np.linalg.norm(positionOffset - bPosition) <= 2:
            d = positionOffset - bPosition
            positionOffset += d * (2 / np.linalg.norm(d) - 1)

            delta = (positionOffset - bPosition) / 2

            dot1to2 = np.dot(velocityOffset, delta)
            dot2to1 = np.dot(bVelocity, delta)

            velocityOffset += delta * (dot2to1 - dot1to2)

    

    position_result = solve_ivp_method(positionOffset, velocityOffset, alpha, time)

    positionOffset = np.array([position_result[0], position_result[1]], dtype=np.float32)
    velocityOffset = np.array([position_result[2], position_result[3]], dtype=np.float32)

    if checkHole(k, balls, dim, position, gameInformation, debug):
        return
    

    velocityOffset = updateWallCollision(positionOffset, velocityOffset, dim)

    setBallPosition(balls, k, positionOffset)
    setBallVelocity(balls, k, velocityOffset)
    writeBallData(gameInformation, 0, k, np.linalg.norm(velocityOffset))

def move(balls, editBalls, ballBufferSize, ballAmount, alpha, height, width, time, gameInformation, debug):
    ballShape = (ballBufferSize, ballAmount, 0, 0)
    gameInfoShape = (2, 0, 0, 0)

    ballsData = DataContainer(balls, editBalls, debug, ballShape, TYPE_SINGLE_SIM_BALLS)
    gameInfoData = DataContainer(gameInformation, gameInformation, debug, gameInfoShape, TYPE_SINGLE_SIM_INFO)
    dim = BoardDimensions(height, width)

    for i in range(ballAmount):
        move_(i, ballsData, dim, alpha, time, gameInfoData, debug)


"""
Formatage des données à renvoyer (en chaine de caractères)
"""

balls = np.array([np.float32(i) for i in os.sys.argv[1].split(';')], dtype=np.float32)
editBalls = np.array([np.float32(i) for i in os.sys.argv[2].split(';')], dtype=np.float32)
ballBufferSize = int(os.sys.argv[3])
ballAmount = int(os.sys.argv[4])
alpha = np.float32(os.sys.argv[5])
height = np.float32(os.sys.argv[6])
width = np.float32(os.sys.argv[7])
time = np.float32(os.sys.argv[8])
gameInformation = np.array([np.float32(i) for i in os.sys.argv[9].split(';')], dtype=np.float32)

move(balls, editBalls, ballBufferSize, ballAmount, alpha, height, width, time, gameInformation, 0)

editBalls_str = ";".join([str(np.float32(i)) for i in editBalls])

# Lister les arguments
print("1!" + editBalls_str)
