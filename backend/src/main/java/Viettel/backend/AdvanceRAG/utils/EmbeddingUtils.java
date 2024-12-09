package Viettel.backend.AdvanceRAG.utils;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import java.util.List;
import java.util.Map;

public class EmbeddingUtils {

    // Combine embeddings using weighted sums
    public static void combineEmbeddings(
            List<Map<String, Object>> documents,
            List<String> embeddingFields,
            List<Double> weights,
            String combinedField) {

        if (embeddingFields.size() != weights.size()) {
            throw new IllegalArgumentException("The number of embedding fields must match the number of weights.");
        }

        // Normalize weights to sum to 1
        double weightSum = weights.stream().mapToDouble(Double::doubleValue).sum();
        List<Double> normalizedWeights = weights.stream()
                .map(w -> w / weightSum)
                .toList();

        for (Map<String, Object> doc : documents) {
            RealVector combinedEmbedding = null;

            for (int i = 0; i < embeddingFields.size(); i++) {
                String field = embeddingFields.get(i);
                Double weight = normalizedWeights.get(i);

                float[] embeddingArray = (float[]) doc.get(field);
                if (embeddingArray != null) {
                    RealVector embeddingVector = new ArrayRealVector(toDoubleArray(embeddingArray));
                    embeddingVector.mapMultiplyToSelf(weight);

                    if (combinedEmbedding == null) {
                        combinedEmbedding = embeddingVector;
                    } else {
                        combinedEmbedding = combinedEmbedding.add(embeddingVector);
                    }
                }
            }

            if (combinedEmbedding != null) {
                doc.put(combinedField, toFloatArray(combinedEmbedding.toArray()));
            }
        }
    }

    // Convert float[] to double[]
    private static double[] toDoubleArray(float[] array) {
        double[] doubleArray = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            doubleArray[i] = array[i];
        }
        return doubleArray;
    }

    // Convert double[] to float[]
    private static float[] toFloatArray(double[] array) {
        float[] floatArray = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            floatArray[i] = (float) array[i];
        }
        return floatArray;
    }
}

