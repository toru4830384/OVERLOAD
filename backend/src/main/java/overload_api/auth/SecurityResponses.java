package overload_api.auth;
import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;
/** Securityフィルターの失敗も、画面が区別できるJSONで返す。 */
public final class SecurityResponses {
    private SecurityResponses() {}
    /** 固定メッセージだけを出力し、入力パスワードや例外内容を含めない。 */
    public static void problem(HttpServletResponse response, int status, String code, String detail) throws IOException {
        response.setStatus(status);
        response.setContentType("application/problem+json"); response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write("{\"status\":" + status + ",\"code\":\"" + code + "\",\"detail\":\"" + detail + "\"}");
    }
}
