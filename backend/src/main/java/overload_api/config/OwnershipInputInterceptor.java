package overload_api.config;
import jakarta.servlet.http.*;
import org.springframework.web.servlet.HandlerInterceptor;
/** 旧クエリによる所有者指定を廃止し、クライアントの誤った移行を検出する。 */
public class OwnershipInputInterceptor implements HandlerInterceptor {
    /** @return 所有者IDをクエリに指定した要求はControllerへ進めない */
    @Override public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request.getParameter("user_id") != null || request.getParameter("userId") != null)
            throw new IllegalArgumentException("ユーザーIDは認証情報から決定します");
        return true;
    }
}
