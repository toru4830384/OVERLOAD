package overload_api.auth;
import jakarta.servlet.http.HttpServletRequest;
/** Servletコンテナと同じ復号済みパスで保護範囲を判断する。 */
final class ApiPaths {
    private ApiPaths() {}
    static String path(HttpServletRequest request) {
        return org.springframework.web.util.UrlPathHelper.defaultInstance.getPathWithinApplication(request);
    }
}
