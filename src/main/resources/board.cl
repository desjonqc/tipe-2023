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

// Types de résolution
__constant uint TYPE_SINGLE_SIM_BALLS = 0;
__constant uint TYPE_SINGLE_SIM_INFO = 1;
__constant uint TYPE_MULTI_SIM_BALLS = 2;
__constant uint TYPE_MULTI_SIM_INFO = 3;

// Fonctions utiles aux vecteurs (jusqu'à 4d) permettant de modifier / obtenir une unique composante.

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

// Dimensions du plateau de jeu
struct BoardDimensions {
    int height;
    int width;
};

// Conteneur de données pour les calculs
struct DataContainer {
    __global float* data; // Données de base
    __global float* edit; // Données modifiées
    __global float* debug; // Tableau de debug
    const int4 shape; // Dimensions des données
    const uint type; // Type de résolution (voir plus haut)
};

// Fonction déterminant si toutes les boules sont arrêtées
__kernel void everyBallStopped(__global float* balls, int ballBufferSize, __global bool* out) {
    int i = get_global_id(0);
    if (balls[i * ballBufferSize + 2] != 0 || balls[i * ballBufferSize + 3] != 0) {
        out[0] = true;
    }
}

// Fonction abs pour les floats
float abs_(float f) {
    if (f < 0) {
        return -f;
    }
    return f;
}

// Récupère l'indice d'une boule dans le tableau de données suivant la dimension du tableau (voir shape)
int getIndex(struct DataContainer data, int j, int ballId) {
    if (data.type == TYPE_SINGLE_SIM_BALLS) {
        return j + data.shape.x * ballId;
    } else if (data.type == TYPE_SINGLE_SIM_INFO) {
        return j;
    } else if (data.type == TYPE_MULTI_SIM_BALLS) {
        return j + data.shape.x * ballId + data.shape.x * data.shape.y * get_global_id(1)
            + data.shape.x * data.shape.y * data.shape.z * get_global_id(2);
    } else if (data.type == TYPE_MULTI_SIM_INFO) {
        return j + data.shape.x * get_global_id(1) + data.shape.x * data.shape.y * get_global_id(2);
    }
    return 0;
}

// Récupère la valeur d'une boule dans le tableau de données
float readBallData(struct DataContainer data, int j, int ballId) {
    return data.data[getIndex(data, j, ballId)];
}

// Récupère la valeur de la boule de travail (courante) dans le tableau de données
float readData(struct DataContainer data, int j) {
    return readBallData(data, j, get_global_id(0));
}

// Modifie la valeur d'une boule dans le tableau de données
void writeAbsoluteBallData(struct DataContainer data, int j, int ballId, float value) {
    data.edit[getIndex(data, j, ballId)] = value;
}

// Modifie la valeur de la boule de travail (courante) dans le tableau de données
void writeAbsoluteData(struct DataContainer data, int j, float value) {
    writeAbsoluteBallData(data, j, get_global_id(0), value);
}

// Modifie la valeur d'une boule dans le tableau de données en ajoutant à la valeur actuelle
void writeBallData(struct DataContainer data, int j, int ballId, float valueOffset) {
    data.edit[getIndex(data, j, ballId)] += valueOffset;
}

// Modifie la valeur de la boule de travail (courante) dans le tableau de données en ajoutant à la valeur actuelle
void writeData(struct DataContainer data, int j, float valueOffset) {
    writeBallData(data, j, get_global_id(0), valueOffset);
}

// Récupère la position d'une boule dans le tableau de données
float2 readBallPosition(struct DataContainer data, int ballId) {
    return (float2) (readBallData(data, 0, ballId), readBallData(data, 1, ballId));
}

// Récupère la position de la boule de travail (courante) dans le tableau de données
float2 readPosition(struct DataContainer data) {
    return (float2) (readData(data, 0), readData(data, 1));
}

// Récupère la vélocité d'une boule dans le tableau de données
float2 readBallVelocity(struct DataContainer data, int ballId) {
    return (float2) (readBallData(data, 2, ballId), readBallData(data, 3, ballId));
}

