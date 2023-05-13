#ifdef cl_khr_fp64
    #pragma OPENCL EXTENSION cl_khr_fp64 : enable
#elif defined(cl_amd_fp64)
    #pragma OPENCL EXTENSION cl_amd_fp64 : enable
#endif

__constant int MAX_ARRAY_BUFFER_SIZE = 4;


struct BoardDimensions {
    int height;
    int width;
};

struct DataContainer {
    __global float* data;
    const int* shape;
    const int* gIds;
    const int* sizes;
};


__kernel void everyBallStopped(__global float* balls, int ballBufferSize, __global bool* out) {
    int i = get_global_id(0);
    if (balls[i * ballBufferSize + 2] != 0 || balls[i * ballBufferSize + 3] != 0) {
        out[0] = true;
    }
}

float abs_(float f) {
    if (f < 0) {
        return -f;
    }
    return f;
}


int getIndex(int* shape, int* indices, int* arraySizes) {
    int index = 0;
    int offset = 1;
    for (int i = 0; i < arraySizes[1]; i++) {
        index += indices[i] * offset;
        if (i < arraySizes[0]) {
            offset *= shape[i];
        }
    }
    return index;
}

void clear(int indices[]) {
    for (int i = 0; i < MAX_ARRAY_BUFFER_SIZE; i++) {
        indices[i] = 0;
    }
}
//
//int* getIndices(int* shape, int length, int index) {
//    const int size = sizeof(shape) / sizeof(int);
//    int indices[size];
//    clear(indices);
//    int offset = 1;
//    for (int i = 0; i < size; i++) {
//        indices[i] = index / offset % shape[i];
//        offset *= shape[i];
//    }
//    return indices;
//}

int* buildIndices(struct DataContainer data, int j, int ballId) {
    int size = data.sizes[1]; // 3; gIds = [1, 2, 3]
    int indices[MAX_ARRAY_BUFFER_SIZE]; // [0, 0, 0, 0]
    clear(indices);
    indices[0] = j; // [j, 0, 0, 0]
    if (data.gIds[0] != -1)
        indices[data.gIds[0]] = ballId; // [j, ballId, 0, 0]
    for (int i = 1; i < size; i++) { // i = 2
        if (data.gIds[i] != -1) // 3 != -1
            indices[data.gIds[i]] = get_global_id(i); // [j, ballId, get_global_id(1), get_global_id(2)]
    }
    return indices;
}

float readBallData(struct DataContainer data, int j, int ballId) {
    const int sizes[] = {data.sizes[0], data.sizes[0]};
    return data.data[getIndex(data.shape, buildIndices(data, j, ballId), sizes)];
}

float readData(struct DataContainer data, int j) {
    return readBallData(data, j, get_global_id(0));
}

void writeAbsoluteBallData(struct DataContainer data, int j, int ballId, float value) {
    const int sizes[] = {data.sizes[0], data.sizes[0]};
    data.data[getIndex(data.shape, buildIndices(data, j, ballId), sizes)] = value;
}

void writeAbsoluteData(struct DataContainer data, int j, float value) {
    writeAbsoluteBallData(data, j, get_global_id(0), value);
}

void writeBallData(struct DataContainer data, int j, int ballId, float valueOffset) {
    const int sizes[] = {data.sizes[0], data.sizes[0]};
    data.data[getIndex(data.shape, buildIndices(data, j, ballId), sizes)] += valueOffset;
}

void writeData(struct DataContainer data, int j, float valueOffset) {
    writeBallData(data, j, get_global_id(0), valueOffset);
}

float2 readBallPosition(struct DataContainer data, int ballId) {
    return (float2) (readBallData(data, 0, ballId), readBallData(data, 1, ballId));
}

float2 readPosition(struct DataContainer data) {
    return (float2) (readData(data, 0), readData(data, 1));
}

float2 readBallVelocity(struct DataContainer data, int ballId) {
    return (float2) (readBallData(data, 2, ballId), readBallData(data, 3, ballId));
}

float2 readVelocity(struct DataContainer data) {
    return (float2) (readData(data, 2), readData(data, 3));
}

float2 updatePosition(float2 position, float2 velocity, float time) {
    return position + velocity * time;
}

float2 updateVelocity(float2 velocity, float alpha) {
    if (length(velocity) < 0.1) {
        return (float2) (0, 0);
    }
    return velocity + velocity * alpha;
}

void setBallPosition(struct DataContainer data, int ballId, float2 positionOffset) {
    writeBallData(data, 0, ballId, positionOffset.x);
    writeBallData(data, 1, ballId, positionOffset.y);
}

void setPosition(struct DataContainer data, float2 positionOffset) {
    writeData(data, 0, positionOffset.x);
    writeData(data, 1, positionOffset.y);
}

void setBallVelocity(struct DataContainer data, int ballId, float2 velocityOffset) {
    writeBallData(data, 2, ballId, velocityOffset.x);
    writeBallData(data, 3, ballId, velocityOffset.y);
}

