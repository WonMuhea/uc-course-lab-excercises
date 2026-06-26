#include <stdio.h>
#include <stdlib.h>

/* * Comparison function required by qsort to sort integers in ascending order.
 */
int compare(const void *a, const void *b) {
    return (*(int*)a - *(int*)b);
}

/* * Calculates the mean by iterating through the array and maintaining 
 * a running sum.
 */
double calculate_mean(int *arr, int size) {
    double sum = 0;
    for (int i = 0; i < size; i++) sum += arr[i];
    return sum / size;
}

/* * Calculates the median by sorting the array in-place and returning 
 * the middle element(s).
 */
double calculate_median(int *arr, int size) {
    qsort(arr, size, sizeof(int), compare);
    if (size % 2 == 0) return (arr[size/2 - 1] + arr[size/2]) / 2.0;
    return (double)arr[size/2];
}

/* * Prints the mode(s) by iterating through the sorted array and 
 * tracking the frequency of consecutive numbers.
 */
void print_mode(int *arr, int size) {
    int max_count = 0;
    
    // First pass: Find the highest frequency
    for (int i = 0; i < size; i++) {
        int count = 1;
        while (i + 1 < size && arr[i] == arr[i+1]) { count++; i++; }
        if (count > max_count) max_count = count;
    }
    
    // Second pass: Identify numbers that match the max frequency
    printf("Modes: ");
    for (int i = 0; i < size; i++) {
        int count = 1;
        while (i + 1 < size && arr[i] == arr[i+1]) { count++; i++; }
        if (count == max_count) printf("%d ", arr[i]);
    }
    printf("\n");
}

int main() {
    int data[] = {3, 1, 4, 1, 5, 9, 2, 6, 5, 5};
    int size = sizeof(data) / sizeof(data[0]);
    
    printf("Mean: %.2f\n", calculate_mean(data, size));
    printf("Median: %.2f\n", calculate_median(data, size));
    print_mode(data, size);
    
    return 0;
}