#ifdef cl_khr_fp64
    #pragma OPENCL EXTENSION cl_khr_fp64 : enable
#elif defined(cl_amd_fp64)
    #pragma OPENCL EXTENSION cl_amd_fp64 : enable
#endif

__constant int MAX_ARRAY_BUFFER_SIZE = 4;
__constant int4 masks4[] = {(int4) (0xffffffff, 0, 0, 0),
                          (int4) (0, 0xffffffff, 0, 0),
                          (int4) (0, 0, 0xffffffff, 0),
                          (int4) (0, 0, 0, 0xffffffff)};
__constant int3 masks3[] = {(int3) (0xffffffff, 0, 0),
                          (int3) (0, 0xffffffff, 0),
                          (int3) (0, 0, 0xffffffff)};
__constant int2 masks2[] = {(int2) (0xffffffff, 0),
                          (int2) (0, 0xffffffff)};


float* debug_global;

int4 setComponent4(int4 vector, int index, int value) {
    int4 mask = masks4[index];
    return (vector & ~mask) + ((int4) (value) & mask);
}

int getComponent4(int4 vector, int index) {
    int4 vect = vector & masks4[index];
    return vect.x + vect.y + vect.z + vect.w;
}

int3 setComponent3(int3 vector, int index, int value) {
    int3 mask = masks3[index];
    return (vector & ~mask) + ((int3) (value) & mask);
}

int getComponent3(int3 vector, int index) {
    int3 vect = vector & masks3[index];
    return vect.x + vect.y + vect.z;
}

int2 setComponent2(int2 vector, int index, int value) {
    int2 mask = masks2[index];
    return (vector & ~mask) + ((int2) (value) & mask);
}

int getComponent2(int2 vector, int index) {
    int2 vect = vector & masks2[index];
    return vect.x + vect.y;
}

struct BoardDimensions {
    int height;
    int width;
};