void setVelocity(struct DataContainer data, float2 velocityOffset) {
    writeData(data, 2, velocityOffset.x);
    writeData(data, 3, velocityOffset.y);
}

bool checkHole(struct DataContainer data, struct BoardDimensions dim, float2 position, struct DataContainer gameInformation, __global float* debug) {
    float absX = abs_(position.x);
    float absY = abs_(position.y);
    if ((absX > dim.width / 2 - 1.75 || absX < 1.75) && absY > dim.height / 2 - 1.75) {
        int i = get_global_id(0);
        writeAbsoluteData(data, 0, i * 3);
        writeAbsoluteData(data, 1, -dim.height / 2 - 2);
        writeAbsoluteData(data, 2, 0);
        writeAbsoluteData(data, 3, 0);
        writeAbsoluteData(data, 4, i == 0 ? 0 : -1);

        if (i == 0) {
            writeData(gameInformation, 1, -1.2);
        } else if (i < 8) {
            writeData(gameInformation, 1, 1.1);
        } else if (i > 8) {
            writeData(gameInformation, 1, -1);
        } else {
            int s = 1;
            for (int j = 1; j < min(data.shape[1] / 2, 8); j++) {
                if (i != j && readBallData(data, 4, j) >= 0) {
                    s = -1;
                    break;
                }
            }
            writeAbsoluteData(data, 1, s * 1000.0f);
        }
        return true;
    }
    return false;
}

float2 updateWallCollision(float2 position, float2 velocity, struct BoardDimensions dim) {
    float absX = abs_(position.x);
    float absY = abs_(position.y);
    if (absX > dim.width / 2 - 1) {
        velocity.x = -velocity.x;
    }

    if (absY > dim.height / 2 - 1) {
        velocity.y = -velocity.y;
    }
    return velocity;
}

void move_(struct DataContainer balls, struct BoardDimensions dim, float alpha, float time, struct DataContainer gameInformation, __global float* debug) {
    if (readData(balls, 4) == -1) {
        return;
    }
    const float2 position = readPosition(balls);
    const float2 velocity = readVelocity(balls);

    float2 positionOffset = updatePosition(position, velocity, time);
    float2 velocityOffset = updateVelocity(velocity, alpha);

    if (checkHole(balls, dim, position, gameInformation, debug)) {
        return;
    }

    velocityOffset = updateWallCollision(positionOffset, velocityOffset, dim);

    for (int j = get_global_id(0) + 1; j < balls.shape[1]; j++) {
        const float2 bPosition = readBallPosition(balls, j);
        const float2 bVelocity = readBallVelocity(balls, j);
        float2 bPositionOffset = bPosition;

        if (length(positionOffset - bPositionOffset) < 2) {
            if (length(positionOffset - bPositionOffset) < 2) {
                if (length(velocityOffset) > length(bVelocity)) {
                    float2 d = (float2) positionOffset - bPositionOffset;
                    positionOffset += d * (2 / length(d) - 1);
                } else {
                    float2 d = (float2) bPositionOffset - positionOffset;
                    bPositionOffset += d * (2 / length(d) - 1);
                }
            }
//            int contactIndicator = (int)readData(balls, 4);
//            int contactIndicatorB = (int)readBallData(balls, 4, j);
//
//            writeAbsoluteData(balls, 4, contactIndicator | ((int)pow(2.0f, j)));
//            writeAbsoluteBallData(balls, 4, j, contactIndicatorB | ((int)pow(2.0f, get_global_id(0))));

            float2 delta = (positionOffset - bPositionOffset) / 2;

            float dot1to2 = dot(velocityOffset, delta);
            float dot2to1 = dot(bVelocity, delta);

            velocityOffset += delta * (dot2to1 - dot1to2);

            setBallVelocity(balls, j, delta * (dot1to2 - dot2to1));

            setBallPosition(balls, j, bPositionOffset - bPosition);

        } else {
//            int contactIndicator = (int)readData(balls, 4);
//            int contactIndicatorB = (int)readBallData(balls, 4, j);
//
//            writeAbsoluteData(balls, 4, contactIndicator & (~((int)pow(2.0f, j))));
//            writeAbsoluteBallData(balls, 4, j, contactIndicatorB & (~((int)pow(2.0f, get_global_id(0)))));
        }
    }
    setPosition(balls, positionOffset - position);
    setVelocity(balls, velocityOffset - velocity);
    if (isnan(length(velocityOffset))) {
        debug[0] = 1;
        debug[2] = velocityOffset.x;
        debug[3] = velocityOffset.y;
    }
    writeData(gameInformation, 0, length(velocityOffset));
}

int4 buildIndicesDebug(struct DataContainer data, int j, int ballId, __global float* debug) {
    const int size = data.sizes[1];
    int4 indices = (0, 0, 0, 0);
    indices[0] = j;
    if (data.gIds[0] != -1) {
        debug[2] = data.gIds[0];
        indices[data.gIds[0]] = ballId;
    }
    for (int i = 1; i < size; i++) {
        if (data.gIds[i] != -1) {
            debug[data.gIds[i] + 2] = get_global_id(i);
            indices[data.gIds[i]] = get_global_id(i);
        }
    }
    return indices;
}

