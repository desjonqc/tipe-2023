#ifdef cl_khr_fp64
    #pragma OPENCL EXTENSION cl_khr_fp64 : enable
#elif defined(cl_amd_fp64)
    #pragma OPENCL EXTENSION cl_amd_fp64 : enable
#endif

// Fonction simple de calcul de flops
__kernel void flops(__global float* values, int n) {
    int id = get_global_id(0);
    int id2 = get_global_id(1);
    values[id + n * id2] = id * 0.35f;
}