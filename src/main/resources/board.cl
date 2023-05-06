#ifdef cl_khr_fp64
    #pragma OPENCL EXTENSION cl_khr_fp64 : enable
#elif defined(cl_amd_fp64)
    #pragma OPENCL EXTENSION cl_amd_fp64 : enable
#else
    #pragma OPENCL EXTENSION cl_khr_fp64 : disable
#endif


struct BoardDimensions {
    int height;
    int width;
};

struct DataContainer {
    __global float* data;
    int* shape;
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

int getIndex(int shape[], int indices[]) {
        int index = 0;
        if (sizeof(indices) > sizeof(shape) - 1)
            return -1;
        int offset = 1;
        for (int i = 0; i < sizeof(indices); i++) {
            index += indices[i] * offset;
            if (i < sizeof(indices) - 1)
                offset *= shape[i];
        }
        return index;
    }

int* getIndices(int* shape, int index) {
        int indices[sizeof(shape)];
        int offset = 1;
        for (int i = 0; i < sizeof(indices); i++) {
            indices[i] = index / offset % shape[i];
            offset *= shape[i];
        }
        return indices;
    }

float2 readPosition(__global float* balls, int ballBufferSize, int i) {
    return (float2) (balls[i * ballBufferSize], balls[i * ballBufferSize + 1]);
}

float2 readVelocity(__global float* balls, int ballBufferSize, int i) {
    return (float2) (balls[i * ballBufferSize + 2], balls[i * ballBufferSize + 3]);
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

void setPosition(__global float* balls, int ballBufferSize, int i, float2 positionOffset) {
    balls[i * ballBufferSize] += positionOffset.x;
    balls[i * ballBufferSize + 1] += positionOffset.y;
}

void setVelocity(__global float* balls, int ballBufferSize, int i, float2 velocityOffset) {
    balls[i * ballBufferSize + 2] += velocityOffset.x;
    balls[i * ballBufferSize + 3] += velocityOffset.y;
}

bool checkHole(__global float* balls, int ballBufferSize, int i, float height, float width, int ballAmount, int indexOffset, __global float* gameInformation) {
    float absX = abs_(balls[i * ballBufferSize]);
    float absY = abs_(balls[i * ballBufferSize + 1]);
    if ((absX > width / 2 - 1.75 || absX < 1.75) && absY > height / 2 - 1.75) {
        balls[i * ballBufferSize] = i * 3;
        balls[i * ballBufferSize + 1] = -height / 2 -2;
        balls[i * ballBufferSize + 2] = 0;
        balls[i * ballBufferSize + 3] = 0;
        balls[i * ballBufferSize + 4] = i == 0 ? 0 : -1;

        if (get_global_id(0) == 0) {
            gameInformation[indexOffset * 2 + 1] -= 1.2;
        } else if (get_global_id(0) < 8) {
            gameInformation[indexOffset * 2 + 1] += 1.1;
        } else if (get_global_id(0) > 8) {
            gameInformation[indexOffset * 2 + 1] -= 1.0;
        } else {
            int s = 1;
            for (int j = 1; j < min(ballAmount / 2, 8); j++) {
                if (i != (j + indexOffset * ballAmount) && balls[(j + indexOffset * ballAmount) * ballBufferSize + 4] >= 0) {
                    s = -1;
                    break;
                }
            }
            gameInformation[indexOffset * 2 + 1] = s * 1000.0;
        }
        return true;
    }
    return false;
}

float2 updateWallCollision(float2 position, float2 velocity, float height, float width) {
    float absX = abs_(position.x);
    float absY = abs_(position.y);
    if (absX > width / 2 - 1) {
        velocity.x = -velocity.x;
    }

    if (absY > height / 2 - 1) {
        velocity.y = -velocity.y;
    }
    return velocity;
}

__kernel void move_(int indexOffset, __global float* balls, int ballBufferSize, int ballAmount, float alpha, float height, float width, float time, __global float* gameInformation, __global float* debug) {
    int i = get_global_id(0) + indexOffset * ballAmount;
    const float2 position = readPosition(balls, ballBufferSize, i);
    const float2 velocity = readVelocity(balls, ballBufferSize, i);

    float2 positionOffset = updatePosition(position, velocity, time);
    float2 velocityOffset = updateVelocity(velocity, alpha);

    if (checkHole(balls, ballBufferSize, i, height, width, ballAmount, indexOffset, gameInformation)) {
        return;
    }

    velocityOffset = updateWallCollision(positionOffset, velocityOffset, height, width);

    for (int j_ = get_global_id(0) + 1; j_ < ballAmount; j_++) {
        int j = j_ + indexOffset * ballAmount;
        const float2 bPosition = readPosition(balls, ballBufferSize, j);
        const float2 bVelocity = readVelocity(balls, ballBufferSize, j);
        float2 bPositionOffset = bPosition;

        if (length(positionOffset - bPositionOffset) < 2) {
            while (length(positionOffset - bPositionOffset) < 2) {
                if (length(velocityOffset) > length(bVelocity)) {
                    positionOffset -= velocityOffset * 0.1 / length(velocityOffset);
                } else {
                    bPositionOffset -= bVelocity * 0.1 / length(bVelocity);
                }
            }
            balls[i * ballBufferSize + 4] = ((int) balls[i * ballBufferSize + 4]) | ((int)pow(2.0f, j));
            balls[j * ballBufferSize + 4] = ((int) balls[j * ballBufferSize + 4]) | ((int)pow(2.0f, i));

            float2 delta = (positionOffset - bPositionOffset) / 2;

            float dot1to2 = dot(velocityOffset, delta);
            float dot2to1 = dot(bVelocity, delta);

            velocityOffset += delta * (dot2to1 - dot1to2);

            balls[j * ballBufferSize + 2] += delta.x * (dot1to2 - dot2to1);
            balls[j * ballBufferSize + 3] += delta.y * (dot1to2 - dot2to1);

            setPosition(balls, ballBufferSize, j, bPositionOffset - bPosition);

        } else {
            balls[i * ballBufferSize + 4] = ((int) balls[i * ballBufferSize + 4]) & (~((int)pow(2.0f, j)));
            balls[j * ballBufferSize + 4] = ((int) balls[j * ballBufferSize + 4]) & (~((int)pow(2.0f, i)));
        }
    }
    setPosition(balls, ballBufferSize, i, positionOffset - position);
    setVelocity(balls, ballBufferSize, i, velocityOffset - velocity);
    if (isnan(length(velocityOffset))) {
        debug[0] = 1;
        debug[1] = i;
        debug[2] = velocityOffset.x;
        debug[3] = velocityOffset.y;
    }
    gameInformation[indexOffset * 2] += length(velocityOffset);
}

__kernel void move_2(__global float* balls, int ballBufferSize, int ballAmount, float alpha, float height, float width, float time, __global float* gameInformation, __global float* debug, int anglePartition, int normPartition, short first) {
    int angle = get_global_id(2);
    int norm = get_global_id(1);
    int indexOffset = angle + anglePartition * norm;
    if (first == 1) {
        balls[2 + indexOffset * ballBufferSize * ballAmount] = norm * cos((float) angle * 2 * M_PI_F / anglePartition) * 300 / normPartition;
        balls[3 + indexOffset * ballBufferSize * ballAmount] = norm * sin((float) angle * 2 * M_PI_F / anglePartition) * 300 / normPartition;
    } else if (gameInformation[(angle + anglePartition * norm) * 2] == 0) {
        return;
    }
    gameInformation[(angle + anglePartition * norm) * 2] = 0;
    move_(indexOffset, balls, ballBufferSize, ballAmount, alpha, height, width, time, gameInformation, debug);
}

__kernel void move(__global float* balls, int ballBufferSize, int ballAmount, float alpha, float height, float width, float time, __global float* gameInformation, __global float* debug) {
    move_(0, balls, ballBufferSize, ballAmount, alpha, height, width, time, gameInformation, debug);
}

__kernel void copy_buffer(__global float* ballsShort, __global float* balls, int ballBufferSize, int ballAmount, int anglePartition) {
    int i = get_global_id(0);
    int angle = get_global_id(2);
    int norm = get_global_id(1);
    int new_size[] = {ballBufferSize, ballAmount, anglePartition};
    int old_size[] = {ballBufferSize};
    for (int j = 0; j < ballBufferSize; j++) {
        int indices = {j, i, angle, norm};
        int old_indices = {j, i};
        balls[getIndex(new_size, indices)] = ballsShort[getIndex(old_size, old_indices)];
    }
}


