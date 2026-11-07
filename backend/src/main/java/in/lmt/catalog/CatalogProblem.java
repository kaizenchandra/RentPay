package in.lmt.catalog;

record CatalogProblemData(int status, String code, String detail, String field) {}
class CatalogProblem extends RuntimeException {
    final CatalogProblemData data;
    CatalogProblem(int status, String code, String detail, String field) {
        super(detail); data = new CatalogProblemData(status, code, detail, field);
    }
}
