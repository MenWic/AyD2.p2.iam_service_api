package ayd2.p2b.iam_service_api.common.response;

public record ApiResponse<T>(T data, String message) {

    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data, null);
    }
}
