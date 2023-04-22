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

__kernel void move_(int i, __global float* balls, int ballBufferSize, int ballAmount, float alpha, float height, float width, float time, __global float* debug) {

    float vx = balls[i * ballBufferSize + 2];
    float vy = balls[i * ballBufferSize + 3];

    balls[i * ballBufferSize] += vx * time;
    balls[i * ballBufferSize + 1] += vy * time;

    float x = balls[i * ballBufferSize];
    float y = balls[i * ballBufferSize + 1];

    float absX = abs_(x);
    float absY = abs_(y);

    if (abs_((vx*vx + vy*vy)) < 0.07) {
        balls[i * ballBufferSize + 2] = 0;
        balls[i * ballBufferSize + 3] = 0;
    } else {
        balls[i * ballBufferSize + 2] += vx * alpha;
        balls[i * ballBufferSize + 3] += vy * alpha;
    }

    if ((absX > width / 2 - 1.75 || absX < 1.75) && absY > height / 2 - 1.75) {
        balls[i * ballBufferSize] = i * 3;
        balls[i * ballBufferSize + 1] = -height / 2 -2;
        balls[i * ballBufferSize + 2] = 0;
        balls[i * ballBufferSize + 3] = 0;
        balls[i * ballBufferSize + 4] = i == 0 ? 0 : -1;
        return;
    }

    if (absX > width / 2 - 1) {
        balls[i * ballBufferSize + 2] = -balls[i * ballBufferSize + 2];
    }

    if (absY > height / 2 - 1) {
        balls[i * ballBufferSize + 3] = -balls[i * ballBufferSize + 3];
    }

    for (int j = i + 1; j < ballAmount; j++) {
        float bX = balls[j * ballBufferSize];
        float bY = balls[j * ballBufferSize + 1];
        int pj = pow((float)2, j);
        int pi = pow((float)2, i);
        float dist = pow((float) (bX - x), 2) + pow((float) (bY - y), 2);
        if (dist <= 4) {
            float bvX = balls[j * ballBufferSize + 2];
            float bvY = balls[j * ballBufferSize + 3];
            while (pow((float) (balls[j * ballBufferSize] - balls[i * ballBufferSize]), 2) + pow((float) (balls[j * ballBufferSize + 1] - balls[i * ballBufferSize + 1]), 2) < 4) {
                if (vx * vx + vy * vy >= bvX * bvX + bvY * bvY) {
                    balls[i * ballBufferSize] -= vx * 0.1 / (sqrt(vx * vx + vy * vy));
                    balls[i * ballBufferSize + 1] -= vy * 0.1 / (sqrt(vx * vx + vy * vy));
                } else {
                    balls[j * ballBufferSize] -= bvX * 0.1 / (sqrt(bvX * bvX + bvY * bvY));
                    balls[j * ballBufferSize + 1] -= bvY * 0.1 / (sqrt(bvX * bvX + bvY * bvY));
                }

                debug[4] = 1;
            }
            balls[i * ballBufferSize + 4] = ((int) balls[i * ballBufferSize + 4]) | pj;
            balls[j * ballBufferSize + 4] = ((int) balls[j * ballBufferSize + 4]) | pi;

            float deltaX = (bX - x) / 2.0;
            float deltaY = (bY - y) / 2.0;

            float dot2to1 = (-balls[i * ballBufferSize + 2] * deltaX + bvY * deltaY);
            float dot1to2 = -(bvX * deltaX - balls[i * ballBufferSize + 3] * deltaY);

            balls[j * ballBufferSize + 2] += deltaX * (dot1to2 - dot2to1);
            balls[j * ballBufferSize + 3] += deltaY * (dot1to2 - dot2to1);

            balls[i * ballBufferSize + 2] -= deltaX * (dot1to2 - dot2to1);
            balls[i * ballBufferSize + 3] -= deltaY * (dot1to2 - dot2to1);

        } else {
            balls[i * ballBufferSize + 4] = ((int) balls[i * ballBufferSize + 4]) & (~pj);
            balls[j * ballBufferSize + 4] = ((int) balls[j * ballBufferSize + 4]) & (~pi);
        }
    }
}

__kernel void move_2(__global float* balls, int ballBufferSize, int ballAmount, float alpha, float height, float width, float time, __global float* debug, int norme) {
    int angle = get_global_id(2);
    balls[2] = norme * cos((float) angle);
    balls[3] = norme * sin((float) angle);
    move_(get_global_id(0) + ballAmount * angle, balls, ballBufferSize, ballAmount, alpha, height, width, time, debug);
}

__kernel void move(__global float* balls, int ballBufferSize, int ballAmount, float alpha, float height, float width, float time, __global float* debug) {
    move_(get_global_id(0), balls, ballBufferSize, ballAmount, alpha, height, width, time, debug);
}

__kernel void test(__global float4* balls) {
    balls[0].x = 1;
    balls[0].y = 1;
    balls[0].z = 1;
    balls[0].w = 1;
}