// Récupère la vélocité de la boule de travail (courante) dans le tableau de données
float2 readVelocity(struct DataContainer data) {
    return (float2) (readData(data, 2), readData(data, 3));
}

// ##### EULER EXPLICITE #####

// Modifie une position en applicant un développement limité à l'ordre 1 sur le temps
float2 updatePosition(float2 position, float2 velocity, float time) {
    return position + velocity * time;
}

// Modifie une vélocité en applicant un développement limité à l'ordre 1 sur le temps (PFD)
float2 updateVelocity(float2 velocity, float alpha) {
    if (length(velocity) < 0.1f) {
        return (float2) (0, 0);
    }
    return velocity - velocity * alpha / 500.0f;
}

// ##### RUNGE KUTTA #####

// Convertit une position et une vélocité en un vecteur de 4 floats
float4 rungeKuttaFloat4Converter(float2 position, float2 velocity) {
    if (length(velocity) < 0.1f) {
        velocity = (float2) (0, 0);
    }
    return (float4) (position.x, position.y, velocity.x, velocity.y);
}

// Fonction de calcul de Runge Kutta
float4 rungeKuttaFunc(float4 y, float alpha) {
    return (float4) (y.z, y.w, -alpha * y.z, -alpha * y.w);
}

// Calcul de Runge Kutta
float4 rungeKutta(float2 position, float2 velocity, float alpha, float time) {
    float4 y = rungeKuttaFloat4Converter(position, velocity);
    float4 k1 = rungeKuttaFunc(y, alpha);
    float4 k2 = rungeKuttaFunc(y + (time / 2.0f) * k1, alpha);
    float4 k3 = rungeKuttaFunc(y + (time / 2.0f) * k2, alpha);
    float4 k4 = rungeKuttaFunc(y + time * k3, alpha);

    return y + (time / 6.0f) * (k1 + 2 * k2 + 2 * k3 + k4);
}

// Met à jour la position d'une boule
void setBallPosition(struct DataContainer data, int ballId, float2 positionOffset) {
    writeBallData(data, 0, ballId, positionOffset.x);
    writeBallData(data, 1, ballId, positionOffset.y);
}

// Met à jour la position de la boule de travail (courante)
void setPosition(struct DataContainer data, float2 positionOffset) {
    writeAbsoluteData(data, 0, positionOffset.x);
    writeAbsoluteData(data, 1, positionOffset.y);
}

// Met à jour la vélocité d'une boule
void setBallVelocity(struct DataContainer data, int ballId, float2 velocityOffset) {
    writeBallData(data, 2, ballId, velocityOffset.x);
    writeBallData(data, 3, ballId, velocityOffset.y);
}

// Met à jour la vélocité de la boule de travail (courante)
void setVelocity(struct DataContainer data, float2 velocityOffset) {
    writeAbsoluteData(data, 2, velocityOffset.x);
    writeAbsoluteData(data, 3, velocityOffset.y);
}