int getIndexDebug(int4 shape, int4 indices, int2 arraySizes, __global float* debug) {
    int index = 0;
    int offset = 1;
    debug[4] = arraySizes[1];
    for (int i = 0; i < MAX_ARRAY_BUFFER_SIZE; i++) {
        index += indices[i] * offset;
        debug[5 + i] = indices[i];
        if (i < arraySizes[0]) {
            offset *= shape[i];
        }
    }
    return index;
}

void debugIndices(int offset, int4 indices, __global float* debug) {
    for (int i = 0; i < MAX_ARRAY_BUFFER_SIZE; i++) {
        debug[offset + i] = indices[i];
    }
}

__kernel void move_2(__global float* balls, int ballBufferSize, int ballAmount, float alpha, float height, float width, float time, __global float* gameInformation, __global float* debug, int anglePartition, int normPartition, short first) {
    const int ballShape[] = {ballBufferSize, ballAmount, anglePartition, normPartition};
    const int ballGId[] = {1, 2, 3};
    const int ballSizes[] = {4, 3};
    const int gameInfoShape[] = {2, anglePartition, normPartition};
    const int gameInfoGId[] = {-1, 1, 2};
    const int gameInfoSizes[] = {3, 3};

    struct DataContainer ballsData = {balls, ballShape, ballGId, ballSizes};
    struct DataContainer gameInfoData = {gameInformation, gameInfoShape, gameInfoGId, gameInfoSizes};
    struct BoardDimensions dim = {height, width};

    int angle = get_global_id(1);
    int norm = get_global_id(2);
    if (first == 1 && get_global_id(0) == 0) {
        writeData(ballsData, 2, norm * cos((float) angle * 2 * M_PI_F / anglePartition) * 300 / normPartition);
        writeData(ballsData, 3, norm * sin((float) angle * 2 * M_PI_F / anglePartition) * 300 / normPartition);
    } else if (readData(gameInfoData, 0) == 0) {
        return;
    }
    writeAbsoluteData(gameInfoData, 0, 0);
    if (norm == 50 && angle == 0 && get_global_id(0) == 0) {
        int debugIndices[] = {0, 0, 0, 50};
        int sizes[] = {4, 4};
//        int* indicesDebug = buildIndicesDebug(ballsData, 1, 0, debug);
//
//        debug[0] = getIndex(ballShape, indicesDebug, sizes);
//        writeData(gameInfoData, 1, 12);
//        debug[1] = readData(gameInfoData, 1);
        debug[6] = getIndex(ballShape, buildIndices(ballsData, 1, 0), sizes);
    }
    move_(ballsData, dim, alpha, time, gameInfoData, debug);
}

__kernel void move(__global float* balls, int ballBufferSize, int ballAmount, float alpha, float height, float width, float time, __global float* gameInformation, __global float* debug) {
    const int ballShape[] = {ballBufferSize, ballAmount};
    const int ballGId[] = {1};
    const int ballSizes[] = {2, 1};
    const int gameInfoShape[] = {2};
    const int gameInfoGId[] = {-1};
    const int gameInfoSizes[] = {1, 1};

    struct DataContainer ballsData = {balls, ballShape, ballGId, ballSizes};
    struct DataContainer gameInfoData = {gameInformation, gameInfoShape, gameInfoGId, gameInfoSizes};
    struct BoardDimensions dim = {height, width};

    if (get_global_id(0) == 0) {
//        debug[0] = getIndex(ballShape, indicesDebug, sizes);
//        writeData(gameInfoData, 1, 12);
//        debug[1] = readData(gameInfoData, 1);
        int sizes[] = {2, 2};
        int4 indicesDebug = buildIndicesDebug(ballsData, 1, 0, debug);
        debugIndices(6, indicesDebug, debug);
//        debug[0] = getIndexDebug(ballShape, , sizes, debug);
    }
    move_(ballsData, dim, alpha, time, gameInfoData, debug);
}

__kernel void copy_buffer(__global float* ballsShort, __global float* balls, int ballBufferSize, int ballAmount, int anglePartition, __global float* debug) {
    int i = get_global_id(0);
    int angle = get_global_id(1);
    int norm = get_global_id(2);
    int new_size[] = {ballBufferSize, ballAmount, anglePartition};
    int old_size[] = {ballBufferSize};
    int debugIndices[] = {0, i, angle, norm};
    int newArraySizes[] = {3, 4};
    int oldArraySizes[] = {1, 2};
    for (int j = 0; j < ballBufferSize; j++) {
        int indices[] = {j, i, angle, norm};
        int old_indices[] = {j, i};
        balls[getIndex(new_size, indices, newArraySizes)] = ballsShort[getIndex(old_size, old_indices, oldArraySizes)];
    }
}