struct DataContainer {
    __global float* data;
    const int4 shape;
    const int3 gIds;
    const int2 sizes;
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


int4 buildIndices(struct DataContainer data, int j, int ballId) {
    const int size = getComponent2(data.sizes, 1);

    int4 indices = (int4) (j, 0, 0, 0);
    if (getComponent3(data.gIds, 0) != -1) {
        indices = (int4) setComponent4(indices, getComponent3(data.gIds, 0), ballId);
    }
    for (int i = 1; i < size; i++) {
        if (getComponent3(data.gIds, i) != -1) {
            indices = (int4) setComponent4(indices, getComponent3(data.gIds, i), get_global_id(i));
        }
    }
    return indices;
}

int getIndex(int4 shape, int4 indices, int2 arraySizes) {
    int index = 0;
    int offset = 1;
    for (int i = 0; i < MAX_ARRAY_BUFFER_SIZE; i++) {
        index += getComponent4(indices, i) * offset;
        if (i < getComponent2(arraySizes, 0)) {
            offset *= getComponent4(shape, i);
        }
    }
    return index;
}

float readBallData(struct DataContainer data, int j, int ballId) {
    return data.data[getIndex(data.shape, buildIndices(data, j, ballId), (int2) (getComponent2(data.sizes, 0)))];
}

float readData(struct DataContainer data, int j) {
    return readBallData(data, j, get_global_id(0));
}

void writeAbsoluteBallData(struct DataContainer data, int j, int ballId, float value) {
    data.data[getIndex(data.shape, buildIndices(data, j, ballId), (int2) (getComponent2(data.sizes, 0)))] = value;
}

void writeAbsoluteData(struct DataContainer data, int j, float value) {
    writeAbsoluteBallData(data, j, get_global_id(0), value);
}

void writeBallData(struct DataContainer data, int j, int ballId, float valueOffset) {
    data.data[getIndex(data.shape, buildIndices(data, j, ballId), (int2) (getComponent2(data.sizes, 0)))] += valueOffset;
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
    if (length(velocity) < 0.1f) {
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
    if ((absX > dim.width / 2 - 1.75f || absX < 1.75f) && absY > dim.height / 2 - 1.75f) {
        int i = get_global_id(0);
        writeAbsoluteData(data, 0, i * 3);
        writeAbsoluteData(data, 1, -dim.height / 2 - 2);
        writeAbsoluteData(data, 2, 0);
        writeAbsoluteData(data, 3, 0);
        writeAbsoluteData(data, 4, -1);

        if (i == 0) {
            writeData(gameInformation, 1, -12);
        } else if (i < 8) {
            writeData(gameInformation, 1, 11);
        } else if (i > 8) {
            writeData(gameInformation, 1, -10);
        } else {
            int s = 1;
            for (int j = 1; j < 8; j++) {
                if (readBallData(data, 4, j) >= 0) {
                    s = -1;
                    break;
                }
            }
            writeData(gameInformation, 1, s * 1000);
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

    for (int j = get_global_id(0) + 1; j < getComponent4(balls.shape, 1); j++) {
        const float2 bPosition = readBallPosition(balls, j);
        const float2 bVelocity = readBallVelocity(balls, j);
        float2 bPositionOffset = bPosition;

        if (length(positionOffset - bPositionOffset) < 2) {
            if (length(velocityOffset) > length(bVelocity)) {
                float2 d = (float2) positionOffset - bPositionOffset;
                positionOffset += d * (2 / length(d) - 1);
            } else {
                float2 d = (float2) bPositionOffset - positionOffset;
                bPositionOffset += d * (2 / length(d) - 1);
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
    writeData(gameInformation, 0, length(velocityOffset));
}

void debugIndices(int offset, int4 indices, __global float* debug) {
    for (int i = 0; i < MAX_ARRAY_BUFFER_SIZE; i++) {
        debug[offset + i] = getComponent4(indices, i);
    }
}

__kernel void move_2(__global float* balls, int ballBufferSize, int ballAmount, float alpha, float height, float width, float time, __global float* gameInformation, __global float* debug, int anglePartition, int normPartition, short first) {
    const int4 ballShape = (int4) (ballBufferSize, ballAmount, anglePartition, normPartition);
    const int3 ballGId = (int3) (1, 2, 3);
    const int2 ballSizes = (int2) (4, 3);
    const int4 gameInfoShape = (int4) (2, anglePartition, normPartition, 0);
    const int3 gameInfoGId = (int3) (-1, 1, 2);
    const int2 gameInfoSizes = (int2) (3, 3);

//
//    const int4 ballShape = (int4) (ballBufferSize, ballAmount, 0, 0);
//    const int3 ballGId = (int3) (1, -1, -1);
//    const int2 ballSizes = (int2) (2, 1);

    struct DataContainer ballsData = {balls, ballShape, ballGId, ballSizes};
    struct DataContainer gameInfoData = {gameInformation, gameInfoShape, gameInfoGId, gameInfoSizes};
    struct BoardDimensions dim = {height, width};

    int angle = get_global_id(1);
    int norm = get_global_id(2);
    if (angle != 0 || norm != 0) {
        return;
    }
    if (first == 1 && get_global_id(0) == 0) {
        writeAbsoluteBallData(ballsData, 2, 0, 142.82053f);
        writeAbsoluteBallData(ballsData, 3, 0, -203.96886f);

//        writeData(ballsData, 2, norm * cos((float) angle * 2 * M_PI_F / anglePartition) * 300.0f / normPartition);
//        writeData(ballsData, 3, norm * sin((float) angle * 2 * M_PI_F / anglePartition) * 300.0f / normPartition);
    } else if (get_global_id(0) == 0) {
        int4 indices = (int4) (1, 0, 0, 0);
        int i1 = getComponent4(indices, 0);
        indices = setComponent4(indices, 1, i1);
        int i2 = getComponent4(indices, 1);
        indices = setComponent4(indices, 2, i2);
        int i3 = getComponent4(indices, 2);
        indices = setComponent4(indices, 3, i3);
        int i4 = getComponent4(indices, 3);
        debug[0] = indices.x;
        debug[1] = indices.y;
        debug[2] = indices.z;
        debug[3] = indices.w;
        debug[4] = i1;
        debug[5] = i2;
        debug[6] = i3;
        debug[7] = i4;
    }

    for (int j = get_global_id(0) + 1; j < getComponent4(ballsData.shape, 1); j++) {
        for (int i = 0; i < 4; i++) {
            int4 indices = buildIndices(ballsData, i, j);
            if (indices.x != i || indices.y != j || indices.z != 0 || indices.w != 0) {
                debug[4] = get_global_id(0);
                debug[5] = j;
                debug[6] = i;
            }
        }
    }
//    else if (readData(gameInfoData, 0) == 0) {
//        return;
//    }
    if (get_global_id(0) == 0) {
        writeAbsoluteData(gameInfoData, 0, 0);
    }
    move_(ballsData, dim, alpha, time, gameInfoData, debug);
}

__kernel void move(__global float* balls, int ballBufferSize, int ballAmount, float alpha, float height, float width, float time, __global float* gameInformation, __global float* debug) {
    const int4 ballShape = (int4) (ballBufferSize, ballAmount, 0, 0);
    const int3 ballGId = (int3) (1, -1, -1);
    const int2 ballSizes = (int2) (2, 1);
    const int4 gameInfoShape = (int4) (2, 0, 0, 0);
    const int3 gameInfoGId = (int3) (-1, 0, 0);
    const int2 gameInfoSizes = (int2) (1, 1);

    struct DataContainer ballsData = {balls, ballShape, ballGId, ballSizes};
    struct DataContainer gameInfoData = {gameInformation, gameInfoShape, gameInfoGId, gameInfoSizes};
    struct BoardDimensions dim = {height, width};

    if (get_global_id(0) == 0) {
        float2 velocity = readVelocity(ballsData);
        debug[9] = velocity.x;
        debug[8] = velocity.y;
    }
    move_(ballsData, dim, alpha, time, gameInfoData, debug);
}

__kernel void copy_buffer(__global float* ballsShort, __global float* balls, int ballBufferSize, int ballAmount, int anglePartition, __global float* debug) {
    int i = get_global_id(0);
    int angle = get_global_id(1);
    int norm = get_global_id(2);
    for (int j = 0; j < ballBufferSize; j++) {
        balls[getIndex((int4) (ballBufferSize, ballAmount, anglePartition, 0), (int4) (j, i, angle, norm), (int2) (3, 4))]
            = ballsShort[getIndex((int4) (ballBufferSize, 0, 0, 0), (int4) (j, i, 0, 0), (int2) (1, 2))];
    }
}