// Vérifie si la boule est dans un trou et attribue les points en conséquence
bool checkHole(struct DataContainer data, struct BoardDimensions dim, float2 position, struct DataContainer gameInformation, __global float* debug) {
    float absX = abs_(position.x);
    float absY = abs_(position.y);
    if ((absX > dim.width / 2 - 1.75f || absX < 1.75f) && absY > dim.height / 2 - 1.75f) {
        int i = get_global_id(0);
        writeAbsoluteData(data, 0, i * 3);
        writeAbsoluteData(data, 1, -dim.height / 2 - 5);
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

// Vérifie la collision d'une boule avec un mur et met à jour la vélocité en conséquence
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

// Fonction de résolution utilisant Runge Kutta
void compute_runge_kutta(struct DataContainer balls, struct BoardDimensions dim, float alpha, float time, struct DataContainer gameInformation, __global float* debug) {
    if (readData(balls, 4) == -1) { // Si la boule est hors jeu, on la laisse hors jeu.
        writeAbsoluteData(balls, 4, -1);
        writeAbsoluteData(balls, 0, readData(balls, 0));
        writeAbsoluteData(balls, 1, readData(balls, 1));
        writeAbsoluteData(balls, 2, readData(balls, 2));
        writeAbsoluteData(balls, 3, readData(balls, 3));
        return;
    }

    // Récupération de la position et de la vélocité de la boule de travail
    const float2 position = readPosition(balls);
    const float2 velocity = readVelocity(balls);

    // Création des vecteurs de travail
    float2 positionOffset = position;
    float2 velocityOffset = velocity;

    // Vérification des collisions entre les boules
    for (int j = 0; j < balls.shape.y; j++) {
        if (j == get_global_id(0)) { // Cas où on compare la boule avec elle-même
            continue;
        }
        const float2 bPosition = readBallPosition(balls, j);
        const float2 bVelocity = readBallVelocity(balls, j);
        if (length(positionOffset - bPosition) <= 2) { // Si les boules se touchent, on effectue le calcul des nouvelles trajectoires
            float2 d = (float2) positionOffset - bPosition;
            positionOffset += d * (2 / length(d) - 1);

            float2 delta = (positionOffset - bPosition) / 2;

            float dot1to2 = dot(velocityOffset, delta);
            float dot2to1 = dot(bVelocity, delta);

            velocityOffset += delta * (dot2to1 - dot1to2);
        }
    }

    float4 position_result = rungeKutta(positionOffset, velocityOffset, alpha, time); // Calcul de Runge Kutta

    positionOffset = (float2) (position_result.x, position_result.y);
    velocityOffset = (float2) (position_result.z, position_result.w);

    if (checkHole(balls, dim, position, gameInformation, debug)) { // Vérification des trous
        return;
    }

    velocityOffset = updateWallCollision(positionOffset, velocityOffset, dim); // Vérification des collisions avec les murs

    setPosition(balls, positionOffset);
    setVelocity(balls, velocityOffset);
    writeData(gameInformation, 0, length(velocityOffset));
}

void compute_euler_explicit(struct DataContainer balls, struct BoardDimensions dim, float alpha, float time, struct DataContainer gameInformation, __global float* debug) {
    if (readData(balls, 4) == -1) {
        writeAbsoluteData(balls, 4, -1);
        writeAbsoluteData(balls, 0, readData(balls, 0));
        writeAbsoluteData(balls, 1, readData(balls, 1));
        writeAbsoluteData(balls, 2, readData(balls, 2));
        writeAbsoluteData(balls, 3, readData(balls, 3));
        return;
    }
    const float2 position = readPosition(balls);
    const float2 velocity = readVelocity(balls);

    float2 positionOffset = position;
    float2 velocityOffset = velocity;
    for (int j = 0; j < balls.shape.y; j++) {
        if (j == get_global_id(0)) {
            continue;
        }
        const float2 bPosition = readBallPosition(balls, j);
        const float2 bVelocity = readBallVelocity(balls, j);
        if (length(positionOffset - bPosition) <= 2) {
            float2 d = (float2) positionOffset - bPosition;
            positionOffset += d * (2 / length(d) - 1);

            float2 delta = (positionOffset - bPosition) / 2;

            float dot1to2 = dot(velocityOffset, delta);
            float dot2to1 = dot(bVelocity, delta);

            velocityOffset += delta * (dot2to1 - dot1to2);
        }
    }

    positionOffset = (float2) updatePosition(positionOffset, velocityOffset, time);
    velocityOffset = (float2) updateVelocity(velocityOffset, alpha);

    if (checkHole(balls, dim, position, gameInformation, debug)) {
        return;
    }

    velocityOffset = updateWallCollision(positionOffset, velocityOffset, dim);

    setPosition(balls, positionOffset);
    setVelocity(balls, velocityOffset);
    writeData(gameInformation, 0, length(velocityOffset));
}

void debugIndices(int offset, int4 indices, __global float* debug) {
    for (int i = 0; i < MAX_ARRAY_BUFFER_SIZE; i++) {
        debug[offset + i] = getComponent4(indices, i);
    }
}

// Fonction destinée à la recherche du meilleur coup (Runge Kutta)
__kernel void moveBestShotRungeKutta(__global float* balls, __global float* editBalls, int ballBufferSize, int ballAmount, float alpha, float height, float width, float time, __global float* gameInformation, __global float* debug, int anglePartition, int normPartition, short first) {
    const int4 ballShape = (int4) (ballBufferSize, ballAmount, anglePartition, normPartition);
    const int4 gameInfoShape = (int4) (2, anglePartition, normPartition, 0);

    struct DataContainer ballsData = {balls, editBalls, debug, ballShape, TYPE_MULTI_SIM_BALLS};
    struct DataContainer gameInfoData = {gameInformation, gameInformation, debug, gameInfoShape, TYPE_MULTI_SIM_INFO};
    struct BoardDimensions dim = {height, width};

    int angle = get_global_id(1);
    int norm = get_global_id(2);

    if (first == 1) {
        float2 position = readPosition(ballsData);
        float2 velocity = readVelocity(ballsData);
        if (get_global_id(0) == 0) {
            velocity = ((float2) (cos((float) angle * 2 * M_PI_F / anglePartition), sin((float) angle * 2 * M_PI_F / anglePartition))) * norm * 300.0f / normPartition;
        }
        setPosition(ballsData, position);
        setVelocity(ballsData, velocity);
        return;
    }
    if (get_global_id(0) == 0) {
        writeAbsoluteData(gameInfoData, 0, 0);
    }
    compute_runge_kutta(ballsData, dim, alpha, time, gameInfoData, debug);
}

// Fonction destinée au jeu avec interface graphique (Runge Kutta)
__kernel void moveRungeKutta(__global float* balls, __global float* editBalls, int ballBufferSize, int ballAmount, float alpha, float height, float width, float time, __global float* gameInformation, __global float* debug) {
    const int4 ballShape = (int4) (ballBufferSize, ballAmount, 0, 0);
    const int4 gameInfoShape = (int4) (2, 0, 0, 0);

    struct DataContainer ballsData = {balls, editBalls, debug, ballShape, TYPE_SINGLE_SIM_BALLS};
    struct DataContainer gameInfoData = {gameInformation, gameInformation, debug, gameInfoShape, TYPE_SINGLE_SIM_INFO};
    struct BoardDimensions dim = {height, width};

    compute_runge_kutta(ballsData, dim, alpha, time, gameInfoData, debug);
}

// Fonction destinée au jeu avec interface graphique (Euler explicite)
__kernel void moveEulerExplicit(__global float* balls, __global float* editBalls, int ballBufferSize, int ballAmount, float alpha, float height, float width, float time, __global float* gameInformation, __global float* debug) {
    const int4 ballShape = (int4) (ballBufferSize, ballAmount, 0, 0);
    const int4 gameInfoShape = (int4) (2, 0, 0, 0);

    struct DataContainer ballsData = {balls, editBalls, debug, ballShape, TYPE_SINGLE_SIM_BALLS};
    struct DataContainer gameInfoData = {gameInformation, gameInformation, debug, gameInfoShape, TYPE_SINGLE_SIM_INFO};
    struct BoardDimensions dim = {height, width};

    compute_euler_explicit(ballsData, dim, alpha, time, gameInfoData, debug);
}

// Permet de convertir des données du plateau (avec interface graphique) en données pour la recherche du meilleur coup
__kernel void copy_buffer(__global float* ballsShort, __global float* balls, int ballBufferSize, int ballAmount, int anglePartition, __global float* debug) {
    int i = get_global_id(0);
    int angle = get_global_id(1);
    int norm = get_global_id(2);
    struct DataContainer previousContainer = {ballsShort, ballsShort, debug, (int4) (ballBufferSize, 0, 0, 0), TYPE_SINGLE_SIM_BALLS};
    struct DataContainer currentContainer = {balls, balls, debug, (int4) (ballBufferSize, ballAmount, anglePartition, 0), TYPE_MULTI_SIM_BALLS};
    for (int j = 0; j < ballBufferSize; j++) {
        balls[getIndex(currentContainer, j, i)]
            = ballsShort[getIndex(previousContainer, j, i)];
    }
}