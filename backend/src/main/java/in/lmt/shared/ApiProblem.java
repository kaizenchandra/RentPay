package in.lmt.shared;
public class ApiProblem extends RuntimeException {
    public final int status; public final String code; public final String field;
    public ApiProblem(int status, String code, String detail) { this(status,code,detail,null); }
    public ApiProblem(int status, String code, String detail, String field) { super(detail); this.status=status;this.code=code;this.field=field; }
}
