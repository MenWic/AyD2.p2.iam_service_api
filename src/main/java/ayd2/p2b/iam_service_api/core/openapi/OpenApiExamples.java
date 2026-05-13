package ayd2.p2b.iam_service_api.core.openapi;

public final class OpenApiExamples {

    private OpenApiExamples() {
    }

    public static final String LOGIN_REQUEST = """
            {
              "email": "participant@domain.com",
              "password": "MyStrongPassword123"
            }
            """;

    public static final String REFRESH_REQUEST = """
            {
              "refreshToken": "eyJhbGciOiJIUzI1NiJ9.refresh.payload"
            }
            """;

    public static final String REGISTER_REQUEST = """
            {
              "email": "participant@domain.com",
              "password": "MyStrongPassword123",
              "fullName": "Jane Doe",
              "organization": "Code n Bugs",
              "phone": "555-0101",
              "personalId": "A123B",
              "photoUrl": "https://cdn.domain.com/photo.png"
            }
            """;

    public static final String USER_SUCCESS = """
            {
              "data": {
                "id": "11111111-1111-1111-1111-111111111111",
                "email": "participant@domain.com",
                "fullName": "Jane Doe",
                "organization": "Code n Bugs",
                "phone": "555-0101",
                "personalId": "A123B",
                "photoUrl": "https://cdn.domain.com/photo.png",
                "active": true,
                "roles": ["PARTICIPANT"]
              },
              "message": null
            }
            """;

    public static final String AUTH_SUCCESS = """
            {
              "data": {
                "accessToken": "eyJhbGciOiJIUzI1NiJ9.access.payload",
                "refreshToken": "eyJhbGciOiJIUzI1NiJ9.refresh.payload",
                "user": {
                  "id": "11111111-1111-1111-1111-111111111111",
                  "email": "participant@domain.com",
                  "fullName": "Jane Doe",
                  "organization": "Code n Bugs",
                  "phone": "555-0101",
                  "personalId": "A123B",
                  "photoUrl": "https://cdn.domain.com/photo.png",
                  "active": true,
                  "roles": ["PARTICIPANT"]
                }
              },
              "message": null
            }
            """;

    public static final String REFRESH_SUCCESS = """
            {
              "data": {
                "accessToken": "eyJhbGciOiJIUzI1NiJ9.new.access.payload"
              },
              "message": null
            }
            """;

    public static final String COMMITTEE_ELIGIBILITY_SUCCESS = """
            {
              "data": {
                "eligible": true
              },
              "message": null
            }
            """;

    public static final String PAGED_USERS_SUCCESS = """
            {
              "data": {
                "items": [
                  {
                    "id": "11111111-1111-1111-1111-111111111111",
                    "email": "participant@domain.com",
                    "fullName": "Jane Doe",
                    "organization": "Code n Bugs",
                    "phone": "555-0101",
                    "personalId": "A123B",
                    "photoUrl": "https://cdn.domain.com/photo.png",
                    "active": true,
                    "roles": ["PARTICIPANT"]
                  }
                ],
                "page": 0,
                "size": 20,
                "totalItems": 1,
                "totalPages": 1
              },
              "message": null
            }
            """;

    public static final String VALIDATION_ERROR = """
            {
              "type": "about:blank",
              "title": "Bad Request",
              "status": 400,
              "detail": "Validation failed",
              "code": "validation.failed",
              "errors": {
                "email": "must be a well-formed email address"
              }
            }
            """;

    public static final String INVALID_CREDENTIALS_ERROR = """
            {
              "type": "about:blank",
              "title": "Unauthorized",
              "status": 401,
              "detail": "Invalid credentials",
              "code": "auth.invalid_credentials"
            }
            """;

    public static final String TOKEN_INVALID_ERROR = """
            {
              "type": "about:blank",
              "title": "Unauthorized",
              "status": 401,
              "detail": "Invalid authentication",
              "code": "auth.token_invalid"
            }
            """;

    public static final String FORBIDDEN_ERROR = """
            {
              "type": "about:blank",
              "title": "Forbidden",
              "status": 403,
              "detail": "Access denied",
              "code": "auth.forbidden"
            }
            """;

    public static final String NOT_FOUND_ERROR = """
            {
              "type": "about:blank",
              "title": "Not Found",
              "status": 404,
              "detail": "Resource not found",
              "code": "resource.not_found"
            }
            """;

    public static final String CONFLICT_ERROR = """
            {
              "type": "about:blank",
              "title": "Conflict",
              "status": 409,
              "detail": "Resource conflict",
              "code": "resource.conflict"
            }
            """;

    public static final String INTERNAL_ERROR = """
            {
              "type": "about:blank",
              "title": "Internal Server Error",
              "status": 500,
              "detail": "Unexpected error",
              "code": "system.internal_error"
            }
            """;
}
