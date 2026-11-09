package overload_api.auth;
import java.io.*;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.web.filter.OncePerRequestFilter;
/** Content-Lengthがない要求も含め、JSONの読込量を上限以内に制限する。 */
public class ApiRequestLimitFilter extends OncePerRequestFilter {
    private static final int MAX_BYTES=262_144;
    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        String path=ApiPaths.path(request);
        return !path.startsWith("/api/") || !(request.getMethod().equals("POST") || request.getMethod().equals("PUT"));
    }
    /** 大きな要求はJSONの解析やパスワード照合より前に413で拒否する。 */
    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException {
        if(request.getContentLengthLong()>MAX_BYTES) { reject(response);return; }
        byte[] body=request.getInputStream().readNBytes(MAX_BYTES+1);
        if(body.length>MAX_BYTES) {reject(response);return;}
        chain.doFilter(new HttpServletRequestWrapper(request) {
            @Override public ServletInputStream getInputStream() {
                var input=new ByteArrayInputStream(body);
                return new ServletInputStream() {
                    @Override public int read() {return input.read();}
                    @Override public int read(byte[] b,int offset,int length) {return input.read(b,offset,length);}
                    @Override public boolean isFinished() {return input.available()==0;}
                    @Override public boolean isReady() {return true;}
                    @Override public void setReadListener(ReadListener listener) {throw new UnsupportedOperationException("同期JSON API専用です");}
                };
            }
            @Override public BufferedReader getReader() {return new BufferedReader(new InputStreamReader(getInputStream(),StandardCharsets.UTF_8));}
        },response);
    }
    private static void reject(HttpServletResponse response) throws IOException {
        SecurityResponses.problem(response,413,"REQUEST_TOO_LARGE","送信するデータが上限を超えています");
    }
}
